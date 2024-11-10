package org.eclipse.tradista.ir.irswapoption.service;

import static org.eclipse.tradista.core.pricing.util.PricerUtil.cnd;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerBusinessDelegate;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerService;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
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
@Interceptors(IRSwapOptionTradeProductScopeFilteringInterceptor.class)
public class IRSwapOptionPricerServiceBean implements IRSwapOptionPricerService {

	@EJB
	private SwaptionVolatilitySurfaceService swaptionVolatilitySurfaceService;

	@EJB
	private IRSwapPricerService irSwapPricerService;

	@Override
	public BigDecimal forwardSwapRateForwardSwapRate(PricingParameter params, IRSwapOptionTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {

		SingleCurrencyIRSwapTrade underlying = trade.getUnderlying();
		// We need to simulate the settlement date of the underlying in order to
		// calculate its Forward swap rate
		// So, simulated settlement date will be pricing date + settlement
		// offset
		underlying.setSettlementDate(DateUtil.addBusinessDay(pricingDate,
				trade.getUnderlying().getCurrency().getCalendar(), trade.getSettlementDateOffset()));
		// underlying trade date must be set to pricing date
		underlying.setTradeDate(pricingDate);

		return irSwapPricerService.forwardSwapRateForwardSwapRate(params, trade.getUnderlying(), currency, pricingDate);
	}

	@Override
	public BigDecimal npvBlack(PricingParameter params, IRSwapOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		BigDecimal npv;
		BigDecimal pv = null;

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramPremiumCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramPremiumCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			pv = irSwapPricerService.npvDiscountedLegsDiff(params, trade.getUnderlying(), currency, pricingDate);
		}
		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		if (trade.getExerciseDate() == null) {
			pv = pvBlack(params, trade, currency, pricingDate);
		}

		// convert the premium, then add (or subtract) it from the PV, depending
		// of the trade direction
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramPremiumCcyPricingCcyFXCurve != null ? paramPremiumCcyPricingCcyFXCurve.getId() : 0);
		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal pvBlack(PricingParameter params, IRSwapOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (trade.getExerciseDate() != null) {
			trade.getUnderlying().setSettlementDate(trade.getUnderlyingSettlementDate());
			if (trade.isBuy()) {
				return irSwapPricerService.fixedLegPvDiscountedCashFlow(params, trade.getUnderlying(), currency,
						pricingDate);
			} else {
				return irSwapPricerService.floatingLegPvDiscountedCashFlow(params, trade.getUnderlying(), currency,
						pricingDate);
			}
		}

		if (!LocalDate.now().isBefore(trade.getMaturityDate()) || !pricingDate.isBefore(trade.getMaturityDate())
				|| trade.getExerciseDate() != null) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}
		SingleCurrencyIRSwapTrade underlying = trade.getUnderlying();

		if (trade.getStyle().equals(VanillaOptionTrade.Style.AMERICAN)) {
			throw new TradistaBusinessException("Black valuation formula cannot be used with an American Option.");
		}

		InterestRateCurve irSwapCurrIRCurve = params.getDiscountCurves().get(underlying.getCurrency());
		if (irSwapCurrIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), underlying.getCurrency()));
		}

		InterestRateCurve paymentReferenceRateIndexCurve = params.getIndexCurves()
				.get(underlying.getPaymentReferenceRateIndex());
		if (paymentReferenceRateIndexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an index curve for index %s. please add it or change the Pricing Parameter.",
					params.getName(), underlying.getPaymentReferenceRateIndex()));
		}

		CurrencyPair pair = new CurrencyPair(underlying.getCurrency(), currency);
		FXCurve paramUndCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramUndCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// 1. Calculation of the Forward Swap Forward Rate

		BigDecimal fsr = forwardSwapRateForwardSwapRate(params, trade, currency, pricingDate);

		// Volatility surface
		// We retrieve the swaption volatility surface from the PP Swaption Volatility
		// Surface
		// Module for the underlying's reference index
		PricingParameterVolatilitySurfaceModule module = null;
		for (PricingParameterModule mod : params.getModules()) {
			if (mod instanceof PricingParameterVolatilitySurfaceModule) {
				module = (PricingParameterVolatilitySurfaceModule) mod;
				break;
			}
		}
		SwaptionVolatilitySurface surface = module
				.getSwaptionVolatilitySurface(underlying.getReceptionReferenceRateIndex());
		if (surface == null) {
			throw new TradistaBusinessException(String.format(
					"The Swaption volatility surface doesn't exist for the index %s. please add it or change the Pricing Parameters Set.",
					underlying.getReceptionReferenceRateIndex()));
		}

		// Gets the implied volatility from a surface stored in the system.
		// Param in : swaption time to maturity, swap tenor
		int maturity = PricerUtil.daysToYear(trade.getTradeDate(), trade.getMaturityDate()).intValue();
		int tenor = PricerUtil.daysToYear(underlying.getSettlementDate(), underlying.getMaturityDate()).intValue();
		BigDecimal volat;
		try {
			volat = surface.getVolatilityByOptionExpiryAndSwapTenor(maturity, tenor);
		} catch (TradistaBusinessException abe) {
			throw new TradistaBusinessException(abe.getMessage());
		}

		try {

			LocalDate varDate = underlying.getSettlementDate();
			Tenor frequency = underlying.getPaymentFrequency();
			BigDecimal k = underlying.getPaymentFixedInterestRate();
			BigDecimal pv = BigDecimal.ZERO;

			// Now, we will iterate over the underlying cashflows to calculate the
			// value
			BigDecimal discount = BigDecimal.ZERO;

			// if frequency = 1, nominal fraction is nominal
			BigDecimal nominalFraction = underlying.getAmount();
			if (frequency.equals(Tenor.THREE_MONTHS)) {
				nominalFraction = nominalFraction.multiply(new BigDecimal("0.25"));
			}
			if (frequency.equals(Tenor.SIX_MONTHS)) {
				nominalFraction = nominalFraction.multiply(new BigDecimal("0.5"));
			}

			while (!varDate.isAfter(underlying.getMaturityDate())) {
				if (varDate.isAfter(underlying.getSettlementDate())) {

					BigDecimal d1 = BigDecimal.ZERO;
					BigDecimal d2 = BigDecimal.ZERO;
					BigDecimal p = PricerUtil.getDiscountFactor(irSwapCurrIRCurve, varDate);

					if (frequency.equals(Tenor.THREE_MONTHS)) {
						discount = discount.add(new BigDecimal("0.25"));
					}
					if (frequency.equals(Tenor.SIX_MONTHS)) {
						discount = discount.add(new BigDecimal("0.5"));
					}
					if (frequency.equals(Tenor.ONE_YEAR)) {
						discount = discount.add(BigDecimal.ONE);
					}

					BigDecimal discountedNominalFraction = nominalFraction
							.multiply(BigDecimal.valueOf(Math.exp(p.negate().multiply(discount).doubleValue())));
					if (!underlying.isInterestsToPayFixed()) {
						k = PricerUtil.getDiscountFactor(paymentReferenceRateIndexCurve, varDate);
					}

					d1 = BigDecimal.valueOf(Math.log(fsr.doubleValue() / k.doubleValue()))
							.add(volat.pow(2).multiply((discount.divide(BigDecimal.valueOf(2)))))
							.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

					d2 = d1.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

					if (trade.isCall()) {
						pv.add(discountedNominalFraction.multiply(
								(fsr.multiply(cnd(d1.doubleValue())).subtract(k.multiply(cnd(d2.doubleValue()))))));
					} else {
						pv.add(discountedNominalFraction.multiply(
								k.multiply(cnd(-d2.doubleValue())).subtract(fsr.multiply(cnd(-d1.doubleValue())))));
					}

				}
				varDate = DateUtil.addTenor(varDate, frequency);
			}

			pv = PricerUtil.convertAmount(pv, underlying.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramUndCcyPricingCcyFXCurve != null ? paramUndCcyPricingCcyFXCurve.getId() : 0);

			return pv;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal realizedPnlOptionExercise(PricingParameter params, IRSwapOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramPremiumCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramPremiumCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// convert the premium
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(),
				paramPremiumCcyPricingCcyFXCurve != null ? paramPremiumCcyPricingCcyFXCurve.getId() : 0);

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
		BigDecimal npvLegsCashFlows = new IRSwapPricerBusinessDelegate().realizedPnlLegsCashFlows(params,
				trade.getUnderlying(), currency, pricingDate);

		// add (or subtract) the premium from the realized PNL,
		// depending of the trade direction

		if (trade.isBuy()) {
			npvLegsCashFlows = npvLegsCashFlows.subtract(convertedPremium);
		} else {
			npvLegsCashFlows = convertedPremium.subtract(npvLegsCashFlows);
		}

		return npvLegsCashFlows;
	}

	@Override
	public BigDecimal unrealizedPnlBlack(PricingParameter params, IRSwapOptionTrade trade, Currency currency,
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

		// On option, let's calculate the unrealized pnl as the NPV. How else
		// can we calculate something that may be not exercised ?
		return npvBlack(params, trade, currency, pricingDate);

	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, IRSwapOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		return realizedPnlOptionExercise(params, trade, currency, pricingDate)
				.add(unrealizedPnlBlack(params, trade, currency, pricingDate));

	}

}