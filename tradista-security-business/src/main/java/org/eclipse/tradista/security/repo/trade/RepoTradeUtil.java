package org.eclipse.tradista.security.repo.trade;

import static org.eclipse.tradista.core.pricing.util.PricerUtil.ONE_HUNDRED;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.Transfer.Type;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.bond.service.BondBusinessDelegate;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.RepoTrade;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public final class RepoTradeUtil {

	private static TransferBusinessDelegate transferBusinessDelegate = new TransferBusinessDelegate();

	private static BondBusinessDelegate bondBusinessDelegate = new BondBusinessDelegate();

	private static EquityBusinessDelegate equityBusinessDelegate = new EquityBusinessDelegate();

	private static BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

	private static QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();

	private RepoTradeUtil() {
	}

	public static Map<Security, Map<Book, BigDecimal>> getAllocatedCollateral(RepoTrade trade)
			throws TradistaBusinessException {
		Map<Security, Map<Book, BigDecimal>> securities = null;
		List<Transfer> givenCollateral = null;

		try {
			givenCollateral = transferBusinessDelegate.getTransfers(Type.PRODUCT, Transfer.Status.KNOWN, Direction.PAY,
					TransferPurpose.COLLATERAL_SETTLEMENT, trade.getId(), 0, 0, 0, null, null, null, null, null, null);
		} catch (TradistaBusinessException _) {
			// Not expected here.
		}

		if (givenCollateral != null && !givenCollateral.isEmpty()) {
			givenCollateral = givenCollateral.stream()
					.filter(t -> t.getSettlementDate() == null || t.getSettlementDate().isBefore(LocalDate.now())
							|| t.getSettlementDate().isEqual(LocalDate.now()))
					.toList();
			securities = HashMap.newHashMap(givenCollateral.size());
			for (Transfer t : givenCollateral) {
				if (securities.containsKey(t.getProduct())) {
					Map<Book, BigDecimal> bookMap = securities.get(t.getProduct());
					BigDecimal newQty = bookMap.get(trade.getBook()).add(((ProductTransfer) t).getQuantity());
					bookMap.put(trade.getBook(), newQty);
					securities.put((Security) t.getProduct(), bookMap);
				} else {
					Map<Book, BigDecimal> bookMap = new HashMap<>();
					BigDecimal newQty = ((ProductTransfer) t).getQuantity();
					bookMap.put(trade.getBook(), newQty);
					securities.put((Security) t.getProduct(), bookMap);
				}
			}
		}
		List<Transfer> returnedCollateral = null;
		try {
			returnedCollateral = transferBusinessDelegate.getTransfers(Type.PRODUCT, Transfer.Status.KNOWN,
					Direction.RECEIVE, TransferPurpose.RETURNED_COLLATERAL, trade.getId(), 0, 0, 0, null, null, null,
					null, null, null);
		} catch (TradistaBusinessException _) {
			// Not expected here.
		}
		if (returnedCollateral != null && !returnedCollateral.isEmpty()) {
			returnedCollateral = returnedCollateral.stream()
					.filter(t -> t.getSettlementDate() == null || t.getSettlementDate().isBefore(LocalDate.now())
							|| t.getSettlementDate().isEqual(LocalDate.now()))
					.toList();
			if (!returnedCollateral.isEmpty()) {
				if (securities == null) {
					securities = HashMap.newHashMap(returnedCollateral.size());
				}
				for (Transfer t : returnedCollateral) {
					if (securities.containsKey(t.getProduct())) {
						Map<Book, BigDecimal> bookMap = securities.get(t.getProduct());
						BigDecimal newQty = bookMap.get(trade.getBook()).subtract(((ProductTransfer) t).getQuantity());
						bookMap.put(trade.getBook(), newQty);
						securities.put((Security) t.getProduct(), bookMap);
					}
				}
			}
		}

		return securities;
	}

	public void checkCollateralConsistency(RepoTrade trade) throws TradistaBusinessException {
		// Checking business consistency of collateral to add
		StringBuilder errMsg = new StringBuilder();
		if (trade.getCollateralToAdd() != null && !trade.getCollateralToAdd().isEmpty()) {
			for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToAdd().entrySet()) {
				// 1. Security must exist
				Security secInDb = bondBusinessDelegate.getBondById(entry.getKey().getId());
				if (secInDb == null) {
					secInDb = equityBusinessDelegate.getEquityById(entry.getKey().getId());
				}
				if (secInDb == null) {
					errMsg.append(String.format(
							"The security %s cannot be found in the system, it cannot be added as collateral.%n",
							entry.getKey()));
					continue;
				}
				if (trade instanceof GCRepoTrade gcRepoTrade) {
					// 2. (GC Repos only) Security must be part of the GC Basket
					if (!gcRepoTrade.getGcBasket().getSecurities().contains(entry.getKey())) {
						errMsg.append(String.format(
								"The security %s cannot be found in the GC Basket %s, it cannot be added as collateral.%n",
								entry.getKey(), gcRepoTrade.getGcBasket().getName()));
						continue;
					}
				}
				// 3. Books should exist
				Map<Book, BigDecimal> bookMap = entry.getValue();
				for (Map.Entry<Book, BigDecimal> bookEntry : bookMap.entrySet()) {
					Book bookInDb = bookBusinessDelegate.getBookById(bookEntry.getKey().getId());
					if (bookInDb == null) {
						errMsg.append(String.format(
								"The origin book %s cannot be found in the system, it cannot be used as collateral source.%n",
								bookEntry.getKey().getName()));
					}
				}
			}

		}

		// Checking business consistency of collateral to remove
		if (trade.getCollateralToRemove() != null && !trade.getCollateralToRemove().isEmpty()) {
			for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToRemove().entrySet()) {
				// 1. Security must exist
				Security secInDb = bondBusinessDelegate.getBondById(entry.getKey().getId());
				if (secInDb == null) {
					secInDb = equityBusinessDelegate.getEquityById(entry.getKey().getId());
				}
				if (secInDb == null) {
					errMsg.append(String.format(
							"The security %s cannot be found in the system, it cannot be removed from collateral.%n",
							entry.getKey()));
					continue;
				}
				// 2. Books should exist
				Map<Book, BigDecimal> bookMap = entry.getValue();
				for (Map.Entry<Book, BigDecimal> bookEntry : bookMap.entrySet()) {
					Book bookInDb = bookBusinessDelegate.getBookById(bookEntry.getKey().getId());
					if (bookInDb == null) {
						errMsg.append(String.format(
								"The book %s cannot be found in the system, it cannot be used as collateral source.%n",
								bookEntry.getKey().getName()));
					}
				}
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	public static BigDecimal getClosingLegPayment(RepoTrade trade, long quoteSetId, DayCountConvention dcc)
			throws TradistaBusinessException {
		BigDecimal amount = trade.getAmount();
		BigDecimal repoRate;
		if (trade.isFixedRepoRate()) {
			repoRate = PricerUtil.divide(trade.getRepoRate(), ONE_HUNDRED);
		} else {

			String quoteName = Index.INDEX + "." + trade.getIndex() + "." + trade.getIndexTenor();
			Set<QuoteValue> quoteValues = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(
					quoteSetId, quoteName, QuoteType.INTEREST_RATE, trade.getSettlementDate(), trade.getEndDate());

			if (quoteValues == null || quoteValues.isEmpty()) {
				String errorMsg = String.format(
						"Floating rate cannot be determined. Impossible to get the %s index closing value between %tD and %tD in QuoteSet %s.",
						quoteName, trade.getSettlementDate(), trade.getEndDate(), quoteSetId);
				throw new TradistaBusinessException(errorMsg);
			}

			Map<LocalDate, QuoteValue> quoteValuesMap = quoteValues.stream()
					.collect(Collectors.toMap(QuoteValue::getDate, Function.identity()));

			List<LocalDate> dates = trade.getSettlementDate().datesUntil(trade.getEndDate()).toList();

			repoRate = BigDecimal.ZERO;

			StringBuilder errorMsg = new StringBuilder();
			for (LocalDate date : dates) {
				if (!quoteValuesMap.containsKey(date) || quoteValuesMap.get(date).getClose() == null) {
					errorMsg.append(String.format("%tD ", date));
				} else {
					repoRate = repoRate.add(quoteValuesMap.get(date).getClose());
				}
			}
			if (!errorMsg.isEmpty()) {
				errorMsg = new StringBuilder(String.format(
						"Floating rate cannot be determined. Impossible to get the %s index closing value in QuoteSet %s for dates : ",
						quoteName, quoteSetId)).append(errorMsg);
				throw new TradistaBusinessException(errorMsg.toString());
			}

			repoRate = PricerUtil.divide(repoRate, new BigDecimal(dates.size()));
			repoRate = repoRate.add(trade.getIndexOffset());
			repoRate = PricerUtil.divide(repoRate, ONE_HUNDRED);
		}

		// 3. Multiply notional by repo rate (applying the accrual factor) then add it
		// to the nominal amount to get the closing leg payment
		return amount.add(trade.getAmount().multiply(repoRate)
				.multiply(PricerUtil.daysToYear(dcc, trade.getSettlementDate(), trade.getEndDate())));
	}

}