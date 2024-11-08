package org.eclipse.tradista.security.equity.ui.view;

import java.util.Set;

import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
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

public class TradistaEquityComboBox extends ComboBox<Equity> {

	public TradistaEquityComboBox() {
		EquityBusinessDelegate equityBusinessDelegate = new EquityBusinessDelegate();
		Set<Equity> allEquities = equityBusinessDelegate.getAllEquities();
		if (allEquities != null && !allEquities.isEmpty()) {
			setItems(FXCollections.observableArrayList(allEquities));
		}
	}

}