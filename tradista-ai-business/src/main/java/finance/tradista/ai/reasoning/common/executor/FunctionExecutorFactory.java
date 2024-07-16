package finance.tradista.ai.reasoning.common.executor;

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

import finance.tradista.ai.reasoning.common.model.FunctionExecutor;
import finance.tradista.ai.reasoning.common.model.NPVFXGTEFunction;
import finance.tradista.core.common.util.TradistaUtil;

public final class FunctionExecutorFactory {

	public static FunctionExecutor<?> getFunctionExecutor(String functionName) {
		return TradistaUtil.getInstance(FunctionExecutor.class, "finance.tradista.ai.reasoning.common.executor."
				+ NPVFXGTEFunction.NPV_FX_GTE.toUpperCase() + "FunctionExecutor");
	}
}