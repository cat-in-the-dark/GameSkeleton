package org.catinthedark.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory

class GameHandler : SimpleChannelInboundHandler<Any>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        log.info("Received $msg")
    }
}