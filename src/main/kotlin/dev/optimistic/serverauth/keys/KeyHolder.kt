package dev.optimistic.serverauth.keys

import dev.optimistic.serverauth.Constants
import java.nio.file.Files
import java.nio.file.Path
import java.security.Key
import java.security.spec.EncodedKeySpec
import java.util.*

abstract class KeyHolder<K : Key, S : EncodedKeySpec>(
    val path: Path,
    private val regex: Regex,
    private val specGenerator: (ByteArray) -> S,
    private val keyGenerator: (S) -> K
) {
    private val idToKeyMap: MutableMap<UUID, K> = Collections.synchronizedMap(HashMap())

    init {
        initialize()
    }

    fun initialize() {
        idToKeyMap.clear()

        if (Files.notExists(path)) {
            Files.createDirectories(path)
            return
        }

        for (path in Files.newDirectoryStream(path)) {
            val fileName = path.fileName.toString()
            if (regex.matchEntire(fileName) === null)
                continue

            val id = Constants.getUuidFromUndashed(regex, fileName)!!
            val data = Files.readAllBytes(path)
            val publicKey = try {
                keyGenerator(specGenerator(data))
            } catch (e: Exception) {
                Constants.logger.warn("Error loading key from path $path", e)
                continue
            }

            idToKeyMap[id] = publicKey
        }
    }

    fun getKey(id: UUID) = idToKeyMap[id]
    fun getLoaded() = this.idToKeyMap.size
}