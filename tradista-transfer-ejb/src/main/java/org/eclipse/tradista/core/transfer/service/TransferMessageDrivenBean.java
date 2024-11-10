package org.eclipse.tradista.core.transfer.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;

import jakarta.annotation.Resource;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

@MessageDriven(name = "TransferMessageDriveBean")
public class TransferMessageDrivenBean implements MessageListener {

	@Resource
	private MessageDrivenContext context;

	@Override
	public void onMessage(Message msg) {

		ObjectMessage objectMessage = (ObjectMessage) msg;
		try {
			TradeEvent<?> tradeEvent = (TradeEvent<?>) objectMessage.getObject();
			TransferBusinessDelegate transferBusinessDelegate = new TransferBusinessDelegate();
			transferBusinessDelegate.createTransfers(tradeEvent);
		} catch (JMSException | TradistaBusinessException e) {
			e.printStackTrace();
			context.setRollbackOnly();
		}

	}

}