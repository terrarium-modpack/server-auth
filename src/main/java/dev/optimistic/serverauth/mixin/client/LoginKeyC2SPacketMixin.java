package dev.optimistic.serverauth.mixin.client;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.serverauth.ClientConstants;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.security.Key;
import java.security.PrivateKey;

@Mixin(LoginKeyC2SPacket.class)
public abstract class LoginKeyC2SPacketMixin {
    @Unique
    private boolean isNonce;

    @WrapOperation(method = "<init>(Ljavax/crypto/SecretKey;Ljava/security/PublicKey;[B)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/encryption/NetworkEncryptionUtils;encrypt(Ljava/security/Key;[B)[B"))
    private byte[] init$key(Key key, byte[] data, Operation<byte[]> original) {
        // both the nonce and the secret are encrypted with the server's public key, however we only want to touch the secret
        if (isNonce) return original.call(key, data);
        isNonce = true;
        PrivateKey privateKey = ClientConstants.INSTANCE.getPrivateKey().read();
        if (privateKey == null) return original.call(key, data);

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

        return original.call(key, ciphertext);
    }
}
