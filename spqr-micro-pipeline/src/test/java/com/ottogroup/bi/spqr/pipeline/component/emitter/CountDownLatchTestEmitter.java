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
package com.ottogroup.bi.spqr.pipeline.component.emitter;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Receives messages from attached queue and counts down an assigned {@link CountDownLatch} for
 * each one 
 * @author mnxfst
 * @since Mar 17, 2015
 */
public class CountDownLatchTestEmitter implements Emitter {

	private String id = null;
	private CountDownLatch latch = null;
	private long messageCount = 0;
	private long awaitMessages = 0;
	private int max = 0;
	private int min = Integer.MAX_VALUE;
	private int avg = 0;	
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
		this.awaitMessages = Long.valueOf(properties.getProperty("await"));
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		return false;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter#getTotalNumOfMessages()
	 */
	public long getTotalNumOfMessages() {
		return this.messageCount;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public boolean onMessage(StreamingDataMessage message) {
		
		try {
		this.latch.countDown();
		this.messageCount++;
		if(messageCount >= awaitMessages)
			System.out.println("Received " + messageCount + " messages");
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return true;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.EMITTER;
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
