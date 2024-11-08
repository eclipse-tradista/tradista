package org.eclipse.tradista.security.specificrepo.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoPricerBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

	private static final long serialVersionUID = -930281138085259718L;
	
	private SpecificRepoPricerBusinessDelegate specificRepoPricerBusinessDelegate;

	public PricerMeasureREALIZED_PNL() {
		specificRepoPricerBusinessDelegate = new SpecificRepoPricerBusinessDelegate();
	}

	@Pricing(defaultREALIZED_PNL = true)
	public BigDecimal realizedPayments(PricingParameter params, SpecificRepoTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		return specificRepoPricerBusinessDelegate.realizedPayments(trade, currency, pricingDate, params);
	}

	public String toString() {
		return "REALIZED_PNL";
	}

}