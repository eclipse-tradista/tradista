package org.eclipse.tradista.core.trade.model;

import org.eclipse.tradista.core.product.model.Product;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public abstract class VanillaOptionTrade<T extends Trade<? extends Product>> extends OptionTrade<T> {

	private static final long serialVersionUID = -9194967197051172928L;

	public static enum Style {
		EUROPEAN, AMERICAN;

		public String toString() {
			switch (this) {
			case EUROPEAN:
				return "European";
			case AMERICAN:
				return "American";
			}
			return super.toString();
		}
	};

	private Style style;

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}
}