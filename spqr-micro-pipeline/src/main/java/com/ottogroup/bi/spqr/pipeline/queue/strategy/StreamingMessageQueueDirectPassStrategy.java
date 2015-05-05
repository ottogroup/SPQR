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
package com.ottogroup.bi.spqr.pipeline.queue.strategy;

import java.util.concurrent.TimeUnit;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;

/**
 * Default strategy if no other is provided. All timeouts are ignored and calls are directly forwarded
 * to the underlying {@link StreamingMessageQueueConsumer}. Even if the consumer returns <i>null</i> it
 * returns the value to the caller
 * @author mnxfst
 * @since Apr 21, 2015
 */
public class StreamingMessageQueueDirectPassStrategy implements StreamingMessageQueueWaitStrategy {

	public static final String STRATEGY_NAME = "directPass";
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#waitFor(com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer)
	 */
	public StreamingDataMessage waitFor(StreamingMessageQueueConsumer queue) throws InterruptedException {
		return queue.next();
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#waitFor(com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer, long, java.util.concurrent.TimeUnit)
	 */
	public StreamingDataMessage waitFor(StreamingMessageQueueConsumer queue, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
		return queue.next();
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#forceLockRelease()
	 */
	public void forceLockRelease() {
		// no-op
	}

}
