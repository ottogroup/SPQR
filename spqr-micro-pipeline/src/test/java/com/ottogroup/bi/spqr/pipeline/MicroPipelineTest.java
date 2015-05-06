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
package com.ottogroup.bi.spqr.pipeline;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.pipeline.component.emitter.CountDownLatchTestEmitter;
import com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter;
import com.ottogroup.bi.spqr.pipeline.component.emitter.EmitterRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.component.source.RandomNumberTestSource;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.pipeline.component.source.SourceRuntimeEnvironment;
import com.ottogroup.bi.spqr.pipeline.queue.chronicle.DefaultStreamingMessageQueue;

/**
 * Test case for {@link MicroPipeline}
 * @author mnxfst
 * @since Mar 17, 2015
 */
public class MicroPipelineTest {

	private static ExecutorService executorService = Executors.newCachedThreadPool();
	
	@AfterClass
	public static void shutdown() {
		if(executorService != null)
			executorService.shutdownNow();
	}
	
	/**
	 * Performance test for {@link MicroPipeline}. It will insert a fixed number of messages
	 * at {@link Source} level and consume the same number at {@link Emitter}. It measures the
	 * overall duration it took to consume all messages, computes the average traveling time
	 * as well as the shortest and longest path
	 */
	@Test
	public void test_performance1() throws Exception {
	
		if(true)
			return;
		
		@SuppressWarnings("unused")
		MicroPipelineConfiguration cfg = null; //new ObjectMapper().readValue(new File("/home/mnxfst/projects/spqr/twitter-to-kafka.json"), MicroPipelineConfiguration.class);
		final int numGeneratedMessages = 10000000;
		final String msg = "test message";new ObjectMapper().writeValueAsString(cfg);
		
		final CountDownLatch latch = new CountDownLatch(numGeneratedMessages);

		///////////////////////////////////////////////////////////////////////
		// queue
		Properties queueProps = new Properties();
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
//		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, "/mnt/ramdisk");
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, "/tmp");
		DefaultStreamingMessageQueue queue = new DefaultStreamingMessageQueue();
		queue.setId("test_performance1_queue");
		queue.initialize(queueProps);

		Properties statsQueueProps = new Properties();
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT, "true");
		queueProps.setProperty(DefaultStreamingMessageQueue.CFG_CHRONICLE_QUEUE_PATH, "/tmp");
		DefaultStreamingMessageQueue statsQueue = new DefaultStreamingMessageQueue();
		statsQueue.setId(MicroPipeline.STATISTICS_QUEUE_NAME);
		statsQueue.initialize(statsQueueProps);
		
		///////////////////////////////////////////////////////////////////////
		// source
		Properties rndGenProps = new Properties();
		rndGenProps.setProperty(RandomNumberTestSource.CFG_CONTENT, msg);		
		rndGenProps.setProperty(RandomNumberTestSource.CFG_MAX_NUM_GENERATED, String.valueOf(numGeneratedMessages));		

		RandomNumberTestSource source = new RandomNumberTestSource();
		source.initialize(rndGenProps);
		source.setId("test_performance1_source");
		
		SourceRuntimeEnvironment srcEnv = new SourceRuntimeEnvironment(source, queue.getProducer(), statsQueue.getProducer());

		///////////////////////////////////////////////////////////////////////
		// emitter
		Properties emitterProps = new Properties();
		emitterProps.setProperty("await", String.valueOf(numGeneratedMessages));
		
		CountDownLatchTestEmitter emitter = new CountDownLatchTestEmitter();
		emitter.setLatch(latch);		
		emitter.setId("test_performance1_emitter");
		emitter.initialize(emitterProps);
		
		EmitterRuntimeEnvironment emitterEnv = new EmitterRuntimeEnvironment(emitter, queue.getConsumer(), statsQueue.getProducer());
		
		MicroPipeline pipeline = new MicroPipeline("test_performance1");
		pipeline.addQueue(queue.getId(), queue);		
		pipeline.addSource(source.getId(), srcEnv);
		pipeline.addEmitter(emitter.getId(), emitterEnv);
		long s1 = System.currentTimeMillis();
		executorService.submit(srcEnv);
		executorService.submit(emitterEnv);
		while(!latch.await(5000, TimeUnit.MILLISECONDS));
		long s2 = System.currentTimeMillis();
		
		long duration = s2-s1;
		
		System.out.println(numGeneratedMessages + " messages transferred in " + duration + "ms");
		
	}
}
