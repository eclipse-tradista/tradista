package org.eclipse.tradista.security.bond.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;
import org.eclipse.tradista.security.bond.model.Coupon;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public final class PricerBondUtil {

	public static BigDecimal discountCoupons(BigDecimal rate, long discountCurveId, Bond bond, LocalDate pricingDate,
			long indexCurveId) throws PricerException, TradistaBusinessException {
		BigDecimal price = BigDecimal.ZERO;
		// 1. Generate the pending coupons
		List<Coupon> pendingCoupons = getPendingCoupons(bond, pricingDate, indexCurveId, true);
		// 2. loop in the coupons and increment the price with the discounted
		// flows
		price = PricerBondUtil.discounted(discountCurveId, bond, pendingCoupons, pricingDate);
		return price;
	}

	private static BigDecimal discounted(long curveId, Bond bond, List<Coupon> coupons, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {
		BigDecimal price = BigDecimal.ZERO;
		for (Coupon coupon : coupons) {
			BigDecimal discountedCoupon = PricerUtil.discountAmount(coupon.getAmount(), curveId, pricingDate,
					coupon.getDate(), null);
			price = price.add(discountedCoupon);
		}
		BigDecimal discountedPrincipalAtMaturity = PricerUtil.discountAmount(bond.getPrincipal(), curveId, pricingDate,
				bond.getMaturityDate(), null);
		price = price.add(discountedPrincipalAtMaturity);
		return price;
	}

	public static List<Coupon> getPendingCoupons(Bond bond, LocalDate tradeDate, long indexCurveId,
			boolean calculateAmount) throws TradistaBusinessException, PricerException {
		Tenor frequency = bond.getCouponFrequency();
		List<Coupon> coupons = new ArrayList<Coupon>();
		LocalDate datedDate = bond.getDatedDate();
		LocalDate couponDate = bond.getDatedDate();
		// We try to calculate the coupons only if the bond is not a ZC and only
		// if the start date is not after the bond maturity date
		if (!bond.getCouponFrequency().equals(Tenor.NO_TENOR) && !tradeDate.isAfter(bond.getMaturityDate())) {
			while (!couponDate.isAfter(bond.getMaturityDate())) {
				if (couponDate.isAfter(datedDate) && !couponDate.isBefore(tradeDate)) {
					Coupon coupon = new Coupon(couponDate);
					if (calculateAmount) {
						if (bond.getCouponType().equals("Fixed")) {
							coupon.setAmount(
									bond.getPrincipal().multiply(bond.getCoupon().divide(BigDecimal.valueOf(100))));
						} else {
							BigDecimal rate = PricerUtil.getValueAsOfDateFromCurve(indexCurveId, couponDate);
							if (bond.isCap()) {
								if (rate.compareTo(bond.getCap()) == 1) {
									rate = bond.getCap();
								}
							}
							if (bond.isFloor()) {
								if (rate.compareTo(bond.getFloor()) == -1) {
									rate = bond.getFloor();
								}
							}
							if (bond.isCollar()) {
								if (rate.compareTo(bond.getCap()) == 1) {
									rate = bond.getCap();
								}
								if (rate.compareTo(bond.getFloor()) == -1) {
									rate = bond.getFloor();
								}
							}
							if (bond.getSpread() != null) {
								rate = rate.add(bond.getSpread());
							}
							if (bond.getLeverageFactor() != null) {
								rate = rate.multiply(bond.getLeverageFactor());
							}
							coupon.setAmount((rate.divide(BigDecimal.valueOf(100))).multiply(bond.getPrincipal()));
						}
					}
					coupons.add(coupon);
				}
				couponDate = DateUtil.addTenor(couponDate, frequency);
			}
		}
		return coupons;
	}

	public static List<CashFlow> getPayments(Bond bond, LocalDate startDate, LocalDate endDate, QuoteSet qs,
			long indexCurveId) throws TradistaBusinessException {
		if (bond == null) {
			throw new TradistaBusinessException("The bond is mandatory.");
		}

		if (startDate == null) {
			startDate = LocalDate.MIN;
		}

		if (endDate == null) {
			endDate = LocalDate.MAX;
		}

		Tenor frequency = bond.getCouponFrequency();
		List<CashFlow> cfs = new ArrayList<CashFlow>();
		LocalDate datedDate = bond.getDatedDate();
		LocalDate couponDate = bond.getDatedDate();
		// We try to calculate the coupons only if the bond is not a ZC and only
		// if the start date is not after the bond maturity date
		if (!bond.getCouponFrequency().equals(Tenor.NO_TENOR) && !startDate.isAfter(bond.getMaturityDate())) {
			while (!couponDate.isAfter(bond.getMaturityDate())) {
				if (couponDate.isAfter(datedDate) && !couponDate.isBefore(startDate) && !couponDate.isAfter(endDate)) {
					CashFlow coupon = new CashFlow();
					coupon.setDate(couponDate);
					coupon.setCurrency(bond.getCurrency());
					if (bond.getCouponType().equals("Fixed")) {
						coupon.setAmount(
								bond.getPrincipal().multiply(bond.getCoupon().divide(BigDecimal.valueOf(100))));
					} else {
						BigDecimal rate;
						try {
							rate = PricerUtil.getInterestRateAsOfDate(
									bond.getReferenceRateIndex().getName() + "." + bond.getCouponFrequency(),
									qs.getId(), indexCurveId, bond.getCouponFrequency(), null, couponDate);
						} catch (PricerException pe) {
							throw new TradistaBusinessException(pe.getMessage());
						}
						if (bond.isCap()) {
							if (rate.compareTo(bond.getCap()) == 1) {
								rate = bond.getCap();
							}
						}
						if (bond.isFloor()) {
							if (rate.compareTo(bond.getFloor()) == -1) {
								rate = bond.getFloor();
							}
						}
						if (bond.isCollar()) {
							if (rate.compareTo(bond.getCap()) == 1) {
								rate = bond.getCap();
							}
							if (rate.compareTo(bond.getFloor()) == -1) {
								rate = bond.getFloor();
							}
						}
						if (bond.getSpread() != null) {
							rate = rate.add(bond.getSpread());
						}
						if (bond.getLeverageFactor() != null) {
							rate = rate.multiply(bond.getLeverageFactor());
						}
						coupon.setAmount((rate.divide(BigDecimal.valueOf(100))).multiply(bond.getPrincipal()));
					}
					cfs.add(coupon);
				}

				couponDate = DateUtil.addTenor(couponDate, frequency);
			}
		}

		if (!bond.getMaturityDate().isBefore(startDate) && !bond.getMaturityDate().isAfter(endDate)) {
			CashFlow notionalPaidBack = new CashFlow();
			notionalPaidBack.setDate(bond.getMaturityDate());
			notionalPaidBack.setAmount(bond.getPrincipal());
			notionalPaidBack.setCurrency(bond.getCurrency());
			cfs.add(notionalPaidBack);
		}
		return cfs;
	}

	public static BigDecimal getTotalPaymentAmount(Bond bond, LocalDate startDate, LocalDate endDate, QuoteSet qs,
			long indexCurveId) throws TradistaBusinessException {
		List<CashFlow> cfs = PricerBondUtil.getPayments(bond, startDate, endDate, qs, indexCurveId);
		return PricerUtil.getTotalFlowsAmount(cfs, null, null, 0, null);
	}

	public static List<CashFlow> generateCashFlows(BondTrade trade, LocalDate pricingDate, QuoteSet qs,
			long indexCurveId) throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}

		if (pricingDate == null) {
			pricingDate = LocalDate.MIN;
		}

		Bond bond = trade.getProduct();

		Tenor frequency = bond.getCouponFrequency();
		List<CashFlow> cfs = new ArrayList<CashFlow>();
		LocalDate datedDate = bond.getDatedDate();
		LocalDate couponDate = bond.getDatedDate();

		if (!trade.getSettlementDate().isBefore(pricingDate)) {
			CashFlow cf = new CashFlow();
			cf.setDate(trade.getSettlementDate());
			cf.setAmount(trade.getAmount().multiply(trade.getQuantity()));
			cf.setCurrency(trade.getCurrency());
			cf.setPurpose(TransferPurpose.BOND_PAYMENT);
			cf.setDirection(trade.isBuy() ? CashFlow.Direction.PAY : CashFlow.Direction.RECEIVE);
			cfs.add(cf);
		}

		// We try to calculate the coupons only if the bond is not a ZC and only
		// if the start date is not after the bond maturity date
		if (!bond.getCouponFrequency().equals(Tenor.NO_TENOR) && !pricingDate.isAfter(bond.getMaturityDate())) {
			while (!couponDate.isAfter(bond.getMaturityDate())) {
				if (couponDate.isAfter(datedDate) && !couponDate.isBefore(pricingDate)) {
					CashFlow coupon = new CashFlow();
					coupon.setDate(couponDate);
					coupon.setCurrency(bond.getCurrency());
					coupon.setPurpose(TransferPurpose.COUPON);
					coupon.setDirection(trade.isBuy() ? CashFlow.Direction.RECEIVE : CashFlow.Direction.PAY);
					if (bond.getCouponType().equals("Fixed")) {
						coupon.setAmount(bond.getPrincipal().multiply(bond.getCoupon().divide(BigDecimal.valueOf(100)))
								.multiply(trade.getQuantity()));
					} else {
						BigDecimal rate;
						try {
							rate = PricerUtil.getInterestRateAsOfDate(
									bond.getReferenceRateIndex().getName() + "." + bond.getCouponFrequency(),
									qs.getId(), indexCurveId, bond.getCouponFrequency(), null, couponDate);
						} catch (PricerException pe) {
							throw new TradistaBusinessException(pe.getMessage());
						}
						if (bond.isCap()) {
							if (rate.compareTo(bond.getCap()) == 1) {
								rate = bond.getCap();
							}
						}
						if (bond.isFloor()) {
							if (rate.compareTo(bond.getFloor()) == -1) {
								rate = bond.getFloor();
							}
						}
						if (bond.isCollar()) {
							if (rate.compareTo(bond.getCap()) == 1) {
								rate = bond.getCap();
							}
							if (rate.compareTo(bond.getFloor()) == -1) {
								rate = bond.getFloor();
							}
						}
						if (bond.getSpread() != null) {
							rate = rate.add(bond.getSpread());
						}
						if (bond.getLeverageFactor() != null) {
							rate = rate.multiply(bond.getLeverageFactor());
						}
						coupon.setAmount((rate.divide(BigDecimal.valueOf(100))).multiply(bond.getPrincipal())
								.multiply(trade.getQuantity()));
					}
					cfs.add(coupon);
				}

				couponDate = DateUtil.addTenor(couponDate, frequency);
			}
		}

		if (!bond.getMaturityDate().isBefore(pricingDate)) {
			CashFlow notionalPaidBack = new CashFlow();
			notionalPaidBack.setDate(bond.getMaturityDate());
			notionalPaidBack.setAmount(bond.getPrincipal().multiply(trade.getQuantity()));
			notionalPaidBack.setCurrency(bond.getCurrency());
			notionalPaidBack.setPurpose(TransferPurpose.NOTIONAL_REPAYMENT);
			notionalPaidBack.setDirection(trade.isBuy() ? CashFlow.Direction.RECEIVE : CashFlow.Direction.PAY);
			cfs.add(notionalPaidBack);
		}
		return cfs;

	}

}