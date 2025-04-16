package dev.optimistic.serverauth

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.optimistic.serverauth.keys.PublicKeyHolder
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class Initializer : DedicatedServerModInitializer, CommandRegistrationCallback {
    override fun onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(this)
    }

    override fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        context: CommandBuildContext,
        selection: Commands.CommandSelection
    ) {
        dispatcher.register(
            Commands.literal("serverauth")
                .requires { it.hasPermission(4) }
                .then(Commands.literal("reload")
                    .executes {
                        val previous = PublicKeyHolder.getLoaded()
                        PublicKeyHolder.initialize()
                        val new = PublicKeyHolder.getLoaded()
                        it.source.sendSystemMessage(Component.literal("Reloaded keys for a load count delta of ${new - 
                                previous}"))
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
        )
    }
}