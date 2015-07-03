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
package com.ottogroup.bi.spqr.pipeline.queue.memory;

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueBlockingWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueDirectPassStrategy;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Memory based implementation of {@link StreamingDataMessage}. Compared to {@link DefaultStreamingMessageQueue}
 * this is fully based on in-memory structures and thus does not provide any message persistence features.
 * @author mnxfst
 * @since Jul 3, 2015
 */
public class InMemoryStreamingMessageQueue implements StreamingMessageQueue {

	/** our faithful logging facility ..... ;-) */
	private static final Logger logger = Logger.getLogger(InMemoryStreamingMessageQueue.class);
	
	/////////////////////////////////////////////////////////////////////
	// available configuration options 
	public static final String CFG_QUEUE_MESSAGE_WAIT_STRATEGY = "queue.message.waitStrategy";
	/////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////
	// setting for 'type' property to get an instance of this type
	public static final String CFG_QUEUE_TYPE = "memory";
	/////////////////////////////////////////////////////////////////////

	/** unique queue identifier */
	private String id = null;
	/** internal queue holding elements */
	private ConcurrentLinkedQueue<StreamingDataMessage> queue = new ConcurrentLinkedQueue<StreamingDataMessage>();
	/** message queue consumer */
	private InMemoryStreamingMessageQueueConsumer queueConsumer = null;
	/** message queue producer */
	private InMemoryStreamingMessageQueueProducer queueProducer = null;
	/** wait strategy */
	private StreamingMessageQueueWaitStrategy queueWaitStrategy = null;
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException {
		
		////////////////////////////////////////////////////////////////////////////////
		// extract and validate input
		if(properties == null)
			throw new RequiredInputMissingException("Missing required properties");		
	
		if(StringUtils.isBlank(this.id))
			throw new RequiredInputMissingException("Missing required queue identifier");

		this.queueWaitStrategy = getWaitStrategy(StringUtils.trim(properties.getProperty(CFG_QUEUE_MESSAGE_WAIT_STRATEGY)));
		////////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////////
		// initialize producer and consumer instances
		this.queueProducer = new InMemoryStreamingMessageQueueProducer(this.id, this.queue, this.queueWaitStrategy);
		this.queueConsumer = new InMemoryStreamingMessageQueueConsumer(this.id, this.queue, this.queueWaitStrategy);
		////////////////////////////////////////////////////////////////////////////////

		logger.info("In-memory streaming message queue successfully initialized");
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#shutdown()
	 */
	public boolean shutdown() {
		this.queue.clear();
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#insert(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public boolean insert(StreamingDataMessage message) {
		if(message != null)
			return this.queue.offer(message);
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#next()
	 */
	public StreamingDataMessage next() {
		return this.queue.poll();
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getProducer()
	 */
	public StreamingMessageQueueProducer getProducer() {
		return this.queueProducer;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getConsumer()
	 */
	public StreamingMessageQueueConsumer getConsumer() {
		return this.queueConsumer;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#setMessageInsertionCounter(com.codahale.metrics.Counter)
	 */
	public void setMessageInsertionCounter(Counter counter) {
		this.queueProducer.setMessageInsertionCounter(counter);
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#setMessageRetrievalCounter(com.codahale.metrics.Counter)
	 */
	public void setMessageRetrievalCounter(Counter counter) {
		this.queueConsumer.setMessageRetrievalCounter(counter);
	}

	/**
	 * Return an instance of the referenced {@link StreamingMessageQueueWaitStrategy}
	 * @param waitStrategyName name of strategy to instantiate (eg. {@link StreamingMessageQueueBlockingWaitStrategy#STRATEGY_NAME} (default))
	 * @return
	 */
	protected StreamingMessageQueueWaitStrategy getWaitStrategy(final String waitStrategyName) {			
		if(StringUtils.equalsIgnoreCase(waitStrategyName, StreamingMessageQueueDirectPassStrategy.STRATEGY_NAME))
			return new StreamingMessageQueueDirectPassStrategy();
		return new StreamingMessageQueueBlockingWaitStrategy();
	}

	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getId()
	 */
	public String getId() {
		return this.id;
	}

}
