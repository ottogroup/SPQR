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
package com.ottogroup.bi.spqr.pipeline.exception;

import com.ottogroup.bi.spqr.pipeline.component.MicroPipelineComponent;

/**
 * Thrown in case the initialization of a {@link MicroPipelineComponent} failed
 * @author mnxfst
 * @since Mar 6, 2015
 */
public class ComponentInitializationFailedException extends Exception {

	private static final long serialVersionUID = -7893237496464557534L;

	public ComponentInitializationFailedException() {		
	}

	public ComponentInitializationFailedException(String message) {
		super(message);
	}

	public ComponentInitializationFailedException(Throwable cause) {
		super(cause);
	}

	public ComponentInitializationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
