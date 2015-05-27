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
package com.ottogroup.bi.spqr.operator.json.flatten;

import java.util.Properties;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperator;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Flattens JSON content to key/value structure where the key is represented by the concatenated JSON nodes and the key is the value attached to
 * the final node belonging to the path 
 * @author mnxfst
 * @since May 27, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR, name="jsonContentFlattening", version="0.0.1", description="Flattens JSON content to k/v structure")
public class JsonContentFlattening implements DirectResponseOperator {

	private String id;
	private long totalNumOfMessages;

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException,	ComponentInitializationFailedException {
		// nothing to do ... right now
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DirectResponseOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public StreamingDataMessage[] onMessage(StreamingDataMessage message) {
		return new StreamingDataMessage[]{message};
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.DIRECT_RESPONSE_OPERATOR;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.Operator#getTotalNumOfMessages()
	 */
	public long getTotalNumOfMessages() {
		return this.totalNumOfMessages;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}

}
