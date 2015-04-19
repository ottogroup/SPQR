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
import com.ottogroup.bi.spqr.websocket.kafka.KafkaConsumer;

/**
 * All required configuration options for setting up the {@link KafkaConsumer} 
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRKafkaConsumerConfiguration implements Serializable {

	private static final long serialVersionUID = 5190373154294783314L;

	/** how to handle offset determination when no inital offset is available from zookeeper: "smallest", "largest" */
	@JsonProperty(value="kafkaAutoOffsetReset", required=true)
	private String autoOffsetReset = "largest";
	/** zookeeper connect string, eg: localhost:2181 */
	@JsonProperty(value="zookeeperConnect", required=true)
	private String zookeeperConnect = null;
	/** zookeeper timeout given in milliseconds */
	@JsonProperty(value="zookeeperSessionTimeout", required=true)
	private int zookeeperSessionTimeout = 0;
	/** zookeeper sync'ing interval given in milliseconds */
	@JsonProperty(value="zookeeperSyncInterval", required=true)
	private int zookeeperSyncInterval = 0;
	/** enables auto committing offsets to zookeeper, values: "true", "false" */
	@JsonProperty(value="zookeeperAutoCommitEnabled", required=true)
	private boolean zookeeperAutoCommitEnabled = false;
	/** interval given in milliseconds to execute offset auto commits to zookeeper */
	@JsonProperty(value="zookeeperAutoCommitInterval", required=true)
	private int zookeeperAutoCommitInterval = 0;

	public String getAutoOffsetReset() {
		return autoOffsetReset;
	}
	public void setAutoOffsetReset(String autoOffsetReset) {
		this.autoOffsetReset = autoOffsetReset;
	}
	public String getZookeeperConnect() {
		return zookeeperConnect;
	}
	public void setZookeeperConnect(String zookeeperConnect) {
		this.zookeeperConnect = zookeeperConnect;
	}
	public int getZookeeperSessionTimeout() {
		return zookeeperSessionTimeout;
	}
	public void setZookeeperSessionTimeout(int zookeeperSessionTimeout) {
		this.zookeeperSessionTimeout = zookeeperSessionTimeout;
	}
	public int getZookeeperSyncInterval() {
		return zookeeperSyncInterval;
	}
	public void setZookeeperSyncInterval(int zookeeperSyncInterval) {
		this.zookeeperSyncInterval = zookeeperSyncInterval;
	}
	public boolean isZookeeperAutoCommitEnabled() {
		return zookeeperAutoCommitEnabled;
	}
	public void setZookeeperAutoCommitEnabled(boolean zookeeperAutoCommitEnabled) {
		this.zookeeperAutoCommitEnabled = zookeeperAutoCommitEnabled;
	}
	public int getZookeeperAutoCommitInterval() {
		return zookeeperAutoCommitInterval;
	}
	public void setZookeeperAutoCommitInterval(int zookeeperAutoCommitInterval) {
		this.zookeeperAutoCommitInterval = zookeeperAutoCommitInterval;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
