package org.eclipse.tradista.core.cashinventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.cashinventory.service.CashInventoryService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.inventory.model.CashInventory;
import org.eclipse.tradista.core.transfer.model.CashTransfer;

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

public class CashInventoryBusinessDelegate {

	private CashInventoryService cashInventoryService;

	public CashInventoryBusinessDelegate() {
		cashInventoryService = TradistaServiceLocator.getInstance().getCashInventoryService();
	}

	public void updateCashInventory(CashTransfer transfer) throws TradistaBusinessException {

		if (transfer == null) {
			throw new TradistaBusinessException("The CashTransfer cannot be null.");
		}

		StringBuffer errMsg = new StringBuffer();

		// TODO Should we validate the transfer here

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		SecurityUtil.runEx(() -> cashInventoryService.updateCashInventory(transfer));
	}

	public Set<CashInventory> getCashInventoriesBeforeDateByCurrencyAndBookIds(long currencyId, long bookId,
			LocalDate date) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}

		return SecurityUtil.run(
				() -> cashInventoryService.getCashInventoriesBeforeDateByCurrencyAndBookIds(currencyId, bookId, date));
	}

	public Set<CashInventory> getOpenPositionsFromCashInventoryByCurrencyAndBookIds(long currencyId, long bookId)
			throws TradistaBusinessException {
		return SecurityUtil.run(
				() -> cashInventoryService.getOpenPositionsFromCashInventoryByCurrencyAndBookIds(currencyId, bookId));
	}

	public BigDecimal getAmountByDateCurrencyAndBookIds(long currencyId, long bookId, LocalDate date)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}
		return SecurityUtil.run(() -> cashInventoryService.getAmountByDateCurrencyAndBookIds(currencyId, bookId, date));
	}

	public Set<CashInventory> getCashInventories(LocalDate from, LocalDate to, long currencyId, long bookId,
			boolean onlyOpenPositions) throws TradistaBusinessException {

		if (onlyOpenPositions && to != null) {
			throw new TradistaBusinessException(
					String.format("'Only Open positions' and 'To' cannot be selected together.%n"));
		}
		if (from != null && to != null) {
			if (to.isBefore(from)) {
				throw new TradistaBusinessException(String.format("'To' date cannot be before 'From' date.%n"));
			}
		}
		return SecurityUtil
				.runEx(() -> cashInventoryService.getCashInventories(from, to, currencyId, bookId, onlyOpenPositions));
	}

}