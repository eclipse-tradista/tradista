package org.eclipse.tradista.core.marketdata.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

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

@FacesConverter("quoteSetConverter")
public class QuoteSetConverter implements Serializable, Converter<QuoteSet> {

	private static final long serialVersionUID = -2974706453367560177L;

	private QuoteBusinessDelegate quoteBusinessDelegate;

	public QuoteSetConverter() {
		quoteBusinessDelegate = new QuoteBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, QuoteSet quoteSet) {
		return quoteSet.getName();
	}

	@Override
	public QuoteSet getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			return quoteBusinessDelegate.getQuoteSetByName(value);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert quote set %s", value), tbe);
		}
	}

}