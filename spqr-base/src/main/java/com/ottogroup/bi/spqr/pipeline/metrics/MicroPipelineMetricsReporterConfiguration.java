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
package com.ottogroup.bi.spqr.pipeline.metrics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Provides settings for activating metrics collection within micro pipelines
 * @author mnxfst
 * @since May 27, 2015
 */
@JsonRootName(value="microPipelineMetricsConfiguration")
public class MicroPipelineMetricsReporterConfiguration implements Serializable {
	
	private static final long serialVersionUID = -3613911950147597674L;
	
	public static final String SETTING_CSV_OUTPUT_FILE = "outputFile";
	
	public static final String SETTING_GRAPHITE_HOST = "host";
	public static final String SETTING_GRAPHITE_PORT = "port";
	
	public static final String SETTING_KAFKA_BROKER_LIST = "brokerList";
	public static final String SETTING_KAFKA_ZOOKEEPER_CONNECT = "zookeeperConnect";
	public static final String SETTING_KAFKA_CLIENT_ID = "clientId";
	public static final String SETTING_KAFKA_TOPIC_ID = "topicId";

	/** metrics reporter identifier, eg. kafkaReporter */
	@JsonProperty(value="id", required=true)
	private String id = null;
	/** type, eg. CONSOLE, KAFKA, CSV, GRAPHITE */
	@JsonProperty(value="type", required=true)
	private MetricReporterType type = MetricReporterType.CONSOLE;
	/** reporting period given in seconds */
	@JsonProperty(value="period", required=true)
	private int period = 1;
	/** settings */
	@JsonProperty(value="settings", required=true)
	private Map<String, String> settings = new HashMap<String, String>();

	public void addSetting(final String key, final String value) {
		this.settings.put(key, value);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MetricReporterType getType() {
		return type;
	}

	public void setType(MetricReporterType type) {
		this.type = type;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
	
}
