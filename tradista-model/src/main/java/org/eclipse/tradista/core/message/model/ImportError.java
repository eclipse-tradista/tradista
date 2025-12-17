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

	public enum ImportErrorType {
		STRUCTURE, MAPPING;

		@Override
		public String toString() {
			switch (this) {
			case STRUCTURE:
				return "Structure";
			case MAPPING:
				return "Mapping";
			}
			return super.toString();
		}

		/**
		 * Gets a Type from a display name. Display names are used in GUIs. A display
		 * name of a Type is the result of its toString() method.
		 * 
		 * @param type
		 * @return
		 */
		public static ImportErrorType getType(String displayName) {
			switch (displayName) {
			case "Structure":
				return STRUCTURE;
			case "Mapping":
				return MAPPING;
			}
			return null;
		}
	}

	private ImportErrorType importErrorType;

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

	public ImportErrorType getImportErrorType() {
		return importErrorType;
	}

	public void setImportErrorType(ImportErrorType importErrorType) {
		this.importErrorType = importErrorType;
	}

	public boolean isMapping() {
		return importErrorType == ImportErrorType.MAPPING;
	}

	public boolean isStructure() {
		return importErrorType == ImportErrorType.STRUCTURE;
	}
}