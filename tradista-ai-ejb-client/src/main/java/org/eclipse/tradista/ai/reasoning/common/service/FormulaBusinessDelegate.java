package org.eclipse.tradista.ai.reasoning.common.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.ai.reasoning.common.model.Formula;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

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

public class FormulaBusinessDelegate {

	private FormulaService formulaService;

	public FormulaBusinessDelegate() {
		formulaService = TradistaServiceLocator.getInstance().getFormulaService();
	}

	public List<Formula> getAllFormulas() {
		return SecurityUtil.run(() -> formulaService.getAllFormulas());
	}

	public boolean saveFormulas(Formula... formulas) throws TradistaBusinessException {
		if (formulas != null && formulas.length > 0) {
			return SecurityUtil.runEx(() -> formulaService.saveFormulas(formulas));
		}
		return false;

	}

	public List<Formula> validateFormulas(Formula... formulas) throws TradistaBusinessException {
		if (formulas != null && formulas.length > 0) {
			return SecurityUtil.runEx(() -> formulaService.validateFormulas(formulas));
		}
		return null;

	}

	public boolean query(String functionName, String... parameters) throws TradistaBusinessException {
		if (StringUtils.isEmpty(functionName)) {
			throw new TradistaBusinessException("The function name is mandatory.");
		}
		return SecurityUtil.runEx(() -> formulaService.query(functionName, parameters));
	}

}