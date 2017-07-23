package org.catinthedark.server

import java.net.SocketAddress

data class OnClientConnected(
        val remoteAddress: SocketAddress?,
        val id: String
)

data class OnClientDisconnected(
        val remoteAddress: SocketAddress?,
        val id: String
)


data class TCPMessage(
        val payload: Any,
        val to: String? = null // NULL means send to all
)

data class UDPMessage(
        val payload: Any,
        val to: String? = null // NULL means send to all
)

class ServerStarted
class ServerStopped