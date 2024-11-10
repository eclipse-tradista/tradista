package org.eclipse.tradista.core.marketdata.bootstraphandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.tenor.model.Tenor;

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

public interface BootstrapHandler {

	String getInstrumentName();

	String getKeyFromQuoteName(String quoteName);

	Instrument getInstrumentByKey(String key, LocalDate quoteDate) throws TradistaBusinessException;

	boolean isZeroCoupon(Instrument instrument, LocalDate quoteDate);

	LocalDate getMaturityDate(Instrument instrument);

	String getKey(Instrument instrument, LocalDate localDate);

	List<CashFlow> getPendingCashFlows(Instrument instrument, LocalDate date, BigDecimal price,
			Map<LocalDate, List<BigDecimal>> zeroCouponRates, Map<LocalDate, BigDecimal> interpolatedValues)
			throws TradistaBusinessException;

	Tenor getFrequency(Instrument instrument);

	/**
	 * Calculates a Zero coupon rate for instruments identified as "Zero Coupon"
	 * instruments (ie helper.isZeroCoupon == true)
	 * 
	 * @param instrument
	 * @param price
	 * @param date
	 * @return
	 */
	BigDecimal calcZeroCoupon(Instrument instrument, BigDecimal price, LocalDate date);

	/**
	 * Price used during the zero coupons determination by bootstrapping. It is used
	 * in this equalization : discounted cfs = price
	 * 
	 * @param price
	 * @return
	 */
	BigDecimal getPrice(BigDecimal price);

}
