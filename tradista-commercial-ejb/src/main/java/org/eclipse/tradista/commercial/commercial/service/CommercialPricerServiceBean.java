package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.pricer.CommercialPricerUtil;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class CommercialPricerServiceBean implements CommercialPricerService {

	@Override
	public BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		try {
			return CommercialPricerUtil.npvDiscountedCashFlow(params, trade, currency, pricingDate);
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, CommercialTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {
		try {
			return CommercialPricerUtil.generateCashFlows(params, trade, pricingDate);
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

}