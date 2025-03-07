package org.eclipse.tradista.security.equity.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;

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

public final class PricerEquityUtil {

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	public static BigDecimal discountDividends(PricingParameter params, EquityTrade trade, long curveId,
			LocalDate startDate, LocalDate endDate) throws PricerException, TradistaBusinessException {

		StringBuffer errMsg = new StringBuffer();

		if (trade == null) {
			errMsg.append(String.format("Trade is mandatory.%n"));
		}

		if (params == null) {
			errMsg.append("Pricing Parameters Set is mandatory.");
		} else {
			if (params.getQuoteSet() == null) {
				errMsg.append("Pricing Parameters Set quote set is mandatory.");
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		BigDecimal price = BigDecimal.ZERO;
		// 1. Generate the pending coupons
		List<CashFlow> pendingDividends = getPendingDividends(trade, startDate, endDate, params);
		// 2. discount them as of pricing date using the given curve
		PricerUtil.discountCashFlows(pendingDividends, startDate, curveId, null);
		// 3. Sum the value of the discounted cashflows.
		price = PricerUtil.getTotalFlowsAmount(pendingDividends, trade.getProduct().getDividendCurrency(), startDate,
				curveId, null);

		return price;
	}

	public static BigDecimal getEquityPrice(PricingParameter params, Equity equity, LocalDate date)
			throws PricerException, TradistaBusinessException {
		EquityTrade trade = new EquityTrade();

		trade.setQuantity(BigDecimal.valueOf(1));
		trade.setProduct(equity);

		// Filling the trade with (dummy) values needed by the trade validator.
		trade.setBook(new Book(StringUtils.EMPTY, null));
		trade.setCounterparty(new LegalEntity(StringUtils.EMPTY));
		trade.setAmount(BigDecimal.valueOf(1));
		trade.setTradeDate(LocalDate.now());
		trade.setSettlementDate(LocalDate.now());

		return new PricerMeasurePV().monteCarloSimulation(params, trade, equity.getCurrency(), date);

	}

	public static List<CashFlow> getPendingDividends(EquityTrade trade, LocalDate startDate, LocalDate endDate,
			PricingParameter params) throws TradistaBusinessException {

		StringBuffer errMsg = new StringBuffer();

		if (trade == null) {
			errMsg.append(String.format("Trade is mandatory.%n"));
		} else {
			if (trade.getProduct() == null) {
				errMsg.append(String.format("Trade %d has no equity. Equity is mandatory.%n", trade.getId()));
			} else {
				if (!trade.getProduct().isPayDividend()) {
					return null;
				}
			}
		}

		if (trade.isSell()) {
			return null;
		}

		if (params == null) {
			errMsg.append(String.format("Pricing Parameters Set is mandatory.%n"));
		} else {
			if (params.getQuoteSet() == null) {
				errMsg.append(String.format("Pricing Parameters Set %s has no quote set. Quote set is mandatory.",
						params.getName()));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		Equity equity = trade.getProduct();

		Tenor frequency = equity.getDividendFrequency();
		List<CashFlow> dividends = new ArrayList<CashFlow>();

		LocalDate activeFrom = equity.getActiveFrom();

		LocalDate cashFlowDate = activeFrom;

		LocalDate activeTo = equity.getActiveTo();

		// Retrieve the dividend yield from quotes
		String quoteName = Equity.EQUITY + "." + equity.getIsin() + "." + equity.getExchange();
		BigDecimal dividendYield = null;
		QuoteValue quoteValue = new QuoteBusinessDelegate().getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(
				params.getQuoteSet().getId(), quoteName, QuoteType.DIVIDEND_YIELD, startDate);

		if (quoteValue != null) {
			dividendYield = quoteValue.getLast();
			// If no closing value is found, we look for the last one
			if (dividendYield == null) {
				dividendYield = quoteValue.getClose();
			}
		}

		if (dividendYield == null) {
			throw new TradistaBusinessException(
					String.format("The dividend yield for this equity: %s was not found in %s or %s quotes as of %tD.",
							equity, QuoteValue.CLOSE, QuoteValue.LAST, startDate));
		}

		if (activeTo.isBefore(endDate)) {
			endDate = activeTo;
		}

		while (!cashFlowDate.isAfter(endDate)) {
			if (cashFlowDate.isAfter(activeFrom) && !cashFlowDate.isBefore(startDate)
					&& !cashFlowDate.isBefore(trade.getSettlementDate())) {
				CashFlow cashFlow = new CashFlow();
				cashFlow.setDate(cashFlowDate);
				cashFlow.setCurrency(equity.getDividendCurrency());
				// Calculate the expected equity price at dividend date
				try {
					cashFlow.setAmount(getEquityPrice(params, equity, cashFlowDate)
							.multiply(dividendYield.divide(BigDecimal.valueOf(100),
									configurationBusinessDelegate.getScale(),
									configurationBusinessDelegate.getRoundingMode()))
							.multiply(PricerUtil.daysToYear(equity.getDividendFrequency()))
							.multiply(trade.getQuantity()));
				} catch (PricerException pe) {
					throw new TradistaBusinessException(pe.getMessage());
				}
				if (trade.isBuy()) {
					cashFlow.setDirection(CashFlow.Direction.RECEIVE);
				} else {
					cashFlow.setDirection(CashFlow.Direction.PAY);
				}
				cashFlow.setPurpose(TransferPurpose.DIVIDEND);
				dividends.add(cashFlow);
			}

			cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);
		}
		return dividends;
	}

	/**
	 * Returns the list of received dividends for a given equity. Be sure to call
	 * this method when all the dividends you want to get are already known. There
	 * is no forecasting in this method.
	 * 
	 * @param equity       the concerned equity
	 * @param startDate    the start date
	 * @param endDate      the end date
	 * @param quoteSetName the quote set name
	 * @return the list of received dividends for a given equity
	 * @throws TradistaBusinessException
	 */
	public static List<CashFlow> getDividends(Equity equity, LocalDate startDate, LocalDate endDate, long quoteSetId)
			throws TradistaBusinessException {
		if (equity == null) {
			throw new TradistaBusinessException("The equity is mandatory.");
		}

		if (!equity.isPayDividend()) {
			return null;
		}

		if (startDate == null) {
			startDate = LocalDate.MIN;
		}

		if (endDate == null) {
			endDate = LocalDate.MAX;
		}

		Tenor frequency = equity.getDividendFrequency();
		List<CashFlow> dividends = new ArrayList<CashFlow>();

		if (equity.getActiveFrom().isAfter(startDate)) {
			startDate = equity.getActiveFrom();
		}

		LocalDate cashFlowDate = startDate;

		LocalDate activeTo = equity.getActiveTo();
		if (activeTo.isBefore(endDate)) {
			endDate = activeTo;
		}

		String quoteName = Equity.EQUITY + "." + equity.getIsin() + equity.getExchange();
		while (!cashFlowDate.isAfter(endDate)) {
			if (cashFlowDate.isAfter(startDate)) {
				CashFlow cashFlow = new CashFlow();
				cashFlow.setDate(cashFlowDate);
				cashFlow.setCurrency(equity.getDividendCurrency());

				BigDecimal equityPrice = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId,
						QuoteType.EQUITY_PRICE, QuoteValue.CLOSE, cashFlowDate);

				BigDecimal dividendYield = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId,
						QuoteType.DIVIDEND_YIELD, QuoteValue.CLOSE, cashFlowDate);

				BigDecimal dividend = equityPrice
						.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode())
						.multiply(dividendYield);

				cashFlow.setAmount(dividend);

				dividends.add(cashFlow);
			}

			cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);
		}
		return dividends;
	}

	public static BigDecimal getTotalDividendsAmount(Equity equity, LocalDate startDate, LocalDate endDate,
			long quoteSetId) throws TradistaBusinessException {
		if (!equity.isPayDividend()) {
			return BigDecimal.ZERO;
		}
		List<CashFlow> cfs = getDividends(equity, startDate, endDate, quoteSetId);
		return PricerUtil.getTotalFlowsAmount(cfs, null, null, 0, null);
	}

	public static List<CashFlow> generateCashFlows(EquityTrade trade, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		StringBuffer errMsg = new StringBuffer();

		if (trade == null) {
			errMsg.append(String.format("Trade is mandatory.%n"));
		}

		if (params == null) {
			errMsg.append(String.format("Pricing Parameters Set is mandatory.%n"));
		} else {
			if (params.getQuoteSet() == null) {
				errMsg.append("Pricing Parameters Set quote set is mandatory.");
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		if (pricingDate == null) {
			pricingDate = LocalDate.MIN;
		}

		List<CashFlow> cfs = new ArrayList<>();
		CashFlow payment = new CashFlow();

		if (!trade.getSettlementDate().isBefore(pricingDate)) {
			payment.setAmount(trade.getAmount().multiply(trade.getQuantity()));
			payment.setCurrency(trade.getCurrency());
			payment.setDate(trade.getSettlementDate());
			payment.setPurpose(TransferPurpose.EQUITY_PAYMENT);
			if (trade.isBuy()) {
				payment.setDirection(CashFlow.Direction.PAY);
			} else {
				payment.setDirection(CashFlow.Direction.RECEIVE);
			}

			cfs.add(payment);
		}

		if (trade.getProduct().isPayDividend()) {
			cfs.addAll(
					PricerEquityUtil.getPendingDividends(trade, pricingDate, trade.getProduct().getActiveTo(), params));
		}

		return cfs;
	}

}