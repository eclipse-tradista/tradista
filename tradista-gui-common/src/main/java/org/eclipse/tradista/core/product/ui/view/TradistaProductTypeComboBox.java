package org.eclipse.tradista.core.product.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class TradistaProductTypeComboBox extends ComboBox<String> {

	public TradistaProductTypeComboBox() {
		this(null);
	}

	public TradistaProductTypeComboBox(@NamedArg("productFamily") String productFamily) {
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		Set<String> allProductTypes = null;
		if (productFamily == null) {
			allProductTypes = productBusinessDelegate.getAvailableProductTypes();
		} else if (productFamily.equals("fx")) {
			allProductTypes = productBusinessDelegate.getAvailableFXProductTypes();
		}
		if (allProductTypes != null && !allProductTypes.isEmpty()) {
			setItems(FXCollections.observableArrayList(allProductTypes));
		}
	}

}