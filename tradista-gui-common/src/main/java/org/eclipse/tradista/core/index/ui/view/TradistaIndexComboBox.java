package org.eclipse.tradista.core.index.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
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

public class TradistaIndexComboBox extends ComboBox<Index> {

	public TradistaIndexComboBox() {
		IndexBusinessDelegate indexBusinessDelegate = new IndexBusinessDelegate();
		Set<Index> allIndexCodes = indexBusinessDelegate.getAllIndexes();
		if (allIndexCodes != null && !allIndexCodes.isEmpty()) {
			setItems(FXCollections.observableArrayList(allIndexCodes));
		}
	}

}