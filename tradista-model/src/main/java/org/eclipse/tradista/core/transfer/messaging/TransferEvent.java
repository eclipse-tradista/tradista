package org.eclipse.tradista.core.transfer.messaging;

import org.eclipse.tradista.core.common.messaging.Event;
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

public abstract class TransferEvent<X extends Transfer> implements Event {

	private static final long serialVersionUID = -1640618458564189936L;

	private X transfer;

	private X oldTransfer;

	public X getTransfer() {
		return transfer;
	}

	public void setTransfer(X transfer) {
		this.transfer = transfer;
	}

	public X getOldTransfer() {
		return oldTransfer;
	}

	public void setOldTransfer(X transfer) {
		oldTransfer = transfer;
	}

}