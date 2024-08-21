package finance.tradista.security.common.service;

import java.util.HashSet;
import java.util.Set;

import finance.tradista.security.bond.model.Bond;
import finance.tradista.security.bond.service.BondBusinessDelegate;
import finance.tradista.security.common.model.Security;
import finance.tradista.security.equity.model.Equity;
import finance.tradista.security.equity.service.EquityBusinessDelegate;

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
}