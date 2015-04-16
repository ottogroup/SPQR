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
package com.ottogroup.bi.spqr.resman.server;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.repository.ComponentDescriptor;
import com.ottogroup.bi.spqr.repository.ComponentRepository;
import com.ottogroup.bi.spqr.resman.node.SPQRNodeManager;
import com.ottogroup.bi.spqr.resman.resource.node.SPQRNodeManagementResource;
import com.ottogroup.bi.spqr.resman.resource.pipeline.SPQRPipelineManagementResource;

/**
 * @author mnxfst
 * @since Apr 10, 2015
 */
public class SPQRResourceManagerServer extends Application<SPQRResourceManagerConfiguration> {

	/** our faithful logging facility ... ;-) */
	private static final Logger logger = Logger.getLogger(SPQRResourceManagerServer.class);
	
	/** reference to component repository - required for deploying new artifacts to nodes on request */
	private ComponentRepository componentRepository = null;
	/** reference to spqr node manager */
	private SPQRNodeManager spqrNodeManager = null;
	
	/**
	 * @see io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)
	 */
	public void run(SPQRResourceManagerConfiguration configuration,	Environment environment) throws Exception {		
		initializeLog4j(configuration.getLog4jConfiguration());		
		this.componentRepository = loadAndDeployApplicationRepository(configuration.getComponentRepositoryFolder());
		this.spqrNodeManager = new SPQRNodeManager(5, new JerseyClientBuilder(environment).using(configuration.getHttpClient()));
		environment.jersey().register(new SPQRPipelineManagementResource());
		environment.jersey().register(new SPQRNodeManagementResource(spqrNodeManager));
		Runtime.getRuntime().addShutdownHook(new SPQRResourceManagerShutdownHandler());		
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
	 * Reads contents from {@link ApplicationRepositoryConfiguration referenced repository} and deploys them to given
	 * {@link ActorRef deployment receiver}.
	 * @param cfg
	 * @param jarDeploymentReceiverRef
	 * @throws RequiredInputMissingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public ComponentRepository loadAndDeployApplicationRepository(final String repositoryPath) throws RequiredInputMissingException, JsonParseException, JsonMappingException, IOException {

		////////////////////////////////////////////////////////////////////////////
		// validate provided input and ensure that folder exists
		if(StringUtils.isBlank(repositoryPath))
			throw new RequiredInputMissingException("Missing required input for parameter 'repositoryPath'");

		
		File repositoryFolder = new File(StringUtils.trim(repositoryPath));
		if(repositoryFolder == null || !repositoryFolder.isDirectory())
			throw new RequiredInputMissingException("No repository found at provided folder '"+repositoryPath+"'");
		//
		////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////

		final ComponentRepository library = new ComponentRepository();

		////////////////////////////////////////////////////////////////////////////
		// find component repositories below repository folder 
		File[] repoFolders = repositoryFolder.listFiles();
		if(repoFolders == null || repoFolders.length < 1)
			return library;
		//
		////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////
		// for each entry check if it is a folder and pass it on the library
		int folderCount = 0;
		int componentsCount = 0;
		for(File folder : repoFolders) {
			if(folder.isDirectory()) {
				try {
					logger.info("Processing folder '"+folder.getAbsolutePath()+"'");
					Map<String, ComponentDescriptor> descriptors = library.addComponentFolder(folder.getAbsolutePath());
					componentsCount = componentsCount + descriptors.size();
					folderCount++;
				} catch(Exception e) {
					logger.error("Failed to add folder '"+folder.getAbsolutePath()+"' to component repository. Error: " + e.getMessage());
				}
			} 
		}
		//
		////////////////////////////////////////////////////////////////////////////
		
		logger.info("Components deployment finished [repo="+repositoryPath+", componentFolders="+folderCount+", componets="+componentsCount+"]");
		return library;
	}		
	
	/**
	 * Executes the SPQR resource manager
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new SPQRResourceManagerServer().run(args);
	}
}
