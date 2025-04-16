package dev.optimistic.serverauth.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.serverauth.ClientConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(targets = "net/minecraft/client/gui/screens/ConnectScreen$1")
public abstract class ConnectScreenThreadMixin {
    @WrapOperation(method = "run", at = @At(value = "NEW", target = "(Ljava/lang/String;ILnet/minecraft/network/ConnectionProtocol;)Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;"))
    private ClientIntentionPacket run$newIntentionPacket(String hostName, int port, ConnectionProtocol intention, Operation<ClientIntentionPacket> original) {
        ClientIntentionPacket orig = original.call(hostName, port, intention);
        UUID id = ClientConstants.INSTANCE.getUuid();

        if (id == null)
            return orig;

        return ClientConstants.INSTANCE.modifyIntentionPacket(id, orig);
    }
}
