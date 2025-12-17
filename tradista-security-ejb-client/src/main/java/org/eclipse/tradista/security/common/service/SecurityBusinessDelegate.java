package org.eclipse.tradista.security.common.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.service.BondBusinessDelegate;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class SecurityBusinessDelegate {

	protected BondBusinessDelegate bondBusinessDelegate;

	protected EquityBusinessDelegate equityBusinessDelegate;

	public SecurityBusinessDelegate() {
		bondBusinessDelegate = new BondBusinessDelegate();
		equityBusinessDelegate = new EquityBusinessDelegate();
	}

	public Set<Security> getAllSecurities() {
		Set<Security> securities = new HashSet<>();
		Set<Bond> bonds = bondBusinessDelegate.getAllBonds();
		Set<Equity> equities = equityBusinessDelegate.getAllEquities();
		if (bonds != null) {
			securities.addAll(bonds);
		}
		if (equities != null) {
			securities.addAll(equities);
		}
		return securities;
	}

	public Security getSecurityByIsinAndExchangeCode(String isin, String exchangeCode)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(isin)) {
			errMsg.append(String.format("The isin is mandatory.%n"));
		}
		if (StringUtils.isBlank(exchangeCode)) {
			errMsg.append(String.format("The exchange code is mandatory.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		Security security = bondBusinessDelegate.getBondByIsinAndExchangeCode(isin, exchangeCode);
		if (security == null) {
			security = equityBusinessDelegate.getEquityByIsinAndExchangeCode(isin, exchangeCode);
		}
		return security;
	}
}