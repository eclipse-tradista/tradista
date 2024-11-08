package org.eclipse.tradista.ir.ircapfloorcollar.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircapfloorcollar.service.IRCapFloorCollarPricerBusinessDelegate;

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

	private static final long serialVersionUID = -9053442842837930057L;

	private IRCapFloorCollarPricerBusinessDelegate irCapFloorCollarPricerBusinessDelegate;

	public PricerMeasureREALIZED_PNL() {
		super();
		irCapFloorCollarPricerBusinessDelegate = new IRCapFloorCollarPricerBusinessDelegate();
	}

	@Pricing(defaultREALIZED_PNL = true)
	public BigDecimal realizedPnl(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {

		return irCapFloorCollarPricerBusinessDelegate.realizedPnlPaymentTriggers(params, trade, currency, pricingDate);
	}

	public String toString() {
		return "REALIZED_PNL";
	}

}
