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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds statistical information about a single pipeline component. Values tracked are:
 * <ul>
 *   <li>component identifier</li>
 *   <li>number of messages processed for specified time frame</li>
 *   <li>start time</li>
 *   <li>end time</li>
 *   <li>min. duration required for processing a single message (provided in milliseconds)</li>
 *   <li>max. duration required for processing a single message (provided in milliseconds)</li>
 *   <li>avg. duration required for processing a single message (provided in milliseconds)</li>
 *   <li>min. message size found in specified time frame</li>
 *   <li>max. message size found in specified time frame</li>
 *   <li>avg. message size found in specified time frame</li>
 *   <li>errors</li>
 * </ul>
 * @author mnxfst
 * @since May 6, 2015
 */
public class MicroPipelineComponentStatistics implements Serializable {

	private static final long serialVersionUID = 3548413649029481410L;
	
	/** identifier of pipeline component which generated the stats */
	@JsonProperty(value="cid", required=true)
	private String componentId = null;
	/** number of messages processed since last event */
	@JsonProperty(value="numMsg", required=true)
	private int numOfMessages = 0;	
	/** start time */
	@JsonProperty(value="st", required=true)
	private long startTime = 0;
	/** end time */
	@JsonProperty(value="et", required=true)
	private long endTime = 0;
	/** min. duration required for processing a single message */
	@JsonProperty(value="minDur")
	private int minDuration = Integer.MAX_VALUE;
	/** max. duration required for processing a single message */
	@JsonProperty(value="maxDur")
	private int maxDuration = Integer.MIN_VALUE;
	/** avg. duration required for processing a single message */
	@JsonProperty(value="avgDur")
	private int avgDuration = 0;
	/** min. message size */
	@JsonProperty(value="minSize")
	private int minSize = Integer.MAX_VALUE;
	/** max. message size */
	@JsonProperty(value="maxSize")
	private int maxSize = Integer.MIN_VALUE;
	/** avg. message size */
	@JsonProperty(value="avgSize")
	private int avgSize = 0;
	/** error rate */
	@JsonProperty(value="err", required=true)
	private int errors = 0;
	
	/**
	 * Default constructor
	 */
	public MicroPipelineComponentStatistics() {		
	}
	
	/**
	 * Initializes the component stats using the provided input 
	 * @param componentId
	 * @param numOfMessages
	 * @param startTime
	 * @param endTime
	 * @param minDuration
	 * @param maxDuration
	 * @param avgDuration
	 * @param minSize
	 * @param maxSize
	 * @param avgSize
	 * @param errors
	 */
	public MicroPipelineComponentStatistics(final String componentId, final int numOfMessages, final long startTime, final long endTime, 
			final int minDuration, final int maxDuration, final int avgDuration, final int minSize, final int maxSize, final int avgSize,
			final int errors) {
		this.componentId = componentId;
		this.numOfMessages = numOfMessages;
		this.startTime = startTime;
		this.endTime = endTime;
		this.minDuration = minDuration;
		this.maxDuration = maxDuration;
		this.avgDuration = avgDuration;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.avgSize = avgSize;
		this.errors = errors;
	}
	
	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public int getNumOfMessages() {
		return numOfMessages;
	}

	public void setNumOfMessages(int numOfMessages) {
		this.numOfMessages = numOfMessages;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getMinDuration() {
		return minDuration;
	}

	public void setMinDuration(int minDuration) {
		this.minDuration = minDuration;
	}

	public int getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(int maxDuration) {
		this.maxDuration = maxDuration;
	}

	public int getAvgDuration() {
		return avgDuration;
	}

	public void setAvgDuration(int avgDuration) {
		this.avgDuration = avgDuration;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getAvgSize() {
		return avgSize;
	}

	public void setAvgSize(int avgSize) {
		this.avgSize = avgSize;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}
	
	
}
