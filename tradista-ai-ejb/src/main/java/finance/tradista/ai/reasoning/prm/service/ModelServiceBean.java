package finance.tradista.ai.reasoning.prm.service;

import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.ai.reasoning.prm.model.Constant;
import finance.tradista.ai.reasoning.prm.model.Function;
import finance.tradista.ai.reasoning.prm.persistence.ConstantSQL;
import finance.tradista.ai.reasoning.prm.persistence.FunctionSQL;
import finance.tradista.core.common.exception.TradistaBusinessException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/*
 * Copyright 2019 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

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
			throw new TradistaBusinessException(String.format(
					"This function '%s' already exists in the model.",
					function.getName()));
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
			throw new TradistaBusinessException(String.format(
					"This constant '%s' with type '%s' already exists in the model.",
					constant.getName(), constant.getType()));
		} 
	}

}