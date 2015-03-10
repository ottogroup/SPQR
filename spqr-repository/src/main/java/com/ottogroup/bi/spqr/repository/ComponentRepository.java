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
package com.ottogroup.bi.spqr.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.repository.exception.ComponentInstantiationFailedException;
import com.ottogroup.bi.spqr.repository.exception.UnknownComponentException;

/**
 * Manages all configured component repositories. For each it creates a new {@link CachedComponentClassLoader} to keep 
 * classes isolated and thus support multi-version implementation 
 * @author mnxfst
 * @since Dec 2, 2014
 * TODO testing
 */
public class ComponentRepository {
	
	/** list of all existing component class loaders */
	private final List<CachedComponentClassLoader> componentClassLoaders = new ArrayList<>();
		
	/**
	 * Adds a new folder to the repository 
	 * @throws RequiredInputMissingException
	 * @throws IOException 
	 */
	public Map<String, ComponentDescriptor> addComponentFolder(final String componentFolder) throws RequiredInputMissingException, IOException {
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// validate provided input
		if(StringUtils.isBlank(componentFolder)) {
			throw new RequiredInputMissingException("Missing required component folder");
		}		
		//
		//////////////////////////////////////////////////////////////////////////////////////////

		CachedComponentClassLoader componentClassLoader = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		componentClassLoader.initialize(componentFolder);
		this.componentClassLoaders.add(componentClassLoader);
		
		return componentClassLoader.getManagedComponents();
	}
	
	/**
	 * Adds a component class loader explicitly - currently used for testing purpose only 
	 * @param cachedComponentClassLoader
	 */
	protected void addComponentClassLoader(final CachedComponentClassLoader cachedComponentClassLoader) {
		this.componentClassLoaders.add(cachedComponentClassLoader);
	}
	
	/**
	 * Looks up the reference {@link DataComponent}, instantiates it and passes over the provided {@link Properties}
	 * @param name name of component to instantiate (required)
	 * @param version version of component to instantiate (required)
	 * @param properties properties to use for instantiation
	 * @return
	 * @throws RequiredInputMissingException thrown in case a required parameter value is missing
	 * @throws ComponentInstantiationFailedException thrown in case the instantiation failed for any reason
	 * @throws UnknownComponentException thrown in case the name and version combination does not reference a managed component
	 */
	public MicroPipelineComponent newInstance(final String id, final String name, final String version, final Properties properties) 
			throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// validate provided input		
		if(StringUtils.isBlank(id))
			throw new RequiredInputMissingException("Missing required component id");
		if(StringUtils.isBlank(name))
			throw new RequiredInputMissingException("Missing required component name");
		if(StringUtils.isBlank(version))
			throw new RequiredInputMissingException("Missing required component version");
		//
		//////////////////////////////////////////////////////////////////////////////////////////

		for(CachedComponentClassLoader pccl : this.componentClassLoaders) {
			if(pccl.isManagedComponent(name, version)) {
				return pccl.newInstance(id, name, version, properties);
			}
		}		
		
		throw new UnknownComponentException("Unknown component [name="+name+", version="+version+"]");
	}
	
}
