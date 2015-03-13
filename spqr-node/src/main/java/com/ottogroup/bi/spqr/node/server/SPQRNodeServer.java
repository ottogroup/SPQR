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
package com.ottogroup.bi.spqr.node.server;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;

import com.ottogroup.bi.spqr.pipeline.MicroPipeline;

/**
 * Ramps up SPQR processing nodes which receive requests for {@link MicroPipeline} execution
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class SPQRNodeServer extends Application<SPQRNodeServerConfiguration> {

	
	/**
	 * @see io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)
	 */
	public void run(SPQRNodeServerConfiguration configuration, Environment environment) throws Exception {
		initializeLog4j(configuration.getLog4jConfiguration());
	}
	
	/**
	 * Initializes the log4j framework by pointing it to the configuration file referenced by parameter
	 * @param log4jConfigurationFile
	 */
	protected void initializeLog4j(final String log4jConfigurationFile) {		
		String log4jPropertiesFile = StringUtils.lowerCase(StringUtils.trim(log4jConfigurationFile));
		if(StringUtils.isNoneBlank(log4jConfigurationFile)) {
			File log4jFile = new File(log4jPropertiesFile);
			if(log4jFile.isFile()) {
				try {
					PropertyConfigurator.configure(new FileInputStream(log4jFile));
				} catch (FileNotFoundException e) {
					System.out.println("No log4j configuration found at '"+log4jConfigurationFile+"'");
				}
			} else {
				System.out.println("No log4j configuration found at '"+log4jConfigurationFile+"'");
			}
		} else {
			System.out.println("No log4j configuration file provided");
		}
	}
	
	/**
	 * Ramps up the node server instance
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new SPQRNodeServer().run(args);
	}

}
