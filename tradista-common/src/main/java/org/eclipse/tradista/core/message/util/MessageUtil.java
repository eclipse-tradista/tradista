package org.eclipse.tradista.core.message.util;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.trade.model.Trade;

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

	public enum ObjectTypes {
		TRADE;

		@Override
		public String toString() {
			return switch (this) {
			case TRADE -> "Trade";
			default -> super.toString();
			};
		}
	}

	private MessageUtil() {
	}

	public static String getObjectType(TradistaObject object) {
		if (object == null) {
			throw new TradistaTechnicalException("The object is mandatory to determine the message object type");
		}
		return switch (object) {
		case Trade<?> _ -> MessageUtil.ObjectTypes.TRADE.toString();
		default -> null;
		};
	}

}