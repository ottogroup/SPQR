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

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;


/**
 * Receives an input {@link StreamingDataMessage message} and generates a response immediately, eg. filter, map or merge operator 
 * @author mnxfst
 * @since Dec 14, 2014
 */
public interface DirectResponseOperator extends Operator {

	/**
	 * Receives a single message, processes its content and responds with either
	 * zero, one or an arbitrary number of messages
	 * @param message
	 * @return
	 */
	public StreamingDataMessage[] onMessage(final StreamingDataMessage message);	
}
