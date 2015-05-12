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

/**
 * Interface to be implemented by a wait strategies applied where waiting for message is required
 * @author mnxfst
 * @since Apr 20, 2015
 */
public interface MessageWaitStrategy <E> {

	/** 
	 * Wait for next element from referenced {@link Queue}.  
	 * @param queue
	 * @return
	 * @throws InterruptedException
	 */
	public byte[] waitFor(final Queue<E> queue) throws InterruptedException;
	
	/**
	 * Wait for next element from referenced {@link Queue}. If the timeout is 
	 * reached the result may contain <i>null</i>.
	 * @param queue
	 * @param timeout
	 * @param timeoutUnit
	 * @return
	 * @throws InterruptedException
	 */
	public byte[] waitFor(final Queue<E> queue, final long timeout, final TimeUnit timeoutUnit) throws InterruptedException;
	
	/**
	 * Forces release of existing locks
	 */
	public void forceLockRelease();
}
