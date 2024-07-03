package dev.optimistic.serverauth.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import dev.optimistic.serverauth.ducks.TaintHolder;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    @Shadow
    @Final
    private ClientConnection connection;

    @WrapOperation(method = "joinServerSession", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;joinServer(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/lang/String;)V", remap = false))
    private void joinServerSession(MinecraftSessionService instance, GameProfile gameProfile, String accessToken, String serverId, Operation<Void> original) {
        if (!((TaintHolder) connection).serverauth$isTainted())
            original.call(instance, gameProfile, accessToken, serverId);
    }
}
