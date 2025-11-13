package org.eclipse.tradista.core.message.service;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.eclipse.tradista.core.user.model.User;
import org.springframework.util.CollectionUtils;

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

	private MessageBusinessDelegate messageBusinessDelegate;
	private TradeBusinessDelegate tradeBusinessDelegate;

	public MessageAuthorizationFilteringInterceptor() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		tradeBusinessDelegate = new TradeBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		Method method = ic.getMethod();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes[0].equals(Message.class)) {
			Message message = (Message) parameters[0];
			StringBuilder errMsg = new StringBuilder();
			if (message.getId() != 0) {
				List<Message> msgs = messageBusinessDelegate.getMessages(message.getId(), null, null, null, 0, null,
						null, null, null, null, null);
				if (!CollectionUtils.isEmpty(msgs)) {
					errMsg.append(String.format("The message %d was not found.%n", message.getId()));
				}
			}
			if (message.getObjectId() != 0) {
				if ("Trade".equals(message.getObjectType())) {
					Trade<?> trade = tradeBusinessDelegate.getTradeById(message.getObjectId());
					if (trade == null) {
						errMsg.append(String.format("The trade %d was not found.%n", message.getObjectId()));
					}
				}
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
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
						return (m.getObjectId() == 0) || (!"Trade".equals(m.getObjectType()))
								|| (tradeBusinessDelegate.getTradeById(m.getObjectId()).getBook().getProcessingOrg()
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