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
package com.ottogroup.bi.spqr.metrics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

/**
 * Attached to a {@link MicroPipeline} the handler manages the {@link Metric} collection and
 * their export
 * @author mnxfst
 * @since May 6, 2015
 */
public class ComponentMetricsHandler {

	/** our faithful logging facility ... ;-) */
	private final static Logger logger = Logger.getLogger(ComponentMetricsHandler.class);
	
	/** identifier of processing node this collector lives on */
	private final String processingNodeId;
	/** identifier of pipeline the collector is attached to */
	private final String pipelineId;
	/** metric registry */
	private final MetricRegistry metricRegistry;
	/** metrics reporter */
	private final SPQRGraphiteReporter metricReporter;

	/**
	 * Initializes the handler using the provided input
	 * @param processingNodeId
	 * @param pipelineId
	 * @param metricRegistry
	 */
	public ComponentMetricsHandler(final String processingNodeId, final String pipelineId,  
			final MetricRegistry metricRegistry) {
		this.processingNodeId = processingNodeId;
		this.pipelineId = pipelineId;
		this.metricRegistry = metricRegistry;
//		final Graphite graphite = new Graphite(new InetSocketAddress("localhost", 2003));
		try {
			graphite.connect();
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.metricReporter = SPQRGraphiteReporter.forRegistry(metricRegistry)
				.prefixedWith("spqr.pipelines")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);				
		
//
//		this.metricReporter = CsvReporter.forRegistry(metricRegistry)
//				.formatFor(Locale.US)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build(new File("/tmp/data"));
		metricReporter.start(1, TimeUnit.SECONDS);
		
		logger.info("component metrics handler init [node="+this.processingNodeId+", pipeline="+this.pipelineId+", metricReporter="+metricReporter+"]");
		if(logger.isDebugEnabled())
			logger.debug("component metrics handler init [node="+this.processingNodeId+", pipeline="+this.pipelineId+"]");
	}

	public MetricRegistry getMetricRegistry() {
		return this.metricRegistry;
	}

	public void shutdown() {
//		if(this.metricReporter instanceof JmxReporter) {
//			((JmxReporter)(this.metricReporter)).stop();
//			logger.info("JMX metrics reporter successfully halted");
//		} else if(this.metricReporter instanceof ScheduledReporter) {
//			((ScheduledReporter)(this.metricReporter)).stop();
//			logger.info("Scheduled metrics reporter successfully halted");
//		}
		this.metricReporter.stop();
	}
	
}
