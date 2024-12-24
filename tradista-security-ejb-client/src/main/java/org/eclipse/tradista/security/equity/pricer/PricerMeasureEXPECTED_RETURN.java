package org.eclipse.tradista.security.equity.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.service.EquityPricerBusinessDelegate;

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

public class PricerMeasureEXPECTED_RETURN extends PricerMeasure {

	private static final long serialVersionUID = 7565285618234200040L;

	private EquityPricerBusinessDelegate equityPricerBusinessDelegate;

	public PricerMeasureEXPECTED_RETURN() {
		super();
		equityPricerBusinessDelegate = new EquityPricerBusinessDelegate();
	}

	public String toString() {
		return "EXPECTED_RETURN";
	}

	/**
	 * Returns the equity expected return rate using CAPM : ERR = Rf + B(Rm - Rf) Rf
	 * : the Risk free rate B : the equity Beta Rm : the expected market return rate
	 * 
	 * @param params
	 * @param trade
	 * @param currency
	 * @return
	 * @throws PricerException
	 * @throws TradistaBusinessException
	 */
	@Pricing
	public BigDecimal capm(PricingParameter params, EquityTrade trade, Currency currency, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {
		return equityPricerBusinessDelegate.expectedReturnCapm(params, trade, currency, pricingDate);
	}

}