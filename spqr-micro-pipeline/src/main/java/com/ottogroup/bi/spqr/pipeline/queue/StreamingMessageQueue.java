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

import java.util.Properties;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.MicroPipeline;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Provides the interface for a queue used internally inside the {@link MicroPipeline} to interconnect 
 * {@link MicroPipelineComponent} instances. It transports {@link StreamingDataMessage} entities only
 * and must be accessed by only one producer but supports multiple consumers.
 * @author mnxfst
 * @since Mar 5, 2015
 * TODO provide statistics interface
 */
public interface StreamingMessageQueue  {


	/**
	 * Sets the component identifier
	 * @param id
	 */
	public void setId(final String id);
	
	/**
	 * Returns the component identifier which is unique
	 * within the boundaries of a {@link MicroPipeline}
	 * @return
	 */
	public String getId();
	
	/**
	 * Initializes the component before it gets executed
	 * @param properties
	 * @throws RequiredInputMissingException
	 */
	public void initialize(final Properties properties) throws RequiredInputMissingException;
	
	/**
	 * Tells the component to shut itself down
	 */
	public boolean shutdown();
	
	/**
	 * Inserts the given {@link StreamingDataMessage} into the underlying queue
	 * @param message
	 * @return
	 */
	public boolean insert(final StreamingDataMessage message);
	
	/**
	 * Retrieves the next {@link StreamingDataMessage} from the underlying queue
	 * @return
	 */
	public StreamingDataMessage next();
	
	/**
	 * Returns a {@link StreamingMessageQueueProducer} instance with access to underlying queue
	 * @return
	 */
	public StreamingMessageQueueProducer getProducer();
	
	/**
	 * Returns a {@link StreamingMessageQueueConsumer} instance with access to underlying queue
	 * @return
	 */
	public StreamingMessageQueueConsumer getConsumer();
	
	
}

