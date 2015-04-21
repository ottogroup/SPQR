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
package com.ottogroup.bi.spqr.websocket.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ottogroup.bi.spqr.websocket.server.cfg.SPQRWebSocketServerConfiguration;

/**
 * Ramps up the web socket server which provides access to pipelines results. As SPQR uses kafka by default
 * to interconnect pipelines and emit results the server currently supports to read content from 
 * kafka topics accordingly. <br/><br/>
 * In contrast to spqr node and resource management server this one is based on netty as it provides 
 * support for web sockets. 
 *  
 * @author mnxfst
 * @since Apr 17, 2015
 */
public class SPQRWebSocketServer {
	
	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRWebSocketServer.class);

	public static final String CFG_HELP = "help";
	public static final String CFG_HELP_SHORT = "h";
	public static final String CFG_CONFIGURATION_FILE = "config";
	public static final String CFG_CONFIGURATION_FILE_SHORT = "c";

	/**
	 * Initializes and executes the server components
	 * @param configurationFile reference to configuration file
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	protected void run(final String configurationFile) throws IOException, InterruptedException {
		
		///////////////////////////////////////////////////////////////////
		// lookup configuration file and create handle
		File cfgFile = new File(StringUtils.trim(configurationFile));
		if(!cfgFile.isFile())
			throw new FileNotFoundException("No configuration file found at '"+configurationFile+"'");
		//
		///////////////////////////////////////////////////////////////////

		run(new FileInputStream(cfgFile));		
	}
	
	/**
	 * Initializes and executes the server components. Although the other {@link SPQRWebSocketServer#run(String)}
	 * could do the same work this one allows better testing as the {@link InputStream} parameter allows
	 * to inject a variable which may be controlled much better. In the case it would be necessary to 
	 * provide a reference to a temporary file which is controlled by the use case ... somehow too much work ;-)
	 * This one is easier.  
	 * @param configurationFileStream stream holding configuration file content
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	protected void run(final InputStream configurationFileStream) throws IOException, InterruptedException {

		///////////////////////////////////////////////////////////////////
		// parse out configuration from provided input stream
		ObjectMapper jsonMapper = new ObjectMapper();
		SPQRWebSocketServerConfiguration cfg = jsonMapper.readValue(configurationFileStream, SPQRWebSocketServerConfiguration.class);
		//
		///////////////////////////////////////////////////////////////////

		PropertyConfigurator.configure(cfg.getLog4jConfigurationFile());

		EventLoopGroup bossGroup = new NioEventLoopGroup(cfg.getBossEventGroupThreads());
		EventLoopGroup workerGroup = new NioEventLoopGroup(cfg.getWorkerEventGroupThreads());
		
		logger.info("websocket server [port="+cfg.getPort()+", bossGroupThreads="+cfg.getBossEventGroupThreads()+", workerGroupThreads="+cfg.getWorkerEventGroupThreads()+"]");
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			         .channel(NioServerSocketChannel.class)
			         .handler(new LoggingHandler(LogLevel.INFO))
			         .childHandler(new SPQRWebSocketServerInitializer());
			
			Channel channel = bootstrap.bind(cfg.getPort()).sync().channel();
			channel.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	/**
	 * Returns the available command-line options
	 * @return
	 */
	public static Options getOptions() {
		Options options = new Options();
		options.addOption(CFG_CONFIGURATION_FILE_SHORT, CFG_CONFIGURATION_FILE, true, "Configuration file");
		options.addOption(CFG_HELP_SHORT, CFG_HELP, false, "Help");
		return options;
	}
	
	/**
	 * Checks the command-line settings and ramps up the server
	 * @param args arguments passed from command-line
	 * @throws ParseException indicates that parsing the command-line failed for any reason
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException, InterruptedException {

		////////////////////////////////////////////////////////////////////////
		// evaluate command-line and ensure that it provides valid input
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = parser.parse(getOptions(), args);
		
		if(commandLine.hasOption(CFG_HELP)) {
			new HelpFormatter().printHelp("spqr-websocket-server", getOptions());
			return;
		}
		
		if(!commandLine.hasOption(CFG_CONFIGURATION_FILE)) {
			new HelpFormatter().printHelp("spqr-websocket-server", "", getOptions(), "Missing required configuration file");
			return;
		}
		//
		////////////////////////////////////////////////////////////////////////
	
		new SPQRWebSocketServer().run(commandLine.getOptionValue(CFG_CONFIGURATION_FILE));
	}
}
