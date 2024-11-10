package org.eclipse.tradista.security.bond.pricer;

import org.eclipse.tradista.core.pricing.pricer.Parameterizable;
import org.eclipse.tradista.core.pricing.pricer.Pricer;

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

@Parameterizable(name = "Default Bond Pricer")
public class PricerBond extends Pricer {

	private static final long serialVersionUID = -7385707311723563392L;

	public PricerBond() {
		super();
		getPricerMeasures().add(new PricerMeasurePV());
		getPricerMeasures().add(new PricerMeasureNPV());
		getPricerMeasures().add(new PricerMeasureCLEAN_PRICE());
		getPricerMeasures().add(new PricerMeasureDIRTY_PRICE());
		getPricerMeasures().add(new PricerMeasureYTM());
		getPricerMeasures().add(new PricerMeasurePARYIELD());

		// Product pricer measures
		getProductPricerMeasures().add(new PricerMeasurePNL());
		getProductPricerMeasures().add(new PricerMeasureREALIZED_PNL());
		getProductPricerMeasures().add(new PricerMeasureUNREALIZED_PNL());
	}

}