package dev.optimistic.serverauth.util

data class MutableLazy<T>(private val init: () -> T?) {
    private var inner: T? = null
    private val lock = Any()

    fun read(): T? {
        synchronized(lock) {
            if (inner == null) inner = init()
            return inner
        }
    }

    // for changing private keys on the fly
    fun write(newValue: T?) {
        synchronized(lock) {
            inner = newValue
        }
    }
}