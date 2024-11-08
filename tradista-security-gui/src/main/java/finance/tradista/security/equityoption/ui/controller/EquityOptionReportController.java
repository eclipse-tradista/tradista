package org.eclipse.tradista.security.equityoption.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.service.EquityOptionBusinessDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class EquityOptionReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker creationDateFromDatePicker;

	@FXML
	private DatePicker creationDateToDatePicker;

	@FXML
	private TextField idTextField;

	@FXML
	private TextField codeTextField;

	@FXML
	private TableView<EquityOptionProperty> report;

	@FXML
	private TableColumn<EquityOptionProperty, Number> id;

	@FXML
	private TableColumn<EquityOptionProperty, String> code;

	@FXML
	private TableColumn<EquityOptionProperty, String> equity;

	@FXML
	private TableColumn<EquityOptionProperty, String> quantity;

	@FXML
	private TableColumn<EquityOptionProperty, String> style;

	@FXML
	private TableColumn<EquityOptionProperty, String> exchange;

	private EquityOptionBusinessDelegate equityOptionBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		equityOptionBusinessDelegate = new EquityOptionBusinessDelegate();

		id.setCellValueFactory(cellData -> cellData.getValue().getId());
		code.setCellValueFactory(cellData -> cellData.getValue().getCode());
		equity.setCellValueFactory(cellData -> cellData.getValue().getEquity());
		quantity.setCellValueFactory(cellData -> cellData.getValue().getQuantity());
		style.setCellValueFactory(cellData -> cellData.getValue().getStyle());
		exchange.setCellValueFactory(cellData -> cellData.getValue().getExchange());
	}

	@FXML
	protected void load() {
		ObservableList<EquityOptionProperty> data = null;
		if (!idTextField.getText().isEmpty()) {
			EquityOption equityOption = equityOptionBusinessDelegate
					.getEquityOptionById(Long.parseLong(idTextField.getText()));
			if (equityOption != null) {
				EquityOptionProperty eop = new EquityOptionProperty(equityOption);
				data = FXCollections.observableArrayList(eop);
			}
			report.setItems(data);
			report.refresh();

		} else if (!codeTextField.getText().isEmpty()) {
			try {
				Set<EquityOption> equityOptions = equityOptionBusinessDelegate
						.getEquityOptionsByCode(codeTextField.getText());
				if (equityOptions != null) {
					if (!equityOptions.isEmpty()) {
						List<EquityOptionProperty> eopList = new ArrayList<>(equityOptions.size());
						for (EquityOption equityOption : equityOptions) {
							eopList.add(new EquityOptionProperty(equityOption));
						}
						data = FXCollections.observableArrayList(eopList);
					}
				}
				report.setItems(data);
				report.refresh();
			} catch (TradistaBusinessException tbe) {
			}
		} else {
			if (creationDateFromDatePicker.getValue() == null && creationDateToDatePicker.getValue() == null) {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Load Equity Options");
				confirmation.setHeaderText("Load Equity Options");
				confirmation.setContentText(
						"You are loading all the equity options present in the system, it can take time. Are you sure to continue?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.get() == ButtonType.OK) {
					fillReport();
				}

			} else {
				fillReport();
			}
		}
	}

	private void fillReport() {
		ObservableList<EquityOptionProperty> data = null;
		Set<EquityOption> equityOptions;
		try {
			equityOptions = equityOptionBusinessDelegate.getEquityOptionsByCreationDate(
					creationDateFromDatePicker.getValue(), creationDateToDatePicker.getValue());
			if (equityOptions != null) {
				if (!equityOptions.isEmpty()) {
					List<EquityOptionProperty> eopList = new ArrayList<>(equityOptions.size());
					for (EquityOption equityOption : equityOptions) {
						eopList.add(new EquityOptionProperty(equityOption));
					}
					data = FXCollections.observableArrayList(eopList);
				}
			}
			report.setItems(data);
			report.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void export() {
		try {
			TradistaGUIUtil.export(report, "EquityOptions", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}