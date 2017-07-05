package org.catinthedark.example

import org.catinthedark.example.handlers.GameContext
import org.catinthedark.server.Context
import org.catinthedark.server.EventBus
import org.catinthedark.server.Holder
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
    EventBus.register("org.catinthedark.example.handlers")
    EventBus.send("HELLO", holder, async) // TODO: select ctx from some context holder
    EventBus.send(1, holder, sticky)
    EventBus.send(1.3, holder, sticky)

//            EventBus.post("ONE-LATER", holder, 100, queue1)

    EventBus.send("ONE-1", holder, queue1)
    EventBus.send("TWO-1", holder, queue2)

    EventBus.send("ONE-2", holder, queue1)
    EventBus.send("TWO-2", holder, queue2)

    EventBus.send("ONE-3", holder, queue1)

    async.shutdown()
    queue1.shutdown()
    queue2.shutdown()
}