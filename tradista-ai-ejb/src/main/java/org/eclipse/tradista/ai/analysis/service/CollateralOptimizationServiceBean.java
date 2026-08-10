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
package org.eclipse.tradista.ai.analysis.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.ai.analysis.prompt.PromptTemplateRegistry;
import org.eclipse.tradista.ai.reasoning.common.service.LocalConfigurationService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaults;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsBusinessDelegate;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.Coupon;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.ProcessingOrgDefaultsCollateralManagementModule;
import org.jboss.ejb3.annotation.SecurityDomain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class CollateralOptimizationServiceBean implements CollateralOptimizationService {

	@EJB
	private LocalConfigurationService localConfigurationService;

	@Override
	public Map<Security, BigDecimal> optimizeCollateral(GCRepoTrade trade, BigDecimal exposure,
			Map<Security, BigDecimal> availableQuantities, boolean considerBasel3LiquidityRatios,
			boolean excludeBondsPayingCoupons) throws TradistaBusinessException {

		ChatModel model = localConfigurationService.getChatModel();

		PromptTemplate promptTemplate = PromptTemplateRegistry.getCollateralOptimizationPromptTemplate();
		Map<String, Object> data = new HashMap<>();
		data.put("tradeDetails", formatTradeDetails(trade));
		data.put("exposure", exposure.toString());
		data.put("availableCollateralList", loadAndFormatAvailableCollateral(trade, availableQuantities));
		data.put("considerBasel3LiquidityRatios", String.valueOf(considerBasel3LiquidityRatios));
		data.put("excludeBondsPayingCoupons", String.valueOf(excludeBondsPayingCoupons));

		Prompt prompt = promptTemplate.apply(data);
		String response = model.chat(prompt.text());

		return parseLLMResponse(response, availableQuantities);
	}

	private String formatTradeDetails(GCRepoTrade trade) {
		return "GC Repo Trade ID: " + trade.getId() + "\n" + "Margin Rate: " + trade.getMarginRate() + "\n"
				+ "Settlement Date: " + trade.getSettlementDate() + "\n" + "End Date: " + trade.getEndDate();
	}

	private String loadAndFormatAvailableCollateral(GCRepoTrade trade, Map<Security, BigDecimal> availableQuantities)
			throws TradistaBusinessException {
		ProcessingOrgDefaultsBusinessDelegate poDefaultsBusinessDelegate = new ProcessingOrgDefaultsBusinessDelegate();
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();

		QuoteSet qs = null;
		if (trade != null && trade.getBook() != null && trade.getBook().getProcessingOrg() != null) {
			ProcessingOrgDefaults poDefaults = poDefaultsBusinessDelegate
					.getProcessingOrgDefaultsByPoId(trade.getBook().getProcessingOrg().getId());
			if (poDefaults != null) {
				ProcessingOrgDefaultsCollateralManagementModule module = (ProcessingOrgDefaultsCollateralManagementModule) poDefaults
						.getModuleByName(ProcessingOrgDefaultsCollateralManagementModule.COLLATERAL_MANAGEMENT);
				if (module != null) {
					qs = module.getQuoteSet();
				}
			}
		}

		if (qs == null) {
			throw new TradistaBusinessException(
					"The Collateral Quote Set for Processing Org Defaults of the trade's Processing Org has not been found.");
		}

		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		Map<Security, BigDecimal> prices = new HashMap<>();
		StringBuilder missingPrices = new StringBuilder();

		if (availableQuantities != null && !availableQuantities.isEmpty()) {
			for (Security sec : availableQuantities.keySet()) {
				String exchangeCode = sec.getExchange() != null ? sec.getExchange().getCode() : "";
				String quoteName = sec.getProductType() + "." + sec.getIsin() + "." + exchangeCode;
				QuoteType quoteType = sec.getProductType().equals(Bond.BOND) ? QuoteType.BOND_PRICE
						: QuoteType.EQUITY_PRICE;

				QuoteValue qv = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(qs.getId(),
						quoteName, quoteType, today);
				BigDecimal price = (qv != null) ? (qv.getClose() != null ? qv.getClose() : qv.getLast()) : null;

				if (price == null) {
					missingPrices.append(String.format(
							"Price '%s' (QuoteType: %s) on QuoteSet '%s' as of %s for security ISIN %s.%n", quoteName,
							quoteType, qs.getName(), today, sec.getIsin()));
				} else {
					prices.put(sec, price);
				}
			}
		}

		if (!missingPrices.isEmpty()) {
			throw new TradistaBusinessException(
					"Cannot optimize collateral allocation because the following security prices could not be found: "
							+ missingPrices.toString());
		}

		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISIN | Exchange | Type | Available Quantity | Unit Price | Total Market Value | Currency | Next Coupon Date\n");

		if (availableQuantities != null) {
			for (Map.Entry<Security, BigDecimal> entry : availableQuantities.entrySet()) {
				Security sec = entry.getKey();
				BigDecimal qty = entry.getValue();
				BigDecimal price = prices.get(sec);
				BigDecimal marketValue = (price != null && qty != null) ? price.multiply(qty) : null;

				LocalDate nextCouponDate = null;
				if (sec instanceof Bond bond && bond.getCoupons() != null) {
					for (Coupon c : bond.getCoupons()) {
						if (c.getDate() != null && !c.getDate().isBefore(today)) {
							if (nextCouponDate == null || c.getDate().isBefore(nextCouponDate)) {
								nextCouponDate = c.getDate();
							}
						}
					}
				}

				sb.append(sec.getIsin()).append(" | ")
						.append(sec.getExchange() != null ? sec.getExchange().getCode() : "N/A").append(" | ")
						.append(sec.getProductType()).append(" | ").append(qty != null ? qty.toString() : "0")
						.append(" | ").append(price != null ? price.toString() : "N/A").append(" | ")
						.append(marketValue != null ? marketValue.toString() : "N/A").append(" | ")
						.append(sec.getCurrency() != null ? sec.getCurrency().getIsoCode() : "N/A").append(" | ")
						.append(nextCouponDate != null ? nextCouponDate.toString() : "N/A").append("\n");
			}
		}
		return sb.toString();
	}

	private Map<Security, BigDecimal> parseLLMResponse(String response, Map<Security, BigDecimal> availableQuantities) {
		Map<Security, BigDecimal> allocation = new HashMap<>();
		try {
			int startIndex = response.indexOf("[");
			int endIndex = response.lastIndexOf("]");
			if (startIndex >= 0 && endIndex > startIndex) {
				String jsonString = response.substring(startIndex, endIndex + 1);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode arrayNode = mapper.readTree(jsonString);
				if (arrayNode.isArray()) {
					for (JsonNode node : arrayNode) {
						String isin = node.get("isin").asText();
						BigDecimal quantity = new BigDecimal(node.get("quantity").asText());

						for (Security sec : availableQuantities.keySet()) {
							if (sec.getIsin().equals(isin)) {
								allocation.put(sec, quantity);
								break;
							}
						}
					}
				}
			} else {
				throw new TradistaTechnicalException(
						"Could not find a JSON array in the LLM response. Response: " + response);
			}
		} catch (Exception e) {
			throw new TradistaTechnicalException("Error parsing LLM response: " + e.getMessage());
		}
		return allocation;
	}
}