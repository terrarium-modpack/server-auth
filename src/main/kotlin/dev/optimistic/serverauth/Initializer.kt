package dev.optimistic.serverauth

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.optimistic.serverauth.keys.PublicKeyHolder
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

class Initializer : DedicatedServerModInitializer, CommandRegistrationCallback {
    override fun onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(this)
    }

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.register(
            CommandManager.literal("serverauth")
                .requires { it.hasPermissionLevel(4) }
                .then(CommandManager.literal("reload")
                    .executes {
                        val previous = PublicKeyHolder.getLoaded()
                        PublicKeyHolder.initialize()
                        val new = PublicKeyHolder.getLoaded()
                        it.source.sendMessage(Text.literal("Reloaded keys for a load count delta of ${new - previous}"))
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
        )
    }
}