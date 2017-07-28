package org.catinthedark.example

import org.catinthedark.example.handlers.GameContext
import org.catinthedark.server.Context
import org.catinthedark.server.Holder
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.invokers.AsyncInvoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.invokers.StickyInvoker

fun starter(contextBuilder: () -> Context = { GameContext(data = 1) }) {
    val queue1 = SimpleInvoker()
    val queue2 = SimpleInvoker()
    val async = AsyncInvoker()
    val sticky = StickyInvoker()

    val ctx = contextBuilder() // actually its a struct for all room data
    val holder = Holder(ctx, "REQUEST")
    BusRegister.register("org.catinthedark.example.handlers")
    EventBus.send("#starter", async, "HELLO", holder) // TODO: select ctx from some context holder
    EventBus.send("#starter", sticky, 1, holder)
    EventBus.send("#starter", sticky, 1.3, holder)

//            EventBus.post("ONE-LATER", holder, 100, queue1)

    EventBus.send("#starter", queue1, "ONE-1", holder)
    EventBus.send("#starter", queue2, "TWO-1", holder)

    EventBus.send("#starter", queue1, "ONE-2", holder)
    EventBus.send("#starter", queue2, "TWO-2", holder)

    EventBus.send("#starter", queue1, "ONE-3", holder)

    async.shutdown()
    queue1.shutdown()
    queue2.shutdown()
}