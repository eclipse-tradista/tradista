package org.eclipse.tradista.fx.fxoption.service;

import static org.eclipse.tradista.core.pricing.util.PricerUtil.cnd;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.service.FXPricerBusinessDelegate;
import org.eclipse.tradista.fx.fx.service.FXPricerService;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/*
 * Copyright 2015 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

@SecurityDomain(value = "other")
@PermitAll
@Stateless
@Interceptors(FXOptionTradeProductScopeFilteringInterceptor.class)
public class FXOptionPricerServiceBean implements FXOptionPricerService {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	@EJB
	private FXPricerService fxPricerService;

	@EJB
	private FXVolatilitySurfaceService fxVolatilitySurfaceService;

	@PostConstruct
	public void init() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public BigDecimal npvBlackAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		BigDecimal npv;
		BigDecimal pv = null;
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			pv = fxPricerService.npvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
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
				pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal npvCoxRossRubinstein(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		BigDecimal npv;
		BigDecimal pv = null;
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			FXPricerBusinessDelegate fxPricerBusinessDelegate = new FXPricerBusinessDelegate();
			pv = fxPricerBusinessDelegate.npvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
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
				pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal pvBlackAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			if (trade.isBuy()) {
				return fxPricerService.primaryPvDiscountedLegsDiff(params, trade.getUnderlying(), currency,
						pricingDate);
			} else {
				return fxPricerService.quotePvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
			}
		}

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())
				|| trade.getExerciseDate() != null) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (trade.getStyle().equals(VanillaOptionTrade.Style.AMERICAN)) {
			throw new TradistaBusinessException(
					"Black and Scholes valuation formula cannot be used with an American Option.");
		}

		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			return fxPricerService.npvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
		}

		// Primary currency IR curve retrieval
		InterestRateCurve paramPrimCurrIRCurve = params.getDiscountCurves().get(trade.getUnderlying().getCurrency());
		if (paramPrimCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a '"
							+ "%s' value. please add it or change the Pricing Parameter.",
					params.getName(), paramPrimCurrIRCurve));
		}

		// Quote currency IR curve retrieval
		InterestRateCurve paramQuoteCurrIRCurve = params.getDiscountCurves()
				.get(trade.getUnderlying().getCurrencyOne());
		if (paramQuoteCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a '"
							+ "%s' value. please add it or change the Pricing Parameter.",
					params.getName(), paramQuoteCurrIRCurve));
		}

		CurrencyPair pair = new CurrencyPair(trade.getUnderlying().getCurrency(),
				trade.getUnderlying().getCurrencyOne());
		FXCurve paramCcyCcyOneFXCurve = params.getFxCurves().get(pair);
		if (paramCcyCcyOneFXCurve == null) {
			// TODO Add log warn
		}

		pair = new CurrencyPair(trade.getUnderlying().getCurrencyOne(), trade.getUnderlying().getCurrency());
		FXCurve paramCcyOneCcyFXCurve = params.getFxCurves().get(pair);
		if (paramCcyOneCcyFXCurve == null) {
			// TODO Add log warn
		}

		pair = new CurrencyPair(trade.getUnderlying().getCurrency(), currency);
		FXCurve paramUndCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramUndCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// Volatility surface
		// Note : the volatility is supposed to be a constant value in B&S.
		// We retrieve the fx volatility surface from the PP FX Volatility Surface
		// Module for the underlying currency pair
		PricingParameterVolatilitySurfaceModule module = null;
		for (PricingParameterModule mod : params.getModules()) {
			if (mod instanceof PricingParameterVolatilitySurfaceModule) {
				module = (PricingParameterVolatilitySurfaceModule) mod;
				break;
			}
		}
		FXVolatilitySurface surface = module.getFXVolatilitySurface(
				new CurrencyPair(trade.getUnderlying().getCurrencyOne(), trade.getUnderlying().getCurrency()));
		if (surface == null) {
			throw new TradistaBusinessException(String.format(
					"The FX volatility surface doesn't exist for the currency pair %s.%s. please add it or change the Pricing Parameters Set.",
					trade.getUnderlying().getCurrencyOne(), trade.getUnderlying().getCurrency()));

		}

		BigDecimal pv;

		try {

			// underlying spot value : FX.QUOTECURR.PRIMARYCURR Exchange rate at
			// trade date
			String quoteName = "FX." + trade.getUnderlying().getCurrency().getIsoCode() + "."
					+ trade.getUnderlying().getCurrencyOne().getIsoCode();
			BigDecimal s = PricerUtil.getValueAsOfDate(quoteName, params.getQuoteSet().getId(), QuoteType.EXCHANGE_RATE,
					QuoteValue.LAST, paramCcyCcyOneFXCurve != null ? paramCcyCcyOneFXCurve.getId() : 0, pricingDate);
			if (s == null) {
				quoteName = "FX." + trade.getUnderlying().getCurrencyOne().getIsoCode() + "."
						+ trade.getUnderlying().getCurrency().getIsoCode();
				BigDecimal invS = PricerUtil.getValueAsOfDate(quoteName, params.getQuoteSet().getId(),
						QuoteType.EXCHANGE_RATE, QuoteValue.LAST,
						paramCcyOneCcyFXCurve != null ? paramCcyOneCcyFXCurve.getId() : 0, pricingDate);
				if (invS == null) {
					throw new PricerException(String.format(
							"The underlying spot data cannot be found (Quote Value: %s Quote Date: %s Quote Set:%s Curve: %s.",
							quoteName, pricingDate, params.getQuoteSet(),
							trade.getUnderlying().getCurrency().getIsoCode() + "."
									+ trade.getUnderlying().getCurrencyOne().getIsoCode()));
				} else {
					s = BigDecimal.ONE.divide(invS, configurationBusinessDelegate.getRoundingMode());
				}
			}

			// the strike is underlying Quote Amount / Primary Amount (primary
			// amount to be bought)
			BigDecimal k = trade.getStrike();

			BigDecimal d1;
			BigDecimal d2;

			// time from pricing date to option maturity date. Expressed in years.
			BigDecimal time = PricerUtil.daysToYear(pricingDate, trade.getMaturityDate());

			// Gets the implied volatility from a surface stored in the system.
			// Param in : option time to maturity
			int maturity = PricerUtil.daysToYear(trade.getTradeDate(), trade.getMaturityDate()).intValue();
			BigDecimal volat;

			volat = surface.getVolatilityByOptionExpiry(maturity);

			BigDecimal r = PricerUtil.getDiscountFactor(paramPrimCurrIRCurve, trade.getMaturityDate());
			BigDecimal rf = PricerUtil.getDiscountFactor(paramQuoteCurrIRCurve, trade.getMaturityDate());

			d1 = BigDecimal.valueOf(Math.log(s.doubleValue() / k.doubleValue()))
					.add(r.add(volat.pow(2).divide(BigDecimal.valueOf(2)))).multiply(time)
					.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(time.doubleValue()))));

			d2 = d1.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(time.doubleValue()))));

			if (trade.isCall()) {
				pv = s.multiply(cnd(d1.doubleValue()))
						.subtract(k.multiply(BigDecimal.valueOf(Math.exp(rf.negate().multiply(time).doubleValue())))
								.multiply(cnd(d2.doubleValue())));
			} else {
				pv = k.multiply(BigDecimal.valueOf(Math.exp(r.negate().multiply(time).doubleValue()))
						.multiply(cnd(d2.negate().doubleValue()))).subtract(s.multiply(cnd(d1.negate().doubleValue())));

			}

			// multiply the value by the notional in primary currency. So we have a
			// pv expressed in quote currency
			pv = pv.multiply(trade.getUnderlying().getAmountOne());

			// We have now a pv in Quote currency, let's convert it in Pricing
			// currency
			pv = PricerUtil.convertAmount(pv, trade.getUnderlying().getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramUndCcyPricingCcyFXCurve != null ? paramUndCcyPricingCcyFXCurve.getId() : 0);

		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}

		return pv;
	}

	@Override
	public BigDecimal pvCoxRossRubinstein(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			if (trade.isBuy()) {
				return fxPricerService.primaryPvDiscountedLegsDiff(params, trade.getUnderlying(), currency,
						pricingDate);
			} else {
				return fxPricerService.quotePvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
			}
		}

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())
				|| trade.getExerciseDate() != null) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		// Binomial tree step duration is a Pricing parameter
		String binomialTreeHeight = "binomialTreeHeight";
		String binomialTreeHeightStr = params.getParams().get(binomialTreeHeight);

		if (binomialTreeHeightStr == null) {
			throw new TradistaBusinessException(params.getName() + " Pricing Parameter doesn't contain a '"
					+ binomialTreeHeight + "' value. please add it or change the Pricing Parameter.");
		}
		int binomialTreeHeightValue = Integer.parseInt(binomialTreeHeightStr);

		// Primary currency IR curve retrieval
		InterestRateCurve paramPrimCurrIRCurve = params.getDiscountCurves().get(trade.getUnderlying().getCurrency());
		if (paramPrimCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a '"
							+ "%s' value. please add it or change the Pricing Parameter.",
					params.getName(), paramPrimCurrIRCurve));
		}

		// Quote currency IR curve retrieval
		InterestRateCurve paramQuoteCurrIRCurve = params.getDiscountCurves()
				.get(trade.getUnderlying().getCurrencyOne());
		if (paramQuoteCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a '"
							+ "%s' value. please add it or change the Pricing Parameter.",
					params.getName(), paramQuoteCurrIRCurve));
		}

		CurrencyPair pair = new CurrencyPair(trade.getUnderlying().getCurrency(),
				trade.getUnderlying().getCurrencyOne());
		FXCurve paramCcyCcyOneFXCurve = params.getFxCurves().get(pair);
		if (paramCcyCcyOneFXCurve == null) {
			// TODO Add log warn
		}

		pair = new CurrencyPair(trade.getUnderlying().getCurrency(), currency);
		FXCurve paramUndCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramUndCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}
		try {
			BigDecimal r = PricerUtil.getDiscountFactor(paramPrimCurrIRCurve, trade.getMaturityDate());
			BigDecimal rf = PricerUtil.getDiscountFactor(paramQuoteCurrIRCurve, trade.getMaturityDate());

			// double deltaT = T / n;
			double deltaT = PricerUtil.daysToYear(pricingDate, trade.getMaturityDate())
					.divide(BigDecimal.valueOf(binomialTreeHeightValue),
							configurationBusinessDelegate.getRoundingMode())
					.doubleValue();

			// Volatility curve
			// Note : the volatility is supposed to be a constant value in CRR. So,
			// it could also be retrieved from a Pricing Param, and not from a
			// surface
			// We retrieve the fx volatility surface from the PP FX Volatility Surface
			// Module for the underlying currency pair
			PricingParameterVolatilitySurfaceModule module = null;
			for (PricingParameterModule mod : params.getModules()) {
				if (mod instanceof PricingParameterVolatilitySurfaceModule) {
					module = (PricingParameterVolatilitySurfaceModule) mod;
					break;
				}
			}
			FXVolatilitySurface surface = module.getFXVolatilitySurface(
					new CurrencyPair(trade.getUnderlying().getCurrencyOne(), trade.getUnderlying().getCurrency()));
			if (surface == null) {
				throw new TradistaBusinessException(String.format(
						"The FX volatility surface doesn't exist for the currency pair %s.%s. please add it or change the Pricing Parameters Set.",
						trade.getUnderlying().getCurrencyOne(), trade.getUnderlying().getCurrency()));

			}

			// Gets the implied volatility from a surface stored in the system.
			// Param in : option time to maturity
			int maturity = PricerUtil.daysToYear(trade.getTradeDate(), trade.getMaturityDate()).intValue();
			BigDecimal sigma = surface.getVolatilityByOptionExpiry(maturity);

			double up = Math.exp(sigma.multiply(BigDecimal.valueOf(Math.sqrt(deltaT))).doubleValue());

			double p0 = (up * Math.exp(r.negate().multiply(BigDecimal.valueOf(deltaT)).doubleValue())
					- Math.exp(rf.multiply(BigDecimal.valueOf(deltaT)).doubleValue())) * up / (Math.pow(up, 2) - 1);
			double p1 = Math.exp(r.negate().multiply(BigDecimal.valueOf(deltaT)).doubleValue()) - p0;
			double[] p = new double[binomialTreeHeightValue];

			// the strike is the quote amount of the FX
			BigDecimal k = trade.getUnderlying().getAmount();

			// Convert k in primary currency
			k = PricerUtil.convertAmount(k, trade.getUnderlying().getCurrency(), trade.getUnderlying().getCurrencyOne(),
					pricingDate, params.getQuoteSet().getId(),
					paramCcyCcyOneFXCurve != null ? paramCcyCcyOneFXCurve.getId() : 0);

			// underlying spot value :
			BigDecimal s = trade.getUnderlying().getAmountOne();

			// initial values at time T
			for (int i = 0; i < binomialTreeHeightValue; i++) {
				if (trade.isCall()) {
					p[i] = k.doubleValue() - s.doubleValue() * Math.pow(up, (2 * i - binomialTreeHeightValue));
				} else {
					p[i] = s.doubleValue() * Math.pow(up, (2 * i - binomialTreeHeightValue)) - k.doubleValue();
				}
				if (p[i] < 0) {
					p[i] = 0;
				}
			}

			// move to earlier times
			for (int j = binomialTreeHeightValue - 2; j >= 0; j--) {
				for (int i = 0; i <= j; i++) {
					p[i] = p0 * p[i] + p1 * p[i + 1]; // binomial value
					if (trade.getStyle().equals(VanillaOptionTrade.Style.AMERICAN)) {
						double exercise;

						if (trade.isCall()) {
							exercise = k.doubleValue() - s.doubleValue() * Math.pow(up, (2 * i - j)); // exercise
																										// value
						} else {
							exercise = s.doubleValue() * Math.pow(up, (2 * i - j)) - k.doubleValue(); // exercise
																										// value
						}

						if (p[i] < exercise) {
							p[i] = exercise;
						}
					}
				}

			}

			BigDecimal pv = PricerUtil.convertAmount(BigDecimal.valueOf(p[0]), trade.getUnderlying().getCurrencyOne(),
					currency, pricingDate, params.getQuoteSet().getId(),
					paramUndCcyPricingCcyFXCurve != null ? paramUndCcyPricingCcyFXCurve.getId() : 0);

			return pv;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

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
		// 2. There was an exercise, we check if the pricing date before or
		// after the settlement date
		LocalDate underlyingSettlementDate = trade.getUnderlyingSettlementDate();
		if (pricingDate.isBefore(underlyingSettlementDate)) {
			return BigDecimal.ZERO;
		}
		// 3. There was a realized PNL, we calculate it.
		trade.getUnderlying().setSettlementDate(underlyingSettlementDate);
		BigDecimal mtm = fxPricerService.realizedPnlMarkToMarket(params, trade.getUnderlying(), currency, pricingDate);

		// add (or subtract) the premium from the realized PNL,
		// depending of the trade direction

		if (trade.isBuy()) {
			mtm = mtm.subtract(convertedPremium);
		} else {
			mtm = convertedPremium.subtract(mtm);
		}

		return mtm;
	}

	@Override
	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

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

		// On option, let's calculate the unrealized pnl as the NPV.
		return npvBlackAndScholes(params, trade, currency, pricingDate);

	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, FXOptionTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {

		return realizedPnlMarkToMarket(params, trade, currency, pricingDate)
				.add(unrealizedPnlBlackAndScholes(params, trade, currency, pricingDate));

	}

	@Override
	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// convert the premium
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

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

		// On option, let's calculate the unrealized pnl as the Mark to Market of the
		// underlying
		trade.getUnderlying().setSettlementDate(pricingDate);
		BigDecimal mtm = fxPricerService.unrealizedPnlMarkToMarket(params, trade.getUnderlying(), currency,
				pricingDate);

		// add (or subtract) the premium from the realized PNL,
		// depending of the trade direction

		if (trade.isBuy()) {
			mtm = mtm.subtract(convertedPremium);
		} else {
			mtm = convertedPremium.subtract(mtm);
		}

		return mtm;
	}

}