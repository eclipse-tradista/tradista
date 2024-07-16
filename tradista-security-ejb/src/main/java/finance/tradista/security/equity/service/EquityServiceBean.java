package finance.tradista.security.equity.service;

import java.time.LocalDate;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.security.bond.model.Bond;
import finance.tradista.security.bond.service.BondService;
import finance.tradista.security.equity.model.Equity;
import finance.tradista.security.equity.persistence.EquitySQL;

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
public class EquityServiceBean implements EquityService {

	@EJB
	private BondService bondService;

	@Override
	@Interceptors(EquityProductScopeFilteringInterceptor.class)
	public long saveEquity(Equity equity) throws TradistaBusinessException {
		if (equity.getId() == 0) {
			checkProductExistence(equity);
			return EquitySQL.saveEquity(equity);
		} else {
			Equity oldEquity = EquitySQL.getEquityById(equity.getId());
			if (!equity.getIsin().equals(oldEquity.getIsin())
					|| !equity.getExchange().equals(oldEquity.getExchange())) {
				checkProductExistence(equity);
			}
			return EquitySQL.saveEquity(equity);
		}
	}

	private void checkProductExistence(Equity equity) throws TradistaBusinessException {
		if (getEquityByIsinAndExchangeCode(equity.getIsin(), equity.getExchange().getCode()) != null) {
			throw new TradistaBusinessException(String.format("This equity '%s' already exists in the exchange %s.",
					equity.getIsin(), equity.getExchange().getCode()));
		} else {
			Bond bond = bondService.getBondByIsinAndExchangeCode(equity.getIsin(), equity.getExchange().getCode());
			if (bond != null) {
				throw new TradistaBusinessException(
						String.format("There is already a bond with the same ISIN %s in the exchange %s.",
								equity.getIsin(), equity.getExchange().getCode()));
			}
		}
	}

	@Override
	public Set<Equity> getEquitiesByCreationDate(LocalDate date) {
		return EquitySQL.getEquitiesByCreationDate(date);
	}

	@Override
	public Set<Equity> getAllEquities() {
		return EquitySQL.getAllEquities();
	}

	@Override
	public Equity getEquityById(long id) {
		return EquitySQL.getEquityById(id);
	}

	@Override
	public Set<Equity> getEquitiesByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minActiveDate,
			LocalDate maxActiveDate) {
		return EquitySQL.getEquitiesByDates(minCreationDate, maxCreationDate, minActiveDate, maxActiveDate);
	}

	@Override
	public Set<Equity> getEquitiesByIsin(String isin) {
		return EquitySQL.getEquitiesByIsin(isin);
	}

	@Override
	public Equity getEquityByIsinAndExchangeCode(String isin, String exchangeCode) {
		return EquitySQL.getEquityByIsinAndExchangeCode(isin, exchangeCode);
	}

}