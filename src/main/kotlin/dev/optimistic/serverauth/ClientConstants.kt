package dev.optimistic.serverauth

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket
import java.security.PrivateKey
import java.util.*

object ClientConstants {
    val uuid: UUID? = System.getProperty("uuid")?.run { UUID.fromString(this) }
    val privateKeyThreadLocal: ThreadLocal<PrivateKey> = ThreadLocal()

    init {
        if (uuid == null) Constants.logger.warn("uuid property unset, server-auth will not function")
    }

    fun modifyHandshakePacket(serverAuthId: UUID, handshakePacket: HandshakeC2SPacket): HandshakeC2SPacket =
        HandshakeC2SPacket(
            "\u0000server-auth\u0000${serverAuthId.toString().replace("-", "")}",
            handshakePacket.port,
            handshakePacket.intendedState
        )
}