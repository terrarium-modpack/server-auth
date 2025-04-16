package dev.optimistic.serverauth.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import dev.optimistic.serverauth.ducks.TaintHolder;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class ClientHandshakePacketListenerImplMixin {
    @Shadow
    @Final
    private Connection connection;

    @WrapOperation(method = "authenticateServer", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;joinServer(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/lang/String;)V", remap = false))
    private void joinServerSession(MinecraftSessionService instance, GameProfile gameProfile, String accessToken, String serverId, Operation<Void> original) {
        if (!((TaintHolder) connection).serverauth$isTainted())
            original.call(instance, gameProfile, accessToken, serverId);
    }
}
