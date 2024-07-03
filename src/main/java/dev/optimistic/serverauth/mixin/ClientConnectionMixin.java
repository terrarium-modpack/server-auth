package dev.optimistic.serverauth.mixin;

import com.google.common.base.Objects;
import dev.optimistic.serverauth.ClientConstants;
import dev.optimistic.serverauth.Constants;
import dev.optimistic.serverauth.ducks.TaintHolder;
import dev.optimistic.serverauth.keys.PrivateKeyHolder;
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

        UUID id = ClientConstants.INSTANCE.getUuid();
        if (id == null) return;

        if (!Objects.equal(id, Constants.INSTANCE.deserializeServerAuthId(handshakePacket))) return;
        tainted = true;
        ClientConstants.INSTANCE.getPrivateKeyThreadLocal().set(PrivateKeyHolder.INSTANCE.getOrInitialize(id));
    }
}
