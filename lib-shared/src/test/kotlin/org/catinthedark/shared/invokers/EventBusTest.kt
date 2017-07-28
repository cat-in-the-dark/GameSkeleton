package org.catinthedark.shared.invokers

import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.junit.Assert
import org.junit.Test

class EventBusTest {
    val invoker: Invoker = StickyInvoker()

    var called = 0

    @Handler(preHandlerPath = "/")
    fun handler(msg: String, extras: String) {
        println("Received: $msg, $extras")
        called += 1
    }

    @Handler()
    fun handler(msg: Int) {
        println("Received: $msg")
        called += 1
    }

    @Test
    fun Should_Send() {
        BusRegister.register(this)
        BusRegister.registerPreHandler("/", { _, message, _ ->
            Pair(message, listOf("EXTRAS"))
        })
        EventBus.send("EventBusTest#Shoudl_Send", invoker,"Hello")
        EventBus.send("EventBusTest#Shoudl_Send", invoker,1)
        Assert.assertEquals(2, called)
    }
}