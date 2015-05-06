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
package com.ottogroup.bi.spqr.pipeline.stats;

import org.apache.commons.lang3.SerializationUtils;

import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;


/**
 * Attached to each component runtime environment, instances of this class collect {@link MicroPipelineComponentStatistics}
 * and return them when asked for 
 * @author mnxfst
 * @since May 6, 2015
 */
public class ComponentStatisticsCollector {

	private final MicroPipelineComponentStatistics stats;
	private int durationSum = 0;
	private int sizeSum = 0;
	
	/**
	 * Initializes the collector using the provided input
	 * @param componentId
	 */
	public ComponentStatisticsCollector(final String componentId) {
		this.stats = new MicroPipelineComponentStatistics();
		this.stats.setComponentId(componentId);
		this.stats.setStartTime(System.currentTimeMillis());
	}
	
	/**
	 * Report stats information for each meassage
	 * @param size
	 * @param duration
	 * @param isError
	 */
	public void reportStats(final int size, final int duration, final boolean isError) {
		
		stats.setNumOfMessages(stats.getNumOfMessages()+1);
		if(isError)
			stats.setErrors(stats.getErrors()+1);
		
		if(stats.getMinDuration() > duration)
			stats.setMinDuration(duration);
		if(stats.getMaxDuration() < duration)
			stats.setMaxDuration(duration);
		this.durationSum = this.durationSum + duration;
		
		if(stats.getMinSize() > size)
			stats.setMinSize(size);
		if(stats.getMaxSize() < size)
			stats.setMaxSize(size);
		this.sizeSum = this.sizeSum + size;
	}
	
	public StreamingDataMessage getStatistics() {
		
		stats.setAvgDuration(this.durationSum / this.stats.getNumOfMessages());
		stats.setAvgSize(this.sizeSum / this.stats.getNumOfMessages());
		stats.setEndTime(System.currentTimeMillis());
		byte[] body = SerializationUtils.serialize(stats);
		
		////////////////////////////////////////////
		// reset container
		stats.setAvgDuration(0);
		stats.setAvgSize(0);
		stats.setEndTime(0);
		stats.setErrors(0);
		stats.setMaxDuration(Integer.MIN_VALUE);
		stats.setMaxSize(Integer.MIN_VALUE);
		stats.setMinDuration(Integer.MAX_VALUE);
		stats.setMinSize(Integer.MAX_VALUE);
		stats.setNumOfMessages(0);
		stats.setStartTime(System.currentTimeMillis());
		////////////////////////////////////////////
		
		return new StreamingDataMessage(body, System.currentTimeMillis());
	}
	
}
