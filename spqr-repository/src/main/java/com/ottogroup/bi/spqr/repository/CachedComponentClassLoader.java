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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ottogroup.bi.spqr.exception.ComponentInitializationFailedException;
import com.ottogroup.bi.spqr.exception.RequiredInputMissingException;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;
import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponentType;
import com.ottogroup.bi.spqr.pipeline.component.annotation.SPQRComponent;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;
import com.ottogroup.bi.spqr.repository.exception.ComponentInstantiationFailedException;
import com.ottogroup.bi.spqr.repository.exception.UnknownComponentException;

/**
 * Reads the contents of a provided jar (array of bytes), extracts all {@link MicroPipelineComponent} and allows
 * to access them by providing the component name as well as its version
 * @author mnxfst
 * @since Oct 29, 2014
 * TODO how to manage multiple resources having the same name but being located in different JAR files?
 * TODO add performance tracking while loading classes
 * TODO more testing on initialize and newInstance
 */
public class CachedComponentClassLoader extends ClassLoader {

	private static final Logger logger = Logger.getLogger(CachedComponentClassLoader.class);

	private static final String ANNOTATION_TYPE_METHOD = "type";
	private static final String ANNOTATION_NAME_METHOD = "name";
	private static final String ANNOTATION_VERSION_METHOD = "version";
	private static final String ANNOTATION_DESCRIPTION_METHOD = "description";
	
	/** resources */
	private Map<String, byte[]> resources = new HashMap<String, byte[]>();
	/** class file byte code */
	private Map<String, byte[]> byteCode = new HashMap<String, byte[]>();
	/** already resolved classes */
	private Map<String, Class<?>> cachedClasses = new HashMap<>();
	/** managed pipeline components */
	private Map<String, ComponentDescriptor> managedComponents = new HashMap<>();
	
	/**
	 * Initializes the class using the provided input
	 * @param parentClassLoader
	 */
	public CachedComponentClassLoader(final ClassLoader parentClassLoader) {
		super(parentClassLoader);
	}
	
	/**
	 * Loads referenced class:
	 * <ul>
	 *   <li>find in already loaded classes</li>
	 *   <li>look it up in previously loaded and thus cached classes</li>
	 *   <li>find it in jar files</li>
	 *   <li>ask parent class loader</li>
	 * </ul>
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
		synchronized (getClassLoadingLock(name)) {
			
			// check if class has already been loaded
			Class<?> clazz = findLoadedClass(name);
			if(clazz != null)
				return clazz;
			
			// check internal cache for already loaded classes
			clazz = cachedClasses.get(name);
			if(clazz != null) {
				return clazz;
			}
			
			// check if the managed jars contain the class
			if(byteCode.containsKey(name)) 
				clazz = findClass(name);
			if(clazz != null) {
				return clazz;
			}
			
			// otherwise hand over the request to the parent class loader
			clazz = super.loadClass(name);
			if(clazz == null)
				throw new ClassNotFoundException("Class '" + name + "' not found");
			
			return clazz;
		}
	}
	
	/**
	 * Find class inside managed jars or hand over the parent
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		// find in already loaded classes to make things shorter
		Class<?> clazz = findLoadedClass(name);
		if(clazz != null)
			return clazz;
		
		// check if the class searched for is contained inside managed jars,
		// otherwise hand over the request to the parent class loader
		byte[] data = this.byteCode.get(name);
		if(data == null || data.length < 1) {
			return null;
		}			
		
		return defineClass(name, data, 0, data.length);
	}
	
	/**
	 * Handle resource lookups by first checking the managed JARs and hand
	 * it over to the parent if no entry exits
	 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String name) {
		
		// lookup the name of the JAR file holding the resource 
		byte[] data = this.resources.get(name);
		
		// if there is no such file, hand over the request to the parent class loader
		if(data == null || data.length < 1)
			return super.getResourceAsStream(name);
		
		
		return new ByteArrayInputStream(data);
	}
	
	/**
	 * Initializes the class loader by pointing it to folder holding managed JAR files
	 * @param componentFolder
	 * @throws IOException
	 * @throws RequiredInputMissingException
	 */
	public void initialize(final String componentFolder) throws IOException, RequiredInputMissingException {
		
		///////////////////////////////////////////////////////////////////
		// validate input
		if(StringUtils.isBlank(componentFolder))
			throw new RequiredInputMissingException("Missing required value for parameter 'componentFolder'");

		File folder = new File(componentFolder);
		if(!folder.isDirectory())
			throw new IOException("Provided input '"+componentFolder+"' does not reference a valid folder");

		File[] jarFiles = folder.listFiles();
		if(jarFiles == null || jarFiles.length < 1)
			throw new RequiredInputMissingException("No JAR files found in folder '"+componentFolder+"'");
		//
		///////////////////////////////////////////////////////////////////
		
		logger.info("Initializing component classloader [folder="+componentFolder+"]");
		
		// step through jar files, ensure it is a file and iterate through its contents
		for(File jarFile : jarFiles) {
			if(jarFile.isFile()) {

				JarInputStream jarInputStream = null;
				try {

					jarInputStream = new JarInputStream(new FileInputStream(jarFile));
					JarEntry jarEntry = null;
					while((jarEntry = jarInputStream.getNextJarEntry()) != null) {
						String jarEntryName = jarEntry.getName();
						// if the current file references a class implementation, replace slashes by dots, strip 
						// away the class suffix and add a reference to the classes-2-jar mapping 
						if(StringUtils.endsWith(jarEntryName, ".class")) {
							jarEntryName = jarEntryName.substring(0, jarEntryName.length() - 6).replace('/','.');
							this.byteCode.put(jarEntryName, loadBytes(jarInputStream));
						} else {
							// ...and add a mapping for resource to jar file as well
							this.resources.put(jarEntryName, loadBytes(jarInputStream));
						}
					}
				} catch(Exception e) {
					logger.error("Failed to read from JAR file '"+jarFile.getAbsolutePath()+"'. Error: " + e.getMessage());
				} finally {
					try {
						jarInputStream.close();
					} catch(Exception e) {
						logger.error("Failed to close open JAR file '"+jarFile.getAbsolutePath()+"'. Error: " + e.getMessage());
					}
				}
			}
		}
		
		logger.info("Analyzing " + this.byteCode.size() + " classes for component annotation");
		
		// load classes from jars marked component files and extract the deployment descriptors
		for(String cjf : this.byteCode.keySet()) {
			
			try {
				Class<?> c = loadClass(cjf);
				Annotation spqrComponentAnnotation = getSPQRComponentAnnotation(c);
				if(spqrComponentAnnotation != null) {
					
					Method spqrAnnotationTypeMethod = spqrComponentAnnotation.getClass().getMethod(ANNOTATION_TYPE_METHOD, (Class[])null);
					Method spqrAnnotationNameMethod = spqrComponentAnnotation.getClass().getMethod(ANNOTATION_NAME_METHOD, (Class[])null);
					Method spqrAnnotationVersionMethod = spqrComponentAnnotation.getClass().getMethod(ANNOTATION_VERSION_METHOD, (Class[])null);
					Method spqrAnnotationDescriptionMethod = spqrComponentAnnotation.getClass().getMethod(ANNOTATION_DESCRIPTION_METHOD, (Class[])null);

					@SuppressWarnings("unchecked")
					Enum<MicroPipelineComponentType> o = (Enum<MicroPipelineComponentType>)spqrAnnotationTypeMethod.invoke(spqrComponentAnnotation, (Object[])null);
					final MicroPipelineComponentType componentType = Enum.valueOf(MicroPipelineComponentType.class, o.name());
					final String componentName = (String)spqrAnnotationNameMethod.invoke(spqrComponentAnnotation, (Object[])null);
					final String componentVersion = (String)spqrAnnotationVersionMethod.invoke(spqrComponentAnnotation, (Object[])null);
					final String componentDescription = (String)spqrAnnotationDescriptionMethod.invoke(spqrComponentAnnotation, (Object[])null);

					this.managedComponents.put(getManagedComponentKey(componentName, componentVersion), new ComponentDescriptor(c.getName(), componentType, componentName, componentVersion, componentDescription));
					logger.info("pipeline component found [type="+componentType+", name="+componentName+", version="+componentVersion+"]");;
				}
			} catch(Throwable e) {
				e.printStackTrace();
				logger.error("Failed to load class '"+cjf+"'. Error: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Steps through {@link Annotation} list, searches for {@link SPQRComponent} 
	 * annotation and returns true if any is found
	 * @param clazz
	 * @return
	 */
	protected Annotation getSPQRComponentAnnotation(final Class<?> clazz) {
		Annotation[] annotations = clazz.getAnnotations();
		if(annotations != null && annotations.length > 0) {
			for(Annotation annotation : annotations) {
				@SuppressWarnings("unchecked")
				Class<Annotation> type = (Class<Annotation>) annotation.annotationType();
				if(StringUtils.equals(type.getName(), SPQRComponent.class.getName())) {
					return annotation;
				}
			}
		}
		return null;
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

		long start = System.currentTimeMillis();
		
		// generate component key and retrieve the associated descriptor ... if available
		String componentKey = getManagedComponentKey(name, version);
		ComponentDescriptor descriptor = this.managedComponents.get(componentKey);
		if(descriptor == null)
			throw new UnknownComponentException("Unknown component [name="+name+", version="+version+"]");
		
		// if the descriptor exists, load the referenced component class, create an instance and hand over the properties
		try {
			Class<?> messageHandlerClass = loadClass(descriptor.getComponentClass());
			MicroPipelineComponent instance = (MicroPipelineComponent)messageHandlerClass.newInstance();
			instance.setId(id);
			long beforeInit = System.currentTimeMillis();
			instance.initialize(properties);
			long endAfterInit = System.currentTimeMillis();
			if(logger.isDebugEnabled())
				logger.debug("newInstance[name="+name+", version="+version+", overall="+(endAfterInit-start)+"ms, instance="+(beforeInit-start)+"ms, init="+(endAfterInit-beforeInit)+"ms]");
			return instance;
		} catch(IllegalAccessException e) {
			throw new ComponentInstantiationFailedException("Failed to instantiate component [name="+name+", version="+version+", reason="+e.getMessage());
		} catch (InstantiationException e) {
			throw new ComponentInstantiationFailedException("Failed to instantiate component [name="+name+", version="+version+", reason="+e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new ComponentInstantiationFailedException("Failed to instantiate component [name="+name+", version="+version+", reason="+e.getMessage());
		} catch (ComponentInitializationFailedException e) {
			throw new ComponentInstantiationFailedException("Failed to instantiate component [name="+name+", version="+version+", reason="+e.getMessage());
		} 
	}

	/**
	 * Returns true in case the referenced component managed by this class loader
	 * @param name
	 * @param version
	 * @return
	 */
	public boolean isManagedComponent(final String name, final String version) {
		return this.managedComponents.containsKey(getManagedComponentKey(name, version));
	}
	
	/**
	 * Generates a key to be used for looking up the component+version specific class
	 * @param name
	 * @param version
	 * @return
	 */
	public String getManagedComponentKey(final String name, final String version) {
		StringBuffer buf = new StringBuffer(name.trim().toLowerCase()).append("~").append(version.trim().toLowerCase());
		return buf.toString();
	}

	
    /**
	  * Reads jar file contents from stream
	  * @param is jar input stream
	  * @throws IOException
	  */
	protected byte[] loadBytes(InputStream is) throws IOException {
		BufferedInputStream bufInputStream = new BufferedInputStream(is, 8192);
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		int c;
		int bufferSize = 8192;
		byte[] buffer = new byte[bufferSize];
		while ((c = bufInputStream.read(buffer)) != -1) {
			byteOutStream.write(buffer, 0, c);
		}
		return byteOutStream.toByteArray();		 
	}

	/**
	 * @return the managedComponents
	 */
	public Map<String, ComponentDescriptor> getManagedComponents() {
		return managedComponents;
	}
}
