package org.eclipse.tradista.security.equity.service;

import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.validator.EquityValidator;

import jakarta.enterprise.inject.Default;

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

@Default
public class EquityBusinessDelegate {

	private EquityService equityService;

	private EquityValidator validator;

	public EquityBusinessDelegate() {
		equityService = TradistaServiceLocator.getInstance().getEquityService();
		validator = new EquityValidator();
	}

	public long saveEquity(Equity equity) throws TradistaBusinessException {
		validator.validateProduct(equity);
		return SecurityUtil.runEx(() -> equityService.saveEquity(equity));
	}

	public Set<Equity> getEquitiesByCreationDate(LocalDate date) {
		return SecurityUtil.run(() -> equityService.getEquitiesByCreationDate(date));
	}

	public Set<Equity> getAllEquities() {
		return SecurityUtil.run(() -> equityService.getAllEquities());
	}

	public Equity getEquityById(long id) {
		return SecurityUtil.run(() -> equityService.getEquityById(id));
	}

	public Set<Equity> getEquitiesByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minActiveDate,
			LocalDate maxActiveDate) throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (minCreationDate != null && maxCreationDate != null) {
			if (maxCreationDate.isBefore(minCreationDate)) {
				errorMsg.append(String.format("'To' creation date cannot be before 'From' creation date.%n"));
			}
		}
		if (minActiveDate != null && maxActiveDate != null) {
			if (maxActiveDate.isBefore(minActiveDate)) {
				errorMsg.append("'To' active date cannot be before 'From' active date.");
			}
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		return SecurityUtil.run(
				() -> equityService.getEquitiesByDates(minCreationDate, maxCreationDate, minActiveDate, maxActiveDate));
	}

	public Set<Equity> getEquitiesByIsin(String isin) {
		return SecurityUtil.run(() -> equityService.getEquitiesByIsin(isin));
	}

	public Equity getEquityByIsinAndExchangeCode(String isin, String exchangeCode) {
		return SecurityUtil.run(() -> equityService.getEquityByIsinAndExchangeCode(isin, exchangeCode));
	}

}