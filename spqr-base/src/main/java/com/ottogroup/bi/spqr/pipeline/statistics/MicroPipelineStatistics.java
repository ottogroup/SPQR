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
 * Holds statistical information about a single pipeline. Values tracked are:
 * <ul>
 *   <li>pipeline identifier</li>
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
 * @since Apr 14, 2015
 */
public class MicroPipelineStatistics implements Serializable {

	private static final long serialVersionUID = -2458412374912750561L;

	public static final int SIZE_OF_INT = Integer.SIZE / Byte.SIZE;
	public static final int SIZE_OF_LONG = Long.SIZE / Byte.SIZE;
	
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
	private int minDuration = 0;
	/** max. duration required for processing a single message */
	@JsonProperty(value="maxDur")
	private int maxDuration = 0;
	/** avg. duration required for processing a single message */
	@JsonProperty(value="avgDur")
	private int avgDuration = 0;
	/** min. message size */
	@JsonProperty(value="minSize")
	private int minSize = 0;
	/** max. message size */
	@JsonProperty(value="maxSize")
	private int maxSize = 0;
	/** avg. message size */
	@JsonProperty(value="avgSize")
	private int avgSize = 0;
	/** error rate */
	@JsonProperty(value="err", required=true)
	private int errors = 0;
		
	public MicroPipelineStatistics() {		
	}
	
	public MicroPipelineStatistics(final String processingNodeId, final String pipelineId, final String componentId, final long startTime, final int numOfMessages,
			final int minDuration, final int maxDuration, final int avgDuration,
			final int minSize, final int maxSize, final int avgSize) {
		this.startTime = startTime;
		this.componentId = componentId;
		this.processingNodeId = processingNodeId;
		this.pipelineId = pipelineId;
		this.numOfMessages = numOfMessages;
		this.minDuration = minDuration;
		this.maxDuration = maxDuration;
		this.avgDuration = avgDuration;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.avgSize = avgSize;
	}
	
	/**
	 * Converts the provided byte array into a {@link MicroPipelineStatistics} representation
	 * @param statsContent
	 * @return
	 */
	public static MicroPipelineStatistics fromByteArray(final byte[] statsContent) {
		
		MicroPipelineStatistics stats = new MicroPipelineStatistics();
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
		
		byte[] procNodeId = new byte[buf.getInt()];
		buf.get(procNodeId);
		
		byte[] pipelineId = new byte[buf.getInt()];
		buf.get(pipelineId);
		
		byte[] componentId = new byte[buf.getInt()];
		buf.get(componentId);
		
		stats.setProcessingNodeId(new String(procNodeId));
		stats.setPipelineId(new String(pipelineId));
		stats.setComponentId(new String(componentId));
		
		return stats;		
	}
	
	/**
	 * Convert this {@link MicroPipelineStatistics} instance into its byte array representation 
	 * @return
	 */
	public byte[] toByteArray() {

		/////////////////////////////////////////////////////////
		// describes how the size of the result array is computed
		//		SIZE_OF_INT + 
		//		SIZE_OF_LONG + 
		//		SIZE_OF_LONG + 
		//		SIZE_OF_INT + 
		//		SIZE_OF_INT + 
		//		SIZE_OF_INT + 
		//		SIZE_OF_INT + 
		//		SIZE_OF_INT +
		//		SIZE_OF_INT +
		//		SIZE_OF_INT +
		//		procNodeId.length +
		//		pid.length +
		//      cid.length +
		//		(SIZE_OF_INT * 3)); <-- add extra int's 
		//           for storing the field sizes of processingNodeId, pipelineId and componentId
		//           which are required when extracting content from byte array		
		// >> 11x SIZE_OF_INT 
		// >>  3x SIZE_OF_LONG
		//
		// ByteBuffer buffer = ByteBuffer.allocate(11 * SIZE_OF_INT + 3 * SIZE_OF_LONG + procNodeId.length + pid.length + cid.length);
		
		// allocated buffer
		
		byte[] procNodeId = (this.processingNodeId != null ? this.processingNodeId.getBytes() : new byte[0]);
		byte[] pid = (this.pipelineId != null ? this.pipelineId.getBytes() : new byte[0]);
		byte[] cid = (this.componentId != null ? this.componentId.getBytes() : new byte[0]);

		ByteBuffer buffer = ByteBuffer.allocate(11 * SIZE_OF_INT + 3 * SIZE_OF_LONG + procNodeId.length + pid.length + cid.length);

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
		buffer.putInt(procNodeId.length);
		buffer.put(procNodeId);
		buffer.putInt(pid.length);
		buffer.put(pid);
		buffer.putInt(cid.length);
		buffer.put(cid);
		
		return buffer.array();		
	}
	
	
	
	public static void main(String[] args) {
		MicroPipelineStatistics stats = new MicroPipelineStatistics("procNodeId-1", "--", "component-123", System.currentTimeMillis(), 1234, 2, 432, 56, 67890, 98765, 45678);
		stats.setErrors(9383);
		stats.setEndTime(System.currentTimeMillis()+ 1000);
		
		long n1 = System.nanoTime();
		byte[] converted = stats.toByteArray();
		long n2 = System.nanoTime();
		
		System.out.println("length: " + converted.length + ", conversion: " +(n2-n1)+"ns");
		
		long s1 = System.nanoTime();
		MicroPipelineStatistics reStats = MicroPipelineStatistics.fromByteArray(converted);
		long s2 = System.nanoTime();
		System.out.println("length: " + converted.length + ", conversion: " +(s2-s1)+"ns");
		System.out.println(stats);
		System.out.println(reStats);
		
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
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

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public void incNumOfMessages() {
		this.numOfMessages++;
	}

	public void incNumOfMessages(int v) {
		this.numOfMessages = this.numOfMessages + v;
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

	public int getNumOfMessages() {
		return numOfMessages;
	}

	public void setNumOfMessages(int numOfMessages) {
		this.numOfMessages = numOfMessages;
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "MicroPipelineStatistics [processingNodeId=" + processingNodeId
				+ ", pipelineId=" + pipelineId + ", componentId=" + componentId
				+ ", numOfMessages=" + numOfMessages + ", startTime="
				+ startTime + ", endTime=" + endTime + ", minDuration="
				+ minDuration + ", maxDuration=" + maxDuration
				+ ", avgDuration=" + avgDuration + ", minSize=" + minSize
				+ ", maxSize=" + maxSize + ", avgSize=" + avgSize + ", errors="
				+ errors + "]";
	}
	
	
	
}
