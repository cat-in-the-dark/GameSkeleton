package com.catinthedark.server.web;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

public class JsonHttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpServerHandler.class);
    
    private final Map<String, Supplier<String>> handlers;
    
    public JsonHttpServerHandler(Map<String, Supplier<String>> handlers) {
        this.handlers = handlers;
    }
    
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest)msg;
            QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
            
            Supplier<String> handler = handlers.get(queryDecoder.path());
            if (handler != null) {
                String data = handler.get();
                FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
                res.content().writeBytes(data.getBytes());
                ctx.writeAndFlush(res);
                return;
            }
        }
        
        ctx.fireChannelRead(msg);
    }
}
