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
package com.ottogroup.bi.spqr.pipeline.component.emitter;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * To be implemented by all {@link MicroPipelineComponent micro pipeline components} that export data
 * to any destination, eg. Kafka, ElasticSearch or file system 
 * @author mnxfst
 * @since Dec 15, 2014
 */
public interface Emitter {

	/**
	 * Provides a new message to the operator
	 * @param message
	 */
	public void onMessage(final StreamingDataMessage message);

	/**
	 * Returns the total number of messages processed by this component
	 * @return
	 */
	public long getTotalNumOfMessages();

}
