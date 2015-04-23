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
package com.ottogroup.bi.spqr.operator.esper;

import java.util.Map;
import java.util.Properties;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator;
import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Integrates the {@link http://espertech.com/ ESPER} project into SPQR pipelines.
 * @author mnxfst
 * @since Apr 23, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR, name="esperOperator", version="0.0.1", description="ESPER integration operator")
public class EsperOperator implements DelayedResponseOperator {

	private String id = null;
	private long totalNumOfMessages = 0;
	private long numOfMessagesSinceLastResult = 0;
	private DelayedResponseOperatorWaitStrategy waitStrategy = null;
	

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.DELAYED_RESPONSE_OPERATOR;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getResult()
	 */
	public StreamingDataMessage[] getResult() {
		return null;
	}

	public void update(Map<String, Object> eventMap) {
        for(String s : eventMap.keySet()) {
        	System.out.println(s + " --> " + eventMap.get(s));
        }
    }
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#getNumberOfMessagesSinceLastResult()
	 */
	public long getNumberOfMessagesSinceLastResult() {
		return this.numOfMessagesSinceLastResult;
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

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperator#setWaitStrategy(com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy)
	 */
	public void setWaitStrategy(DelayedResponseOperatorWaitStrategy waitStrategy) {
		this.waitStrategy = waitStrategy;
	}

}
