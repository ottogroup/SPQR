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

import com.ottogroup.bi.spqr.pipeline.component.operator.DelayedResponseOperatorWaitStrategy;

/**
 * Thrown in case a requested {@link DelayedResponseOperatorWaitStrategy} does not exist
 * @author mnxfst
 * @since Mar 12, 2015
 */
public class UnknownWaitStrategyException extends Exception {

	private static final long serialVersionUID = 53649492583889138L;

	public UnknownWaitStrategyException() {		
	}

	public UnknownWaitStrategyException(String message) {
		super(message);
	}

	public UnknownWaitStrategyException(Throwable cause) {
		super(cause);
	}

	public UnknownWaitStrategyException(String message, Throwable cause) {
		super(message, cause);
	}
}
