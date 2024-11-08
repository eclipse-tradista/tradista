package org.eclipse.tradista.mm.loandeposit.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.mm.loandeposit.model.DepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.pricer.PricerLoanDepositUtil;

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
@Interceptors(LoanDepositTradeProductScopeFilteringInterceptor.class)
public class LoanDepositPricerServiceBean implements LoanDepositPricerService {

	@Override
	public BigDecimal npvDiscountedCashFlow(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getEndDate()) || !pricingDate.isBefore(trade.getEndDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed, it is not allowed to specify a pricing date in the past.");
			}
		}

		BigDecimal npv;
		// First, sum all the discounted coupons
		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		InterestRateCurve indexCurve = null;
		if (trade.getFloatingRateIndex() != null) {
			indexCurve = params.getIndexCurves().get(trade.getFloatingRateIndex());
			if (indexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getFloatingRateIndex()));
			}
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		try {
			BigDecimal discountedPayments = PricerUtil.getTotalFlowsAmount(
					PricerLoanDepositUtil.getPayments(trade, pricingDate, params.getQuoteSet().getId(),
							indexCurve != null ? indexCurve.getId() : 0),
					null, pricingDate, discountCurve.getId(), trade.getDayCountConvention());

			// Calculate discounted notional at start date
			BigDecimal discountedNotional = PricerUtil.discountAmount(trade.getAmount(), discountCurve.getId(),
					pricingDate, trade.getSettlementDate(), trade.getDayCountConvention());

			// Calculate discounted notional at end date
			BigDecimal discountedGivenBackNotional = PricerUtil.discountAmount(trade.getAmount(), discountCurve.getId(),
					pricingDate, trade.getEndDate(), trade.getDayCountConvention());

			// Case of Loan: discountedNotional - payments + discountedGivenBackNotional
			npv = discountedNotional.subtract(discountedPayments).subtract(discountedGivenBackNotional);

			if (trade.isSell()) {
				npv = npv.negate();
			}

			if (trade instanceof DepositTrade) {
				npv = npv.negate();
			}

			// Finally, convert in pricing currency
			npv = PricerUtil.convertAmount(npv, trade.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

			return npv;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlCashFlows(params, trade, currency, pricingDate)
				.add(unrealizedPnlDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlCashFlows(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getEndDate()) || pricingDate.equals(trade.getEndDate())) {
			return PricerUtil.getTotalFlowsAmount(
					PricerLoanDepositUtil.getPayments(trade, pricingDate, params.getQuoteSet().getId(), 0), currency,
					null, 0, null);
		}

		// if pricing date is before settlement date, there is no realized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlDiscountedCashFlow(PricingParameter params, LoanDepositTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getEndDate())) {
			return npvDiscountedCashFlow(params, trade, currency, pricingDate);
		}

		// if pricing date is after end date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, LoanDepositTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getEndDate())) {
			throw new TradistaBusinessException(
					"When the trade end date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getEndDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade end date, it is not possible to forecast cashflows.");
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade start date has passed and the pricing date is in the past, it is not possible to forecast cashflows.");
			}
		}

		InterestRateCurve paymentIndexCurve = null;

		if (!trade.isFixed()) {
			paymentIndexCurve = params.getIndexCurves().get(trade.getFloatingRateIndex());
			if (paymentIndexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getFloatingRateIndex()));
			}
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		List<CashFlow> cashFlows = PricerLoanDepositUtil.generateCashFlows(trade, pricingDate, params.getQuoteSet(),
				paymentIndexCurve != null ? paymentIndexCurve.getId() : 0);

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