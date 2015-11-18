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
package com.ottogroup.bi.spqr.pipeline.component.queue.chronicle;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;

import net.openhft.chronicle.Chronicle;

/**
 * Test case for {@link DefaultStreamingMessageQueue}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class DefaultStreamingMessageQueueTest {

	private static final Logger logger = Logger.getLogger(DefaultStreamingMessageQueueTest.class);
	
	private static final int numberOfMessagesPerfTest = 100;
	
	/**
	 * Test case for {@link DefaultStreamingMessageQueue#initialize(java.util.Properties)} being provided
	 * null where an exception is the expected behavior
	 */
	@Test
	public void testInitialize_withNullProperties() {
		try {
			new DefaultStreamingMessageQueue().initialize(null);
			Assert.fail("Missing required properties");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link DefaultStreamingMessageQueue#initialize(java.util.Properties)} being provided
	 * an empty properties set where an exception is the expected behavior 
	 */
	@Test
	public void testInitialize_withEmptyProperties() throws RequiredInputMissingException {
		DefaultStreamingMessageQueue queue = new DefaultStreamingMessageQueue();
		queue.setId("testInitialize_withEmptyProperties");
		queue.initialize(new Properties());	
	}

	/**
	 * Test case for {@link DefaultStreamingMessageQueue#initialize(java.util.Properties)} being provided
	 * a properties set where the id is missing which must lead to a runtime exception 
	 */
	@Test
	public void testInitialize_withEmptyIdentifier() {
		try {
			Properties props = new Properties();
			props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
			props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));
			new DefaultStreamingMessageQueue().initialize(props);
			Assert.fail("Missing required properties");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link DefaultStreamingMessageQueue#initialize(java.util.Properties)} being provided
	 * a properties set where the queue path is missing which must lead to a runtime exception 
	 */
	@Test
	public void testInitialize_withEmptyPath() throws RequiredInputMissingException {
		Properties props = new Properties();
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, "");
		DefaultStreamingMessageQueue queue = new DefaultStreamingMessageQueue();
		queue.setId("testInitialize_withEmptyPath");
		queue.initialize(props);
	}

	/**
	 * Test case for {@link DefaultStreamingMessageQueue#initialize(java.util.Properties)} being provided
	 * a properties set where all required settings are contained - no exception is expected 
	 */
	@Test
	public void testInitialize_withValidSettings() throws RequiredInputMissingException {
		Properties props = new Properties();
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));
		DefaultStreamingMessageQueue queue = new DefaultStreamingMessageQueue();
		queue.setId("testInitialize_withValidSettings");
		queue.initialize(props);
	}

	/**
	 * Test case for {@link DefaultStreamingMessageQueue#next()} requesting a single message from a
	 * temporary queue which must be returned without errors 
	 */
	@Test
	public void testNext_withSingleMessage() throws IOException, RequiredInputMissingException {
		Properties props = new Properties();
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));
		DefaultStreamingMessageQueue inbox = new DefaultStreamingMessageQueue();
		inbox.setId("testNext_withSingleMessages");
		inbox.initialize(props);

		long timestamp = System.currentTimeMillis();
		String text = "This is a simple test message";
		byte[] content = text.getBytes(); 

		Assert.assertTrue(inbox.getProducer().insert(new StreamingDataMessage(content, timestamp)));		
		StreamingDataMessage msg = inbox.getConsumer().next();

		Assert.assertTrue("Values must be equal", StringUtils.equalsIgnoreCase(new String(content), new String(msg.getBody())));
		Assert.assertEquals("Values must be equal", timestamp, msg.getTimestamp());		
	}
	
	/**
	 * Inserts a configurable number of messages into a {@link Chronicle} and measures the
	 * duration it takes to read the content from it using the {@link DefaultStreamingMessageQueue} implementation
	 */
//	@Test
	public void testNext_performanceTest() throws Exception {

		Properties props = new Properties();
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		props.put(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));
		final DefaultStreamingMessageQueue inbox = new DefaultStreamingMessageQueue();
		inbox.setId("testNext_performanceTest");
		inbox.initialize(props);
		
		final StreamingMessageQueueProducer producer = inbox.getProducer();
		final StreamingMessageQueueConsumer consumer = inbox.getConsumer();
		
		
		
		final CountDownLatch latch = new CountDownLatch(numberOfMessagesPerfTest);
		
		ExecutorService svc = Executors.newCachedThreadPool();

		Future<Integer> producerDurationFuture = svc.submit(new Callable<Integer>() {
			
			public Integer call() {
				StreamingDataMessage object = new StreamingDataMessage(new byte[]{01,2,3,4,5,6,7,9}, System.currentTimeMillis());
				long s1 = System.nanoTime();
				for(int i = 0; i < numberOfMessagesPerfTest; i++) {
					producer.insert(object);
				}
				long s2 = System.nanoTime();
				return (int)(s2-s1);
			}
		});

		Future<Integer> durationFuture = svc.submit(new Callable<Integer>() {
			public Integer call() {
				StreamingDataMessage msg = null;
				long start = System.nanoTime();
				while(true) {					
					msg = consumer.next();
					if(msg != null) {
						latch.countDown();
						if(latch.getCount() == 0)
							break;
					} else {
						LockSupport.parkNanos(1);
					}
					
				}
				long end = System.nanoTime();
				return (int)(end-start);
			}
		});
		

		try {
			Assert.assertTrue("Failed to receive expected number of messages", latch.await(10,  TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Assert.fail("Failed to receive expected number of messages");
		}
		
		int producerDuration = producerDurationFuture.get();
		int duration = durationFuture.get(); 
		
		double messagesPerNano = ((double)numberOfMessagesPerfTest / (double)duration);
		double messagesPerNanoRounded = (double)Math.round(messagesPerNano* 10000) / 10000;
		
		double messagesPerMilli = messagesPerNano * 1000000;
		messagesPerMilli = (double)Math.round(messagesPerMilli * 100) / 100;
		
		long messagesPerSecondTmps = Math.round(messagesPerNano * 1000000 * 1000);
		double messagesPerSecond = (double)Math.round(messagesPerSecondTmps);;
		
		double nanosPerMessage = ((double)duration / (double)numberOfMessagesPerfTest);
		nanosPerMessage = (double)Math.round(nanosPerMessage * 100) /100;

		logger.info("message count: " + numberOfMessagesPerfTest);
		logger.info("message producing: " + producerDuration +"ns, "+TimeUnit.NANOSECONDS.toMillis(producerDuration)+"ms, "+TimeUnit.NANOSECONDS.toSeconds(producerDuration)+"s");
		logger.info("message consumption: " + duration +"ns, "+TimeUnit.NANOSECONDS.toMillis(duration)+"ms, "+TimeUnit.NANOSECONDS.toSeconds(duration)+"s");
		logger.info("message throughput: " + messagesPerNanoRounded + " msgs/ns, "+ messagesPerMilli + " msgs/ms, " + messagesPerSecond + " msgs/s");		
		
		svc.shutdownNow();
	}
}
