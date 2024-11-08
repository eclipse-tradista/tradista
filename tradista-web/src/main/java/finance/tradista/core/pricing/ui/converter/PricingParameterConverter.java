package org.eclipse.tradista.core.pricing.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
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

@FacesConverter("pricingParameterConverter")
public class PricingParameterConverter implements Serializable, Converter<PricingParameter> {

	private static final long serialVersionUID = -6835622240440539539L;

	private PricerBusinessDelegate pricerBusinessDelegate;

	public PricingParameterConverter() {
		pricerBusinessDelegate = new PricerBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, PricingParameter pricingParameter) {
		return pricingParameter.getName();
	}

	@Override
	public PricingParameter getAsObject(FacesContext context, UIComponent component, String value) {
		PricingParameter pricingParameter = null;
		try {
			LegalEntity currentPo = ClientUtil.getCurrentUser().getProcessingOrg();
			long poId = currentPo != null ? currentPo.getId() : 0;
			pricingParameter = pricerBusinessDelegate.getPricingParameterByNameAndPoId(value, poId);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert index %s", value), tbe);
		}
		return pricingParameter;
	}

}