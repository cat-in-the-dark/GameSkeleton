package org.catinthedark.example.client.handlers

import org.catinthedark.client.OnConnected
import org.catinthedark.client.OnConnectionFailure
import org.catinthedark.client.OnDisconnected
import org.catinthedark.client.TCPMessage
import org.catinthedark.example.client.invoker
import org.catinthedark.example.shared.messages.OnGameStart
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("connectionHandlers")

@Handler
fun onConnected(ev: OnConnected) {
    log.info("Connected: $ev")
    EventBus.send("connectionHandlers#onConnected", invoker,
            TCPMessage(OnGameStart("Hello world!"))
    )
}

@Handler
fun onDisconnected(ev: OnDisconnected) {
    log.info("Disconnected: $ev")
}

@Handler
fun onConnectionFailure(ev: OnConnectionFailure) {
    log.error("ConnectionFailed: $ev")
}