package org.eclipse.tradista.security.bond.service;

import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.validator.BondValidator;

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

public class BondBusinessDelegate {

	private BondService bondService;

	private BondValidator validator;

	public BondBusinessDelegate() {
		bondService = TradistaServiceLocator.getInstance().getBondService();
		validator = new BondValidator();
	}

	public long saveBond(Bond bond) throws TradistaBusinessException {
		validator.validateProduct(bond);
		return SecurityUtil.runEx(() -> bondService.saveBond(bond));
	}

	public Set<Bond> getBondsByCreationDate(LocalDate date) {
		return SecurityUtil.run(() -> bondService.getBondsByCreationDate(date));
	}

	public Set<Bond> getAllBonds() {
		return SecurityUtil.run(() -> bondService.getAllBonds());
	}

	public Bond getBondById(long id) {
		return SecurityUtil.run(() -> bondService.getBondById(id));
	}

	public Set<Bond> getBondsByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minMaturityDate,
			LocalDate maxMaturityDate) throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (minCreationDate != null && maxCreationDate != null) {
			if (maxCreationDate.isBefore(minCreationDate)) {
				errorMsg.append(String.format("'To' creation date cannot be before 'From' creation date.%n"));
			}
		}
		if (minMaturityDate != null && maxMaturityDate != null) {
			if (maxMaturityDate.isBefore(minMaturityDate)) {
				errorMsg.append("'To' maturity date cannot be before 'From' maturity date.");
			}
		}
		if (!errorMsg.isEmpty()) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		return SecurityUtil.run(
				() -> bondService.getBondsByDates(minCreationDate, maxCreationDate, minMaturityDate, maxMaturityDate));
	}

	public Set<Bond> getBondsByIsin(String isin) {
		return SecurityUtil.run(() -> bondService.getBondsByIsin(isin));
	}

	public Bond getBondByIsinAndExchangeCode(String isin, String exchangeCode) {
		return SecurityUtil.run(() -> bondService.getBondByIsinAndExchangeCode(isin, exchangeCode));
	}

}