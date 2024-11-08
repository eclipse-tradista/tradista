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

public class PricerMeasureEXPOSURE extends PricerMeasure {

	private static final long serialVersionUID = -4097653953464619077L;

	private SpecificRepoPricerBusinessDelegate specificRepoPricerBusinessDelegate;

	public PricerMeasureEXPOSURE() {
		specificRepoPricerBusinessDelegate = new SpecificRepoPricerBusinessDelegate();
	}

	public String toString() {
		return "EXPOSURE";
	}

	@Pricing
	public BigDecimal exposure(PricingParameter params, SpecificRepoTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return specificRepoPricerBusinessDelegate.getExposure(trade, currency, pricingDate, params);
	}
}