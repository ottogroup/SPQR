/**
 * Copyright 2014 Otto (GmbH & Co KG)
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
package com.ottogroup.bi.spqr.websocket.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import uk.co.real_logic.queues.MessageWaitStrategy;
import uk.co.real_logic.queues.OneToOneConcurrentArrayQueue3;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;

/**
 * Consumer managing readers of configured kafka topic 
 * @author mnxfst
 * @since Jan 30, 2015
 *
 */
public class KafkaTopicConsumer {

	private static final Logger logger = Logger.getLogger(KafkaTopicConsumer.class);
	
	///////////////////////////////////////////////////////////////////////////////////
	// available configuration options
	/** how to handle offset determination when no inital offset is available from zookeeper: "smallest", "largest" */
	public static final String CFG_OPT_KAFKA_AUTO_OFFSET_RESET = "kafka.consumer.autoOffsetReset";
	/** identifies the group of consumer processes the consumer belongs to */
	public static final String CFG_OPT_KAFKA_GROUP_ID = "kafka.consumer.groupId";
	/** kafka topic to consume data from */ 
	public static final String CFG_OPT_KAFKA_TOPIC = "kafka.consumer.topic";
	/** number of threads used for reading from topic */
	public static final String CFG_OPT_KAFKA_NUM_THREADS = "kafka.consumer.threads";
	/** zookeeper connect string, eg: localhost:2181 */
	public static final String CFG_OPT_KAFKA_ZOOKEEPER_CONNECT = "kafka.consumer.zookeeperConnect";
	/** zookeeper timeout given in milliseconds */
	public static final String CFG_OPT_KAFKA_ZOOKEEPER_SESSION_TIMEOUT = "kafka.consumer.zookeeperSessionTimeout";
	/** zookeeper sync'ing interval given in milliseconds */
	public static final String CFG_OPT_KAFKA_ZOOKEEPER_SYNC_INTERVAL = "kafka.consumer.zookeeperSyncInterval";
	/** enables auto committing offsets to zookeeper, values: "true", "false" */
	public static final String CFG_OPT_KAFKA_ZOOKEEPER_AUTO_COMMIT_ENABLED = "kafka.consumer.autoCommitEnabled";
	/** interval given in milliseconds to execute offset auto commits to zookeeper */
	public static final String CFG_OPT_KAFKA_ZOOKEEPER_AUTO_COMMIT_INTERVAL = "kafka.consumer.autoCommitInterval";
	//
	///////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////
	// kafka connection parameter names
	private static final String KAFKA_CONN_PARAM_AUTO_OFFSET_RESET = "auto.offset.reset";	
	private static final String KAFKA_CONN_PARAM_GROUP_ID = "group.id";
	private static final String KAFKA_CONN_PARAM_ZK_CONNECT = "zookeeper.connect";
	private static final String KAFKA_CONN_PARAM_ZK_SESSION_TIMEOUT = "zookeeper.session.timeout.ms";
	private static final String KAFKA_CONN_PARAM_ZK_SYNC_INTERVAL = "zookeeper.sync.time.ms";
	private static final String KAFKA_CONN_PARAM_AUTO_COMMIT_ENABLED = "auto.commit.enable";
	private static final String KAFKA_CONN_PARAM_AUTO_COMMIT_INTERVAL = "auto.commit.interval.ms";	//
	///////////////////////////////////////////////////////////////////////////////////
	
	public static final String KAFKA_AUTO_OFFSET_RESET_TYPE_LARGEST = "largest";
	public static final String KAFKA_AUTO_OFFSET_RESET_TYPE_SMALLEST = "smallest";

	/** message queue used to send incoming messages from partition consumers to websocket emitter */
	private final OneToOneConcurrentArrayQueue3<byte[]> messages;
	/** wait strategy applied on queue */
	private final MessageWaitStrategy<byte[]> messageWaitStrategy;
	/** externally provided executor service used as runtime environment for partition consumers */
	private final ExecutorService executorService;
	/** map of partition consumers initialized on the kafka topic this consumer is attached to */
	private final Map<String, KafkaTopicStreamConsumer> partitionConsumers = new HashMap<String, KafkaTopicStreamConsumer>();
	/** kafka topic client - establishes and manages the connection with a kafak topic */
	private ConsumerConnector kafkaConsumerConnector = null;
	
	///////////////////////////////////////////////////////////////////////////////////
	// configuration values
	/** zookeeper connection string, eg. localhost:2181 */
	private String zookeeperConnect = null;
	/** timeout value applied to zookeeper session */
	private String zookeeperSessionTimeout = null;
	/** interval used for sync'ing with zookeeper */
	private String zookeeperSyncInterval = null;
	/** messages must be committed to mark them as successfully consumed - default: true */
	private String autoCommitEnabled = "true"; // default
	/** interval used for committing messages - activated when autoCommitEnabled is set to true */
	private String autoCommitInterval = null;
	/** offset reset type, values: "largest" (default) or "smallest" */
	private String autoOffsetResetType = "largest"; // default
	/** group identifier to use when establishing a connection with kafka */ 
	private String groupId = null;
	/** topic to read data from */
	private String topic = null;
	/** number of threads used for consuming contents from topic */ 
	private int numOfThreads = 5; // default;
	
	/**
	 * Initializes the consumer using the provided input
	 * @param message
	 * @param executorService
	 */
	public KafkaTopicConsumer(final OneToOneConcurrentArrayQueue3<byte[]> message, final MessageWaitStrategy<byte[]> messageWaitStrategy, final ExecutorService executorService) {
		this.messages = message;
		this.executorService = executorService;
		this.messageWaitStrategy = messageWaitStrategy;
	}
	
	public void initialize(final Map<String, String> settings) throws RequiredInputMissingException {
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		// extract data required for setting up a consumer from configuration 
		
		// read out auto offset reset type and check if it contains a valid value, otherwise reset to 'LARGEST'
		this.autoOffsetResetType = settings.get(CFG_OPT_KAFKA_AUTO_OFFSET_RESET);
		if(!StringUtils.equalsIgnoreCase(this.autoOffsetResetType, KAFKA_AUTO_OFFSET_RESET_TYPE_LARGEST) && 
				!StringUtils.equalsIgnoreCase(this.autoOffsetResetType,  KAFKA_AUTO_OFFSET_RESET_TYPE_SMALLEST))
			this.autoOffsetResetType = KAFKA_AUTO_OFFSET_RESET_TYPE_LARGEST;

		// read out value indicating whether auto commit is enabled or not and validate it for 'true' or 'false' --> otherwise set to default 'true'
		this.autoCommitEnabled = settings.get(CFG_OPT_KAFKA_ZOOKEEPER_AUTO_COMMIT_ENABLED);
		if(!StringUtils.equalsIgnoreCase(this.autoCommitEnabled, "true") &&
				!StringUtils.equalsIgnoreCase(this.autoCommitEnabled, "false"))
			this.autoCommitEnabled = "true";
		
		// check if the provided session timeout is a valid number --> otherwise reset to default '6000' 
		this.zookeeperSessionTimeout = settings.get(CFG_OPT_KAFKA_ZOOKEEPER_SESSION_TIMEOUT);
		try {
			long longVal = Long.parseLong(this.zookeeperSessionTimeout);
			if(longVal < 1) {
				logger.info("Found invalid session timeout value '"+this.zookeeperSessionTimeout+"'. Resetting to '6000'");
				this.zookeeperSessionTimeout = "6000";
			}
		} catch(Exception e) {
			logger.info("Found invalid session timeout value '"+this.zookeeperSessionTimeout+"'. Resetting to '6000'");
			this.zookeeperSessionTimeout = "6000";
		}

		// check if the provided sync interval is a valid number --> otherwise reset to default '2000'
		this.zookeeperSyncInterval = settings.get(CFG_OPT_KAFKA_ZOOKEEPER_SYNC_INTERVAL);
		try {
			long longVal = Long.parseLong(this.zookeeperSyncInterval);
			if(longVal < 1) {
				logger.info("Found invalid session sync interval '"+this.zookeeperSyncInterval+"'. Resetting to '2000'");
				this.zookeeperSyncInterval = "2000";
			}
		} catch(Exception e) {
			logger.info("Found invalid session sync interval '"+this.zookeeperSyncInterval+"'. Resetting to '2000'");
			this.zookeeperSyncInterval = "2000";
		}
		
		// check if the provided auto commit interval is a valid number --> otherwise reset to default '60 * 1000'
		this.autoCommitInterval = settings.get(CFG_OPT_KAFKA_ZOOKEEPER_AUTO_COMMIT_INTERVAL);
		try {
			long longVal = Long.parseLong(this.autoCommitInterval);
			if(longVal < 1) {
				logger.info("Found invalid auto commit interval '"+this.autoCommitInterval+"'. Resetting to '60000'");
				this.autoCommitInterval = "60000";
			}
		} catch(Exception e) {
			logger.info("Found invalid auto commit interval '"+this.autoCommitInterval+"'. Resetting to '60000'");
			this.autoCommitInterval = "60000";
		}

		String numOfThreadsStr = settings.get(CFG_OPT_KAFKA_NUM_THREADS);
		try {
			this.numOfThreads = Integer.parseInt(numOfThreadsStr);
		} catch(Exception e) {
			logger.info("Found invalid number of partitions '"+numOfThreadsStr+"'. Resetting to '60000'");
		}
		if(this.numOfThreads < 1)
			this.numOfThreads = 5;
		
		this.groupId = settings.get(CFG_OPT_KAFKA_GROUP_ID);
		this.topic = settings.get(CFG_OPT_KAFKA_TOPIC);
		this.zookeeperConnect = settings.get(CFG_OPT_KAFKA_ZOOKEEPER_CONNECT);

		//
		///////////////////////////////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		// establish connection with kafka

		Properties props = new Properties();
		props.put(KAFKA_CONN_PARAM_ZK_CONNECT, this.zookeeperConnect);
		props.put(KAFKA_CONN_PARAM_ZK_SESSION_TIMEOUT, this.zookeeperSessionTimeout);
		props.put(KAFKA_CONN_PARAM_ZK_SYNC_INTERVAL, this.zookeeperSyncInterval);
		props.put(KAFKA_CONN_PARAM_GROUP_ID, this.groupId);
		props.put(KAFKA_CONN_PARAM_AUTO_COMMIT_INTERVAL, this.autoCommitInterval);
		props.put(KAFKA_CONN_PARAM_AUTO_OFFSET_RESET, this.autoOffsetResetType);
		props.put(KAFKA_CONN_PARAM_AUTO_COMMIT_ENABLED, this.autoCommitEnabled);
		
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		this.kafkaConsumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
		
		// get access to topic streams
		Map<String, Integer> topicCountMap = new HashMap<>();
		topicCountMap.put(this.topic, this.numOfThreads);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = this.kafkaConsumerConnector.createMessageStreams(topicCountMap);
				
		// receive topic streams, each entry holds a single partition
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(this.topic);
		if(streams == null || streams.isEmpty())
			throw new RuntimeException("Failed to establish connection with kafka topic [zkConnect="+this.zookeeperConnect+", topic="+this.topic+"]");
		
		// iterate through streams and assign each to a partition reader
		for(KafkaStream<byte[], byte[]> kafkaStream : streams) {
				
			if(kafkaStream == null)
				throw new RuntimeException("Found null entry in list of kafka streams [zkConnect="+this.zookeeperConnect+", topic="+this.topic+"]");
			
			KafkaTopicStreamConsumer partitionConsumer = new KafkaTopicStreamConsumer(kafkaStream, this.messages, this.messageWaitStrategy);
			executorService.submit(partitionConsumer);
			this.partitionConsumers.put("kafka-topic-"+topic, partitionConsumer);
		}
	}
	
	/**
	 * Shuts down all running {@link KafkaTopicStreamConsumer} instances, commits all offsets and finally 
	 * closes the {@link ConsumerConnector}
	 */
	public void shutdown() {

		// shutdown all partition consumers and remove their instances from the internal map
		for(String id : this.partitionConsumers.keySet()) {
			KafkaTopicStreamConsumer kafkaTopicPartitionConsumer = this.partitionConsumers.get(id);
			kafkaTopicPartitionConsumer.shutdown();
			this.partitionConsumers.remove(id);
		}
		
		// finally commit all offsets and shutdown the consumer
		this.kafkaConsumerConnector.commitOffsets();
		this.kafkaConsumerConnector.shutdown();

	}
	
}
