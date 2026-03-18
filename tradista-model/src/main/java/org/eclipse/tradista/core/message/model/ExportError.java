package org.eclipse.tradista.core.message.model;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class ExportError extends MessageError {

	private static final long serialVersionUID = -6633784657229214840L;

	public static final String EXPORT = "Export";

	public enum ExportErrorType {
		GENERATION, SENDING;

		@Override
		public String toString() {
			switch (this) {
			case GENERATION:
				return "Generation";
			case SENDING:
				return "Sending";
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
		public static ExportErrorType getType(String displayName) {
			switch (displayName) {
			case "Generation":
				return GENERATION;
			case "Sending":
				return SENDING;
			}
			return null;
		}
	}

	private ExportErrorType exportErrorType;

	@Override
	public String getType() {
		return EXPORT;
	}

	@Override
	public String getSubjectKey() {
		if (getMessage() != null) {
			return getType() + "-" + getMessage().getId();
		} else
			return null;
	}

	public ExportErrorType getExportErrorType() {
		return exportErrorType;
	}

	public void setExportErrorType(ExportErrorType exportErrorType) {
		this.exportErrorType = exportErrorType;
	}

	public boolean isGeneration() {
		return exportErrorType == ExportErrorType.GENERATION;
	}

	public boolean isSending() {
		return exportErrorType == ExportErrorType.SENDING;
	}
}