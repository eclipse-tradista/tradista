package org.eclipse.tradista.core.transfer.messaging;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

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

public class TransferEventReceiver {

	private TransferBusinessDelegate transferBusinessDelegate;

	@PostConstruct
	public void init() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@ServiceActivator(inputChannel = "transferQueue", poller = @Poller(fixedDelay = "${transfer.poller.delay:5000}", maxMessagesPerPoll = "${transfer.poller.max:20}"), adviceChain = {
			"dlqAdvice", "txAdvice" })
	public void processEvent(TradeEvent<?> event) throws TradistaBusinessException {
		transferBusinessDelegate.createTransfers(event);
	}
}