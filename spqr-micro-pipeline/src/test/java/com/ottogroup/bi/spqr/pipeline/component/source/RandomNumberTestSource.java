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

import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Generates random numbers until it is stopped or it reaches a max of generated numbers as configured
 * @author mnxfst
 * @since Mar 6, 2015
 */
public class RandomNumberTestSource implements Source {

	private static final Logger logger = Logger.getLogger(RandomNumberTestSource.class);
	public static final String CFG_MAX_NUM_GENERATED = "maxNumGenerated";
	public static final String CFG_SEED = "seed";
	
	private String id = null;
	private int maxNumGenerated = 0;
	private long seed = 0;
	private boolean running = false;
	private IncomingMessageCallback callback;
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) {
		this.maxNumGenerated = Integer.parseInt(StringUtils.trim(properties.getProperty(CFG_MAX_NUM_GENERATED)));
		try {
			this.seed = Long.parseLong(StringUtils.trim(properties.getProperty(CFG_SEED)));
		} catch(Exception e) {
			//
		}
		this.running = true;
	}


	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		logger.info("Starting random number generator");
		Random rnd = null;
		if(seed > 0)
			rnd = new Random(seed);
		else
			rnd = new Random();
		
		int count = 0;
		long s1 = System.currentTimeMillis();
		while(running && maxNumGenerated != 0) {
			count ++;
			this.callback.onMessage(new StreamingDataMessage(""+rnd.nextInt(), System.currentTimeMillis())); // string conversion takes too long!
			
			maxNumGenerated--;
			
		}
		logger.info(count + " numbers generated in: " + (System.currentTimeMillis()-s1)+"ms");
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		this.running = false;
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.source.Source#setIncomingMessageCallback(com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback)
	 */
	public void setIncomingMessageCallback(IncomingMessageCallback incomingMessageCallback) {
		this.callback = incomingMessageCallback; 
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.SOURCE;
	}

}
