package org.catinthedark.example.server.handlers

import org.catinthedark.example.server.clients
import org.catinthedark.example.server.clientsRooms
import org.catinthedark.server.OnClientDisconnected
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientDisconnected")

@Handler
fun onClientDisconnected(ev: OnClientDisconnected) {
    log.info("$ev")
    clients.remove(ev.id)
    clientsRooms.remove(ev.id)
}