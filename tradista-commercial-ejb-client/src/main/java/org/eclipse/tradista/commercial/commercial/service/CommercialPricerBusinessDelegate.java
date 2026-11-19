package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

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

public class CommercialPricerBusinessDelegate {

	private CommercialPricerService commercialPricerService;

	private CommercialTradeValidator validator;

	public CommercialPricerBusinessDelegate() {
		commercialPricerService = TradistaServiceLocator.getInstance().getCommercialPricerService();
		validator = new CommercialTradeValidator();
	}

	public BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return SecurityUtil
				.runEx(() -> commercialPricerService.npvDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(CommercialTrade trade, PricingParameter pp, LocalDate pricingDate)
			throws TradistaBusinessException {
		StringBuffer errorMsg = new StringBuffer();
		if (trade == null) {
			errorMsg.append(String.format("The trade cannot be null.%n"));
		}
		if (pp == null) {
			errorMsg.append(String.format("The pricing parameters cannot be null.%n"));
		}
		if (pricingDate == null) {
			errorMsg.append("The pricing date cannot be null.");
		}
		if (!errorMsg.isEmpty()) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> commercialPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}