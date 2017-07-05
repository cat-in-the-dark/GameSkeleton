package org.catinthedark.shared.serialization

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class NettyDecoder(
        private val deserializer: Deserializer
) : ByteToMessageDecoder() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val len = msg.readableBytes()
        if (len == 0) return

        try {
            val bytes = ByteArray(len)
            msg.readBytes(bytes)
            out.add(deserializer(bytes))
        } catch (e: Exception) {
            log.error("Can't decode msg $msg: ${e.message}", e)
        }
    }
}