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

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Provides <b>write-only</b> access to a {@link StreamingMessageQueue}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public interface StreamingMessageQueueProducer {

	/**
	 * Returns the identifier of the queue this producer is attached to
	 * @return
	 */
	public String getQueueId();

	/**
	 * Inserts the given {@link StreamingDataMessage} into the underlying queue
	 * @param message
	 * @return
	 */
	public boolean insert(final StreamingDataMessage message);
	
	/**
	 * Returns the optional {@link StreamingMessageQueueWaitStrategy} assigned to the underlying queue
	 * @return 
	 */
	public StreamingMessageQueueWaitStrategy getWaitStrategy();
}
