package org.eclipse.tradista.core.legalentity.ui.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class LegalEntityCreatorDialog extends TradistaDialog<LegalEntity> {

	public LegalEntityCreatorDialog() {
		super();
		setTitle("Legal Entity Copy");
		setHeaderText("Please specify short and long names for the new Legal Entity.");
		Label shortNameLabel = new Label("Short Name: ");
		TextField shortNameTextField = new TextField();
		Label longNameLabel = new Label("Long Name: ");
		TextField longNameTextField = new TextField();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(shortNameLabel, 1, 1);
		grid.add(shortNameTextField, 2, 1);
		grid.add(longNameLabel, 1, 2);
		grid.add(longNameTextField, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Copy", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<>() {
			@Override
			public LegalEntity call(ButtonType b) {
				if (b == buttonTypeOk) {
					LegalEntity legalEntity = new LegalEntity(shortNameTextField.getText());
					legalEntity.setLongName(longNameTextField.getText());
					return legalEntity;
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

}