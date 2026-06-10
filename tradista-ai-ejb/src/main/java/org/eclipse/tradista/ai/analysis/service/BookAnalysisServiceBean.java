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
import java.util.Map;

import org.eclipse.tradista.ai.analysis.prompt.PromptTemplateRegistry;
import org.eclipse.tradista.ai.reasoning.common.service.LocalConfigurationService;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.service.CheckBookAccess;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.jboss.ejb3.annotation.SecurityDomain;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class BookAnalysisServiceBean implements BookAnalysisService {

	@EJB
	private LocalConfigurationService localConfigurationService;;

	private BookBusinessDelegate bookBusinessDelegate;

	@PostConstruct
	public void init() {
		bookBusinessDelegate = new BookBusinessDelegate();
	}

	@Override
	public String analyseBook(@CheckBookAccess Book book) throws TradistaBusinessException {
		Map<String, Map<String, BigDecimal>> bookContent = bookBusinessDelegate.getBookContent(book.getId());
		ChatModel model = localConfigurationService.getChatModel();

		String bookData = formatBookData(bookContent);

		PromptTemplate template = PromptTemplateRegistry.getBookAnalysisPromptTemplate();
		Map<String, Object> variables = Map.of("bookName", book.getName(), "bookData", bookData);

		Prompt prompt = template.apply(variables);

		return model.chat(prompt.text());
	}

	private String formatBookData(Map<String, Map<String, BigDecimal>> bookContent) {
		StringBuilder sb = new StringBuilder();
		bookContent.forEach((category, items) -> {
			if (!items.isEmpty()) {
				sb.append(category).append(":\n");
				items.forEach((key, value) -> sb.append("- ").append(key).append(": ").append(value).append("\n"));
				sb.append("\n");
			}
		});
		return sb.toString();
	}
}