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
package com.ottogroup.bi.spqr.websocket.server.cfg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.ottogroup.bi.spqr.websocket.server.SPQRWebSocketServer;

/**
 * Provides all configuration options required for setting up the {@link SPQRWebSocketServer}
 * @author mnxfst
 * @since Apr 17, 2015
 */
@JsonRootName("spqrWebSocketServerConfiguration")
public class SPQRWebSocketServerConfiguration implements Serializable {
	
	private static final long serialVersionUID = 4463978663305593008L;

	/** log4j configuration file */
	@JsonProperty(value="log4jConfigurationFile", required=true)
	private String log4jConfigurationFile = null;
	/** port the server listens to for incoming requests */
	@JsonProperty(value="port", required=true)
	private int port = 8080;
	/** threads used by boss event loop group */
	@JsonProperty(value="bossEventGroupThreads", required=true)
	private int bossEventGroupThreads = 1;
	/** threads used by worker event loop group */
	@JsonProperty(value="workerEventGroupThreads", required=true)
	private int workerEventGroupThreads = 1;
	/** kafka consumer configuration */
	@JsonProperty(value="kafkaConsumer", required=true)
	private SPQRKafkaConsumerConfiguration kafkaConsumer = null;
	
	public String getLog4jConfigurationFile() {
		return log4jConfigurationFile;
	}
	public void setLog4jConfigurationFile(String log4jConfigurationFile) {
		this.log4jConfigurationFile = log4jConfigurationFile;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public SPQRKafkaConsumerConfiguration getKafkaConsumer() {
		return kafkaConsumer;
	}
	public void setKafkaConsumer(SPQRKafkaConsumerConfiguration kafkaConsumer) {
		this.kafkaConsumer = kafkaConsumer;
	}
	public int getBossEventGroupThreads() {
		return bossEventGroupThreads;
	}
	public void setBossEventGroupThreads(int bossEventGroupThreads) {
		this.bossEventGroupThreads = bossEventGroupThreads;
	}
	public int getWorkerEventGroupThreads() {
		return workerEventGroupThreads;
	}
	public void setWorkerEventGroupThreads(int workerEventGroupThreads) {
		this.workerEventGroupThreads = workerEventGroupThreads;
	}
	
	
}
