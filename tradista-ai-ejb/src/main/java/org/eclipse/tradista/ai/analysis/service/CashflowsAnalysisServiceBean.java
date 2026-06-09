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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.ai.analysis.prompt.PromptTemplateRegistry;
import org.eclipse.tradista.ai.reasoning.common.service.LocalConfigurationService;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.util.CollectionUtils;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class CashflowsAnalysisServiceBean implements CashflowsAnalysisService {

	@EJB
	private LocalConfigurationService localConfigurationService;

	@Override
	public String analyseCashflows(List<CashFlow> tMinusOneCashflows, List<CashFlow> tCashflows)
			throws TradistaBusinessException {
		ChatModel model = localConfigurationService.getChatModel();
		PromptTemplate promptTemplate = PromptTemplateRegistry.getCashflowsAnalysisPromptTemplate();
		Map<String, Object> data = new HashMap<>();
		data.put("tMinusOneCashflowsData", formatCashflowsData(tMinusOneCashflows));
		data.put("tCashflowsData", formatCashflowsData(tCashflows));
		Prompt prompt = promptTemplate.apply(data);

		return model.chat(prompt.text());
	}

	private String formatCashflowsData(List<CashFlow> cashflows) {
		if (CollectionUtils.isEmpty(cashflows)) {
			return "No cashflows generated.";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Date | Direction | Purpose | Amount | Discounted Amount | Discount Factor | Currency\n");
		sb.append("--------------------------------------------------------------------------------------\n");

		for (CashFlow cf : cashflows) {
			sb.append(String.format("%s | %s | %s | %s | %s | %s | %s\n", cf.getDate(), cf.getDirection(),
					cf.getPurpose(), cf.getAmount(), cf.getDiscountedAmount(), cf.getDiscountFactor(),
					cf.getCurrency()));
		}
		return sb.toString();
	}
}
