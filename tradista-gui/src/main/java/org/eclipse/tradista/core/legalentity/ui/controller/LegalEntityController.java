package org.eclipse.tradista.core.legalentity.ui.controller;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.ui.view.LegalEntityCreatorDialog;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

public class LegalEntityController extends TradistaControllerAdapter {

	@FXML
	private TextField shortName;

	@FXML
	private TextField longName;

	@FXML
	private ComboBox<LegalEntity.Role> role;

	@FXML
	private TextArea description;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	@FXML
	private TextField load;

	private LegalEntity legalEntity;

	@FXML
	private Label legalEntityId;

	@FXML
	private ComboBox<String> loadingCriterion;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		loadingCriterion.getItems().add("id");
		loadingCriterion.getItems().add("short name");
		loadingCriterion.getSelectionModel().selectFirst();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		role.setItems(FXCollections.observableArrayList(LegalEntity.Role.values()));
		role.setValue(LegalEntity.Role.COUNTERPARTY);
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Legal Entity");
		confirmation.setHeaderText("Save Legal Entity");
		confirmation.setContentText("Do you want to save this Legal Entity?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (legalEntity == null) {
					legalEntity = new LegalEntity(shortName.getText());
				}
				legalEntity.setLongName(longName.getText());
				legalEntity.setDescription(description.getText());
				legalEntity.setRole(role.getValue());
				legalEntity.setId(legalEntityBusinessDelegate.saveLegalEntity(legalEntity));
				legalEntityId.setText(String.valueOf(legalEntity.getId()));
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		try {
			LegalEntityCreatorDialog dialog = new LegalEntityCreatorDialog();
			Optional<LegalEntity> result = dialog.showAndWait();
			if (result.isPresent()) {
				LegalEntity copyLegalEntity = new LegalEntity(shortName.getText());
				copyLegalEntity.setLongName(result.get().getLongName());
				copyLegalEntity.setDescription(description.getText());
				copyLegalEntity.setRole(role.getValue());
				copyLegalEntity.setId(legalEntityBusinessDelegate.saveLegalEntity(copyLegalEntity));
				legalEntity = copyLegalEntity;
				legalEntityId.setText(String.valueOf(legalEntity.getId()));
				shortName.setText(legalEntity.getShortName());
				longName.setText(legalEntity.getLongName());
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		LegalEntity legalEntity = null;
		String legalEntityShortName = null;
		long legalEntityId = 0;
		try {
			try {
				if (!load.getText().isEmpty()) {
					if (loadingCriterion.getValue().equals("id")) {
						legalEntityId = Long.parseLong(load.getText());
					} else {
						legalEntityShortName = load.getText();
					}
				} else {
					throw new TradistaBusinessException("Please specify an id or a short name.");
				}
			} catch (NumberFormatException nfe) {
				throw new TradistaBusinessException(
						String.format("The legal entity id is incorrect: %s", load.getText()));
			}

			if (loadingCriterion.getValue().equals("id")) {
				legalEntity = legalEntityBusinessDelegate.getLegalEntityById(legalEntityId);
			} else {
				legalEntity = legalEntityBusinessDelegate.getLegalEntityByShortName(legalEntityShortName);
			}

			if (legalEntity == null) {
				throw new TradistaBusinessException(
						String.format("The legal entity %s doesn't exist in the system.", load.getText()));
			}

			load(legalEntity);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(LegalEntity legalEntity) {
		this.legalEntity = legalEntity;
		legalEntityId.setText(Long.toString(legalEntity.getId()));
		description.setText(legalEntity.getDescription());
		longName.setText(legalEntity.getLongName());
		shortName.setText(legalEntity.getShortName());
		role.setValue(legalEntity.getRole());
	}

	@Override
	@FXML
	public void clear() {
		legalEntity = null;
		legalEntityId.setText("");
		shortName.clear();
		longName.clear();
		description.clear();
		role.setValue(LegalEntity.Role.COUNTERPARTY);
	}

}