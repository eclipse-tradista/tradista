package org.eclipse.tradista.core.importer.model;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.message.model.IncomingMessage;

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

public interface Importer<X> extends Runnable {

	String getType();

	String getName();

	LegalEntity getProcessingOrg();

	/**
	 * Identifies the Processing Org involved in the processing of the external
	 * message. By default, returns the Importer's configured Processing Org.
	 * 
	 * @param externalMessage the external message being processed
	 * @return the ProcessingOrg associated with the message
	 * @throws TradistaBusinessException if the Processing Org cannot be identified
	 *                                   from the message
	 */
	default LegalEntity getMessageProcessingOrg(X externalMessage) throws TradistaBusinessException {
		return getProcessingOrg();
	}

	void importMessage(X externalMessage) throws TradistaBusinessException;

	/**
	 * Parse the message, optionally creating an object
	 * 
	 * @param externalMessage the message to be imported in Eclipse Tradista
	 * @throws TradistaBusinessException if there was an error during the message
	 *                                   parsing
	 */
	Optional<? extends TradistaObject> parseMessage(X externalMessage) throws TradistaBusinessException;

	/**
	 * Process the message, optionally creating an object and save it in Eclipse
	 * Tradista
	 * 
	 * @param externalMessage the message to be imported in Eclipse Tradista
	 * @param msg             the message object created in Eclipse Tradista to
	 *                        represent the imported message
	 * @return the message representing the importing message
	 * @throws TradistaBusinessException if there was an error during the message
	 *                                   processing
	 */
	default IncomingMessage processMessage(X externalMessage, IncomingMessage msg) throws TradistaBusinessException {
		return persistObject(externalMessage, msg, parseMessage(externalMessage));
	}

	IncomingMessage persistObject(X externalMessage, IncomingMessage msg, Optional<? extends TradistaObject> object)
			throws TradistaBusinessException;

	X buildMessage(String externalMessage);

	/**
	 * Checks the external message, ensuring it has a valid structure.
	 * 
	 * @param externalMessage the message to be imported in Eclipse Tradista
	 * @throws TradistaBusinessException if there was an error during the message
	 *                                   validation
	 */
	void validateMessage(X externalMessage) throws TradistaBusinessException;

}