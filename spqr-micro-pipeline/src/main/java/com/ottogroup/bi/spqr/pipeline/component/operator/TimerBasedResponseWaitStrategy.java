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

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Implements a timer based {@link DelayedResponseOperatorWaitStrategy}.  
 * @author mnxfst
 * @since Mar 12, 2015
 *
 */
public class TimerBasedResponseWaitStrategy implements DelayedResponseOperatorWaitStrategy {
	
	private static final Logger logger = Logger.getLogger(TimerBasedResponseWaitStrategy.class);
	
	public static final String WAIT_STRATEGY_NAME = "timerBased";
	public static final int DEFAULT_MAX_DURATION = 1000;
	public static final String CFG_MAX_DURATION = "maxDuration";

	
	private DelayedResponseCollector delayedResponseCollector = null;
	private boolean running = false;
	/** provided number of milliseconds to wait between two result collections */
	private long maxDuration = DEFAULT_MAX_DURATION;  
	/** time of last collector run */
	private long lastResultCollection = System.currentTimeMillis();

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) {
		try {
			this.maxDuration = Long.parseLong(properties.getProperty(CFG_MAX_DURATION));
			if(this.maxDuration < 1)
				this.maxDuration = DEFAULT_MAX_DURATION;
		} catch(Exception e) {
			if(logger.isDebugEnabled())
				logger.debug("Failed to parse setting '"+CFG_MAX_DURATION+". Reason: " + e.getMessage());
			this.maxDuration = DEFAULT_MAX_DURATION;
		}
		
		if(logger.isDebugEnabled())
			logger.debug("timer based wait strategy initialzed [duration="+maxDuration+"]");
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.running = true;
		while(running) {
			long tmp = System.currentTimeMillis() - lastResultCollection;
			if(tmp < maxDuration) {
				try {
					Thread.sleep(maxDuration - tmp);
				} catch (Exception e) {
					// keep on 
				}
			} else {
				release();
				try {
					Thread.sleep(maxDuration);
				} catch(Exception e) {
					// keep on
				}
			}
		}
	}


	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#release()
	 */
	public void release() {
		this.delayedResponseCollector.retrieveMessages();
		this.lastResultCollection = System.currentTimeMillis();
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public void onMessage(StreamingDataMessage message) {
		// do nothing as the strategy implementation is timer not content based
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
