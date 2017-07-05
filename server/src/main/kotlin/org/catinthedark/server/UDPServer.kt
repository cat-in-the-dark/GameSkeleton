package org.catinthedark.server

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import org.catinthedark.shared.serialization.Deserializer
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.catinthedark.shared.serialization.Serializer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class UDPServer(
        private val serializer: Serializer,
        private val deserializer: Deserializer
) {
    private val PORT = 8081
    private val log = LoggerFactory.getLogger(this::class.java)

    fun run() {
        val group = NioEventLoopGroup(1)
        try {
            val b = Bootstrap()
            b.group(group)
                    .channel(NioDatagramChannel::class.java)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
//                    .handler(object: SimpleChannelInboundHandler<DatagramPacket>(){
//                        override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
//                            val buffer = msg.content()
//                            val bytes = ByteArray(buffer.readableBytes())
//                            buffer.readBytes(bytes)
//                            println("Receive ${bytes.toList()}")
//                        }
//                    })
                    .handler(object : ChannelInitializer<NioDatagramChannel>() {
                        override fun initChannel(ch: NioDatagramChannel) {
                            println("INIT CAHNNEL")
                            val pipe = ch.pipeline()

                            pipe.addLast("decoder", NettyDecoder(deserializer))
                            pipe.addLast("encoder", NettyEncoder(serializer))
                        }
                    })

            val addr = InetSocketAddress("0.0.0.0", PORT)
            val f = b.bind(addr).sync()

            log.info("UDP server is up on $addr")
            f.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
}