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
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
import org.springframework.util.CollectionUtils;

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

public class ExportErrorBusinessDelegate {

	private ExportErrorService exportErrorService;

	private MessageBusinessDelegate messageBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	public ExportErrorBusinessDelegate() {
		exportErrorService = TradistaServiceLocator.getInstance().getExportErrorService();
		messageBusinessDelegate = new MessageBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
	}

	public long saveExportError(ExportError error) throws TradistaBusinessException {
		if (error == null) {
			throw new TradistaBusinessException("The export error cannot be null.");
		}
		return SecurityUtil.runEx(() -> exportErrorService.saveExportError(error));
	}

	public List<ExportError> getExportErrors(Set<String> exporterTypes, Set<String> exporterNames, long messageId,
			Status status, ExportErrorType exportErrorType, LocalDate errorDateFrom, LocalDate errorDateTo,
			LocalDate solvingDateFrom, LocalDate solvingDateTo) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (!CollectionUtils.isEmpty(exporterTypes) && !CollectionUtils.isEmpty(exporterNames)) {
			errMsg.append("You must select either Exporter Types or Names but not both.");
		}
		ErrorUtil.checkErrorDates(errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo, errMsg);
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> exportErrorService.getExportErrors(exporterTypes, exporterNames, messageId,
				status, exportErrorType, errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo));
	}

	public void tryToSolve(ExportError error) throws TradistaBusinessException {
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

	public ExportError getExportError(long msgId, ExportErrorType exportErrorType) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (msgId <= 0) {
			errMsg.append(String.format("The message id must be positive.%n"));
		}
		if (exportErrorType == null) {
			errMsg.append("The export error type is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return exportErrorService.getExportError(msgId, exportErrorType);
	}

}