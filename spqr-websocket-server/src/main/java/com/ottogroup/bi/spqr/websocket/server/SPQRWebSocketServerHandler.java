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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ottogroup.bi.spqr.websocket.kafka.SPQRKafkaWebSocketHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * Handles all incoming communication
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/pipeline";
    
    private WebSocketServerHandshaker handshaker = null;
    private ExecutorService executorService = Executors.newCachedThreadPool();
	
	/**
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof WebSocketFrame) {
			this.executorService.submit(new SPQRKafkaWebSocketHandler(ctx.channel()));
		} else {
			System.out.println("msg: " + msg);
		}
		
	}

}
