package dev.optimistic.serverauth.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import dev.optimistic.serverauth.ducks.TaintHolder;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class ClientHandshakePacketListenerImplMixin {
    @Shadow
    @Final
    private Connection connection;

    @WrapWithCondition(method = "authenticateServer", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;joinServer(Ljava/util/UUID;Ljava/lang/String;Ljava/lang/String;)V", remap = false))
    private boolean joinServerSession(MinecraftSessionService instance, UUID uuid, String accessToken, String serverHash) {
        return !((TaintHolder) connection).serverauth$isTainted();
    }
}
