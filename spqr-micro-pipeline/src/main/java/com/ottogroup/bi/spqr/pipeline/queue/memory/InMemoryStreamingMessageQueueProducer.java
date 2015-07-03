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

import java.util.concurrent.ConcurrentLinkedQueue;

import com.codahale.metrics.Counter;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Produces messages to attached {@link InMemoryStreamingMessageQueue}
 * @author mnxfst
 * @since Jul 3, 2015
 */
public class InMemoryStreamingMessageQueueProducer implements StreamingMessageQueueProducer {

	/** identifier of queue this consumer is attached to */
	private final String queueId;
	/** queue the consumer reads from */
	private final ConcurrentLinkedQueue<StreamingDataMessage> queue;
	/** assigned wait strategy for fetching messages */
	private final StreamingMessageQueueWaitStrategy waitStrategy;
	/** counts the number of message insertions */
	private Counter messageInsertionCounter = null;

	/**
	 * Initializes the producer using the provided input
	 * @param queueId
	 * @param queue
	 * @param waitStrategy
	 */
	public InMemoryStreamingMessageQueueProducer(final String queueId, final ConcurrentLinkedQueue<StreamingDataMessage> queue, final StreamingMessageQueueWaitStrategy waitStrategy) {
		this.queueId = queueId;
		this.queue = queue;
		this.waitStrategy = waitStrategy;
	}
	

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer#insert(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public boolean insert(StreamingDataMessage message) {		
		if(message != null) {
			this.queue.offer(message);
			if(this.messageInsertionCounter != null)
				this.messageInsertionCounter.inc();
		}		
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer#getWaitStrategy()
	 */
	public StreamingMessageQueueWaitStrategy getWaitStrategy() {
		return this.waitStrategy;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer#setMessageInsertionCounter(com.codahale.metrics.Counter)
	 */
	public void setMessageInsertionCounter(Counter counter) {
		this.messageInsertionCounter = counter;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer#getQueueId()
	 */
	public String getQueueId() {
		return this.queueId;
	}

}
