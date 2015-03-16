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

package com.ottogroup.bi.spqr.operator.webtrends.source;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Describes the request to be initiated with {@linkplain http://streams.webtrends.com webtrends stream api} in order
 * to fetch a token required for further communication. See {@linkplain https://github.com/Webtrends/Streams-OAuth-Libraries} for 
 * more detailed information
 * @author mnxfst
 * @since 28.02.2014
 */
public class WebtrendsTokenRequest {

	protected final String REQUEST_PARAM_CLIENT_ID = "client_id";
	protected final String REQUEST_PARAM_CLIENT_ASSERTION = "client_assertion";
	
	public String audience = "auth.webtrends.com";
    public String scope = "sapi.webtrends.com";
    public String authUrl = "https://sauth.webtrends.com/v1/token";
    public String expiration;
  
	private String clientId;
	private String clientSecret;
  
	public WebtrendsTokenRequest(String authUrl, String audience, String scope, String clientId, String clientSecret) {
		this.authUrl = authUrl;
		this.audience = audience;
		this.scope = scope;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		expiration = String.valueOf((new Date()).getTime() + (30 * 60));
	}
  
	public String execute() throws Exception {

		final String assertion = buildAssertion();
	    final Map<String, String> requestParams = new HashMap<String, String>() {
			private static final long serialVersionUID = 1919397363313726934L;

			{
	    		put(REQUEST_PARAM_CLIENT_ID, clientId);
	    	  	put(REQUEST_PARAM_CLIENT_ASSERTION, java.net.URLEncoder.encode(assertion, "UTF-8"));
	    	}
	    };
	    
	    
	    final String result = httpPost(requestParams);
      
	    ObjectMapper mapper = new ObjectMapper();
	    JsonFactory factory = mapper.getFactory();
	    JsonParser jp = factory.createParser(result);
	    JsonNode obj = mapper.readTree(jp);
      
	    JsonNode error = obj.findValue("Error");
	    if (error != null)
        	throw new Exception(obj.findValue("Description").asText());
      
	    return obj.findValue("access_token").asText();
	}
  
	private String buildAssertion() {
      	// Json representation of the header
		final String header = "{\"typ\":\"JWT\", \"alg\":\"HS256\"}";
		// Token will expire in one day
      
		// Json representation of the claims set
		final String claims = "{\"iss\":\"" + clientId + "\",\"prn\":\"" + clientId + "\",\"aud\":\"" + audience + "\",\"exp\":" + expiration + ",\"scope\":\"" + scope + "\" }";
		// Base64Encode the component parts
		final String encodedHeader = base64Encode(header);
		final String encodedClaims = base64Encode(claims);
		// The content to be hashed
		final String message = String.format("%s.%s", encodedHeader, encodedClaims);

		final String signature = getHMAC256(message, clientSecret);

		return String.format("%s.%s.%s", encodedHeader, encodedClaims, signature);
	}
  
	private static String base64Encode(final String input) {
    	return new String(Base64.encodeBase64(input.getBytes()));
	}

	private static String base64Encode(final byte[] input) {
    	return new String(Base64.encodeBase64(input));
	}
  
	private String getHMAC256(final String input, final String secret) {
    	String temp = null;
    	final SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    	try {
        	final Mac mac = Mac.getInstance("HmacSHA256");
        	mac.init(keySpec);
        	// update method adds the given byte to the Mac's input data. 
        	mac.update(input.getBytes());
        	final byte[] m = mac.doFinal();
        	// The base64-encoder in Commons Codec
        	temp = base64Encode(m);
    	} catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
    	} catch (InvalidKeyException e) {
        	e.printStackTrace();
    	}
    	return temp;
	}
  
	private String httpPost(Map<String, String> requestParams) throws Exception {
    	final URL url = new URL(authUrl);

    	final StringBuilder data = new StringBuilder();
    	for (String key : requestParams.keySet())
        	data.append(String.format("%s=%s&", key, requestParams.get(key)));

    	final String dataString = data.toString();
    	final String formData = dataString.substring(0, dataString.length() - 1);

    	final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    	connection.setRequestMethod("POST");
    	connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
    	connection.setDoOutput(true);

    	final OutputStream outputStream = connection.getOutputStream();
    	final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
    	outputStreamWriter.write(formData);
    	outputStreamWriter.flush();

    	InputStream is = connection.getResponseCode() != 400 ? connection.getInputStream() : connection.getErrorStream();
      
    	final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    	final StringBuffer stringBuffer = new StringBuffer();
    	String line;
    	while ((line = rd.readLine()) != null)
        	stringBuffer.append(line);

    	outputStreamWriter.close();
    	rd.close();
      
    	if (connection.getResponseCode() == 400)
        	throw new Exception("error: " + stringBuffer.toString());

    	return stringBuffer.toString();
	}
}
