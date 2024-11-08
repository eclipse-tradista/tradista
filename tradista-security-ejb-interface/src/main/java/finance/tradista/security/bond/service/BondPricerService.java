package org.eclipse.tradista.security.bond.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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
public interface BondPricerService {

	BigDecimal ytmNewtonRaphson(PricingParameter params, BondTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal parYieldParYield(PricingParameter params, BondTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal npvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal cleanPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal dirtyPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pnlDefault(PricingParameter params, Bond bond, Book book, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	BigDecimal realizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal unrealizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;

	List<CashFlow> generateCashFlows(PricingParameter params, BondTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException;
}
