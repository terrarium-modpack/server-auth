package dev.optimistic.serverauth.mixin;

import com.google.common.base.Objects;
import dev.optimistic.serverauth.ClientConstants;
import dev.optimistic.serverauth.Constants;
import dev.optimistic.serverauth.ducks.TaintHolder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.security.PrivateKey;
import java.util.UUID;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements TaintHolder {
    @Unique
    private boolean tainted;

    @Override
    public boolean serverauth$isTainted() {
        return tainted;
    }

    @Inject(method = "sendInternal", at = @At("HEAD"))
    private void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, NetworkState packetState, NetworkState currentState, CallbackInfo ci) {
        if (!(packet instanceof HandshakeC2SPacket handshakePacket)) return;

        PrivateKey privateKey = ClientConstants.INSTANCE.getPrivateKey().read();
        if (privateKey == null) return;

        // the uuid will always be non-null if the private key is non-null
        UUID id = ClientConstants.INSTANCE.getUuid();
        if (!Objects.equal(id, Constants.INSTANCE.deserializeServerAuthId(handshakePacket))) return;

        tainted = true;
    }
}
