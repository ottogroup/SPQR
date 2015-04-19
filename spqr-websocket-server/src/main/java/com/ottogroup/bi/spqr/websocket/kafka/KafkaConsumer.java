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
package com.ottogroup.bi.spqr.websocket.kafka;

/**
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class KafkaConsumer {

	/** how to handle offset determination when no inital offset is available from zookeeper: "smallest", "largest" */
	public static final String CFG_OPT_KAFKA_AUTO_OFFSET_RESET = "kafka.consumer.autoOffsetReset";
	/** identifies the group of consumer processes the consumer belongs to */
	public static final String CFG_OPT_KAFKA_GROUP_ID = "kafka.consumer.groupId";
	/** kafka topic to consume data from */ 
	public static final String CFG_OPT_KAFKA_TOPIC = "kafka.consumer.topic";
	/** number of topic partitions to read from */
	public static final String CFG_OPT_KAFKA_PARTITIONS = "kafka.consumer.partitions";
	/** interval given in milliseconds to read data from kafka */ 
	public static final String CFG_OPT_KAFKA_TOPIC_POLLING_INTERVAL = "kafka.consumer.topicPollingInterval";
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
}
