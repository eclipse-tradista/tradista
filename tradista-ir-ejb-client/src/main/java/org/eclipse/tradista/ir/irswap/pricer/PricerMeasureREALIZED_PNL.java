package org.eclipse.tradista.ir.irswap.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerBusinessDelegate;

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

public class PricerMeasureREALIZED_PNL extends PricerMeasure {

	private static final long serialVersionUID = -486300699168969227L;
	private IRSwapPricerBusinessDelegate irSwapPricerBusinessDelegate;

	public PricerMeasureREALIZED_PNL() {
		super();
		irSwapPricerBusinessDelegate = new IRSwapPricerBusinessDelegate();
	}

	@Pricing(defaultREALIZED_PNL = true)
	public BigDecimal legsCashFlows(PricingParameter params, SingleCurrencyIRSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {

		return irSwapPricerBusinessDelegate.realizedPnlLegsCashFlows(params, trade, currency, pricingDate);
	}

	public String toString() {
		return "REALIZED_PNL";
	}

}