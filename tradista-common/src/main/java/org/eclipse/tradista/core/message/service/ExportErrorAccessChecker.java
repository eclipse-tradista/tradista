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

package org.eclipse.tradista.core.message.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.message.model.ExportError;

/**
 * access checker for export errors. Validates the linked message existence by
 * delegating to {@link MessageAccessChecker}.
 */
public class ExportErrorAccessChecker implements AccessChecker {

	private MessageAccessChecker messageAccessChecker;

	public ExportErrorAccessChecker() {
		messageAccessChecker = new MessageAccessChecker();
	}

	@Override
	public void check(Object value, StringBuilder errMsg) throws TradistaBusinessException {
		if (value instanceof ExportError exportError) {
			if (exportError.getMessage() != null && exportError.getMessage().getId() != 0) {
				messageAccessChecker.check(exportError.getMessage().getId(), errMsg);
			}
		}
	}

}