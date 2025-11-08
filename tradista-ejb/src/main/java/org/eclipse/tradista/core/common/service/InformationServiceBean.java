package org.eclipse.tradista.core.common.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.service.MarketDataInformationBusinessDelegate;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.fx.common.service.FXInformationService;
import org.eclipse.tradista.ir.common.service.IRInformationService;
import org.eclipse.tradista.mm.common.service.MMInformationService;
import org.eclipse.tradista.security.common.service.SecurityInformationService;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
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

	private ProductBusinessDelegate productBusinessDelegate;

	@EJB
	private FXInformationService fxInformationService;

	@EJB
	private MMInformationService mmInformationService;

	@EJB
	private IRInformationService irInformationService;

	@EJB
	private SecurityInformationService securityInformationService;

	@PostConstruct
	private void init() {
		productBusinessDelegate = new ProductBusinessDelegate();
	}

	@Override
	public Map<String, String> getModules() {
		Map<String, String> modules = new LinkedHashMap<>();

		// Get the core version
		modules.put("Core", TradistaUtil.getModuleVersion("core.common.service", this.getClass().getClassLoader()));

		// Get the Market Data version
		modules.putAll(new MarketDataInformationBusinessDelegate().getMarketDataModuleVersions());

		Set<String> prods = productBusinessDelegate.getAvailableProductTypes();

		if (prods != null && !prods.isEmpty()) {
			for (String prod : prods) {
				String prodFamily = null;
				try {
					prodFamily = productBusinessDelegate.getProductFamily(prod);
				} catch (TradistaBusinessException _) {
					// Should not happen here
				}
				switch (prodFamily) {
				case ("fx"): {
					modules.put("FX", fxInformationService.getFXModuleVersion());
					break;
				}
				case ("mm"): {
					modules.put("MM", mmInformationService.getMMModuleVersion());
					break;
				}
				case ("ir"): {
					modules.put("IR", irInformationService.getIRModuleVersion());
					break;
				}
				case ("security"): {
					modules.put("Security", securityInformationService.getSecurityModuleVersion());
					break;
				}
				}
			}
		}

		return modules;
	}

}