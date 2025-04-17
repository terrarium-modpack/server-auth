package dev.optimistic.serverauth.mixin;

import com.google.common.base.Objects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.serverauth.ClientConstants;
import dev.optimistic.serverauth.Constants;
import dev.optimistic.serverauth.ducks.TaintHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.security.PrivateKey;
import java.util.UUID;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements TaintHolder {
    @Unique
    private boolean tainted;

    @Override
    public boolean serverauth$isTainted() {
        return tainted;
    }

    @Inject(method = "doSendPacket", at = @At("HEAD"))
    private void doSendPacket(Packet<?> packet, PacketSendListener sendListener, boolean flush, CallbackInfo ci) {
        if (!(packet instanceof ClientIntentionPacket intentionPacket)) return;

        PrivateKey privateKey = ClientConstants.INSTANCE.getPrivateKey().read();
        if (privateKey == null) return;

        // the uuid will always be non-null if the private key is non-null
        UUID id = ClientConstants.INSTANCE.getUuid();
        if (!Objects.equal(id, Constants.INSTANCE.deserializeServerAuthId(intentionPacket))) return;

        tainted = true;
    }

    // initiateServerboundConnection lambda
    @WrapOperation(method = "method_52900", at = @At(value = "NEW", target = "(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;"))
    private ClientIntentionPacket run$newIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention, Operation<ClientIntentionPacket> original) {
        ClientIntentionPacket orig = original.call(protocolVersion, hostName, port, intention);
        UUID id = ClientConstants.INSTANCE.getUuid();

        if (id == null)
            return orig;

        return ClientConstants.INSTANCE.modifyIntentionPacket(id, orig);
    }
}
