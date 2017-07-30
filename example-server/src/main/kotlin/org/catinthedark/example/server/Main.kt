package org.catinthedark.example.server

import org.catinthedark.server.TCPServer
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.serialization.KryoCustomizer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BusRegister.register("org.catinthedark.example.server.handlers")
            BusRegister.registerPreHandler("addContext", { _, message, ctx ->
                val id = ctx.getOrNull(0) ?: return@registerPreHandler Pair(message, ctx)
                when (id) {
                    is String -> Pair(message, listOf(lobby))
                    else -> Pair(message, ctx)
                }
            })
            val kryo = KryoCustomizer.buildAndRegister("org.catinthedark.example.shared.messages")
            val server = TCPServer(kryo, "0.0.0.0", 8080, invoker)

            server.run()
        }
    }
}
