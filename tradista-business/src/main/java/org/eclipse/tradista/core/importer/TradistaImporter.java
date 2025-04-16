package org.eclipse.tradista.core.importer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
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

	protected abstract void start();

	protected abstract String getProductType(X externalMessage);

	@Override
	public void importMessage(X externalMessage) throws TradistaBusinessException {
		IncomingMessage msg = null;
		try {
			msg = createMessage(externalMessage);
			validateMessage(externalMessage);
			Optional<? extends TradistaObject> object = processMessage(externalMessage);
			if (object.isPresent()) {
				saveObject(object.get());
				msg.setObjectId(object.get().getId());
			}
		} catch (TradistaBusinessException tbe) {
			ImportError importError = new ImportError();
			importError.setErrorDate(LocalDateTime.now());
			importError.setErrorMessage(tbe.getMessage());
			importError.setStatus(Status.UNSOLVED);
			importError.setMessage(msg);
			importErrorBusinessDelegate.saveImportError(importError);
		}
		if (msg != null) {
			messageBusinessDelegate.saveMessage(msg);
		}
	}

	protected IncomingMessage createMessage(X externalMessage) throws TradistaBusinessException {
		IncomingMessage message = new IncomingMessage();
		message.setContent(externalMessage.toString());
		LocalDateTime now = LocalDateTime.now();
		message.setCreationDateTime(now);
		message.setLastUpdateDateTime(now);
		message.setType(getType());
		message.setWorkflow(getType());
		message.setStatus(workflowBusinessDelegate.getInitialStatus(message.getWorkflow()));
		return message;
	}

	/**
	 * Checks the external message, ensuring it has a valid structure.
	 * 
	 * @param externalMessage the message to be imported in Eclipse Tradista
	 * @throws TradistaBusinessException if there was an error during the message
	 *                                   validation
	 */
	protected abstract void validateMessage(X externalMessage) throws TradistaBusinessException;

	/**
	 * Process the message, optionally creating an object in Eclipse Tradista
	 * 
	 * @param externalMessage the message to be imported in Eclipse Tradista
	 * @throws TradistaBusinessException if there was an error during the message
	 *                                   processing
	 */
	protected abstract Optional<? extends TradistaObject> processMessage(X externalMessage)
			throws TradistaBusinessException;

	protected abstract void saveObject(TradistaObject tradistaObject) throws TradistaBusinessException;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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