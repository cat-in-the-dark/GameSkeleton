package org.catinthedark.example.handlers2

import org.catinthedark.client.OnConnected
import org.catinthedark.client.TCPMessage
import org.catinthedark.client.UDPMessage
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientConnected")

@Handler
fun onClientConnected(ev: OnConnected) {
    log.info("onClientConnected $ev")
    EventBus.send("#onClientConnected", invoker, TCPMessage(ClientMessage("TCP: Hello server! I'm client!")))
    EventBus.send("#onClientConnected", invoker, UDPMessage(ClientMessage("UDP: Hello server! I'm client!")))
}