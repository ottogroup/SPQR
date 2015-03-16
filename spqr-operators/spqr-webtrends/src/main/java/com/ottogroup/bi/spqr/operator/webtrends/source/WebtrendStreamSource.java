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
package com.ottogroup.bi.spqr.operator.webtrends.source;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;

/**
 * Establishes a connection with a {@link WebSocket} at streams.webtrends.com and consumes
 * all messages emitted to the socket. All received content is forwarded to the micro pipeline
 * this source is attached to. 
 * @author mnxfst
 * @since Mar 16, 2015
 * TODO testing
 */
@SPQRComponent(type=MicroPipelineComponentType.SOURCE, name="webtrendsSource", version="0.0.1", description="Consumes the webtrends streams api")
public class WebtrendStreamSource implements Source {

	private static final Logger logger = Logger.getLogger(WebtrendStreamSource.class);

	////////////////////////////////////////////////////////////////////
	// required config options
	public static final String CFG_WT_AUTH_AUDIENCE = "webtrends.auth.audience";
	public static final String CFG_WT_AUTH_SCOPE = "webtrends.auth.scope";
	public static final String CFG_WT_AUTH_URL = "webtrends.auth.url";
	public static final String CFG_WT_CLIENT_ID = "webtrends.client.id";
	public static final String CFG_WT_CLIENT_SECRET = "webtrends.client.secret";
	public static final String CFG_WT_STREAM_URL = "webtrends.stream.url";
	public static final String CFG_WT_STREAM_TYPE = "webtrends.stream.type";
	public static final String CFG_WT_STREAM_QUERY = "webtrends.stream.query";
	public static final String CFG_WT_STREAM_VERSION = "webtrends.stream.version";
	public static final String CFG_WT_SCHEMA_VERSION = "webtrends.schema.version";
	//
	////////////////////////////////////////////////////////////////////

	private String id = null;
	private IncomingMessageCallback incomingMessageCallback = null;
	private String authUrl;
	private String authAudience;
	private String authScope;
	private String clientId;
	private String clientSecret;
	private String eventStreamUrl;
	private String streamType;
	private String streamQuery;
	private String streamVersion;
	private String schemaVersion;
	private long messageCount = 0;

	/** OAuth token received from webtrends */
	private String oAuthToken;
	/** required for timeout handling when connecting with api */
	private final CountDownLatch latch = new CountDownLatch(1);
	/** client used to establish and maintain the websocket connection */
	private WebSocketClient webtrendsStreamSocketClient;
	/** associated websocket session */
	private Session websocketSession;
	/** internal message queue used for buffering before data is being handed over to publisher */
	private final BlockingQueue<String> streamMessageQueue = new LinkedBlockingQueue<String>(100000);
	/** run state */
	private boolean isRunning = false;

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException, ComponentInitializationFailedException {
		
		//////////////////////////////////////////////////////////////////
		// extract settings and validate values
		this.authAudience = StringUtils.trim(properties.getProperty(CFG_WT_AUTH_AUDIENCE));
		this.authScope = StringUtils.trim(properties.getProperty(CFG_WT_AUTH_SCOPE));
		this.authUrl = StringUtils.trim(properties.getProperty(CFG_WT_AUTH_URL));
		this.clientId = StringUtils.trim(properties.getProperty(CFG_WT_CLIENT_ID));
		this.clientSecret = StringUtils.trim(properties.getProperty(CFG_WT_CLIENT_SECRET));
		this.eventStreamUrl = StringUtils.trim(properties.getProperty(CFG_WT_STREAM_URL));
		this.streamType = StringUtils.trim(properties.getProperty(CFG_WT_STREAM_TYPE));
		this.streamQuery = StringUtils.trim(properties.getProperty(CFG_WT_STREAM_QUERY));
		this.streamVersion = StringUtils.trim(properties.getProperty(CFG_WT_STREAM_VERSION));
		this.schemaVersion = StringUtils.trim(properties.getProperty(CFG_WT_SCHEMA_VERSION));

		if(StringUtils.isBlank(authAudience))
				throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_AUTH_AUDIENCE+"'");
		if(StringUtils.isBlank(authScope))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_AUTH_SCOPE+"'");
		if(StringUtils.isBlank(authUrl))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_AUTH_URL+"'");
		if(StringUtils.isBlank(clientId))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_CLIENT_ID+"'");
		if(StringUtils.isBlank(clientSecret))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_CLIENT_SECRET+"'");
		if(StringUtils.isBlank(eventStreamUrl))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_STREAM_URL+"'");
		if(StringUtils.isBlank(streamType))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_STREAM_TYPE+"'");
		if(StringUtils.isBlank(streamQuery))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_STREAM_QUERY+"'");
		if(StringUtils.isBlank(streamVersion))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_STREAM_VERSION+"'");
		if(StringUtils.isBlank(schemaVersion))
			throw new RequiredInputMissingException("Missing required input for parameter '"+CFG_WT_SCHEMA_VERSION+"'");
		//
		//////////////////////////////////////////////////////////////////

		// authenticate with the webtrends service
		WebtrendsTokenRequest tokenRequest = new WebtrendsTokenRequest(this.authUrl, this.authAudience, this.authScope, this.clientId, this.clientSecret);
		try {
			this.oAuthToken = tokenRequest.execute();
		} catch(Exception e) {
			throw new RuntimeException("Failed to request token from '"+authUrl+"'. Error: " + e.getMessage());
		}
		
		// initialize the webtrends stream socket client and connect the listener
		this.webtrendsStreamSocketClient = new WebSocketClient();
		try {
			this.webtrendsStreamSocketClient.start();
			ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();			
			this.webtrendsStreamSocketClient.connect(this, new URI(this.eventStreamUrl), upgradeRequest);
			await(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			throw new RuntimeException("Unable to connect to web socket: " + e.getMessage(), e);
		}
		
		this.isRunning = true;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		try {
			this.websocketSession.close();
		} catch(Exception e) {
			logger.error("Failed to close websocket session at source [id="+id+"]: " + e.getMessage());
		}
		try {
			this.webtrendsStreamSocketClient.stop();
		} catch(Exception e) {
			logger.error("Failed to close websocket client at source [id="+id+"]: " + e.getMessage());
		}
		return true;	
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(logger.isDebugEnabled())
			logger.debug("webtrends stream consumer initialized [id="+id+"]");
		
		
		// keep on consuming until either the consumer or the client is interrupted  
		while(this.isRunning && this.websocketSession.isOpen()) {			
			try {
				String msg = streamMessageQueue.poll(100, TimeUnit.MILLISECONDS);
				if(msg != null) {
					this.incomingMessageCallback.onMessage(new StreamingDataMessage(msg, System.currentTimeMillis()));
					this.messageCount++;
				}
				
			} catch (InterruptedException e) {
				logger.error("Failed to read data from websocket. Error: " + e.getMessage());
			}
		}
		
		shutdown();

		logger.info("webtrends stream consumer received " + this.messageCount + " messages");
	}

	/**
	 * Executed after establishing web socket connection with streams api
	 * @param session
	 */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.websocketSession = session;
		sendUpdate(this.websocketSession, this.oAuthToken, this.streamType, this.streamQuery, this.streamVersion, this.schemaVersion);
	}
	
	/**
	 * Executed by web socket implementation when receiving a message from the
	 * streams api. The message will be directly handed over to the configured 
	 * {@link ActorRef message receiver}  
	 * @param message
	 */
	@OnWebSocketMessage
	public void onMessage(String message) {
		try {
			this.streamMessageQueue.offer(message, 1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Failed to offer element to internal queue. Ignoring event. Error: " + e.getMessage());
		}
	}

	/**
	 * Executed when closing the web socket connection
	 * @param statusCode
	 * @param reason
	 */
	@OnWebSocketClose		    
	public void onClose(int statusCode, String reason) {	
		//
	}

	/**
	 * Sends an update towards the webtrends stream api using the contents of the 
	 * provided {@link WebtrendsStreamListenerQueryUpdateMessage message}
	 * @param msg
	 */
	protected void sendUpdate(final Session session, final String oAuthToken, final String streamType, final String streamQuery, final String streamVersion, final String schemaVersion) {
		
		// build SAPI query object
		final StringBuilder sb = new StringBuilder();
		sb.append("{\"access_token\":\"");
		sb.append(oAuthToken);
		sb.append("\",\"command\":\"stream\"");
		sb.append(",\"stream_type\":\"");
		sb.append(streamType);
		sb.append("\",\"query\":\"");
		sb.append(streamQuery);
		sb.append("\",\"api_version\":\"");
		sb.append(streamVersion);
		sb.append("\",\"schema_version\":\"");
		sb.append(schemaVersion);
		sb.append("\"}");

		try {
			session.getRemote().sendString(sb.toString());
		} catch(IOException e) {
		   	throw new RuntimeException("Unable to open stream", e);
		}
	}
	
	/**
	 * Timeout handler
	 * @param duration
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean await(int duration, TimeUnit unit) throws InterruptedException {
		return latch.await(duration, unit);
	}
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getType()
	 */
	public MicroPipelineComponentType getType() {
		return MicroPipelineComponentType.SOURCE;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.source.Source#setIncomingMessageCallback(com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback)
	 */
	public void setIncomingMessageCallback(
			IncomingMessageCallback incomingMessageCallback) {
		this.incomingMessageCallback = incomingMessageCallback;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}

}
