/**
 * Copyright 2014 Otto (GmbH & Co KG)
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

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.source.IncomingMessageCallback;
import com.twitter.hbc.httpclient.BasicClient;

/**
 * Test case for {@link TwitterStreamSource}
 * @author mnxfst
 * @since Dec 1, 2014
 *
 */
public class TwitterStreamSourceTest {
	
	private static ExecutorService executor;
	
	@BeforeClass
	public static void initialize() throws Exception {
		executor = Executors.newCachedThreadPool();
	}
	
	@AfterClass
	public static void shutdown() throws Exception {
		if(executor != null)
			executor.shutdownNow();
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * null as input
	 */
	@Test
	public void testInitialize_withNullInput() throws Exception {
		try {
			new TwitterStreamSource().initialize(null);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * an empty properties instance
	 */
	@Test
	public void testInitialize_withEmptyProperties() throws Exception {
		try {
			new TwitterStreamSource().initialize(new Properties());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties missing the component id
	 */
	@Test
	public void testInitialize_withEmptyComponentId() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "ckey");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "csecret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "tkey");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "tsecret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		
		try {
			new TwitterStreamSource().initialize(props);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties missing the consumer key
	 */
	@Test
	public void testInitialize_withEmptyConsumerKey() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "csecret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "tkey");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "tsecret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		
		try {
			new TwitterStreamSource().initialize(props);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties missing the consumer secret
	 */
	@Test
	public void testInitialize_withEmptyConsumerSecret() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "ckey");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "tkey");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "tsecret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		
		try {
			new TwitterStreamSource().initialize(props);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties missing the token key
	 */
	@Test
	public void testInitialize_withEmptyTokenKey() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "ckey");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "csecret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "tsecret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		
		try {
			new TwitterStreamSource().initialize(props);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties missing the token secret
	 */
	@Test
	public void testInitialize_withEmptyTokenSecret() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "ckey");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "csecret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "tkey");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		
		try {
			new TwitterStreamSource().initialize(props);
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties showing all required input
	 */
	@Test
	public void testInitialize_withRequiredInput() throws Exception {
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "ckey");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "csecret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "tkey");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "tsecret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "");
		TwitterStreamSource source = new TwitterStreamSource();
		source.setId("test-id");
		source.setTwitterClient(Mockito.mock(BasicClient.class));
		source.initialize(props);
	}

	/**
	 * Test case for {@link TwitterStreamSource#initialize(java.util.Properties)} being provided 
	 * properties showing all input
	 */
	@Test
	public void testInitialize_withAllInput() throws Exception {
		
		String componentId = "testInitialize_withAllInput";
		String consumerKey = "testInitialize_withAllInput-consumer-key";
		String consumerSecret = "testInitialize_withAllInput-consumer-secret";
		String profiles = "123, 456, 789";
		String tokenKey = "testInitialize_withAllInput-token-key";
		String tokenSecret = "testInitialize_withAllInput-token-secret";
		String languages = "de, fr, en";
		String searchTerms = "soccer, fifa, uefa";
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, consumerKey);
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, consumerSecret);
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, profiles);
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, tokenKey);
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, tokenSecret);
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, languages);
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, searchTerms);
		TwitterStreamSource consumer = new TwitterStreamSource();
		consumer.setId(componentId);
		consumer.setTwitterClient(Mockito.mock(BasicClient.class));
		consumer.initialize(props);
		
		Assert.assertEquals("Values must be equal", componentId, consumer.getId());
		Assert.assertEquals("Values must be equal", consumerKey, consumer.getConsumerKey());
		Assert.assertEquals("Values must be equal", consumerSecret, consumer.getConsumerSecret());
		Assert.assertEquals("Values must be equal", tokenKey, consumer.getTokenKey());
		Assert.assertEquals("Values must be equal", tokenSecret, consumer.getTokenSecret());
		Assert.assertEquals("Values must be equal", 3, consumer.getProfiles().size());
		Assert.assertTrue("Must be inclued", consumer.getProfiles().contains(123l));
		Assert.assertTrue("Must be inclued", consumer.getProfiles().contains(456l));
		Assert.assertTrue("Must be inclued", consumer.getProfiles().contains(789l));
		Assert.assertEquals("Values must be equal", 3, consumer.getLanguages().size());
		Assert.assertTrue("Must be inclued", consumer.getLanguages().contains("de"));
		Assert.assertTrue("Must be inclued", consumer.getLanguages().contains("en"));
		Assert.assertTrue("Must be inclued", consumer.getLanguages().contains("fr"));
		Assert.assertEquals("Values must be equal", 3, consumer.getSearchTerms().size());
		Assert.assertTrue("Must be inclued", consumer.getSearchTerms().contains("uefa"));
		Assert.assertTrue("Must be inclued", consumer.getSearchTerms().contains("fifa"));
		Assert.assertTrue("Must be inclued", consumer.getSearchTerms().contains("soccer"));
		
		Assert.assertTrue("Must be true", consumer.isRunning());
	}
	
	/**
	 * Test case for {@link TwitterStreamSource#run()} with a
	 * twitter client showing true when calling isDone 
	 */
	@Test
	public void testRun_withTwitterClientWhichIsDone() throws Exception {
		
		BasicClient twitterClientMock = Mockito.mock(BasicClient.class);
		Mockito.when(twitterClientMock.isDone()).thenReturn(true);
		
		Properties props = new Properties();
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "consumer-key");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "consumer-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "1");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "token-key");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "token-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "en");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "soccer");
		TwitterStreamSource consumer = new TwitterStreamSource();
		consumer.setId("test-id");
		consumer.initialize(props);		
		consumer.setTwitterClient(twitterClientMock);
		consumer.getStreamMessageQueue().offer("test-message");
		Assert.assertFalse("Value must be true", consumer.getStreamMessageQueue().isEmpty());
		executor.submit(consumer);
		Thread.sleep(100);
		consumer.run();
		Assert.assertFalse("Value must be true", consumer.getStreamMessageQueue().isEmpty());
		Assert.assertEquals("Values must be equal", 0, consumer.getMessageCount());
	}
	
	/**
	 * Test case for {@link TwitterStreamSource#run()} where isRunning shows false
	 */
	@Test
	public void testRun_withIsRunningSetToFalse() throws Exception {
		
		BasicClient twitterClientMock = Mockito.mock(BasicClient.class);
		IncomingMessageCallback callback = Mockito.mock(IncomingMessageCallback.class);
		Mockito.when(twitterClientMock.isDone()).thenReturn(false);
		
		Properties props = new Properties();
		
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "consumer-key");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "consumer-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "1");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "token-key");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "token-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "en");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "soccer");
		TwitterStreamSource consumer = new TwitterStreamSource();
		consumer.setId("test-id");
		consumer.setTwitterClient(twitterClientMock);
		consumer.initialize(props);		
		consumer.setIncomingMessageCallback(callback);
		consumer.setRunning(false);
		String msgContent = "{\"content\":\"test-message\", \"timestamp_ms\":\""+System.currentTimeMillis()+"\"}";
		consumer.getStreamMessageQueue().offer(msgContent);
		Assert.assertFalse("Value must be false", consumer.getStreamMessageQueue().isEmpty());
		executor.submit(consumer);
		Thread.sleep(100);
		consumer.shutdown();
		Assert.assertEquals("Must show one element as none were consumed", 1, consumer.getStreamMessageQueue().size());
		Assert.assertEquals("Values must be equal", 0, consumer.getMessageCount());
	}

	/**
	 * Test case for {@link TwitterStreamSource#run()} with at least one iteration
	 */
	@Test
	public void testRun_withAtLeastOneIteration() throws Exception {
		
		IncomingMessageCallback callback = Mockito.mock(IncomingMessageCallback.class);
		BasicClient twitterClientMock = Mockito.mock(BasicClient.class);
		Mockito.when(twitterClientMock.isDone()).thenReturn(false);
		
		Properties props = new Properties();
		
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "consumer-key");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "consumer-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "1");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "token-key");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "token-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "en");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "soccer");
		TwitterStreamSource consumer = new TwitterStreamSource();
		consumer.setId("test-id");
		consumer.setTwitterClient(twitterClientMock);
		consumer.setIncomingMessageCallback(callback);
		consumer.initialize(props);		
		consumer.getStreamMessageQueue().offer("{\"content\":\"test-message\", \"timestamp_ms\":\""+System.currentTimeMillis()+"\"}");
		Assert.assertFalse("Value must be false", consumer.getStreamMessageQueue().isEmpty());
		executor.submit(consumer);
		Thread.sleep(100);
		consumer.shutdown();
		Assert.assertTrue("Value must be true", consumer.getStreamMessageQueue().isEmpty());
		Assert.assertEquals("Values must be equal", 1, consumer.getMessageCount());
	}
	
	/**
	 * Test case for {@link TwitterStreamSource#run()} with one valid and one invalid message. Must show no errors
	 * as the source does not care about the message format
	 */
	@Test
	public void testRun_withOneValidOneInvalidMessage() throws Exception {
		
		IncomingMessageCallback callback = Mockito.mock(IncomingMessageCallback.class);
		BasicClient twitterClientMock = Mockito.mock(BasicClient.class);
		Mockito.when(twitterClientMock.isDone()).thenReturn(false);
		
		Properties props = new Properties();
		
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_KEY, "consumer-key");
		props.put(TwitterStreamSource.CFG_TWITTER_CONSUMER_SECRET, "consumer-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_PROFILES, "1");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_KEY, "token-key");
		props.put(TwitterStreamSource.CFG_TWITTER_TOKEN_SECRET, "token-secret");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_LANGUAGES, "en");
		props.put(TwitterStreamSource.CFG_TWITTER_TWEET_SEARCH_TERMS, "soccer");
		TwitterStreamSource consumer = new TwitterStreamSource();
		consumer.setTwitterClient(twitterClientMock);
		consumer.setId("testRun_withOneValidOneInvalidMessage");
		consumer.initialize(props);		
		consumer.setIncomingMessageCallback(callback);
		consumer.getStreamMessageQueue().offer("{\"content\":\"test-message\", \"timestamp_ms\":\""+System.currentTimeMillis()+"\"}");
		consumer.getStreamMessageQueue().offer("noJsonContent");
		Assert.assertEquals("Values must be equal", 2, consumer.getStreamMessageQueue().size());
		executor.submit(consumer);
		Thread.sleep(100);
		consumer.shutdown();
		Assert.assertTrue("Value must be true", consumer.getStreamMessageQueue().isEmpty());
		Assert.assertEquals("Values must be equal", 2, consumer.getMessageCount());
	}
	
}
