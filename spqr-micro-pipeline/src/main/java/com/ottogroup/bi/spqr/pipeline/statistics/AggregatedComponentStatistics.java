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
	public static final int SIZE_OF_DOUBLE = Double.SIZE / Byte.SIZE;
	
	/** identifier of host running the pipeline the stats belong to */
	@JsonProperty(value="hid", required=true)
	private String processingNodeId = null;
	/** identifier of pipeline which generated the stats */ 
	@JsonProperty(value="pid", required=true)
	private String pipelineId = null;
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
	private double avgDuration = 0.0;
	/** min. message size */
	@JsonProperty(value="minSize")
	private int minSize = Integer.MAX_VALUE;
	/** max. message size */
	@JsonProperty(value="maxSize")
	private int maxSize = Integer.MIN_VALUE;
	/** avg. message size */
	@JsonProperty(value="avgSize")
	private double avgSize = 0.0;
	/** error rate */
	@JsonProperty(value="err", required=true)
	private int errors = 0;
	/** size counter */
	@JsonProperty(value="scount", required = true)
	private int sizeCount = 0;
	/** duration counter */
	@JsonProperty(value="dcount", required=true)
	private int durationCount = 0;
	
	/**
	 * Default constructor
	 */
	public AggregatedComponentStatistics() {		
	}
	
	/**
	 * Initializes the component stats using the provided input 
	 * @param processingNodeId
	 * @param pipelineId
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
	public AggregatedComponentStatistics(final String processingNodeId, final String pipelineId, final String componentId, final int numOfMessages, final long startTime, final long endTime, 
			final int minDuration, final int maxDuration, final int avgDuration, final int minSize, final int maxSize, final int avgSize,
			final int errors) {
		this.processingNodeId = processingNodeId;
		this.pipelineId = pipelineId;
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
		stats.setAvgDuration(buf.getDouble());
		stats.setMinSize(buf.getInt());
		stats.setMaxSize(buf.getInt());
		stats.setAvgSize(buf.getDouble());
		stats.setErrors(buf.getInt());
		
		byte[] processingNodeId = new byte[buf.getInt()];
		buf.get(processingNodeId);
		
		byte[] pipelineId = new byte[buf.getInt()];
		buf.get(pipelineId);
		
		byte[] componentId = new byte[buf.getInt()];
		buf.get(componentId);
		
		stats.setProcessingNodeId(new String(processingNodeId));
		stats.setPipelineId(new String(pipelineId));
		stats.setComponentId(new String(componentId));
		
		return stats;		
	}
	
	/**
	 * Convert this {@link MicroPipelineStatistics} instance into its byte array representation 
	 * @return
	 */
	public byte[] toBytes() {
		
		byte[] hid = (this.processingNodeId != null ? this.processingNodeId.getBytes() : new byte[0]);
		byte[] pid = (this.pipelineId != null ? this.pipelineId.getBytes() : new byte[0]);
		byte[] cid = (this.componentId != null ? this.componentId.getBytes() : new byte[0]);

		// 9x SIZE_OF_INT: 8 attribute values, 1 size of component identifier representation
		// 2x SIZE_OF_LONG: start time and end time
		// 3x length of identifiers
		
		ByteBuffer buffer = ByteBuffer.allocate(2 * SIZE_OF_DOUBLE + 9 * SIZE_OF_INT + 2 * SIZE_OF_LONG  + hid.length + pid.length + cid.length);

		buffer.putInt(this.numOfMessages);
		buffer.putLong(this.startTime);
		buffer.putLong(this.endTime);
		buffer.putInt(this.minDuration);
		buffer.putInt(this.maxDuration);
		buffer.putDouble(this.avgDuration);
		buffer.putInt(this.minSize);
		buffer.putInt(this.maxSize);
		buffer.putDouble(this.avgSize);
		buffer.putInt(this.errors);
		
		buffer.putInt(hid.length);
		buffer.put(hid);
		
		buffer.putInt(pid.length);
		buffer.put(pid);
		
		buffer.putInt(cid.length);
		buffer.put(cid);
		
		return buffer.array();		
	}
	
	/**
	 * Adds a new event to the aggregated stats 
	 * @param duration
	 * @param size
	 * @param isError
	 */
	public void addEvent(final int duration, final int size, final boolean isError) {
		this.numOfMessages = this.numOfMessages + 1;
		if(isError)
			this.errors = this.errors + 1;

		// handle size information
		if(this.minSize > size)
			this.minSize = size;
		if(this.maxSize < size)
			this.maxSize = size;
		this.sizeCount = this.sizeCount + size;
		
		// handle duration information
		if(this.minDuration > duration)
			this.minDuration = duration;
		if(this.maxDuration < duration)
			this.maxDuration = duration;
		this.durationCount = this.durationCount + duration;
	}
	
	/**
	 * Resets all counters to provide a clean setup
	 */
	public void start() {
		this.minDuration = 0;
		this.maxDuration = 0;
		this.avgDuration = 0.0;
		this.durationCount = 0;

		this.minSize = 0;
		this.maxSize = 0;
		this.avgSize = 0.0;
		this.sizeCount = 0;

		this.numOfMessages = 0;
		this.errors = 0;

		this.startTime = System.currentTimeMillis();
		this.endTime = 0;		
	}
	
	/**
	 * Marks the end of the aggregation period and calculates average values
	 */
	public void finish() {
		this.endTime = System.currentTimeMillis();
		this.avgDuration = (double)this.durationCount / (double)numOfMessages;
		this.avgSize = (double)this.sizeCount / (double)numOfMessages;
	}

	public String getProcessingNodeId() {
		return processingNodeId;
	}

	public void setProcessingNodeId(String processingNodeId) {
		this.processingNodeId = processingNodeId;
	}

	public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
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

	public double getAvgDuration() {
		return avgDuration;
	}

	public void setAvgDuration(double avgDuration) {
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

	public double getAvgSize() {
		return avgSize;
	}

	public void setAvgSize(double avgSize) {
		this.avgSize = avgSize;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public int getSizeCount() {
		return sizeCount;
	}

	public void setSizeCount(int sizeCount) {
		this.sizeCount = sizeCount;
	}

	public int getDurationCount() {
		return durationCount;
	}

	public void setDurationCount(int durationCount) {
		this.durationCount = durationCount;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "AggregatedComponentStatistics [processingNodeId="
				+ processingNodeId + ", pipelineId=" + pipelineId
				+ ", componentId=" + componentId + ", numOfMessages="
				+ numOfMessages + ", startTime=" + startTime + ", endTime="
				+ endTime + ", minDuration=" + minDuration + ", maxDuration="
				+ maxDuration + ", avgDuration=" + avgDuration + ", minSize="
				+ minSize + ", maxSize=" + maxSize + ", avgSize=" + avgSize
				+ ", errors=" + errors + ", sizeCount=" + sizeCount
				+ ", durationCount=" + durationCount + "]";
	}
	
	
}
