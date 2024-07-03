package dev.optimistic.serverauth.keys

import dev.optimistic.serverauth.Constants
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

object PublicKeyHolder : KeyHolder<PublicKey, X509EncodedKeySpec>(
    Constants.keyDir.resolve("public"),
    Constants.publicKeyFileNameRegex,
    { X509EncodedKeySpec(it) },
    { Constants.keyFactory.generatePublic(it) }
)