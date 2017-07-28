package org.catinthedark.shared.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class NettyDecoder(
        private val kryo: Kryo
) : ByteToMessageDecoder() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        try {
            if (buf.readableBytes() == 0) return
            val length = buf.readInt()
            if (length == 0 || length > buf.readableBytes()) {
                log.warn("Length is $length, ReadableBytes is ${buf.readableBytes()}")
                return
            }
            val bytes = ByteArray(length)
            buf.readBytes(bytes)
            out.add(kryo.readClassAndObject(Input(bytes)))
        } catch (e: Exception) {
            log.error("Can't decode data.", e)
        }
    }
}