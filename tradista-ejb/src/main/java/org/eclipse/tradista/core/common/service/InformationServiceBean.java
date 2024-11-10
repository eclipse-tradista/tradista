package org.eclipse.tradista.core.common.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.marketdata.service.MarketDataInformationBusinessDelegate;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.fx.common.service.FXInformationBusinessDelegate;
import org.eclipse.tradista.ir.common.service.IRInformationBusinessDelegate;
import org.eclipse.tradista.mm.common.service.MMInformationBusinessDelegate;
import org.eclipse.tradista.security.common.service.SecurityInformationBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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
public class InformationServiceBean implements InformationService {

	ProductBusinessDelegate productBusinessDelegate;

	@PostConstruct
	private void init() {
		productBusinessDelegate = new ProductBusinessDelegate();
	}

	@Override
	public Map<String, String> getModules() {
		Map<String, String> modules = new LinkedHashMap<String, String>();

		// Get the core version
		modules.put("Core", getClass().getPackage().getImplementationVersion());

		// Get the Market Data version
		try {
			modules.putAll(new MarketDataInformationBusinessDelegate().getMarketDataModuleVersions());
		} catch (TradistaTechnicalException tte) {
		}

		Set<String> prods = productBusinessDelegate.getAvailableProductTypes();

		if (prods != null && !prods.isEmpty()) {
			for (String prod : prods) {
				String prodFamily = null;
				try {
					prodFamily = productBusinessDelegate.getProductFamily(prod);
				} catch (TradistaBusinessException abe) {
					// Should not happen here
				}
				switch (prodFamily) {
				case ("fx"): {
					modules.put("FX", new FXInformationBusinessDelegate().getFXModuleVersion());
					break;
				}
				case ("mm"): {
					modules.put("MM", new MMInformationBusinessDelegate().getMMModuleVersion());
					break;
				}
				case ("ir"): {
					modules.put("IR", new IRInformationBusinessDelegate().getIRModuleVersion());
					break;
				}
				case ("security"): {
					modules.put("Security", new SecurityInformationBusinessDelegate().getSecurityModuleVersion());
					break;
				}
				}
			}
		}

		return modules;
	}

}