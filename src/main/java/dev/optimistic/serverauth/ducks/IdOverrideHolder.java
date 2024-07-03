package dev.optimistic.serverauth.ducks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IdOverrideHolder {
    @Nullable
    UUID serverauth$getIdOverride();

    void serverauth$setIdOverride(@NotNull UUID id);
}
