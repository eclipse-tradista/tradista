package org.eclipse.tradista.core.inventory.messaging;

import org.eclipse.tradista.core.cashinventory.service.CashInventoryBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.transfer.messaging.CashTransferEvent;
import org.eclipse.tradista.core.transfer.messaging.ProductTransferEvent;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;

import jakarta.annotation.PostConstruct;

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

public class InventoryEventReceiver {

	private CashInventoryBusinessDelegate cashInventoryBusinessDelegate;
	private ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	@PostConstruct
	public void init() {
		cashInventoryBusinessDelegate = new CashInventoryBusinessDelegate();
		productInventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
	}

	@ServiceActivator(inputChannel = "cashInventoryQueue", poller = @Poller(fixedDelay = "${cashInventory.poller.delay:5000}", maxMessagesPerPoll = "${cashInventory.poller.max:20}"), adviceChain = {
			"dlqAdvice", "txAdvice" })
	public synchronized void processCashTransferEvent(CashTransferEvent event) throws TradistaBusinessException {
		if (event.getOldTransfer() != null) {
			if (event.getOldTransfer().getDirection().equals(Transfer.Direction.RECEIVE)) {
				event.getOldTransfer().setAmount(event.getOldTransfer().getAmount().negate());
			}
			cashInventoryBusinessDelegate.updateCashInventory(event.getOldTransfer());
		}

		if (event.getTransfer() != null) {
			if (event.getTransfer().getDirection().equals(Transfer.Direction.PAY)) {
				event.getTransfer().setAmount(event.getTransfer().getAmount().negate());
			}
			cashInventoryBusinessDelegate.updateCashInventory(event.getTransfer());
		}
	}

	@ServiceActivator(inputChannel = "productInventoryQueue", poller = @Poller(fixedDelay = "${productInventory.poller.delay:5000}", maxMessagesPerPoll = "${productInventory.poller.max:20}"), adviceChain = {
			"dlqAdvice", "txAdvice" })
	public synchronized void processProductTransferEvent(ProductTransferEvent event) throws TradistaBusinessException {
		if (event.getOldTransfer() != null) {
			if (event.getOldTransfer().getDirection().equals(Transfer.Direction.RECEIVE)) {
				event.getOldTransfer().setQuantity(event.getOldTransfer().getQuantity().negate());
			}
			productInventoryBusinessDelegate.updateProductInventory(event.getOldTransfer());
		}

		if (event.getTransfer() != null) {
			if (event.getTransfer().getDirection().equals(Transfer.Direction.PAY)) {
				event.getTransfer().setQuantity(event.getTransfer().getQuantity().negate());
			}
			productInventoryBusinessDelegate.updateProductInventory(event.getTransfer());
		}
	}
}