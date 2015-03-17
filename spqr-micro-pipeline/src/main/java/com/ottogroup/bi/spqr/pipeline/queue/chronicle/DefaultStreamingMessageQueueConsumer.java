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
package com.ottogroup.bi.spqr.pipeline.queue.chronicle;

import net.openhft.chronicle.ExcerptTailer;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;

/**
 * Default {@link StreamingMessageQueueConsumer} implementation accessing {@link DefaultStreamingMessageQueue}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class DefaultStreamingMessageQueueConsumer implements StreamingMessageQueueConsumer {

	private final ExcerptTailer queueReader;
	
	/**
	 * Initializes the consumer using the provided input
	 * @param queueReader
	 */
	public DefaultStreamingMessageQueueConsumer(final ExcerptTailer queueReader) {
		this.queueReader = queueReader;
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer#next()
	 */
	public StreamingDataMessage next() {
		
		// check if a new message is available and read it from chronicle if possible
		if(queueReader.nextIndex()) {
			long timestamp = queueReader.readLong();
			int bytes = queueReader.readInt();
			byte[] body = new byte[bytes];
			queueReader.read(body);
			queueReader.finish();
			return new StreamingDataMessage(body, timestamp);
		}
		
		// otherwise return null;
		return null;
		
	}

}
