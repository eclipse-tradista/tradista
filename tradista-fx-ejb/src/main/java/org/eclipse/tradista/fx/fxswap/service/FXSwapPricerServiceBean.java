package org.eclipse.tradista.fx.fxswap.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXPricerService;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.service.FXSwapPricerService;

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
@Interceptors(FXSwapTradeProductScopeFilteringInterceptor.class)
public class FXSwapPricerServiceBean implements FXSwapPricerService {

	@EJB
	private FXPricerService fxPricerService;

	@Override
	public BigDecimal fwdLegNpvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDateForward())
				|| !pricingDate.isBefore(trade.getSettlementDateForward())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		// 1. Construct a FX trade from the forward leg
		FXTrade fwdLegFX = new FXTrade();
		fwdLegFX.setAmountOne(trade.getAmountOneForward());
		fwdLegFX.setAmount(trade.getAmountTwoForward());
		fwdLegFX.setBuySell(!trade.isBuy());
		fwdLegFX.setCurrencyOne(trade.getCurrencyOne());
		fwdLegFX.setCurrency(trade.getCurrency());
		fwdLegFX.setSettlementDate(trade.getSettlementDateForward());
		fwdLegFX.setTradeDate(trade.getTradeDate());

		// 2. Calculate the Spot leg NPV
		BigDecimal fwdLegNPV = fxPricerService.npvDiscountedLegsDiff(params, fwdLegFX, currency, pricingDate);

		return fwdLegNPV;
	}

	@Override
	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDateForward())
				|| !pricingDate.isBefore(trade.getSettlementDateForward())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (!pricingDate.isAfter(trade.getSettlementDate())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has already passed, the pricing date must be after it.");
			}
		}

		// 1. Calculate the Spot leg NPV

		BigDecimal spotLegNPV = spotLegNpvDiscountedLegsDiff(params, trade, currency, pricingDate);

		// 2. Calculate the Fwd leg NPV

		BigDecimal fwdLegNPV = fwdLegNpvDiscountedLegsDiff(params, trade, currency, pricingDate);

		// 3. Add the two legs NPV

		return spotLegNPV.add(fwdLegNPV);
	}

	@Override
	public BigDecimal spotLegNpvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDate()) || !pricingDate.isBefore(trade.getSettlementDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		// 1. Construct a FX trade from the spot leg
		FXTrade spotLegFX = new FXTrade();
		spotLegFX.setAmountOne(trade.getAmountOneSpot());
		spotLegFX.setAmount(trade.getAmount());
		spotLegFX.setBuySell(trade.isBuy());
		spotLegFX.setCurrencyOne(trade.getCurrencyOne());
		spotLegFX.setCurrency(trade.getCurrency());
		spotLegFX.setSettlementDate(trade.getSettlementDate());
		spotLegFX.setTradeDate(trade.getTradeDate());
		spotLegFX.setCounterparty(trade.getCounterparty());
		spotLegFX.setBook(trade.getBook());

		// 2. Calculate the Spot leg NPV
		BigDecimal spotLegNPV = fxPricerService.npvDiscountedLegsDiff(params, spotLegFX, currency, pricingDate);

		return spotLegNPV;
	}

	@Override
	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDate()) || pricingDate.equals(trade.getSettlementDate())) {
			// We calculate the MTM of each leg
			// 1. Construct a FX trade from the spot leg
			FXTrade spotLegFX = new FXTrade();
			spotLegFX.setAmountOne(trade.getAmountOneSpot());
			spotLegFX.setAmount(trade.getAmount());
			spotLegFX.setBuySell(trade.isBuy());
			spotLegFX.setCurrencyOne(trade.getCurrencyOne());
			spotLegFX.setCurrency(trade.getCurrency());
			spotLegFX.setSettlementDate(trade.getSettlementDate());
			spotLegFX.setTradeDate(trade.getTradeDate());
			spotLegFX.setCounterparty(trade.getCounterparty());
			spotLegFX.setBook(trade.getBook());

			// 2. Calculate the Spot leg MTM
			BigDecimal spotLegMTM = fxPricerService.unrealizedPnlMarkToMarket(params, spotLegFX, currency, pricingDate);

			BigDecimal fwdLegMTM = BigDecimal.ZERO;

			if (!pricingDate.isBefore(trade.getSettlementDateForward())) {

				// 3. Construct a FX trade from the forward leg
				FXTrade fwdLegFX = new FXTrade();
				fwdLegFX.setAmountOne(trade.getAmountOneForward());
				fwdLegFX.setAmount(trade.getAmountTwoForward());
				fwdLegFX.setBuySell(!trade.isBuy());
				fwdLegFX.setCurrencyOne(trade.getCurrencyOne());
				fwdLegFX.setCurrency(trade.getCurrency());
				fwdLegFX.setSettlementDate(trade.getSettlementDateForward());
				fwdLegFX.setTradeDate(trade.getTradeDate());

				// 4. Calculate the forward leg MTM
				fwdLegMTM = fxPricerService.unrealizedPnlMarkToMarket(params, fwdLegFX, currency, pricingDate);
			}

			return spotLegMTM.add(fwdLegMTM);
		}

		// if pricing date is before settlement date, there is no
		// realized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDateForward())
				|| pricingDate.equals(trade.getSettlementDateForward())) {
			// if pricing date is equal to or after the settlement date forward,
			// the pnl
			// is already realized
			return BigDecimal.ZERO;
		}

		// If the trade is not yet settled, we use the NPV to estimate the
		// unrealized pnl.
		return npvDiscountedLegsDiff(params, trade, currency, pricingDate);

	}

	@Override
	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDateForward())
				|| pricingDate.equals(trade.getSettlementDateForward())) {
			// if pricing date is equal to or after the settlement date forward,
			// the pnl
			// is already realized
			return BigDecimal.ZERO;
		}

		// 1. Construct a FX trade from the spot leg
		FXTrade spotLegFX = new FXTrade();
		spotLegFX.setAmountOne(trade.getAmountOneSpot());
		spotLegFX.setAmount(trade.getAmount());
		spotLegFX.setBuySell(trade.isBuy());
		spotLegFX.setCurrencyOne(trade.getCurrencyOne());
		spotLegFX.setCurrency(trade.getCurrency());
		spotLegFX.setSettlementDate(trade.getSettlementDate());
		spotLegFX.setTradeDate(trade.getTradeDate());
		spotLegFX.setCounterparty(trade.getCounterparty());
		spotLegFX.setBook(trade.getBook());

		// 2. Calculate the Spot leg MTM
		BigDecimal spotLegMTM = fxPricerService.unrealizedPnlMarkToMarket(params, spotLegFX, currency, pricingDate);

		// 3. Construct a FX trade from the forward leg
		FXTrade fwdLegFX = new FXTrade();
		fwdLegFX.setAmountOne(trade.getAmountOneForward());
		fwdLegFX.setAmount(trade.getAmountTwoForward());
		fwdLegFX.setBuySell(!trade.isBuy());
		fwdLegFX.setCurrencyOne(trade.getCurrencyOne());
		fwdLegFX.setCurrency(trade.getCurrency());
		fwdLegFX.setSettlementDate(trade.getSettlementDateForward());
		fwdLegFX.setTradeDate(trade.getTradeDate());

		// 4. Calculate the forward leg MTM
		BigDecimal fwdLegMTM = fxPricerService.unrealizedPnlMarkToMarket(params, fwdLegFX, currency, pricingDate);

		return spotLegMTM.add(fwdLegMTM);
	}

	@Override
	public BigDecimal defaultPNL(PricingParameter params, FXSwapTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return realizedPnlMarkToMarket(params, trade, currency, pricingDate)
				.add(unrealizedPnlLegsDiff(params, trade, currency, pricingDate));
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, FXSwapTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDateForward())) {
			throw new TradistaBusinessException(
					"When the forward leg settlement date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getSettlementDateForward())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the forward leg settlement date, it is not possible to forecast cashflows.");
		}

		// Primary currency IR curve retrieval
		InterestRateCurve primaryLegDiscountCurve = params.getDiscountCurves().get(trade.getCurrencyOne());
		if (primaryLegDiscountCurve == null) {
			// TODO Add log warn
		}

		// Quote currency IR curve retrieval
		InterestRateCurve quoteLegDiscountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (quoteLegDiscountCurve == null) {
			// TODO Add log warn
		}

		CashFlow primarySpotCf = new CashFlow();
		primarySpotCf.setDate(trade.getSettlementDate());
		primarySpotCf.setPurpose(TransferPurpose.PRIMARY_CURRENCY_SPOT);
		primarySpotCf.setAmount(trade.getAmountOneSpot());
		primarySpotCf.setCurrency(trade.getCurrencyOne());

		if (trade.isBuy()) {
			primarySpotCf.setDirection(CashFlow.Direction.RECEIVE);
		} else {
			primarySpotCf.setDirection(CashFlow.Direction.PAY);
		}

		CashFlow primaryFwdCf = new CashFlow();
		primaryFwdCf.setDate(trade.getSettlementDateForward());
		primaryFwdCf.setPurpose(TransferPurpose.PRIMARY_CURRENCY_FORWARD);
		primaryFwdCf.setAmount(trade.getAmountOneForward());
		primaryFwdCf.setCurrency(trade.getCurrencyOne());

		if (trade.isBuy()) {
			primaryFwdCf.setDirection(CashFlow.Direction.PAY);
		} else {
			primaryFwdCf.setDirection(CashFlow.Direction.RECEIVE);
		}

		CashFlow quoteSpotCf = new CashFlow();
		quoteSpotCf.setDate(trade.getSettlementDate());
		quoteSpotCf.setPurpose(TransferPurpose.QUOTE_CURRENCY_SPOT);
		quoteSpotCf.setAmount(trade.getAmount());
		quoteSpotCf.setCurrency(trade.getCurrency());

		if (trade.isBuy()) {
			quoteSpotCf.setDirection(CashFlow.Direction.PAY);
		} else {
			quoteSpotCf.setDirection(CashFlow.Direction.RECEIVE);
		}

		CashFlow quoteFwdCf = new CashFlow();
		quoteFwdCf.setDate(trade.getSettlementDateForward());
		quoteFwdCf.setPurpose(TransferPurpose.QUOTE_CURRENCY_FORWARD);
		quoteFwdCf.setAmount(trade.getAmountTwoForward());
		quoteFwdCf.setCurrency(trade.getCurrency());

		if (trade.isBuy()) {
			quoteFwdCf.setDirection(CashFlow.Direction.RECEIVE);
		} else {
			quoteFwdCf.setDirection(CashFlow.Direction.PAY);
		}

		List<CashFlow> cashFlows = new ArrayList<CashFlow>();

		if (primaryLegDiscountCurve != null) {
			try {
				PricerUtil.discountCashFlow(primarySpotCf, pricingDate, primaryLegDiscountCurve.getId(), null);
				PricerUtil.discountCashFlow(primaryFwdCf, pricingDate, primaryLegDiscountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		if (quoteLegDiscountCurve != null) {
			try {
				PricerUtil.discountCashFlow(quoteSpotCf, pricingDate, quoteLegDiscountCurve.getId(), null);
				PricerUtil.discountCashFlow(quoteFwdCf, pricingDate, quoteLegDiscountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlows.add(primarySpotCf);
		cashFlows.add(quoteSpotCf);
		cashFlows.add(primaryFwdCf);
		cashFlows.add(quoteFwdCf);

		return cashFlows;
	}

}