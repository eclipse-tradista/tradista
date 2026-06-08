package org.eclipse.tradista.ai.reasoning.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.ai.reasoning.common.util.TradistaAIProperties;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.jboss.ejb3.annotation.SecurityDomain;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

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
@Startup
@Singleton
public class AIConfigurationServiceBean implements LocalConfigurationService {

	@PostConstruct
	public void init() {
		Properties properties = new Properties();
		InputStream in = TradistaAIProperties.class.getResourceAsStream("/META-INF/solver.properties");
		if (in != null) {
			try {
				properties.load(in);
				in.close();
				TradistaAIProperties.loadSolver(properties);
			} catch (IOException _) {
				// should not happen here.
			}
		}

		Properties llmProps = new Properties();
		InputStream llmIn = TradistaAIProperties.class.getResourceAsStream("/META-INF/llm.properties");
		if (llmIn != null) {
			try {
				llmProps.load(llmIn);
				llmIn.close();
				TradistaAIProperties.loadLlm(llmProps);
			} catch (IOException _) {
				// should not happen here.
			}
		}
	}

	@Override
	public String getSolverPath() {
		return TradistaAIProperties.getSolverPath();
	}

	@Override
	public ChatModel getChatModel() {
		final String OLLAMA = "ollama";
		String provider = TradistaAIProperties.getLlmProvider();
		if (StringUtils.isBlank(provider)) {
			throw new TradistaTechnicalException("The LLM Provider is mandatory.");
		}
		if (TradistaAIProperties.getLlmProvider().equals(OLLAMA)) {
			final String DEFAULT_OLLAMA_URL = "http://localhost:11434";
			final String DEFAULT_OLLAMA_MODEL_NAME = "llama3";
			String url = TradistaAIProperties.getLlmUrl();
			String modelName = TradistaAIProperties.getLlmModel();
			return OllamaChatModel.builder().baseUrl(url != null ? url : DEFAULT_OLLAMA_URL)
					.modelName(modelName != null ? modelName : DEFAULT_OLLAMA_MODEL_NAME).build();
		} else {
			throw new TradistaTechnicalException(
					String.format("Unsupported LLM Provider: %s", TradistaAIProperties.getLlmProvider()));
		}
	}
}