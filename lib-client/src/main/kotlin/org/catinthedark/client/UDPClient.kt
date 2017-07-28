package org.catinthedark.client

import com.esotericsoftware.kryo.Kryo
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import org.catinthedark.shared.event_bus.BusRegister
import org.catinthedark.shared.event_bus.EventBus
import org.catinthedark.shared.event_bus.Handler
import org.catinthedark.shared.invokers.Invoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.serialization.NettyDecoder
import org.catinthedark.shared.serialization.NettyEncoder
import org.slf4j.LoggerFactory

class UDPClient(
        private val kryo: Kryo,
        private val invoker: Invoker = SimpleInvoker()
) {
    private val group = NioEventLoopGroup(1)
    private val bootstrap = Bootstrap()
    private val log = LoggerFactory.getLogger(this::class.java)
    private var channel: Channel? = null

    init {
        BusRegister.register(this)
        bootstrap.group(group)
                .channel(NioDatagramChannel::class.java)
                .handler(object : ChannelInitializer<AbstractChannel>() {
                    override fun initChannel(ch: AbstractChannel) {
                        val pipe = ch.pipeline()

                        pipe.addLast("decoder", NettyDecoder(kryo))
                        pipe.addLast("encoder", NettyEncoder(kryo))
                        pipe.addLast("handler", MessageHandler())
                    }
                })
    }

    fun connect(host: String, port: Int) {
        bootstrap.connect(host, port).addListener(object : ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture) {
                if (future.isSuccess) {
                    channel = future.channel()
                    addCloseDetectListener(future.channel())
                    EventBus.send("UDPClient#connect", invoker, OnConnected())
                } else {
                    channel = null
                    future.channel().close()
                    bootstrap.connect(host, port).addListener(this) // reconnect
                    EventBus.send("UDPClient#connect", invoker, OnConnectionFailure(future.cause()))
                }
            }
        })
    }

    private fun addCloseDetectListener(ch: Channel) {
        ch.closeFuture().addListener {
            channel = null
            EventBus.send("UDPClient#addCloseDetectListener", invoker, OnDisconnected())
        }
    }

    @Handler
    fun send(msg: UDPMessage) {
        try {
            if (channel?.isActive == true) {
                channel?.writeAndFlush(msg.payload)
            } else {
                throw Exception("Channel is not active.")
            }
        } catch (e: Exception) {
            log.warn("Can't send TCP message $msg.", e)
            EventBus.send("UDPClient#send", invoker, OnSendingUDPMessageError(e, msg))
        }
    }
}