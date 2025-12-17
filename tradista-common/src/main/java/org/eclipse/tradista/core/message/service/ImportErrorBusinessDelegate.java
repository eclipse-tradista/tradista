package org.eclipse.tradista.core.message.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.error.util.ErrorUtil;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.ImportError.ImportErrorType;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
import org.springframework.util.CollectionUtils;

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

	private MessageBusinessDelegate messageBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	public ImportErrorBusinessDelegate() {
		importErrorService = TradistaServiceLocator.getInstance().getImportErrorService();
		messageBusinessDelegate = new MessageBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
	}

	public long saveImportError(ImportError error) throws TradistaBusinessException {
		if (error == null) {
			throw new TradistaBusinessException("The import error cannot be null.");
		}
		return SecurityUtil.runEx(() -> importErrorService.saveImportError(error));
	}

	public List<ImportError> getImportErrors(Set<String> importerTypes, Set<String> importerNames, long messageId,
			Status status, ImportErrorType importErrorType, LocalDate errorDateFrom, LocalDate errorDateTo,
			LocalDate solvingDateFrom, LocalDate solvingDateTo) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (!CollectionUtils.isEmpty(importerTypes) && !CollectionUtils.isEmpty(importerNames)) {
			errMsg.append("You must select either Importer Types or Names but not both.");
		}
		ErrorUtil.checkErrorDates(errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo, errMsg);
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> importErrorService.getImportErrors(importerTypes, importerNames, messageId,
				status, importErrorType, errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo));
	}

	public void tryToSolve(ImportError error) throws TradistaBusinessException {
		if (error == null) {
			throw new TradistaBusinessException("The import error cannot be null.");
		}
		Message message = error.getMessage();
		if (message != null) {
			Set<String> availableActions = workflowBusinessDelegate.getAvailableActionsFromStatus(message.getWorkflow(),
					message.getStatus());
			// RETRY is for now treated as a special action. See it makes sense to make this
			// configurable.
			if (!CollectionUtils.isEmpty(availableActions) && availableActions.contains(ActionConstants.RETRY)) {
				messageBusinessDelegate.saveMessage(message, ActionConstants.RETRY);
			}
		}
	}

	public ImportError getImportError(long msgId, ImportErrorType importErrorType) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (msgId <= 0) {
			errMsg.append(String.format("The message id must be positive.%n"));
		}
		if (importErrorType == null) {
			errMsg.append("The import error type is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return importErrorService.getImportError(msgId, importErrorType);
	}

}