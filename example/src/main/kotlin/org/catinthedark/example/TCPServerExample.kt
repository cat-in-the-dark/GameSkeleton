package org.catinthedark.example

import com.esotericsoftware.kryo.Kryo
import org.catinthedark.server.TCPServer

fun tcpServer() {
    val kryo = Kryo()
    val server = TCPServer(kryo)

    server.run()
}