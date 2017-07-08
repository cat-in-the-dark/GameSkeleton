package org.catinthedark.example

import com.esotericsoftware.kryo.Kryo
import org.catinthedark.client.TCPClient
import org.catinthedark.example.handlers2.ClientMessage
import org.catinthedark.example.handlers2.ServerMessage
import org.catinthedark.server.TCPServer
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.serialization.KryoCustomizer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val kryo = Kryo().apply {
                KryoCustomizer.register(this,
                        ClientMessage::class,
                        ServerMessage::class
                )
            }
            Events.Registrator.register("org.catinthedark.example.handlers2")

            Thread {
                tcpServer(kryo)
            }.start()

            Thread {
                client(kryo)
            }.start()
        }

        fun tcpServer(kryo: Kryo) {
            val server = TCPServer(kryo)

            server.run()
        }

        fun client(kryo: Kryo) {
            val client = TCPClient(kryo)
            client.connect("0.0.0.0", 8080)
        }
    }
}