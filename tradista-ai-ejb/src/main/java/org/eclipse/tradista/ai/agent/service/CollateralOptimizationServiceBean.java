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
package org.eclipse.tradista.ai.agent.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.ai.analysis.prompt.PromptTemplateRegistry;
import org.eclipse.tradista.ai.reasoning.common.service.LocalConfigurationService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.jboss.ejb3.annotation.SecurityDomain;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		data.put("availableCollateralList", formatAvailableCollateral(availableQuantities));
		data.put("considerBasel3LiquidityRatios", String.valueOf(considerBasel3LiquidityRatios));
		data.put("excludeBondsPayingCoupons", String.valueOf(excludeBondsPayingCoupons));
		
		Prompt prompt = promptTemplate.apply(data);
		String response = model.chat(prompt.text());
		
		return parseLLMResponse(response, availableQuantities);
	}

	private String formatTradeDetails(GCRepoTrade trade) {
		return "GC Repo Trade ID: " + trade.getId() + "\n" +
				"Margin Rate: " + trade.getMarginRate() + "\n" +
				"Settlement Date: " + trade.getSettlementDate() + "\n" +
				"End Date: " + trade.getEndDate();
	}

	private String formatAvailableCollateral(Map<Security, BigDecimal> availableQuantities) {
		StringBuilder sb = new StringBuilder();
		sb.append("ISIN | Available Quantity\n");
		for (Map.Entry<Security, BigDecimal> entry : availableQuantities.entrySet()) {
			sb.append(entry.getKey().getIsin()).append(" | ").append(entry.getValue()).append("\n");
		}
		return sb.toString();
	}

	private Map<Security, BigDecimal> parseLLMResponse(String response, Map<Security, BigDecimal> availableQuantities) throws TradistaBusinessException {
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
				throw new TradistaBusinessException("Could not find a JSON array in the LLM response. Response: " + response);
			}
		} catch (Exception e) {
			throw new TradistaBusinessException("Error parsing LLM response: " + e.getMessage(), e);
		}
		return allocation;
	}
}
