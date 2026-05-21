package org.eclipse.tradista.core.message.service;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.model.Message.ObjectType;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.eclipse.tradista.core.user.model.User;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class MessageAuthorizationFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private TradeBusinessDelegate tradeBusinessDelegate;

	public MessageAuthorizationFilteringInterceptor() {
		tradeBusinessDelegate = new TradeBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof List) {
				List<Message> messages = (List<Message>) value;
				value = messages.stream().filter(m -> {
					try {
						return (m.getObjectId() == 0) || (!ObjectType.TRADE.equals(m.getObjectType()))
								|| (tradeBusinessDelegate.getTradeById(m.getObjectId()).getProcessingOrg()
										.equals(user.getProcessingOrg()));
					} catch (TradistaBusinessException _) {
						return false;
					}
				}).toList();
			}
		}
		return value;
	}

}