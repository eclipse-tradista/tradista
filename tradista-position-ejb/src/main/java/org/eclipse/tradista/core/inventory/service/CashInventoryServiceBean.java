package org.eclipse.tradista.core.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.cashinventory.service.CashInventoryFilteringInterceptor;
import org.eclipse.tradista.core.cashinventory.service.CashInventoryService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.inventory.model.CashInventory;
import org.eclipse.tradista.core.inventory.persistence.CashInventorySQL;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Singleton
public class CashInventoryServiceBean implements CashInventoryService {

	@Override
	@Lock(LockType.WRITE)
	public void updateCashInventory(CashTransfer transfer) throws TradistaBusinessException {
		boolean foundOpenQuantity = false;
		LocalDate cashInventoryDate = transfer.getSettlementDate();
		boolean foundSameCashInventory = false;
		boolean cashInventoryFound = false;
		LocalDate firstCashInventoryFromDateAfterTradeSettlementDate = null;
		Set<CashInventory> cashInventoriesToBeSaved = new HashSet<CashInventory>();

		// 1. Gets the last inventory before the trade date for this currency
		// and
		// book
		CashInventory cashInventory = CashInventorySQL.getLastCashInventoryBeforeDateByCurrencyAndBookIds(
				transfer.getCurrency().getId(), transfer.getBook().getId(), transfer.getSettlementDate());
		if (cashInventory != null) {
			cashInventoryFound = true;
			if (cashInventory.getFrom().equals(transfer.getSettlementDate())) {
				foundSameCashInventory = true;
			}
			if (cashInventory.getTo() == null) {
				foundOpenQuantity = true;
			} else {
				cashInventoryDate = cashInventory.getTo();
			}
			if (foundSameCashInventory) {
				cashInventory.setAmount(cashInventory.getAmount().add(transfer.getAmount()));
			} else {
				CashInventory newCashInventory = new CashInventory(cashInventory.getCurrency(),
						transfer.getSettlementDate(), cashInventory.getBook());
				newCashInventory.setTo(cashInventory.getTo());
				newCashInventory.setAmount(cashInventory.getAmount().add(transfer.getAmount()));
				cashInventoriesToBeSaved.add(newCashInventory);

				cashInventory.setTo(transfer.getSettlementDate().minusDays(1));
			}
			cashInventoriesToBeSaved.add(cashInventory);
		}

		if (!foundOpenQuantity) {
			while (!foundOpenQuantity) {
				cashInventory = CashInventorySQL.getFirstCashInventoryAfterDateByCurrencyAndBookIds(
						transfer.getCurrency().getId(), transfer.getBook().getId(), cashInventoryDate);
				if (cashInventory != null) {
					if (firstCashInventoryFromDateAfterTradeSettlementDate == null) {
						firstCashInventoryFromDateAfterTradeSettlementDate = cashInventory.getFrom();
					}
					cashInventory.setAmount(cashInventory.getAmount().add(transfer.getAmount()));
					if (cashInventory.getTo() == null) {
						foundOpenQuantity = true;
					} else {
						cashInventoryDate = cashInventory.getTo().plusDays(1);
					}
					cashInventoriesToBeSaved.add(cashInventory);
				} else {
					foundOpenQuantity = true;
				}
			}

		}

		if (!cashInventoryFound) {
			CashInventory newCashInventory = new CashInventory(transfer.getCurrency(), transfer.getSettlementDate(),
					transfer.getBook());
			LocalDate to = null;
			if (firstCashInventoryFromDateAfterTradeSettlementDate != null) {
				to = firstCashInventoryFromDateAfterTradeSettlementDate.minusDays(1);
			}
			newCashInventory.setAmount(transfer.getAmount());
			newCashInventory.setTo(to);
			cashInventoriesToBeSaved.add(newCashInventory);
		}

		// Remove the inventories with quantity = 0 and To == null
		Set<Long> cashInventoryIdsToBeDeleted = new HashSet<Long>();
		Set<CashInventory> cashInventoriesToBeDeleted = new HashSet<CashInventory>();
		for (CashInventory inv : cashInventoriesToBeSaved) {
			if (inv.getAmount().signum() == 0 && inv.getTo() == null) {
				if (inv.getId() > 0) {
					cashInventoryIdsToBeDeleted.add(inv.getId());
				}
				cashInventoriesToBeDeleted.add(inv);
			}
		}

		cashInventoriesToBeSaved.removeAll(cashInventoriesToBeDeleted);

		if (!cashInventoryIdsToBeDeleted.isEmpty()) {
			CashInventorySQL.remove(cashInventoryIdsToBeDeleted);
		}

		CashInventorySQL.save(cashInventoriesToBeSaved);

		fusionContiguousCashInventories(transfer.getCurrency().getId(), transfer.getBook().getId());
	}

	private void fusionContiguousCashInventories(long currencyId, long bookId) {
		Map<LocalDate, CashInventory> cashInventories = new HashMap<LocalDate, CashInventory>();
		Set<CashInventory> cashInventoriesByCurrencyAndBookIds = CashInventorySQL
				.getCashInventoriesByCurrencyAndBookIds(currencyId, bookId);
		Set<CashInventory> cashInventoriesToBeSaved = new HashSet<CashInventory>();
		Set<Long> cashInventoryIdsToBeDeleted = new HashSet<Long>();

		if (cashInventoriesByCurrencyAndBookIds != null && !cashInventoriesByCurrencyAndBookIds.isEmpty()) {
			for (CashInventory inv : cashInventoriesByCurrencyAndBookIds) {
				cashInventories.put(inv.getTo(), inv);
			}

			for (CashInventory inv : cashInventoriesByCurrencyAndBookIds) {
				LocalDate key = inv.getFrom().minusDays(1);
				if (cashInventories.containsKey(key)
						&& inv.getAmount().compareTo(cashInventories.get(key).getAmount()) == 0) {
					CashInventory cashInvToBeUpdated = cashInventories.get(key);
					cashInvToBeUpdated.setTo(inv.getTo());
					cashInventoriesToBeSaved.add(cashInvToBeUpdated);
					cashInventoryIdsToBeDeleted.add(inv.getId());
				}
			}

			if (!cashInventoryIdsToBeDeleted.isEmpty()) {
				CashInventorySQL.remove(cashInventoryIdsToBeDeleted);
			}

			if (!cashInventoriesToBeSaved.isEmpty()) {
				CashInventorySQL.save(cashInventoriesToBeSaved);
			}
		}

	}

	@Override
	public Set<CashInventory> getCashInventoriesBeforeDateByCurrencyAndBookIds(long currencyId, long bookId,
			LocalDate date) {
		return CashInventorySQL.getCashInventoriesBeforeDateByCurrencyAndBookIds(currencyId, bookId, date);
	}

	@Override
	public Set<CashInventory> getOpenPositionsFromCashInventoryByCurrencyAndBookIds(long currencyId, long bookId) {
		return CashInventorySQL.getOpenPositionsFromCashInventoryByCurrencyAndBookIds(currencyId, bookId);
	}

	@Override
	public BigDecimal getAmountByDateCurrencyAndBookIds(long currencyId, long bookId, LocalDate date) {
		return CashInventorySQL.getAmountByDateCurrencyAndBookIds(currencyId, bookId, date);
	}

	@Interceptors(CashInventoryFilteringInterceptor.class)
	@Override
	public Set<CashInventory> getCashInventories(LocalDate from, LocalDate to, long currencyId, long bookId,
			boolean onlyOpenPositions) throws TradistaBusinessException {
		return CashInventorySQL.getCashInventories(from, to, currencyId, bookId, onlyOpenPositions);
	}

	@Override
	public Map<String, BigDecimal> getBookCashContent(LocalDate date, long bookId) {
		Set<Currency> currencies = CurrencySQL.getAllCurrencies();
		Map<String, BigDecimal> bookCashContent = null;
		if (currencies != null && !currencies.isEmpty()) {
			bookCashContent = new HashMap<String, BigDecimal>(currencies.size());
			for (Currency curr : currencies) {
				BigDecimal amount = CashInventorySQL.getAmountByDateCurrencyAndBookIds(curr.getId(), bookId, date);
				if (amount.signum() != 0) {
					bookCashContent.put(curr.getIsoCode(), amount);
				}
			}
		}
		return bookCashContent;
	}

}