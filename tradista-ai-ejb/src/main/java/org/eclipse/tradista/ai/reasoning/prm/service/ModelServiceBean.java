package org.eclipse.tradista.ai.reasoning.prm.service;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.ai.reasoning.prm.model.Constant;
import org.eclipse.tradista.ai.reasoning.prm.model.Function;
import org.eclipse.tradista.ai.reasoning.prm.persistence.ConstantSQL;
import org.eclipse.tradista.ai.reasoning.prm.persistence.FunctionSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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
@Stateless
public class ModelServiceBean implements ModelService {

	@Override
	public long saveFunction(Function function) throws TradistaBusinessException {
		if (function.getId() == 0) {
			checkFunctionNameExistence(function);
			return FunctionSQL.saveFunction(function);
		} else {
			Function oldFunction = FunctionSQL.getFunctionById(function.getId());
			if (!function.getName().equals(oldFunction.getName())) {
				checkFunctionNameExistence(function);
			}
			return FunctionSQL.saveFunction(function);
		}
	}

	private void checkFunctionNameExistence(Function function) throws TradistaBusinessException {
		if (FunctionSQL.getFunctionByName(function.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("This function '%s' already exists in the model.", function.getName()));
		}
	}

	@Override
	public long saveConstant(Constant constant) throws TradistaBusinessException {
		if (constant.getId() == 0) {
			checkConstantNameAndTypeExistence(constant);
			return ConstantSQL.saveConstant(constant);
		} else {
			Constant oldConstant = ConstantSQL.getConstantById(constant.getId());
			if (!constant.getName().equals(oldConstant.getName())) {
				checkConstantNameAndTypeExistence(constant);
			}
			return ConstantSQL.saveConstant(constant);
		}
	}

	private void checkConstantNameAndTypeExistence(Constant constant) throws TradistaBusinessException {
		if (ConstantSQL.getConstantByNameAndType(constant.getName(), constant.getType()) != null) {
			throw new TradistaBusinessException(
					String.format("This constant '%s' with type '%s' already exists in the model.", constant.getName(),
							constant.getType()));
		}
	}

}