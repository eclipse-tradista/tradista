package org.eclipse.tradista.core.cashinventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.inventory.model.CashInventory;
import org.eclipse.tradista.core.transfer.model.CashTransfer;

import jakarta.ejb.Remote;

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

@Remote
public interface CashInventoryService {

	void updateCashInventory(CashTransfer transfer) throws TradistaBusinessException;

	Set<CashInventory> getCashInventoriesBeforeDateByCurrencyAndBookIds(long currencyId, long bookId, LocalDate date);

	Set<CashInventory> getOpenPositionsFromCashInventoryByCurrencyAndBookIds(long currencyId, long bookId);

	BigDecimal getAmountByDateCurrencyAndBookIds(long currencyId, long bookId, LocalDate date);

	Set<CashInventory> getCashInventories(LocalDate from, LocalDate to, long currencyId, long bookId,
			boolean onlyOpenPositions) throws TradistaBusinessException;

	Map<String, BigDecimal> getBookCashContent(LocalDate date, long bookId);

}