package org.eclipse.tradista.core.inventory.service;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;

import org.eclipse.tradista.core.cashinventory.service.CashInventoryService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.messaging.CashTransferEvent;
import org.eclipse.tradista.core.transfer.model.Transfer;

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

@MessageDriven(name = "CashInventoryMessageDriveBean")
public class CashInventoryMessageDrivenBean implements MessageListener {

	@Resource
	private MessageDrivenContext context;

	@EJB
	private CashInventoryService cashInventoryService;

	@Override
	public void onMessage(Message msg) {

		ObjectMessage objectMessage = (ObjectMessage) msg;
		try {
			CashTransferEvent event = (CashTransferEvent) objectMessage.getObject();

			// Important to have a class level lock here, otherwise deadlock can occur.
			synchronized (this.getClass()) {
				// If there was already a transfer, first we erase the trace of this
				// old transfer in
				// the inventory
				// Note: old transfer is not null only when it was KNOWN
				if (event.getOldTransfer() != null) {
					if (event.getOldTransfer().getDirection().equals(Transfer.Direction.RECEIVE)) {
						event.getOldTransfer().setAmount(event.getOldTransfer().getAmount().negate());
					}
					cashInventoryService.updateCashInventory(event.getOldTransfer());
				}

				// Note: transfer is not null only when it is KNOWN
				if (event.getTransfer() != null) {
					if (event.getTransfer().getDirection().equals(Transfer.Direction.PAY)) {
						event.getTransfer().setAmount(event.getTransfer().getAmount().negate());
					}
					cashInventoryService.updateCashInventory(event.getTransfer());
				}
			}

		} catch (JMSException | TradistaBusinessException e) {
			e.printStackTrace();
			context.setRollbackOnly();
		}

	}

}