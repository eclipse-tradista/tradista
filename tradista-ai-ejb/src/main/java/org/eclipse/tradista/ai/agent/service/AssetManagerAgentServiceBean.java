package org.eclipse.tradista.ai.agent.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.ai.agent.model.Agent;
import org.eclipse.tradista.ai.agent.model.AssetManagerAgent;
import org.eclipse.tradista.ai.agent.persistence.AgentSQL;
import org.eclipse.tradista.ai.agent.persistence.AssetManagerAgentSQL;
import org.eclipse.tradista.ai.reasoning.common.executor.FunctionExecutorFactory;
import org.eclipse.tradista.ai.reasoning.common.model.Formula;
import org.eclipse.tradista.ai.reasoning.common.model.Function;
import org.eclipse.tradista.ai.reasoning.common.model.FunctionExecutor;
import org.eclipse.tradista.ai.reasoning.common.model.NPVFXGTEFunction;
import org.eclipse.tradista.ai.reasoning.common.service.FormulaService;
import org.eclipse.tradista.core.cashinventory.service.CashInventoryBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.inventory.model.CashInventory;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.fx.common.util.FXUtil;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
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
public class AssetManagerAgentServiceBean implements AssetManagerAgentService {

	@EJB
	private FormulaService formulaService;

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private CashInventoryBusinessDelegate cashInventoryBusinessDelegate;

	@PostConstruct
	public void init() {
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		cashInventoryBusinessDelegate = new CashInventoryBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public long saveAssetManagerAgent(AssetManagerAgent agent) throws TradistaBusinessException {
		if (agent.getId() == 0) {
			checkNameExistence(agent);
			return AssetManagerAgentSQL.saveAssetManagerAgent(agent);
		} else {
			Agent oldAgent = AgentSQL.getAgentById(agent.getId());
			if (!agent.getName().equals(oldAgent.getName())) {
				checkNameExistence(agent);
			}
			return AssetManagerAgentSQL.saveAssetManagerAgent(agent);
		}
	}

	private void checkNameExistence(AssetManagerAgent agent) throws TradistaBusinessException {
		if (AssetManagerAgentSQL.getAssetManagerAgentByName(agent.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("This asset manager agent '%s' already exists.", agent.getName()));
		}
	}

	@Override
	public Set<AssetManagerAgent> getAllStartedAssetManagerAgents() {
		return AssetManagerAgentSQL.getAllStartedAssetManagerAgents();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeMandate(AssetManagerAgent agent) throws TradistaBusinessException {

		Map<String, Boolean> buyFxs;

		// 1. If the mandate amount was not transferred, transfer it to the book
		// TODO

		// 2. Retrieve the currencies in position in the mandate's book
		Set<CashInventory> cashPosition = cashInventoryBusinessDelegate
				.getOpenPositionsFromCashInventoryByCurrencyAndBookIds(0, agent.getMandate().getBook().getId());

		if (cashPosition != null && !cashPosition.isEmpty()) {
			Set<Currency> currencies = HashSet.newHashSet(cashPosition.size());
			Map<String, Boolean> npvBooleans = new HashMap<>();
			Map<String, BigDecimal> npvs = new HashMap<>();
			for (CashInventory inv : cashPosition) {
				currencies.add(inv.getCurrency());
			}
			Set<Currency> authorizedCurrencies = HashSet.newHashSet(agent.getMandate().getCurrencyAllocations().size());
			for (String isoCode : agent.getMandate().getCurrencyAllocations().keySet()) {
				authorizedCurrencies.add(currencyBusinessDelegate.getCurrencyByIsoCode(isoCode));
			}
			// 3. Extract the formulas from the KB
			List<Formula> formulas = formulaService.getAllFormulas();
			// 4. Extract the methods with variable from the formula
			Set<Function<?>> functions = null;
			if (formulas != null && !formulas.isEmpty()) {
				for (Formula formula : formulas) {
					if (functions == null) {
						functions = new HashSet<>();
					}
					Set<Function<?>> fcts = formula.getFunctions();
					if (fcts != null && !fcts.isEmpty()) {
						functions.addAll(fcts);
					}
				}
			} else {
				throw new TradistaBusinessException("The knowledge base does not contain formulas");
			}
			// 5. for each possible currency, evaluate each of these methods
			// using a
			// mapping layer fol/tradista
			for (Currency c1 : authorizedCurrencies) {
				for (Currency c2 : currencies) {
					if (!c1.equals(c2)) {
						for (Function<?> f : functions) {
							if (f.getName().equals(NPVFXGTEFunction.NPV_FX_GTE)) {
								FunctionExecutor<Boolean> functionExecutor = (FunctionExecutor<Boolean>) FunctionExecutorFactory
										.getFunctionExecutor(NPVFXGTEFunction.NPV_FX_GTE);
								BigDecimal threshold = ((NPVFXGTEFunction) f).getThreshold();

								// Key is FX-Primary Currency-Quote Currency
								String npvFXKey = "FX-" + c1.getIsoCode() + "-" + c2.getIsoCode() + "-" + threshold;
								try {
									npvBooleans.put(npvFXKey, functionExecutor.execute(f, c1, c2,
											agent.getMandate().getInitialCashCurrency(),
											agent.getMandate().getEndDate(), agent.getPricingParameter(), threshold));
								} catch (TradistaBusinessException tbe) {
									// Log when npv calculation fails, but don't stop the process.
									tbe.printStackTrace();
								}
							}
						}
					}
				}
			}
			// Enrich the KB with the formulas result of the previous evaluation
			if (npvBooleans != null && !npvBooleans.isEmpty()) {
				Formula[] newFormulas = new Formula[npvBooleans.size()];
				int i = 0;
				for (Map.Entry<String, Boolean> entry : npvBooleans.entrySet()) {
					String stringThreshold = entry.getKey().split("-")[entry.getKey().split("-").length - 1]
							.replace(".", "dot");
					String neg = entry.getValue() ? "" : "-";
					newFormulas[i] = new Formula(neg + NPVFXGTEFunction.NPV_FX_GTE + "_" + stringThreshold + "("
							+ entry.getKey().substring(3, 6).toLowerCase() + ","
							+ entry.getKey().substring(7, 10).toLowerCase() + ",\""
							+ agent.getMandate().getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "\").");
					i++;
				}
				formulaService.saveFormulas(newFormulas);
			}

			// 6. Ask for achat(x,y) for each possible currencies
			buyFxs = HashMap.newHashMap(npvBooleans.size());
			for (Currency c1 : authorizedCurrencies) {
				for (Currency c2 : currencies) {
					if (!c1.equals(c2)) {
						boolean buy = formulaService.query("buyfx", c1.getIsoCode().toLowerCase(),
								c2.getIsoCode().toLowerCase(),
								"\"" + agent.getMandate().getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "\"");
						String npvFXKey = "FX-" + c1.getIsoCode() + "-" + c2.getIsoCode();
						buyFxs.put(npvFXKey, buy);
						if (buy) {
							npvs.put(npvFXKey, FXUtil.getNPV(c1, c2, agent.getMandate().getInitialCashCurrency(),
									agent.getMandate().getEndDate(), agent.getPricingParameter()));
						}
					}
				}
			}

			// 8. Sort the NPV map by descending values order
			Map<String, BigDecimal> sortedNPVs = npvs.entrySet().stream()
					.sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
							LinkedHashMap::new));

			// 9. Proceed with the buys, starting with the combination showing
			// the best npv,
			// respecting the min and max allocations
			Set<Currency> soldCurrencies = new HashSet<>();
			for (Map.Entry<String, BigDecimal> entry : sortedNPVs.entrySet()) {
				// Try to buy the maximum quantity, respecting the
				// allocations
				// Get the quote currency
				Currency quoteCurrency = currencyBusinessDelegate.getCurrencyByIsoCode(entry.getKey().substring(7, 10));
				// Get the primary currency
				Currency primaryCurrency = currencyBusinessDelegate
						.getCurrencyByIsoCode(entry.getKey().substring(3, 6));
				// If we already entered in a deal selling this currency, we
				// won't buy it
				if (soldCurrencies.contains(primaryCurrency)) {
					break;
				}
				// Get the cash position for the quote currency
				CashInventory quoteCurrencyPosition = null;
				// Get the cash position for the primary currency
				CashInventory primaryCurrencyPosition = null;
				for (CashInventory ci : cashPosition) {
					if (ci.getCurrency().equals(quoteCurrency)) {
						quoteCurrencyPosition = ci;
					}
					if (ci.getCurrency().equals(primaryCurrency)) {
						primaryCurrencyPosition = ci;
					}
					if (quoteCurrencyPosition != null && primaryCurrency != null) {
						break;
					}
				}
				FXTrade optimalTrade = getOptimalFXTrade(agent, quoteCurrency, primaryCurrency, quoteCurrencyPosition,
						primaryCurrencyPosition, cashPosition);
				if (optimalTrade != null) {
					if (agent.isOnlyInformative()) {
						// TODO See how to display optimalTrade Information
					} else {
						new FXTradeBusinessDelegate().saveFXTrade(optimalTrade);
					}
					soldCurrencies.add(optimalTrade.getCurrencyOne());
				}
			}
		}
	}

	private FXTrade getOptimalFXTrade(AssetManagerAgent agent, Currency quoteCurrency, Currency primaryCurrency,
			CashInventory quoteCurrencyPosition, CashInventory primaryCurrencyPosition,
			Set<CashInventory> cashInventories) throws TradistaBusinessException {
		FXTrade trade = new FXTrade();
		trade.setCurrencyOne(quoteCurrency);
		trade.setCurrency(primaryCurrency);
		// TODO Handle selection of counterparties.
		// Calculate total portfolio value in mandate currency
		BigDecimal totalValue = BigDecimal.ZERO;

		for (CashInventory inv : cashInventories) {
			totalValue.add(PricerUtil.convertAmount(inv.getAmount(), inv.getCurrency(),
					agent.getMandate().getInitialCashCurrency(), LocalDate.now(),
					agent.getPricingParameter().getQuoteSet().getId(), 0));
		}
		// get the max allocable amount of primary currency expressed in mandate
		// currency
		BigDecimal primaryCurrencyMaxAllocation = totalValue
				.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode())
				.multiply(BigDecimal.valueOf(agent.getMandate().getCurrencyAllocations()
						.get(primaryCurrency.getIsoCode()).getMaxAllocation()));

		// Previous calculated amount - primary amount already in portfolio
		// -->
		// this is the optimal value to bought
		BigDecimal primaryCurrencyOptimalAmount = primaryCurrencyMaxAllocation
				.subtract(PricerUtil.convertAmount(primaryCurrencyPosition.getAmount(),
						primaryCurrencyPosition.getCurrency(), agent.getMandate().getInitialCashCurrency(),
						LocalDate.now(), agent.getPricingParameter().getQuoteSet().getId(), 0));

		// if it is not possible to buy more primary currency, return null
		if (primaryCurrencyOptimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
			// TODO log
			return null;
		}

		// Min quote currency allocable amount expressed in mandate currency
		BigDecimal quoteCurrencyMinAllocation = totalValue
				.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode())
				.multiply(BigDecimal.valueOf(agent.getMandate().getCurrencyAllocations().get(quoteCurrency.getIsoCode())
						.getMinAllocation()));

		// Current quote currency position - previous calculated value is
		// the max amount to be sold
		BigDecimal quoteCurrencyMaxAmount = quoteCurrencyPosition.getAmount()
				.subtract(PricerUtil.convertAmount(quoteCurrencyMinAllocation, quoteCurrencyPosition.getCurrency(),
						quoteCurrency, LocalDate.now(), agent.getPricingParameter().getQuoteSet().getId(), 0));

		// if it is not possible to sell more quote currency, return null
		if (quoteCurrencyMaxAmount.compareTo(BigDecimal.ZERO) <= 0) {
			// TODO log
			return null;
		}

		// Primary currency optimal amount converted in quote currency
		BigDecimal quoteCurrencyOptimalAmount = PricerUtil.convertAmount(primaryCurrencyOptimalAmount,
				agent.getMandate().getInitialCashCurrency(), quoteCurrency, LocalDate.now(),
				agent.getPricingParameter().getQuoteSet().getId(), 0);

		BigDecimal quoteCurrencyAmount = BigDecimal
				.valueOf(Math.min(quoteCurrencyOptimalAmount.doubleValue(), quoteCurrencyMaxAmount.doubleValue()));

		if (quoteCurrencyOptimalAmount.compareTo(quoteCurrencyAmount) != 0) {
			// primary amount must be decreased because min allocation of quote currency has
			// been reached.
			primaryCurrencyOptimalAmount = PricerUtil.convertAmount(quoteCurrencyAmount, quoteCurrency, primaryCurrency,
					LocalDate.now(), agent.getPricingParameter().getQuoteSet().getId(), 0);
		} else {
			primaryCurrencyOptimalAmount = PricerUtil.convertAmount(primaryCurrencyOptimalAmount,
					agent.getMandate().getInitialCashCurrency(), primaryCurrency, LocalDate.now(),
					agent.getPricingParameter().getQuoteSet().getId(), 0);
		}
		trade.setAmount(primaryCurrencyOptimalAmount);
		trade.setAmountOne(quoteCurrencyAmount);
		trade.setTradeDate(LocalDate.now());
		trade.setBook(agent.getMandate().getBook());
		trade.setBuySell(true);
		trade.setSettlementDate(agent.getMandate().getEndDate());

		return trade;
	}

}