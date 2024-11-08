package org.eclipse.tradista.core.transfer.ui.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.book.model.BlankBook;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.currency.model.BlankCurrency;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

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

public class TransferReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker creationDateFromDatePicker;

	@FXML
	private DatePicker creationDateToDatePicker;

	@FXML
	private DatePicker settlementDateFromDatePicker;

	@FXML
	private DatePicker settlementDateToDatePicker;

	@FXML
	private DatePicker fixingDateFromDatePicker;

	@FXML
	private DatePicker fixingDateToDatePicker;

	@FXML
	private ComboBox<String> typeComboBox;

	@FXML
	private ComboBox<String> statusComboBox;

	@FXML
	private ComboBox<String> directionComboBox;

	@FXML
	private ComboBox<String> purposeComboBox;

	@FXML
	private TextField tradeIdTextField;

	@FXML
	private TextField idTextField;

	@FXML
	private TextField productIdTextField;

	@FXML
	private ComboBox<Book> bookComboBox;

	@FXML
	private ComboBox<Currency> currencyComboBox;

	@FXML
	private Label currencyLabel;

	@FXML
	private TableView<Transfer> report;

	@FXML
	private TableColumn<Transfer, String> creationDate;

	@FXML
	private TableColumn<Transfer, String> settlementDate;

	@FXML
	private TableColumn<Transfer, String> fixingDate;

	@FXML
	private TableColumn<Transfer, String> id;

	@FXML
	private TableColumn<Transfer, String> productId;

	@FXML
	private TableColumn<Transfer, String> type;

	@FXML
	private TableColumn<Transfer, Status> status;

	@FXML
	private TableColumn<Transfer, String> direction;

	@FXML
	private TableColumn<Transfer, String> purpose;

	@FXML
	private TableColumn<Transfer, String> tradeId;

	@FXML
	private TableColumn<Transfer, String> book;

	@FXML
	private TableColumn<Transfer, String> currency;

	@FXML
	private TableColumn<Transfer, String> quantity;

	private TransferBusinessDelegate transferBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		transferBusinessDelegate = new TransferBusinessDelegate();

		id.setCellValueFactory(new PropertyValueFactory<>("id"));

		creationDate.setCellValueFactory(new PropertyValueFactory<>("creationDateTime"));

		settlementDate.setCellValueFactory(new PropertyValueFactory<>("settlementDate"));

		fixingDate.setCellValueFactory(new PropertyValueFactory<>("fixingDateTime"));

		type.setCellValueFactory(new PropertyValueFactory<>("type"));

		status.setCellValueFactory(new PropertyValueFactory<>("status"));

		status.setCellFactory(column -> {
			return new TableCell<Transfer, Status>() {
				@Override
				protected void updateItem(Status item, boolean empty) {
					super.updateItem(item, empty);

					setText(empty ? "" : getItem().toString());
					setGraphic(null);

					TableRow<?> currentRow = getTableRow();

					if (!isEmpty()) {
						// See https://www.htmlcodes.ws/color/html-color-tester.cfm
						// TO DO: use a Util class for colors
						if (item.equals(Transfer.Status.KNOWN)) {
							// lightgreen color
							currentRow.setStyle("-fx-background-color:rgba(144, 238, 144, .7)");
						}
						if (item.equals(Transfer.Status.UNKNOWN)) {
							// #ffc14d color
							currentRow.setStyle("-fx-background-color:rgba(255, 193, 77, .7)");
						}
						if (item.equals(Transfer.Status.CANCELED)) {
							// indianred color
							currentRow.setStyle("-fx-background-color:rgba(205,92,92,.7)");
						}
						if (item.equals(Transfer.Status.POTENTIAL)) {
							// lightskyblue
							currentRow.setStyle("-fx-background-color:rgba(135, 206, 250,.7)");
						}
					}
				}
			};
		});

		direction.setCellValueFactory(new PropertyValueFactory<>("direction"));

		purpose.setCellValueFactory(new PropertyValueFactory<>("purpose"));

		book.setCellValueFactory(new PropertyValueFactory<>("book"));

		currency.setCellValueFactory(new Callback<CellDataFeatures<Transfer, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Transfer, String> t) {
				String currency = StringUtils.EMPTY;
				if (t.getValue().getType().equals(Transfer.Type.CASH)) {
					currency = ((CashTransfer) t.getValue()).getCurrency().toString();
				}
				return new ReadOnlyObjectWrapper<String>(currency);
			}
		});

		tradeId.setCellValueFactory(new Callback<CellDataFeatures<Transfer, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Transfer, String> t) {
				String tradeId = StringUtils.EMPTY;
				if (t.getValue().getTrade() != null) {
					tradeId = Long.toString(t.getValue().getTrade().getId());
				}
				return new ReadOnlyObjectWrapper<String>(tradeId);
			}
		});

		productId.setCellValueFactory(new Callback<CellDataFeatures<Transfer, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Transfer, String> t) {
				String productId = StringUtils.EMPTY;
				if (t.getValue().getProduct() != null) {
					productId = Long.toString(t.getValue().getProduct().getId());
				}
				return new ReadOnlyObjectWrapper<String>(productId);
			}
		});

		quantity.setCellValueFactory(new Callback<CellDataFeatures<Transfer, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Transfer, String> t) {
				String quantity = StringUtils.EMPTY;
				BigDecimal amountOrQuantity;
				if (t.getValue().getType().equals(Transfer.Type.CASH)) {
					amountOrQuantity = ((CashTransfer) t.getValue()).getAmount();
				} else {
					amountOrQuantity = ((ProductTransfer) t.getValue()).getQuantity();
				}
				if (amountOrQuantity == null) {
					quantity = StringUtils.EMPTY;
				} else {
					quantity = TradistaGUIUtil.formatAmount(amountOrQuantity);
				}
				return new ReadOnlyObjectWrapper<String>(quantity);
			}
		});

		TradistaGUIUtil.fillComboBox(Arrays.asList(Transfer.Type.values()).stream().map(Object::toString).toList(),
				typeComboBox);
		typeComboBox.getItems().add(0, StringUtils.EMPTY);
		typeComboBox.getSelectionModel().selectFirst();

		typeComboBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (newValue != null) {
					boolean isCashOrAll = (newValue.equals(StringUtils.EMPTY)
							|| newValue.equals(Transfer.Type.CASH.toString()));
					currencyComboBox.setVisible(isCashOrAll);
					currencyLabel.setVisible(isCashOrAll);
				}
			}
		});

		TradistaGUIUtil.fillComboBox(Arrays.asList(Transfer.Status.values()).stream().map(Object::toString).toList(),
				statusComboBox);
		statusComboBox.getItems().add(0, StringUtils.EMPTY);
		statusComboBox.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillComboBox(Arrays.asList(Transfer.Direction.values()).stream().map(Object::toString).toList(),
				directionComboBox);
		directionComboBox.getItems().add(0, StringUtils.EMPTY);
		directionComboBox.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillComboBox(Arrays.asList(TransferPurpose.values()).stream().map(Object::toString).toList(),
				purposeComboBox);
		purposeComboBox.getItems().add(0, StringUtils.EMPTY);
		purposeComboBox.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillCurrencyComboBox(currencyComboBox);
		currencyComboBox.getItems().add(0, BlankCurrency.getInstance());
		currencyComboBox.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillBookComboBox(bookComboBox);
		bookComboBox.getItems().add(0, BlankBook.getInstance());
		bookComboBox.getSelectionModel().selectFirst();
	}

	@FXML
	protected void load() {
		ObservableList<Transfer> data = null;

		if (!idTextField.getText().isEmpty()) {
			try {
				Transfer transfer = transferBusinessDelegate.getTransferById(Long.parseLong(idTextField.getText()));
				if (transfer != null) {
					data = FXCollections.observableArrayList(transfer);
				}
				report.setItems(data);
				report.refresh();
			} catch (NumberFormatException e) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
						String.format("The transfer id: %s is incorrect.", idTextField.getText()));
				alert.showAndWait();
			} catch (TradistaBusinessException abe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
				alert.showAndWait();
			}

		} else {

			if (typeComboBox.getValue().isEmpty() && statusComboBox.getValue().isEmpty()
					&& directionComboBox.getValue().isEmpty() && purposeComboBox.getValue().isEmpty()
					&& tradeIdTextField.getText().isEmpty() && idTextField.getText().isEmpty()
					&& productIdTextField.getText().isEmpty() && bookComboBox.getValue().equals(BlankBook.getInstance())
					&& currencyComboBox.getValue().equals(BlankCurrency.getInstance())
					&& fixingDateFromDatePicker.getValue() == null && fixingDateToDatePicker.getValue() == null
					&& settlementDateFromDatePicker.getValue() == null && settlementDateToDatePicker.getValue() == null
					&& creationDateFromDatePicker.getValue() == null && creationDateToDatePicker.getValue() == null) {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Load Transfers");
				confirmation.setHeaderText("Load Transfers");
				confirmation.setContentText(
						"You are loading all the transfers present in the system, it can take time. Are you sure to continue?");

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
		ObservableList<Transfer> data = null;
		try {
			String productIdString = productIdTextField.getText();
			String tradeIdString = tradeIdTextField.getText();
			long productId = 0;
			long tradeId = 0;
			long currencyId = 0;
			long bookId = 0;
			Transfer.Type type = null;
			Transfer.Status status = null;
			Transfer.Direction direction = null;
			TransferPurpose purpose = null;
			if (!StringUtils.isEmpty(productIdString)) {
				try {
					productId = Long.parseLong(productIdString);
				} catch (NumberFormatException nfe) {
					throw new TradistaBusinessException(
							String.format("The product id: %s is incorrect.", productIdString));
				}
			}
			if (!StringUtils.isEmpty(tradeIdString)) {
				try {
					tradeId = Long.parseLong(tradeIdString);
				} catch (NumberFormatException nfe) {
					throw new TradistaBusinessException(String.format("The trade id: %s is incorrect.", tradeIdString));
				}
			}
			if (!StringUtils.isEmpty(typeComboBox.getValue())) {
				type = Transfer.Type.getType(typeComboBox.getValue());
			}
			if (!StringUtils.isEmpty(statusComboBox.getValue())) {
				status = Transfer.Status.getStatus(statusComboBox.getValue());
			}
			if (!StringUtils.isEmpty(directionComboBox.getValue())) {
				direction = Transfer.Direction.getDirection(directionComboBox.getValue());
			}
			if (!StringUtils.isEmpty(purposeComboBox.getValue())) {
				purpose = TransferPurpose.getTransferPurpose(purposeComboBox.getValue());
			}
			if (!currencyComboBox.getValue().equals(BlankCurrency.getInstance())) {
				currencyId = currencyComboBox.getValue().getId();
			}
			if (!bookComboBox.getValue().equals(BlankBook.getInstance())) {
				bookId = bookComboBox.getValue().getId();
			}
			List<Transfer> transfers = transferBusinessDelegate.getTransfers(type, status, direction, purpose, tradeId,
					productId, bookId, currencyId, fixingDateFromDatePicker.getValue(),
					fixingDateToDatePicker.getValue(), settlementDateFromDatePicker.getValue(),
					settlementDateToDatePicker.getValue(), creationDateFromDatePicker.getValue(),
					creationDateToDatePicker.getValue());
			if (transfers != null) {
				data = FXCollections.observableArrayList(transfers);
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
			TradistaGUIUtil.export(report, "Transfers", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}