package org.catinthedark.shared

/**
 * TimeCache updates the value by [func] only when it's time to update [cache].
 */
class TimeCache<out T>(
        private val func: () -> T,
        val syncTime: Long // in ms
) {
    private var lastSync: Long = 0
    private var cache: T? = null

    fun get(): T? {
        val now = System.currentTimeMillis()
        if (now - lastSync >= syncTime) {
            lastSync = now
            cache = func()
        }
        return cache
    }
}