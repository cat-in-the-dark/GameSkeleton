package org.catinthedark.server

import org.catinthedark.server.handlers.GameContext
import org.catinthedark.server.invokers.AsyncInvoker
import org.catinthedark.server.invokers.QueueInvoker
import org.catinthedark.server.invokers.StickyInvoker

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            UDPServer().run()
//            TCPServer().run()
            starter({ GameContext(data = 1) })
        }

        fun starter(contextBuilder: () -> Context) {
            val queue1 = QueueInvoker()
            val queue2 = QueueInvoker()
            val async = AsyncInvoker()
            val sticky = StickyInvoker()

            val ctx = contextBuilder() // actually its a struct for all room data
            val holder = Holder(ctx, "REQUEST")
            EventBus.register("org.catinthedark.server.handlers")
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
    }
}