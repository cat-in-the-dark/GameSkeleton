package org.catinthedark.example.handlers2

import org.catinthedark.client.Message
import org.catinthedark.client.OnConnected
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.event_bus.Handler
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.StickyInvoker
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("onClientConnected")
private val invoker: Invoker = StickyInvoker()

@Handler
fun onClientConnected(ev: OnConnected) {
    log.info("onClientConnected $ev")
    Events.Bus.send(invoker, Message("Hello world!"))
}