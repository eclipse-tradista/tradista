package org.eclipse.tradista.security.gcrepo.ui.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.Trade.Direction;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.Workflow;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.service.GCBasketBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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
public class GCRepoTradeController implements Serializable {

	private static final long serialVersionUID = -8633490083412044246L;

	private GCRepoTrade trade;

	private Set<LegalEntity> allCounterparties;

	private Set<Currency> allCurrencies;

	private Set<Index> allIndexes;

	private Set<Book> allBooks;

	private Direction[] allDirections;

	private Set<GCBasket> allBaskets;

	private GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private IndexBusinessDelegate indexBusinessDelegate;

	private GCBasketBusinessDelegate gcBasketBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	private String idToBeLoaded;

	private String[] allInterestTypes;

	private String interestType;

	private Tenor[] allIndexTenors;

	private Workflow workflow;

	private String action;

	private String[] allAvailableActions;

	// Used to get the reduction amount in case of partial termination
	private BigDecimal originalCashAmount;

	private static final String TRADE_MSG = "tradeMsg";

	@PostConstruct
	public void init() throws TradistaBusinessException {
		gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();
		gcBasketBusinessDelegate = new GCBasketBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		indexBusinessDelegate = new IndexBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
		allCurrencies = currencyBusinessDelegate.getAllCurrencies();
		allCounterparties = legalEntityBusinessDelegate.getAllCounterparties();
		allIndexes = indexBusinessDelegate.getAllIndexes();
		allBooks = bookBusinessDelegate.getAllBooks();
		allDirections = Trade.Direction.values();
		allInterestTypes = new String[] { "Fixed", "Floating" };
		allIndexTenors = Arrays.asList(Tenor.values()).stream().filter(t -> !t.equals(Tenor.NO_TENOR))
				.toArray(Tenor[]::new);
		allBaskets = gcBasketBusinessDelegate.getAllGCBaskets();
		workflow = workflowBusinessDelegate.getWorkflowByName(GCRepoTrade.GC_REPO);
		trade = new GCRepoTrade();
		trade.setStatus(workflowBusinessDelegate.getInitialStatus(workflow.getName()));
		setTradeDate(LocalDate.now());
		setStartDate(LocalDate.now());
	}

	public String getId() {
		return trade.getId() == 0 ? "" : Long.toString(trade.getId());
	}

	public void setId(String id) {
		trade.setId(Long.parseLong(id));
	}

	public Direction[] getAllDirections() {
		return allDirections;
	}

	public void setAllDirections(Direction[] allDirections) {
		this.allDirections = allDirections;
	}

	public BigDecimal getAmount() {
		return trade.getAmount();
	}

	public void setAmount(BigDecimal amount) {
		trade.setAmount(amount);
	}

	public Status getStatus() {
		return trade.getStatus();
	}

	public void setStatus(Status status) {
		trade.setStatus(status);
	}

	public GCRepoTrade getTrade() {
		return trade;
	}

	public void setTrade(GCRepoTrade trade) {
		this.trade = trade;
	}

	public BigDecimal getRepoRate() {
		return trade.getRepoRate();
	}

	public void setRepoRate(BigDecimal repoRate) {
		trade.setRepoRate(repoRate);
	}

	public Trade.Direction getDirection() {
		return (trade.isBuy() ? Direction.BUY : Direction.SELL);
	}

	public void setDirection(Trade.Direction direction) {
		trade.setBuySell(Direction.BUY.equals(direction));
	}

	public LocalDate getTradeDate() {
		return trade.getTradeDate();
	}

	public void setTradeDate(LocalDate tradeDate) {
		trade.setTradeDate(tradeDate);
	}

	public LocalDate getStartDate() {
		return trade.getSettlementDate();
	}

	public void setStartDate(LocalDate startDate) {
		trade.setSettlementDate(startDate);
	}

	public LocalDate getEndDate() {
		return trade.getEndDate();
	}

	public void setEndDate(LocalDate endDate) {
		trade.setEndDate(endDate);
	}

	public Currency getCurrency() {
		return trade.getCurrency();
	}

	public void setCurrency(Currency currency) {
		trade.setCurrency(currency);
	}

	public LegalEntity getCounterparty() {
		return trade.getCounterparty();
	}

	public void setCounterparty(LegalEntity legalEntity) {
		trade.setCounterparty(legalEntity);
	}

	public Book getBook() {
		return trade.getBook();
	}

	public void setBook(Book book) {
		trade.setBook(book);
	}

	public Index getIndex() {
		return trade.getIndex();
	}

	public void setIndex(Index index) {
		trade.setIndex(index);
	}

	public Tenor getIndexTenor() {
		return trade.getIndexTenor();
	}

	public void setIndexTenor(Tenor indexTenor) {
		trade.setIndexTenor(indexTenor);
	}

	public BigDecimal getIndexOffset() {
		return trade.getIndexOffset();
	}

	public void setIndexOffset(BigDecimal indexOffset) {
		trade.setIndexOffset(indexOffset);
	}

	public boolean getRightOfSubstitution() {
		return trade.isRightOfSubstitution();
	}

	public void setRightOfSubstitution(boolean rightOfSubstitution) {
		trade.setRightOfSubstitution(rightOfSubstitution);
	}

	public boolean getRightOfReuse() {
		return trade.isRightOfReuse();
	}

	public void setRightOfReuse(boolean rightOfReuse) {
		trade.setRightOfReuse(rightOfReuse);
	}

	public boolean getCrossCurrencyCollateral() {
		return trade.isCrossCurrencyCollateral();
	}

	public void setCrossCurrencyCollateral(boolean crossCurrencyCollateral) {
		trade.setCrossCurrencyCollateral(crossCurrencyCollateral);
	}

	public boolean getTerminableOnDemand() {
		return trade.isTerminableOnDemand();
	}

	public void setTerminableOnDemand(boolean terminableOnDemand) {
		trade.setTerminableOnDemand(terminableOnDemand);
	}

	public Short getNoticePeriod() {
		return trade.getNoticePeriod();
	}

	public void setNoticePeriod(Short noticePeriod) {
		trade.setNoticePeriod(noticePeriod);
	}

	public BigDecimal getMarginRate() {
		return trade.getMarginRate();
	}

	public void setMarginRate(BigDecimal marginRate) {
		trade.setMarginRate(marginRate);
	}

	public GCBasket getBasket() {
		return trade.getGcBasket();
	}

	public void setBasket(GCBasket gcBasket) {
		trade.setGcBasket(gcBasket);
	}

	public Set<Currency> getAllCurrencies() {
		return allCurrencies;
	}

	public void setAllCurrencies(Set<Currency> allCurrencies) {
		this.allCurrencies = allCurrencies;
	}

	public Set<Index> getAllIndexes() {
		return allIndexes;
	}

	public void setAllIndexes(Set<Index> allIndexes) {
		this.allIndexes = allIndexes;
	}

	public Set<LegalEntity> getAllCounterparties() {
		return allCounterparties;
	}

	public void setAllCounterparties(Set<LegalEntity> allCounterparties) {
		this.allCounterparties = allCounterparties;
	}

	public Set<Book> getAllBooks() {
		return allBooks;
	}

	public void setAllBooks(Set<Book> allBooks) {
		this.allBooks = allBooks;
	}

	public String getInterestType() {
		return interestType;
	}

	public void setInterestType(String interestType) {
		this.interestType = interestType;
	}

	public String[] getAllInterestTypes() {
		return allInterestTypes;
	}

	public void setAllInterestTypes(String[] allInterestTypes) {
		this.allInterestTypes = allInterestTypes;
	}

	public Tenor[] getAllIndexTenors() {
		return allIndexTenors;
	}

	public void setAllIndexTenors(Tenor[] allIndexTenors) {
		this.allIndexTenors = allIndexTenors;
	}

	public Set<GCBasket> getAllBaskets() {
		return allBaskets;
	}

	public void setAllBaskets(Set<GCBasket> allBaskets) {
		this.allBaskets = allBaskets;
	}

	public String getIdToBeLoaded() {
		return idToBeLoaded;
	}

	public void setIdToBeLoaded(String idToBeLoaded) {
		this.idToBeLoaded = idToBeLoaded;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void save() {
		try {
			final String actionToApply = (action != null) ? action : ActionConstants.NEW;
			if (trade.getId() == 0) {
				trade.setCreationDate(LocalDate.now());
			}
			if (interestType == null || interestType.equals("Fixed")) {
				trade.setIndex(null);
				trade.setIndexTenor(null);
				trade.setIndexOffset(null);
			}
			long tradeId = gcRepoTradeBusinessDelegate.saveGCRepoTrade(trade, actionToApply);
			if (trade.getId() == 0) {
				trade.setId(tradeId);
			}
			trade = gcRepoTradeBusinessDelegate.getGCRepoTradeById(tradeId);
			Set<String> availableActions = workflowBusinessDelegate.getAvailableActionsFromStatus(workflow.getName(),
					trade.getStatus());
			if (availableActions != null && !availableActions.isEmpty()) {
				allAvailableActions = availableActions.toArray(new String[availableActions.size()]);
			}
			originalCashAmount = trade.getAmount();
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Trade " + trade.getId() + " successfully saved"));
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", te.getMessage()));
		}
	}

	public void copy() {
		long oldId = trade.getId();
		Map<LocalDate, BigDecimal> oldPartialTerminations = trade.getPartialTerminations();
		Map<Security, Map<Book, BigDecimal>> oldCollateralToAdd = trade.getCollateralToAdd();
		Map<Security, Map<Book, BigDecimal>> oldCollateralToRemove = trade.getCollateralToRemove();
		try {
			trade.setCreationDate(LocalDate.now());
			trade.setId(0);
			trade.setStatus(workflowBusinessDelegate.getInitialStatus(workflow.getName()));
			if (interestType == null || interestType.equals("Fixed")) {
				trade.setIndex(null);
				trade.setIndexTenor(null);
				trade.setIndexOffset(null);
			}
			// Copied trades will not have added/removed collaterals and partial
			// terminations
			trade.setCollateralToAdd(null);
			trade.setCollateralToRemove(null);
			trade.setPartialTerminations(null);
			long tradeId = gcRepoTradeBusinessDelegate.saveGCRepoTrade(trade, ActionConstants.NEW);
			trade = gcRepoTradeBusinessDelegate.getGCRepoTradeById(tradeId);
			Set<String> availableActions = workflowBusinessDelegate.getAvailableActionsFromStatus(workflow.getName(),
					trade.getStatus());
			if (availableActions != null && !availableActions.isEmpty()) {
				allAvailableActions = availableActions.toArray(new String[availableActions.size()]);
			}
			originalCashAmount = trade.getAmount();
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Trade " + trade.getId() + " successfully created"));
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			trade.setId(oldId);
			trade.setCollateralToAdd(oldCollateralToAdd);
			trade.setCollateralToRemove(oldCollateralToRemove);
			trade.setPartialTerminations(oldPartialTerminations);
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", te.getMessage()));
		}
	}

	public void load() {
		long tradeId;
		try {
			tradeId = Long.parseLong(idToBeLoaded);
			GCRepoTrade gcTrade = gcRepoTradeBusinessDelegate.getGCRepoTradeById(tradeId);
			if (gcTrade != null) {
				trade.setId(gcTrade.getId());
				trade.setBuySell(gcTrade.isBuy());
				trade.setCounterparty(gcTrade.getCounterparty());
				trade.setCurrency(gcTrade.getCurrency());
				trade.setProduct(gcTrade.getProduct());
				trade.setAmount(gcTrade.getAmount());
				trade.setSettlementDate(gcTrade.getSettlementDate());
				trade.setTradeDate(gcTrade.getTradeDate());
				trade.setBook(gcTrade.getBook());
				trade.setCreationDate(gcTrade.getCreationDate());
				trade.setCrossCurrencyCollateral(gcTrade.isCrossCurrencyCollateral());
				trade.setGcBasket(gcTrade.getGcBasket());
				trade.setEndDate(gcTrade.getEndDate());
				trade.setIndex(gcTrade.getIndex());
				trade.setIndexOffset(gcTrade.getIndexOffset());
				trade.setMarginRate(gcTrade.getMarginRate());
				trade.setNoticePeriod(gcTrade.getNoticePeriod());
				trade.setRepoRate(gcTrade.getRepoRate());
				trade.setRightOfReuse(gcTrade.isRightOfReuse());
				trade.setRightOfSubstitution(gcTrade.isRightOfSubstitution());
				trade.setTerminableOnDemand(gcTrade.isTerminableOnDemand());
				trade.setStatus(gcTrade.getStatus());
				trade.setPartialTerminations(gcTrade.getPartialTerminations());
				Set<String> availableActions = workflowBusinessDelegate
						.getAvailableActionsFromStatus(workflow.getName(), gcTrade.getStatus());
				if (availableActions != null && !availableActions.isEmpty()) {
					allAvailableActions = availableActions.toArray(new String[availableActions.size()]);
				}
				originalCashAmount = gcTrade.getAmount();
				FacesContext.getCurrentInstance().addMessage(TRADE_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Info", "Trade " + trade.getId() + " successfully loaded."));
			} else {
				FacesContext.getCurrentInstance().addMessage(TRADE_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "Trade " + idToBeLoaded + " was not found."));
			}
		} catch (NumberFormatException nfe) {
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please type a valid id."));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

	}

	public void clear() {
		trade = new GCRepoTrade();
		try {
			trade.setStatus(workflowBusinessDelegate.getInitialStatus(workflow.getName()));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(TRADE_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error", String.format("Could not clear trade data: %s", tbe.getMessage())));
		}
		setTradeDate(LocalDate.now());
		setStartDate(LocalDate.now());
		originalCashAmount = null;
		action = null;
		allAvailableActions = null;
		FacesContext.getCurrentInstance().addMessage(TRADE_MSG,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Form cleared"));
	}

	public void updateIndex() {
		if (interestType != null && interestType.equals("Floating")) {
			if (trade.getIndex() == null && allIndexes != null && !allIndexes.isEmpty()) {
				trade.setIndex(allIndexes.stream().findFirst().get());
			}
			if (trade.getIndexTenor() == null) {
				trade.setIndexTenor(allIndexTenors[0]);
			}
		}
		if (interestType != null && interestType.equals("Fixed")) {
			trade.setIndex(null);
			trade.setIndexTenor(null);
		}
	}

	public String[] getAllAvailableActions() {
		return allAvailableActions;
	}

	public void setAllAvailableActions(String[] allAvailableActions) {
		this.allAvailableActions = allAvailableActions;
	}

	public BigDecimal getOriginalCashAmount() {
		return originalCashAmount;
	}

	public void setOriginalCashAmount(BigDecimal originalCashAmount) {
		this.originalCashAmount = originalCashAmount;
	}

	public void updateTrade(Map<Security, Map<Book, BigDecimal>> securitiesToAdd,
			Map<Security, Map<Book, BigDecimal>> securitiesToRemove) {
		if (securitiesToAdd != null && !securitiesToAdd.isEmpty()) {
			trade.setCollateralToAdd(securitiesToAdd);
		}
		if (securitiesToRemove != null && !securitiesToRemove.isEmpty()) {
			trade.setCollateralToRemove(securitiesToRemove);
		}
		if (action != null && action.equals(ActionConstants.PARTIALLY_TERMINATE)) {
			trade.addParTialTermination(LocalDate.now(), originalCashAmount.subtract(trade.getAmount()));
		}
	}

}