package org.eclipse.tradista.ai.reasoning.common.service;

import java.util.List;

import org.eclipse.tradista.ai.reasoning.common.model.Formula;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

import jakarta.ejb.Remote;

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

@Remote
public interface FormulaService {

	long saveFormula(Formula formula) throws TradistaBusinessException;

	List<Formula> getAllFormulas();

	boolean saveFormulas(Formula... formulas) throws TradistaBusinessException;

	boolean query(String functionName, String... parameters) throws TradistaBusinessException;

	List<Formula> validateFormulas(Formula... formulas) throws TradistaBusinessException;

}
