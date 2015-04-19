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
package com.ottogroup.bi.spqr.websocket.kafka;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Receives a web socket connection and starts to emit incoming messages received from configured
 * kafka topic 
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRKafkaWebSocketHandler implements Runnable {

	private final Channel websocketContext;
	private boolean running = false;
	
	/**
	 * Initializes the handler using the provided input
	 * @param websocketContext
	 */
	public SPQRKafkaWebSocketHandler(final Channel websocketContext) {
		this.websocketContext = websocketContext;
		this.running = true;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		while(running) {
			this.websocketContext.writeAndFlush(new TextWebSocketFrame("Hello world"));
		}
		
		this.websocketContext.close();
		
	}

}
