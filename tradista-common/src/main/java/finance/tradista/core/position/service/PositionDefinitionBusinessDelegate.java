package finance.tradista.core.position.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.servicelocator.TradistaServiceLocator;
import finance.tradista.core.common.util.SecurityUtil;
import finance.tradista.core.position.model.PositionDefinition;
import finance.tradista.core.position.service.PositionDefinitionService;
import finance.tradista.core.product.service.ProductBusinessDelegate;

/*
 * Copyright 2018 Olivier Asuncion
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

public class PositionDefinitionBusinessDelegate {

	private PositionDefinitionService positionDefinitionService;

	public PositionDefinitionBusinessDelegate() {
		positionDefinitionService = TradistaServiceLocator.getInstance().getPositionDefinitionService();
	}

	public PositionDefinition getPositionDefinitionByName(String name) throws TradistaBusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new TradistaBusinessException("The name is mandatory.");
		}
		return SecurityUtil.run(() -> positionDefinitionService.getPositionDefinitionByName(name));
	}

	public PositionDefinition getPositionDefinitionById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The id must be positive.");
		}
		return SecurityUtil.run(() -> positionDefinitionService.getPositionDefinitionById(id));
	}

	public Set<PositionDefinition> getAllPositionDefinitions() {
		return SecurityUtil.run(() -> positionDefinitionService.getAllPositionDefinitions());
	}

	public long savePositionDefinition(PositionDefinition positionDefinition) throws TradistaBusinessException {
		checkPositionDefinition(positionDefinition);
		return SecurityUtil.runEx(() -> positionDefinitionService.savePositionDefinition(positionDefinition));
	}

	public boolean deletePositionDefinition(String name) throws TradistaBusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new TradistaBusinessException("The name is mandatory.");
		}
		return SecurityUtil.run(() -> positionDefinitionService.deletePositionDefinition(name));
	}

	private void checkPositionDefinition(PositionDefinition positionDefinition) throws TradistaBusinessException {
		if (positionDefinition == null) {
			throw new TradistaBusinessException("The position definition cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (positionDefinition.getBook() == null) {
			errMsg.append(String.format("The book is mandatory.%n"));
		}
		if (positionDefinition.getCurrency() == null) {
			errMsg.append(String.format("The currency is mandatory.%n"));
		}
		if (StringUtils.isEmpty(positionDefinition.getName())) {
			errMsg.append(String.format("The name is mandatory.%n"));
		}
		if (positionDefinition.getPricingParameter() == null) {
			errMsg.append(String.format("The pricing parameter set is mandatory.%n"));
		}
		if (positionDefinition.getProcessingOrg() == null) {
			errMsg.append(String.format("The processing org is mandatory.%n"));
		}
		if (positionDefinition.getProcessingOrg() != null && positionDefinition.getBook() != null
				&& positionDefinition.getBook().getProcessingOrg() != null
				&& !positionDefinition.getBook().getProcessingOrg().equals(positionDefinition.getProcessingOrg())) {
			errMsg.append(String.format("The Position Definition and Book processing orgs must be the same.%n"));
		}
		if (positionDefinition.getProcessingOrg() != null && positionDefinition.getPricingParameter() != null
				&& positionDefinition.getPricingParameter().getProcessingOrg() != null && !positionDefinition
						.getPricingParameter().getProcessingOrg().equals(positionDefinition.getProcessingOrg())) {
			errMsg.append(String
					.format("The Position Definition and Pricing Parameters Set processing orgs must be the same.%n"));
		}
		if (!StringUtils.isEmpty(positionDefinition.getProductType())) {
			if (new ProductBusinessDelegate().canBeListed(positionDefinition.getProductType())
					&& positionDefinition.getCounterparty() != null) {
				errMsg.append(String.format(
						"It is not possible to define a position on listed products for a specific counterparty.%n"));
			}
		}
		if (positionDefinition.getProduct() != null) {
			if (positionDefinition.getProductType() != null) {
				if (!positionDefinition.getProduct().getProductType().equals(positionDefinition.getProductType()))
					errMsg.append(String.format("The product %s should have the % product type.%n",
							positionDefinition.getProduct(), positionDefinition.getProductType()));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	public Set<PositionDefinition> getAllRealTimePositionDefinitions() {
		return SecurityUtil.run(() -> positionDefinitionService.getAllRealTimePositionDefinitions());
	}

	public Set<String> getPositionDefinitionsByPricingParametersSetId(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The id is mandatory.");
		}
		return SecurityUtil.run(() -> positionDefinitionService.getPositionDefinitionsByPricingParametersSetId(id));
	}

}