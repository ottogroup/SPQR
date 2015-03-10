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

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.repository.exception.ComponentInstantiationFailedException;
import com.ottogroup.bi.spqr.repository.exception.UnknownComponentException;

/**
 * Test case for {@link ComponentRepository}
 * @author mnxfst
 * @since Dec 18, 2014
 *
 */
public class ComponentRepositoryTest {

	
	/**
	 * Test case for {@link ComponentRepository#newInstance(String, String, String, java.util.Properties, Class)} with
	 * missing id
	 */
	@Test
	public void testNewInstance_withMissingId() throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {

		String name = "test-name";
		String version = "test-version";

		CachedComponentClassLoader cachedComponentClassLoaderMock = Mockito.mock(CachedComponentClassLoader.class);
		ComponentRepository componentRepository = new ComponentRepository();
		componentRepository.addComponentClassLoader(cachedComponentClassLoaderMock);

		try {
			componentRepository.newInstance("", name, version, new Properties());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link ComponentRepository#newInstance(String, String, String, java.util.Properties, Class)} with
	 * missing name
	 */
	@Test
	public void testNewInstance_withMissingName() throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {

		String id = "test-id";
		String version = "test-version";

		CachedComponentClassLoader cachedComponentClassLoaderMock = Mockito.mock(CachedComponentClassLoader.class);
		ComponentRepository componentRepository = new ComponentRepository();
		componentRepository.addComponentClassLoader(cachedComponentClassLoaderMock);

		try {
			componentRepository.newInstance(id, "", version, new Properties());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link ComponentRepository#newInstance(String, String, String, java.util.Properties, Class)} with
	 * missing version
	 */
	@Test
	public void testNewInstance_withMissingVersion() throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {

		String id = "test-id";
		String name = "test-name";

		CachedComponentClassLoader cachedComponentClassLoaderMock = Mockito.mock(CachedComponentClassLoader.class);
		ComponentRepository componentRepository = new ComponentRepository();
		componentRepository.addComponentClassLoader(cachedComponentClassLoaderMock);

		try {
			componentRepository.newInstance(id, name, "", new Properties());
			Assert.fail("Missing required input");
		} catch(RequiredInputMissingException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link ComponentRepository#newInstance(String, String, String, java.util.Properties, Class)} provided
	 * reference to non-managed component
	 */
	@Test
	public void testNewInstance_withNonManagedComponent() throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {

		String id = "test-id";
		String name = "test-name";
		String version = "test-version";

		CachedComponentClassLoader cachedComponentClassLoaderMock = Mockito.mock(CachedComponentClassLoader.class);
		ComponentRepository componentRepository = new ComponentRepository();
		componentRepository.addComponentClassLoader(cachedComponentClassLoaderMock);

		Mockito.when(cachedComponentClassLoaderMock.isManagedComponent(name, version)).thenReturn(false);
		
		try {
			componentRepository.newInstance(id, name, version, new Properties());
			Assert.fail("Unknown component");
		} catch(UnknownComponentException e) {
			// expected
		}
		
		Mockito.verify(cachedComponentClassLoaderMock, Mockito.atLeastOnce()).isManagedComponent(name, version);		
	}

	/**
	 * Test case for {@link ComponentRepository#newInstance(String, String, String, java.util.Properties, Class)} 
	 * throwing a {@link ComponentInstantiationFailedException}
	 */
	@Test
	public void testNewInstance_withComponentInstantiationFailedException() throws RequiredInputMissingException, ComponentInstantiationFailedException, UnknownComponentException {

		String id = "test-id";
		String name = "test-name";
		String version = "test-version";
		Properties props = new Properties();

		ComponentInstantiationFailedException cifx = new ComponentInstantiationFailedException();
		CachedComponentClassLoader cachedComponentClassLoaderMock = Mockito.mock(CachedComponentClassLoader.class);
		ComponentRepository componentRepository = new ComponentRepository();
		componentRepository.addComponentClassLoader(cachedComponentClassLoaderMock);

		Mockito.when(cachedComponentClassLoaderMock.isManagedComponent(name, version)).thenReturn(true);
		Mockito.when(cachedComponentClassLoaderMock.newInstance(id, name, version, props)).thenThrow(cifx);		
		
		try {
			componentRepository.newInstance(id, name, version, new Properties());
			Assert.fail("Instantiation should fail");
		} catch(ComponentInstantiationFailedException e) {
			// expected
		}
		
		Mockito.verify(cachedComponentClassLoaderMock, Mockito.atLeastOnce()).isManagedComponent(name, version);
		Mockito.verify(cachedComponentClassLoaderMock, Mockito.atLeastOnce()).newInstance(id, name, version, props);		
	}

}
