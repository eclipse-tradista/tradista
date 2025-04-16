package org.eclipse.tradista.core.message.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.messsage.service.ImportErrorService;

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

public class ImportErrorBusinessDelegate {

	private ImportErrorService importErrorService;

	public ImportErrorBusinessDelegate() {
		importErrorService = TradistaServiceLocator.getInstance().getImportErrorService();
	}

	public long saveImportError(ImportError error) throws TradistaBusinessException {
		if (error == null) {
			throw new TradistaBusinessException("The import error cannot be null.");
		}
		return SecurityUtil.runEx(() -> importErrorService.saveImportError(error));
	}

}