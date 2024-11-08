package org.eclipse.tradista.fx.fx.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationService;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXPricerService;

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
@Interceptors(FXProductScopeFilteringInterceptor.class)
public class FXPricerServiceBean implements FXPricerService {

	@EJB
	private ConfigurationService configurationService;

	@Override
	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (!LocalDate.now().isBefore(trade.getSettlementDate()) || !pricingDate.isBefore(trade.getSettlementDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		if (trade.isBuy()) {
			return primaryPvDiscountedLegsDiff(params, trade, currency, pricingDate)
					.subtract(quotePvDiscountedLegsDiff(params, trade, currency, pricingDate));
		} else {
			return quotePvDiscountedLegsDiff(params, trade, currency, pricingDate)
					.subtract(primaryPvDiscountedLegsDiff(params, trade, currency, pricingDate));
		}
	}

	@Override
	public BigDecimal primaryPvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (!LocalDate.now().isBefore(trade.getSettlementDate()) || !pricingDate.isBefore(trade.getSettlementDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		try {
			Currency primaryCurrency = trade.getCurrencyOne();
			CurrencyPair pair = new CurrencyPair(primaryCurrency, currency);
			FXCurve paramFXCurve = params.getFxCurves().get(pair);
			if (paramFXCurve == null) {
				// TODO Add log warn
			}
			// 1. Primary currency IR curve retrieval
			InterestRateCurve paramPrimCurrIRCurve = params.getDiscountCurves().get(primaryCurrency);
			if (paramPrimCurrIRCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
						params.getName(), primaryCurrency));
			}

			DayCountConvention dcc = new DayCountConvention("ACT/360");

			// 2. Discount the primary leg
			BigDecimal discountedPrimaryLeg = PricerUtil.discountAmount(trade.getAmountOne(),
					paramPrimCurrIRCurve.getId(), pricingDate, trade.getSettlementDate(), dcc);

			// Finally apply the conversion to the pricing currency
			discountedPrimaryLeg = PricerUtil.convertAmount(discountedPrimaryLeg, primaryCurrency, currency,
					pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

			return discountedPrimaryLeg;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal quotePvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (!LocalDate.now().isBefore(trade.getSettlementDate()) || !pricingDate.isBefore(trade.getSettlementDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		try {
			Currency quoteCurrency = ((FXTrade) trade).getCurrency();
			// Quote currency IR curve retrieval
			InterestRateCurve paramQuoteCurrIRCurve = params.getDiscountCurves().get(quoteCurrency);
			if (paramQuoteCurrIRCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
						params.getName(), quoteCurrency));
			}

			CurrencyPair pair = new CurrencyPair(quoteCurrency, currency);
			FXCurve paramFXCurve = params.getFxCurves().get(pair);
			if (paramFXCurve == null) {
				// TODO add a log warn
			}

			DayCountConvention dcc = new DayCountConvention("ACT/360");

			// Discount the quote leg
			BigDecimal discountedQuoteLeg = PricerUtil.discountAmount(trade.getAmount(), paramQuoteCurrIRCurve.getId(),
					pricingDate, trade.getSettlementDate(), dcc);

			// Finally apply the conversion to the pricing currency
			discountedQuoteLeg = PricerUtil.convertAmount(discountedQuoteLeg, quoteCurrency, currency, pricingDate,
					params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

			return discountedQuoteLeg;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDate()) || pricingDate.equals(trade.getSettlementDate())) {
			// Calculation of the MTM
			return markToMarket(params, trade, currency, trade.getSettlementDate());
		}

		// if pricing date is before settlement date, there is no realized pnl.
		return BigDecimal.ZERO;

	}

	@Override
	public BigDecimal unrealizedPnlDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDate()) || pricingDate.equals(trade.getSettlementDate())) {
			// if pricing date is equal to or after the settlement date, the pnl
			// is already realized
			return BigDecimal.ZERO;
		}

		// If the trade is not yet settled, we use the NPV to estimate the
		// unrealized pnl.
		return npvDiscountedLegsDiff(params, trade, currency, pricingDate);
	}

	@Override
	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getSettlementDate()) || pricingDate.equals(trade.getSettlementDate())) {
			// if pricing date is equal to or after the settlement date, the pnl
			// is already realized
			return BigDecimal.ZERO;
		}

		// If the trade is not yet settled, we calculate the Mark to Market to estimate
		// the unrealized pnl.
		return markToMarket(params, trade, currency, pricingDate);
	}

	protected BigDecimal markToMarket(PricingParameter params, FXTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}
		// 1. Get the current FX rate:
		BigDecimal currentFxRate = PricerUtil.getFXExchangeRate(trade.getCurrencyOne(), trade.getCurrency(),
				pricingDate, params.getQuoteSet().getId(), 0);

		// 2. Get the FX rate used for the trade:
		BigDecimal tradeFXRate = trade.getAmount().divide(trade.getAmountOne(), configurationService.getScale(),
				configurationService.getRoundingMode());

		// 3. Calculate the diff
		BigDecimal diff = currentFxRate.subtract(tradeFXRate);

		// 4. Multiply the diff by the trade amount in quote currency, we will get the
		// MTM (expressed in the quote currency).
		BigDecimal mtm = diff.multiply(trade.getAmountOne());

		// Finally apply the conversion to the pricing currency
		mtm = PricerUtil.convertAmount(mtm, trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramFXCurve != null ? paramFXCurve.getId() : 0);

		return mtm;
	}

	@Override
	public BigDecimal defaultPNL(PricingParameter params, FXTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return realizedPnlMarkToMarket(params, trade, currency, pricingDate)
				.add(unrealizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, FXTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			throw new TradistaBusinessException(
					"When the trade settlement date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getSettlementDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade settlement date, it is not possible to forecast cashflows.");
		}

		CashFlow primaryCf = new CashFlow();
		primaryCf.setDate(trade.getSettlementDate());
		primaryCf.setPurpose(TransferPurpose.PRIMARY_CURRENCY);
		primaryCf.setAmount(trade.getAmountOne());
		primaryCf.setCurrency(trade.getCurrencyOne());

		if (trade.isBuy()) {
			primaryCf.setDirection(CashFlow.Direction.RECEIVE);
		} else {
			primaryCf.setDirection(CashFlow.Direction.PAY);
		}

		InterestRateCurve primaryLegDiscountCurve = params.getDiscountCurves().get(trade.getCurrencyOne());

		CashFlow quoteCf = new CashFlow();
		quoteCf.setDate(trade.getSettlementDate());
		quoteCf.setPurpose(TransferPurpose.QUOTE_CURRENCY);
		quoteCf.setAmount(trade.getAmount());
		quoteCf.setCurrency(trade.getCurrency());

		if (trade.isBuy()) {
			quoteCf.setDirection(CashFlow.Direction.PAY);
		} else {
			quoteCf.setDirection(CashFlow.Direction.RECEIVE);
		}

		InterestRateCurve quoteLegDiscountCurve = params.getDiscountCurves().get(trade.getCurrency());

		List<CashFlow> cashFlows = new ArrayList<CashFlow>();

		if (primaryLegDiscountCurve != null) {
			try {
				PricerUtil.discountCashFlow(primaryCf, pricingDate, primaryLegDiscountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		if (quoteLegDiscountCurve != null) {
			try {
				PricerUtil.discountCashFlow(quoteCf, pricingDate, quoteLegDiscountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlows.add(primaryCf);
		cashFlows.add(quoteCf);

		return cashFlows;
	}

}