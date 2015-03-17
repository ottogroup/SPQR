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
package com.ottogroup.bi.spqr.operator.twitter.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.pipeline.message.StreamingDataMessage;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

/**
 * Consumes the Twitter stream API ({@linkplain http://https://dev.twitter.com/streaming/overview}) and provides
 * all received tweets as {@link StreamingDataMessage} instances to underlying micro pipeline
 * @author mnxfst
 * @since Mar 17, 2015
 * TODO provide wait strategy which does not consume all available cpu resources
 */
@SPQRComponent(type=MicroPipelineComponentType.SOURCE, name="twitterSource", version="0.0.1", description="Consumes Twitter streaming API")
public class TwitterStreamSource implements Source {

	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(TwitterStreamSource.class);
	
	///////////////////////////////////////////////////////////////////////////////////
	// configuration options 
	public static final String CFG_TWITTER_CONSUMER_KEY = "twitter.consumer.key";
	public static final String CFG_TWITTER_CONSUMER_SECRET = "twitter.consumer.secret";
	public static final String CFG_TWITTER_TOKEN_KEY = "twitter.token.key";
	public static final String CFG_TWITTER_TOKEN_SECRET = "twitter.token.secret";
	public static final String CFG_TWITTER_TWEET_SEARCH_TERMS = "twitter.tweet.terms";
	public static final String CFG_TWITTER_TWEET_LANGUAGES = "twitter.tweet.languages";
	public static final String CFG_TWITTER_PROFILES = "twitter.profiles";
	//
	///////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////
	// required stream settings
	/** component identifer */
	private String id = null;
	/** receives all incoming messages */
	private IncomingMessageCallback incomingMessageCallback = null;
	/** client handling communication with stream.twitter.com */
	private BasicClient twitterClient = null;
	/** consumer key issued by twitter.com */
	private String consumerKey = null;
	/** consumer secrect issued by twitter.com */
	private String consumerSecret = null;
	/** token key issued by twitter.com */
	private String tokenKey = null;
	/** token secret issued by twitter.com */
	private String tokenSecret = null;
	/** terms to search for in twitter status stream - applied across all status updates */ 
	private final List<String> searchTerms = new ArrayList<>();
	/** twitter profiles to retrieve data from - applied in addition to search terms, data will be merged */ 
	private final List<Long> profiles = new ArrayList<>();
	/** languages to filter twitter stream for */
	private final List<String> languages = new ArrayList<>();
	/** locations to filter twitter stream for */
	private final List<Location> locations = new ArrayList<>();
	/** internal message queue used for buffering before data is being handed over to publisher */
	private final BlockingQueue<String> streamMessageQueue = new LinkedBlockingQueue<String>(100000);
	private final BlockingQueue<Event> eventMessageQueue = new LinkedBlockingQueue<Event>(100000);
	/** indicates whether the source is still running */
	private boolean running = false;
	/** counts the number of messages processed so far */
	private long messageCount = 0;
	//
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#initialize(java.util.Properties)
	 */
	public void initialize(Properties properties) throws RequiredInputMissingException,	ComponentInitializationFailedException {
		if(properties == null || properties.isEmpty())
			throw new RequiredInputMissingException("Missing required configuration");
		
		//////////////////////////////////////////////////////////
		// extract required configurational data 
		this.consumerKey = properties.getProperty(CFG_TWITTER_CONSUMER_KEY);
		this.consumerSecret = properties.getProperty(CFG_TWITTER_CONSUMER_SECRET);
		this.tokenKey = properties.getProperty(CFG_TWITTER_TOKEN_KEY);
		this.tokenSecret = properties.getProperty(CFG_TWITTER_TOKEN_SECRET);

		String inSearchTerms = properties.getProperty(CFG_TWITTER_TWEET_SEARCH_TERMS);
		String[] splittedSearchTerms = (inSearchTerms != null ? inSearchTerms.split(",") : null);
		if(splittedSearchTerms != null) {
			for(String sst : splittedSearchTerms) {
				this.searchTerms.add(StringUtils.trim(sst));
			}
		}
		
		String inLanguages = properties.getProperty(CFG_TWITTER_TWEET_LANGUAGES);
		String[] splittedLanguages = (inLanguages != null ? inLanguages.split(",") : null);
		if(splittedLanguages != null) {
			for(String s : splittedLanguages) {
				this.languages.add(StringUtils.trim(s));
			}
		}
		
		String inProfiles = properties.getProperty(CFG_TWITTER_PROFILES);
		String[] splittedProfiles = (inProfiles != null ? inProfiles.split(",") : null);
		if(splittedProfiles != null) {
			for(String sp : splittedProfiles) {
				if(StringUtils.isNotBlank(sp)) {
					try {
						this.profiles.add(Long.parseLong(sp.trim()));
					} catch(Exception e) {
						logger.error("Failed to parse profile identifier from input '"+sp+"'");
					}
				}
			}
		}
		//
		//////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////////////
		// validate provided input before attempting to establish connection with stream.twitter.com
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required component identifier");
		if(StringUtils.isBlank(this.consumerKey))
			throw new RequiredInputMissingException("Missing required consumer key to establish connection with stream.twitter.com");
		if(StringUtils.isBlank(this.consumerSecret))
			throw new RequiredInputMissingException("Missing required consumer secrect to establish connection with stream.twitter.com");
		if(StringUtils.isBlank(this.tokenKey))
			throw new RequiredInputMissingException("Missing required token key to establish connection with stream.twitter.com");
		if(StringUtils.isBlank(this.tokenSecret))
			throw new RequiredInputMissingException("Missing required token secret to establish connection with stream.twitter.com");
		
		boolean isFilterTermsEmpty = (this.searchTerms == null || this.searchTerms.isEmpty());
		boolean isLanguagesEmpty = (this.languages == null || this.languages.isEmpty());
		boolean isUserAccountEmpty = (this.profiles == null || this.profiles.isEmpty());
		boolean isLocationsEmpty = (this.locations == null || this.locations.isEmpty());
		
		if(isFilterTermsEmpty && isLanguagesEmpty && isUserAccountEmpty && isLocationsEmpty) 
			throw new RequiredInputMissingException("Mishandle sing information what to filter twitter stream for: terms, languages, user accounts or locations");
		//
		////////////////////////////////////////////////////////////////////////////////////////////

		
		//////////////////////////////////////////////////////////
		// establish connection with stream.twitter.com
		Authentication auth = new OAuth1(this.consumerKey, this.consumerSecret, this.tokenKey, this.tokenSecret);
		StatusesFilterEndpoint filterEndpoint = new StatusesFilterEndpoint();
		if(!isFilterTermsEmpty)
			filterEndpoint.trackTerms(searchTerms);
		if(!isLanguagesEmpty)
			filterEndpoint.languages(languages);
		if(!isUserAccountEmpty)
			filterEndpoint.followings(profiles);
		if(!isLocationsEmpty)
			filterEndpoint.locations(locations);

		if(this.twitterClient == null) {
			this.twitterClient = new ClientBuilder().name(id)
					.hosts(Constants.STREAM_HOST).endpoint(filterEndpoint).authentication(auth)
					.processor(new StringDelimitedProcessor(streamMessageQueue)).eventMessageQueue(eventMessageQueue).build();
					this.twitterClient.connect();
		}
		//
		//////////////////////////////////////////////////////////

		this.running = true;

		if(logger.isDebugEnabled())
			logger.debug("twitter stream consumer initialized [id="+id+"]");

	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#shutdown()
	 */
	public boolean shutdown() {
		this.running = false;
		return true;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// keep on consuming until either the consumer or the client is interrupted  
		while(this.running && !this.twitterClient.isDone()) {
			try {
				String msg = streamMessageQueue.poll(100, TimeUnit.MILLISECONDS);
				if(msg != null) {
					logger.info("message: " + msg);
					this.incomingMessageCallback.onMessage(new StreamingDataMessage(msg, System.currentTimeMillis()));
					this.messageCount++;
				}
				
				Event e = eventMessageQueue.poll(100, TimeUnit.MILLISECONDS);
				if(e != null) {
					logger.info(e.getEventType() + ": " + e.getMessage());
				}
					
			} catch (InterruptedException e) {
			}
				
		}

		// stop the twitter client in case the consumer has been interrupted by external signal
		if(this.twitterClient != null && !this.twitterClient.isDone())
			this.twitterClient.stop();
		
		if(logger.isDebugEnabled())
			logger.debug("twitter stream consumer received " + this.messageCount + " messages");
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
	public void setIncomingMessageCallback(IncomingMessageCallback incomingMessageCallback) {
		this.incomingMessageCallback = incomingMessageCallback;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent#getId()
	 */
	public String getId() {
		return this.id;
	}


	///////////////////////////////////////////////////////////////////////
	// REQUIRED FOR TESTING ONLY

	public long getMessageCount() {
		return messageCount;
	}

	protected String getConsumerKey() {
		return consumerKey;
	}

	protected String getConsumerSecret() {
		return consumerSecret;
	}

	protected String getTokenKey() {
		return tokenKey;
	}

	protected String getTokenSecret() {
		return tokenSecret;
	}

	protected List<Long> getProfiles() {
		return profiles;
	}

	protected List<String> getLanguages() {
		return languages;
	}

	protected List<Location> getLocations() {
		return locations;
	}

	protected boolean isRunning() {
		return running;
	}

	protected List<String> getSearchTerms() {
		return searchTerms;
	}

	protected BlockingQueue<String> getStreamMessageQueue() {
		return streamMessageQueue;
	}

	protected void setTwitterClient(BasicClient twitterClient) {
		this.twitterClient = twitterClient;
	}

	protected void setRunning(boolean isRunning) {
		this.running = isRunning;
	}

	//
	///////////////////////////////////////////////////////////////////////

}
