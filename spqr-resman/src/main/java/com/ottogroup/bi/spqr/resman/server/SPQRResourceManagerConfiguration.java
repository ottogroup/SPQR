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
package com.ottogroup.bi.spqr.resman.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

/**
 * Provides a mapping for all relevant configuration options required on resource
 * manager initialization
 * @author mnxfst
 * @since Apr 10, 2015
 */
public class SPQRResourceManagerConfiguration extends Configuration {

	/** log4j */
	@JsonProperty(value="log4jConfiguration", required=true)
	private String log4jConfiguration = null;
	/** component repository folder */
	@JsonProperty(value="componentRepositoryFolder", required=true)
	private String componentRepositoryFolder = null;
	/** http client configuration */
	@JsonProperty(value="httpClient", required=true)
    private JerseyClientConfiguration httpClient;

	public String getLog4jConfiguration() {
		return log4jConfiguration;
	}
	public void setLog4jConfiguration(String log4jConfiguration) {
		this.log4jConfiguration = log4jConfiguration;
	}
	public String getComponentRepositoryFolder() {
		return componentRepositoryFolder;
	}
	public void setComponentRepositoryFolder(String componentRepositoryFolder) {
		this.componentRepositoryFolder = componentRepositoryFolder;
	}
	public JerseyClientConfiguration getHttpClient() {
		return httpClient;
	}
	public void setHttpClient(JerseyClientConfiguration httpClient) {
		this.httpClient = httpClient;
	}

	
	
}
