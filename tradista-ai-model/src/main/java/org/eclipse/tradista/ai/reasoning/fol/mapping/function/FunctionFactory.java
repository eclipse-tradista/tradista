package org.eclipse.tradista.ai.reasoning.fol.mapping.function;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.ai.reasoning.common.model.Function;
import org.eclipse.tradista.ai.reasoning.common.model.NPVFXGTEFunction;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public final class FunctionFactory {

	public static Function<?> createFunction(String functionName) throws TradistaBusinessException {
		if (StringUtils.isBlank(functionName)) {
			throw new TradistaBusinessException("Error: trying to create a function from a null or empty name.");
		}
		if (functionName.startsWith(NPVFXGTEFunction.NPV_FX_GTE)) {
			String stringThreshold = functionName.split("_")[functionName.split("_").length - 1].replace("dot", ".");
			BigDecimal threshold = new BigDecimal(stringThreshold);
			return new NPVFXGTEFunction(threshold);
		}
		return null;
	}

}
