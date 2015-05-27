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

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.metrics.kafka.KafkaReporter;
import com.ottogroup.bi.spqr.pipeline.metrics.MetricReporterType;
import com.ottogroup.bi.spqr.pipeline.metrics.MicroPipelineMetricsReporterConfiguration;

/**
 * Generates {@link Reporter} instances from provided settings and attaches them to a {@link MetricsHandler}
 * @author mnxfst
 * @since May 27, 2015
 */
public class MetricsReporterFactory {

	/**
	 * Attaches a {@link GraphiteReporter} to provided {@link MetricsHandler}
	 * @param metricsHandler
	 * @param id
	 * @param period
	 * @param host
	 * @param port
	 * @throws RequiredInputMissingException
	 */
	private static void attachGraphiteReporter(final MetricsHandler metricsHandler, final String id, final int period, final String host, final int port) throws RequiredInputMissingException {
		
		//////////////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required metric reporter id");
		if(metricsHandler == null)
			throw new RequiredInputMissingException("Missing required metrics handler");
		if(StringUtils.isBlank(host))
			throw new RequiredInputMissingException("Missing required graphite host");
		if(port < 0)
			throw new RequiredInputMissingException("Missing required graphite port");
		//////////////////////////////////////////////////////////////////////////
		
		final Graphite graphite = new Graphite(new InetSocketAddress(host, port));
		final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricsHandler.getRegistry())
				.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
		        .build(graphite);
		reporter.start((period > 0 ? period : 1), TimeUnit.SECONDS);
		metricsHandler.addScheduledReporter(id, reporter);
	}

	/**
	 * Attaches a {@link CsvReporter} to provided {@link MetricsHandler}
	 * @param metricsHandler
	 * @param id
	 * @param period
	 * @param outputFile
	 * @throws RequiredInputMissingException
	 */
	private static void attachCSVReporter(final MetricsHandler metricsHandler, final String id, final int period, final String outputFile) throws RequiredInputMissingException {

		//////////////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required metric reporter id");
		if(metricsHandler == null)
			throw new RequiredInputMissingException("Missing required metrics handler");
		if(StringUtils.isBlank(outputFile))
			throw new RequiredInputMissingException("Missing required output file for CSV metrics writer");
		File oFile = new File(outputFile);
		if(oFile.isDirectory())
			throw new RequiredInputMissingException("Output file points to directory");
		//////////////////////////////////////////////////////////////////////////

		final CsvReporter reporter = CsvReporter.forRegistry(metricsHandler.getRegistry())
				.formatFor(Locale.US).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(oFile);
		reporter.start((period > 0 ? period : 1), TimeUnit.SECONDS);
		metricsHandler.addScheduledReporter(id, reporter);
	}

	/**
	 * Attaches a {@link ConsoleReporter} to provided {@link MetricsHandler}
	 * @param metricsHandler
	 * @param id
	 * @param period
	 * @throws RequiredInputMissingException
	 */
	private static void attachConsoleReporter(final MetricsHandler metricsHandler, final String id, final int period) throws RequiredInputMissingException {
		//////////////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required metric reporter id");
		if(metricsHandler == null)
			throw new RequiredInputMissingException("Missing required metrics handler");
		//////////////////////////////////////////////////////////////////////////

		final ConsoleReporter reporter = ConsoleReporter.forRegistry(metricsHandler.getRegistry()).
				convertDurationsTo(TimeUnit.SECONDS).convertRatesTo(TimeUnit.MILLISECONDS).formattedFor(Locale.US).build();
		reporter.start((period > 0 ? period : 1), TimeUnit.SECONDS);
		metricsHandler.addScheduledReporter(id, reporter);
	}
	
	/**
	 * Attaches a {@link KafkaReporter} to provided {@link MetricsHandler}
	 * @param metricsHandler
	 * @param id
	 * @param period
	 * @param zookeeperConnect
	 * @param brokerList
	 * @param clientId
	 * @param topicId
	 * @throws RequiredInputMissingException
	 */
	private static void attachKafkaReporter(final MetricsHandler metricsHandler, final String id, final int period, 
			final String zookeeperConnect, final String brokerList, final String clientId, final String topicId) throws RequiredInputMissingException {

		//////////////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required metric reporter id");
		if(metricsHandler == null)
			throw new RequiredInputMissingException("Missing required metrics handler");
		if(StringUtils.isBlank(zookeeperConnect))
			throw new RequiredInputMissingException("Missing required zookeeper connect");
		if(StringUtils.isBlank(brokerList))
			throw new RequiredInputMissingException("Missing required broker list");
		if(StringUtils.isBlank(clientId))
			throw new RequiredInputMissingException("Missing required client identifier");
		if(StringUtils.isBlank(topicId))
			throw new RequiredInputMissingException("Missing required topic identifier");
		//////////////////////////////////////////////////////////////////////////
		
		final KafkaReporter reporter = KafkaReporter.forRegistry(metricsHandler.getRegistry())
				.brokerList("localhost:9092").clientId(clientId)
				.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MICROSECONDS)
				.topic(topicId).zookeeperConnect(zookeeperConnect).build();
		reporter.start((period > 0 ? period : 1), TimeUnit.SECONDS);
		metricsHandler.addScheduledReporter(id, reporter);
	}
	
	/**
	 * Attaches a new reporter of specified {@link MetricReporterType type} to contained {@link MetricsHandler#getRegistry() registry}
	 * @param metricsHandler
	 * @param reporterConfig
	 * @throws RequiredInputMissingException
	 */
	public static void attachReporters(final MetricsHandler metricsHandler, final List<MicroPipelineMetricsReporterConfiguration> reporterConfig) throws RequiredInputMissingException {
		
		if(reporterConfig == null || reporterConfig.isEmpty())
			return;
		
		///////////////////////////////////////////////////////////////////
		// validate input
		if(metricsHandler == null)
			throw new RequiredInputMissingException("Missing required metrics handler");
		///////////////////////////////////////////////////////////////////
		
		for(final MicroPipelineMetricsReporterConfiguration rc : reporterConfig) {
			
			if(rc.getType() == null)
				throw new RequiredInputMissingException("Missing required type for metrics reporter configuration [id="+rc.getId()+"]");
			
			switch(rc.getType()) {
				case CONSOLE: {
					MetricsReporterFactory.attachConsoleReporter(metricsHandler, rc.getId(), rc.getPeriod());
					break;
				}
				case CSV: {
					
					if(rc.getSettings() == null || rc.getSettings().isEmpty())
						throw new RequiredInputMissingException("Missing required settings for metrics reporter configuration [id="+rc.getId()+"]");
					
					MetricsReporterFactory.attachCSVReporter(metricsHandler, rc.getId(), rc.getPeriod(), rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_CSV_OUTPUT_FILE));
					break;
				}
				case GRAPHITE: {				

					if(rc.getSettings() == null || rc.getSettings().isEmpty())
						throw new RequiredInputMissingException("Missing required settings for metrics reporter configuration [id="+rc.getId()+"]");

					String p = rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_GRAPHITE_PORT);
					int port = -1;
					try {
						port = Integer.parseInt(StringUtils.trim(p));
					} catch(Exception e) {
						throw new RequiredInputMissingException("Invalid input found for graphite port [id="+rc.getId()+"]. Error: " + e.getMessage());
					}
					
					MetricsReporterFactory.attachGraphiteReporter(metricsHandler, rc.getId(), rc.getPeriod(),  rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_GRAPHITE_HOST), port);
					
					break;
				}
				case KAFKA: {
					
					MetricsReporterFactory.attachKafkaReporter(metricsHandler, rc.getId(), rc.getPeriod(), 
							rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_KAFKA_ZOOKEEPER_CONNECT),
							rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_KAFKA_BROKER_LIST),
							rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_KAFKA_CLIENT_ID),
							rc.getSettings().get(MicroPipelineMetricsReporterConfiguration.SETTING_KAFKA_TOPIC_ID));
					break;
				}
				default: {
					throw new RequiredInputMissingException("Unknown metrics reporter type: " + rc.getType());
				}
			}
		}
	}	
}
