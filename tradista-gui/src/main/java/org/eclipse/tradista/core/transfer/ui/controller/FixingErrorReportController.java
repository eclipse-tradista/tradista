package org.eclipse.tradista.core.transfer.ui.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class FixingErrorReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker errorDateFromDatePicker;

	@FXML
	private DatePicker errorDateToDatePicker;

	@FXML
	private DatePicker solvingDateFromDatePicker;

	@FXML
	private DatePicker solvingDateToDatePicker;

	@FXML
	private TextField transferIdTextField;

	@FXML
	private ComboBox<String> statusComboBox;

	@FXML
	private TableView<FixingError> report;

	@FXML
	private TableColumn<FixingError, String> errorDate;

	@FXML
	private TableColumn<FixingError, String> solvingDate;

	@FXML
	private TableColumn<FixingError, String> transferId;

	@FXML
	private TableColumn<FixingError, String> message;

	@FXML
	private TableColumn<FixingError, String> status;

	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();

		errorDate.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getErrorDate().toString()));

		solvingDate.setCellValueFactory(cellData -> {
			String solvingDateString = StringUtils.EMPTY;
			LocalDateTime solvDate = cellData.getValue().getSolvingDate();
			if (solvDate != null) {
				solvingDateString = solvDate.toString();
			}
			return new ReadOnlyObjectWrapper<>(solvingDateString);
		});

		transferId.setCellValueFactory(cellData -> {
			if (cellData.getValue().getCashTransfer() != null) {
				return new ReadOnlyObjectWrapper<>(Long.toString(cellData.getValue().getCashTransfer().getId()));
			} else {
				return new ReadOnlyObjectWrapper<>(StringUtils.EMPTY);
			}
		});

		message.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getMessage()));

		status.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStatus().toString()));

		TradistaGUIUtil.fillErrorStatusComboBox(statusComboBox);
	}

	@FXML
	protected void load() {
		try {
			checkAmounts();

			if (StringUtils.isEmpty(statusComboBox.getValue()) && transferIdTextField.getText().isEmpty()
					&& errorDateFromDatePicker.getValue() == null && errorDateToDatePicker.getValue() == null
					&& solvingDateFromDatePicker.getValue() == null && solvingDateToDatePicker.getValue() == null) {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Load Fixing Errors");
				confirmation.setHeaderText("Load Fixing Errors");
				confirmation.setContentText(
						"You are loading all the fixing errors present in the system, it can take time. Are you sure to continue?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					fillReport();
				}
			} else {
				fillReport();
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			if (!transferIdTextField.getText().isEmpty()) {
				Long.parseLong(transferIdTextField.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(String.format("The transfer id is incorrect: %s.%n", transferIdTextField.getText()));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	private void fillReport() {
		ObservableList<FixingError> data = null;
		List<FixingError> errors;
		try {
			errors = fixingErrorBusinessDelegate.getFixingErrors(
					transferIdTextField.getText().isEmpty() ? 0 : Long.parseLong(transferIdTextField.getText()),
					StringUtils.isEmpty(statusComboBox.getValue()) ? null : Status.getStatus(statusComboBox.getValue()),
					errorDateFromDatePicker.getValue(), errorDateToDatePicker.getValue(),
					solvingDateFromDatePicker.getValue(), solvingDateToDatePicker.getValue());

			if (errors != null) {
				data = FXCollections.observableArrayList(errors);
			} else {
				data = FXCollections.emptyObservableList();
			}
			report.setItems(data);
			report.refresh();
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void export() {
		try {
			TradistaGUIUtil.export(report, "FixingErrors", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}