package org.catinthedark.shared.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Output
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.LoggerFactory

class NettyEncoder(
        private val kryo: Kryo
) : MessageToByteEncoder<Any>() {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val output = Output(4 * 1024, 16 * 1024)

    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        try {
            log.info("Encode: $msg")

            output.clear()
            output.setPosition(4)
            kryo.writeClassAndObject(output, msg)
            val len = output.position()
            output.setPosition(0)
            output.writeInt(len - 4)


            out.writeBytes(output.buffer, 0, len)
            ctx.writeAndFlush(Unpooled.copiedBuffer(out))

            log.info("Write and flush")
        } catch (e: Exception) {
            log.error("Can't encode message $msg.", e)
        }
    }
}