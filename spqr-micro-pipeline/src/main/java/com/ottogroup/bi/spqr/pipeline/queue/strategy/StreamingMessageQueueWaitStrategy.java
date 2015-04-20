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
 * Interface to be implemented by all classes providing a wait strategy for
 * reading content from {@link StreamingMessageQueueConsumer} instances
 * @author mnxfst
 * @since Apr 20, 2015
 */
public interface StreamingMessageQueueWaitStrategy {

	/** 
	 * Wait for next element from referenced {@link StreamingMessageQueueConsumer}.  
	 * @param queue
	 * @return
	 * @throws InterruptedException
	 */
	public StreamingDataMessage waitFor(final StreamingMessageQueueConsumer queue) throws InterruptedException;
	
	/**
	 * Wait for next element from referenced {@link StreamingMessageQueueConsumer}. If the timeout is 
	 * reached the result may contain <i>null</i>.
	 * @param queue
	 * @param timeout
	 * @param timeoutUnit
	 * @return
	 * @throws InterruptedException
	 */
	public StreamingDataMessage waitFor(final StreamingMessageQueueConsumer queue, final long timeout, final TimeUnit timeoutUnit) throws InterruptedException;
	
	/**
	 * Forces release of existing locks
	 */
	public void forceLockRelease();
	
}
