package org.catinthedark.example.handlers2

import org.catinthedark.shared.event_bus.Handler
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onMessage")

@Handler
fun onMessage(ev: String) {
    log.info("onMessage $ev")
}