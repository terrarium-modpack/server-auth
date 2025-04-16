package dev.optimistic.serverauth.mixin.server;

import dev.optimistic.serverauth.ducks.IdOverrideHolder;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin$IdOverrideHolderImpl implements IdOverrideHolder {
    @Unique
    private @Nullable UUID id;

    @Override
    public void serverauth$setIdOverride(@NotNull UUID id) {
        this.id = id;
    }

    @Override
    public @Nullable UUID serverauth$getIdOverride() {
        return this.id;
    }
}
