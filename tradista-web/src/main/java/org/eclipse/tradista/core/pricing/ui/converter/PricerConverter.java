package org.eclipse.tradista.core.pricing.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.pricing.pricer.Parameterizable;
import org.eclipse.tradista.core.pricing.pricer.Pricer;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
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

@FacesConverter("pricerConverter")
public class PricerConverter implements Serializable, Converter<Pricer> {

	private static final long serialVersionUID = -1163062733540145757L;

	public PricerConverter() {
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Pricer pricer) {
		return pricer.getClass().getAnnotation(Parameterizable.class).name();
	}

	@Override
	public Pricer getAsObject(FacesContext context, UIComponent component, String value) {
		Pricer pricer = null;
		try {
			pricer = (Pricer) TradistaUtil.getInstance(TradistaUtil
					.getAllClassesByTypeAndAnnotation(Pricer.class, Parameterizable.class, "org.eclipse.tradista").stream()
					.filter(p -> p.getClass().getAnnotation(Parameterizable.class).name().equals(value)).findFirst()
					.get());
		} catch (TradistaTechnicalException tte) {
			throw new ConverterException(String.format("Could not convert pricer %s", value), tte);
		}
		return pricer;
	}

}