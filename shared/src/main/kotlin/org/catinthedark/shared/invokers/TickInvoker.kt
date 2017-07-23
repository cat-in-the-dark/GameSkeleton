package org.catinthedark.shared.invokers

/**
 * This kind of invoker runs in a single thread but do not use some schedulers.
 * Instead, it receives time ticks and handle the timeouts, defers and so on.
 * It should be used in UI systems, because this kind of systems always
 * have it's own inner eventloop. It would be better to reuse loop instead of threading.
 *
 * WARNING!
 * This invoker is not thread safe!!!
 * Use it only in a single thread!
 */
class TickInvoker : DeferrableInvoker {
    private var time: Long = 0L
    private val queue: MutableList<Holder> = mutableListOf()

    /**
     * Call this method in the event loop on every loop.
     * Receives [delta] in ms to calculate system time units for timed events.
     * This call invoke all queued events that must be run at this time.
     */
    fun run(delta: Long) {
        time += delta
        queue.filterNot {
            it.isPeriodic
        }.filter {
            it.nextCallTime <= time
        }.apply {
            forEach { it.func() }
            queue.removeAll(this)
        }

        queue.filter {
            it.isPeriodic
        }.filter {
            it.nextCallTime <= time
        }.forEach {
            it.func()
            it.next()
        }
    }

    /**
     * Put an [func] to the queue which will be called on next [run] call.
     * Invoke will never call the event at the moment of time when #invoke is called.
     * Literally say, [invoke] is the synonym for [defer] with ZERO delay.
     */
    override fun invoke(func: () -> Unit) {
        queue.add(Holder(0L, func, false))
    }


    /**
     * Cancel all funcs in the [queue] and reset the [time].
     */
    override fun shutdown() {
        queue.clear()
        time = 0L
    }

    /**
     * Put an [func] to queue that will be called after timeout in ms.
     * Event will be never called earlier then currentTime+timeout,
     * but might be called later.
     *
     * You can cancel the event before it'll be called by invoking the callback
     */
    override fun defer(func: () -> Unit, timeout: Long): () -> Unit {
        with(Holder(timeout, func, false)) {
            queue.add(this)
            return {
                queue.remove(this)
            }
        }
    }

    /**
     * Works like the [defer] method
     * but [func] will be called every [timeout] in ms until it is canceled.
     *
     * You can cancel the [func] by invoking the returned callback.
     */
    override fun periodic(func: () -> Unit, timeout: Long): () -> Unit {
        with(Holder(timeout, func, true)) {
            queue.add(this)
            return {
                queue.remove(this)
            }
        }
    }

    private inner class Holder(val timeout: Long, val func: () -> Unit, val isPeriodic: Boolean = false) {
        var nextCallTime: Long = time + timeout
            private set

        fun next(): Holder {
            nextCallTime += timeout
            return this
        }
    }
}