package org.eclipse.tradista.core.trade.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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
public class TradeServiceBean implements TradeService {

	private BookBusinessDelegate bookBusinessDelegate;

	private ProductBusinessDelegate productBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	@PostConstruct
	private void init() {
		bookBusinessDelegate = new BookBusinessDelegate();
		productBusinessDelegate = new ProductBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
	}

	@Override
	public List<Trade<? extends Product>> getTradesByCreationDate(LocalDate creationDate) {
		return TradeSQL.getTradesByCreationDate(creationDate);
	}

	@Override
	public List<Trade<? extends Product>> getTradesByDates(LocalDate startCreationDate, LocalDate endCreationDate,
			LocalDate startTradeDate, LocalDate endTradeDate) {
		return TradeSQL.getTradesByDates(startCreationDate, endCreationDate, startTradeDate, endTradeDate);
	}

	@Override
	public Trade<? extends Product> getTradeById(long id) {
		return TradeSQL.getTradeById(id, false);
	}

	@Override
	public Trade<? extends Product> getTradeById(long id, boolean includeUnderlying) {
		return TradeSQL.getTradeById(id, includeUnderlying);
	}

	@Override
	public Set<Trade<? extends Product>> getTrades(PositionDefinition posDef) {
		return TradeSQL.getTrades(posDef);
	}

	@Override
	public void checkTradeBasics(Trade<?> trade) throws TradistaBusinessException {
		checkTradeBasics(trade, false);
	}

	@Override
	public void checkTradeBasics(Trade<?> trade, boolean checkCurrency) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (bookBusinessDelegate.getBookById(trade.getBook().getId()) == null) {
			errMsg.append(String.format("The book %s was not found.%n", trade.getBook()));
		}

		if (legalEntityBusinessDelegate.getLegalEntityById(trade.getCounterparty().getId()) == null) {
			errMsg.append(String.format("The counterparty %s was not found.%n", trade.getCounterparty()));
		}

		if (trade.getProduct() != null) {
			if (productBusinessDelegate.getProductById(trade.getProduct().getId()) == null) {
				errMsg.append(String.format("The product %s was not found.%n", trade.getProduct()));
			}
		}

		if (checkCurrency) {
			if (currencyBusinessDelegate.getCurrencyById(trade.getCurrency().getId()) == null) {
				errMsg.append(String.format("The currency %s was not found.%n", trade.getCurrency()));
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}
}