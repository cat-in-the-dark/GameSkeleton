package com.catinthedark.server;

import com.catinthedark.server.web.JsonHttpServerHandler;
import com.corundumstudio.socketio.SocketIOChannelInitializer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ExtendedPipelineFactory extends SocketIOChannelInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ExtendedPipelineFactory.class);

    private Map<String, Supplier<String>> jsonHandlers = new HashMap<>();
    
    @Override
    protected void initChannel(Channel ch) throws Exception {
        super.initChannel(ch);
        JsonHttpServerHandler handler = new JsonHttpServerHandler(jsonHandlers);
        ch.pipeline().addBefore(SocketIOChannelInitializer.PACKET_HANDLER, "HttpStaticHandler", handler);
        LOG.info("Init ExtendPipelineFactory");
    }

    public void registerJsonHandler(String path, Supplier<String> handler) {
        jsonHandlers.put(path, handler);
    }
}
