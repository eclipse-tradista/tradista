package org.eclipse.tradista.core.common.ui.view;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.legalentity.model.BlankLegalEntity;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class TradistaCopyDialog extends TradistaDialog<TradistaCopyDialog.Result> {

	public static class Result {
		private String name;
		private LegalEntity processingOrg;

		public Result(String name, LegalEntity processingOrg) {
			this.name = name;
			this.processingOrg = processingOrg;
		}

		public String getName() {
			return name;
		}

		public LegalEntity getProcessingOrg() {
			return processingOrg;
		}
	}

	public TradistaCopyDialog(String entityName, LegalEntity initialPO, String initialName, boolean allowGlobal) {
		super();
		setTitle(String.format("%s Copy", entityName));

		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField(initialName);

		Label poLabel = new Label("Processing Org: ");
		ComboBox<LegalEntity> poComboBox = new ComboBox<>();
		TradistaGUIUtil.fillProcessingOrgComboBox(poComboBox);
		if (allowGlobal) {
			poComboBox.getItems().add(0, BlankLegalEntity.getInstance());
		}

		if (initialPO != null) {
			poComboBox.setValue(initialPO);
		} else {
			poComboBox.getSelectionModel().selectFirst();
		}

		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");

		HBox headerHBox = new HBox();
		Label label1 = new Label();
		Label label2 = new Label();
		label2.getStyleClass().add("labelBold");
		Label label3 = new Label();
		headerHBox.getChildren().addAll(label1, label2, label3);

		grid.add(headerHBox, 1, 1, 2, 1);
		GridPane.setMargin(headerHBox, new Insets(0, 0, 20, 0));

		updateHeader(entityName, poComboBox.getValue(), label1, label2, label3);

		poComboBox.valueProperty().addListener((_, _, newValue) -> {
			updateHeader(entityName, newValue, label1, label2, label3);
			if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
				getDialogPane().getScene().getWindow().sizeToScene();
			}
		});

		grid.add(nameLabel, 1, 2);
		grid.add(nameTextField, 2, 2);
		grid.add(poLabel, 1, 3);
		grid.add(poComboBox, 2, 3);

		getDialogPane().setContent(grid);
		getDialogPane().setMinWidth(500);

		ButtonType buttonTypeOk = new ButtonType("Copy", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);

		setResultConverter(b -> {
			if (b == buttonTypeOk) {
				LegalEntity po = (poComboBox.getValue() instanceof BlankLegalEntity) ? null : poComboBox.getValue();
				return new Result(nameTextField.getText(), po);
			}
			return null;
		});

		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

	private void updateHeader(String entityName, LegalEntity po, Label label1, Label label2, Label label3) {
		if (po == null || po instanceof BlankLegalEntity) {
			label1.setText(String.format("The new %s will be global.", entityName));
			label2.setText(StringUtils.EMPTY);
			label3.setText(StringUtils.EMPTY);
		} else {
			label1.setText(String.format("The new %s will be linked to Processing Org ", entityName));
			label2.setText(po.getShortName());
			label3.setText(".");
		}
	}

}
