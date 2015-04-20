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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import uk.co.real_logic.queues.BlockingWaitStrategy;
import uk.co.real_logic.queues.MessageWaitStrategy;
import uk.co.real_logic.queues.OneToOneConcurrentArrayQueue3;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;

/**
 * Receives a web socket connection and starts to emit incoming messages received from configured
 * kafka topic 
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class KafkaTopicWebSocketEmitter implements Runnable {

	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(KafkaTopicWebSocketEmitter.class);
	
	/** web socket channel to write content to */
	private final Channel websocketChannel;
	/** queue used for receiving kafka content */
	private final OneToOneConcurrentArrayQueue3<byte[]> messages;
	/** strategy to apply when waiting for messages from queue */
	private final MessageWaitStrategy<byte[]> messageWaitStrategy;
	/** runtime environment for topic stream consumers */
	private final ExecutorService executorService;
	/** consumer client establishing a connection with the Kafka server and spawning stream readers */
	private KafkaTopicConsumer consumer;
	/** connection string used for establishing a connection with zookeeper */ 
	private final String zookeeperConnect;
	/** group identifier assigned to kafka client when establishing a connection with kafka */ 
	private final String groupId;
	/** topic to consume data from */
	private final String topicId;
	/** indicates whether the emitter is running or halted */
	private boolean running = false;
	
	/**
	 * Initializes the socket emitter using the provided input 
	 * @param websocketChannel channel to write incoming messages to
	 * @param messageQueueCapacity max. number of elements to be contained inside the queue used for internal data exchange
	 * @param executorService runtime environment stream consumers will live in
	 * @param zookeeperConnect zookeeper connection string, eg. localhost:2181
	 * @param groupId group identifier used by client when connecting with Kafka 
	 * @param topicId topic to consume data from
	 */
	public KafkaTopicWebSocketEmitter(final Channel websocketChannel, final int messageQueueCapacity, final ExecutorService executorService,
			final String zookeeperConnect, final String groupId, final String topicId) {
		this.websocketChannel = websocketChannel;
		this.messages = new OneToOneConcurrentArrayQueue3<byte[]>(messageQueueCapacity);
		this.messageWaitStrategy = new BlockingWaitStrategy();
		this.executorService = executorService;
		
		this.zookeeperConnect = zookeeperConnect;
		this.groupId = groupId;
		this.topicId = topicId;
		
		if(logger.isDebugEnabled())
			logger.debug("kafka topic to websocket emitter initialized [topic="+topicId+", group="+groupId+", zkConnect="+zookeeperConnect+"]");
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		// prepare topic consumer settings and initialize the client
		Map<String, String> settings = new HashMap<>();
		settings.put(KafkaTopicConsumer.CFG_OPT_KAFKA_GROUP_ID, groupId);
		settings.put(KafkaTopicConsumer.CFG_OPT_KAFKA_ZOOKEEPER_CONNECT, zookeeperConnect);
		settings.put(KafkaTopicConsumer.CFG_OPT_KAFKA_TOPIC, topicId);		
		this.consumer = new KafkaTopicConsumer(this.messages, this.messageWaitStrategy, this.executorService);

		try {
			this.consumer.initialize(settings);
		} catch (RequiredInputMissingException e) {
			throw new RuntimeException(e);
		}
		
		// keep on running until externally halted (set running to 'false') 
		this.running = true;		
		while(this.running) {
			
			// fetch message from queue via provided wait strategy
			byte[] message = null;
			try {
				message = this.messageWaitStrategy.waitFor(messages);
			} catch(InterruptedException e) {
				// 
			}
			
			// if the byte array contains anything, forward it to web socket, otherwise try to fetch a new message from queue
			if(message != null) {
				try {
					websocketChannel.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(message)));
				} catch(Exception e) {
					logger.error("Failed to write message to websocket. Error: " + e.getMessage());
				}
			}
		}
		
		// shut down consumer and websocket channel
		this.consumer.shutdown();
		this.websocketChannel.close();
				
		if(logger.isDebugEnabled())
			logger.debug("kafka topic to websocket emitter shut down");
	}
	
	/** 
	 * Signle the emitter to halt
	 */
	public void shutdown() {
		this.running = false;
	}

}
