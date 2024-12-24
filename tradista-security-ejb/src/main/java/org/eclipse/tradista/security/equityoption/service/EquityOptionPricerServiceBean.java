package org.eclipse.tradista.security.equityoption.service;

import static org.eclipse.tradista.core.pricing.util.PricerUtil.cnd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.pricer.PricerEquityUtil;
import org.eclipse.tradista.security.equity.service.EquityPricerBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityPricerService;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule;
import org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.security.equityoption.pricer.PricerEquityOptionUtil;
import org.jboss.ejb3.annotation.SecurityDomain;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
@Interceptors(EquityOptionProductScopeFilteringInterceptor.class)
public class EquityOptionPricerServiceBean implements EquityOptionPricerService {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public void init() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@EJB
	private EquityPricerService equityPricerService;

	@EJB
	private EquityOptionVolatilitySurfaceService equityOptionVolatilitySurfaceService;

	@EJB
	private EquityOptionTradeService equityOptionTradeService;

	@Override
	public BigDecimal pvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			BigDecimal pv = equityPricerService.npvMontecarloSimulation(params, trade.getUnderlying(), currency,
					pricingDate);
			if (trade.getEquityOption() != null) {
				pv = pv.multiply(trade.getQuantity());
			}
			return pv;
		}
		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		try {
			// Binomial tree step duration is a Pricing parameter
			// Volatility curve
			String binomialTreeHeight = "binomialTreeHeight";
			String binomialTreeHeightStr = params.getParams().get(binomialTreeHeight);

			if (binomialTreeHeightStr == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain a '%s' value. please add it or change the Pricing Parameter.",
						params.getName(), binomialTreeHeight));
			}
			int binomialTreeHeightValue = Integer.parseInt(binomialTreeHeightStr);

			// Retrieval of the equity's currency
			Currency undCurrency = null;
			if (trade.getEquityOption() != null) {
				undCurrency = trade.getEquityOption().getUnderlying().getCurrency();
			} else {
				undCurrency = trade.getUnderlying().getCurrency();
			}
			InterestRateCurve discountCurve = params.getDiscountCurves().get(undCurrency);
			if (discountCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
						params.getName(), undCurrency));
			}

			BigDecimal r = PricerUtil.getDiscountFactor(discountCurve.getId(), trade.getMaturityDate());
			BigDecimal q = BigDecimal.ZERO;

			EquityTrade underlying = trade.getUnderlying();

			if (underlying == null) {
				// the equity option trade is using a listed equityOption
				underlying = new EquityTrade();
				underlying.setCurrency(trade.getCurrency());
				underlying.setProduct(trade.getEquityOption().getUnderlying());
				underlying.setQuantity(trade.getEquityOption().getQuantity());
			}

			CurrencyPair pair = new CurrencyPair(underlying.getCurrency(), currency);
			FXCurve paramUndCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
			if (paramUndCcyPricingCcyFXCurve == null) {
				// TODO Add log warn
			}

			if (underlying.getProduct().isPayDividend()) {
				// Retrieve the dividend yield from quotes or curve, depending of
				// pricing date
				String quoteName = Equity.EQUITY + "." + underlying.getProduct().getIsin();
				Map<Equity, InterestRateCurve> ppDividendYieldCurves = new HashMap<Equity, InterestRateCurve>();
				if (params.getModules() != null && !params.getModules().isEmpty()) {
					for (PricingParameterModule module : params.getModules()) {
						if (module instanceof PricingParameterDividendYieldCurveModule) {
							ppDividendYieldCurves = ((PricingParameterDividendYieldCurveModule) module)
									.getDividendYieldCurves();
							break;
						}
					}
				}
				InterestRateCurve dividendYieldCurve = ppDividendYieldCurves.get(underlying.getProduct());
				if (dividendYieldCurve == null) {
					// TODO Add log warn
				}
				q = PricerUtil.getValueAsOfDate(quoteName, params.getQuoteSet().getId(), QuoteType.DIVIDEND_YIELD,
						QuoteValue.LAST, dividendYieldCurve != null ? dividendYieldCurve.getId() : 0, pricingDate);

				if (q.doubleValue() == 0) {
					// TODO add a warning log.
				}
			}

			// double deltaT = T / n;
			BigDecimal deltaT = PricerUtil.daysToYear(pricingDate, trade.getMaturityDate()).divide(
					BigDecimal.valueOf(binomialTreeHeightValue), configurationBusinessDelegate.getRoundingMode());

			// Volatility Surface
			// Note : the volatility is supposed to be a constant value in CRR, so
			// it could also have been retrieved from a pricing parameter.
			// We retrieve the equity option volatility surface from the PP EO Volatility
			// Surface
			// Module for the underlying
			PricingParameterVolatilitySurfaceModule module = null;
			for (PricingParameterModule mod : params.getModules()) {
				if (mod instanceof PricingParameterVolatilitySurfaceModule) {
					module = (PricingParameterVolatilitySurfaceModule) mod;
					break;
				}
			}
			EquityOptionVolatilitySurface surface = module.getEquityOptionVolatilitySurface(underlying.getProduct());
			if (surface == null) {
				throw new TradistaBusinessException(String.format(
						"The Equity Option volatility surface doesn't exist for the equity %s. please add it or change the Pricing Parameters Set.",
						underlying.getProduct()));
			}

			// time from trade date to option maturity date. Expressed in years.
			BigDecimal time = PricerUtil.daysToYear(trade.getTradeDate(), trade.getMaturityDate());

			int optionExpiry = time.intValue();

			// Gets the implied volatility from a surface stored in the system.
			// Param in : option time to maturity
			BigDecimal sigma;
			try {
				sigma = surface.getVolatilityByOptionExpiry(optionExpiry);
			} catch (TradistaBusinessException abe) {
				throw new TradistaBusinessException(abe.getMessage());
			}

			BigDecimal up = BigDecimal.valueOf(
					Math.exp(sigma.multiply(BigDecimal.valueOf(Math.sqrt(deltaT.doubleValue()))).doubleValue()));

			BigDecimal p0 = (up.multiply(BigDecimal.valueOf(Math.exp(r.negate().multiply(deltaT).doubleValue())))
					.subtract(BigDecimal.valueOf(Math.exp(q.multiply(deltaT).doubleValue())))).multiply(up)
					.divide(BigDecimal.valueOf(Math.pow(up.doubleValue(), 2)).subtract(BigDecimal.valueOf(1)),
							configurationBusinessDelegate.getRoundingMode());
			BigDecimal p1 = BigDecimal.valueOf(Math.exp(r.negate().multiply(deltaT).doubleValue())).subtract(p0);

			BigDecimal[] p = new BigDecimal[binomialTreeHeightValue];

			BigDecimal s = null;
			// underlying spot value :
			if (!pricingDate.isAfter(LocalDate.now())) {
				// if the pricing date is in the past or the current date, we try to
				// find the underlying price in the quotes or curve, depending of
				// the pricing date
				s = PricerUtil.getValueAsOfDateFromQuote(Equity.EQUITY + "." + underlying.getProduct().getIsin(),
						params.getQuoteSet().getId(), QuoteType.EQUITY_PRICE, QuoteValue.LAST, pricingDate);

				if (s == null) {
					// the underlying price was not found in the quotes, we use the
					// equity pricer
					s = equityPricerService.pvMonteCarloSimulation(params, underlying, currency, pricingDate);
				}
			} else {
				s = equityPricerService.pvMonteCarloSimulation(params, underlying, currency, pricingDate);
			}

			// the strike is the quote amount of the FX
			BigDecimal k = trade.getStrike();

			// initial values at time T
			for (int i = 0; i < binomialTreeHeightValue; i++) {
				if (trade.isCall()) {
					p[i] = k.subtract(s).multiply(
							BigDecimal.valueOf(Math.pow(up.doubleValue(), (2 * i - binomialTreeHeightValue))));
				} else {
					p[i] = s.multiply(BigDecimal.valueOf(Math.pow(up.doubleValue(), (2 * i - binomialTreeHeightValue))))
							.subtract(k);
				}
				if (p[i].doubleValue() < 0) {
					p[i] = BigDecimal.ZERO;
				}
			}

			// move to earlier times
			for (int j = binomialTreeHeightValue - 2; j >= 0; j--) {
				for (int i = 0; i <= j; i++) {
					p[i] = (p0.multiply(p[i])).add(p1.multiply(p[i + 1])); // binomial
																			// value
					if (trade.getStyle().equals(VanillaOptionTrade.Style.AMERICAN)) {
						BigDecimal exercise;

						if (trade.isCall()) {
							exercise = k
									.subtract(s.multiply(BigDecimal.valueOf(Math.pow(up.doubleValue(), (2 * i - j))))); // exercise
																														// value
						} else {
							exercise = s.multiply(BigDecimal.valueOf(Math.pow(up.doubleValue(), (2 * i - j))))
									.subtract(k); // exercise
													// value
						}

						if (p[i].doubleValue() < exercise.doubleValue()) {
						}
						p[i] = exercise;
					}
				}

			}

			BigDecimal pv = PricerUtil.convertAmount(p[0], underlying.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramUndCcyPricingCcyFXCurve != null ? paramUndCcyPricingCcyFXCurve.getId() : 0);

			if (trade.getEquityOption() != null) {
				pv = pv.multiply(trade.getQuantity());
			}

			return pv;

		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal npvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		BigDecimal npv;
		BigDecimal pv = null;
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			pv = equityPricerService.npvMontecarloSimulation(params, trade.getUnderlying(), currency, pricingDate);
		}
		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		if (trade.getExerciseDate() == null) {
			pv = pvBlackAndScholes(params, trade, currency, pricingDate);
		}

		// convert the premium, then add (or subtract) it from the PV, depending
		// of the trade direction
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);
		if (trade.getEquityOption() != null) {
			convertedPremium = convertedPremium.multiply(trade.getQuantity());
		}

		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal npvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		BigDecimal npv;
		BigDecimal pv = null;
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			pv = equityPricerService.npvMontecarloSimulation(params, trade.getUnderlying(), currency, pricingDate);
		}
		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		if (trade.getExerciseDate() == null) {
			pv = pvCoxRossRubinstein(params, trade, currency, pricingDate);
		}

		// convert the premium, then add (or subtract) it from the PV, depending
		// of the trade direction
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		if (trade.getEquityOption() != null) {
			convertedPremium = convertedPremium.multiply(trade.getQuantity());
		}

		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal expectedReturnCapm(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		EquityTrade underlying = trade.getUnderlying();

		if (underlying == null) {
			// the equity option trade is using a listed equityOption
			underlying = new EquityTrade();
			underlying.setCurrency(trade.getCurrency());
			underlying.setProduct(trade.getEquityOption().getUnderlying());
			underlying.setQuantity(trade.getEquityOption().getQuantity());
		}
		return equityPricerService.expectedReturnCapm(params, underlying, currency, pricingDate);
	}

	@Override
	public BigDecimal pvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (trade.getStyle().equals(VanillaOptionTrade.Style.AMERICAN)) {
			throw new TradistaBusinessException(
					"Black and Scholes valuation formula cannot be used with an American Option.");
		}

		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			BigDecimal pv = equityPricerService.npvMontecarloSimulation(params, trade.getUnderlying(), currency,
					pricingDate);
			if (trade.getEquityOption() != null) {
				pv = pv.multiply(trade.getQuantity());
			}
			return pv;
		}
		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		try {
			// Retrieval of the equity's currency
			Currency undCurrency = null;
			if (trade.getEquityOption() != null) {
				undCurrency = trade.getEquityOption().getUnderlying().getCurrency();
			} else {
				undCurrency = trade.getUnderlying().getCurrency();
			}
			InterestRateCurve discountCurve = params.getDiscountCurves().get(undCurrency);
			if (discountCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
						params.getName(), undCurrency));
			}

			Equity equity;

			if (trade.getEquityOption() != null) {
				equity = trade.getEquityOption().getUnderlying();
			} else {
				equity = trade.getUnderlying().getProduct();
			}
			// Volatility Surface
			// Note : the volatility is supposed to be a constant value in CRR, so
			// it could also have been retrieved from a pricing parameter.
			// We retrieve the equity option volatility surface from the PP EO Volatility
			// Surface
			// Module for the underlying
			PricingParameterVolatilitySurfaceModule module = null;
			for (PricingParameterModule mod : params.getModules()) {
				if (mod instanceof PricingParameterVolatilitySurfaceModule) {
					module = (PricingParameterVolatilitySurfaceModule) mod;
					break;
				}
			}
			EquityOptionVolatilitySurface surface = module.getEquityOptionVolatilitySurface(equity);
			if (surface == null) {
				throw new TradistaBusinessException(String.format(
						"The Equity Option volatility surface doesn't exist for the equity %s. please add it or change the Pricing Parameters Set.",
						equity));
			}

			BigDecimal pv;

			BigDecimal s;

			EquityTrade underlying = trade.getUnderlying();

			if (underlying == null) {
				// the equity option trade is using a listed equityOption
				underlying = new EquityTrade();
				underlying.setCurrency(trade.getCurrency());
				underlying.setProduct(trade.getEquityOption().getUnderlying());
				underlying.setQuantity(trade.getEquityOption().getQuantity());
			}

			CurrencyPair pair = new CurrencyPair(underlying.getCurrency(), currency);
			FXCurve paramUndCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
			if (paramUndCcyPricingCcyFXCurve == null) {
				// TODO Add log warn
			}

			// underlying spot value :
			if (!pricingDate.isAfter(LocalDate.now())) {
				// if the pricing date is in the past or the current date, we try to
				// find the underlying price in the quote
				s = PricerUtil.getValueAsOfDateFromQuote(Equity.EQUITY + "." + equity.getIsin(),
						params.getQuoteSet().getId(), QuoteType.EQUITY_PRICE, QuoteValue.LAST, pricingDate);

				if (s == null) {
					// the underlying price was not found in the quotes, we use the
					// equity pricer
					s = equityPricerService.pvMonteCarloSimulation(params, underlying, currency, pricingDate);
				}
			} else {
				s = equityPricerService.pvMonteCarloSimulation(params, underlying, currency, pricingDate);
			}

			// If the equity pays dividends, we should subtract to s the discounted
			// value of the dividends during the option life (cf Hull)
			if (underlying.getProduct().isPayDividend()) {
				try {
					BigDecimal pendingDividends = PricerEquityUtil.discountDividends(params, underlying,
							discountCurve.getId(), pricingDate, trade.getMaturityDate());
					s = s.subtract(pendingDividends);
				} catch (PricerException pe) {
					// TODO add a warning log.
				}
			}

			// the strike is the quote amount of the FX
			BigDecimal k = trade.getStrike();

			BigDecimal d1;
			BigDecimal d2;

			// time from trade date to option maturity date. Expressed in years.
			BigDecimal time = PricerUtil.daysToYear(trade.getTradeDate(), trade.getMaturityDate());

			int optionExpiry = time.intValue();

			// Gets the implied volatility from a surface stored in the system.
			// Param in : option time to maturity
			BigDecimal volat = surface.getVolatilityByOptionExpiry(optionExpiry);

			BigDecimal r = PricerUtil.getDiscountFactor(discountCurve.getId(), trade.getMaturityDate());

			d1 = BigDecimal.valueOf(Math.log(s.doubleValue() / k.doubleValue()))
					.add(r.add(volat.pow(2).divide(BigDecimal.valueOf(2)))).multiply(time)
					.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(time.doubleValue()))),
							configurationBusinessDelegate.getRoundingMode());

			d2 = d1.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(time.doubleValue()))));

			if (trade.isCall()) {
				pv = s.multiply(cnd(d1.doubleValue()))
						.subtract(k.multiply(BigDecimal.valueOf(Math.exp(r.negate().multiply(time).doubleValue())))
								.multiply(cnd(d2.doubleValue())));
			} else {
				pv = k.multiply(BigDecimal.valueOf(Math.exp(r.negate().multiply(time).doubleValue()))
						.multiply(cnd(d2.negate().doubleValue()))).subtract(s.multiply(cnd(d1.negate().doubleValue())));

			}

			pv = PricerUtil.convertAmount(pv, underlying.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramUndCcyPricingCcyFXCurve != null ? paramUndCcyPricingCcyFXCurve.getId() : 0);

			if (trade.getEquityOption() != null) {
				pv = pv.multiply(trade.getQuantity());
			}

			return pv;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, EquityOption equityOption, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlDefault(params, equityOption, book, currency, pricingDate)
				.add(unrealizedPnlBlackAndScholes(params, equityOption, book, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlDefault(PricingParameter params, EquityOption equityOption, Book book,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		BigDecimal realizedPnl = BigDecimal.ZERO;

		// then, we subtract the prices of the trades traded before the pricing
		// date
		List<EquityOptionTrade> trades = equityOptionTradeService
				.getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(pricingDate, equityOption.getId(),
						bookId);

		if (trades != null && !trades.isEmpty())

			for (EquityOptionTrade trade : trades) {
				if (trade.getEquityOption() != null) {
					if (trade.isBuy()) {
						if (trade.getExerciseDate() != null) {
							realizedPnl.add(PricerEquityOptionUtil.getProfitAfterExercice(trade));
						}
						realizedPnl.subtract(trade.getAmount().multiply(trade.getQuantity()));
					} else {
						realizedPnl.add(trade.getAmount().multiply(trade.getQuantity()));
					}
				}
			}

		return realizedPnl;

	}

	@Override
	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOption equityOption, Book book,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getOpenPositionsFromInventoryByProductAndBookIds(equityOption.getId(), bookId);
		BigDecimal unrealizedPnl = BigDecimal.ZERO;
		if (inventories != null && inventories.isEmpty()) {
			EquityOptionTrade trade = new EquityOptionTrade();
			trade.setBuySell(true);
			trade.setEquityOption(equityOption);
			trade.setType(equityOption.getType());
			trade.setMaturityDate(equityOption.getMaturityDate());
			trade.setQuantity(inventories.toArray(new ProductInventory[0])[0].getQuantity());
			return pvBlackAndScholes(params, trade, currency, pricingDate);
		}
		return unrealizedPnl;
	}

	@Override
	public BigDecimal realizedPnlOptionExercise(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (trade.getEquityOption() != null) {
			// TODO Log warn "For listed option, realized pnl must be calculated
			// at product level"
			return BigDecimal.ZERO;
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// convert the premium
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		// 1. If there was no exercise, the realized PNL is 0.
		if (trade.getExerciseDate() == null) {
			if (pricingDate.isAfter(trade.getMaturityDate())) {
				if (trade.isBuy()) {
					return convertedPremium.negate();
				} else {
					return convertedPremium;
				}

			} else {
				return BigDecimal.ZERO;
			}
		}
		// 2. There was an exercise, we check if the pricing date is before or
		// after the settlement date
		LocalDate underlyingSettlementDate = trade.getUnderlyingSettlementDate();
		if (pricingDate.isBefore(underlyingSettlementDate)) {
			return BigDecimal.ZERO;
		}
		// 3. There was a realized PNL, we calculate it.
		trade.getUnderlying().setSettlementDate(underlyingSettlementDate);
		BigDecimal npvAsOfSettlementDate = new EquityPricerBusinessDelegate().realizedPnlDefault(params,
				trade.getUnderlying().getProduct(), trade.getBook(), currency, pricingDate);

		// add (or subtract) the premium from the realized PNL,
		// depending of the trade direction

		if (trade.isBuy()) {
			npvAsOfSettlementDate = npvAsOfSettlementDate.subtract(convertedPremium);
		} else {
			npvAsOfSettlementDate = convertedPremium.subtract(npvAsOfSettlementDate);
		}

		return npvAsOfSettlementDate;
	}

	@Override
	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (trade.getEquityOption() != null) {
			// TODO Log warn "For listed option, realized pnl must be calculated
			// at product level"
			return BigDecimal.ZERO;
		}

		if (trade.getMaturityDate().isBefore(pricingDate)) {
			return BigDecimal.ZERO;
		}

		if (trade.getExerciseDate() != null) {
			// There was an exercise, we check if the pricing date is after the
			// settlement date
			LocalDate underlyingSettlementDate = trade.getUnderlyingSettlementDate();
			if (pricingDate.isAfter(underlyingSettlementDate) || pricingDate.equals(underlyingSettlementDate)) {
				return BigDecimal.ZERO;
			}
		}

		// On option, let's calculate the unrealized pnl as the NPV. How else
		// can we calculate something that may be not exercised ?
		return npvBlackAndScholes(params, trade, currency, pricingDate);

	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		return realizedPnlOptionExercise(params, trade, currency, pricingDate)
				.add(unrealizedPnlBlackAndScholes(params, trade, currency, pricingDate));

	}

}