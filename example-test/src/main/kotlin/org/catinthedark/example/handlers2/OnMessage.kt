package org.catinthedark.example.handlers2

import org.catinthedark.server.OnClientConnected
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onMessage")

data class ClientMessage(val data: String = "")
data class ServerMessage(val data: String = "")

@Handler
fun onMessageFromClient(ev: ClientMessage) {
    log.info("onMessageFromClient $ev")
    EventBus.post("onMessageFromClient", invoker, 1000, org.catinthedark.server.TCPMessage(ServerMessage("TCP: Hello client! I'm server.")))
}

@Handler
fun onMessageFromServer(ev: ServerMessage) {
    log.info("onMessageFromServer: $ev")
    EventBus.post("onMessageFromServer", invoker, 1000, org.catinthedark.client.TCPMessage(ClientMessage("TCP: Hello server! I'm client!")))
}

@Handler
fun onConnectedToServer(ev: OnClientConnected) {
    log.info("Connected $ev")
}