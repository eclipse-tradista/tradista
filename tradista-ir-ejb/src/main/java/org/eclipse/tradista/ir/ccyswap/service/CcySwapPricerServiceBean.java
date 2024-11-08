package org.eclipse.tradista.ir.ccyswap.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.ccyswap.service.CcySwapPricerService;
import org.eclipse.tradista.ir.irswap.pricer.PricerIRSwapUtil;

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
@Interceptors(CcySwapTradeProductScopeFilteringInterceptor.class)
public class CcySwapPricerServiceBean implements CcySwapPricerService {

	@Override
	public BigDecimal fixedLegPvDiscountedCashFlow(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed, it is not allowed to specify a pricing date in the past.");
			}
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrencyTwo(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		// 1. Determine the pending flows of the fixed leg
		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(trade.getCurrencyTwo());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrencyTwo()));
		}
		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getPaymentReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getPaymentReferenceRateIndex()));
		}
		BigDecimal discountedCashFlows = null;
		try {
			discountedCashFlows = PricerIRSwapUtil.discountFixedLegCashFlows(trade, irSwapCurrIRCurve.getId(),
					pricingDate, params.getQuoteSet(), indexCurve.getId());

			// Convert in the pricing currency
			discountedCashFlows = PricerUtil.convertAmount(discountedCashFlows, trade.getCurrencyTwo(), currency,
					pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}

		return discountedCashFlows;

	}

	@Override
	public BigDecimal floatingLegPvDiscountedCashFlow(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed, it is not allowed to specify a pricing date in the past.");
			}
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		// 1. Determine the pending flows of the fixed leg
		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}
		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReceptionReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReceptionReferenceRateIndex()));
		}
		BigDecimal discountedCashFlows;
		try {
			discountedCashFlows = PricerIRSwapUtil.discountFloatingLegCashFlows(trade, irSwapCurrIRCurve.getId(),
					pricingDate, params.getQuoteSet(), indexCurve.getId());
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}

		// Convert in the pricing currency
		discountedCashFlows = PricerUtil.convertAmount(discountedCashFlows, trade.getCurrency(), currency, pricingDate,
				params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

		return discountedCashFlows;
	}

	@Override
	public BigDecimal forwardSwapRateForwardSwapRate(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(trade.getCurrencyTwo());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrencyTwo()));
		}

		CcySwapTrade swap = new CcySwapTrade();
		swap.setNotionalAmountTwo(BigDecimal.ONE);
		swap.setCurrencyTwo(trade.getCurrencyTwo());
		swap.setPaymentFrequency(trade.getPaymentFrequency());
		swap.setPaymentFixedInterestRate(trade.getPaymentFixedInterestRate());
		swap.setPaymentDayCountConvention(trade.getPaymentDayCountConvention());
		swap.setMaturityDate(trade.getMaturityDate());
		swap.setSettlementDate(trade.getSettlementDate());
		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getPaymentReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getPaymentReferenceRateIndex()));
		}
		try {
			BigDecimal a = PricerIRSwapUtil.discountFixedLegCashFlows(swap, irSwapCurrIRCurve.getId(), pricingDate,
					params.getQuoteSet(), indexCurve.getId());

			BigDecimal p0 = PricerUtil.getDiscountFactor(irSwapCurrIRCurve, swap.getSettlementDate());

			BigDecimal pn = PricerUtil.getDiscountFactor(irSwapCurrIRCurve, swap.getMaturityDate());

			return p0.subtract(pn).divide(a);
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed, it is not allowed to specify a pricing date in the past.");
			}
		}

		BigDecimal npv = fixedLegPvDiscountedCashFlow(params, trade, currency, pricingDate)
				.subtract(floatingLegPvDiscountedCashFlow(params, trade, currency, pricingDate));

		if (trade.isBuy()) {
			npv = npv.negate();
		}

		return npv;
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, CcySwapTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return realizedPnlLegsCashFlows(params, trade, currency, pricingDate)
				.add(unrealizedPnlLegsDiff(params, trade, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlLegsCashFlows(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		try {
			if (pricingDate.isAfter(trade.getMaturityDate()) || pricingDate.equals(trade.getMaturityDate())) {
				// We calculate the cashflows made for this trade (no discount).
				BigDecimal realizedPnl = PricerIRSwapUtil.getRealizedPNL(trade, params.getQuoteSet().getId(), currency);

				if (trade.isSell()) {
					realizedPnl = realizedPnl.negate();
				}
				return realizedPnl;
			}

		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}

		// if pricing date is before maturity date, there is no realized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getMaturityDate())) {
			return npvDiscountedLegsDiff(params, trade, currency, pricingDate);
		}

		// if pricing date is after maturity date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, CcySwapTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		List<CashFlow> floatingLegCfs = new ArrayList<CashFlow>();
		List<CashFlow> fixedLegCfs = new ArrayList<CashFlow>();
		List<CashFlow> cashFlows;
		List<CashFlow> cashFlowsToReturn = new ArrayList<CashFlow>();

		if (!LocalDate.now().isBefore(trade.getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the trade maturity date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade maturity date, it is not possible to forecast cashflows.");
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed and the pricing date is in the past, it is not possible to forecast cashflows.");
			}
		}

		InterestRateCurve paymentIndexCurve = null;
		if (!trade.isInterestsToPayFixed()) {
			paymentIndexCurve = params.getIndexCurves().get(trade.getPaymentReferenceRateIndex());
			if (paymentIndexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getPaymentReferenceRateIndex()));
			}
		}

		InterestRateCurve receptionIndexCurve = params.getIndexCurves().get(trade.getReceptionReferenceRateIndex());
		if (receptionIndexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReceptionReferenceRateIndex()));
		}

		// Primary currency IR curve retrieval
		InterestRateCurve discountCurveFloatingLeg = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurveFloatingLeg == null) {
			// TODO Add log warn
		}

		// Quote currency IR curve retrieval
		InterestRateCurve discountCurveFixedLeg = params.getDiscountCurves().get(trade.getCurrencyTwo());
		if (discountCurveFixedLeg == null) {
			// TODO Add log warn
		}

		cashFlows = PricerIRSwapUtil.generateCashFlows(trade, pricingDate, params.getQuoteSet(),
				paymentIndexCurve != null ? paymentIndexCurve.getId() : 0, receptionIndexCurve.getId());

		if (cashFlows != null && !cashFlows.isEmpty()) {
			for (CashFlow cf : cashFlows) {
				if (cf.getPurpose().equals(TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT)
						|| (cf.getPurpose().equals(TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT))
						|| (cf.getPurpose().equals(TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT))) {
					floatingLegCfs.add(cf);
				}
				if (cf.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)
						|| (cf.getPurpose().equals(TransferPurpose.FIXED_LEG_NOTIONAL_PAYMENT))
						|| (cf.getPurpose().equals(TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT))) {
					fixedLegCfs.add(cf);
				}
			}
		}

		if (discountCurveFloatingLeg != null) {
			try {
				PricerUtil.discountCashFlows(floatingLegCfs, pricingDate, discountCurveFloatingLeg.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlowsToReturn.addAll(floatingLegCfs);

		if (discountCurveFixedLeg != null) {
			try {
				PricerUtil.discountCashFlows(fixedLegCfs, pricingDate, discountCurveFixedLeg.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlowsToReturn.addAll(fixedLegCfs);

		Collections.sort(cashFlowsToReturn);

		return cashFlowsToReturn;
	}

}