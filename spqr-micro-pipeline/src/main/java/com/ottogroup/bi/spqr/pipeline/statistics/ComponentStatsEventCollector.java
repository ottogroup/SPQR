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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.pipeline.MicroPipeline;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Attached to a {@link MicroPipeline} instance this one collects {@link ComponentMessageProcessingEvent} entities,
 * aggregates their contents into {@link AggregatedComponentStatistics}, generates a {@link MicroPipelineStatistics} instance
 * which gets emitted to a connected destination, eg. kafka topic
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
	/** timer to execute stats aggregation in pre-defined period */
	private final Timer statsAggregationTimer;
	/** internal buffer size */
	private final int internalBufferSize;
	/** cache to hold all incoming message processing event */
	private final ConcurrentLinkedQueue<ComponentMessageStatsEvent> processingEvents;
	/** indicates whether the collector is running or not */
	private boolean running = false;
	/** counter for tracking number of received messages */
	private final AtomicInteger messageCounter;
	/** last aggregation */
	private long lastAggregationRun;
	
	public ComponentStatsEventCollector(final String processingNodeId, final String pipelineId, final int aggregateDuration, 
			final StreamingMessageQueueConsumer statsQueueConsumer, final StreamingMessageQueueWaitStrategy statsQueueWaitStrategy, final int internalBufferSize) {
		
		this.processingNodeId = processingNodeId;
		this.pipelineId = pipelineId;
		this.statsQueueConsumer = statsQueueConsumer;
		this.statsQueueWaitStrategy = statsQueueWaitStrategy;
		this.internalBufferSize = internalBufferSize;
		this.processingEvents = new ConcurrentLinkedQueue<>();
		this.running = true;
		this.messageCounter = new AtomicInteger(internalBufferSize);
		this.lastAggregationRun = System.currentTimeMillis();

		this.statsAggregationTimer = new Timer(pipelineId+"-stats-collector", true);
		this.statsAggregationTimer.schedule(new TimerTask() {
			public void run() {
				aggregate();
			}
		}, aggregateDuration, aggregateDuration);
		
		if(logger.isDebugEnabled())
			logger.debug("stats event collector init [node="+this.processingNodeId+", pipeline="+this.pipelineId+", aggrDur="+aggregateDuration+", bufSize="+internalBufferSize+"]");
	}
	
	/**
	 * Aggregates {@link ComponentMessageStatsEvent} received by the collector and forwards the results
	 * to attached sink
	 */
	public void aggregate() {

		// if there do not exist any events, reset the counter (just to be sure) and return flow control to caller
		if(this.processingEvents.isEmpty()) {
			this.messageCounter.getAndSet(0);
			return;
		}
			
		// retrieve all events, clear the queue and reset message counter 
		ComponentMessageStatsEvent[] events = (ComponentMessageStatsEvent[])this.processingEvents.toArray();
		this.processingEvents.clear(); // TODO check for deadlocks as clearing means polling til last element is reached
		this.messageCounter.getAndSet(0);

		Map<String, AggregatedComponentStatistics> components = new HashMap<String, AggregatedComponentStatistics>();
		for(final ComponentMessageStatsEvent event : events) {
			AggregatedComponentStatistics cstats = components.get(event.getComponentId());
			if(cstats == null)
				cstats = new AggregatedComponentStatistics(event.getComponentId(), 0, this.lastAggregationRun, System.currentTimeMillis(), Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 0);
			cstats.setAvgDuration(0);
			cstats.setAvgSize(0);

			if(event.isError())
				cstats.setErrors(cstats.getErrors() + 1);			
			if(cstats.getMinDuration() > event.getDuration())
				cstats.setMinDuration(event.getDuration());
			if(cstats.getMaxDuration() < event.getDuration())
				cstats.setMaxDuration(event.getDuration());
			if(cstats.getMinSize() > event.getSize())
				cstats.setMinSize(event.getSize());
			if(cstats.getMaxSize() < event.getSize())
				cstats.setMaxSize(event.getSize());
			cstats.setNumOfMessages(cstats.getNumOfMessages() + 1);
		}
		
		this.lastAggregationRun = System.currentTimeMillis();
		
		for(String cid : components.keySet() ) {
			AggregatedComponentStatistics cstats = components.get(cid);
			logger.info("[cid="+cstats.getComponentId()+", start="+cstats.getStartTime()+", end="+cstats.getEndTime()+", msgs="+cstats.getNumOfMessages()+", errs="+cstats.getErrors()+
					", minSize="+cstats.getMinSize()+", maxSize="+cstats.getMaxSize()+ ", avgSize="+cstats.getAvgSize() + 
					", minDur="+cstats.getMinDuration() +", maxDur="+cstats.getMaxDuration()+", avgDur="+cstats.getAvgDuration()+"]");
		}
	}
	
	public void run() {
		while(running) {
			try {
				final StreamingDataMessage message = this.statsQueueWaitStrategy.waitFor(this.statsQueueConsumer);
				if(this.messageCounter.get() < this.internalBufferSize) {
					if(message != null && message.getBody() != null) {
						this.processingEvents.add(ComponentMessageStatsEvent.fromBytes(message.getBody()));
						this.messageCounter.incrementAndGet();
					}
				} else {
					logger.error("stats queue full [node="+this.processingNodeId+", pipeline="+this.pipelineId+", bufSize="+this.internalBufferSize+"]");
				}
			} catch(InterruptedException e) {
				// do nothing - waiting was interrupted
			} catch(Exception e) {
				logger.error("stats processing error [node="+this.processingNodeId+", pipeline="+this.pipelineId+", bufSize="+this.internalBufferSize+"]: " + e.getMessage(), e);
			}
		}
	}
	
	
	
}
