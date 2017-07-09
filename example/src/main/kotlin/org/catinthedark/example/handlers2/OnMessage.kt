package org.catinthedark.example.handlers2

import org.catinthedark.client.Message
import org.catinthedark.server.OnClientConnected
import org.catinthedark.server.TCPMessage
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onMessage")

data class ClientMessage(val data: String = "")
data class ServerMessage(val data: String = "")

@Handler
fun onMessageFromClient(ev: ClientMessage) {
    log.info("onMessageFromClient $ev")
    Events.Bus.post(invoker, 1000, TCPMessage(ServerMessage("Hello client! I'm server.")))
}

@Handler
fun onMessageFromServer(ev: ServerMessage) {
    log.info("onMessageFromServer: $ev")
    Events.Bus.post(invoker, 1000, Message(ClientMessage("Hello server! I'm client!")))
}

@Handler
fun onConnectedToServer(ev: OnClientConnected) {
    log.info("Connected $ev")
}