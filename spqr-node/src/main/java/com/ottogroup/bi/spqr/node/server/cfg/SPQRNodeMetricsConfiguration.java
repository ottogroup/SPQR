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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.ottogroup.bi.spqr.metrics.MetricsHandler;
import com.ottogroup.bi.spqr.pipeline.metrics.MicroPipelineMetricsReporterConfiguration;

/**
 * Settings required for configuring an optional {@link MetricsHandler}
 * @author mnxfst
 * @since May 21, 2015
 */
@JsonRootName(value="spqrNodeMetricsConfiguration")
public class SPQRNodeMetricsConfiguration implements Serializable {

	private static final long serialVersionUID = 3555151965536734286L;

	/** attaches a memory usage metric collector */
	@JsonProperty(value="attachMemoryUsageMetricCollector", required=false)
	private boolean attachMemoryUsageMetricCollector = false;
	/** attaches a file descriptor metric collector */
	@JsonProperty(value="attachFileDescriptorMetricCollector", required=false)
	private boolean attachFileDescriptorMetricCollector = false;
	/** attaches a class loading metric collector */
	@JsonProperty(value="attachClassLoadingMetricCollector", required=false)
	private boolean attachClassLoadingMetricCollector = false;
	/** attaches a garbage collector metric collector */
	@JsonProperty(value="attachGCMetricCollector", required=false)
	private boolean attachGCMetricCollector = false;
	/** attaches a thread state metric collector */
	@JsonProperty(value="attachThreadStateMetricCollector", required=false)
	private boolean attachThreadStateMetricCollector = false;
	/** metrics reporter */
	@JsonProperty(value="metricsReporter", required=false)
	private List<MicroPipelineMetricsReporterConfiguration> metricsReporter = new ArrayList<MicroPipelineMetricsReporterConfiguration>();
	
	public boolean isAttachMemoryUsageMetricCollector() {
		return attachMemoryUsageMetricCollector;
	}
	public void setAttachMemoryUsageMetricCollector(
			boolean attachMemoryUsageMetricCollector) {
		this.attachMemoryUsageMetricCollector = attachMemoryUsageMetricCollector;
	}
	public boolean isAttachFileDescriptorMetricCollector() {
		return attachFileDescriptorMetricCollector;
	}
	public void setAttachFileDescriptorMetricCollector(
			boolean attachFileDescriptorMetricCollector) {
		this.attachFileDescriptorMetricCollector = attachFileDescriptorMetricCollector;
	}
	public boolean isAttachClassLoadingMetricCollector() {
		return attachClassLoadingMetricCollector;
	}
	public void setAttachClassLoadingMetricCollector(
			boolean attachClassLoadingMetricCollector) {
		this.attachClassLoadingMetricCollector = attachClassLoadingMetricCollector;
	}
	public boolean isAttachGCMetricCollector() {
		return attachGCMetricCollector;
	}
	public void setAttachGCMetricCollector(boolean attachGCMetricCollector) {
		this.attachGCMetricCollector = attachGCMetricCollector;
	}
	public boolean isAttachThreadStateMetricCollector() {
		return attachThreadStateMetricCollector;
	}
	public void setAttachThreadStateMetricCollector(
			boolean attachThreadStateMetricCollector) {
		this.attachThreadStateMetricCollector = attachThreadStateMetricCollector;
	}
	public List<MicroPipelineMetricsReporterConfiguration> getMetricsReporter() {
		return metricsReporter;
	}
	public void setMetricsReporter(
			List<MicroPipelineMetricsReporterConfiguration> metricsReporter) {
		this.metricsReporter = metricsReporter;
	}
	
}
