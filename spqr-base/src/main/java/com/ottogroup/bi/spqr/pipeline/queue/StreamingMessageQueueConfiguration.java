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
package com.ottogroup.bi.spqr.pipeline.queue;

import java.io.Serializable;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Configuration required for setting up a {@link StreamingMessageQueue}
 * @author mnxfst
 * @since Mar 6, 2015
 */
@JsonRootName(value="streamingMessageQueueConfiguration")
public class StreamingMessageQueueConfiguration implements Serializable {

	private static final long serialVersionUID = -8500785433030074837L;

	/** component identifier */
	@JsonProperty(value="id", required=true)
	private String id = null;
	/** queue settings */
	@JsonProperty(value="queueSettings", required=true)
	private Properties properties = null;
	
	public StreamingMessageQueueConfiguration() {		
	}
	
	public StreamingMessageQueueConfiguration(final String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	
	
}
