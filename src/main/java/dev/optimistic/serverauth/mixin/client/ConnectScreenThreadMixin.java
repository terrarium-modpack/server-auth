package dev.optimistic.serverauth.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.serverauth.ClientConstants;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(targets = "net/minecraft/client/gui/screen/ConnectScreen$1")
public abstract class ConnectScreenThreadMixin {
    @WrapOperation(method = "run", at = @At(value = "NEW", target = "(Ljava/lang/String;ILnet/minecraft/network/NetworkState;)Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;"))
    private HandshakeC2SPacket run$newHandshakePacket(String address, int port, NetworkState intendedState, Operation<HandshakeC2SPacket> original) {
        HandshakeC2SPacket orig = original.call(address, port, intendedState);
        UUID id = ClientConstants.INSTANCE.getUuid();

        if (id == null)
            return orig;

        return ClientConstants.INSTANCE.modifyHandshakePacket(id, orig);
    }
}
