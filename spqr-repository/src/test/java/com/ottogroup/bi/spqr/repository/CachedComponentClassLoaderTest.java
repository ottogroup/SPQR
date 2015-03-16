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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link CachedComponentClassLoader}
 * @author mnxfst
 * @since Dec 2, 2014
 * TODO more testing
 */
public class CachedComponentClassLoaderTest {

	/**
	 * Test case for {@link CachedComponentClassLoader#PipelineComponentClassloader(ClassLoader)}
	 * being provided null
	 */
	@Test
	public void testConstructor_withNullParentClassloader() throws ClassNotFoundException {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(null);
		Assert.assertNotNull("The instance must not be null", cl);
		Class<?> c = cl.loadClass("java.lang.String");
		Assert.assertEquals("Values must be equal as the classloader switches to bootstrap loader if parent is missing", c.getName(), "java.lang.String");
	}
	
	/**
	 * Test case for {@link CachedComponentClassLoader#loadClass(String)}
	 * being provided an empty input 
	 */
	@Test
	public void testLoadClass_withEmptyInput() {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		try {
			cl.loadClass("");
			Assert.fail("No such class exist");
		} catch(ClassNotFoundException e) {
			// expected
		}
	}
	
	/**
	 * Test case for {@link CachedComponentClassLoader#loadClass(String)}
	 * being provided an non-existing class reference 
	 */
	@Test
	public void testLoadClass_withInvalidClassReference() {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		try {
			cl.loadClass("java.lang.StringUnknown");
			Assert.fail("No such class exist");
		} catch(ClassNotFoundException e) {
			// expected
		}
	}

	/**
	 * Test case for {@link CachedComponentClassLoader#loadClass(String)}
	 * being provided a reference towards a standard class 
	 */
	@Test
	public void testLoadClass_withStandardClassReference() throws ClassNotFoundException {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		Class<?> c = cl.loadClass("java.lang.String");
		Assert.assertEquals("Values must be equal", c.getName(), "java.lang.String");
	}

	/**
	 * Test case for {@link CachedComponentClassLoader#findClass(String)} being provided 
	 * an empty string
	 */
	@Test
	public void testFindClass_withEmptyInput() throws Exception {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		Assert.assertNull(cl.findClass(""));
	}
	
	/**
	 * Test case for {@link CachedComponentClassLoader#findClass(String)} being provided 
	 * an invalid class reference
	 */
	@Test
	public void testFindClass_withInvalidClassReference() throws Exception {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		Assert.assertNull(cl.findClass("java.lang.Stringunknown"));
	}
	
	/**
	 * Test case for {@link CachedComponentClassLoader#findClass(String)} being provided 
	 * a standard class reference
	 */
	@Test
	public void testFindClass_withStandardClassReference() throws Exception {
		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
		Assert.assertNull(cl.findClass("java.lang.String"));
	}
//	
//	@Test
//	public void testInitialize_withValidPath() throws IOException, RequiredInputMissingException {
//		CachedComponentClassLoader cl = new CachedComponentClassLoader(CachedComponentClassLoader.class.getClassLoader());
//		cl.initialize("/opt/transport/streaming/spqr-0.1.0/spqr-node/repo/spqr-kafka");
//	}
//	


}
