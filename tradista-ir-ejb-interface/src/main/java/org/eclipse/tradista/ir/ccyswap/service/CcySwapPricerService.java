package org.eclipse.tradista.ir.ccyswap.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Remote
public interface CcySwapPricerService {

	BigDecimal fixedLegPvDiscountedCashFlow(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal floatingLegPvDiscountedCashFlow(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal forwardSwapRateForwardSwapRate(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal npvDiscountedLegsDiff(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pnlDefault(PricingParameter params, CcySwapTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal realizedPnlLegsCashFlows(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal unrealizedPnlLegsDiff(PricingParameter params, CcySwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	List<CashFlow> generateCashFlows(PricingParameter params, CcySwapTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException;
}