package org.eclipse.tradista.fx.fxndf.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.eclipse.tradista.fx.fxndf.service.FXNDFPricerBusinessDelegate;

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

public class PricerMeasureUNREALIZED_PNL extends PricerMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2041318953385865632L;

	private FXNDFPricerBusinessDelegate fxNdfPricerBusinessDelegate;

	public PricerMeasureUNREALIZED_PNL() {
		super();
		fxNdfPricerBusinessDelegate = new FXNDFPricerBusinessDelegate();
	}

	@Pricing
	public BigDecimal markToModel(PricingParameter params, FXNDFTrade trade, Currency currency, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {

		return fxNdfPricerBusinessDelegate.unrealizedPnlLegsDiff(params, trade, currency, pricingDate);
	}

	@Pricing(defaultUNREALIZED_PNL = true)
	public BigDecimal markToMarket(PricingParameter params, FXNDFTrade trade, Currency currency, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {

		return fxNdfPricerBusinessDelegate.unrealizedPnlMarkToMarket(params, trade, currency, pricingDate);
	}

	public String toString() {
		return "UNREALIZED_PNL";
	}

}
