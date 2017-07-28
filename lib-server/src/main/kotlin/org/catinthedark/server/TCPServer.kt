package org.catinthedark.server

import com.esotericsoftware.kryo.Kryo
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.AbstractChannel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class TCPServer(
        private val kryo: Kryo,
        val host: String = "0.0.0.0",
        val port: Int = 8080,
        private val invoker: Invoker = SimpleInvoker()
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun run() {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .handler(LoggingHandler(LogLevel.INFO))
                    .childHandler(object : ChannelInitializer<AbstractChannel>() {
                        override fun initChannel(ch: AbstractChannel) {
                            val pipe = ch.pipeline()

                            pipe.addLast("decoder", NettyDecoder(kryo))
                            pipe.addLast("encoder", NettyEncoder(kryo))
                            pipe.addLast("handler", GameHandler(invoker))
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)


            val addr = InetSocketAddress(host, port)
            val f = b.bind(addr).addListener {
                log.info("TCP sever is up on $addr")
                EventBus.send("TCPServer#run", invoker, ServerStarted())
            }.sync()

            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            EventBus.send("TCPServer#run", invoker, ServerStopped())
        }
    }
}