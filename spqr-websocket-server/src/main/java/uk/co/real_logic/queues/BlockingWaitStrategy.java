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
package uk.co.real_logic.queues;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a wait strategy in the style of {@link https://github.com/jbrisbin/disruptor/blob/master/src/main/java/com/lmax/disruptor/BlockingWaitStrategy.java}.
 * It tries to read messages from an assigned {@link Queue}. If there is no content the strategy blocks until
 * either a timeout is reached or sleeping is interrupted from the outside. Apply this strategy when throughput and latency may
 * be spoiled in favor of CPU consumption. 
 * @author mnxfst
 * @since Apr 20, 2015
 */
public class BlockingWaitStrategy implements MessageWaitStrategy<byte[]> {
	
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	
	/**
	 * @see uk.co.real_logic.queues.MessageWaitStrategy#waitFor(java.util.Queue)
	 */
	public byte[] waitFor(Queue<byte[]> queue) throws InterruptedException {
		
		byte[] message = null;
		
		if((message = queue.poll()) == null) {
			
			// acquire lock			
			lock.lock();
			try {				
				// try to fetch the next element from the queue.
				// if there is no entry available, wait for external notification (forceLockRelease required)
				while((message = queue.poll()) == null) {
					condition.await();
				}
				
			} finally {
				// release lock
				lock.unlock();
			}			
		}
		
		return message;
	}

	/**
	 * @see com.ottogroup.bi.spqr.websocket.strategy.MessageWaitStrategy#waitFor(Queue, long, TimeUnit)}
	 */
	public byte[] waitFor(Queue<byte[]> queue, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
		byte[] message = null;
		
		if((message = queue.poll()) == null) {
			
			// acquire lock			
			lock.lock();
			try {				
				// try to fetch the next element from the queue.
				// if there is no entry available, wait for time out and try again
				while((message = queue.poll()) == null) {
					if(!condition.await(timeout, timeoutUnit)) {
						break;
					}
				}
				
			} finally {
				// release lock
				lock.unlock();
			}			
		}
		
		return message;
	}

	/**
	 * @see uk.co.real_logic.queues.MessageWaitStrategy#forceLockRelease()
	 */
	public void forceLockRelease() {
		
		// acquire lock
		lock.lock();
		try {
			// free all
			condition.signalAll();
		} finally {
			// release lock
			lock.unlock();
		}
		
		
	}

}
