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
package com.ottogroup.bi.spqr.operator.kafka.emitter;

import java.nio.charset.Charset;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Emits {@link StreamingDataMessage} received from the surrounding micro pipeline and writes
 * the {@link StreamingDataMessage#getBody() body} to the configured kafka topic
 * @author mnxfst
 * @since Mar 16, 2015
 */
@SPQRComponent(type=MicroPipelineComponentType.EMITTER, name="kafkaEmitter", version="0.0.1", description="Kafka topic emitter")
public class KafkaTopicEmitter implements Emitter {

	private static final Logger logger = Logger.getLogger(KafkaTopicEmitter.class);
	
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
	
	private String id = null;
	private String clientId = null;
	private String zookeeperConnect = null;
	private String topicId = null;
	private boolean messageAcking = false;
	private String brokerList = null;	
	private long messageCounter = 0;
	private Charset charset = null;
	private Producer<byte[], byte[]> kafkaProducer;

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {

		/////////////////////////////////////////////////////////////////////////
		// input extraction and validation
		
		if(properties == null)
			throw new RequiredInputMissingException("Missing required properties");
		
		clientId = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_OPT_KAFKA_CLIENT_ID)));
		if(StringUtils.isBlank(clientId))
			throw new RequiredInputMissingException("Missing required kafka client id");
		
		zookeeperConnect = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_OPT_ZOOKEEPER_CONNECT)));
		if(StringUtils.isBlank(zookeeperConnect))
			throw new RequiredInputMissingException("Missing required zookeeper connect");
		
		topicId = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_OPT_TOPIC_ID)));
		if(StringUtils.isBlank(topicId))
			throw new RequiredInputMissingException("Missing required topic");

		brokerList = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_OPT_BROKER_LIST)));
		if(StringUtils.isBlank(brokerList))
			throw new RequiredInputMissingException("Missing required broker list");
		
		String charsetName = StringUtils.trim(properties.getProperty(CFG_OPT_CHARSET));
		try {
			if(StringUtils.isNotBlank(charsetName))
				charset = Charset.forName(charsetName);
			else
				charset = Charset.forName("UTF-8");
		} catch(Exception e) {
			throw new RequiredInputMissingException("Invalid character set provided: " + charsetName + ". Error: " + e.getMessage());
		}
		
		messageAcking = StringUtils.equalsIgnoreCase(StringUtils.trim(properties.getProperty(CFG_OPT_MESSAGE_ACKING)), "true");
		//
		/////////////////////////////////////////////////////////////////////////

		if(logger.isDebugEnabled())
			logger.debug("kafka emitter[id="+this.id+", client="+clientId+", topic="+topicId+", broker="+brokerList+", zookeeper="+zookeeperConnect+", charset="+charset.name()+", messageAck="+messageAcking+"]");		
		
		// initialize the producer only if it is not already exist --- typically assigned through test case!
		if(kafkaProducer == null) { 
			Properties props = new Properties();		
			props.put(CFG_ZK_CONNECT, zookeeperConnect);
			props.put(CFG_BROKER_LIST, brokerList);
			props.put(CFG_REQUEST_REQUIRED_ACKS, messageAcking);
			props.put(CFG_CLIENT_ID, clientId);
			this.kafkaProducer = new Producer<>(new ProducerConfig(props));
		}
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		
		if(this.kafkaProducer != null) {
			try {
				kafkaProducer.close();
			} catch(Exception e) {
				logger.error("Failed to close kafka producer [id="+id+"]. Reason: "+e.getMessage());
			}
		}
		
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public boolean onMessage(StreamingDataMessage message) {
		if(message != null && StringUtils.isNotBlank(message.getBody())) {
			this.kafkaProducer.send(new KeyedMessage<byte[], byte[]>(this.topicId, message.getBody().getBytes(charset)));
			this.messageCounter++;
		}
		return true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter#getTotalNumOfMessages()
	 */
	public long getTotalNumOfMessages() {
		return this.messageCounter;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.EMITTER;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Assigns a kafka producer instance - <b>FOR TESTING PURPOSE ONLY</b> 
	 * @param producer
	 */
	protected void setProducer(final Producer<byte[], byte[]> producer) {
		this.kafkaProducer = producer;
	}
	
}
