package org.catinthedark.example

import com.esotericsoftware.kryo.Kryo
import org.catinthedark.client.TCPClient
import org.catinthedark.client.UDPClient
import org.catinthedark.example.handlers2.ClientMessage
import org.catinthedark.example.handlers2.ServerMessage
import org.catinthedark.server.TCPServer
import org.catinthedark.server.UDPServer
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
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
            BusRegister.register("org.catinthedark.example.handlers2")

            Thread {
                tcpServer(kryo)
            }.start()

            Thread {
                tcpClient(kryo)
            }.start()

//            Thread {
//                udpServer(kryo)
//            }.start()
//
//            Thread {
//                udpClient(kryo)
//            }.start()
        }

        fun tcpServer(kryo: Kryo) {
            val server = TCPServer(kryo,"0.0.0.0", 8080)

            server.run()
        }

        fun udpServer(kryo: Kryo) {
            val server = UDPServer(kryo,"0.0.0.0", 8081)

            server.run()
        }

        fun tcpClient(kryo: Kryo) {
            val client = TCPClient(kryo)
            client.connect("0.0.0.0", 8080)
        }

        fun udpClient(kryo: Kryo) {
            val client = UDPClient(kryo)
            client.connect("0.0.0.0", 8081)
        }
    }
}