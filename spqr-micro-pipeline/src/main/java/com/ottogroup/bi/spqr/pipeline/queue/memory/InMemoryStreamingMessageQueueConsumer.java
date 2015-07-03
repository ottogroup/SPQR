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
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Consumes messages from memory based {@link InMemoryStreamingMessageQueue queue}
 * @author mnxfst
 * @since Jul 3, 2015
 */
public class InMemoryStreamingMessageQueueConsumer implements StreamingMessageQueueConsumer {

	/** identifier of queue this consumer is attached to */
	private final String queueId;
	/** queue the consumer reads from */
	private final ConcurrentLinkedQueue<StreamingDataMessage> queue;
	/** assigned wait strategy */
	private final StreamingMessageQueueWaitStrategy waitStrategy;
	/** counter instance used for metric collection */
	private Counter messageRetrievalCounter = null;
	
	/**
	 * Initializes the consumer using the provided input
	 * @param queueId
	 * @param queue
	 * @param waitStrategy
	 */
	public InMemoryStreamingMessageQueueConsumer(final String queueId, final ConcurrentLinkedQueue<StreamingDataMessage> queue, final StreamingMessageQueueWaitStrategy waitStrategy) {
		this.queueId = queueId;
		this.queue = queue;
		this.waitStrategy = waitStrategy;
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer#getQueueId()
	 */
	public String getQueueId() {
		return this.queueId;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer#next()
	 */
	public StreamingDataMessage next() {
		final StreamingDataMessage nextMessage = this.queue.poll();
		if(this.messageRetrievalCounter != null && nextMessage != null)
			this.messageRetrievalCounter.inc();
		return nextMessage;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer#getWaitStrategy()
	 */
	public StreamingMessageQueueWaitStrategy getWaitStrategy() {
		return this.waitStrategy;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer#setMessageRetrievalCounter(com.codahale.metrics.Counter)
	 */
	public void setMessageRetrievalCounter(Counter counter) {
		this.messageRetrievalCounter = counter;
	}

}
