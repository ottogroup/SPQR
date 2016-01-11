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

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;

/**
 * Implements a wait strategy in the style of {@link https://github.com/LMAX-Exchange/disruptor/blob/master/src/main/java/com/lmax/disruptor/SleepingWaitStrategy.java}.
 * It tries to read messages from an assigned {@link Queue}. If there is no content the strategy blocks for a fixed time. 
 * @author mnxfst
 * @since Jan 11, 2016
 */
public class StreamingMessageQueueSleepingWaitStrategy implements StreamingMessageQueueWaitStrategy {

	public static final String STRATEGY_NAME = "sleepingWait";

	private final int retries = 200;
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#waitFor(com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer)
	 */
	public StreamingDataMessage waitFor(StreamingMessageQueueConsumer queue) throws InterruptedException {
		
		StreamingDataMessage message = null;
		int counter = retries;
		while((message = queue.next()) == null) {			
			if(counter > 100) {
				--counter;
			} else if(counter > 0) {
				--counter;
				Thread.yield();
			} else {
				LockSupport.parkNanos(1l);
			}			
		}
		return message;		
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#waitFor(com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer, long, java.util.concurrent.TimeUnit)
	 */
	public StreamingDataMessage waitFor(StreamingMessageQueueConsumer queue, long timeout, TimeUnit timeoutUnit) throws InterruptedException {

		StreamingDataMessage message = null;
		int counter = retries;
		while((message = queue.next()) == null) {			
			if(counter > 100) {
				--counter;
			} else if(counter > 0) {
				--counter;
				Thread.yield();
			} else {
				LockSupport.parkNanos(1l);
			}			
		}
		return message;		
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy#forceLockRelease()
	 */
	public void forceLockRelease() {
		// nothing to do
	}

}
