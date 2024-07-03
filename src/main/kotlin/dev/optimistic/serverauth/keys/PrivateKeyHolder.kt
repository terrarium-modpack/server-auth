package dev.optimistic.serverauth.keys

import dev.optimistic.serverauth.Constants
import java.nio.file.Files
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object PrivateKeyHolder : KeyHolder<PrivateKey, PKCS8EncodedKeySpec>(
    Constants.keyDir.resolve("private"),
    Constants.privateKeyFileNameRegex,
    { PKCS8EncodedKeySpec(it) },
    { Constants.keyFactory.generatePrivate(it) }
) {
    fun getOrInitialize(key: UUID): PrivateKey {
        val existingKey = getKey(key)
        if (existingKey != null) return existingKey
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(1024)
        val keyPair = keyPairGenerator.genKeyPair()
        val undashed = key.toString().replace("-", "")
        Files.write(path.resolve(undashed), keyPair.private.encoded)
        Files.write(PublicKeyHolder.path.resolve("$undashed.pub"), keyPair.public.encoded)
        initialize()
        return getKey(key)!!
    }
}