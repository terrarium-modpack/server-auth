package dev.optimistic.serverauth

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.handshake.ClientIntentionPacket
import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.util.*

object Constants {
    private const val MOD_ID = "server-auth"
    val logger = LoggerFactory.getLogger(MOD_ID)!!
    val keyDir = FabricLoader.getInstance().configDir.resolve(MOD_ID).resolve("keys")
    val keyFactory = KeyFactory.getInstance("RSA")

    private const val UNDASHED_ID_REGEX_TEMPLATE =
        "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})"
    private val modifiedServerAddressRegex =
        Regex("^\\u0000server-auth\\u0000$UNDASHED_ID_REGEX_TEMPLATE\$")
    val publicKeyFileNameRegex = Regex("^$UNDASHED_ID_REGEX_TEMPLATE\\.pub$")
    val privateKeyFileNameRegex = Regex("^$UNDASHED_ID_REGEX_TEMPLATE$")

    fun getUuidFromUndashed(regex: Regex, input: String): UUID? {
        val match = regex.matchEntire(input) ?: return null
        return UUID.fromString("${match.groupValues[1]}-${match.groupValues[2]}-${match.groupValues[3]}-${match.groupValues[4]}-${match.groupValues[5]}")
    }

    fun deserializeServerAuthId(intentionPacket: ClientIntentionPacket): UUID? =
        getUuidFromUndashed(modifiedServerAddressRegex, intentionPacket.hostName)
}