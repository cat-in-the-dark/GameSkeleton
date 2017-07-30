package org.catinthedark.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.invokers.Invoker

class MessageHandler(
        private val invoker: Invoker
) : SimpleChannelInboundHandler<Any>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg == null) return
        EventBus.send("MessageHandler#channelRead0", invoker, msg)
    }
}