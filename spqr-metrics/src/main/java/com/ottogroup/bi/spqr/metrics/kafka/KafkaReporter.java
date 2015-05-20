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
package com.ottogroup.bi.spqr.metrics.kafka;

import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Streamlines the gathered {@link Metric metrics} and exports them to an attached {@link http://kafka.apache.org}. Implementation
 * is similar to {@link https://github.com/ottogroup/SPQR/blob/master/spqr-operators/spqr-kafka/src/main/java/com/ottogroup/bi/spqr/operator/kafka/emitter/KafkaTopicEmitter.java}.
 * @author mnxfst
 * @since Mai 20, 2015
 *
 */
public class KafkaReporter extends ScheduledReporter {

	///////////////////////////////////////////////////////////////////////////////////
	// available configuration options
	/** setting to read client identifier from which is used when connection to kafka cluster */
	public static final String CFG_OPT_KAFKA_CLIENT_ID = "clientId";
	/** setting to read zookeeper connect string from */
	public static final String CFG_OPT_ZOOKEEPER_CONNECT = "zookeeperConnect";
	/** setting to read topic id from which is used to write data to */
	public static final String CFG_OPT_TOPIC_ID = "topic";
	/** setting to read value from which indicates whether to wait for acks before sending next message or not */
	public static final String CFG_OPT_MESSAGE_ACKING = "messageAcking";
	/** setting to read broker list from */
	public static final String CFG_OPT_BROKER_LIST = "metadataBrokerList";
	/** charset to apply when extracting message from kafka topic - default: UTF-8 */
	public static final String CFG_OPT_CHARSET = "charset";
	//
	///////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////
	// settings required for connecting the producer with a kafka node 
	private static final String CFG_BROKER_LIST = "metadata.broker.list";
	private static final String CFG_ZK_CONNECT = "zookeeper.connect";
	private static final String CFG_REQUEST_REQUIRED_ACKS = "request.required.acks";
	private static final String CFG_CLIENT_ID = "client.id";
	//
	///////////////////////////////////////////////////////////////////////////////////
	
	private final String topicId;
	private final Producer<byte[], byte[]> kafkaProducer;
	private final ObjectMapper jsonMapper;

	private KafkaReporter(MetricRegistry registry, String name, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter, 
			String topicId, ProducerConfig kafkaProducerConfig, String processingNodeId) {
		
		super(registry, name, filter, rateUnit, durationUnit);
		this.topicId = topicId;
		this.kafkaProducer = new Producer<>(kafkaProducerConfig);		
		this.jsonMapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true, MetricFilter.ALL));

//		TODO
//		kafkaExecutor = Executors
//				.newSingleThreadExecutor(new ThreadFactoryBuilder()
//						.setNameFormat("kafka-producer-%d").build());
	}

	
	/**
	 * @see com.codahale.metrics.ScheduledReporter#report(java.util.SortedMap, java.util.SortedMap, java.util.SortedMap, java.util.SortedMap, java.util.SortedMap)
	 */
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		
		 
		
	}

	public static void main(String[] args) throws IOException{
		
		
		
	}
	
}
