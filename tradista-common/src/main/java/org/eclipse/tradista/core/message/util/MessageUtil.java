package org.eclipse.tradista.core.message.util;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.common.util.TradistaConstants;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.model.OutgoingMessage;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;

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

public final class MessageUtil {

	private static TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();

	private MessageUtil() {
	}

	public static Message.ObjectType getObjectType(TradistaObject object) {
		if (object == null) {
			throw new TradistaTechnicalException("The object is mandatory to determine the message object type");
		}
		return switch (object) {
		case Trade<?> _ -> Message.ObjectType.TRADE;
		default -> null;
		};
	}

	public static TradistaObject loadObject(long objectId, Message.ObjectType objectType)
			throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (objectId <= 0) {
			errorMessage.append(String.format("The object id (%d) must be positive.%n", objectId));
		}
		if (objectType == null) {
			errorMessage.append("The object type is mandatory.");
		}
		if (!errorMessage.isEmpty()) {
			throw new TradistaBusinessException(errorMessage.toString());
		}
		if (objectType.equals(Message.ObjectType.TRADE)) {
			return tradeBusinessDelegate.getTradeById(objectId);
		}
		return null;
	}

	public static String getMessageManagerClassName(String productType, String messageType, boolean isIncoming)
			throws TradistaBusinessException {
		String direction = isIncoming ? IncomingMessage.INCOMING : OutgoingMessage.OUTGOING;
		return TradistaConstants.TRADISTA_PACKAGE + "." + new ProductBusinessDelegate().getProductFamily(productType)
				+ "." + productType.toLowerCase() + "." + messageType.toLowerCase() + "." + productType + messageType
				+ direction + "MessageManager";
	}

}