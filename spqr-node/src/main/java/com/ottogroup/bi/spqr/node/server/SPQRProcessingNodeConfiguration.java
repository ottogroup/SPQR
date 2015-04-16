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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Groups all settings required for pure service configuration (no port or log settings)
 * @author mnxfst
 * @since Apr 16, 2015
 */
@JsonRootName(value="processingNodeConfiguration")
public class SPQRProcessingNodeConfiguration implements Serializable {

	private static final long serialVersionUID = -469477242548214229L;

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
	/** host - forwarded to resource manager during startup */
	@JsonProperty(value="host", required=true)
	private String host = null;
	/** protocol to be used for communication with this node */
	@JsonProperty(value="protocol", required=true)
	private String protocol = "http";
	/** service port */
	@JsonProperty(value="servicePort", required=true)
	private int servicePort = 8080;
	/** admin port */
	@JsonProperty(value="adminPort", required=true)
	private int adminPort = 8081;
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
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getServicePort() {
		return servicePort;
	}
	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}
	public int getAdminPort() {
		return adminPort;
	}
	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
	}
	
	
}
