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
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Represents the statistical information for a single message being processed by a
 * single component. 
 * @author mnxfst
 * @since May 12, 2015
 */
@JsonRootName(value="componentMessageStatisticEvent")
public class ComponentMessageProcessingEvent implements Serializable {

	private static final long serialVersionUID = -796247355297487130L;

	public static final int SIZE_OF_INT = Integer.SIZE / Byte.SIZE;
	public static final int SIZE_OF_LONG = Long.SIZE / Byte.SIZE;

	/** identifier of pipeline component which generated the stats */
	@JsonProperty(value="cid", required=true)
	private String componentId = null;
	/** error indicator */
	@JsonProperty(value="err", required=true)
	private boolean error = false;
	/** processing duration */
	@JsonProperty(value="dur", required=true)
	private int duration = 0;
	/** message size (bytes) */
	@JsonProperty(value="sze", required=true)
	private int size = 0;
	/** timestamp */
	@JsonProperty(value="time", required=true)
	private long timestamp = 0;
	
	/**
	 * Default constructor
	 */
	public ComponentMessageProcessingEvent() {		
	}
	
	/**
	 * Initializes the event using the provided input
	 * @param componentId
	 * @param error
	 * @param duration
	 * @param size		

	 * @param timestamp
	 */
	public ComponentMessageProcessingEvent(final String componentId, final boolean error, final int duration, final int size, final long timestamp) {
		this.componentId = componentId;
		this.error = error;
		this.duration = duration;
		this.size = size;
		this.timestamp = timestamp;
	}
	
	/**
	 * Converts the event into its byte array representation
	 * @return
	 */
	public byte[] toBytes() {		
		byte[] cid = (this.componentId != null ? this.componentId.getBytes() : new byte[0]);
		ByteBuffer buffer = ByteBuffer.allocate(cid.length + 1 + 3 * SIZE_OF_INT + SIZE_OF_LONG);

		buffer.put((byte)(error ? 1 : 0 ));
		buffer.putInt(this.duration);
		buffer.putInt(this.size);
		buffer.putLong(this.timestamp);
		buffer.putInt(cid.length);
		buffer.put(cid);

		return buffer.array();
	}
	
	/**
	 * Converts the byte array into a {@link ComponentMessageProcessingEvent} representation
	 * @param content
	 * @return
	 */
	public static ComponentMessageProcessingEvent fromBytes(final byte[] content) {

		ComponentMessageProcessingEvent event = new ComponentMessageProcessingEvent();
		if(content == null || content.length < 1)
			return event; 
		
		ByteBuffer buf = ByteBuffer.wrap(content);
		byte err = buf.get();
		event.setError((err == 1 ? true : false));
		event.setDuration(buf.getInt());
		event.setSize(buf.getInt());
		event.setTimestamp(buf.getLong());
		
		byte[] componentId = new byte[buf.getInt()];
		buf.get(componentId);		
		event.setComponentId(new String(componentId));

		return event;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	
	
}
