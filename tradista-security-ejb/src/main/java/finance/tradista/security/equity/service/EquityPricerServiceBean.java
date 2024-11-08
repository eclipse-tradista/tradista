package org.eclipse.tradista.security.equity.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationService;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.pricer.PricerEquityUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Stateless
@Interceptors(EquityProductScopeFilteringInterceptor.class)
@PermitAll
@SecurityDomain(value = "other")
public class EquityPricerServiceBean implements EquityPricerService {

	@EJB
	private ConfigurationService configurationService;

	@Override
	public BigDecimal pvMonteCarloSimulation(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getActiveTo())
				|| !pricingDate.isBefore(trade.getProduct().getActiveTo())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (pricingDate.isBefore(LocalDate.now())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		CurrencyPair pair = new CurrencyPair(trade.getProduct().getCurrency(), currency);
		FXCurve paramEquityCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramEquityCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		BigDecimal expectReturn = expectedReturnCapm(params, trade, currency, pricingDate);

		String quoteName = Equity.EQUITY + "." + trade.getProduct().getIsin() + "." + trade.getProduct().getExchange();
		BigDecimal volatility = null;
		BigDecimal s = null;
		List<QuoteValue> quoteValues = new QuoteBusinessDelegate()
				.getQuoteValuesByQuoteSetIdQuoteNameAndDate(params.getQuoteSet().getId(), quoteName, LocalDate.now());

		if (quoteValues != null) {
			for (QuoteValue value : quoteValues) {
				// This is the historical volatility of this equity.
				if (value.getQuote().getType().equals(QuoteType.VOLATILITY)) {
					volatility = value.getClose();
					if (volatility == null) {
						volatility = value.getLast();
					}
				}
				if (value.getQuote().getType().equals(QuoteType.EQUITY_PRICE)) {
					s = value.getClose();
					if (s == null) {
						s = value.getLast();
					}
				}
				if (s != null && volatility != null) {
					break;
				}
			}
		}

		if (volatility == null) {
			throw new TradistaBusinessException(String.format(
					"The '%s' (%s and %s) quote value of %s cannot be found as of date %tD", QuoteType.VOLATILITY,
					QuoteValue.CLOSE, QuoteValue.LAST, trade.getProduct(), LocalDate.now()));
		}
		if (s == null) {
			throw new TradistaBusinessException(String.format(
					"The '%s' (%s and %s) quote of %s cannot be found as of date %tD", QuoteType.EQUITY_PRICE,
					QuoteValue.CLOSE, QuoteValue.LAST, trade.getProduct(), LocalDate.now()));

		}

		LocalDate startDate = LocalDate.now();
		LocalDate endDate = pricingDate;

		BigDecimal deltaT;
		BigDecimal sPrice = s;
		BigDecimal sPriceAllSimulations = BigDecimal.ZERO;
		Random random = new Random();
		int simulationsCount = 100;
		String simulationsCountString = params.getParams().get("MonteCarloSimulationsCount");
		if (!StringUtils.isEmpty(simulationsCountString)) {
			try {
				simulationsCount = Integer.parseInt(simulationsCountString);
			} catch (NumberFormatException nfe) {
				throw new TradistaBusinessException(String
						.format("The MonteCarloSimulationsCount value is not correct: %s", simulationsCountString));
			}
		}

		if (simulationsCount <= 0) {
			throw new TradistaBusinessException(
					String.format("The MonteCarloSimulationsCount value must be positive: %s", simulationsCountString));
		}

		for (int i = 1; i <= simulationsCount; i++) {
			s = sPrice;
			startDate = LocalDate.now();
			while (!startDate.equals(endDate)) {
				BigDecimal epsilon = PricerUtil.inverseCnd(random.nextDouble());
				deltaT = PricerUtil.daysToYear(startDate, endDate);
				BigDecimal deltaPrice = (expectReturn.multiply(deltaT).multiply(s)).add(volatility.multiply(s)
						.multiply(epsilon).multiply(BigDecimal.valueOf(Math.sqrt(deltaT.doubleValue()))));
				s = s.multiply(BigDecimal.ONE.add(deltaPrice.divide(BigDecimal.valueOf(100),
						configurationService.getScale(), configurationService.getRoundingMode())));
				startDate = startDate.plusDays(1);
			}
			sPriceAllSimulations = sPriceAllSimulations.add(s);
		}

		s = sPriceAllSimulations.divide(BigDecimal.valueOf(simulationsCount), configurationService.getScale(),
				configurationService.getRoundingMode());

		s = PricerUtil.convertAmount(s, trade.getProduct().getCurrency(), currency, pricingDate,
				params.getQuoteSet().getId(),
				paramEquityCcyPricingCcyFXCurve != null ? paramEquityCcyPricingCcyFXCurve.getId() : 0);

		return s.multiply(trade.getQuantity());
	}

	@Override
	public BigDecimal npvMontecarloSimulation(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getActiveTo())
				|| !pricingDate.isBefore(trade.getProduct().getActiveTo())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (pricingDate.isBefore(LocalDate.now())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		BigDecimal convertedPrice = PricerUtil.convertAmount(trade.getAmount().multiply(trade.getQuantity()),
				trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		BigDecimal npv = pvMonteCarloSimulation(params, trade, currency, pricingDate).subtract(convertedPrice);

		if (trade.isSell()) {
			npv = npv.negate();
		}

		return npv;
	}

	@Override
	public BigDecimal expectedReturnCapm(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		String expectedMarketReturn = params.getParams().get("ExpectedMarketReturn");

		Equity equity = trade.getProduct();

		if (expectedMarketReturn == null) {
			throw new TradistaBusinessException("The ExpectedMarketReturn parameter doesn't exist");
		}
		BigDecimal rm;

		try {
			rm = new BigDecimal(expectedMarketReturn);
		} catch (NumberFormatException nfe) {
			throw new TradistaBusinessException(
					String.format("The ExpectedMarketReturn value is not correct: %s", expectedMarketReturn));
		}

		BigDecimal beta = null;

		String quoteName = Equity.EQUITY + "." + equity.getIsin() + "." + equity.getExchange();
		QuoteValue quoteValue = new QuoteBusinessDelegate().getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(
				params.getQuoteSet().getId(), quoteName, QuoteType.BETA, LocalDate.now());

		if (quoteValue != null) {
			beta = quoteValue.getLast();
		}

		if (beta == null) {
			throw new TradistaBusinessException(
					String.format("The '%s' Last quote of %s cannot be found as of date %tD", QuoteType.BETA,
							trade.getProduct(), LocalDate.now()));
		}

		InterestRateCurve curve = params.getDiscountCurve(equity.getCurrency());
		if (curve == null) {
			throw new TradistaBusinessException(String.format(
					"The discount curve doesn't exist for the Currency %s. please add it or change the Pricing Parameters Set.",
					equity.getCurrency()));
		}
		try {
			BigDecimal rf = PricerUtil.getDiscountFactor(curve, pricingDate);

			return rf.add(beta.multiply(rm.subtract(rf)));
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlDefault(params, equity, book, currency, pricingDate)
				.add(unrealizedPnlMarkToMarket(params, equity, book, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlDefault(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getInventoriesBeforeDateByProductAndBookIds(equity.getId(), bookId, pricingDate);
		BigDecimal realizedPnl = BigDecimal.ZERO;
		// First, we add the payments of the bond we owned before the pricing
		// date
		if (inventories != null && !inventories.isEmpty()) {
			for (ProductInventory inv : inventories) {
				BigDecimal equityPayments = PricerEquityUtil.getTotalDividendsAmount(equity, inv.getFrom(), pricingDate,
						params.getQuoteSet().getId());
				realizedPnl = realizedPnl.add(equityPayments);
			}
		}

		// then, we subtract the prices of the trades traded before the pricing
		// date
		List<EquityTrade> trades = new EquityTradeBusinessDelegate()
				.getEquityTradesBeforeTradeDateByEquityAndBookIds(pricingDate, equity.getId(), bookId);

		if (trades != null && !trades.isEmpty())

		{
			for (EquityTrade trade : trades) {
				if (trade.isBuy()) {
					realizedPnl = realizedPnl.subtract(trade.getAmount().multiply(trade.getQuantity()));
				} else {
					realizedPnl = realizedPnl.add(trade.getAmount().multiply(trade.getQuantity()));
				}
			}
		}
		return realizedPnl;

	}

	@Override
	public BigDecimal unrealizedPnlMarkToModel(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;

		if (pricingDate.isBefore(LocalDate.now())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getOpenPositionsFromInventoryByProductAndBookIds(equity.getId(), bookId);
		BigDecimal unrealizedPnl = BigDecimal.ZERO;
		if (inventories != null && !inventories.isEmpty()) {
			EquityTrade trade = new EquityTrade();
			trade.setBuySell(true);
			trade.setProduct(equity);
			trade.setQuantity(inventories.toArray(new ProductInventory[0])[0].getQuantity());
			return pvMonteCarloSimulation(params, trade, currency, pricingDate);
		}
		return unrealizedPnl;
	}

	@Override
	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getOpenPositionsFromInventoryByProductAndBookIds(equity.getId(), bookId);
		BigDecimal unrealizedPnl = BigDecimal.ZERO;

		CurrencyPair pair = new CurrencyPair(equity.getCurrency(), currency);
		FXCurve paramEquityCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramEquityCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		if (inventories != null && inventories.size() > 0) {

			// 1. Get the current Equity price:
			BigDecimal currentEquityPrice = PricerUtil.getEquityPrice(equity.getIsin(), equity.getExchange().getCode(),
					pricingDate, params.getQuoteSet().getId());

			// 2. Get the price paid for the equities currently owned
			BigDecimal equitiesQty = BigDecimal.ZERO;

			for (ProductInventory pi : inventories) {
				equitiesQty = equitiesQty.add(pi.getQuantity());
			}

			// 3. Multiply the price by the quantity
			unrealizedPnl = currentEquityPrice.multiply(equitiesQty);

			// Finally apply the conversion to the pricing currency
			unrealizedPnl = PricerUtil.convertAmount(unrealizedPnl, equity.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramEquityCcyPricingCcyFXCurve != null ? paramEquityCcyPricingCcyFXCurve.getId() : 0);
		}

		return unrealizedPnl;
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, EquityTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getActiveTo())) {
			throw new TradistaBusinessException(
					"When the equity activity period has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getProduct().getActiveTo())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the equity activity end date, it is not possible to forecast cashflows.");
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade start date has passed and the pricing date is in the past, it is not possible to forecast cashflows.");
			}
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		List<CashFlow> cashFlows = PricerEquityUtil.generateCashFlows(trade, pricingDate, params);

		if (discountCurve != null) {
			try {
				PricerUtil.discountCashFlows(cashFlows, pricingDate, discountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		return cashFlows;
	}

}