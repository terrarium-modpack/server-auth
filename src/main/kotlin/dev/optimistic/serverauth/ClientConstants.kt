package dev.optimistic.serverauth

import dev.optimistic.serverauth.keys.PrivateKeyHolder.getOrInitialize
import dev.optimistic.serverauth.util.MutableLazy
import net.minecraft.network.protocol.handshake.ClientIntentionPacket
import java.security.PrivateKey
import java.util.*

object ClientConstants {
    val uuid: UUID? = System.getProperty("uuid")?.run { UUID.fromString(this) }
    val privateKey: MutableLazy<PrivateKey> = MutableLazy { uuid?.run { return@run getOrInitialize(this) } }

    init {
        if (uuid == null) Constants.logger.warn("uuid property unset, server-auth will not function")
    }

    fun modifyIntentionPacket(serverAuthId: UUID, intentionPacket: ClientIntentionPacket): ClientIntentionPacket =
        ClientIntentionPacket(
            intentionPacket.protocolVersion,
            "\u0000server-auth\u0000${serverAuthId.toString().replace("-", "")}",
            intentionPacket.port,
            intentionPacket.intention
        )
}