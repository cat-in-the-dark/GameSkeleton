package org.catinthedark.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.SimpleInvoker

class GameHandler : SimpleChannelInboundHandler<Any>() {
    private val invoker: Invoker = SimpleInvoker()

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg == null) return
        Events.Bus.send(invoker, msg)
    }
}