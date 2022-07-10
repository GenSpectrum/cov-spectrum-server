package ch.ethz.covspectrum.util

import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * A simple, in-memory cache that is thread-safe
 */
class FifoCache<K, V>(private val maxSize: Int) {
    class LimitedLinkedHashMap<K, V>(private val maxSize: Int): LinkedHashMap<K, V>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    private val innerMap = Collections.synchronizedMap(LimitedLinkedHashMap<K, V>(maxSize))

    fun put(key: K, value: V): V? {
        return innerMap.put(key, value)
    }

    fun get(key: K): V? {
        return innerMap.get(key)
    }
}
