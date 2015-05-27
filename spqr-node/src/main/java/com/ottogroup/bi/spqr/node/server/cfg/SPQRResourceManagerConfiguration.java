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
package com.ottogroup.bi.spqr.node.server.cfg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Provides all settings required for getting into contact with the remote resource manager
 * @author mnxfst
 * @since Apr 16, 2015
 */
@JsonRootName(value="resourceManagerConfiguration")
public class SPQRResourceManagerConfiguration implements Serializable {

	private static final long serialVersionUID = -3346086687554212055L;

	public enum ResourceManagerMode implements Serializable {
		LOCAL, REMOTE
	}

	/** resource manager to use: local (standalone mode) or remote (cluster mode) */
	@JsonProperty(value="mode", required=true)
	private ResourceManagerMode mode = ResourceManagerMode.LOCAL;
	/** protocol to use for remote communication, http or https */
	@JsonProperty(value="protocol", required=false)
	private String protocol = "http";
	/** host the resource manager lives on */
	@JsonProperty(value="host", required=false)
	private String host = null;
	/** port the resource manager listens to */
	@JsonProperty(value="port", required=false)
	private int port = 8080;
	
	public SPQRResourceManagerConfiguration() {		
	}

	public ResourceManagerMode getMode() {
		return mode;
	}

	public void setMode(ResourceManagerMode mode) {
		this.mode = mode;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
	
}
