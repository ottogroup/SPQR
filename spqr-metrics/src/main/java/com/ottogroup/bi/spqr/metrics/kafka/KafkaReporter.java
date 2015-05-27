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

import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Streamlines the gathered {@link Metric metrics} and exports them to an attached {@link http://kafka.apache.org}. Implementation
 * is similar to {@link https://github.com/ottogroup/SPQR/blob/master/spqr-operators/spqr-kafka/src/main/java/com/ottogroup/bi/spqr/operator/kafka/emitter/KafkaTopicEmitter.java}.
 * @author mnxfst
 * @since Mai 20, 2015
 *
 */
public class KafkaReporter extends ScheduledReporter {
	
	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(KafkaReporter.class);

	private static final String REPORTER_NAME = "kafka-reporter";
	
	///////////////////////////////////////////////////////////////////////////////////
	// settings required for connecting the producer with a kafka node 
	private static final String CFG_BROKER_LIST = "metadata.broker.list";
	private static final String CFG_ZK_CONNECT = "zookeeper.connect";
	private static final String CFG_REQUEST_REQUIRED_ACKS = "request.required.acks";
	private static final String CFG_CLIENT_ID = "client.id";
	//
	///////////////////////////////////////////////////////////////////////////////////

	private final MetricRegistry registry;
	private final String topicId;
	private final Producer<byte[], byte[]> kafkaProducer;
	private final ObjectMapper jsonMapper;
	private final ExecutorService kafkaExecutor = Executors.newSingleThreadExecutor();

	/**
	 * Instantiates a reporter instance sending {@link Metric metrics} to kafka topic
	 * @param registry
	 * @param name
	 * @param rateUnit
	 * @param durationUnit
	 * @param filter
	 * @param topicId
	 * @param kafkaProducerConfig
	 */
	private KafkaReporter(MetricRegistry registry, String name, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter, 
			String topicId, ProducerConfig kafkaProducerConfig) {
		super(registry, name, filter, rateUnit, durationUnit);
		this.registry = registry;
		this.topicId = topicId;
		this.kafkaProducer = new Producer<>(kafkaProducerConfig);		
		this.jsonMapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit, durationUnit, false, filter));
	}

	
	/**
	 * @see com.codahale.metrics.ScheduledReporter#report(java.util.SortedMap, java.util.SortedMap, java.util.SortedMap, java.util.SortedMap, java.util.SortedMap)
	 */
	public void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		// do nothing --- invoked by ScheduledReporter#report() which is overridden down below
	}
	
	/**
	 * @see com.codahale.metrics.ScheduledReporter#report()
	 */
	public void report() {
        synchronized (this) {
        	// exec async
        	kafkaExecutor.submit(new Runnable() {				
				public void run() {
		        	try {
						kafkaProducer.send(new KeyedMessage<byte[], byte[]>(topicId, jsonMapper.writeValueAsBytes(registry)));
					} catch (JsonProcessingException e) {
						logger.error("Failed to send message to kafka [topic="+topicId+"]. Reason: " + e.getMessage(), e);
					}
				}
			});
        }
	}
	
	 /**
     * Returns a new {@link Builder} for {@link KafkaReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link KafkaReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link KafkaReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String topic;
        private String brokerList = null;
        private String zkConnect = null;
        private String clientId = null;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }
        
        /**
         * Name of topic to report metrics to
         * @param topic
         * @return
         */
        public Builder topic(String topic) {
        	this.topic = topic;
        	return this;
        }

        /**
         * List of Kafka brokers to establish a connection with, eg. localhost:9092
         * @param brokerList
         * @return
         */
        public Builder brokerList(String brokerList) {
        	this.brokerList = brokerList;
        	return this;
        }
        
        /**
         * Zookeeper connect string, eg. localhost:2181
         * @param zookeeperConnect
         * @return
         */
        public Builder zookeeperConnect(String zookeeperConnect) {
        	this.zkConnect = zookeeperConnect;
        	return this;
        }
        
        /**
         * Client identifier used when connecting to Kafka
         * @param clientId
         * @return
         */
        public Builder clientId(String clientId) {
        	this.clientId = clientId;
        	return this;
        }

        /**
         * Builds a {@link KafkaReporter} using the provided {@link ProducerConfig}. All manually
         * provided settings, like zookeeperConnect, will be ignored
         * @param config
         * @return
         */
        public KafkaReporter build(ProducerConfig config) {
        	return new KafkaReporter(registry, REPORTER_NAME, rateUnit, durationUnit, filter, topic, config);
        }
        
        /**
         * Builds a {@link KafkaReporter} using the previously provided settings. Ensure that all
         * settings required to establish a connection with Kafka are provided
         * @return a {@link KafkaReporter}
         */
        public KafkaReporter build() {
        	
        	Properties props = new Properties();
        	props.put(CFG_BROKER_LIST, this.brokerList);
        	props.put(CFG_CLIENT_ID, this.clientId);
        	props.put(CFG_REQUEST_REQUIRED_ACKS, "0");
        	props.put(CFG_ZK_CONNECT, this.zkConnect);        	
        	
        	return build(new ProducerConfig(props));
        }
    }
}
