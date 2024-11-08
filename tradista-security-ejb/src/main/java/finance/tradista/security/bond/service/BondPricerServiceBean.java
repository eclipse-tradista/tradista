package org.eclipse.tradista.security.bond.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;
import org.eclipse.tradista.security.bond.model.Coupon;
import org.eclipse.tradista.security.bond.pricer.PricerBondUtil;

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
@Interceptors(BondProductScopeFilteringInterceptor.class)
public class BondPricerServiceBean implements BondPricerService {

	@EJB
	private BondTradeService bondTradeService;

	@Override
	public BigDecimal ytmNewtonRaphson(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		// 1. Define a starting rate
		BigDecimal rate = new BigDecimal("0.01");
		boolean found = false;
		BigDecimal discountedCFsMinusMarketPrice;
		BigDecimal discountedCFsMinusMarketPriceDerivated;

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getProduct().getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getProduct().getCurrency()));
		}

		InterestRateCurve indexCurve = null;
		if (!trade.getProduct().getCouponType().equals("Fixed")) {
			indexCurve = params.getIndexCurves().get(trade.getProduct().getReferenceRateIndex());
			if (indexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getProduct().getReferenceRateIndex()));
			}
		}

		try {
			// 2. Enter in the "solve and retry" process
			while (!found) {
				// compute f(x)
				discountedCFsMinusMarketPrice = discountedCFsMinusMarketPrice(rate, trade.getProduct(), pricingDate,
						discountCurve.getId(), indexCurve != null ? indexCurve.getId() : 0);
				// compute f'(x)
				// check the diff
				discountedCFsMinusMarketPriceDerivated = derivatedDiscountedCFsMinusMarketPrice(rate,
						trade.getProduct(), pricingDate, indexCurve != null ? indexCurve.getId() : 0);
				// compute f'(x)
				// diff > 0.001 ? contine the process with new x value
				BigDecimal newRate = rate.subtract(discountedCFsMinusMarketPrice
						.divide(discountedCFsMinusMarketPriceDerivated, RoundingMode.HALF_EVEN));
				BigDecimal diff = newRate.subtract(rate);
				if (diff.abs().doubleValue() <= 0.000001) {
					found = true;
				}
				rate = newRate;
			}
			// 3. return new x
			return rate.multiply(BigDecimal.valueOf(100));
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal parYieldParYield(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		Bond bond = trade.getProduct();
		InterestRateCurve discountCurve = params.getDiscountCurves().get(bond.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), bond.getCurrency()));
		}

		try {
			BigDecimal annuity = annuity(discountCurve.getId(), bond, pricingDate);

			BigDecimal couponsByYear = couponsByYear(bond.getCouponFrequency());
			return (new BigDecimal("100").subtract(PricerUtil.discountAmount(new BigDecimal("100"),
					discountCurve.getId(), pricingDate, bond.getMaturityDate(), null))).multiply(couponsByYear)
					.divide(annuity, RoundingMode.HALF_EVEN);
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal npvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
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
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// First, get the pv
		BigDecimal pv = pvDiscountedCashFlow(params, trade, currency, pricingDate);

		// Secondly, take into account the the price paid
		BigDecimal npv;

		BigDecimal convertedPrice = PricerUtil.convertAmount(trade.getAmount().multiply(trade.getQuantity()),
				trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		if (trade.isBuy()) {
			npv = pv.subtract(convertedPrice);
		} else {
			npv = convertedPrice.subtract(pv);
		}

		return npv;
	}

	@Override
	public BigDecimal pvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
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
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// Get the sum of all the discounted cashflows (clean price)
		BigDecimal pv = cleanPriceDiscountedCashFlow(params, trade, currency, pricingDate);

		if (trade.isSell()) {
			pv = pv.negate();
		}

		// Finally, convert in pricing currency
		pv = PricerUtil.convertAmount(pv, trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		return pv.multiply(trade.getQuantity());
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, BondTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the bond maturity date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is after the bond maturity date, it is not possible to forecast cashflows.");
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed and a pricing date is in the past, it is not possible to forecast cashflows.");
			}
		}

		InterestRateCurve indexCurve = null;
		if (!trade.getProduct().getCouponType().equals("Fixed")) {
			indexCurve = params.getIndexCurves().get(trade.getProduct().getReferenceRateIndex());
			if (indexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), trade.getProduct().getReferenceRateIndex()));
			}
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getProduct().getCurrency());
		if (discountCurve == null) {
			// TODO Add log warn
		}

		List<CashFlow> cashFlows = PricerBondUtil.generateCashFlows(trade, pricingDate, params.getQuoteSet(),
				indexCurve != null ? indexCurve.getId() : 0);

		if (discountCurve != null) {
			try {
				PricerUtil.discountCashFlows(cashFlows, pricingDate, discountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		return cashFlows;
	}

	private BigDecimal couponsByYear(Tenor frequency) {
		if (frequency.equals(Tenor.THREE_MONTHS)) {
			return BigDecimal.valueOf(4);
		}
		if (frequency.equals(Tenor.SIX_MONTHS)) {
			return BigDecimal.valueOf(2);
		}
		if (frequency.equals(Tenor.ONE_YEAR)) {
			return BigDecimal.ONE;
		}

		return null;
	}

	private BigDecimal annuity(long curveId, Bond bond, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {
		BigDecimal price = BigDecimal.ZERO;
		// 1. Generate the pending coupons
		List<Coupon> pendingCoupons = PricerBondUtil.getPendingCoupons(bond, pricingDate, 0, false);
		// 2. loop in the coupons and increment the price with the discounted
		// flows
		for (Coupon coupon : pendingCoupons) {
			price = price.add(PricerUtil.discountAmount(BigDecimal.ONE, curveId, pricingDate, coupon.getDate(), null));
		}
		return price;
	}

	private BigDecimal derivatedDiscountedCFsMinusMarketPrice(BigDecimal rate, Bond product, LocalDate pricingDate,
			long indexCurveId) throws PricerException, TradistaBusinessException {
		return derivatedDiscountCoupons(rate, product, pricingDate, indexCurveId);
	}

	private BigDecimal derivatedDiscountCoupons(BigDecimal rate, Bond bond, LocalDate pricingDate, long indexCurveId)
			throws PricerException, TradistaBusinessException {
		BigDecimal price = BigDecimal.ZERO;
		// 1. Generate the pending coupons
		List<Coupon> pendingCoupons = PricerBondUtil.getPendingCoupons(bond, pricingDate, indexCurveId, true);
		// 2. loop in the coupons and increment the price with the actualized
		// flows
		Tenor frequency = bond.getCouponFrequency();
		BigDecimal discount = BigDecimal.ZERO;
		if (frequency.equals(Tenor.THREE_MONTHS)) {
			discount = new BigDecimal("0.25");
		}
		if (frequency.equals(Tenor.SIX_MONTHS)) {
			discount = new BigDecimal("0.5");
		}
		if (frequency.equals(Tenor.ONE_YEAR)) {
			discount = BigDecimal.ONE;
		}
		price = derivatedDiscounted(rate, bond, pendingCoupons, discount);
		return price;
	}

	private BigDecimal derivatedDiscounted(BigDecimal rate, Bond bond, List<Coupon> coupons, BigDecimal discount)
			throws PricerException {
		BigDecimal price = BigDecimal.ZERO;
		BigDecimal discountInc = discount;
		BigDecimal discountFactor;

		discountFactor = rate;
		for (Coupon coupon : coupons) {
			price = price.add(bond.getPrincipal().multiply(coupon.getAmount().divide(BigDecimal.valueOf(100)))
					.multiply(discount.negate())
					.multiply(new BigDecimal(Math.exp(discountFactor.negate().multiply(discount).doubleValue()))));
			discount = discount.add(discountInc);
		}
		price = price.add((bond.getPrincipal().multiply(discount.negate())
				.multiply(new BigDecimal(Math.exp(discountFactor.negate().multiply(discount).doubleValue())))));
		return price;
	}

	private BigDecimal discountedCFsMinusMarketPrice(BigDecimal rate, Bond bond, LocalDate pricingDate,
			long discountCurveId, long indexCurveId) throws PricerException, TradistaBusinessException {
		return PricerBondUtil.discountCoupons(rate, 0, bond, pricingDate, indexCurveId)
				.subtract(PricerBondUtil.discountCoupons(null, discountCurveId, bond, pricingDate, indexCurveId));
	}

	@Override
	public BigDecimal cleanPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
			return BigDecimal.ZERO;
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed, it is not allowed to specify a pricing date in the past.");
			}
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// First, sum all the discounted coupons
		Bond bond = trade.getProduct();
		InterestRateCurve discountCurve = params.getDiscountCurves().get(bond.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), bond.getCurrency()));
		}

		InterestRateCurve indexCurve = null;
		if (!trade.getProduct().getCouponType().equals("Fixed")) {
			indexCurve = params.getIndexCurves().get(bond.getReferenceRateIndex());
			if (indexCurve == null) {
				throw new TradistaBusinessException(String.format(
						"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
						params.getName(), bond.getReferenceRateIndex()));
			}
		}

		try {
			BigDecimal discountedCoupons = PricerBondUtil.discountCoupons(null, discountCurve.getId(), bond,
					pricingDate, indexCurve != null ? indexCurve.getId() : 0);

			if (trade.isSell()) {
				discountedCoupons = discountedCoupons.negate();
			}

			// Finally, convert in pricing currency
			discountedCoupons = PricerUtil.convertAmount(discountedCoupons, trade.getCurrency(), currency, pricingDate,
					params.getQuoteSet().getId(),
					paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

			return discountedCoupons.multiply(trade.getQuantity());
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public BigDecimal dirtyPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
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
		FXCurve paramTradeCcyPricingCcyFXCurve = params.getFxCurves().get(pair);
		if (paramTradeCcyPricingCcyFXCurve == null) {
			// TODO Add log warn
		}

		// First, get the clean price (sum of all the pending cashflows)
		BigDecimal cleanPrice = cleanPriceDiscountedCashFlow(params, trade, currency, pricingDate);

		Bond bond = trade.getProduct();
		BigDecimal accruedInterest = null;
		if (pricingDate.isBefore(bond.getDatedDate())) {
			accruedInterest = BigDecimal.ZERO;
		}
		if (!pricingDate.isBefore(bond.getLastCouponDate())) {
			accruedInterest = BigDecimal.ZERO;
		}
		if (accruedInterest == null) {
			// Find the coupon just before the trade date
			for (Coupon coupon : bond.getCoupons()) {
				if (coupon.getDate().isBefore(pricingDate)) {
					LocalDate nextCouponDate = DateUtil.addTenor(coupon.getDate(), bond.getCouponFrequency());
					if (pricingDate.isBefore(nextCouponDate)) {
						accruedInterest = PricerUtil.daysToYear(coupon.getDate(), pricingDate).multiply(
								bond.getPrincipal().multiply(coupon.getAmount().divide(BigDecimal.valueOf(100))));
						break;
					}
				}
			}
		}

		// Convert the accrued interest in pricing currency
		accruedInterest = PricerUtil.convertAmount(accruedInterest, trade.getCurrency(), currency, pricingDate,
				params.getQuoteSet().getId(),
				paramTradeCcyPricingCcyFXCurve != null ? paramTradeCcyPricingCcyFXCurve.getId() : 0);

		// Then, add the accrued interest:
		BigDecimal dirtyPrice = cleanPrice.add(accruedInterest.multiply(trade.getQuantity()));
		return dirtyPrice;
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlDefault(params, bond, book, currency, pricingDate)
				.add(unrealizedPnlDefault(params, bond, book, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getInventoriesBeforeDateByProductAndBookIds(bond.getId(), bookId, pricingDate);
		BigDecimal realizedPnl = BigDecimal.ZERO;
		// First, we add the payments of the bond we owned before the pricing
		// date
		if (inventories != null && !inventories.isEmpty()) {
			for (ProductInventory inv : inventories) {
				// index curve name is null as won't be used here.
				BigDecimal bondPayments = PricerBondUtil.getTotalPaymentAmount(bond, inv.getFrom(), pricingDate,
						params.getQuoteSet(), 0);
				realizedPnl.add(bondPayments);
			}
		}

		// then, we subtract the prices of the trades traded before the pricing
		// date
		List<BondTrade> trades = bondTradeService.getBondTradesBeforeTradeDateByBondAndBookIds(pricingDate,
				bond.getId(), bookId);

		if (trades != null && !trades.isEmpty())

		{
			for (BondTrade trade : trades) {
				if (trade.isBuy()) {
					realizedPnl.subtract(trade.getAmount().multiply(trade.getQuantity()));
				} else {
					realizedPnl.add(trade.getAmount().multiply(trade.getQuantity()));
				}
			}
		}
		return realizedPnl;

	}

	@Override
	public BigDecimal unrealizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getOpenPositionsFromInventoryByProductAndBookIds(bond.getId(), bookId);
		BigDecimal unrealizedPnl = BigDecimal.ZERO;
		if (inventories != null && !inventories.isEmpty()) {
			BondTrade trade = new BondTrade();
			trade.setBuySell(true);
			trade.setProduct(bond);
			trade.setSettlementDate(inventories.toArray(new ProductInventory[0])[0].getFrom());
			trade.setQuantity(inventories.toArray(new ProductInventory[0])[0].getQuantity());
			return pvDiscountedCashFlow(params, trade, currency, pricingDate);
		}
		return unrealizedPnl;
	}

}