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
package com.ottogroup.bi.spqr.pipeline.component.source;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;

/**
 * Test case for {@link SourceRuntimeEnvironment}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class SourceRuntimeEnvironmentTest {
	
	/**
	 * Test case for {@link SourceRuntimeEnvironment#SourceRuntimeEnvironment(Source, java.util.Map)} being
	 * provided null as input to source parameter which must lead to an exception
	 */
	@Test
	public void testConstructor_withNullSource() {
		StreamingMessageQueueProducer mockProducer = Mockito.mock(StreamingMessageQueueProducer.class);
		try {			
			new SourceRuntimeEnvironment(null, mockProducer);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SourceRuntimeEnvironment#SourceRuntimeEnvironment(Source, java.util.Map)} being
	 * provided null as input to queue producer parameter which must lead to an exception
	 */
	@Test
	public void testConstructor_withNullQueueProducers() {
		Source mockSource = Mockito.mock(Source.class);
		try {			
			new SourceRuntimeEnvironment(mockSource, null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link SourceRuntimeEnvironment#run()} with a fully functional {@link Source} implementation
	 */
	@Test
	public void testRun_withFullyFunctionalSource() throws RequiredInputMissingException, IOException, InterruptedException, ComponentInitializationFailedException {
		
		final int numGenerated = 10000;
		final CountDownLatch latch = new CountDownLatch(numGenerated);
		
		ExecutorService svc = Executors.newCachedThreadPool();
		Properties queueProps = new Properties();
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, System.getProperty("java.io.tmpdir"));

		Properties rndGenProps = new Properties();		
		rndGenProps.setProperty(RandomNumberTestSource.CFG_MAX_NUM_GENERATED, String.valueOf(numGenerated));		

		RandomNumberTestSource source = new RandomNumberTestSource();
		source.initialize(rndGenProps);
		source.setId("testRun_withFullyFunctionalSource");
		
		DefaultStreamingMessageQueue queue = new DefaultStreamingMessageQueue();
		queue.setId("testRun_withFullyFunctionalSource");
		queue.initialize(queueProps);
	
		final StreamingMessageQueueConsumer consumer = queue.getConsumer();
		
		SourceRuntimeEnvironment env = null;
		try {
			env = new SourceRuntimeEnvironment(source, queue.getProducer());			
			svc.submit(env);
			svc.submit(new Runnable() {
				
				public void run() {
					int count = 0;
					long s1 = System.currentTimeMillis();
					int numElements = numGenerated;
					while(numElements > 0) {
						if(consumer.next() != null) {
							count++;
							numElements--;
							latch.countDown();
									
						}
					}
					System.out.println("Received " + count + " messages in " + (System.currentTimeMillis()-s1)+"ms");
				}
			});
			
			
			Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
		} finally {
			env.shutdown();
			svc.shutdownNow();
		}
		
		
	}

}
