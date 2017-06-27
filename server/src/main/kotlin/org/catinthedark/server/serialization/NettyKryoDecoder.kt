package org.catinthedark.server.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class NettyKryoDecoder(
        private val kryo: Kryo = Kryo()
) : ByteToMessageDecoder() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val len = msg.readableBytes()
        if (len == 0) return

        try {
            Input().use { input ->
                val bytes = ByteArray(len)
                msg.readBytes(bytes)
                input.buffer = bytes
                out.add(kryo.readClassAndObject(input))
            }
        } catch (e: Exception) {
            log.error("Can't decode msg $msg: ${e.message}", e)
        }
    }
}