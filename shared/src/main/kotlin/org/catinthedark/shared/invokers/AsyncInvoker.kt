package org.catinthedark.shared.invokers

import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AsyncInvoker : DeferrableInvoker {
    private val executor = Executors.newScheduledThreadPool(4)
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun invoke(func: () -> Unit) {
        executor.submit(func)
    }

    override fun defer(func: () -> Unit, timeout: Long): () -> Unit {
        val f = executor.schedule(func, timeout, TimeUnit.MILLISECONDS)

        return {
            try {
                f.cancel(true)
            } catch (e: Exception) {
                log.error("Can't stop defer action of period $timeout: ${e.message}", e)
            }
        }
    }

    override fun periodic(func: () -> Unit, timeout: Long): () -> Unit {
        val f = executor.scheduleWithFixedDelay(func, timeout, timeout, TimeUnit.MILLISECONDS)

        return {
            try {
                f.cancel(true)
            } catch (e: Exception) {
                log.error("Can't stop periodic action of period $timeout: ${e.message}", e)
            }
        }
    }

    override fun shutdown() {
        executor.shutdown()
    }
}