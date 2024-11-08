package org.eclipse.tradista.ai.reasoning.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

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

public class NPVFXGTEFunction extends Function<Boolean> {

	public static final String NPV_FX_GTE = "npv_fx_gte";

	private BigDecimal threshold;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1958965455115885635L;

	public NPVFXGTEFunction(BigDecimal threshold) {
		super();
		setName(NPV_FX_GTE);
		addParameterType("Quote Currency", Currency.class, true);
		addParameterType("Primary Currency", Currency.class, true);
		addParameterType("Value Currency", Currency.class, true);
		addParameterType("Settlement Date", LocalDate.class, true);
		addParameterType("Pricing Parameters Set", PricingParameter.class, true);
		addParameterType("Threshold", BigDecimal.class, true);
		this.threshold = threshold;
	}

	public BigDecimal getThreshold() {
		return threshold;
	}

	public void setThreshold(BigDecimal threshold) {
		this.threshold = threshold;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NPVFXGTEFunction other = (NPVFXGTEFunction) obj;
		if (threshold == null) {
			if (other.threshold != null)
				return false;
		} else if (threshold.compareTo(other.threshold) != 0)
			return false;
		return true;
	}

}