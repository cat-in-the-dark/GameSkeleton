package org.catinthedark.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.invokers.Invoker
import org.slf4j.LoggerFactory

/**
 * This is main Netty handler.
 * It sends events to [EventBus] about client connections, disconnections and messages.
 * This service listen to [TCPMessage] on [EventBus].
 * You can send a message to other clients by sending [TCPMessage] via [EventBus]
 */
class GameHandler(
        private val invoker: Invoker,
        private val channels: MutableMap<String, Channel>
) : SimpleChannelInboundHandler<Any>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        BusRegister.register(this)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        super.handlerRemoved(ctx)
        BusRegister.unregister(this)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg == null) return
        EventBus.send("GameHandler#channelRead0", invoker, msg, ctx.channel().id().asLongText())
    }

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        val id = ctx.channel().id().asLongText()
        channels.put(id, ctx.channel())
        EventBus.send("GameHandler#channelRegistered", invoker, OnClientConnected(
                ctx.channel().remoteAddress(),
                id
        ))
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        val id = ctx.channel().id().asLongText()
        EventBus.send("GameHandler#channelUnregistered", invoker, OnClientDisconnected(
                ctx.channel().remoteAddress(),
                id
        ))
        channels.remove(id)
        super.channelUnregistered(ctx)
    }
}