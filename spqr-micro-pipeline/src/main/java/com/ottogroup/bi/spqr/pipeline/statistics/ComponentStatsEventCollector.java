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
package com.ottogroup.bi.spqr.pipeline.statistics;

import org.apache.log4j.Logger;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.ottogroup.bi.spqr.pipeline.MicroPipeline;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Attached to a {@link MicroPipeline} the collector reads out all {@link AggregatedComponentStatistics} from the assigned
 * {@link StreamingMessageQueue stats queue} and exports to an attached destination
 * @author mnxfst
 * @since May 6, 2015
 */
public class ComponentStatsEventCollector implements Runnable {

	/** our faithful logging facility ... ;-) */
	private final static Logger logger = Logger.getLogger(ComponentStatsEventCollector.class);
	
	/** identifier of processing node this collector lives on */
	private final String processingNodeId;
	/** identifier of pipeline the collector is attached to */
	private final String pipelineId;
	/** queue consumer to retrieve statistics from */
	private final StreamingMessageQueueConsumer statsQueueConsumer;
	/** queue wait strategy to apply when retrieving stats */
	private final StreamingMessageQueueWaitStrategy statsQueueWaitStrategy;
	/** indicates whether the collector is running or not */
	private boolean running = false;
	private int messageCount = 0;
	
	public ComponentStatsEventCollector(final String processingNodeId, final String pipelineId,  
			final StreamingMessageQueueConsumer statsQueueConsumer, final StreamingMessageQueueWaitStrategy statsQueueWaitStrategy) {
		
		
		
		this.processingNodeId = processingNodeId;
		this.pipelineId = pipelineId;
		this.statsQueueConsumer = statsQueueConsumer;
		this.statsQueueWaitStrategy = statsQueueWaitStrategy;
		this.running = true;

		if(logger.isDebugEnabled())
			logger.debug("stats event collector init [node="+this.processingNodeId+", pipeline="+this.pipelineId+"]");
	}
	
	public void run() {
		while(running) {
			
			try {
//				System.out.println("Running this one. " + this.statsQueueWaitStrategy);
				final StreamingDataMessage message = this.statsQueueWaitStrategy.waitFor(this.statsQueueConsumer);
				if(message != null && message.getBody() != null) {
					// TODO export
					messageCount++;
					AggregatedComponentStatistics stats = AggregatedComponentStatistics.fromBytes(message.getBody());
					System.out.printf("Comp: %s, Count: %d, Size: %d, Min: %d, Max: %d, Avg: %.2f\n", stats.getComponentId(), stats.getNumOfMessages(), stats.getMaxSize(), stats.getMinDuration(), stats.getMaxSize(), stats.getAvgDuration());
				}
			} catch(InterruptedException e) {
				// do nothing - waiting was interrupted
			} catch(Exception e) {
				logger.error("stats processing error [node="+this.processingNodeId+", pipeline="+this.pipelineId+"]: " + e.getMessage(), e);
			}
		}
	}
	
	public void shutdown() {
		this.running = false;
	}
	
	public int getMessageCount() {
		return this.messageCount;
	}
	
}
