package org.eclipse.tradista.web.demo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.ui.converter.BookConverter;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.currency.ui.converter.CurrencyConverter;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.ui.converter.LegalEntityConverter;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.Trade.Direction;
import org.eclipse.tradista.core.trade.ui.converter.DirectionConverter;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityTradeBusinessDelegate;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@Named
@ViewScoped
public class EquityTradeController implements Serializable {

	private static final long serialVersionUID = -4372582261989446912L;

	private EquityTrade equityTrade;

	private Set<Equity> allEquities;

	private Set<LegalEntity> allCounterparties;

	private Set<Currency> allCurrencies;

	private Direction[] allDirections;

	private EquityBusinessDelegate equityBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private EquityTradeBusinessDelegate equityTradeBusinessDelegate;

	private String idToBeLoaded;

	@PostConstruct
	public void init() throws TradistaBusinessException {
		equityBusinessDelegate = new EquityBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		allEquities = equityBusinessDelegate.getAllEquities();
		allCurrencies = currencyBusinessDelegate.getAllCurrencies();
		allCounterparties = legalEntityBusinessDelegate.getAllCounterparties();
		allDirections = Trade.Direction.values();
		equityTrade = new EquityTrade();
		equityTrade.setTradeDate(LocalDate.now());
		equityTrade.setSettlementDate(LocalDate.now());
		equityTrade.setBook(bookBusinessDelegate.getBookByName("Demo Book"));
		equityTrade.setBuySell(true);
		equityTradeBusinessDelegate = new EquityTradeBusinessDelegate();
	}

	public String getId() {
		return equityTrade.getId() == 0 ? "" : Long.toString(equityTrade.getId());
	}

	public void setId(String id) {
		equityTrade.setId(Long.parseLong(id));
	}

	public Equity getEquity() {
		return equityTrade.getProduct();
	}

	public void setEquity(Equity equity) {
		equityTrade.setProduct(equity);
	}

	public Direction[] getAllDirections() {
		return allDirections;
	}

	public void setAllDirections(Direction[] allDirections) {
		this.allDirections = allDirections;
	}

	public BigDecimal getQuantity() {
		return equityTrade.getQuantity();
	}

	public void setQuantity(BigDecimal quantity) {
		equityTrade.setQuantity(quantity);
	}

	public BigDecimal getPrice() {
		return equityTrade.getAmount();
	}

	public void setPrice(BigDecimal price) {
		equityTrade.setAmount(price);
	}

	public Trade.Direction getDirection() {
		return (equityTrade.isBuy() ? Direction.BUY : Direction.SELL);
	}

	public void setDirection(Trade.Direction direction) {
		equityTrade.setBuySell(Direction.BUY.equals(direction));
	}

	public LocalDate getTradeDate() {
		return equityTrade.getTradeDate();
	}

	public void setTradeDate(LocalDate tradeDate) {
		equityTrade.setTradeDate(tradeDate);
	}

	public LocalDate getSettlementDate() {
		return equityTrade.getSettlementDate();
	}

	public void setSettlementDate(LocalDate settlementDate) {
		equityTrade.setSettlementDate(settlementDate);
	}

	public Currency getCurrency() {
		return equityTrade.getCurrency();
	}

	public void setCurrency(Currency currency) {
		equityTrade.setCurrency(currency);
	}

	public LegalEntity getCounterparty() {
		return equityTrade.getCounterparty();
	}

	public void setCounterparty(LegalEntity legalEntity) {
		equityTrade.setCounterparty(legalEntity);
	}

	public Book getBook() {
		return equityTrade.getBook();
	}

	public void setBook(Book book) {
		equityTrade.setBook(book);
	}

	public Set<Equity> getAllEquities() {
		return allEquities;
	}

	public void setAllEquities(Set<Equity> allEquities) {
		this.allEquities = allEquities;
	}

	public Set<Currency> getAllCurrencies() {
		return allCurrencies;
	}

	public void setAllCurrencies(Set<Currency> allCurrencies) {
		this.allCurrencies = allCurrencies;
	}

	public Set<LegalEntity> getAllCounterparties() {
		return allCounterparties;
	}

	public void setAllCounterparties(Set<LegalEntity> allCounterparties) {
		this.allCounterparties = allCounterparties;
	}

	public String getIdToBeLoaded() {
		return idToBeLoaded;
	}

	public void setIdToBeLoaded(String idToBeLoaded) {
		this.idToBeLoaded = idToBeLoaded;
	}

	public void save() {
		try {
			equityTrade.setCreationDate(LocalDate.now());
			long tradeId = equityTradeBusinessDelegate.saveEquityTrade(equityTrade);
			if (equityTrade.getId() == 0) {
				equityTrade.setId(tradeId);
			}
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Trade " + equityTrade.getId() + " successfully saved"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void copy() {
		long oldId = equityTrade.getId();
		try {
			equityTrade.setCreationDate(LocalDate.now());
			equityTrade.setId(0);
			long tradeId = equityTradeBusinessDelegate.saveEquityTrade(equityTrade);
			equityTrade.setId(tradeId);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Trade " + equityTrade.getId() + " successfully created"));
		} catch (TradistaBusinessException tbe) {
			equityTrade.setId(oldId);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void load() {
		long tradeId;
		try {
			tradeId = Long.parseLong(idToBeLoaded);
			EquityTrade eqTrade = equityTradeBusinessDelegate.getEquityTradeById(tradeId);
			equityTrade.setId(eqTrade.getId());
			equityTrade.setBuySell(eqTrade.isBuy());
			equityTrade.setCounterparty(eqTrade.getCounterparty());
			equityTrade.setCurrency(eqTrade.getCurrency());
			equityTrade.setProduct(eqTrade.getProduct());
			equityTrade.setAmount(eqTrade.getAmount());
			equityTrade.setQuantity(eqTrade.getQuantity());
			equityTrade.setSettlementDate(eqTrade.getSettlementDate());
			equityTrade.setTradeDate(eqTrade.getTradeDate());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Trade " + equityTrade.getId() + " successfully loaded"));
		} catch (NumberFormatException nfe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please type a valid id."));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

	}

	public void clear() {
		equityTrade.setId(0);
		equityTrade.setAmount(null);
		equityTrade.setQuantity(null);
		equityTrade.setTradeDate(LocalDate.now());
		equityTrade.setSettlementDate(LocalDate.now());
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Form cleared"));
	}

	public EquityConverter getEquityConverter() {
		return new EquityConverter();
	}

	public CurrencyConverter getCurrencyConverter() {
		return new CurrencyConverter();
	}

	public BookConverter getBookConverter() {
		return new BookConverter();
	}

	public LegalEntityConverter getLegalEntityConverter() {
		return new LegalEntityConverter();
	}

	public DirectionConverter getDirectionConverter() {
		return new DirectionConverter();
	}
}