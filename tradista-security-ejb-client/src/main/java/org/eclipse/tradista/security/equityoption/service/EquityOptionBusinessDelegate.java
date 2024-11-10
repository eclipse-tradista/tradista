package org.eclipse.tradista.security.equityoption.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.validator.EquityOptionValidator;

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

public class EquityOptionBusinessDelegate implements Serializable {

	private static final long serialVersionUID = 5129022391357676738L;
	private EquityOptionService equityOptionService;

	private EquityOptionValidator validator;

	public EquityOptionBusinessDelegate() {
		equityOptionService = TradistaServiceLocator.getInstance().getEquityOptionService();
		validator = new EquityOptionValidator();
	}

	public long saveEquityOption(EquityOption product) throws TradistaBusinessException {
		validator.validateProduct(product);
		return SecurityUtil.runEx(() -> equityOptionService.saveEquityOption(product));
	}

	public Set<EquityOption> getEquityOptionsByCreationDate(LocalDate date) {
		return SecurityUtil.run(() -> equityOptionService.getEquityOptionsByCreationDate(date));
	}

	public Set<EquityOption> getEquityOptionsByCreationDate(LocalDate minDate, LocalDate maxDate)
			throws TradistaBusinessException {
		if (minDate != null && maxDate != null) {
			if (maxDate.isBefore(minDate)) {
				throw new TradistaBusinessException("'To' creation date cannot be before 'From' creation date.");
			}
		}
		return SecurityUtil.run(() -> equityOptionService.getEquityOptionsByCreationDate(minDate, maxDate));
	}

	public Set<EquityOption> getAllEquityOptions() {
		return SecurityUtil.run(() -> equityOptionService.getAllEquityOptions());
	}

	public EquityOption getEquityOptionById(long id) {
		return SecurityUtil.run(() -> equityOptionService.getEquityOptionById(id));
	}

	public EquityOption getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(String code,
			OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate, String contractSpecificationName)
			throws TradistaBusinessException {
		if (code == null) {
			throw new TradistaBusinessException("The code is mandatory.");
		}
		if (type == null) {
			throw new TradistaBusinessException("The type is mandatory.");
		}
		if (contractSpecificationName == null) {
			throw new TradistaBusinessException("The contract specification name is mandatory.");
		}
		if (maturityDate == null) {
			throw new TradistaBusinessException("The maturity date code is mandatory.");
		}
		if (strike == null) {
			throw new TradistaBusinessException("The strike is mandatory.");
		}
		return SecurityUtil.run(
				() -> equityOptionService.getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(code,
						type, strike, maturityDate, contractSpecificationName));
	}

	public Set<EquityOption> getEquityOptionsByCode(String code) throws TradistaBusinessException {
		if (code == null) {
			throw new TradistaBusinessException("The code is mandatory.");
		}
		return SecurityUtil.run(() -> equityOptionService.getEquityOptionsByCode(code));
	}

}