package org.eclipse.tradista.security.equityoption.service;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;

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

public class EquityOptionContractSpecificationBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -1793657842553613816L;

	private EquityOptionContractSpecificationService equityOptionContractSpecificationService;

	public EquityOptionContractSpecificationBusinessDelegate() {
		equityOptionContractSpecificationService = TradistaServiceLocator.getInstance()
				.getEquityOptionSpecificationService();
	}

	public long saveEquityOptionContractSpecification(EquityOptionContractSpecification eos)
			throws TradistaBusinessException {
		validateEquityOptionContractSpecification(eos);
		return SecurityUtil
				.runEx(() -> equityOptionContractSpecificationService.saveEquityOptionContractSpecification(eos));
	}

	public void validateEquityOptionContractSpecification(EquityOptionContractSpecification eos)
			throws TradistaBusinessException {
		if (eos == null) {
			throw new TradistaBusinessException("The Equity Option Contract Specification cannot be null.");
		}

		StringBuilder errMsg = new StringBuilder();

		if (StringUtils.isEmpty(eos.getName())) {
			errMsg.append("The name is mandatory.\n");
		}

		if (eos.getExchange() == null) {
			errMsg.append("The exchange is mandatory.\n");
		}

		if (eos.getMaturityDatesDateRule() == null) {
			errMsg.append("The maturity dates date rule is mandatory.\n");
		}

		if (eos.getQuantity() == null) {
			errMsg.append("The quantity is mandatory.\n");
		} else {
			if (eos.getQuantity().doubleValue() <= 0) {
				errMsg.append("The quantity must be positive.\n");
			}
		}

		if (eos.getMultiplier() == null) {
			errMsg.append("The multiplier is mandatory.\n");
		} else {
			if (eos.getMultiplier().doubleValue() <= 0) {
				errMsg.append("The multiplier must be positive.\n");
			}
		}

		if (eos.getPremiumCurrency() == null) {
			errMsg.append("The premium currency is mandatory.\n");
		}

		if (eos.getSettlementType() == null) {
			errMsg.append("The settlement type is mandatory.\n");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

	public Set<EquityOptionContractSpecification> getAllEquityOptionContractSpecifications() {
		return SecurityUtil
				.run(() -> equityOptionContractSpecificationService.getAllEquityOptionContractSpecifications());
	}

	public EquityOptionContractSpecification getEquityOptionContractSpecificationById(long id) {
		return SecurityUtil
				.run(() -> equityOptionContractSpecificationService.getEquityOptionContractSpecificationById(id));
	}

	public EquityOptionContractSpecification getEquityOptionContractSpecificationByName(String name) {
		return SecurityUtil
				.run(() -> equityOptionContractSpecificationService.getEquityOptionContractSpecificationByName(name));
	}

}