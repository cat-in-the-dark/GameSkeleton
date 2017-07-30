package org.catinthedark.server

import com.esotericsoftware.kryo.Kryo
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.AbstractChannel
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class TCPServer(
        private val kryo: Kryo,
        private val host: String = "0.0.0.0",
        private val port: Int = 8080,
        private val invoker: Invoker
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val channels: MutableMap<String, Channel> = hashMapOf()

    fun run() {
        BusRegister.register(this)
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup(1)
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
                            pipe.addLast("handler", GameHandler(invoker, channels))
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

    fun stop() {
        BusRegister.unregister(this)
    }

    @Handler
    fun send(msg: TCPMessage) {
        if (msg.to == null) {
            // will send to all
            channels.forEach { _, ch ->
                send(msg, ch)
            }
        } else {
            val ch = channels.getOrDefault(msg.to, null)
            send(msg, ch)
        }
    }

    private fun send(msg: TCPMessage, ch: Channel?) {
        if (ch == null) {
            log.warn("There is no channel with id ${msg.to}. Channels $channels")
            return
        }
        log.debug("Sending $msg To ${ch.id()} ${ch.remoteAddress()}")

        if (ch.isActive && ch.isOpen && ch.isWritable) {
            ch.writeAndFlush(msg.payload)
        } else {
            log.warn("Channel with id ${msg.to} ${ch.remoteAddress()} is not ready to receive messages.")
        }
    }
}