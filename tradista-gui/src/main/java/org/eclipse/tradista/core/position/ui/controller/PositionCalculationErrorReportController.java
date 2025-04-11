package org.eclipse.tradista.core.position.ui.controller;

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
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.position.model.BlankPositionDefinition;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionCalculationErrorBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

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

public class PositionCalculationErrorReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker valueDateFromDatePicker;

	@FXML
	private DatePicker valueDateToDatePicker;

	@FXML
	private DatePicker errorDateFromDatePicker;

	@FXML
	private DatePicker errorDateToDatePicker;

	@FXML
	private DatePicker solvingDateFromDatePicker;

	@FXML
	private DatePicker solvingDateToDatePicker;

	@FXML
	private ComboBox<PositionDefinition> positionDefinitionComboBox;

	@FXML
	private ComboBox<String> statusComboBox;

	@FXML
	private TextField tradeIdTextField;

	@FXML
	private TextField productIdTextField;

	@FXML
	private TableView<PositionCalculationError> report;

	@FXML
	private TableColumn<PositionCalculationError, String> errorDate;

	@FXML
	private TableColumn<PositionCalculationError, String> valueDate;

	@FXML
	private TableColumn<PositionCalculationError, String> solvingDate;

	@FXML
	private TableColumn<PositionCalculationError, String> book;

	@FXML
	private TableColumn<PositionCalculationError, String> productType;

	@FXML
	private TableColumn<PositionCalculationError, String> productId;

	@FXML
	private TableColumn<PositionCalculationError, String> counterparty;

	@FXML
	private TableColumn<PositionCalculationError, String> tradeId;

	@FXML
	private TableColumn<PositionCalculationError, String> message;

	@FXML
	private TableColumn<PositionCalculationError, String> status;

	private PositionCalculationErrorBusinessDelegate positionCalculationErrorBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		positionCalculationErrorBusinessDelegate = new PositionCalculationErrorBusinessDelegate();

		errorDate.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getErrorDate().toString()));

		valueDate.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getValueDate().toString()));

		solvingDate.setCellValueFactory(cellData -> {
			String solvingDateString = StringUtils.EMPTY;
			LocalDateTime solvDate = cellData.getValue().getSolvingDate();
			if (solvDate != null) {
				solvingDateString = solvDate.toString();
			}
			return new ReadOnlyObjectWrapper<>(solvingDateString);
		});

		book.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBook().getName()));

		productType.setCellValueFactory(cellData -> {
			Trade<? extends Product> trade = cellData.getValue().getTrade();
			if (trade != null) {
				return new ReadOnlyObjectWrapper<>(trade.getProductType());
			} else {
				Product product = cellData.getValue().getProduct();
				if (product != null) {
					return new ReadOnlyObjectWrapper<>(product.getProductType());
				}
			}
			return null;
		});

		productId.setCellValueFactory(cellData -> {
			Product product = cellData.getValue().getPositionDefinition().getProduct();
			if (product != null) {
				return new ReadOnlyObjectWrapper<>(Long.toString(product.getId()));
			} else {
				product = cellData.getValue().getProduct();
				if (product != null) {
					return new ReadOnlyObjectWrapper<>(Long.toString(product.getId()));
				}
			}
			return new ReadOnlyObjectWrapper<>(StringUtils.EMPTY);
		});

		counterparty.setCellValueFactory(cellData -> {
			String shortName = StringUtils.EMPTY;
			LegalEntity cpty = cellData.getValue().getPositionDefinition().getCounterparty();
			if (cpty != null) {
				shortName = cpty.getShortName();
			}
			return new ReadOnlyObjectWrapper<>(shortName);
		});

		tradeId.setCellValueFactory(cellData -> {
			if (cellData.getValue().getTrade() != null) {
				return new ReadOnlyObjectWrapper<String>(Long.toString(cellData.getValue().getTrade().getId()));
			} else {
				return new ReadOnlyObjectWrapper<>(StringUtils.EMPTY);
			}
		});

		message.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getErrorMessage()));

		status.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStatus().toString()));

		TradistaGUIUtil.fillPositionDefinitionComboBox(true, positionDefinitionComboBox);
		TradistaGUIUtil.fillErrorStatusComboBox(statusComboBox);
	}

	@FXML
	protected void load() {
		try {
			checkAmounts();

			if (positionDefinitionComboBox.getValue().equals(BlankPositionDefinition.getInstance())
					&& StringUtils.isEmpty(statusComboBox.getValue()) && tradeIdTextField.getText().isEmpty()
					&& tradeIdTextField.getText().isEmpty() && productIdTextField.getText().isEmpty()
					&& productIdTextField.getText().isEmpty() && errorDateFromDatePicker.getValue() == null
					&& errorDateToDatePicker.getValue() == null && valueDateFromDatePicker.getValue() == null
					&& valueDateToDatePicker.getValue() == null && solvingDateFromDatePicker.getValue() == null
					&& solvingDateToDatePicker.getValue() == null) {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Load Position Calculation Errors");
				confirmation.setHeaderText("Load Position Calculation Errors");
				confirmation.setContentText(
						"You are loading all the position calculation errors present in the system, it can take time. Are you sure to continue?");

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
			if (!tradeIdTextField.getText().isEmpty()) {
				Long.parseLong(tradeIdTextField.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(String.format("The trade id is incorrect: %s.%n", tradeIdTextField.getText()));
		}
		try {
			if (!productIdTextField.getText().isEmpty()) {
				Long.parseLong(productIdTextField.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(String.format("The product id is incorrect: %s.%n", productIdTextField.getText()));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	private void fillReport() {
		ObservableList<PositionCalculationError> data = null;
		List<PositionCalculationError> errors;
		try {
			errors = positionCalculationErrorBusinessDelegate.getPositionCalculationErrors(
					positionDefinitionComboBox.getValue().equals(BlankPositionDefinition.getInstance()) ? 0
							: positionDefinitionComboBox.getValue().getId(),
					Status.getStatus(statusComboBox.getValue()),
					tradeIdTextField.getText().isEmpty() ? 0 : Long.parseLong(tradeIdTextField.getText()),
					productIdTextField.getText().isEmpty() ? 0 : Long.parseLong(productIdTextField.getText()),
					valueDateFromDatePicker.getValue(), valueDateToDatePicker.getValue(),
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
			TradistaGUIUtil.export(report, "PositionCalculationErrors", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}