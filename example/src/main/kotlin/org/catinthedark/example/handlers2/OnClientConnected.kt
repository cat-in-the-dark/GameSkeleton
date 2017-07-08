package org.catinthedark.example.handlers2

import org.catinthedark.client.Message
import org.catinthedark.client.OnConnected
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientConnected")

@Handler
fun onClientConnected(ev: OnConnected) {
    log.info("onClientConnected $ev")
    Events.Bus.send(invoker, Message(ClientMessage("Hello server! I'm client!")))
}