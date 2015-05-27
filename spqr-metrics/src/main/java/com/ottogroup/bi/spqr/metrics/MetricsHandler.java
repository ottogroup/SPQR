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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;

/**
 * Provides access to a {@link MetricRegistry} and manages associated {@link Reporter} instances.
 * It supports to activate, deactive and remove {@link Metric}. 
 * @author mnxfst
 * @since May 21, 2015
 */
public class MetricsHandler {

	/** manages all registered {@link Metric} metrics */
	private final MetricRegistry metricRegistry = new MetricRegistry();
	/** instances (referenced by their given name) reporting metrics to JMX server */ 
	private Map<String, JmxReporter> jmxReporters = new HashMap<>();
	/** scheduled metrics reporter instances (referenced by their given name) */
	private Map<String, ScheduledReporter> scheduledReporters = new HashMap<>();

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// handler lifecycle
	
	/**
	 * Shuts down the handler instance 
	 */
	public void shutdown() {
		
		for(final JmxReporter jmxReporter : jmxReporters.values()) {
			jmxReporter.stop();
		}
		jmxReporters.clear();
		
		for(final ScheduledReporter scheduledReporter : scheduledReporters.values()) {
			scheduledReporter.stop();
		}
		scheduledReporters.clear();		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// metrics handling

	/**
	 * Returns the {@link MetricRegistry} responsible for collecting {@link Metric metrics}
	 * @return
	 */
	public MetricRegistry getRegistry() {
		return this.metricRegistry;
	}

	/**
	 * Registers a single {@link Metric} with the {@link MetricRegistry}
	 * @param name
	 * @param metric
	 * @return
	 */
	public Metric register(final String name, Metric metric) {
		return this.metricRegistry.register(name, metric);
	}
	
	/**
	 * Registers all contained {@link Metric} instances with the {@link MetricRegistry}
	 * @param metrics
	 */
	public void register(final MetricSet metrics) {
		this.metricRegistry.registerAll(metrics);
	}
	
	/**
	 * Creates a new {@link Counter} or returns an existing one for the given name
	 * @param name
	 * @return
	 */
	public Counter counter(final String name) {
		return this.metricRegistry.counter(name);
	}
	
	/**
	 * Creates either a {@link ResetOnReadCounter} or a {@link Counter} instance
	 * depending on the <i>resetOnRead</i> parameter
	 * @param name
	 * @param resetOnRead
	 * @return
	 */
	public Counter counter(final String name, boolean resetOnRead) {
		if(resetOnRead)
			return this.metricRegistry.register(name, new ResetOnReadCounter());
		return this.metricRegistry.counter(name);
	}
	
	/**
	 * Creates a new {@link Meter} or returns an existing one for the given name
	 * @param name
	 * @return
	 */
	public Meter meter(final String name) {
		return this.metricRegistry.meter(name);
	}
	
	/**
	 * Create a new {@link Timer} or returns an existing one for the given name
	 * @param name
	 * @return
	 */
	public Timer timer(final String name) {
		return this.metricRegistry.timer(name);
	}
	
	/**
	 * Create a new {@link Histogram} or returns an existing one for the given name
	 * @return
	 */
	public Histogram histogram(final String name) {
		return this.metricRegistry.histogram(name);
	}
	
	/**
	 * Removes the referenced metric
	 * @param name
	 */
	public boolean removeMetric(final String name) {
		return this.metricRegistry.remove(name);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// reporter handling
	
	/**
	 * Registers a new {@link JmxReporter}. 
	 * @param id
	 * @param reporterInstance reporter instance (must have been started)
	 * @throws RequiredInputMissingException
	 */
	public void addJmxReporter(final String id, final JmxReporter reporterInstance) throws RequiredInputMissingException {
		
		////////////////////////////////////////////////////////////////
		// input validation
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required reporter identifier");
		if(reporterInstance == null)
			throw new RequiredInputMissingException("Missing required reporter instance");
		////////////////////////////////////////////////////////////////
		
		this.jmxReporters.put(StringUtils.lowerCase(StringUtils.trim(id)), reporterInstance);
	}
	
	/**
	 * {@link JmxReporter#stop() Stops} the referenced {@link JmxReporter} and removes it from map
	 * of managed jmx reporters
	 * @param id
	 */
	public void removeJmxReporter(final String id) {		
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		JmxReporter jmxReporter = this.jmxReporters.get(key);
		if(jmxReporter != null) {
			jmxReporter.stop();
			this.jmxReporters.remove(key);
		}		
	}
	
	/**
	 * {@link JmxReporter#stop() Stops} the referenced {@link JmxReporter} but keeps it referenced
	 * @param id
	 */
	public void stopJmxReporter(final String id) {
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		JmxReporter jmxReporter = this.jmxReporters.get(key);
		if(jmxReporter != null) {
			jmxReporter.stop();
		}		
	}
	
	/**
	 * {@link JmxReporter#start() Starts} the referenced {@link JmxReporter}
	 * @param id
	 */
	public void startJmxReporter(final String id) {
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		JmxReporter jmxReporter = this.jmxReporters.get(key);
		if(jmxReporter != null) {
			jmxReporter.start();
		}		
	}
	
	/**
	 * Registers a new {@link ScheduledReporter}
	 * @param id
	 * @param reporterInstance reporter instance (must have been started)
	 * @throws RequiredInputMissingException
	 */
	public void addScheduledReporter(final String id, final ScheduledReporter reporterInstance) throws RequiredInputMissingException {
		
		////////////////////////////////////////////////////////////////
		// input validation
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required reporter identifier");
		if(reporterInstance == null)
			throw new RequiredInputMissingException("Missing required reporter instance");
		////////////////////////////////////////////////////////////////

		this.scheduledReporters.put(StringUtils.lowerCase(StringUtils.trim(id)), reporterInstance);
	}
	
	/**
	 * {@link ScheduledReporter#stop() Stops} the referenced {@link ScheduledReporter} and removes it from
	 * map of managed scheduled reporters
	 * @param id
	 */
	public void removeScheduledReporter(final String id) {
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		ScheduledReporter scheduledReporter = this.scheduledReporters.get(key);
		if(scheduledReporter != null) {
			scheduledReporter.stop();
			this.scheduledReporters.remove(key);
		}		
	}
	
	/**
	 * {@link ScheduledReporter#stop() Stops} the referenced {@link ScheduledReporter} but keeps it referenced
	 * @param id
	 */
	public void stopScheduledReporter(final String id) {
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		ScheduledReporter scheduledReporter = this.scheduledReporters.get(key);
		if(scheduledReporter != null) {
			scheduledReporter.stop();
		}		
	}
	
	/**
	 * {@link ScheduledReporter#start(long, java.util.concurrent.TimeUnit) Starts} the referenced {@link ScheduledReporter}
	 * @param id
	 * @param period
	 * @param unit
	 */
	public void startScheduledReporter(final String id, final int period, final TimeUnit unit) {
		String key = StringUtils.lowerCase(StringUtils.trim(id));
		ScheduledReporter scheduledReporter = this.scheduledReporters.get(key);
		if(scheduledReporter != null) {
			scheduledReporter.start(period, unit);
		}		
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
