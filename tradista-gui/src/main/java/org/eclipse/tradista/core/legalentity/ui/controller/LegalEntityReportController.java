package org.eclipse.tradista.core.legalentity.ui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class LegalEntityReportController extends TradistaControllerAdapter {

	@FXML
	private TextField shortNameTextField;

	@FXML
	private TableView<LegalEntityProperty> report;

	@FXML
	private TableColumn<LegalEntityProperty, Number> id;

	@FXML
	private TableColumn<LegalEntityProperty, String> shortName;

	@FXML
	private TableColumn<LegalEntityProperty, String> longName;

	@FXML
	private TableColumn<LegalEntityProperty, String> description;

	@FXML
	private TableColumn<LegalEntityProperty, String> role;

	@FXML
	private ComboBox<String> roleComboBox;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		id.setCellValueFactory(cellData -> cellData.getValue().getId());
		shortName.setCellValueFactory(cellData -> cellData.getValue().getShortName());
		longName.setCellValueFactory(cellData -> cellData.getValue().getLongName());
		description.setCellValueFactory(cellData -> cellData.getValue().getDescription());
		role.setCellValueFactory(cellData -> cellData.getValue().getRole());
		TradistaGUIUtil.fillComboBox(
				Arrays.asList(LegalEntity.Role.values()).stream().map(t -> t.toString()).collect(Collectors.toList()),
				roleComboBox);
		roleComboBox.getItems().add(0, StringUtils.EMPTY);
		roleComboBox.getSelectionModel().selectFirst();
	}

	@FXML
	protected void load() {
		if (StringUtils.isEmpty(shortNameTextField.getText()) && roleComboBox.getValue().isEmpty()) {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Load Legal Entities");
			confirmation.setHeaderText("Load Legal Entities");
			confirmation.setContentText(
					"You are loading all the legal entities present in the system, it can take time. Are you sure to continue?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fillReport();
			}
		} else {
			fillReport();
		}
	}

	private void fillReport() {
		ObservableList<LegalEntityProperty> data = null;
		LegalEntity.Role role = null;
		if (!roleComboBox.getValue().isEmpty()) {
			role = LegalEntity.Role.getRole(roleComboBox.getValue());
		}
		Set<LegalEntity> legalEntities = legalEntityBusinessDelegate
				.getLegalEntitiesByShortNameAndRole("%" + shortNameTextField.getText() + "%", role);

		if (legalEntities != null) {
			if (!legalEntities.isEmpty()) {
				List<LegalEntityProperty> bpList = new ArrayList<>(legalEntities.size());
				for (LegalEntity legalEntity : legalEntities) {
					bpList.add(new LegalEntityProperty(legalEntity));
				}
				data = FXCollections.observableArrayList(bpList);
			}
		}
		report.setItems(data);
		report.refresh();
	}

	@FXML
	protected void export() {
		try {
			TradistaGUIUtil.export(report, "LegalEntities", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}