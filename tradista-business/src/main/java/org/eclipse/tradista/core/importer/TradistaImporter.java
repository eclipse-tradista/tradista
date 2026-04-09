package org.eclipse.tradista.core.importer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.importer.model.IncomingMessageManager;
import org.eclipse.tradista.core.importer.util.TradistaImporterUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.ImportError.ImportErrorType;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
import org.eclipse.tradista.core.message.util.MessageUtil;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;

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

public abstract class TradistaImporter<X> implements Importer<X> {

	private MessageBusinessDelegate messageBusinessDelegate;

	private ImportErrorBusinessDelegate importErrorBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	protected TradistaImporter() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		importErrorBusinessDelegate = new ImportErrorBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
	}

	private String name;

	private LegalEntity processingOrg;

	protected abstract void start();

	protected abstract String getProductType(X externalMessage);

	@Override
	public void importMessage(X externalMessage) throws TradistaBusinessException {
		IncomingMessage msg = null;
		try {
			msg = createMessage(externalMessage);
			msg.setId(messageBusinessDelegate.saveMessage(msg));
			msg = (IncomingMessage) messageBusinessDelegate.applyAction(msg, ActionConstants.VALIDATE);
			if (msg.getStatus().toString().equals(StatusConstants.VALIDATED)) {
				// At this stage, the message is considered to have been validated
				msg = (IncomingMessage) messageBusinessDelegate.applyAction(msg, ActionConstants.PROCESS);
				if (msg.getStatus().toString().equals(StatusConstants.PROCESSED)) {
					messageBusinessDelegate.saveMessage(msg);
				} else {
					handleError(msg, name);
				}
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			handleError(msg, te.getMessage());
		}
	}

	/**
	 * Creates or updates an import error for the given incoming message and save
	 * it.
	 * 
	 * @param msg    the incoming message for which there is an error
	 * @param errMsg the error message
	 * @throws TradistaBusinessException in case of error during the message or
	 *                                   error saving
	 */
	public void handleError(IncomingMessage msg, String errMsg) throws TradistaBusinessException {
		final String actionToApply = msg.getStatus().getName().equals(StatusConstants.TO_BE_VALIDATED)
				? ActionConstants.VALIDATION_FAILED
				: ActionConstants.PROCESS_FAILED;
		msg = (IncomingMessage) messageBusinessDelegate.applyAction(msg, actionToApply);
		ImportErrorType errorType = ImportErrorType.STRUCTURE;
		if (!msg.getStatus().toString().equals(StatusConstants.VALIDATION_KO)) {
			errorType = ImportErrorType.MAPPING;
		}
		ImportError importError = importErrorBusinessDelegate.getImportError(msg.getId(), errorType);
		if (importError == null) {
			importError = new ImportError();
			importError.setErrorDate(LocalDateTime.now());
			importError.setErrorMessage(errMsg);
			importError.setStatus(Status.UNSOLVED);
			importError.setMessage(msg);
			importError.setImportErrorType(errorType);
		}
		// We save the message
		msg.setId(messageBusinessDelegate.saveMessage(msg));
		importError.setMessage(msg);
		importErrorBusinessDelegate.saveImportError(importError);
	}

	@Override
	public IncomingMessage persistObject(X externalMessage, IncomingMessage msg,
			Optional<? extends TradistaObject> object) throws TradistaBusinessException {
		if (object.isEmpty()) {
			return msg;
		}
		IncomingMessageManager<X, TradistaObject> incomingMessageManager = getIncomingMessageManager(externalMessage);
		object.get().setId(incomingMessageManager.saveObject(object.get()));

		return msg.toBuilder().objectId(object.get().getId()).objectType(MessageUtil.getObjectType(object.get()))
				.build();
	}

	public IncomingMessage createMessage(X externalMessage) throws TradistaBusinessException {
		IncomingMessage message = new IncomingMessage.Builder().content(externalMessage.toString()).type(getType())
				.interfaceName(getName()).build();

		String workflowName = workflowBusinessDelegate.resolveWorkflow(message);
		org.eclipse.tradista.core.workflow.model.Status initialStatus = workflowBusinessDelegate
				.getInitialStatus(workflowName);

		message = message.toBuilder().status(initialStatus).build();
		return (IncomingMessage) messageBusinessDelegate.applyAction(message, ActionConstants.NEW);
	}

	@SuppressWarnings("unchecked")
	public IncomingMessageManager<X, TradistaObject> getIncomingMessageManager(X externalMessage,
			StringBuilder errMsg) {
		// 1. Get product type from the message
		String productType = getProductType(externalMessage);
		// 2. Get the incoming message manager for this product type
		if (productType != null) {
			try {
				return (IncomingMessageManager<X, TradistaObject>) TradistaImporterUtil
						.getIncomingMessageManager(productType, getType());
			} catch (TradistaBusinessException _) {
				errMsg.append(String.format(
						"Incoming Message Manager could not be found for product type %s and message type %s",
						productType, getType()));
			}
		}
		return null;
	}

	public IncomingMessageManager<X, TradistaObject> getIncomingMessageManager(X externalMessage) {
		return getIncomingMessageManager(externalMessage, new StringBuilder());
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public LegalEntity getProcessingOrg() {
		return processingOrg;
	}

	public void setProcessingOrg(LegalEntity processingOrg) {
		this.processingOrg = processingOrg;
	}

	@Override
	public void run() {
		start();
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TradistaImporter<?> other = (TradistaImporter<?>) obj;
		return Objects.equals(name, other.name);
	}

}