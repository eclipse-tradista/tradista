package org.eclipse.tradista.ir.ircapfloorcollar.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;

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

public final class PricerIRCapFloorCollarUtil {

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	/**
	 * Returns the triggered payments for the given Cap/Floor/Collar trade. Make use
	 * of this method when the trade maturity date is reached, so all payments are
	 * known.
	 * 
	 * @param trade      the trade for which we want to calculate the triggered
	 *                   payments.
	 * @param valueDate  used to discount the payments. If it is null, the payments
	 *                   are not discounted.
	 * @param curveId    the curve used to discount.
	 * @param quoteSetId the quote set id, used to retrieve interest rates from
	 *                   quotes.
	 * @return the triggered payments for the given Cap/Floor/Collar trade.
	 * @throws TradistaBusinessException If a payment cannot be calculated cause of
	 *                                   a missing quote or if it cannot be
	 *                                   discounted.
	 */
	public static List<CashFlow> getTriggeredPayments(IRCapFloorCollarTrade trade, LocalDate valueDate, long curveId,
			long quoteSetId) throws TradistaBusinessException {
		Tenor frequency = trade.getIrForwardTrade().getFrequency();
		List<CashFlow> cashFlows = new ArrayList<CashFlow>();
		LocalDate cashFlowDate = trade.getSettlementDate();
		BigDecimal paymentIr = null;
		BigDecimal ir = null;

		if (!trade.getIrForwardTrade().getFrequency().equals(Tenor.NO_TENOR)) {
			while (!cashFlowDate.isAfter(trade.getIrForwardTrade().getMaturityDate())) {
				LocalDate fixingDate = cashFlowDate;
				LocalDate settlementDate = cashFlowDate;
				LocalDate endOfPeriod = cashFlowDate;
				LocalDate beginningOfPeriod = cashFlowDate;
				CashFlow cashFlow = new CashFlow();
				cashFlow.setCurrency(trade.getIrForwardTrade().getCurrency());
				cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);

				endOfPeriod = cashFlowDate.minusDays(1);
				if (endOfPeriod.isAfter(trade.getIrForwardTrade().getMaturityDate())) {
					endOfPeriod = trade.getIrForwardTrade().getMaturityDate();
				}
				if (trade.getIrForwardTrade().getInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					settlementDate = endOfPeriod;
				}
				if (trade.getIrForwardTrade().getInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					fixingDate = endOfPeriod;
				}
				ir = PricerUtil.getValueAsOfDateFromQuote(
						trade.getIrForwardTrade().getReferenceRateIndex() + "."
								+ trade.getIrForwardTrade().getReferenceRateIndexTenor(),
						quoteSetId, QuoteType.INTEREST_RATE, QuoteValue.LAST, fixingDate);
				if (ir == null) {
					throw new TradistaBusinessException(String.format(
							"Impossible to calculate triggered payments, there is no '%s' %s quote as of % in this quoteSet: %s",
							QuoteValue.LAST,
							trade.getIrForwardTrade().getReferenceRateIndex() + "."
									+ trade.getIrForwardTrade().getReferenceRateIndexTenor(),
							cashFlowDate, quoteSetId));
				}

				cashFlow.setDate(settlementDate);

				if (trade.isCap()) {
					if (ir.compareTo(trade.getCapStrike()) == 1) {
						paymentIr = ir.subtract(trade.getCapStrike());
					}
				}
				if (trade.isFloor()) {
					if (ir.compareTo(trade.getFloorStrike()) == -1) {
						paymentIr = trade.getFloorStrike().subtract(ir);
					}
				}
				if (trade.isCollar()) {
					if (ir.compareTo(trade.getFloorStrike()) == -1) {
						paymentIr = trade.getFloorStrike().subtract(ir);
					}
					if (ir.compareTo(trade.getCapStrike()) == 1) {
						paymentIr = ir.subtract(trade.getCapStrike());
					}
				}
				if (paymentIr != null) {
					// the fractioned notional is the notional * accrual
					// factor calculated using the interest period
					BigDecimal fractionedNotional = trade.getIrForwardTrade().getAmount()
							.multiply(PricerUtil.daysToYear(trade.getIrForwardTrade().getDayCountConvention(),
									beginningOfPeriod, endOfPeriod));
					BigDecimal payment = fractionedNotional.multiply(
							paymentIr.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode()));
					if (valueDate != null) {
						try {
							payment = PricerUtil.discountAmount(payment, curveId, valueDate, cashFlow.getDate(),
									trade.getIrForwardTrade().getDayCountConvention());
						} catch (PricerException pe) {
							throw new TradistaBusinessException(pe.getMessage());
						}
					}
					cashFlow.setAmount(payment);
					cashFlows.add(cashFlow);
				}
			}
		} else {
			LocalDate settlementDate = trade.getSettlementDate();
			LocalDate fixingDate = trade.getSettlementDate();
			if (trade.getIrForwardTrade().getInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				fixingDate = trade.getIrForwardTrade().getMaturityDate();
			}
			ir = PricerUtil.getValueAsOfDateFromQuote(
					trade.getIrForwardTrade().getReferenceRateIndex() + "."
							+ trade.getIrForwardTrade().getReferenceRateIndexTenor(),
					quoteSetId, QuoteType.INTEREST_RATE, QuoteValue.LAST, fixingDate);
			if (ir == null) {
				throw new TradistaBusinessException(String.format(
						"Impossible to calculate triggered payments, there is no '%s' %s quote as of % in this quoteSet: %s",
						QuoteValue.LAST, trade.getIrForwardTrade().getReferenceRateIndex() + "."
								+ trade.getIrForwardTrade().getReferenceRateIndexTenor(),
						cashFlowDate, quoteSetId));
			}

			if (trade.isCap()) {
				if (ir.compareTo(trade.getCapStrike()) == 1) {
					paymentIr = ir.subtract(trade.getCapStrike());
				}
			}
			if (trade.isFloor()) {
				if (ir.compareTo(trade.getFloorStrike()) == -1) {
					paymentIr = trade.getFloorStrike().subtract(ir);
				}
			}
			if (trade.isCollar()) {
				if (ir.compareTo(trade.getFloorStrike()) == -1) {
					paymentIr = trade.getFloorStrike().subtract(ir);
				}
				if (ir.compareTo(trade.getCapStrike()) == 1) {
					paymentIr = ir.subtract(trade.getCapStrike());
				}
			}
			if (paymentIr != null) {
				// the fractioned notional is the notional * accrual
				// factor calculated using the period between the trade settlement and maturity
				// date
				BigDecimal fractionedNotional = trade.getIrForwardTrade().getAmount()
						.multiply(PricerUtil.daysToYear(trade.getIrForwardTrade().getDayCountConvention(),
								trade.getSettlementDate(), trade.getIrForwardTrade().getMaturityDate()));
				BigDecimal payment = fractionedNotional.multiply(
						paymentIr.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode()));
				if (valueDate != null) {
					try {
						payment = PricerUtil.discountAmount(payment, curveId, valueDate, settlementDate,
								trade.getIrForwardTrade().getDayCountConvention());
					} catch (PricerException pe) {
						throw new TradistaBusinessException(pe.getMessage());
					}
				}
				CashFlow cashFlow = new CashFlow();
				cashFlow.setCurrency(trade.getIrForwardTrade().getCurrency());
				if (trade.getIrForwardTrade().getInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					settlementDate = trade.getIrForwardTrade().getMaturityDate();
				}
				cashFlow.setDate(settlementDate);
				cashFlow.setAmount(payment);
				cashFlows.add(cashFlow);
			}
		}

		return cashFlows;
	}

	/**
	 * Returns the total amount of payments triggered for the given trade.
	 * 
	 * @param trade      the trade for which we want to calculate the total amount
	 *                   of payments.
	 * @param valueDate  used to discount the payments. If it is null, the payments
	 *                   are not discounted.
	 * @param curveId    the curve used to discount.
	 * @param quoteSetId the quote set id, used to retrieve interest rates from
	 *                   quotes.
	 * @return the total amount of payments triggered for the given trade.
	 * @throws TradistaBusinessException If a payment cannot be calculated cause of
	 *                                   a missing quote or if it cannot be
	 *                                   discounted.
	 */
	public static BigDecimal getPaymentsTotalAmount(IRCapFloorCollarTrade trade, LocalDate valueDate, long curveId,
			long quoteSetId) throws TradistaBusinessException {
		List<CashFlow> cfs = PricerIRCapFloorCollarUtil.getTriggeredPayments(trade, valueDate, curveId, quoteSetId);
		return PricerUtil.getTotalFlowsAmount(cfs, null, null, 0, null);
	}

}