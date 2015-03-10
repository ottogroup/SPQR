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
package com.ottogroup.bi.spqr.pipeline.component.source;

import java.awt.Component;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;

/**
 * To be implemented by all {@link Component components} that receive data
 * from any source and provide it to the {@link Pipeline}
 * @author mnxfst
 * @since Dec 15, 2014
 */
public interface Source extends MicroPipelineComponent, Runnable {

	/**
	 * Assigns the {@link IncomingMessageCallback} that must be called for each
	 * incoming message
	 * @param incomingMessageCallback
	 */
	public void setIncomingMessageCallback(final IncomingMessageCallback incomingMessageCallback);
	
}