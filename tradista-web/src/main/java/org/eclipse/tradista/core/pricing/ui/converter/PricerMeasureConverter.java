package org.eclipse.tradista.core.pricing.ui.converter;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

@FacesConverter("pricerMeasureConverter")
public class PricerMeasureConverter implements Serializable, Converter<PricerMeasure> {

	private static final long serialVersionUID = 2321182145005313366L;

	public PricerMeasureConverter() {
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, PricerMeasure pricerMeasure) {
		return pricerMeasure.toString();
	}

	@Override
	public PricerMeasure getAsObject(FacesContext context, UIComponent component, String value) {
		PricerMeasure pricerMeasure = null;
		Map<String, Object> attributes = component.getAttributes();
		Pricer pricer = (Pricer) attributes.get("pricer");
		if (pricer != null) {
			Optional<PricerMeasure> optPricerMeasure = pricer.getPricerMeasures().stream()
					.filter(pm -> pm.toString().equals(value)).findFirst();
			if (optPricerMeasure.isPresent()) {
				pricerMeasure = optPricerMeasure.get();
			}
		}
		return pricerMeasure;
	}

}