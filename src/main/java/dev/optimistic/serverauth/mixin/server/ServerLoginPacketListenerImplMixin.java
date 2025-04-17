package dev.optimistic.serverauth.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.optimistic.serverauth.ducks.IdOverrideHolder;
import dev.optimistic.serverauth.keys.PublicKeyHolder;
import dev.optimistic.serverauth.mixin.accessor.ServerboundKeyPacketAccessor;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
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
import java.util.Set;
import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @Unique
    private final IdOverrideHolder duck = (IdOverrideHolder) this;
    @Shadow
    @Final
    MinecraftServer server;

    @WrapOperation(method = "handleKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/login/ServerboundKeyPacket;getSecretKey(Ljava/security/PrivateKey;)Ljavax/crypto/SecretKey;"))
    private SecretKey handleKey$decryptSecretKey(ServerboundKeyPacket instance, PrivateKey privateKey, Operation<SecretKey> original) {
        UUID override = duck.serverauth$getIdOverride();
        if (override == null) return original.call(instance, privateKey);
        PublicKey key = PublicKeyHolder.INSTANCE.getKey(override);
        if (key == null)
            throw new IllegalStateException("Unregistered public key");

        ServerboundKeyPacketAccessor accessor = (ServerboundKeyPacketAccessor) instance;
        byte[] firstDecrypted;
        try {
            firstDecrypted = Crypt.decryptUsingKey(server.getKeyPair().getPrivate(), accessor.getKeyBytes());
        } catch (CryptException e) {
            throw new IllegalStateException("Failed to decrypt first layer of secret key encryption", e);
        }

        byte[] body;
        try {
            body = Crypt.decryptUsingKey(key, firstDecrypted);
        } catch (CryptException e) {
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

    @Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
    public abstract static class AuthenticationThreadMixin {
        @Shadow
        @Final
        ServerLoginPacketListenerImpl field_14176;

        @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;hasJoinedServer(Ljava/lang/String;Ljava/lang/String;Ljava/net/InetAddress;)Lcom/mojang/authlib/yggdrasil/ProfileResult;", remap = false))
        private ProfileResult run$hasJoinedServer(MinecraftSessionService instance, String profileName, String serverId, InetAddress inetAddress, Operation<ProfileResult> original) {
            UUID id = ((IdOverrideHolder) field_14176).serverauth$getIdOverride();
            if (id == null) return original.call(instance, profileName, serverId, inetAddress);

            final GameProfile newProfile = new GameProfile(id, profileName);

            // hasJoined request returns signed data, we should too
            final ProfileResult fetchedProfile = instance.fetchProfile(id, true);
            if (fetchedProfile != null) {
                // mojang after not exposing properties in constructor
                newProfile.getProperties().putAll(fetchedProfile.profile().getProperties());
            }

            return new ProfileResult(newProfile, Set.of());
        }
    }
}
