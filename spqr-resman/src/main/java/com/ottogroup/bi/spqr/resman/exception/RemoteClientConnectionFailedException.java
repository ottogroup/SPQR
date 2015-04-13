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
package com.ottogroup.bi.spqr.resman.exception;

import com.ottogroup.bi.spqr.resman.node.SPQRNodeClient;

/**
 * Exception thrown by {@link SPQRNodeClient} on any failure during remote client access
 * @author mnxfst
 * @since Apr 13, 2015
 */
public class RemoteClientConnectionFailedException extends Exception {

	private static final long serialVersionUID = 2259833389466539291L;

	public RemoteClientConnectionFailedException() {		
	}

	public RemoteClientConnectionFailedException(String message) {
		super(message);
	}

	public RemoteClientConnectionFailedException(Throwable cause) {
		super(cause);
	}

	public RemoteClientConnectionFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
