package org.eclipse.tradista.security.equityoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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
public interface EquityOptionPricerService {

	BigDecimal pvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal npvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal npvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal expectedReturnCapm(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOption equityOption, Book book,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pnlDefault(PricingParameter params, EquityOption equityOption, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal realizedPnlDefault(PricingParameter params, EquityOption equityOption, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal realizedPnlOptionExercise(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	BigDecimal pnlDefault(PricingParameter params, EquityOptionTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException;
}