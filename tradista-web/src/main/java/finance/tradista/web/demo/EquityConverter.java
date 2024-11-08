package org.eclipse.tradista.web.demo;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import java.io.Serializable;

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

@Named
@ViewScoped
public class EquityConverter implements Serializable, Converter<Equity> {

	private static final long serialVersionUID = -4653152149962851313L;
	private EquityBusinessDelegate equityBusinessDelegate;

	public EquityConverter() {
		equityBusinessDelegate = new EquityBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Equity equity) {
		return equity.toString();
	}

	@Override
	public Equity getAsObject(FacesContext context, UIComponent component, String value) {
		return equityBusinessDelegate.getEquityByIsinAndExchangeCode(value.substring(0, value.indexOf(" - ")),
				value.substring(value.indexOf(" - ") + 3));
	}

}