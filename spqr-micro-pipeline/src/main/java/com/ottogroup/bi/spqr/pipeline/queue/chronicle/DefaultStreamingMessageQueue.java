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
package com.ottogroup.bi.spqr.pipeline.queue.chronicle;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.tools.ChronicleTools;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueConsumer;
import com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueueProducer;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueBlockingWaitStrategy;
import com.ottogroup.bi.spqr.pipeline.queue.strategy.StreamingMessageQueueWaitStrategy;

/**
 * Implements a {@link StreamingMessageQueue} based on {@link Chronicle}
 * @author mnxfst
 * @since Mar 5, 2015
 */
public class DefaultStreamingMessageQueue implements StreamingMessageQueue {


	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(DefaultStreamingMessageQueue.class);
	
	public static final String CFG_CHRONICLE_QUEUE_PATH = "queue.chronicle.path";
	public static final String CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT = "queue.chronicle.deleteOnExist";
	public static final String CFG_QUEUE_MESSAGE_WAIT_STRATEGY = "queue.message.waitStrategy";

	/** unique queue identifier */
	private String id = null;
	/** base path to use when creating/accessing the referenced chronicle files */
	private String basePath = null;
	/** delete the chronicle files on start and shutdown */
	private boolean deleteOnExit = true;
	/** chronicle instance - required for creating and accessing appender & tailer */
	private Chronicle chronicle = null;	
	/** provides read access to chronicle */
	private DefaultStreamingMessageQueueConsumer queueConsumer = null;
	/** provides write access to chronicle */
	private DefaultStreamingMessageQueueProducer queueProducer = null;
	/** wait strategy applied on this queue */
	private StreamingMessageQueueWaitStrategy queueWaitStrategy = null;

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException {

		////////////////////////////////////////////////////////////////////////////////
		// extract and validate input
		if(properties == null)
			throw new RequiredInputMissingException("Missing required properties");		
		
		if(StringUtils.isBlank(this.id))
			throw new RequiredInputMissingException("Missing required queue identifier");

		if(StringUtils.equalsIgnoreCase(StringUtils.trim(properties.getProperty(CFG_CHRONICLE_QUEUE_DELETE_ON_EXIT)), "false"))
			this.deleteOnExit = false;

		this.basePath = StringUtils.lowerCase(StringUtils.trim(properties.getProperty(CFG_CHRONICLE_QUEUE_PATH)));
		if(StringUtils.isBlank(this.basePath))
			this.basePath = System.getProperty("java.io.tmpdir");

		String pathToChronicle = this.basePath;
		if(!StringUtils.endsWith(pathToChronicle, File.separator))
			pathToChronicle = pathToChronicle + File.separator;
		pathToChronicle = pathToChronicle + id;
		
		String waitStrategyClass = StringUtils.trim(properties.getProperty(CFG_QUEUE_MESSAGE_WAIT_STRATEGY));
		if(StringUtils.isBlank(waitStrategyClass))
			waitStrategyClass = StreamingMessageQueueBlockingWaitStrategy.class.getName();
		this.queueWaitStrategy = getWaitStrategy(waitStrategyClass);
		
		//
		////////////////////////////////////////////////////////////////////////////////
		
		// clears the queue if requested 
		if(this.deleteOnExit)
			ChronicleTools.deleteOnExit(pathToChronicle);
		
        try {
			this.chronicle = ChronicleQueueBuilder.indexed(pathToChronicle).build();
			this.queueConsumer = new DefaultStreamingMessageQueueConsumer(this.chronicle.createTailer(), this.queueWaitStrategy);
			this.queueProducer = new DefaultStreamingMessageQueueProducer(this.chronicle.createAppender(), this.queueWaitStrategy);
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize chronicle at '"+pathToChronicle+"'. Error: " + e.getMessage());
		}
        
        logger.info("queue[type=chronicle, id="+this.id+", deleteOnExist="+this.deleteOnExit+", path="+pathToChronicle+"']");       		
	}

	/**
	 * Return an instance of the referenced {@link StreamingMessageQueueWaitStrategy wait strategy class}
	 * @param waitStrategyClass
	 * @return
	 */
	protected StreamingMessageQueueWaitStrategy getWaitStrategy(final String waitStrategyClass) {
		try {
			Class<?> clazz = Class.forName(waitStrategyClass);
			return (StreamingMessageQueueWaitStrategy)clazz.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("Failed to instantiate wait strategy class '"+waitStrategyClass+"'. Error: " + e.getMessage());
		}
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#shutdown()
	 */
	public boolean shutdown() {
		try {
			this.chronicle.close();
			return true;
		} catch (IOException e) {
			logger.error("Failed to close underlying chronicle at " + basePath + "/"+id+". Error: " + e.getMessage());;
		}
		return false;
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#insert(com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage)
	 */
	public boolean insert(StreamingDataMessage message) {
		return queueProducer.insert(message); // TODO access appender directly
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#next()
	 */
	public StreamingDataMessage next() {
		return queueConsumer.next(); // TODO access tailer directly
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getProducer()
	 */
	public StreamingMessageQueueProducer getProducer() {		
		return this.queueProducer;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getConsumer()
	 */
	public StreamingMessageQueueConsumer getConsumer() {
		return this.queueConsumer;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.queue.StreamingMessageQueue#getId()
	 */
	public String getId() {
		return this.id;
	}


}
