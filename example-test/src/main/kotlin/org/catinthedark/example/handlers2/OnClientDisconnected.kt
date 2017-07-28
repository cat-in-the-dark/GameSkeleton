package org.catinthedark.example.handlers2

import org.catinthedark.client.OnDisconnected
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientDisconnected")

@Handler
fun onClientDisconnected(ev: OnDisconnected) {
    log.info("onClientDisconnected $ev")
}