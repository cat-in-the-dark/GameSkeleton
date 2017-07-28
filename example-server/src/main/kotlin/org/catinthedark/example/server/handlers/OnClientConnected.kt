package org.catinthedark.example.server.handlers

import org.catinthedark.example.server.clients
import org.catinthedark.example.server.clientsRooms
import org.catinthedark.example.server.lobby
import org.catinthedark.server.OnClientConnected
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientConnected")

@Handler
fun onClientConnected(ev: OnClientConnected) {
    log.info("$ev")
    val address = ev.remoteAddress ?: return
    clients.put(ev.id, address)
    clientsRooms.put(ev.id, lobby)
}