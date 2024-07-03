package dev.optimistic.serverauth.mixin.accessor;

import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoginKeyC2SPacket.class)
public interface LoginKeyC2SPacketAccessor {
    @Accessor
    byte[] getEncryptedSecretKey();
}
