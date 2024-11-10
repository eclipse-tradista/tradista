package org.eclipse.tradista.ir.ircapfloorcollar.service;

import static org.eclipse.tradista.core.pricing.util.PricerUtil.cnd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircapfloorcollar.pricer.PricerIRCapFloorCollarUtil;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
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
@Interceptors(IRCapFloorCollarTradeProductScopeFilteringInterceptor.class)
public class IRCapFloorCollarPricerServiceBean implements IRCapFloorCollarPricerService {

	@Override
	public BigDecimal npvBlack(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		BigDecimal pv = pvBlack(params, trade, currency, pricingDate);
		BigDecimal npv;

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		// conversion of the premium to the Pricing currency
		BigDecimal convertedPremium = PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(), currency,
				pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

		// taking into account the premium
		if (trade.isBuy()) {
			npv = pv.subtract(convertedPremium);
		} else {
			npv = convertedPremium.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal pvBlack(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		IRForwardTrade<Product> irForwardTrade = trade.getIrForwardTrade();

		InterestRateCurve irForwardIRCurve = params.getDiscountCurves().get(irForwardTrade.getCurrency());
		if (irForwardIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), irForwardTrade.getCurrency()));
		}

		CurrencyPair pair = new CurrencyPair(irForwardTrade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		// Volatility surface
		// We retrieve the swaption volatility surface from the PP Swaption Volatility
		// Surface
		// Module for the reference index
		PricingParameterVolatilitySurfaceModule module = null;
		for (PricingParameterModule mod : params.getModules()) {
			if (mod instanceof PricingParameterVolatilitySurfaceModule) {
				module = (PricingParameterVolatilitySurfaceModule) mod;
				break;
			}
		}
		SwaptionVolatilitySurface surface = module.getSwaptionVolatilitySurface(irForwardTrade.getReferenceRateIndex());
		if (surface == null) {
			throw new TradistaBusinessException(String.format(
					"The Swaption volatility surface doesn't exist for the index %s. please add it or change the Pricing Parameters Set.",
					irForwardTrade.getReferenceRateIndex()));
		}

		try {

			// Gets the implied volatility from a surface stored in the system.
			// Param in : swaption time to maturity, swap tenor
			int maturity = PricerUtil.daysToYear(trade.getTradeDate(), irForwardTrade.getMaturityDate()).intValue();
			int tenor = PricerUtil.daysToYear(irForwardTrade.getSettlementDate(), irForwardTrade.getMaturityDate())
					.intValue();
			BigDecimal volat = surface.getVolatilityByOptionExpiryAndSwapTenor(maturity, tenor);

			LocalDate varDate = pricingDate;
			Tenor frequency = irForwardTrade.getFrequency();
			BigDecimal k = BigDecimal.ZERO;
			BigDecimal capk = BigDecimal.ZERO;
			BigDecimal floork = BigDecimal.ZERO;
			if (trade.isCap()) {
				k = trade.getCapStrike();
			} else if (trade.isFloor()) {
				k = trade.getFloorStrike();
			}

			if (trade.isCollar()) {
				capk = trade.getCapStrike();
				floork = trade.getFloorStrike();
			}
			BigDecimal pv = BigDecimal.ZERO;

			// Now, we will iterate over the underlying cashflows to calculate the
			// value
			// For Cap/floor/collar, fixing dates start at settle date and finish at
			// the last fixing dates before maturity (so, 0 to n-1)
			// payment dates start are delayed of one slot (so, 1 to n)
			BigDecimal discount = BigDecimal.ZERO;
			// if frequency = 1, nominal fraction is nominal
			BigDecimal nominalFraction = irForwardTrade.getAmount();
			if (frequency.equals(Tenor.THREE_MONTHS)) {
				nominalFraction = nominalFraction.multiply(new BigDecimal("0.25"));
			}
			if (frequency.equals(Tenor.SIX_MONTHS)) {
				nominalFraction = nominalFraction.multiply(new BigDecimal("0.5"));
			}
			while (varDate.isBefore(irForwardTrade.getMaturityDate())) {
				if (!varDate.isBefore(irForwardTrade.getSettlementDate())) {
					BigDecimal d1 = BigDecimal.ZERO;
					BigDecimal d2 = BigDecimal.ZERO;
					BigDecimal fk = PricerUtil.getDiscountFactor(irForwardIRCurve.getId(), varDate);
					LocalDate valuationDate = varDate;

					DateUtil.addTenor(valuationDate, frequency);

					BigDecimal p = PricerUtil.getDiscountFactor(irForwardIRCurve, valuationDate);

					BigDecimal discountedNominalFraction = nominalFraction
							.multiply(BigDecimal.valueOf(Math.exp(p.negate().multiply(discount).doubleValue())));

					if (trade.isCollar()) {
						BigDecimal d1Cap = BigDecimal.ZERO;
						BigDecimal d2Cap = BigDecimal.ZERO;
						BigDecimal d1Floor = BigDecimal.ZERO;
						BigDecimal d2Floor = BigDecimal.ZERO;
						d1Cap = BigDecimal.valueOf(Math.log(fk.doubleValue() / capk.doubleValue()))
								.add(volat.pow(2).multiply((discount.divide(BigDecimal.valueOf(2)))))
								.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						d2Cap = d1Cap.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						d1Floor = BigDecimal.valueOf(Math.log(fk.doubleValue() / floork.doubleValue()))
								.add(volat.pow(2).multiply((discount.divide(BigDecimal.valueOf(2)))))
								.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						d2Floor = d1Floor
								.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						// Collar is to be long on a Cap and short on a Floor -> we
						// add the PV of the caplet and we subtract the pv of the
						// floorlet
						pv.add(discountedNominalFraction
								.multiply((fk.multiply(cnd(d1Cap.doubleValue()))
										.subtract(capk.multiply(cnd(d2Cap.doubleValue())))))
								.subtract(
										discountedNominalFraction.multiply(floork.multiply(cnd(-d2Floor.doubleValue()))
												.subtract(fk.multiply(cnd(-d1Floor.doubleValue()))))));

					}

					else { // Cases of Caps or Floors

						d1 = BigDecimal.valueOf(Math.log(fk.doubleValue() / k.doubleValue()))
								.add(volat.pow(2).multiply((discount.divide(BigDecimal.valueOf(2)))))
								.divide(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						d2 = d1.subtract(volat.multiply(BigDecimal.valueOf(Math.sqrt(discount.doubleValue()))));

						if (trade.isCap()) {
							pv.add(discountedNominalFraction.multiply(
									(fk.multiply(cnd(d1.doubleValue())).subtract(k.multiply(cnd(d2.doubleValue()))))));
						} else { // Case of a floor
							pv.add(discountedNominalFraction.multiply(
									k.multiply(cnd(-d2.doubleValue())).subtract(fk.multiply(cnd(-d1.doubleValue())))));
						}
					}
				}

				if (frequency.equals(Tenor.THREE_MONTHS)) {
					discount = discount.add(new BigDecimal("0.25"));
					varDate = varDate.plus(3, ChronoUnit.MONTHS);
				}
				if (frequency.equals(Tenor.SIX_MONTHS)) {
					discount = discount.add(new BigDecimal("0.5"));
					varDate = varDate.plus(6, ChronoUnit.MONTHS);
				}
				if (frequency.equals(Tenor.ONE_YEAR)) {
					discount = discount.add(BigDecimal.ONE);
					varDate = varDate.plus(1, ChronoUnit.YEARS);
				}

			}

			// conversion to the Pricing currency
			pv = PricerUtil.convertAmount(pv, irForwardTrade.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

			return pv;

		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlPaymentTriggers(params, trade, currency, pricingDate)
				.add(unrealizedPnlBlack(params, trade, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlPaymentTriggers(PricingParameter params, IRCapFloorCollarTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isAfter(trade.getIrForwardTrade().getMaturityDate())
				|| pricingDate.equals(trade.getIrForwardTrade().getMaturityDate())) {
			CurrencyPair pair = new CurrencyPair(trade.getIrForwardTrade().getCurrency(), currency);
			FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
			if (paramTradeCcyPricingCcyFXCurve == null) {
				// TODO Add log warn
			}
			pair = new CurrencyPair(trade.getCurrency(), currency);
			FXCurve paramPremiumCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
			if (paramPremiumCcyPricingCcyFXCurve == null) {
				// TODO Add log warn
			}
			BigDecimal payments = PricerIRCapFloorCollarUtil.getPaymentsTotalAmount(trade, null, 0,
					params.getQuoteSet().getId());
			BigDecimal realizedPnl = PricerUtil.convertAmount(payments, trade.getIrForwardTrade().getCurrency(),
					currency, pricingDate, params.getQuoteSet().getId(),
					paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

			// We subtract the converted premium.
			realizedPnl = realizedPnl.subtract(PricerUtil.convertAmount(trade.getAmount(), trade.getCurrency(),
					currency, pricingDate, params.getQuoteSet().getId(),
					paramPremiumCcyPricingCcyFXCurve != null ? paramPremiumCcyPricingCcyFXCurve.getId() : 0));
			if (trade.isSell()) {
				realizedPnl = realizedPnl.negate();
			}
			return realizedPnl;
		}

		// if pricing date is before settlement date, there is no realized pnl.
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal unrealizedPnlBlack(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		if (pricingDate.isBefore(trade.getIrForwardTrade().getMaturityDate())) {
			return npvBlack(params, trade, currency, pricingDate);
		}

		// if pricing date is after maturity date, there is no unrealized pnl.
		return BigDecimal.ZERO;
	}

}