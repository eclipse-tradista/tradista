package org.eclipse.tradista.core.legalentity.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@FacesConverter("legalEntityConverter")
public class LegalEntityConverter implements Serializable, Converter<LegalEntity> {

	private static final long serialVersionUID = 3802860683043711768L;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	public LegalEntityConverter() {
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, LegalEntity legalEntity) {
		return legalEntity.toString();
	}

	@Override
	public LegalEntity getAsObject(FacesContext context, UIComponent component, String value) {
		LegalEntity legalEntity = null;
		try {
			legalEntity = legalEntityBusinessDelegate.getLegalEntityByShortName(value);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert legal entity %s", value), tbe);
		}
		return legalEntity;
	}

}