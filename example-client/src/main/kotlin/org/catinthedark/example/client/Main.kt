package org.catinthedark.example.client

import org.catinthedark.client.TCPClient
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.invokers.DeferrableInvoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.serialization.KryoCustomizer

val invoker: DeferrableInvoker = SimpleInvoker()

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BusRegister.register("org.catinthedark.example.client.handlers")
            val kryo = KryoCustomizer.buildAndRegister("org.catinthedark.example.shared.messages")
            val client = TCPClient(kryo, invoker)
            client.connect("0.0.0.0", 8080)
        }
    }
}