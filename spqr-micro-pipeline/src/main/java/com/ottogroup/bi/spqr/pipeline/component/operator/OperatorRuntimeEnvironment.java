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

import java.util.HashMap;
import java.util.Map;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;

/**
 * Runtime environment to execute {@link MicroPipelineComponent}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class OperatorRuntimeEnvironment implements Runnable {

	/** provides read access to underlying queue  */
	private StreamingMessageQueueConsumer messageConsumer = null;
	/** provides write access to underlying queues  */
	private Map<String, StreamingMessageQueueProducer> messageProducers = new HashMap<>();
	
	/** indicates whether the operator runtime is still running or not */
	private boolean running = false;
	
	public OperatorRuntimeEnvironment() {
		this.running = true;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		while(running) {
			
//			StreamingDataMessage message = messageReader.readObject(StreamingDataMessage.class);
//			messageReader.finish();
//			
//			System.out.println(message.getBody());
		}		
	}
	
	/**
	 * Shuts down the runtime environment as well as the attached {@link Operator}
	 */
	public void shutdown() {
		
	}
}
