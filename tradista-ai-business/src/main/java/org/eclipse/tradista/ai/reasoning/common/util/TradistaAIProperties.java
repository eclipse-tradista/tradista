package org.eclipse.tradista.ai.reasoning.common.util;

/********************************************************************************
 * Copyright (c) 2021 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import java.util.Properties;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

public final class TradistaAIProperties {

	private static String llmProvider;
	private static String llmUrl;
	private static String llmModel;
	private static String solverPath;

	private TradistaAIProperties() {
	}

	public static void loadSolver(Properties prop) {
		if (prop == null) {
			throw new TradistaTechnicalException("The properties cannot be null.");
		}
		if (prop.containsKey("solver.path")) {
			solverPath = prop.getProperty("solver.path");
		}
	}

	public static void loadLlm(Properties prop) {
		if (prop == null) {
			throw new TradistaTechnicalException("The properties cannot be null.");
		}
		llmProvider = prop.getProperty("llm.provider");
		llmUrl = prop.getProperty("llm.url");
		llmModel = prop.getProperty("llm.model");
	}

	public static String getSolverPath() {
		return solverPath;
	}

	public static String getLlmProvider() {
		return llmProvider;
	}

	public static String getLlmUrl() {
		return llmUrl;
	}

	public static String getLlmModel() {
		return llmModel;
	}

}