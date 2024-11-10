package org.eclipse.tradista.fx.fx.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.fx.fx.model.FXTrade;

import jakarta.ejb.Remote;

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
public interface FXPricerService {

	BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal primaryPvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal quotePvDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal unrealizedPnlDiscountedLegsDiff(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal defaultPNL(PricingParameter params, FXTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	List<CashFlow> generateCashFlows(PricingParameter params, FXTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;
}
