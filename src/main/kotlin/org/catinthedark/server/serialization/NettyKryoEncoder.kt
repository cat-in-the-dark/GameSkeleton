package org.catinthedark.server.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Output
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class NettyKryoEncoder(
        private val kryo: Kryo = Kryo()
) : MessageToByteEncoder<Any>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        val output = Output().use { output ->
            kryo.writeClassAndObject(output, msg)
            output.flush()
            output
        }

        val bytes = output.toBytes()
        out.writeBytes(bytes)
        ctx.write(Unpooled.copiedBuffer(out))
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
    }
}