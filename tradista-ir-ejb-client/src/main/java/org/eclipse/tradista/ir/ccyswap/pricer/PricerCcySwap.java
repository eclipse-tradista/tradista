package org.eclipse.tradista.ir.ccyswap.pricer;

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

@Parameterizable(name = "Default CcySwap Pricer")
public class PricerCcySwap extends Pricer {

	private static final long serialVersionUID = -6022212913229139492L;

	public PricerCcySwap() {
		super();
		getPricerMeasures().add(new PricerMeasureNPV());
		getPricerMeasures().add(new PricerMeasureFIXED_LEG_PV());
		getPricerMeasures().add(new PricerMeasureFLOATING_LEG_PV());
		getPricerMeasures().add(new PricerMeasureFORWARD_SWAP_RATE());
		getPricerMeasures().add(new PricerMeasurePNL());
		getPricerMeasures().add(new PricerMeasureREALIZED_PNL());
		getPricerMeasures().add(new PricerMeasureUNREALIZED_PNL());
	}

}