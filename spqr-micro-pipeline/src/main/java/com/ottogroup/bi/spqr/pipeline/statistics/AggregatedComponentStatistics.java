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
package com.ottogroup.bi.spqr.pipeline.statistics;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds aggregated statistical information about a single pipeline component. Values tracked are:
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
public class AggregatedComponentStatistics implements Serializable {

	private static final long serialVersionUID = 3548413649029481410L;
	
	public static final int SIZE_OF_INT = Integer.SIZE / Byte.SIZE;
	public static final int SIZE_OF_LONG = Long.SIZE / Byte.SIZE;
	
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
	public AggregatedComponentStatistics() {		
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
	public AggregatedComponentStatistics(final String componentId, final int numOfMessages, final long startTime, final long endTime, 
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
	

	/**
	 * Converts the provided byte array into a {@link AggregatedComponentStatistics} representation
	 * @param statsContent
	 * @return
	 */
	public static AggregatedComponentStatistics fromBytes(final byte[] statsContent) {
		
		AggregatedComponentStatistics stats = new AggregatedComponentStatistics();
		ByteBuffer buf = ByteBuffer.wrap(statsContent);
		
		// ensure that the order is the same as when populating the array 
		stats.setNumOfMessages(buf.getInt());
		stats.setStartTime(buf.getLong());
		stats.setEndTime(buf.getLong());
		stats.setMinDuration(buf.getInt());
		stats.setMaxDuration(buf.getInt());
		stats.setAvgDuration(buf.getInt());
		stats.setMinSize(buf.getInt());
		stats.setMaxSize(buf.getInt());
		stats.setAvgSize(buf.getInt());
		stats.setErrors(buf.getInt());
		
		byte[] componentId = new byte[buf.getInt()];
		buf.get(componentId);
		
		stats.setComponentId(new String(componentId));
		
		return stats;		
	}
	
	/**
	 * Convert this {@link MicroPipelineStatistics} instance into its byte array representation 
	 * @return
	 */
	public byte[] toBytes() {
		
		byte[] cid = (this.componentId != null ? this.componentId.getBytes() : new byte[0]);

		// 9x SIZE_OF_INT: 8 attribute values, 1 size of component identifier representation
		// 2x SIZE_OF_LONG: start time and end time
		// 1x length of component identifier representation
		
		ByteBuffer buffer = ByteBuffer.allocate(9 * SIZE_OF_INT + 2 * SIZE_OF_LONG  + cid.length);

		buffer.putInt(this.numOfMessages);
		buffer.putLong(this.startTime);
		buffer.putLong(this.endTime);
		buffer.putInt(this.minDuration);
		buffer.putInt(this.maxDuration);
		buffer.putInt(this.avgDuration);
		buffer.putInt(this.minSize);
		buffer.putInt(this.maxSize);
		buffer.putInt(this.avgSize);
		buffer.putInt(this.errors);
		buffer.putInt(cid.length);
		buffer.put(cid);
		
		return buffer.array();		
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
