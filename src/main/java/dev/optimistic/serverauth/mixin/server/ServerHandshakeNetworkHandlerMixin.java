package dev.optimistic.serverauth.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.optimistic.serverauth.Constants;
import dev.optimistic.serverauth.ducks.IdOverrideHolder;
import dev.optimistic.serverauth.keys.PublicKeyHolder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.security.PublicKey;
import java.util.UUID;

@Mixin(ServerHandshakeNetworkHandler.class)
public abstract class ServerHandshakeNetworkHandlerMixin {
    @WrapOperation(method = "onHandshake", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;)Lnet/minecraft/server/network/ServerLoginNetworkHandler;"))
    private ServerLoginNetworkHandler onHandshake$newServerLoginNetworkHandler(MinecraftServer server, ClientConnection connection,
                                                                               Operation<ServerLoginNetworkHandler> original,
                                                                               @Local(argsOnly = true) HandshakeC2SPacket packet) {
        ServerLoginNetworkHandler handler = original.call(server, connection);
        UUID override = Constants.INSTANCE.deserializeServerAuthId(packet);
        if (override != null) {
            ((IdOverrideHolder) handler).serverauth$setIdOverride(override);
            PublicKey key = PublicKeyHolder.INSTANCE.getKey(override);
            if (key == null) {
                connection.send(new LoginDisconnectS2CPacket(Text.literal("Unregistered UUID. Please go to your closest immigration office to register your stay")));
                throw new IllegalStateException("Unregistered UUID. Please go to your closest immigration office to register your stay");
            }
        }

        return handler;
    }
}
