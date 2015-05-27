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

import com.codahale.metrics.Metric;
import com.codahale.metrics.graphite.Graphite;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Settings for exporting {@link Metric metrics} to {@link Graphite} 
 * @author mnxfst
 * @since May 21, 2015
 */
@JsonRootName("spqrNodeMetricsGraphiteSettings")
public class MetricsGraphiteReporterConfiguration implements Serializable {
	
	private static final long serialVersionUID = -8220196756657126782L;

	@JsonProperty(value="host", required=true)
	private String host = null;
	
	@JsonProperty(value="port", required=true)
	private String port = null;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	

}
