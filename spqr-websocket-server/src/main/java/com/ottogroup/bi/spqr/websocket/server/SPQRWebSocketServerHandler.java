/**
 * Copyright 2015 Otto (GmbH & Co KG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ottogroup.bi.spqr.websocket.server;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.websocket.kafka.KafkaTopicRequest;
import com.ottogroup.bi.spqr.websocket.kafka.KafkaTopicWebSocketEmitter;

/**
 * Receives incoming connections and binds them to {@link KafkaTopicWebSocketEmitter} 
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/pipeline";
    
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private KafkaTopicWebSocketEmitter emitter = null;
    private WebSocketServerHandshaker handshaker = null;
    private ExecutorService executorService = Executors.newCachedThreadPool();
	
	/**
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame)msg);
		} else {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		}
	}
	
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // if client request to close socket connection: shut it down ;-)
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        
        // not of expected type: return an error
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        // only the first request is forwarded to the kafka topic emitter. if the client wishes to change
        // the topic a new connection must be established - connection migration is not supported by now
        // TODO add support for connection migration
        if(this.emitter == null) {

        	// extract content from request and ensure that it is neither null nor empty
	        String requestContent = ((TextWebSocketFrame) frame).text();
	        if(StringUtils.isBlank(requestContent)) {
	        	ctx.channel().writeAndFlush("Invalid request received. Please see documentation for further infromation");
	        	return;
	        }

	        // convert content into topic request representation and check if it contains a non-null and non-empty topic identifier 
	        KafkaTopicRequest topicRequest = null;
	        try {
	        	topicRequest = this.jsonMapper.readValue(requestContent, KafkaTopicRequest.class);
	        } catch(Exception e) {
	        	ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("Invalid request format. Please see document for further information".getBytes())));
	        	return;
	        }
	        if(StringUtils.isBlank(topicRequest.getTopicId())) {
	        	ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("Invalid request, missing topic identifier. Please see document for further information".getBytes())));
	        	return;
	        }	        	
	        
	        // establish connection with kafka topic and start emitting content to websocket
	        this.emitter = new KafkaTopicWebSocketEmitter(ctx.channel(), 1024, executorService, "localhost:2181", "client-id-"+System.currentTimeMillis(), topicRequest.getTopicId());	
	        executorService.submit(this.emitter);
        }        
    }
	
	/**
	 * @see io.netty.channel.ChannelHandlerAdapter#handlerRemoved(io.netty.channel.ChannelHandlerContext)
	 */
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

		// shutdown the emitter if one is available
		if(this.emitter != null)
			this.emitter.shutdown();
		// shutdown runtime environment
		this.executorService.shutdownNow();
	}

	/**
	 * Handles incoming http request pointing. Parts of the code were copied from {@linkplain http://netty.io} web socket server example. 
	 * The origins may be found at: {@linkplain https://github.com/netty/netty/blob/4.0/example/src/main/java/io/netty/example/http/websocketx/server/WebSocketServerHandler.java} 
	 * @param ctx
	 * @param req
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

		// handle bad requests as indicated by decoder
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // allow only GET methods to comply with REST spec as this is not about modifying content
        // but receiving it ;-)
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Handshake
        String wsLocation = "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                wsLocation, null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    /** 
     * Sends a {@link HttpResponse} according to prepared {@link FullHttpResponse} to the client. The 
     * code was copied from netty.io websocket server example. The origins may be found at:
     * {@linkplain https://github.com/netty/netty/blob/4.0/example/src/main/java/io/netty/example/http/websocketx/server/WebSocketServerHandler.java} 
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
    	// Generate an error page if response getStatus code is not OK (200).
    	if (res.getStatus().code() != 200) {
    		ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
    		res.content().writeBytes(buf);
    		buf.release();
    		HttpHeaders.setContentLength(res, res.content().readableBytes());
    	}

    	// Send the response and close the connection if necessary.
    	ChannelFuture f = ctx.channel().writeAndFlush(res);
    	if(!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
    		f.addListener(ChannelFutureListener.CLOSE);
    	}
	}
    
}
