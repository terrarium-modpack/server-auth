package dev.optimistic.serverauth.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import dev.optimistic.serverauth.ducks.IdOverrideHolder;
import dev.optimistic.serverauth.keys.PublicKeyHolder;
import dev.optimistic.serverauth.mixin.accessor.LoginKeyC2SPacketAccessor;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Unique
    private final IdOverrideHolder duck = (IdOverrideHolder) this;
    @Shadow
    @Final
    MinecraftServer server;

    @WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/login/LoginKeyC2SPacket;decryptSecretKey(Ljava/security/PrivateKey;)Ljavax/crypto/SecretKey;"))
    private SecretKey onKey$decryptSecretKey(LoginKeyC2SPacket instance, PrivateKey privateKey, Operation<SecretKey> original) {
        UUID override = duck.serverauth$getIdOverride();
        if (override == null) return original.call(instance, privateKey);
        PublicKey key = PublicKeyHolder.INSTANCE.getKey(override);
        if (key == null)
            throw new IllegalStateException("Unregistered UUID. Please go to your closest immigration office to register your stay");

        LoginKeyC2SPacketAccessor accessor = (LoginKeyC2SPacketAccessor) instance;
        byte[] firstDecrypted;
        try {
            firstDecrypted = NetworkEncryptionUtils.decrypt(server.getKeyPair().getPrivate(), accessor.getEncryptedSecretKey());
        } catch (NetworkEncryptionException e) {
            throw new IllegalStateException("Failed to decrypt first layer of secret key encryption", e);
        }

        byte[] body;
        try {
            body = NetworkEncryptionUtils.decrypt(key, firstDecrypted);
        } catch (NetworkEncryptionException e) {
            throw new IllegalStateException("Failed to decrypt second layer of secret key encryption", e);
        }

        if (body.length < 8) throw new IllegalArgumentException("Secret key body too small to contain timestamp");
        ByteBuffer buffer = ByteBuffer.wrap(body);
        long timestamp = buffer.getLong();
        long actualTimestamp = System.currentTimeMillis();

        // TODO: Allow forgiveness to be configured
        if ((Math.abs(actualTimestamp - timestamp) / 1000) > 10)
            throw new IllegalStateException("Too much clock skew, please sync your clock or ask the sysadmin to sync the server's");

        int secretKeyLen = body.length - 8;
        byte[] secretKey = new byte[secretKeyLen];
        buffer.get(8, secretKey);
        return new SecretKeySpec(secretKey, "AES");
    }

    @Mixin(targets = "net/minecraft/server/network/ServerLoginNetworkHandler$1")
    public abstract static class AuthenticationThreadMixin {
        @Shadow
        @Final
        ServerLoginNetworkHandler field_14176;

        @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;hasJoinedServer(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/net/InetAddress;)Lcom/mojang/authlib/GameProfile;", remap = false))
        private GameProfile run$hasJoinedServer(MinecraftSessionService instance, GameProfile gameProfile, String data, InetAddress inetAddress, Operation<GameProfile> original) {
            UUID id = ((IdOverrideHolder) field_14176).serverauth$getIdOverride();
            if (id == null) return original.call(instance, gameProfile, data, inetAddress);
            return new GameProfile(id, gameProfile.getName());
        }
    }
}
