package org.eclipse.tradista.ai.reasoning.fol.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.ai.reasoning.fol.model.FolFormula;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;

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

public class FolFormulaBusinessDelegate {

	private FolFormulaService folFormulaService;

	public FolFormulaBusinessDelegate() {
		folFormulaService = TradistaServiceLocator.getInstance().getFolFormulaService();
	}

	public List<FolFormula> getAllFolFormulas() {
		return folFormulaService.getAllFolFormulas();
	}

	public boolean saveFolFormulas(FolFormula... formulas) throws TradistaBusinessException {
		if (formulas != null && formulas.length > 0) {
			return folFormulaService.saveFolFormulas(formulas);
		}
		return false;

	}

	public List<FolFormula> validateFolFormulas(FolFormula... formulas) throws TradistaBusinessException {
		if (formulas != null && formulas.length > 0) {
			return folFormulaService.validateFolFormulas(formulas);
		}
		return null;

	}

	public boolean query(String functionName, String... parameters) throws TradistaBusinessException {
		if (StringUtils.isEmpty(functionName)) {
			throw new TradistaBusinessException("The function name is mandatory.");
		}
		return folFormulaService.query(functionName, parameters);
	}

}