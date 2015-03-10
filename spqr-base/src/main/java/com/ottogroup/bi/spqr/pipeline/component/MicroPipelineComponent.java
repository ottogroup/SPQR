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
package com.ottogroup.bi.spqr.pipeline.component;

import java.util.Properties;

import com.ottogroup.bi.spqr.pipeline.component.emitter.Emitter;
import com.ottogroup.bi.spqr.pipeline.component.operator.Operator;
import com.ottogroup.bi.spqr.pipeline.component.source.Source;

/**
 * Interface to the three major building blocks of a {@link MicroPipeline}: {@link Source}, {@link Operator} and {@link Emitter}
 * @author mnxfst
 * @since Dec 15, 2014
 */
public interface MicroPipelineComponent {

	/**
	 * Sets the component identifier
	 * @param id
	 */
	public void setId(final String id);
	
	/**
	 * Returns the component identifier which is unique
	 * within the boundaries of a {@link MicroPipeline}
	 * @return
	 */
	public String getId();
	
	/**
	 * Initializes the component before it gets executed
	 * @param properties
	 */
	public void initialize(final Properties properties);
	
	/**
	 * Tells the component to shut itself down
	 */
	public boolean shutdown();
	
	/**
	 * Returns the {@link MicroPipelineComponentType}
	 * @return
	 */
	public MicroPipelineComponentType getType();
}
