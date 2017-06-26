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
import org.catinthedark.server.serialization.NettyKryoDecoder
import org.catinthedark.server.serialization.NettyKryoEncoder
import org.slf4j.LoggerFactory

class TCPServer {
    private val PORT = 8080
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

                            val kryo = Kryo()
                            pipe.addLast("decoder", NettyKryoDecoder(kryo))
                            pipe.addLast("encoder", NettyKryoEncoder(kryo))
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            val f = b.bind(PORT).sync()

            log.info("TCP sever is up on port $PORT")
            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}