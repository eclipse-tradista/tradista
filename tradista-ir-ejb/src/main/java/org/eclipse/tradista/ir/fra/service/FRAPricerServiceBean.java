package org.eclipse.tradista.ir.fra.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.jboss.ejb3.annotation.SecurityDomain;

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
@Interceptors(FRATradeProductScopeFilteringInterceptor.class)
public class FRAPricerServiceBean implements FRAPricerService {

	@Override
	public BigDecimal npvValuation(PricingParameter params, FRATrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getStartDate()) || !pricingDate.isBefore(trade.getStartDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		InterestRateCurve fraIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (fraIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReferenceRateIndex()));
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		try {
			BigDecimal fwdRate = PricerUtil.getForwardRate(indexCurve.getId(), trade.getStartDate(), trade.getEndDate(),
					trade.getDayCountConvention());

			BigDecimal npv = PricerUtil.discountAmount(
					trade.getAmount().multiply(trade.getFixedRate().subtract(fwdRate))
							.multiply(PricerUtil.daysToYear(trade.getDayCountConvention(), trade.getStartDate(),
									trade.getEndDate())),
					fraIRCurve.getId(), pricingDate, trade.getPaymentDate(), trade.getDayCountConvention());

			if (trade.isSell()) {
				npv = npv.negate();
			}

			npv = PricerUtil.convertAmount(npv, trade.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

			return npv;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, FRATrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return realizedPnlDefault(params, trade, currency, pricingDate)
				.add(unrealizedPnlDefault(params, trade, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlDefault(PricingParameter params, FRATrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getPaymentDate()) || pricingDate.equals(trade.getPaymentDate())) {
			// We use the npv with a pricing date equals to the trade payment
			// date, so there is no discount calculated on the PNL realized at
			// payment date.
			return npvValuation(params, trade, currency, trade.getPaymentDate());
		}

		// if pricing date is before payment date, there is no realized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlDefault(PricingParameter params, FRATrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getPaymentDate())) {
			return npvValuation(params, trade, currency, pricingDate);
		}

		// if pricing date is after payment date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, FRATrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getPaymentDate())) {
			throw new TradistaBusinessException(
					"When the trade payment date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getPaymentDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade payment date, it is not possible to forecast cashflows.");
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			// TODO Add log warn
		}

		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an index curve for index %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReferenceRateIndex()));
		}

		CashFlow cf = new CashFlow();
		cf.setDate(trade.getMaturityDate());
		cf.setPurpose(TransferPurpose.CASH_SETTLEMENT);
		cf.setCurrency(trade.getCurrency());

		BigDecimal rate;
		try {
			rate = PricerUtil.getInterestRateAsOfDate(
					Index.INDEX + "." + trade.getReferenceRateIndex() + "." + trade.getReferenceRateIndexTenor(),
					params.getQuoteSet().getId(), indexCurve.getId(), trade.getReferenceRateIndexTenor(),
					trade.getDayCountConvention(), trade.getStartDate());
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}

		if (rate == null) {
			throw new TradistaBusinessException(String.format(
					"The rate could not be determined as of date %tD. Impossible to generate the cashflows.",
					pricingDate));
		}

		cf.setAmount(trade.getAmount()
				.multiply(trade.getFixedRate().subtract(rate).divide(new BigDecimal("100"), RoundingMode.HALF_EVEN))
				.multiply(PricerUtil.daysToYear(trade.getDayCountConvention(), trade.getStartDate(),
						trade.getEndDate())));

		if (trade.isBuy()) {
			if (cf.getAmount().signum() >= 0) {
				cf.setDirection(CashFlow.Direction.RECEIVE);
			} else {
				cf.setDirection(CashFlow.Direction.PAY);
				cf.setAmount(cf.getAmount().negate());
			}
		} else {
			if (cf.getAmount().signum() < 0) {
				cf.setDirection(CashFlow.Direction.RECEIVE);
				cf.setAmount(cf.getAmount().negate());
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