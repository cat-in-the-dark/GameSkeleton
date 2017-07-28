package org.catinthedark.shared.invokers

import org.junit.Assert
import org.junit.Test

class TickInvokerTest {
    @Test
    fun Should_Invoke() {
        val invoker = TickInvoker()
        var accum = 0
        invoker.invoke {
            accum += 1
        }
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
    }

    @Test
    fun Should_InvokeDefer() {
        val invoker = TickInvoker()
        var accum = 0
        invoker.defer ({
            accum += 1
        }, 2)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
    }

    @Test
    fun Should_NotFailOnCancelingInvokedTask() {
        val invoker = TickInvoker()
        var accum = 0
        val cancel = invoker.defer ({
            accum += 1
        }, 2)
        Assert.assertEquals(0, accum)
        invoker.run(2)
        Assert.assertEquals(1, accum)
        cancel()
        invoker.run(2)
        Assert.assertEquals(1, accum)
    }

    @Test
    fun Should_CancelDefer() {
        val invoker = TickInvoker()
        var accum = 0
        val defer = invoker.defer ({
            accum += 1
        }, 2)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(0, accum)
        defer()
        invoker.run(1)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(0, accum)
    }

    @Test
    fun Should_InvokePeriodic() {
        val invoker = TickInvoker()
        var accum = 0
        invoker.periodic ({
            accum += 1
        }, 2)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
        invoker.run(1)
        Assert.assertEquals(2, accum)
        invoker.run(1)
        Assert.assertEquals(2, accum)
        invoker.run(1)
        Assert.assertEquals(3, accum)
    }

    @Test
    fun Should_CancelPeriodic() {
        val invoker = TickInvoker()
        var accum = 0
        val periodic = invoker.periodic ({
            accum += 1
        }, 1)
        Assert.assertEquals(0, accum)
        invoker.run(1)
        Assert.assertEquals(1, accum)
        invoker.run(1)
        Assert.assertEquals(2, accum)
        periodic()
        invoker.run(1)
        Assert.assertEquals(2, accum)
        invoker.run(1)
        Assert.assertEquals(2, accum)
    }
}