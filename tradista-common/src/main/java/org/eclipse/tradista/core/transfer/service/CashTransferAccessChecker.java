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

package org.eclipse.tradista.core.transfer.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.transfer.model.Transfer;

/**
 * access checker for cash transfers.
 */
public class CashTransferAccessChecker implements AccessChecker {

	private TransferBusinessDelegate transferBusinessDelegate;

	public CashTransferAccessChecker() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@Override
	public void check(Object value, StringBuilder errMsg) throws TradistaBusinessException {
		if (value instanceof Long id) {
			if (id != 0) {
				Transfer transfer = transferBusinessDelegate.getTransferById(id);
				if (transfer == null || !transfer.isCash()) {
					errMsg.append(String.format("The Cash Transfer %d was not found.%n", id));
				}
			}
		}
	}

}