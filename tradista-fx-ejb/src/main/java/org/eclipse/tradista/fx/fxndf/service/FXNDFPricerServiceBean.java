package org.eclipse.tradista.fx.fxndf.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
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
@Interceptors(FXNDFTradeProductScopeFilteringInterceptor.class)
public class FXNDFPricerServiceBean implements FXNDFPricerService {

	private FXNDFTradeBusinessDelegate fxNdfTradeBusinessDelegate;

	@PostConstruct
	public void init() {
		fxNdfTradeBusinessDelegate = new FXNDFTradeBusinessDelegate();
	}

	@Override
	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (!LocalDate.now().isBefore(trade.getSettlementDate()) || !pricingDate.isBefore(trade.getSettlementDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		BigDecimal npv;

		// 1. Apply the fixing rate to the discountedNominal
		CurrencyPair pair = new CurrencyPair(trade.getNonDeliverableCurrency(), trade.getCurrency());
		FXCurve paramNDFFXCurve = params.getFxCurves().get(pair);
		if (paramNDFFXCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a FX Curve for %s.%s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getNonDeliverableCurrency(), trade.getCurrency()));
		}

		pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		BigDecimal convertedNominal = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(),
				trade.getNonDeliverableCurrency(), fxNdfTradeBusinessDelegate.getFixingDate(trade),
				params.getQuoteSet().getId(), paramNDFFXCurve.getId());

		// 2. Apply the contract rate
		BigDecimal contractAdjustNominal = trade.getAmount().multiply(trade.getNdfRate());

		// 1.a Primary currency IR curve retrieval
		InterestRateCurve paramSettleCurrIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (paramSettleCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a Discount Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		// for now, only use of ACT/360 day count convention
		DayCountConvention dcc = new DayCountConvention("ACT/360");

		try {
			if (trade.isBuy()) {
				npv = PricerUtil.discountAmount(contractAdjustNominal.subtract(convertedNominal),
						paramSettleCurrIRCurve.getId(), pricingDate, trade.getSettlementDate(), dcc);
			} else {
				npv = PricerUtil.discountAmount(convertedNominal.subtract(contractAdjustNominal),
						paramSettleCurrIRCurve.getId(), pricingDate, trade.getSettlementDate(), dcc);
			}
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}

		// Finally apply the conversion to the pricing currency
		npv = PricerUtil.convertAmount(npv, trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramFXCurve != null ? paramFXCurve.getId() : 0);

		return npv;
	}

	@Override
	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDate()) || pricingDate.equals(trade.getSettlementDate())) {
			// We use the npv with a pricing date equals to the trade settlement
			// date, so there is no discount calculated on the PNL realized at
			// settlement date.
			return markToMarket(params, trade, currency, trade.getSettlementDate());
		}

		// if pricing date is before settlement date, there is no realized pnl.
		return BigDecimal.ZERO;

	}

	@Override
	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getSettlementDate())) {
			return npvDiscountedLegsDiff(params, trade, currency, pricingDate);
		}

		// if pricing date is after settlement date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getSettlementDate())) {
			// If the trade is not yet settled, we calculate the Mark to Market to estimate
			// the unrealized pnl.
			return markToMarket(params, trade, currency, pricingDate);
		}

		// if pricing date is after settlement date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	protected BigDecimal markToMarket(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		// 1. Get the current FX rate:
		BigDecimal currentFxRate = PricerUtil.getFXExchangeRate(trade.getCurrency(), trade.getNonDeliverableCurrency(),
				pricingDate, params.getQuoteSet().getId(), 0);

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		// 2. Calculate the diff
		BigDecimal diff = currentFxRate.subtract(trade.getNdfRate());

		// 3. Multiply the diff by the trade amount in quote currency, we will get the
		// MTM (expressed in the quote currency).
		BigDecimal mtm = diff.multiply(trade.getAmount());

		// Finally apply the conversion to the pricing currency
		mtm = PricerUtil.convertAmount(mtm, trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramFXCurve != null ? paramFXCurve.getId() : 0);

		return mtm;
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, FXNDFTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return unrealizedPnlLegsDiff(params, trade, currency, pricingDate)
				.add(realizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, FXNDFTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			throw new TradistaBusinessException(
					"When the trade settlement date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getSettlementDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade settlement date, it is not possible to forecast cashflows.");
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), trade.getNonDeliverableCurrency());
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			// TODO Add log
		}

		CashFlow cf = new CashFlow();
		cf.setDate(trade.getSettlementDate());
		cf.setPurpose(TransferPurpose.CASH_SETTLEMENT);
		cf.setCurrency(trade.getCurrency());

		BigDecimal difference = trade.getNdfRate()
				.subtract(PricerUtil.getFXExchangeRate(trade.getNonDeliverableCurrency(), trade.getCurrency(),
						trade.getSettlementDate(), params.getQuoteSet().getId(),
						paramFXCurve != null ? paramFXCurve.getId() : 0));

		cf.setAmount(trade.getAmount().multiply(difference.abs()));

		if (trade.isBuy()) {
			if (difference.signum() >= 0) {
				cf.setDirection(CashFlow.Direction.PAY);
			} else {
				cf.setDirection(CashFlow.Direction.RECEIVE);
			}
		} else {
			if (difference.signum() >= 0) {
				cf.setDirection(CashFlow.Direction.RECEIVE);
			} else {
				cf.setDirection(CashFlow.Direction.PAY);
			}
		}

		List<CashFlow> cashFlows = new ArrayList<CashFlow>();

		if (discountCurve != null) {
			try {
				PricerUtil.discountCashFlow(cf, pricingDate, discountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlows.add(cf);

		return cashFlows;
	}

}