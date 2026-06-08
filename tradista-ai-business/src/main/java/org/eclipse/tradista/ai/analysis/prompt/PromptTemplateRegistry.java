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
package org.eclipse.tradista.ai.analysis.prompt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tradista.core.common.util.TradistaUtil;

import dev.langchain4j.model.input.PromptTemplate;

public final class PromptTemplateRegistry {

	private static Map<String, PromptTemplate> promptTemplateCache = new ConcurrentHashMap<>();

	private PromptTemplateRegistry() {
	}

	public static PromptTemplate getBookAnalysisPromptTemplate() {
		final String BOOK_ANALYSIS_KEY = "BookAnalysis";
		promptTemplateCache.putIfAbsent(BOOK_ANALYSIS_KEY,
				PromptTemplate.from(TradistaUtil.loadResourceAsString("prompts/bookAnalysis.prompt")));
		return promptTemplateCache.get(BOOK_ANALYSIS_KEY);
	}

}