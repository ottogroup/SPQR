/**
 * Copyright 2014 Otto (GmbH & Co KG)
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
package com.ottogroup.bi.spqr.pipeline.component.operator;

import java.util.Properties;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Interface to implement when providing a new strategy to be applied on {@link DelayedResponseOperator} 
 * while waiting before sending response messages
 * @author mnxfst
 * @since Mar 11, 2015
 */
public interface DelayedResponseOperatorWaitStrategy extends Runnable {

	/**
	 * Assigns the {@link DelayedResponseCollector message collector} for retrieving delayed
	 * content from {@link DelayedResponseOperator}
	 * @param delayedResponseCollector
	 */
	public void setDelayedResponseCollector(final DelayedResponseCollector delayedResponseCollector);
	
	/**
	 * Signals towards the wait strategy that message retrieval must be forced no matter
	 * if the condition evaluated by the strategy holds or not. The instance must be 
	 * re-set to mark a new collection period afterwards.  
	 */
	public void release();
	
	/**
	 * Notifies the wait strategy about each new {@link StreamingDataMessage} passed to the attached
	 * {@link DelayedResponseOperator}. The strategy may ignore it as it is timer based or evaluate its 
	 * body as it is content based or simply count the number of incoming messages as it is volume based.
	 * @param message
	 */
	public void onMessage(final StreamingDataMessage message);
	
	/**
	 * Signals the strategy to shut itself down
	 */
	public void shutdown();
	
	/**
	 * Initializes the wait strategy using the provided {@link Properties}
	 * @param properties
	 */
	public void initialize(final Properties properties);
	
}
