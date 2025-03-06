package org.eclipse.tradista.ai.reasoning.common.executor;

import org.apache.commons.lang3.StringUtils;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import org.eclipse.tradista.ai.reasoning.common.model.FunctionExecutor;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.TradistaUtil;

public final class FunctionExecutorFactory {

	private FunctionExecutorFactory() {
	}

	public static FunctionExecutor<?> getFunctionExecutor(String functionName) {
		if (StringUtils.isEmpty(functionName)) {
			throw new TradistaTechnicalException("Function name is mandatory.");
		}
		return TradistaUtil.getInstance(FunctionExecutor.class,
				"org.eclipse.tradista.ai.reasoning.common.executor." + functionName.toUpperCase() + "FunctionExecutor");
	}
}