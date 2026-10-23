package org.eclipse.tradista.security.gcrepo.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.service.GCBasketBusinessDelegate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

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

@ApplicationScoped
@FacesConverter(value = "gcBasketConverter", managed = true)
public class GCBasketConverter implements Serializable, Converter<GCBasket> {

	private static final long serialVersionUID = -19742544917495063L;

	@Inject
	private GCBasketBusinessDelegate gcBasketBusinessDelegate;

	@Override
	public String getAsString(FacesContext context, UIComponent component, GCBasket gcBasket) {
		String value = null;
		if (gcBasket != null) {
			value = gcBasket.getName();
		}
		return value;
	}

	@Override
	public GCBasket getAsObject(FacesContext context, UIComponent component, String value) {
		GCBasket gcBasket = null;
		if (value != null) {
			try {
				gcBasket = gcBasketBusinessDelegate.getGCBasketByName(value);
			} catch (TradistaBusinessException tbe) {
				throw new ConverterException(tbe.getMessage());
			}
		}
		return gcBasket;
	}

}