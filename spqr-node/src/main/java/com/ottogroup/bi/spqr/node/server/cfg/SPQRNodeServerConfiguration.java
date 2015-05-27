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

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ottogroup.bi.spqr.node.server.SPQRNodeServer;

/**
 * Configuration required for setting up {@link SPQRNodeServer}
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class SPQRNodeServerConfiguration extends Configuration {

	/** processing node specific settings */
	@JsonProperty(value="spqrNode", required=true)
	private SPQRProcessingNodeConfiguration spqrNode = null;
	/** resource manager configuration: local or remote */
	@JsonProperty(value="resourceManager", required=true)
	private SPQRResourceManagerConfiguration resourceManagerConfiguration = null;	
	/** http client configuration */
	@JsonProperty(value="httpClient", required=true)
    private JerseyClientConfiguration httpClient;
			
	
	public SPQRProcessingNodeConfiguration getSpqrNode() {
		return spqrNode;
	}
	public void setSpqrNode(SPQRProcessingNodeConfiguration spqrNode) {
		this.spqrNode = spqrNode;
	}
	public SPQRResourceManagerConfiguration getResourceManagerConfiguration() {
		return resourceManagerConfiguration;
	}
	public void setResourceManagerConfiguration(
			SPQRResourceManagerConfiguration resourceManagerConfiguration) {
		this.resourceManagerConfiguration = resourceManagerConfiguration;
	}
	public JerseyClientConfiguration getHttpClient() {
		return httpClient;
	}
	public void setHttpClient(JerseyClientConfiguration httpClient) {
		this.httpClient = httpClient;
	}
		
}
