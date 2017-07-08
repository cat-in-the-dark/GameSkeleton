package org.catinthedark.client

import com.esotericsoftware.kryo.Kryo
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.catinthedark.shared.event_bus.Events
import org.catinthedark.shared.event_bus.Handler
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.slf4j.LoggerFactory

class TCPClient(
        private val kryo: Kryo,
        private val invoker: Invoker = SimpleInvoker()
) {
    private val group = NioEventLoopGroup()
    private val bootstrap = Bootstrap()
    private val log = LoggerFactory.getLogger(this::class.java)
    private var channel: Channel? = null

    init {
        Events.Registrator.register(this)
        bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<AbstractChannel>() {
                    override fun initChannel(ch: AbstractChannel) {
                        val pipe = ch.pipeline()

                        pipe.addLast("decoder", NettyDecoder(kryo))
                        pipe.addLast("encoder", NettyEncoder(kryo))
                        pipe.addLast("handler", MessageHandler())
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
    }

    fun connect(host: String, port: Int) {
        bootstrap.connect(host, port).addListener(object : ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture) {
                if (future.isSuccess) {
                    channel = future.channel()
                    addCloseDetectListener(future.channel())
                    Events.Bus.send(invoker, OnConnected())
                } else {
                    channel = null
                    future.channel().close()
                    bootstrap.connect(host, port).addListener(this) // reconnect
                    Events.Bus.send(invoker, OnConnectionFailure(future.cause()))
                }
            }
        })
    }

    private fun addCloseDetectListener(ch: Channel) {
        ch.closeFuture().addListener {
            channel = null
            Events.Bus.send(invoker, OnDisconnected())
        }
    }

    @Handler
    fun send(msg: Message) {
        try {
            if (channel?.isActive == true) {
                channel?.writeAndFlush(msg.payload)
            } else {
                throw Exception("Channel is not active.")
            }
        } catch (e: Exception) {
            log.warn("Can't send TCP message $msg.", e)
            Events.Bus.send(invoker, OnSendingMessageError(e, msg))
        }
    }
}