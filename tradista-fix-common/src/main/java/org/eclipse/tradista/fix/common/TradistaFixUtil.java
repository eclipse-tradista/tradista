package org.eclipse.tradista.fix.common;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

import quickfix.InvalidMessage;
import quickfix.MessageUtils;
import quickfix.Session;

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

public final class TradistaFixUtil {

	private TradistaFixUtil() {
		/* This utility class should not be instantiated */
	}

	public static Object buildMessage(Session session, String externalMessage) {
		StringBuilder errMsg = new StringBuilder();
		if (session == null) {
			errMsg.append(String.format("The FIX session is mandatory.%n"));
		}
		if (StringUtils.isBlank(externalMessage)) {
			errMsg.append("The message is mandatory");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		try {
			return MessageUtils.parse(session.getMessageFactory(), session.getDataDictionary(),
					session.getValidationSettings(), externalMessage, session.isValidateChecksum());
		} catch (InvalidMessage im) {
			throw new TradistaTechnicalException(im.getMessage());
		}
	}

}
