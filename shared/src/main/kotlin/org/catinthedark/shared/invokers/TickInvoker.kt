package org.catinthedark.shared.invokers

import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * This kind of invoker runs in a single thread but do not use some schedulers.
 * Instead, it receives time ticks and handleы the timeouts, defers and so on.
 * It should be used in UI systems, because this kind of systems always
 * have it's own inner eventloop.
 * It would be better to reuse loop instead of threading.
 *
 * This invoker is thread safe.
 * So you can put events from any thread.
 */
class TickInvoker : DeferrableInvoker {
    private val log = LoggerFactory.getLogger(this::class.java)
    private var time: Long = 0L
    private val queue: MutableList<Holder> = mutableListOf()
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Call this method in the event loop on every loop.
     * Receives [delta] in ms, for example, to calculate system time for timed events.
     * This call invokes all queued funcs that must be run at this time.
     */
    fun run(delta: Long) {
        time += delta

        lock.writeLock().withLock {
            processDefer()
        }
        // break big lock into two small, like unlock eventloop
        lock.writeLock().withLock {
            processPeriodic()
        }
    }

    private fun processDefer() {
        queue.filterNot {
            it.isPeriodic
        }.filter {
            it.nextCallTime <= time
        }.apply {
            forEach { it.func() }
            queue.removeAll(this)
        }
    }

    private fun processPeriodic() {
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
     * Puts an [func] to the queue which will be called on next [run] call.
     * [invoke] will never call the event at the moment of time when it is called.
     * Literally say, [invoke] is the synonym for [defer] with ZERO delay.
     */
    override fun invoke(func: () -> Unit) {
        lock.writeLock().withLock {
            queue.add(Holder(0L, func, false))
        }
    }


    /**
     * Cancels all funcs in the [queue] and reset the [time].
     */
    override fun shutdown() {
        lock.writeLock().withLock {
            queue.clear()
            time = 0L
        }
    }

    /**
     * Puts an [func] to queue that will be called after timeout in ms.
     * Event will be never called earlier then [time]+[timeout],
     * but might be called later.
     *
     * You can cancel the event before it'll be called by invoking the callback
     */
    override fun defer(func: () -> Unit, timeout: Long): () -> Unit {
        lock.writeLock().withLock {
            with(Holder(timeout, func, false)) {
                queue.add(this)
                return cancelBuilder(this)
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
        lock.writeLock().withLock {
            with(Holder(timeout, func, true)) {
                queue.add(this)
                return cancelBuilder(this)
            }
        }
    }

    private fun cancelBuilder(holder: Holder): () -> Unit {
        return {
            if (!queue.remove(holder)) {
                log.warn("Can't cancel call $holder because it has been already invoked.")
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

        override fun toString(): String {
            return "Holder(timeout=$timeout, isPeriodic=$isPeriodic, nextCallTime=$nextCallTime, currentTime=$time)"
        }


    }
}