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

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Test case for {@link KafkaTopicEmitter}
 * @author mnxfst
 * @since Mar 16, 2015
 */
public class KafkaTopicEmitterTest {

	private Properties defaultProperties = new Properties();
	
	@Before
	public void initializeBeforeTest() {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_BROKER_LIST, "localhost:2181");
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_CHARSET, "UTF-8");
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_KAFKA_CLIENT_ID, "test-client");
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_MESSAGE_ACKING, "TRUE");
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_TOPIC_ID, "test-topic");
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_ZOOKEEPER_CONNECT, "localhost:9092");		
	}
	
	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided null
	 * as input which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withNullProperties() throws Exception {
		try {
			new KafkaTopicEmitter().initialize(null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * an empty client id which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withEmptyClientId() throws Exception {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_KAFKA_CLIENT_ID, "");
		try {
			new KafkaTopicEmitter().initialize(defaultProperties);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * an empty zookeeper connect which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withEmptyZookeeperConnect() throws Exception {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_ZOOKEEPER_CONNECT, "");
		try {
			new KafkaTopicEmitter().initialize(defaultProperties);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * an empty topic id which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withEmptyTopicId() throws Exception {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_TOPIC_ID, "");
		try {
			new KafkaTopicEmitter().initialize(defaultProperties);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * an empty broker list which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withEmptyBrokerList() throws Exception {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_BROKER_LIST, "");
		try {
			new KafkaTopicEmitter().initialize(defaultProperties);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * an invalid charset which must lead to {@link RequiredInputMissingException}
	 */
	@Test
	public void testInitialize_withInvalidCharset() throws Exception {
		this.defaultProperties.put(KafkaTopicEmitter.CFG_OPT_CHARSET, "unknown-charset");
		try {
			new KafkaTopicEmitter().initialize(defaultProperties);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link KafkaTopicEmitter#initialize(java.util.Properties)} being provided 
	 * valid setting 
	 */
	@Test
	public void testInitialize_withValidSettings() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testInitialize_withValidSettings");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
	}
	 
	/**
	 * Test case for {@link KafkaTopicEmitter#shutdown()} executed on a 
	 * previously initialized emitter instance where the producer has been
	 * re-set to null. The call must return with true and no errors must occur
	 * @throws Exception
	 */
	@Test	
	public void testShutdown_withNullProducer() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testShutdown_withNullProducer");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
		emitter.setProducer(null);
		Assert.assertTrue("Shutdown must be successful", emitter.shutdown());		
		Mockito.verify(producer, Mockito.never()).close();
	}
	 
	/**
	 * Test case for {@link KafkaTopicEmitter#shutdown()} executed on a 
	 * previously initialized emitter instance. All calls must executed and
	 * no errors must occur
	 * @throws Exception
	 */
	@Test	
	public void testShutdown_withValidSettings() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testInitialize_withValidSettings");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
		Assert.assertTrue("Shutdown must be successful", emitter.shutdown());		
		Mockito.verify(producer).close();
	}
	 
	/**
	 * Test case for {@link KafkaTopicEmitter#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)} being
	 * provided null which must lead to no method invocations on the producer
	 * @throws Exception
	 */
	@Test	
	public void testOnMessage_withNullMessage() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testOnMessage_withNullMessage");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
		
		emitter.onMessage(null);
		Assert.assertEquals("Values must be equal", 0, emitter.getTotalNumOfMessages());
	}
	 
	/**
	 * Test case for {@link KafkaTopicEmitter#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)} being
	 * provided a message having an empty body which must lead to no method invocations on the producer
	 * @throws Exception
	 */
	@Test	
	public void testOnMessage_withEmptyBody() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testOnMessage_withEmptyBody");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
		
		emitter.onMessage(new StreamingDataMessage("".getBytes(), System.currentTimeMillis()));
		Assert.assertEquals("Values must be equal", 0, emitter.getTotalNumOfMessages());
		Mockito.verify(producer, Mockito.never()).send(new KeyedMessage<byte[], byte[]>(
				this.defaultProperties.getProperty(KafkaTopicEmitter.CFG_OPT_TOPIC_ID), "".getBytes("UTF-8")));
	}
	 
	/**
	 * Test case for {@link KafkaTopicEmitter#onMessage(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)} being
	 * provided a message having a valid body which must lead subsequent calls issued towards the producer and an
	 * increment on the message counter
	 * @throws Exception
	 */
	@Test	
	public void testOnMessage_withValidMessage() throws Exception {
		@SuppressWarnings("unchecked")
		Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);
		KafkaTopicEmitter emitter = new KafkaTopicEmitter();
		emitter.setId("testOnMessage_withEmptyBody");
		emitter.setProducer(producer);
		emitter.initialize(defaultProperties);
		
		StreamingDataMessage message = new StreamingDataMessage("testOnMessage_withValidMessage".getBytes(), System.currentTimeMillis());
		emitter.onMessage(message);
		Assert.assertEquals("Values must be equal", 1, emitter.getTotalNumOfMessages());
	}
		
}
