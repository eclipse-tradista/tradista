package org.eclipse.tradista.security.bond.service;

import java.time.LocalDate;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.persistence.BondSQL;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityService;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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
public class BondServiceBean implements BondService {

	@EJB
	private EquityService equityService;

	@Override
	@Interceptors(BondProductScopeFilteringInterceptor.class)
	public long saveBond(Bond bond) throws TradistaBusinessException {
		if (bond.getId() == 0) {
			checkIsinExistence(bond);
			return BondSQL.saveBond(bond);
		} else {
			Bond oldBond = BondSQL.getBondById(bond.getId());
			if (!bond.getIsin().equals(oldBond.getIsin())) {
				checkIsinExistence(bond);
			}
			return BondSQL.saveBond(bond);
		}
	}

	private void checkIsinExistence(Bond bond) throws TradistaBusinessException {
		if (getBondByIsinAndExchangeCode(bond.getIsin(), bond.getExchange().getCode()) != null) {
			throw new TradistaBusinessException(String.format("This bond '%s' already exists in the exchange %s.",
					bond.getIsin(), bond.getExchange().getCode()));
		} else {
			Set<Equity> equities = equityService.getEquitiesByIsin(bond.getIsin());
			if (equities != null && !equities.isEmpty()) {
				throw new TradistaBusinessException(
						String.format("There is already an equity with the same ISIN %s.", bond.getIsin()));
			}
		}
	}

	@Override
	public Set<Bond> getBondsByCreationDate(LocalDate date) {
		return BondSQL.getBondsByCreationDate(date);
	}

	@Override
	public Set<Bond> getAllBonds() {
		return BondSQL.getAllBonds();
	}

	@Override
	public Bond getBondById(long id) {
		return BondSQL.getBondById(id);
	}

	@Override
	public Set<Bond> getBondsByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minMaturityDate,
			LocalDate maxMaturityDate) {
		return BondSQL.getBondsByDates(minCreationDate, maxCreationDate, minMaturityDate, maxMaturityDate);
	}

	@Override
	public Set<Bond> getBondsByIsin(String isin) {
		return BondSQL.getBondsByIsin(isin);
	}

	@Override
	public Bond getBondByIsinAndExchangeCode(String isin, String exchangeCode) {
		return BondSQL.getBondByIsinAndExchangeCode(isin, exchangeCode);
	}

}