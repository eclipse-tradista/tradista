package org.eclipse.tradista.core.status.constants;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public final class StatusConstants {

	private StatusConstants() {
	}

	public static final String CREATED = "CREATED";
	
	public static final String CANCELED = "CANCELED";

	public static final String ALLOCATED = "ALLOCATED";

	public static final String TO_BE_VALIDATED = "TO_BE_VALIDATED";

	public static final String VALIDATED = "VALIDATED";

	public static final String VALIDATION_KO = "VALIDATION_KO";

	public static final String GENERATED = "GENERATED";

	public static final String TO_BE_GENERATED = "TO_BE_GENERATED";

	public static final String TO_BE_SENT = "TO_BE_SENT";

	public static final String SENT = "SENT";

	public static final String PROCESSED = "PROCESSED";

}