package dev.optimistic.serverauth.mixin.accessor;

import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundKeyPacket.class)
public interface ServerboundKeyPacketAccessor {
    @Accessor("keybytes")
    byte[] getKeyBytes();
}
