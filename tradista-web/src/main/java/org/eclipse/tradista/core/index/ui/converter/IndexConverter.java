package org.eclipse.tradista.core.index.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;

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

@FacesConverter("indexConverter")
public class IndexConverter implements Serializable, Converter<Index> {

	private static final long serialVersionUID = 1302621501579077205L;
	private IndexBusinessDelegate indexBusinessDelegate;

	public IndexConverter() {
		indexBusinessDelegate = new IndexBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Index index) {
		return index.toString();
	}

	@Override
	public Index getAsObject(FacesContext context, UIComponent component, String value) {
		Index index = null;
		try {
			index = indexBusinessDelegate.getIndexByName(value);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert index %s", value), tbe);
		}
		return index;
	}

}