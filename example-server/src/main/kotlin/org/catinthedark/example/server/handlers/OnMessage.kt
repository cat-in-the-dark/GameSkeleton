package org.catinthedark.example.server.handlers

import org.catinthedark.example.server.Room
import org.catinthedark.example.shared.messages.OnGameStart
import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onStartGame")

@Handler
fun onStartGame(ev: OnGameStart, room: Room) {
    log.info("$ev $room")
}