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

import java.util.concurrent.locks.LockSupport;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import uk.co.real_logic.queues.MessageWaitStrategy;
import uk.co.real_logic.queues.OneToOneConcurrentArrayQueue3;

/**
 * Reads content from an assigned {@link KafkaStream} and writes the data to a provided queue.   
 * @author mnxfst
 * @since Apr 20, 2015
 */
public class KafkaTopicStreamConsumer implements Runnable {
	
	/** externally provided queue to use for exchanging messages with underlying kafka emitter */
	private final OneToOneConcurrentArrayQueue3<byte[]> messages;
	/** stream instance to read messages from */
	private final KafkaStream<byte[], byte[]> kafkaTopicPartitionStream;
	/** indicates that the consumer is still running - may be used to shut down the consumer by simply setting to 'false' */
	private boolean running = false;
	private final MessageWaitStrategy<byte[]> messageWaitStrategy;
	
	private static final int RETRIES = 200;
	
	/**
	 * Initializes the partition consumer using the provided input
	 * @param kafkaTopicStream
	 * @param messages queue to write received content to
	 * @param messageWaitStrategy optional wait strategy applied when consuming data from queue. if provided it may be used to signal new elements 
	 */
	public KafkaTopicStreamConsumer(final KafkaStream<byte[], byte[]> kafkaTopicStream, 
			final OneToOneConcurrentArrayQueue3<byte[]> messages, final MessageWaitStrategy<byte[]> messageWaitStrategy) {
		this.kafkaTopicPartitionStream = kafkaTopicStream;
		this.messages = messages;
		this.messageWaitStrategy = messageWaitStrategy;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		// fetch message iterator from stream and mark the consumer as 'running' instance
		ConsumerIterator<byte[], byte[]> topicPartitionStreamIterator = this.kafkaTopicPartitionStream.iterator();				
		this.running = true;

		// keep on running ... until told to stop
		int counter = 0;
		while(running) {
			
			counter = RETRIES;
			while(!topicPartitionStreamIterator.hasNext()) {
				counter = waitFor(counter); // TODO provide a configurable wait strategy
			}

			// receive the next message from stream
			MessageAndMetadata<byte[], byte[]> message = topicPartitionStreamIterator.next();
			if(message != null && message.message() != null && message.message().length > 0) {
				// if the message is neither null nor empty, insert it into the queue and signal the wait strategy to 
				// release any existing locks -- if there is a wait strategy provided at all
				this.messages.add(message.message());
				if(messageWaitStrategy != null)
					messageWaitStrategy.forceLockRelease();
			}
		}
	}
	
	public int waitFor(int counter) {
		if(counter > 100)
			--counter;
		else if(counter > 0) {
			--counter;
			Thread.yield();
		} else {
			LockSupport.parkNanos(1l);
		}
			
		return counter;
	}
	
	/**
	 * Shuts down the partition consumer 
	 */
	public void shutdown() {
		this.running = false;
	}
	
	
}