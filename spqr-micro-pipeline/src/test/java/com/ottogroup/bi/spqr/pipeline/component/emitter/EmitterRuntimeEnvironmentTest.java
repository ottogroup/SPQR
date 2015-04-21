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
package com.ottogroup.bi.spqr.pipeline.component.emitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Test case for {@link EmitterRuntimeEnvironment}
 * @author mnxfst
 * @since Mar 11, 2015
 */
public class EmitterRuntimeEnvironmentTest {

	private final static ExecutorService executorService = Executors.newCachedThreadPool();
	
	@AfterClass
	public static void shutdown() {
		if(executorService != null)
			executorService.shutdownNow();
	}
	
	/**
	 * Test case for {@link EmitterRuntimeEnvironment#EmitterRuntimeEnvironment(Emitter, com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer)}
	 * being provided null as input to emitter parameter which must lead to {@link RequiredInputMissingException} 
	 */
	@Test
	public void testConstructor_withNullEmitter() {
		try {
			new EmitterRuntimeEnvironment(null, Mockito.mock(StreamingMessageQueueConsumer.class));
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link EmitterRuntimeEnvironment#EmitterRuntimeEnvironment(Emitter, com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer)}
	 * being provided null as input to queue consumer parameter which must lead to {@link RequiredInputMissingException} 
	 */
	@Test
	public void testConstructor_withNullQueueConsumer() {
		try {
			new EmitterRuntimeEnvironment(Mockito.mock(Emitter.class), null);
			Assert.fail("Invalid input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link EmitterRuntimeEnvironment#EmitterRuntimeEnvironment(Emitter, com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer)}
	 * being provided valid input which must lead to {@link EmitterRuntimeEnvironment#isRunning()} returning true 
	 */
	@Test
	public void testConstructor_withValidInput() throws RequiredInputMissingException {
		EmitterRuntimeEnvironment env = new EmitterRuntimeEnvironment(Mockito.mock(Emitter.class), Mockito.mock(StreamingMessageQueueConsumer.class));
		Assert.assertTrue("Must return true", env.isRunning());
	}

	/**
	 * Test case for {@link EmitterRuntimeEnvironment} being provided valid input 
	 * to constructor and some valid message which must be recorded at the emitter 
	 */
	@Test
	public void testEmitterEnvironment_withValidInput() throws RequiredInputMissingException, InterruptedException {
		Emitter emitter = Mockito.mock(Emitter.class);
		StreamingDataMessage message = new StreamingDataMessage("test".getBytes(), System.currentTimeMillis());
		StreamingMessageQueueConsumer queueConsumer = Mockito.mock(StreamingMessageQueueConsumer.class);
		StreamingMessageQueueWaitStrategy queueConsumerWaitStrategy = Mockito.mock(StreamingMessageQueueWaitStrategy.class);
		Mockito.when(queueConsumer.getWaitStrategy()).thenReturn(queueConsumerWaitStrategy);
		Mockito.when(queueConsumerWaitStrategy.waitFor(queueConsumer)).thenReturn(message);
				
		EmitterRuntimeEnvironment env = new EmitterRuntimeEnvironment(emitter, queueConsumer);
		executorService.submit(env);
		
		Thread.sleep(100);

		Assert.assertTrue("Must return true", env.isRunning());

		Mockito.verify(queueConsumerWaitStrategy, Mockito.atLeastOnce()).waitFor(queueConsumer);
		Mockito.verify(queueConsumer, Mockito.atLeastOnce()).getWaitStrategy();
		Mockito.verify(emitter, Mockito.atLeastOnce()).onMessage(message);
	}
	
	/**
	 * Test case for {@link EmitterRuntimeEnvironment} being provided valid input 
	 * to constructor and some valid message which lead to a processing error 
	 * inside the emitter 
	 */
	@Test
	public void testEmitterEnvironment_withValidInputButFailingEmitter() throws RequiredInputMissingException, InterruptedException {
		Emitter emitter = Mockito.mock(Emitter.class);
		StreamingMessageQueueConsumer queueConsumer = Mockito.mock(StreamingMessageQueueConsumer.class);
		StreamingDataMessage message = new StreamingDataMessage("test".getBytes(), System.currentTimeMillis());
		Mockito.when(queueConsumer.next()).thenReturn(message);
		Mockito.when(emitter.onMessage(message)).thenThrow(new NullPointerException("error"));
				
		StreamingMessageQueueWaitStrategy queueConsumerWaitStrategy = Mockito.mock(StreamingMessageQueueWaitStrategy.class);
		Mockito.when(queueConsumer.getWaitStrategy()).thenReturn(queueConsumerWaitStrategy);
		Mockito.when(queueConsumerWaitStrategy.waitFor(queueConsumer)).thenReturn(message);

		
		EmitterRuntimeEnvironment env = new EmitterRuntimeEnvironment(emitter, queueConsumer);
		executorService.submit(env);
		Thread.sleep(100);
		Assert.assertTrue("Must return true", env.isRunning());

		Mockito.verify(queueConsumerWaitStrategy, Mockito.atLeastOnce()).waitFor(queueConsumer);
		Mockito.verify(queueConsumer, Mockito.atLeastOnce()).getWaitStrategy();
		Mockito.verify(emitter, Mockito.atLeastOnce()).onMessage(message);
	}
	
}
