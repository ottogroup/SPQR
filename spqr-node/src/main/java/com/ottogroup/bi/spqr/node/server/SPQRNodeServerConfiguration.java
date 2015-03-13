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
package com.ottogroup.bi.spqr.node.server;

import io.dropwizard.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration required for setting up {@link SPQRNodeServer}
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class SPQRNodeServerConfiguration extends Configuration {

	/** log4j */
	@JsonProperty(value="log4jConfiguration", required=true)
	private String log4jConfiguration = null;
	/** component repository folder */
	@JsonProperty(value="componentRepositoryFolder", required=true)
	private String componentRepositoryFolder = null;
	/** folder used for storing temporary queue data */
	@JsonProperty(value="temporaryQueueFolder", required=true)
	private String temporaryQueueFolder = null;
	/** number of threads assigned to internal executor service, default: 0 -- cached thread pool will be used */
	@JsonProperty(value="numOfThreads", required=true)
	private int numOfThreads = 0;
	
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
	public String getTemporaryQueueFolder() {
		return temporaryQueueFolder;
	}
	public void setTemporaryQueueFolder(String temporaryQueueFolder) {
		this.temporaryQueueFolder = temporaryQueueFolder;
	}
	public int getNumOfThreads() {
		return numOfThreads;
	}
	public void setNumOfThreads(int numOfThreads) {
		this.numOfThreads = numOfThreads;
	}
		
}
