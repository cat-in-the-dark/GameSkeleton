package org.catinthedark.example

import org.catinthedark.server.TCPServer
import org.catinthedark.shared.serialization.KryoSerializer

fun tcpServer() {
    val kryo = KryoSerializer()
    val server = TCPServer(kryo, kryo)

    server.run()
}