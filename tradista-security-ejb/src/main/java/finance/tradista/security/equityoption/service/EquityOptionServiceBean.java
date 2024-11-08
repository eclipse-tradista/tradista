package org.eclipse.tradista.security.equityoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.persistence.EquityOptionSQL;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
public class EquityOptionServiceBean implements EquityOptionService {

	@Override
	@Interceptors(EquityOptionProductScopeFilteringInterceptor.class)
	public long saveEquityOption(EquityOption equityOption) throws TradistaBusinessException {
		if (equityOption.getId() == 0) {
			checkProductExistence(equityOption);
			return EquityOptionSQL.saveEquityOption(equityOption);
		} else {
			EquityOption oldEquityOption = EquityOptionSQL.getEquityOptionById(equityOption.getId());
			if (!oldEquityOption.getCode().equals(equityOption.getCode())
					|| !oldEquityOption.getType().equals(equityOption.getType())
					|| !oldEquityOption.getEquityOptionContractSpecification()
							.equals(equityOption.getEquityOptionContractSpecification())
					|| !oldEquityOption.getMaturityDate().equals(equityOption.getMaturityDate())
					|| (oldEquityOption.getStrike().compareTo(equityOption.getStrike()) != 0)) {
				checkProductExistence(equityOption);
			}
			return EquityOptionSQL.saveEquityOption(equityOption);
		}
	}

	private void checkProductExistence(EquityOption equityOption) throws TradistaBusinessException {
		if (getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(equityOption.getCode(),
				equityOption.getType(), equityOption.getStrike(), equityOption.getMaturityDate(),
				equityOption.getEquityOptionContractSpecification().getName()) != null) {
			throw new TradistaBusinessException(String.format(
					"This equity option '%s' with type %s, strike %s maturity date %s and contract specification %s already exists in the system.",
					equityOption.getCode(), equityOption.getType(), equityOption.getStrike(),
					equityOption.getMaturityDate(), equityOption.getEquityOptionContractSpecification().getName()));
		}
	}

	@Override
	public Set<EquityOption> getEquityOptionsByCreationDate(LocalDate date) {
		return EquityOptionSQL.getEquityOptionsByCreationDate(date);
	}

	@Override
	public Set<EquityOption> getEquityOptionsByCreationDate(LocalDate minDate, LocalDate maxDate) {
		return EquityOptionSQL.getEquityOptionsByCreationDate(minDate, maxDate);
	}

	@Override
	public Set<EquityOption> getAllEquityOptions() {
		return EquityOptionSQL.getAllEquityOptions();
	}

	@Override
	public EquityOption getEquityOptionById(long id) {
		return EquityOptionSQL.getEquityOptionById(id);
	}

	@Override
	public Set<EquityOption> getEquityOptionsByCode(String code) {
		return EquityOptionSQL.getEquityOptionsByCode(code);
	}

	@Override
	public EquityOption getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(String code,
			OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate, String contractSpecificationName) {
		return EquityOptionSQL.getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(code, type,
				strike, maturityDate, contractSpecificationName);
	}

}