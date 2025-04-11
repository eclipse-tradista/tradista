package org.eclipse.tradista.core.message.model;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class ImportError extends MessageError {

	private static final long serialVersionUID = 2244756759677298859L;

	public static final String IMPORT = "Import";

	@Override
	public String getType() {
		return IMPORT;
	}

	@Override
	public String getSubjectKey() {
		if (getMessage() != null) {
			return getType() + "-" + getMessage().getId();
		} else
			return null;
	}

}