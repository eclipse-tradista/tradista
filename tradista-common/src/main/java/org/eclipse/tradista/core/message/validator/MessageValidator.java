package org.eclipse.tradista.core.message.validator;

import org.apache.commons.lang3.StringUtils;

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

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.message.model.Message;

public class MessageValidator {

	public void validateMessage(Message message) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (message == null) {
			throw new TradistaBusinessException("the message cannot be null.");
		}
		if (StringUtils.isBlank(message.getType())) {
			errMsg.append(String.format("the message type is mandatory.%n"));
		}
		if (message.getObjectType() == null && message.getObjectId() > 0) {
			errMsg.append(String.format("the message object type cannot be blank when the object id is positive.%n"));
		}
		if (message.getObjectType() != null && message.getObjectId() <= 0) {
			errMsg.append(String.format("the message object id should be positive when the object type is present.%n"));
		}
		if (message.getStatus() == null) {
			errMsg.append("The status is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
