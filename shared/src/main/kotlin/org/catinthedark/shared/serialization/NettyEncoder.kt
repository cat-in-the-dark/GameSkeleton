package org.catinthedark.shared.serialization

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class NettyEncoder(
        private val serializer: Serializer
) : MessageToByteEncoder<Any>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        out.writeBytes(serializer(msg))
        ctx.write(Unpooled.copiedBuffer(out))
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
    }
}