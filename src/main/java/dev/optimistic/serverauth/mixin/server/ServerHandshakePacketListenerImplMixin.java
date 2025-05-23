package dev.optimistic.serverauth.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.optimistic.serverauth.Constants;
import dev.optimistic.serverauth.ducks.IdOverrideHolder;
import dev.optimistic.serverauth.keys.PublicKeyHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.security.PublicKey;
import java.util.UUID;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {
    @Unique
    private static final IllegalStateException EXCEPTION = new IllegalStateException("Unregistered public key");

    @WrapOperation(method = "beginLogin", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Z)Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;"))
    private ServerLoginPacketListenerImpl handleIntention$newServerLoginListener(MinecraftServer server, Connection connection, boolean transferred,
                                                                                 Operation<ServerLoginPacketListenerImpl> original,
                                                                                 @Local(argsOnly = true) ClientIntentionPacket packet) {
        ServerLoginPacketListenerImpl handler = original.call(server, connection, transferred);
        UUID override = Constants.INSTANCE.deserializeServerAuthId(packet);
        if (override != null) {
            ((IdOverrideHolder) handler).serverauth$setIdOverride(override);
            PublicKey key = PublicKeyHolder.INSTANCE.getKey(override);
            if (key == null) {
                connection.send(new ClientboundLoginDisconnectPacket(Component.translatable("multiplayer.disconnect.unregistered_public_key")));
                throw EXCEPTION;
            }
        }

        return handler;
    }
}
