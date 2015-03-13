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

import com.ottogroup.bi.spqr.pipeline.MicroPipeline;

/**
 * Thrown in case the instantiation of a {@link MicroPipeline} failed for any reason
 * @author mnxfst
 * @since Mar 13, 2015
 */
public class PipelineInstantiationFailedException extends Exception {

	private static final long serialVersionUID = -2785833660966006782L;

	public PipelineInstantiationFailedException() {		
	}

	public PipelineInstantiationFailedException(String message) {
		super(message);
	}

	public PipelineInstantiationFailedException(Throwable cause) {
		super(cause);
	}

	public PipelineInstantiationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
