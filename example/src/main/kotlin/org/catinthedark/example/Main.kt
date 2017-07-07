package org.catinthedark.example

import com.esotericsoftware.kryo.Kryo
import org.catinthedark.client.TCPClient
import org.catinthedark.server.TCPServer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val kryo = Kryo()

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