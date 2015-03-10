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

package com.ottogroup.bi.spqr.repository.exception;

/**
 * Thrown in case the instantiation of a pipeline component failed to any reason
 * @author mnxfst
 * @since Sep 4, 2014
 */
public class ComponentInstantiationFailedException extends Exception {

	private static final long serialVersionUID = -8586826517371913125L;

	public ComponentInstantiationFailedException() {		
	}

	public ComponentInstantiationFailedException(String message) {
		super(message);
	}

	public ComponentInstantiationFailedException(Throwable cause) {
		super(cause);
	}

	public ComponentInstantiationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
