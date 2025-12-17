package org.eclipse.tradista.security.gcrepo.ui.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ColorUtil;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.common.service.SecurityBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.service.GCRepoPricerBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.model.ProcessingOrgDefaultsCollateralManagementModule;
import org.eclipse.tradista.security.repo.ui.controller.Collateral;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.xdev.chartjs.model.charts.DoughnutChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.DoughnutData;
import software.xdev.chartjs.model.dataset.DoughnutDataset;
import software.xdev.chartjs.model.options.DoughnutOptions;

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
public class GCRepoCollateralController implements Serializable {

	private static final long serialVersionUID = 3483809203529526285L;

	private GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate;

	private GCRepoPricerBusinessDelegate gcRepoPricerBusinessDelegate;

	private ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private ProcessingOrgDefaultsBusinessDelegate poDefaultsBusinessDelegate;

	private SecurityBusinessDelegate securityBusinessDelegate;

	private String context;

	private List<Collateral> collateralValues;

	private List<Collateral> availableCollateralValues;

	private List<Collateral> addedCollateralValues;

	private List<Collateral> removedCollateralValues;

	private String collateralValueDonutModel;

	private String securityToAdd;

	private String fromBookToAdd;

	private BigDecimal maxQuantityToAdd;

	private BigDecimal quantityToAdd;

	private String exchangeToAdd;

	private String securityToRemove;

	private String fromBookToRemove;

	private BigDecimal maxQuantityToRemove;

	private BigDecimal quantityToRemove;

	private String exchangeToRemove;

	private Set<String> securityQuoteNames;

	public String getSecurityToRemove() {
		return securityToRemove;
	}

	public void setSecurityToRemove(String securityToRemove) {
		this.securityToRemove = securityToRemove;
	}

	public String getFromBookToRemove() {
		return fromBookToRemove;
	}

	public void setFromBookToRemove(String fromBookToRemove) {
		this.fromBookToRemove = fromBookToRemove;
	}

	public BigDecimal getMaxQuantityToRemove() {
		return maxQuantityToRemove;
	}

	public void setMaxQuantityToRemove(BigDecimal maxQuantityToRemove) {
		this.maxQuantityToRemove = maxQuantityToRemove;
	}

	public BigDecimal getQuantityToRemove() {
		return quantityToRemove;
	}

	public void setQuantityToRemove(BigDecimal quantityToRemove) {
		this.quantityToRemove = quantityToRemove;
	}

	private GCRepoTrade trade;

	private static final String COL_MSG = "colMsg";

	@PostConstruct
	public void init() {
		gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();
		gcRepoPricerBusinessDelegate = new GCRepoPricerBusinessDelegate();
		productInventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		poDefaultsBusinessDelegate = new ProcessingOrgDefaultsBusinessDelegate();
		securityBusinessDelegate = new SecurityBusinessDelegate();
		DoughnutChart dc = new DoughnutChart();
		dc.setData(DoughnutChart.data());
		dc.getData().addDataset(new DoughnutDataset());
		collateralValueDonutModel = dc.toJson();

	}

	public List<Collateral> getCollateralValues() {
		return collateralValues;
	}

	public void setCollateralValues(List<Collateral> collateralValues) {
		this.collateralValues = collateralValues;
	}

	public List<Collateral> getAddedCollateralValues() {
		return addedCollateralValues;
	}

	public void setAddedCollateralValues(List<Collateral> addedCollateralValues) {
		this.addedCollateralValues = addedCollateralValues;
	}

	public List<Collateral> getAvailableCollateralValues() {
		return availableCollateralValues;
	}

	public void setAvailableCollateralValues(List<Collateral> availableCollateralValues) {
		this.availableCollateralValues = availableCollateralValues;
	}

	public void setCollateralToAdd(String security, String exchange, String book, BigDecimal quantity) {
		securityToAdd = security;
		exchangeToAdd = exchange;
		fromBookToAdd = book;
		maxQuantityToAdd = quantity;
	}

	public void updateCollateralToAdd(BigDecimal quantity) {

		if (quantity.compareTo(maxQuantityToAdd) > 0) {
			FacesContext.getCurrentInstance().addMessage(COL_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					String.format("You cannot allocate more than %.2f", maxQuantityToAdd.doubleValue())));
			clearCollateralToAdd();
			return;
		}

		if (collateralValues != null) {
			boolean found = false;
			for (Collateral coll : collateralValues) {
				if (coll.getSecurity().equals(securityToAdd) && coll.getExchange().equals(exchangeToAdd)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral collToAdd = new Collateral();
				collToAdd.setQuantity(quantity);
				collToAdd.setSecurity(securityToAdd);
				collToAdd.setExchange(exchangeToAdd);
				collToAdd.setBook(trade.getBook().getName());
				collateralValues.add(collToAdd);
			}
		} else {
			collateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setSecurity(securityToAdd);
			coll.setExchange(exchangeToAdd);
			coll.setBook(trade.getBook().getName());
			collateralValues.add(coll);
		}

		if (availableCollateralValues != null) {
			Collateral toBeRemoved = null;
			for (Collateral coll : availableCollateralValues) {
				if (coll.getSecurity().equals(securityToAdd) && coll.getExchange().equals(exchangeToAdd)) {
					coll.setQuantity(coll.getQuantity().subtract(quantity));
					if (coll.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
						toBeRemoved = coll;
					}
					break;
				}
			}
			if (toBeRemoved != null) {
				availableCollateralValues.remove(toBeRemoved);
			}
		}

		if (addedCollateralValues != null) {
			boolean found = false;
			for (Collateral coll : addedCollateralValues) {
				if (coll.getSecurity().equals(securityToAdd) && coll.getExchange().equals(exchangeToAdd)
						&& coll.getBook().equals(fromBookToAdd)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral collToAdd = new Collateral();
				collToAdd.setQuantity(quantity);
				collToAdd.setBook(fromBookToAdd);
				collToAdd.setSecurity(securityToAdd);
				collToAdd.setExchange(exchangeToAdd);
				addedCollateralValues.add(collToAdd);
			}
		} else {
			addedCollateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setSecurity(securityToAdd);
			coll.setExchange(exchangeToAdd);
			coll.setBook(fromBookToAdd);
			addedCollateralValues.add(coll);
		}

		refreshDonutModel();
	}

	public void updateCollateralToRemove(BigDecimal quantity) {

		if (quantity.compareTo(maxQuantityToRemove) > 0) {
			FacesContext.getCurrentInstance().addMessage(COL_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					String.format("You cannot allocate more than %.2f", maxQuantityToRemove.doubleValue())));
			clearCollateralToRemove();
			return;
		}

		if (availableCollateralValues != null) {
			boolean found = false;
			for (Collateral coll : availableCollateralValues) {
				if (coll.getSecurity().equals(securityToRemove) && coll.getExchange().equals(exchangeToRemove)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral collToRemove = new Collateral();
				collToRemove.setQuantity(quantity);
				collToRemove.setSecurity(securityToRemove);
				collToRemove.setBook(fromBookToRemove);
				collToRemove.setExchange(exchangeToRemove);
				availableCollateralValues.add(collToRemove);
			}
		} else {
			availableCollateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setSecurity(securityToRemove);
			coll.setBook(fromBookToRemove);
			coll.setExchange(exchangeToRemove);
			availableCollateralValues.add(coll);
		}

		if (collateralValues != null) {
			Collateral toBeRemoved = null;
			for (Collateral coll : collateralValues) {
				if (coll.getSecurity().equals(securityToRemove) && coll.getExchange().equals(exchangeToRemove)) {
					coll.setQuantity(coll.getQuantity().subtract(quantity));
					if (coll.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
						toBeRemoved = coll;
					}
					break;
				}
			}
			if (toBeRemoved != null) {
				collateralValues.remove(toBeRemoved);
			}
		}

		if (removedCollateralValues != null) {
			boolean found = false;
			for (Collateral coll : removedCollateralValues) {
				if (coll.getSecurity().equals(securityToRemove) && coll.getExchange().equals(exchangeToRemove)
						&& coll.getBook().equals(fromBookToRemove)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral collToRemove = new Collateral();
				collToRemove.setQuantity(quantity);
				collToRemove.setBook(fromBookToRemove);
				collToRemove.setSecurity(securityToRemove);
				collToRemove.setExchange(exchangeToRemove);
				removedCollateralValues.add(collToRemove);
			}
		} else {
			removedCollateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setSecurity(securityToRemove);
			coll.setExchange(exchangeToRemove);
			coll.setBook(fromBookToRemove);
			removedCollateralValues.add(coll);
		}

		refreshDonutModel();
	}

	public void removeCollateral(String security, String exchange, String fromBook, BigDecimal quantity) {

		if (collateralValues != null) {
			Collateral toBeRemoved = null;
			for (Collateral coll : collateralValues) {
				if (coll.getSecurity().equals(security) && coll.getExchange().equals(exchange)) {
					coll.setQuantity(coll.getQuantity().subtract(quantity));
					if (coll.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
						toBeRemoved = coll;
					}
					break;
				}
			}
			if (toBeRemoved != null) {
				collateralValues.remove(toBeRemoved);
			}
		}

		if (addedCollateralValues != null) {
			addedCollateralValues = addedCollateralValues.stream().filter(c -> !c.getSecurity().equals(security)
					|| !c.getBook().equals(fromBook) || !c.getExchange().equals(exchange)).collect(Collectors.toList());
		}

		if (availableCollateralValues != null) {
			boolean found = false;
			for (Collateral coll : availableCollateralValues) {
				if (coll.getSecurity().equals(security) && coll.getExchange().equals(exchange)
						&& coll.getBook().equals(fromBook)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral removedColl = new Collateral();
				removedColl.setQuantity(quantity);
				removedColl.setBook(fromBook);
				removedColl.setSecurity(security);
				removedColl.setExchange(exchange);
				availableCollateralValues.add(removedColl);
			}
		} else {
			availableCollateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setBook(fromBook);
			coll.setSecurity(security);
			coll.setExchange(exchange);
			availableCollateralValues.add(coll);
		}

		refreshDonutModel();
	}

	public void readdCollateral(String security, String exchange, String book, BigDecimal quantity) {

		if (availableCollateralValues != null) {
			Collateral toBeRemoved = null;
			for (Collateral coll : availableCollateralValues) {
				if (coll.getSecurity().equals(security) && coll.getExchange().equals(exchange)
						&& coll.getBook().equals(book)) {
					coll.setQuantity(coll.getQuantity().subtract(quantity));
					if (coll.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
						toBeRemoved = coll;
					}
					break;
				}
			}
			if (toBeRemoved != null) {
				availableCollateralValues.remove(toBeRemoved);
			}
		}

		if (removedCollateralValues != null) {
			removedCollateralValues = removedCollateralValues.stream()
					.filter(c -> !c.getSecurity().equals(security) || !c.getExchange().equals(exchange))
					.collect(Collectors.toList());
		}

		if (collateralValues != null) {
			boolean found = false;
			for (Collateral coll : collateralValues) {
				if (coll.getSecurity().equals(security) && coll.getExchange().equals(exchange)) {
					coll.setQuantity(coll.getQuantity().add(quantity));
					found = true;
					break;
				}
			}
			if (!found) {
				Collateral readdedColl = new Collateral();
				readdedColl.setQuantity(quantity);
				readdedColl.setSecurity(security);
				readdedColl.setBook(book);
				readdedColl.setExchange(exchange);
				collateralValues.add(readdedColl);
			}
		} else {
			collateralValues = new ArrayList<>();
			Collateral coll = new Collateral();
			coll.setQuantity(quantity);
			coll.setSecurity(security);
			coll.setBook(book);
			coll.setExchange(exchange);
			collateralValues.add(coll);
		}

		refreshDonutModel();
	}

	public void setCollateralToRemove(String security, String exchange, String fromBook, BigDecimal quantity) {
		securityToRemove = security;
		exchangeToRemove = exchange;
		fromBookToRemove = fromBook;
		maxQuantityToRemove = quantity;
	}

	public void clearCollateralToAdd() {
		securityToAdd = null;
		fromBookToAdd = null;
		maxQuantityToAdd = null;
		exchangeToAdd = null;
	}

	public void clearCollateralToRemove() {
		securityToRemove = null;
		fromBookToRemove = null;
		maxQuantityToRemove = null;
		exchangeToRemove = null;
	}

	public void clear() {
		clearCollateralToAdd();
		clearCollateralToRemove();
		context = null;
		trade = null;
		collateralValues = null;
		addedCollateralValues = null;
		availableCollateralValues = null;
		collateralValueDonutModel = null;
		securityQuoteNames = null;
	}

	public void refresh(long tradeId) {

		try {
			GCRepoTrade trade = gcRepoTradeBusinessDelegate.getGCRepoTradeById(tradeId);
			if (trade != null) {
				AllocationConfiguration ac = ((ProcessingOrgDefaultsCollateralManagementModule) poDefaultsBusinessDelegate
						.getProcessingOrgDefaultsByPoId(trade.getBook().getProcessingOrg().getId())
						.getModuleByName(ProcessingOrgDefaultsCollateralManagementModule.COLLATERAL_MANAGEMENT))
						.getAllocationConfiguration();
				if (ac == null) {
					throw new TradistaBusinessException(
							"An Allocation Configuration must be set in the Processing Org Defaults.");
				}
				Set<Book> configuredBooks = ac.getBooks();
				if (configuredBooks == null || configuredBooks.isEmpty()) {
					throw new TradistaBusinessException(String
							.format("Books should be configured in the Allocation Configuration '%s'.", ac.getName()));
				}
				Set<Security> basketSecurities = trade.getGcBasket().getSecurities();
				if (CollectionUtils.isEmpty(basketSecurities)) {
					FacesContext.getCurrentInstance().addMessage(COL_MSG,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning",
									String.format("The basket '%s' doesn't contain securities.", trade.getGcBasket())));
				}
				if (collateralValues == null) {
					collateralValues = new ArrayList<>();
				}
				if (availableCollateralValues == null) {
					availableCollateralValues = new ArrayList<>();
				}
				collateralValues.clear();
				availableCollateralValues.clear();
				if (addedCollateralValues != null) {
					addedCollateralValues.clear();
				}
				if (removedCollateralValues != null) {
					removedCollateralValues.clear();
				}
				Map<Security, Map<Book, BigDecimal>> allocatedSecurities = null;
				Set<ProductInventory> inventory = null;

				// TODO Think about a configurable mechanism for context determination
				if (trade.getStatus().getName().equals("UNDER_ALLOCATED")) {
					context = "ALLOCATION";
				}
				if (trade.getStatus().getName().equals(StatusConstants.ALLOCATED)) {
					context = "SUBSTITUTION";
				}
				allocatedSecurities = gcRepoTradeBusinessDelegate.getAllocatedCollateral(trade);

				if (allocatedSecurities != null) {
					for (Map.Entry<Security, Map<Book, BigDecimal>> entry : allocatedSecurities.entrySet()) {
						if (entry.getValue() != null) {
							for (Map.Entry<Book, BigDecimal> bookEntry : entry.getValue().entrySet()) {
								Collateral col = new Collateral();
								col.setQuantity(bookEntry.getValue());
								col.setSecurity(entry.getKey().getIsin());
								col.setExchange(entry.getKey().getExchange().getCode());
								col.setBook(bookEntry.getKey().getName());
								collateralValues.add(col);
							}
						}
					}
				}

				if (!CollectionUtils.isEmpty(basketSecurities)) {
					for (Book b : configuredBooks) {
						inventory = productInventoryBusinessDelegate.getProductInventories(LocalDate.now(),
								LocalDate.now(), Bond.BOND, 0, b.getId(), false);

						if (inventory != null) {
							for (ProductInventory inv : inventory) {
								if (trade.getGcBasket().getSecurities().contains(inv.getProduct())) {
									Collateral col = new Collateral();
									col.setQuantity(inv.getQuantity());
									col.setSecurity(((Bond) inv.getProduct()).getIsin());
									col.setExchange(((Bond) inv.getProduct()).getExchange().getCode());
									col.setBook(inv.getBook().getName());
									availableCollateralValues.add(col);
								}
							}
						}

						inventory = productInventoryBusinessDelegate.getProductInventories(LocalDate.now(),
								LocalDate.now(), Equity.EQUITY, 0, b.getId(), false);

						if (inventory != null) {
							for (ProductInventory inv : inventory) {
								if (trade.getGcBasket().getSecurities().contains(inv.getProduct())) {
									Collateral col = new Collateral();
									col.setQuantity(inv.getQuantity());
									col.setSecurity(((Equity) inv.getProduct()).getIsin());
									col.setExchange(((Equity) inv.getProduct()).getExchange().getCode());
									col.setBook(inv.getBook().getName());
									availableCollateralValues.add(col);
								}
							}
						}
					}
				}

				this.trade = trade;
				refreshDonutModel();
				updateSecuryQuoteNames();
			}

		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(COL_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getCollateralValueDonutModel() {
		return collateralValueDonutModel;
	}

	public void setCollateralValueDonutModel(String collateralValueDonutModel) {
		this.collateralValueDonutModel = collateralValueDonutModel;
	}

	public String getSecurityToAdd() {
		return securityToAdd;
	}

	public void setSecurityToAdd(String securityToAdd) {
		this.securityToAdd = securityToAdd;
	}

	public String getFromBookToAdd() {
		return fromBookToAdd;
	}

	public void setFromBookToAdd(String fromBookToAdd) {
		this.fromBookToAdd = fromBookToAdd;
	}

	public BigDecimal getMaxQuantityToAdd() {
		return maxQuantityToAdd;
	}

	public void setMaxQuantityToAdd(BigDecimal maxQuantityToAdd) {
		this.maxQuantityToAdd = maxQuantityToAdd;
	}

	public BigDecimal getQuantityToAdd() {
		return quantityToAdd;
	}

	public void setQuantityToAdd(BigDecimal quantityToAdd) {
		this.quantityToAdd = quantityToAdd;
	}

	public String getExchangeToAdd() {
		return exchangeToAdd;
	}

	public void setExchangeToAdd(String exchangeToAdd) {
		this.exchangeToAdd = exchangeToAdd;
	}

	public GCRepoTrade getTrade() {
		return trade;
	}

	public void setTrade(GCRepoTrade trade) {
		this.trade = trade;
	}

	public List<Collateral> getRemovedCollateralValues() {
		return removedCollateralValues;
	}

	public void setRemovedCollateralValues(List<Collateral> removedCollateralValues) {
		this.removedCollateralValues = removedCollateralValues;
	}

	public void refreshDonutModel() {

		try {
			BigDecimal collateralValue = gcRepoPricerBusinessDelegate.getPendingCollateralValue(trade,
					getAddedSecurities(), getRemovedSecurities());
			BigDecimal cashValue = gcRepoPricerBusinessDelegate.getCurrentCashValue(trade);
			BigDecimal exposure = cashValue.subtract(collateralValue);

			collateralValue = collateralValue.max(BigDecimal.ZERO);
			exposure = exposure.max(BigDecimal.ZERO);

			List<Number> values = new ArrayList<>();
			values.add(collateralValue);
			values.add(exposure);

			List<RGBAColor> bgColors = new ArrayList<>();
			bgColors.add(ColorUtil.getTurquoise());
			bgColors.add(ColorUtil.getBloodRed());

			List<String> labels = new ArrayList<>();
			labels.add("Collateral value");
			labels.add("Uncovered exposure");

			collateralValueDonutModel = new DoughnutChart()
					.setData(new DoughnutData()
							.addDataset(new DoughnutDataset().setData(values)
									.addBackgroundColors((Object[]) bgColors.toArray(new RGBAColor[0])))
							.setLabels(labels))
					.setOptions(new DoughnutOptions().setMaintainAspectRatio(Boolean.FALSE)).toJson();

		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(COL_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	/**
	 * Returns the added securities. This method should be public as used in view's
	 * EL.
	 * 
	 * @return the added securities.
	 */
	public Map<Security, Map<Book, BigDecimal>> getAddedSecurities() {
		return collateralSetToSecuritiesMap(addedCollateralValues);
	}

	/**
	 * Returns the removed securities. This method should be public as used in
	 * view's EL.
	 * 
	 * @return the removed securities.
	 */
	public Map<Security, Map<Book, BigDecimal>> getRemovedSecurities() {
		return collateralSetToSecuritiesMap(removedCollateralValues);
	}

	private Map<Security, Map<Book, BigDecimal>> collateralSetToSecuritiesMap(List<Collateral> collateralSet) {
		if (collateralSet == null || collateralSet.isEmpty()) {
			return null;
		}
		Map<Security, Map<Book, BigDecimal>> securities = new HashMap<>();
		for (Collateral col : collateralSet) {
			Security security = null;
			Book book = null;
			try {
				security = securityBusinessDelegate.getSecurityByIsinAndExchangeCode(col.getSecurity(),
						col.getExchange());
				book = bookBusinessDelegate.getBookByName(col.getBook());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
			Map<Book, BigDecimal> bookMap = null;
			if (securities.containsKey(security)) {
				bookMap = securities.get(security);
			} else {
				bookMap = new HashMap<>();
			}
			bookMap.put(book, col.getQuantity());
			securities.put(security, bookMap);

		}
		return securities;
	}

	public String getExchangeToRemove() {
		return exchangeToRemove;
	}

	public Set<String> getSecurityQuoteNames() {
		return securityQuoteNames;
	}

	public void setSecurityQuoteNames(Set<String> securityQuoteNames) {
		this.securityQuoteNames = securityQuoteNames;
	}

	public void updateSecuryQuoteNames() {
		if (securityQuoteNames != null) {
			securityQuoteNames.clear();
		}
		if (!CollectionUtils.isEmpty(availableCollateralValues)) {
			securityQuoteNames = availableCollateralValues.stream()
					.map(c -> "%" + c.getSecurity() + "." + c.getExchange()).collect(Collectors.toSet());
		}
		if (!CollectionUtils.isEmpty(collateralValues)) {
			if (securityQuoteNames == null) {
				securityQuoteNames = new HashSet<>();
			}
			securityQuoteNames.addAll(collateralValues.stream().map(c -> "%" + c.getSecurity() + "." + c.getExchange())
					.collect(Collectors.toSet()));
		}
	}

}