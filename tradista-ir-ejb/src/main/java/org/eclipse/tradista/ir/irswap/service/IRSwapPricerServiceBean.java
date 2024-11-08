package org.eclipse.tradista.ir.irswap.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.pricer.PricerIRSwapUtil;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerService;

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
@Interceptors(IRSwapTradeProductScopeFilteringInterceptor.class)
public class IRSwapPricerServiceBean implements IRSwapPricerService {

	@Override
	public BigDecimal fixedLegPvDiscountedCashFlow(PricingParameter params, SingleCurrencyIRSwapTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {

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

		// 1. Determine the pending flows of the fixed leg
		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		InterestRateCurve indexCurve = null;
		if (!trade.isInterestsToPayFixed()) {
			indexCurve = params.getIndexCurves().get(trade.getPaymentReferenceRateIndex());
			if (indexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getPaymentReferenceRateIndex()));
			}
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		try {
			BigDecimal discountedCashFlows = PricerIRSwapUtil.discountFixedLegCashFlows(trade,
					irSwapCurrIRCurve.getId(), pricingDate, params.getQuoteSet(),
					indexCurve == null ? 0 : indexCurve.getId());

			discountedCashFlows = PricerUtil.convertAmount(discountedCashFlows, trade.getCurrency(), currency,
					pricingDate, params.getQuoteSet().getId(),
					paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

			return discountedCashFlows;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal floatingLegPvDiscountedCashFlow(PricingParameter params, SingleCurrencyIRSwapTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {

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

		// 1. Determine the pending flows of the fixed leg
		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}
		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReceptionReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"Pricing Parameters Set '%s' doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReceptionReferenceRateIndex()));
		}
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}
		try {
			BigDecimal discountedCashFlows = PricerIRSwapUtil.discountFloatingLegCashFlows(trade,
					irSwapCurrIRCurve.getId(), pricingDate, params.getQuoteSet(), indexCurve.getId());

			discountedCashFlows = PricerUtil.convertAmount(discountedCashFlows, trade.getCurrency(), currency,
					pricingDate, params.getQuoteSet().getId(),
					paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

			return discountedCashFlows;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal forwardSwapRateForwardSwapRate(PricingParameter params, SingleCurrencyIRSwapTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {

		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReceptionReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"Pricing Parameter '%s' doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReceptionReferenceRateIndex()));
		}

		try {
			// First, calculate the value of the floating leg
			BigDecimal floatingLegPv = floatingLegPvDiscountedCashFlow(params, trade, currency, pricingDate);

			// Then, iterates until the value of the forward swap rate is found.
			// Starting value: the index value as of trade settlement date
			BigDecimal fwr = PricerUtil.getValueAsOfDateFromCurve(indexCurve.getId(), trade.getSettlementDate());

			boolean found = false;
			double inc = 0.1;
			double lastDiffSign = 0;
			while (!found) {
				trade.setPaymentFixedInterestRate(fwr);
				BigDecimal fixedLegPv = fixedLegPvDiscountedCashFlow(params, trade, currency, pricingDate);
				BigDecimal diff = floatingLegPv.subtract(fixedLegPv);
				if (diff.abs().doubleValue() <= 0.01) {
					found = true;
				} else {
					if (diff.signum() == -1) {
						fwr = fwr.subtract(BigDecimal.valueOf(inc));
						if (lastDiffSign != 0) {
							if (diff.signum() != lastDiffSign) {
								inc = inc / 2;
							}
						}
						lastDiffSign = -1;
					} else {
						if (diff.signum() == 1) {
							fwr = fwr.add(BigDecimal.valueOf(inc));
							if (lastDiffSign != 0) {
								if (diff.signum() != lastDiffSign) {
									inc = inc / 2;
								}
							}
							lastDiffSign = 1;
						}
					}
				}
			}

			return fwr;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, SingleCurrencyIRSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		BigDecimal npv = fixedLegPvDiscountedCashFlow(params, trade, currency, pricingDate)
				.subtract(floatingLegPvDiscountedCashFlow(params, trade, currency, pricingDate));

		if (trade.isBuy()) {
			npv = npv.negate();
		}

		return npv;
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, SingleCurrencyIRSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlLegsCashFlows(params, trade, currency, pricingDate)
				.add(unrealizedPnlLegsDiff(params, trade, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlLegsCashFlows(PricingParameter params, SingleCurrencyIRSwapTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
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
	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, SingleCurrencyIRSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getMaturityDate())) {
			return npvDiscountedLegsDiff(params, trade, currency, pricingDate);
		}

		// if pricing date is after maturity date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, SingleCurrencyIRSwapTrade trade,
			LocalDate pricingDate) throws TradistaBusinessException {

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

		InterestRateCurve paymentReferenceRateIndexCurve = null;

		if (!trade.isInterestsToPayFixed()) {
			paymentReferenceRateIndexCurve = params.getIndexCurves().get(trade.getPaymentReferenceRateIndex());
			if (paymentReferenceRateIndexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an index curve for index %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getPaymentReferenceRateIndex()));
			}
		}

		InterestRateCurve receptionReferenceRateIndexCurve = params.getIndexCurves()
				.get(trade.getReceptionReferenceRateIndex());
		if (receptionReferenceRateIndexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an index curve for index %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReceptionReferenceRateIndex()));
		}
		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			// TODO Add log warn
		}

		List<CashFlow> cashFlows = PricerIRSwapUtil.generateCashFlows(trade, pricingDate, params.getQuoteSet(),
				paymentReferenceRateIndexCurve != null ? paymentReferenceRateIndexCurve.getId() : 0,
				receptionReferenceRateIndexCurve.getId());

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