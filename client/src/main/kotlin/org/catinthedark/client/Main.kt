package org.catinthedark.client

import org.catinthedark.shared.serialization.KryoSerializer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello!")
            val kryo = KryoSerializer()
            val client = TCPClient(kryo, kryo)
            client.connect("0.0.0.0", 8080)
        }
    }
}