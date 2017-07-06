package org.catinthedark.client

import com.esotericsoftware.kryo.Kryo

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello!")
            val kryo = Kryo()
            val client = TCPClient(kryo)
            client.connect("0.0.0.0", 8080)
        }
    }
}