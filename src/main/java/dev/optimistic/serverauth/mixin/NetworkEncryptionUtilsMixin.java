package dev.optimistic.serverauth.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.security.KeyPairGenerator;

@Mixin(NetworkEncryptionUtils.class)
public abstract class NetworkEncryptionUtilsMixin {
    @WrapOperation(method = "generateServerKeyPair", at = @At(value = "INVOKE", target = "Ljava/security/KeyPairGenerator;initialize(I)V"))
    private static void generateServerKeyPair$initialize(KeyPairGenerator instance, int keysize, Operation<Void> original) {
        original.call(instance, 2048);
    }
}
