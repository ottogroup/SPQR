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
package com.ottogroup.bi.spqr.pipeline.queue;

import com.codahale.metrics.Counter;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Provides <b>read-only</b> access to a {@link StreamingMessageQueue}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public interface StreamingMessageQueueConsumer {

	/**
	 * Returns the identifier of the queue this consumer is attached to
	 * @return
	 */
	public String getQueueId();
	
	/**
	 * Retrieves the next {@link StreamingDataMessage} from the underlying queue
	 * @return
	 */
	public StreamingDataMessage next();

	/**
	 * Returns the optional {@link StreamingMessageQueueWaitStrategy} assigned to the queue
	 * @return 
	 */
	public StreamingMessageQueueWaitStrategy getWaitStrategy();

	/**
	 * Attaches an optional {@link Counter} instance for counting retrieved messages 
	 * @param counter
	 */
	public void setMessageRetrievalCounter(final Counter counter);	
}
