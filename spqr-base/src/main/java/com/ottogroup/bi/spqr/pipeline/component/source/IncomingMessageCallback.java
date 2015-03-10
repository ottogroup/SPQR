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

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;


/**
 * Callback invoked for each {@link StreamingDataMessage} received by a {@link Source}. An instance
 * of a class implementing this interface is passed on to the source by invoking 
 * {@link Source#setIncomingMessageCallbale(IncomingMessageCallback)}. The source implementation must
 * take care of it and ensure that incoming messages are handed over in order to get them into to 
 * {@link MicroPipeline} for further processing. 
 * @author mnxfst
 * @since Mar 5, 2015
 */
public interface IncomingMessageCallback {

	/**
	 * Executed for each incoming {@link StreamingDataMessage} received by a {@link Source}. 
	 * @param message
	 */
	public void onMessage(final StreamingDataMessage message);

	
}
