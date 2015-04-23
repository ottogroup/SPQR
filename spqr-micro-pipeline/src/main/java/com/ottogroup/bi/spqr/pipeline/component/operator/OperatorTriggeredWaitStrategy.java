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
package com.ottogroup.bi.spqr.pipeline.component.operator;

import java.util.Properties;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Triggered directly by {@link DelayedResponseOperator} 
 * @author mnxfst
 * @since Apr 23, 2015
 */
public class OperatorTriggeredWaitStrategy implements DelayedResponseOperatorWaitStrategy {

	public static final String WAIT_STRATEGY_NAME = "messageCount";

	private DelayedResponseCollector delayedResponseCollector = null;
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// no code as it is triggered directly from operator ... no need for async behavior here
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#setDelayedResponseCollector(com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseCollector)
	 */
	public void setDelayedResponseCollector(DelayedResponseCollector delayedResponseCollector) {
		this.delayedResponseCollector = delayedResponseCollector;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#release()
	 */
	public void release() {
		this.delayedResponseCollector.retrieveMessages();
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
		// do nothing ... as the operator controls the result retrieval
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#shutdown()
	 */
	public void shutdown() {
		// do nothing ... as there is nothing to shut down
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) {
		// do nothing ... as this serves only as a bridge between the operator and the runtime environment  
	}

}
