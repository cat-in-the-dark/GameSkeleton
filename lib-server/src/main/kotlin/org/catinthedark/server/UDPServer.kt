package org.catinthedark.server

import com.esotericsoftware.kryo.Kryo
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class UDPServer(
        private val kryo: Kryo,
        val host: String = "0.0.0.0",
        val port: Int = 8081,
        private val invoker: Invoker = SimpleInvoker()
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun run() {
        val group = NioEventLoopGroup(1)
        try {
            val b = Bootstrap()
            b.group(group)
                    .channel(NioDatagramChannel::class.java)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(LoggingHandler(LogLevel.INFO))
                    .handler(object : ChannelInitializer<NioDatagramChannel>() {
                        override fun initChannel(ch: NioDatagramChannel) {
                            val pipe = ch.pipeline()

                            pipe.addLast("decoder", NettyDecoder(kryo))
                            pipe.addLast("encoder", NettyEncoder(kryo))
                            pipe.addLast("handler", GameHandler(invoker))
                        }
                    })

            val addr = InetSocketAddress(host, port)
            val f = b.bind(addr).sync()

            log.info("UDP server is up on $addr")
            f.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
}