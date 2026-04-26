package org.eclipse.tradista.core.exporter;

import java.time.LocalDateTime;
import java.util.Objects;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.exporter.model.Exporter;
import org.eclipse.tradista.core.exporter.model.OutgoingMessageManager;
import org.eclipse.tradista.core.exporter.util.TradistaExporterUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.eclipse.tradista.core.message.model.OutgoingMessage;
import org.eclipse.tradista.core.message.service.ExportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
import org.eclipse.tradista.core.message.util.MessageUtil;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;

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

public abstract class TradistaExporter<X extends TradistaObject, Y> implements Exporter<X, Y> {

	private MessageBusinessDelegate messageBusinessDelegate;

	private ExportErrorBusinessDelegate exportErrorBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	protected TradistaExporter() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		exportErrorBusinessDelegate = new ExportErrorBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
	}

	private String name;

	private LegalEntity processingOrg;

	protected abstract void start();

	protected abstract String getProductType(X object);

	@Override
	public void exportObject(X object) throws TradistaBusinessException {
		OutgoingMessage msg = null;
		try {
			msg = createMessage(object);
			msg.setId(messageBusinessDelegate.saveMessage(msg));
			msg = (OutgoingMessage) messageBusinessDelegate.applyAction(msg, ActionConstants.GENERATE);
			if (msg.getStatus().toString().equals(StatusConstants.GENERATED)) {
				// At this stage, the message is considered to have been properly generated
				msg = (OutgoingMessage) messageBusinessDelegate.applyAction(msg, ActionConstants.SEND);
				if (msg.getStatus().toString().equals(StatusConstants.SENT)) {
					messageBusinessDelegate.saveMessage(msg);
				} else {
					handleError(msg, name);
				}
			} else {
				handleError(msg, name);
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			handleError(msg, te.getMessage());
		}
	}

	/**
	 * Generates an export error for the given outgoing message. Save the message if
	 * it was not previously saved.
	 * 
	 * @param msg    the outgoing message for which there is an error
	 * @param errMsg the error message
	 * @throws TradistaBusinessException in case of error during the message or
	 *                                   error saving
	 */
	public void handleError(OutgoingMessage msg, String errMsg) throws TradistaBusinessException {
		final String actionToApply = msg.getStatus().getName().equals(StatusConstants.TO_BE_GENERATED)
				? ActionConstants.GENERATION_FAILED
				: ActionConstants.SENDING_FAILED;
		msg = (OutgoingMessage) messageBusinessDelegate.applyAction(msg, actionToApply);
		ExportErrorType errorType = ExportErrorType.SENDING;
		if (actionToApply.equals(ActionConstants.GENERATION_FAILED)) {
			errorType = ExportErrorType.GENERATION;
		}
		ExportError exportError = exportErrorBusinessDelegate.getExportError(msg.getId(), errorType);
		if (exportError == null) {
			exportError = new ExportError();
			exportError.setErrorDate(LocalDateTime.now());
			exportError.setErrorMessage(errMsg);
			exportError.setStatus(Status.UNSOLVED);
			exportError.setExportErrorType(errorType);
		}
		// We save the message
		msg.setId(messageBusinessDelegate.saveMessage(msg));
		exportError.setMessage(msg);
		exportErrorBusinessDelegate.saveExportError(exportError);
	}

	public OutgoingMessage createMessage(X object) throws TradistaBusinessException {

		OutgoingMessage message = new OutgoingMessage.Builder().type(getType()).objectId(object.getId())
				.objectType(MessageUtil.getObjectType(object)).interfaceName(getName()).build();

		String workflowName = workflowBusinessDelegate.resolveWorkflow(message);
		org.eclipse.tradista.core.workflow.model.Status initialStatus = workflowBusinessDelegate
				.getInitialStatus(workflowName);

		message = message.toBuilder().status(initialStatus).build();

		return (OutgoingMessage) messageBusinessDelegate.applyAction(message, ActionConstants.NEW);
	}

	@SuppressWarnings("unchecked")
	public OutgoingMessageManager<Y, X> getOutgoingMessageManager(X tradistaObject, StringBuilder errMsg) {
		// 1. Get product type from message
		String productType = getProductType(tradistaObject);
		// 2. Get the incoming message manager for this product type
		if (productType != null) {
			try {
				return (OutgoingMessageManager<Y, X>) TradistaExporterUtil.getOutgoingMessageManager(productType,
						getType());
			} catch (TradistaBusinessException _) {
				errMsg.append(String.format(
						"Outgoing Message Manager could not be found for product type %s and message type %s",
						productType, getType()));
			}
		}
		return null;
	}

	public OutgoingMessageManager<Y, X> getOutgoingMessageManager(X tradistaObject) {
		return getOutgoingMessageManager(tradistaObject, new StringBuilder());
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

	/**
	 * Identifies the Processing Org involved in the export of the given object.
	 * By default, returns the Exporter's configured Processing Org.
	 * Subclasses should override this method if they need to determine the PO 
	 * dynamically from the object itself (e.g., from a Trade's Book).
	 * 
	 * @param object the Tradista object being exported
	 * @return the ProcessingOrg associated with the object
	 * @throws TradistaBusinessException if the Processing Org cannot be identified
	 */
	@Override
	public LegalEntity getObjectProcessingOrg(X object) throws TradistaBusinessException {
		if (getProcessingOrg() != null) {
			return getProcessingOrg();
		}
		throw new TradistaBusinessException(String.format(
				"Exporter %s is configured globally (null ProcessingOrg). "
				+ "It must override getObjectProcessingOrg to determine the context "
				+ "from the object %s.",
				getName(), object));
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
		TradistaExporter<?, ?> other = (TradistaExporter<?, ?>) obj;
		return Objects.equals(name, other.name);
	}

}