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
 * Counts the number of {@link StreamingDataMessage} instances received and signals {@link DelayedResponseCollector} to collect results
 * after having seen a configured number of items 
 * @author mnxfst
 * @since Mar 12, 2015
 *
 */
public class MessageCountResponseWaitStrategy implements DelayedResponseOperatorWaitStrategy {
	
	public static final String WAIT_STRATEGY_NAME = "messageCount";
	public static final int DEFAULT_MAX_MESSAGE_COUNT = 50;
	public static final String CFG_MAX_MESSAGE_COUNT_KEY = "maxMessages";
	
	private DelayedResponseCollector delayedResponseCollector = null;
	private boolean running = false;
	/** holds the max number of message after which the strategy signals the collector to collect results. default is set to 50 */
	private int maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT; 
	/** counts the number of messages seen after last value collection or since start - reset to 0 after each collection run */
	private int messageCount = 0;
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) {		
		try {
			this.maxMessageCount = Integer.parseInt(properties.getProperty(DelayedResponseOperator.CFG_WAIT_STRATEGY_SETTINGS_PREFIX + CFG_MAX_MESSAGE_COUNT_KEY));
			if(this.maxMessageCount < 1)
				this.maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT;
		} catch(Exception e) {
			this.maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT;
		}		
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.running = true;
		this.messageCount = 0;
		while(this.running) {
			// TODO replace active waiting 
		}
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#release()
	 */
	public void release() {
		this.delayedResponseCollector.retrieveMessages();
		this.messageCount = 0;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
		messageCount++;		
		if(this.messageCount >= this.maxMessageCount) {
			release(); // ask collector to collect messages and reset counter to 0
		}
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#shutdown()
	 */
	public void shutdown() {
		this.running = false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#setDelayedResponseCollector(com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseCollector)
	 */
	public void setDelayedResponseCollector(DelayedResponseCollector delayedResponseCollector) {
		this.delayedResponseCollector = delayedResponseCollector;
	}

}
