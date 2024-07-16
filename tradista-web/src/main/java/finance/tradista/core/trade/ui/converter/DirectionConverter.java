package finance.tradista.core.trade.ui.converter;

import java.io.Serializable;

import finance.tradista.core.trade.model.Trade.Direction;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
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

@FacesConverter("directionConverter")
public class DirectionConverter implements Serializable, Converter<Direction> {

	private static final long serialVersionUID = 5084880331451449919L;

	public DirectionConverter() {
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Direction direction) {
		return direction.toString();
	}

	@Override
	public Direction getAsObject(FacesContext context, UIComponent component, String value) {
		return Direction.valueOf(value.toUpperCase());
	}

}