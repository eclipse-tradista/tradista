package org.eclipse.tradista.mm.loandeposit.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;
import org.eclipse.tradista.mm.loandeposit.validator.LoanDepositTradeValidator;

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

public class LoanDepositTradeBusinessDelegate {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	private LoanDepositTradeService loanDepositTradeService;

	private LoanDepositTradeValidator validator;

	public LoanDepositTradeBusinessDelegate() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		loanDepositTradeService = TradistaServiceLocator.getInstance().getLoanDepositService();
		validator = new LoanDepositTradeValidator();
	}

	public long saveLoanDepositTrade(LoanDepositTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> loanDepositTradeService.saveLoanDepositTrade(trade));
	}

	public boolean isBusinessDay(LoanDepositTrade mmTrade, LocalDate date) throws TradistaBusinessException {
		Currency tradeCurrency;
		Calendar currencyCalendar;
		if (mmTrade == null) {
			throw new TradistaBusinessException("The MM trade cannot be null");
		}
		tradeCurrency = mmTrade.getCurrency();
		if (tradeCurrency == null) {
			throw new TradistaBusinessException("The MM trade currency cannot be null");
		}
		currencyCalendar = tradeCurrency.getCalendar();
		if (currencyCalendar == null) {
			// TODO add a warning log
		}
		if (currencyCalendar != null) {
			return currencyCalendar.isBusinessDay(date);
		} else {
			return true;
		}
	}

	public BigDecimal getPaymentAmount(LoanDepositTrade trade, LocalDate fixingDate, LocalDate endOfPeriod,
			long quoteSetId, long indexCurveId) throws TradistaBusinessException {
		LocalDate beginningOfPeriod = DateUtil.subtractTenor(endOfPeriod, trade.getPaymentFrequency());
		BigDecimal daysToYear = PricerUtil.daysToYear(trade.getDayCountConvention(), beginningOfPeriod, endOfPeriod);
		BigDecimal r = null;
		if (trade.isFixed()) {
			r = trade.getFixedRate();
		}
		BigDecimal amount = trade.getAmount().multiply(daysToYear);
		// TODO See how to log the calculated interests below.
		if (trade.getInterestType().equals(InterestType.SIMPLE)) {
			if (!trade.isFixed()) {
				try {
					r = PricerUtil.getInterestRateAsOfDate(
							trade.getFloatingRateIndex() + "." + trade.getFloatingRateIndexTenor(), quoteSetId,
							indexCurveId, trade.getFloatingRateIndexTenor(), trade.getDayCountConvention(), fixingDate);
				} catch (PricerException pe) {
					throw new TradistaBusinessException(pe.getMessage());
				}
				if (trade.getSpread() != null) {
					// Spreads are expressed as basis point. It must be divided by 100 before being
					// added to a percentage
					// 1bp = 0.01%
					r = r.add(trade.getSpread().divide(BigDecimal.valueOf(100),
							SecurityUtil.run(() -> configurationBusinessDelegate.getRoundingMode())));
				}
			}
			return amount.multiply(r).divide(BigDecimal.valueOf(100),
					SecurityUtil.run(() -> configurationBusinessDelegate.getRoundingMode()));
		} else {
			double n;
			if (!trade.getCompoundPeriod().equals(Tenor.NO_TENOR)) {
				n = 1 / PricerUtil.daysToYear(trade.getDayCountConvention(), beginningOfPeriod, endOfPeriod)
						.doubleValue();
			} else {
				n = 1;
			}
			// First case: fixed rates
			if (trade.isFixed()) {
				BigDecimal lastPayment = BigDecimal.ZERO;
				double t = PricerUtil.daysToYear(trade.getDayCountConvention(), trade.getSettlementDate(), endOfPeriod)
						.doubleValue();
				double multiplier = Math.pow((1 + ((r.doubleValue() / 100) / n)), n * t) - 1;
				// Subtract the payment of the last period
				double lastPeriod = t - daysToYear.doubleValue();
				if (lastPeriod > 0) {
					lastPayment = trade.getAmount().multiply(
							BigDecimal.valueOf(Math.pow((1 + ((r.doubleValue() / 100) / n)), n * lastPeriod) - 1));
				}
				return (trade.getAmount().multiply(BigDecimal.valueOf(multiplier))).subtract(lastPayment);
			}
			// Second case: floating rates
			else {
				LocalDate valDate = trade.getSettlementDate();
				BigDecimal paymentAmount = trade.getAmount();
				BigDecimal capital = trade.getAmount();
				while (!valDate.isAfter(fixingDate)) {
					try {
						r = PricerUtil.getInterestRateAsOfDate(
								trade.getFloatingRateIndex() + "." + trade.getFloatingRateIndexTenor(), quoteSetId,
								indexCurveId, trade.getFloatingRateIndexTenor(), trade.getDayCountConvention(),
								valDate);

						if (trade.getSpread() != null) {
							// Spreads are expressed as basis point. It must be divided by 100 before being
							// added to a percentage
							// 1bp = 0.01%
							r = r.add(trade.getSpread().divide(BigDecimal.valueOf(100),
									SecurityUtil.run(() -> configurationBusinessDelegate.getRoundingMode())));
						}
						BigDecimal lastCapital = capital;
						capital = capital.multiply(BigDecimal.valueOf((1 + ((r.doubleValue() / 100) / n))));
						paymentAmount = capital.subtract(lastCapital);
						try {
							valDate = DateUtil.addTenor(valDate, trade.getPaymentFrequency());
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					} catch (PricerException pe) {
						throw new TradistaBusinessException(pe.getMessage());
					}
				}
				return paymentAmount;
			}
		}
	}

	public LoanDepositTrade getLoanDepositTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> loanDepositTradeService.getLoanDepositTradeById(id));
	}

}