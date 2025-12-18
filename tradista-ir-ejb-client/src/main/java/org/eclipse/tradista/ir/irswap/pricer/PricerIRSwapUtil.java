package org.eclipse.tradista.ir.irswap.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.model.CashFlow.Direction;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.CurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;

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

public final class PricerIRSwapUtil {

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	private static final String IMPOSSIBLE_TO_CALCULATE_THE_FLOWS = "Impossible to calculate the flows, there is no %s %s quote as of %tD in this quote set %s and no value for this date in the curve %s.";

	private PricerIRSwapUtil() {
	}

	public static BigDecimal discountFixedLegCashFlows(IRSwapTrade irswapTrade, long irSwapCurrIRCurveId,
			LocalDate date, QuoteSet quoteSet, long indexCurveId) throws PricerException, TradistaBusinessException {
		BigDecimal price;
		// 1. Generate the pending cashflows
		List<CashFlow> pendingCashFlows = PricerIRSwapUtil.getPaymentLegFlows(irswapTrade, quoteSet.getId(),
				indexCurveId, date);
		// 2. loop in the coupons and increment the price with the discounted
		// flows
		price = PricerUtil.getTotalFlowsAmount(pendingCashFlows, null, date, irSwapCurrIRCurveId,
				irswapTrade.getPaymentDayCountConvention());
		return price;
	}

	public static BigDecimal discountFloatingLegCashFlows(IRSwapTrade irswapTrade, long irSwapCurrIRCurveId,
			LocalDate date, QuoteSet quoteSet, long indexCurveId) throws PricerException, TradistaBusinessException {
		BigDecimal price;
		// 1. Generate the pending cashflows
		List<CashFlow> pendingCashFlows = PricerIRSwapUtil.getReceptionLegFlows(irswapTrade, quoteSet.getId(),
				indexCurveId, date);
		// 2. loop in the coupons and increment the price with the discounted
		// flows
		price = PricerUtil.getTotalFlowsAmount(pendingCashFlows, null, date, irSwapCurrIRCurveId,
				irswapTrade.getReceptionDayCountConvention());
		return price;
	}

	/**
	 * @param trade        the irswap for which we want to calculate the received
	 *                     cashflows
	 * @param quoteSetId   the quoteset used to retrieved the interest rates
	 * @param indexCurveId The id of the curve used to calculate forward rates
	 * @param startDate    the date where to start the cash flows calculation
	 * @return the received cashflows for the given trade
	 * @throws TradistaBusinessException if there was a problem to retrieve the
	 *                                   interest rate.
	 * @throws PricerException
	 */
	public static List<CashFlow> getReceptionLegFlows(IRSwapTrade trade, long quoteSetId, long indexCurveId,
			LocalDate startDate) throws TradistaBusinessException, PricerException {
		Tenor frequency = trade.getReceptionFrequency();
		List<CashFlow> cashFlows = null;
		LocalDate cashFlowDate = trade.getSettlementDate();
		String quoteName = Index.INDEX + "." + trade.getReceptionReferenceRateIndex() + "."
				+ trade.getReceptionReferenceRateIndexTenor();
		BigDecimal ir;
		if (!frequency.equals(Tenor.NO_TENOR)) {
			while (!cashFlowDate.isAfter(trade.getMaturityDate())) {
				LocalDate beginningOfPeriod = cashFlowDate;
				LocalDate endOfPeriod;
				LocalDate fixingDate = cashFlowDate;
				CashFlow cashFlow = new CashFlow();
				cashFlow.setDate(cashFlowDate);
				cashFlow.setCurrency(trade.getCurrency());
				cashFlow.setPurpose(TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT);
				if (trade.isBuy()) {
					cashFlow.setDirection(Direction.RECEIVE);
				} else {
					cashFlow.setDirection(Direction.PAY);
				}
				cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);
				endOfPeriod = cashFlowDate.minusDays(1);
				if (endOfPeriod.isAfter(trade.getMaturityDate())) {
					endOfPeriod = trade.getMaturityDate();
				}
				if (trade.getReceptionInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					cashFlow.setDate(endOfPeriod);
				}
				if (trade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					fixingDate = endOfPeriod;
				}
				ir = PricerUtil.getInterestRateAsOfDate(quoteName, quoteSetId, indexCurveId,
						trade.getReceptionReferenceRateIndexTenor(), trade.getReceptionDayCountConvention(),
						fixingDate);
				if (ir == null) {
					throw new TradistaBusinessException(
							String.format(IMPOSSIBLE_TO_CALCULATE_THE_FLOWS, QuoteValue.LAST, quoteName, fixingDate,
									new QuoteBusinessDelegate().getQuoteSetById(quoteSetId),
									new CurveBusinessDelegate().getCurveById(indexCurveId)));
				}
				if (trade.getReceptionSpread() != null) {
					ir.add(trade.getReceptionSpread());
				}

				/*
				 * We only consider cashflows generated as of start date of after
				 */
				if (!cashFlowDate.isBefore(startDate)) {
					// the fractioned notional is the notional of the reception leg
					// * accrual factor calculated using the period between the fixing
					// date and the payment date.
					BigDecimal fractionedNotional = trade.getAmount().multiply(PricerUtil
							.daysToYear(trade.getReceptionDayCountConvention(), beginningOfPeriod, endOfPeriod));
					BigDecimal payment = fractionedNotional.multiply(
							ir.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode()));
					cashFlow.setAmount(payment);
					if (cashFlows == null) {
						cashFlows = new ArrayList<>();
					}
					cashFlows.add(cashFlow);
				}
			}
		} else {
			LocalDate settlementDate = trade.getSettlementDate();
			LocalDate fixingDate = trade.getSettlementDate();
			LocalDate beginningOfPeriod = trade.getSettlementDate();
			LocalDate endOfPeriod = trade.getMaturityDate();
			if (trade.getReceptionInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				settlementDate = trade.getMaturityDate();
			}
			if (trade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				fixingDate = trade.getMaturityDate();
			}
			cashFlows = new ArrayList<>();
			CashFlow cashFlow = new CashFlow();
			cashFlow.setDate(settlementDate);
			cashFlow.setCurrency(trade.getCurrency());
			cashFlow.setPurpose(TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT);
			if (trade.isBuy()) {
				cashFlow.setDirection(Direction.RECEIVE);
			} else {
				cashFlow.setDirection(Direction.PAY);
			}
			ir = PricerUtil.getInterestRateAsOfDate(quoteName, quoteSetId, indexCurveId,
					trade.getReceptionReferenceRateIndexTenor(), trade.getReceptionDayCountConvention(), fixingDate);
			if (ir == null) {
				throw new TradistaBusinessException(String.format(IMPOSSIBLE_TO_CALCULATE_THE_FLOWS, QuoteValue.LAST,
						quoteName, fixingDate, new QuoteBusinessDelegate().getQuoteSetById(quoteSetId),
						new CurveBusinessDelegate().getCurveById(indexCurveId)));
			}
			if (trade.getReceptionSpread() != null) {
				ir.add(trade.getReceptionSpread());
			}
			// the fractioned notional is the notional of the reception leg
			// * accrual
			// factor calculated using the period between the fixing
			// date and the payment date.
			BigDecimal fractionedNotional = trade.getAmount().multiply(
					PricerUtil.daysToYear(trade.getReceptionDayCountConvention(), beginningOfPeriod, endOfPeriod));
			BigDecimal payment = fractionedNotional
					.multiply(ir.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode()));
			cashFlow.setAmount(payment);
			cashFlows.add(cashFlow);
		}

		// When it is a CcySwapTrade, notional for this leg is received at
		// settlement date by the trade seller (who pays the floating leg) and
		// he pays it back
		// at maturity date
		if (trade instanceof CcySwapTrade) {
			CashFlow cf = new CashFlow();
			cf.setDate(trade.getSettlementDate());
			cf.setCurrency(trade.getCurrency());
			cf.setAmount(trade.getAmount());
			cf.setPurpose(TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT);
			if (trade.isBuy()) {
				cf.setDirection(Direction.PAY);
			} else {
				cf.setDirection(Direction.RECEIVE);
			}
			cashFlows.add(cf);
			cf = new CashFlow();
			cf.setDate(trade.getMaturityDate());
			cf.setCurrency(trade.getCurrency());
			cf.setAmount(trade.getAmount());
			cf.setPurpose(TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT);
			if (trade.isBuy()) {
				cf.setDirection(Direction.RECEIVE);
			} else {
				cf.setDirection(Direction.PAY);
			}
			cashFlows.add(cf);
		}

		return cashFlows;
	}

	/**
	 * @param trade        the irswap for which we want to calculate the paid
	 *                     cashflows
	 * @param quoteSetId   the quoteset used to retrieved the interest rates
	 * @param indexCurveId The id of the curve used to calculate forward rates
	 * @param startDate    the date where the calculation of cashflows start.
	 * @return the paid cashflows for the given trade
	 * @throws TradistaBusinessException if there was a problem to retrieve the
	 *                                   interest rate.
	 * @throws PricerException           if there was a problem to retrieve the
	 *                                   interest rate.
	 */
	public static List<CashFlow> getPaymentLegFlows(IRSwapTrade trade, long quoteSetId, long indexCurveId,
			LocalDate startDate) throws TradistaBusinessException, PricerException {
		Tenor frequency = trade.getPaymentFrequency();
		List<CashFlow> cashFlows = null;
		LocalDate cashFlowDate = trade.getSettlementDate();

		// When the trade is a Ccy, fixeg legs cashflows are based on notional 2
		// and currency 2.
		Currency currency;
		BigDecimal notional;
		BigDecimal ir;
		String quoteName = Index.INDEX + "." + trade.getPaymentReferenceRateIndex() + "."
				+ trade.getPaymentReferenceRateIndexTenor();
		if (trade instanceof CcySwapTrade ccySwapTrade) {
			notional = ccySwapTrade.getNotionalAmountTwo();
			currency = ccySwapTrade.getCurrencyTwo();
		} else {
			notional = trade.getAmount();
			currency = trade.getCurrency();
		}

		if (!frequency.equals(Tenor.NO_TENOR)) {
			while (!cashFlowDate.isAfter(trade.getMaturityDate())) {
				CashFlow cashFlow = new CashFlow();
				LocalDate endOfPeriod;
				LocalDate beginningOfPeriod = cashFlowDate;
				LocalDate fixingDate = cashFlowDate;
				cashFlow.setDate(cashFlowDate);
				cashFlow.setCurrency(currency);
				cashFlow.setPurpose(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT);
				if (trade.isBuy()) {
					cashFlow.setDirection(Direction.PAY);
				} else {
					cashFlow.setDirection(Direction.RECEIVE);
				}
				cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);

				endOfPeriod = cashFlowDate.minusDays(1);
				if (endOfPeriod.isAfter(trade.getMaturityDate())) {
					endOfPeriod = trade.getMaturityDate();
				}
				if (trade.getPaymentInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					cashFlow.setDate(endOfPeriod);
				}
				if (trade.isInterestsToPayFixed()) {
					ir = trade.getPaymentFixedInterestRate();
				} else {
					if (trade.getPaymentInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
						fixingDate = endOfPeriod;
					}
					ir = PricerUtil.getInterestRateAsOfDate(quoteName, quoteSetId, indexCurveId,
							trade.getPaymentReferenceRateIndexTenor(), trade.getPaymentDayCountConvention(),
							fixingDate);
					if (ir == null) {
						throw new TradistaBusinessException(
								String.format(IMPOSSIBLE_TO_CALCULATE_THE_FLOWS, QuoteValue.LAST, quoteName, fixingDate,
										new QuoteBusinessDelegate().getQuoteSetById(quoteSetId),
										new CurveBusinessDelegate().getCurveById(indexCurveId)));
					}
					if (trade.getPaymentSpread() != null) {
						ir = ir.add(trade.getPaymentSpread());
					}
				}

				/*
				 * We only consider cashflows generated as of start date of after
				 */
				if (!cashFlowDate.isBefore(startDate)) {
					// the fractioned notional is the notional of the reception leg
					// * accrual
					// factor calculated using the period between the fixing
					// date and the payment date.
					BigDecimal fractionedNotional = notional.multiply(PricerUtil
							.daysToYear(trade.getPaymentDayCountConvention(), beginningOfPeriod, endOfPeriod));
					BigDecimal payment = fractionedNotional.multiply(ir.divide(BigDecimal.valueOf(100),
							configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
					cashFlow.setAmount(payment);
					if (cashFlows == null) {
						cashFlows = new ArrayList<>();
					}
					cashFlows.add(cashFlow);
				}
			}
		} else {
			LocalDate settlementDate = trade.getSettlementDate();
			LocalDate fixingDate = trade.getSettlementDate();
			LocalDate beginningOfPeriod = trade.getSettlementDate();
			LocalDate endOfPeriod = trade.getMaturityDate();
			if (trade.getPaymentInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				settlementDate = trade.getMaturityDate();
			}
			if (trade.getPaymentInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				fixingDate = trade.getMaturityDate();
			}
			cashFlows = new ArrayList<>();
			CashFlow cashFlow = new CashFlow();
			cashFlow.setDate(settlementDate);
			cashFlow.setCurrency(currency);
			cashFlow.setPurpose(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT);
			if (trade.isBuy()) {
				cashFlow.setDirection(Direction.PAY);
			} else {
				cashFlow.setDirection(Direction.RECEIVE);
			}
			if (trade.isInterestsToPayFixed()) {
				ir = trade.getPaymentFixedInterestRate();
			} else {
				ir = PricerUtil.getInterestRateAsOfDate(quoteName, quoteSetId, indexCurveId,
						trade.getPaymentReferenceRateIndexTenor(), trade.getPaymentDayCountConvention(), fixingDate);
				if (ir == null) {
					throw new TradistaBusinessException(
							String.format(IMPOSSIBLE_TO_CALCULATE_THE_FLOWS, QuoteValue.LAST, quoteName, fixingDate,
									new QuoteBusinessDelegate().getQuoteSetById(quoteSetId),
									new CurveBusinessDelegate().getCurveById(indexCurveId)));
				}
				if (trade.getPaymentSpread() != null) {
					ir = ir.add(trade.getPaymentSpread());
				}
			}

			// the fractioned notional is the notional of the reception leg
			// * accrual
			// factor calculated using the period between the fixing
			// date and the payment date.
			BigDecimal fractionedNotional = notional.multiply(
					PricerUtil.daysToYear(trade.getPaymentDayCountConvention(), beginningOfPeriod, endOfPeriod));
			BigDecimal payment = fractionedNotional.multiply(ir.divide(BigDecimal.valueOf(100),
					configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
			cashFlow.setAmount(payment);
			cashFlows.add(cashFlow);
		}

		// When it is a CcySwapTrade, notional for this leg is received at
		// settlement date by the trade buyer (who pays the fixed leg) and he
		// pays it back
		// at maturity date
		if (trade instanceof CcySwapTrade) {
			CashFlow cf = new CashFlow();
			cf.setDate(trade.getSettlementDate());
			cf.setCurrency(currency);
			cf.setAmount(notional.negate());
			cf.setPurpose(TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT);
			if (trade.isBuy()) {
				cf.setDirection(Direction.RECEIVE);
			} else {
				cf.setDirection(Direction.PAY);
			}
			cashFlows.add(cf);
			cf = new CashFlow();
			cf.setDate(trade.getMaturityDate());
			cf.setCurrency(currency);
			cf.setAmount(notional);
			cf.setPurpose(TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT);
			if (trade.isBuy()) {
				cf.setDirection(Direction.PAY);
			} else {
				cf.setDirection(Direction.RECEIVE);
			}
			cashFlows.add(cf);
		}

		return cashFlows;
	}

	/**
	 * @param trade         the irswaps for which we want to calculate the realized
	 *                      pnl
	 * @param quoteSetName  the quoteset used to retrieved the interest rates
	 * @param valueCurrency the currency used to present the realized pnl
	 * @return the realized pnl for the given trade
	 * @throws TradistaBusinessException if there was a problem to retrieve an
	 *                                   interest rate or an exchange rate.
	 * @throws PricerException
	 */
	public static BigDecimal getRealizedPNL(IRSwapTrade trade, long quoteSetId, Currency valueCurrency)
			throws TradistaBusinessException, PricerException {
		BigDecimal totalFlowsAmount = BigDecimal.ZERO;
		List<CashFlow> rcfs = PricerIRSwapUtil.getReceptionLegFlows(trade, quoteSetId, 0, trade.getSettlementDate());
		List<CashFlow> pcfs = PricerIRSwapUtil.getPaymentLegFlows(trade, quoteSetId, 0, trade.getSettlementDate());
		totalFlowsAmount = totalFlowsAmount.add(
				PricerUtil.getTotalFlowsAmount(rcfs, valueCurrency, null, 0, trade.getReceptionDayCountConvention()));
		totalFlowsAmount = totalFlowsAmount.subtract(
				PricerUtil.getTotalFlowsAmount(pcfs, valueCurrency, null, 0, trade.getPaymentDayCountConvention()));
		return totalFlowsAmount;
	}

	public static List<CashFlow> generateCashFlows(IRSwapTrade trade, LocalDate pricingDate, QuoteSet qs,
			long paymentIndexCurveId, long receptionIndexCurveId) throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}

		if (pricingDate == null) {
			pricingDate = LocalDate.MIN;
		}

		List<CashFlow> cfs = null;

		try {
			List<CashFlow> paymentLegFlows = PricerIRSwapUtil.getPaymentLegFlows(trade, qs.getId(), paymentIndexCurveId,
					pricingDate);
			List<CashFlow> receptionLegFlows = PricerIRSwapUtil.getReceptionLegFlows(trade, qs.getId(),
					receptionIndexCurveId, pricingDate);
			if (paymentLegFlows != null) {
				if (receptionLegFlows != null) {
					paymentLegFlows.addAll(receptionLegFlows);
				}
				cfs = paymentLegFlows;
			} else {
				if (receptionLegFlows != null) {
					cfs = receptionLegFlows;
				}
			}
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}

		if (cfs != null) {
			Collections.sort(cfs);
		}

		return cfs;
	}

}