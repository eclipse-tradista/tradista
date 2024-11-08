package org.eclipse.tradista.core.position.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class TradistaPositionDefinitionComboBox extends ComboBox<PositionDefinition> {

	public TradistaPositionDefinitionComboBox() {
		PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();
		Set<PositionDefinition> posDefs = positionDefinitionBusinessDelegate.getAllPositionDefinitions();
		ObservableList<PositionDefinition> posDefsObservableList = null;
		if (posDefs == null) {
			posDefsObservableList = FXCollections.emptyObservableList();
		} else {
			posDefsObservableList = FXCollections.observableArrayList(posDefs);
		}
		setItems(posDefsObservableList);
	}

	private PositionDefinition model;

	public PositionDefinition getModel() {
		return model;
	}

	public void setModel(PositionDefinition model) {
		this.model = model;
	}

}