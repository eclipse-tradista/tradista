package finance.tradista.fx.fxswap.pricer;

import finance.tradista.core.pricing.pricer.Parameterizable;
import finance.tradista.core.pricing.pricer.Pricer;

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

@Parameterizable(name = "Default FXSwap Pricer")
public class PricerFXSwap extends Pricer {

	private static final long serialVersionUID = 8415701698899396870L;

	public PricerFXSwap() {
		super();
		getPricerMeasures().add(new PricerMeasureNPV());
		getPricerMeasures().add(new PricerMeasureSPOT_LEG_NPV());
		getPricerMeasures().add(new PricerMeasureFWD_LEG_NPV());
		getPricerMeasures().add(new PricerMeasurePNL());
		getPricerMeasures().add(new PricerMeasureREALIZED_PNL());
		getPricerMeasures().add(new PricerMeasureUNREALIZED_PNL());
	}

}
