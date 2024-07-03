package dev.optimistic.serverauth.mixin.client;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.serverauth.ClientConstants;
import dev.optimistic.serverauth.Constants;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.security.Key;
import java.security.PrivateKey;

@Mixin(LoginKeyC2SPacket.class)
public abstract class LoginKeyC2SPacketMixin {
    @WrapOperation(method = "<init>(Ljavax/crypto/SecretKey;Ljava/security/PublicKey;[B)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/encryption/NetworkEncryptionUtils;encrypt(Ljava/security/Key;[B)[B"))
    private byte[] init$key(Key key, byte[] data, Operation<byte[]> original) {
        ThreadLocal<PrivateKey> privateKeyThreadLocal = ClientConstants.INSTANCE.getPrivateKeyThreadLocal();
        PrivateKey privateKey = privateKeyThreadLocal.get();
        if (privateKey == null) return original.call(key, data);
        privateKeyThreadLocal.remove();

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeLong(System.currentTimeMillis());
        output.write(data);
        byte[] plaintext = output.toByteArray();
        byte[] ciphertext;

        try {
            ciphertext = NetworkEncryptionUtils.encrypt(privateKey, plaintext);
        } catch (NetworkEncryptionException e) {
            throw new IllegalStateException("Encryption failed");
        }

        Constants.INSTANCE.getLogger().info("{}", ciphertext.length);

        return original.call(key, ciphertext);
    }
}
