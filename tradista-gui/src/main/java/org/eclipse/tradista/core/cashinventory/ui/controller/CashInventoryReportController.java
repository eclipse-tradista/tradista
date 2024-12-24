package org.eclipse.tradista.core.cashinventory.ui.controller;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.BlankBook;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashinventory.service.CashInventoryBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.currency.model.BlankCurrency;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.inventory.model.CashInventory;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
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

public class CashInventoryReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker valueDateFromDatePicker;

	@FXML
	private DatePicker valueDateToDatePicker;

	@FXML
	private ComboBox<Book> bookComboBox;

	@FXML
	private ComboBox<Currency> currencyComboBox;

	@FXML
	private CheckBox openPositionsCheckBox;

	@FXML
	private TableView<CashInventory> report;

	@FXML
	private TableColumn<CashInventory, String> currency;

	@FXML
	private TableColumn<CashInventory, String> book;

	@FXML
	private TableColumn<CashInventory, String> amount;

	@FXML
	private TableColumn<CashInventory, String> from;

	@FXML
	private TableColumn<CashInventory, String> to;

	private CashInventoryBusinessDelegate cashInventoryBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		from.setCellValueFactory(new Callback<CellDataFeatures<CashInventory, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<CashInventory, String> inv) {
				return new ReadOnlyObjectWrapper<String>(inv.getValue().getFrom().toString());
			}
		});

		to.setCellValueFactory(new Callback<CellDataFeatures<CashInventory, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<CashInventory, String> inv) {
				LocalDate to = inv.getValue().getTo();
				if (to != null) {
					return new ReadOnlyObjectWrapper<String>(to.toString());
				}
				return new ReadOnlyObjectWrapper<String>(StringUtils.EMPTY);
			}
		});

		book.setCellValueFactory(new Callback<CellDataFeatures<CashInventory, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<CashInventory, String> inv) {
				return new ReadOnlyObjectWrapper<String>(inv.getValue().getBook().getName());
			}
		});

		currency.setCellValueFactory(new Callback<CellDataFeatures<CashInventory, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<CashInventory, String> inv) {
				return new ReadOnlyObjectWrapper<String>(inv.getValue().getCurrency().getIsoCode());
			}
		});

		amount.setCellValueFactory(new Callback<CellDataFeatures<CashInventory, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<CashInventory, String> p) {
				return new ReadOnlyObjectWrapper<String>(TradistaGUIUtil.formatAmount(p.getValue().getAmount()));
			}
		});

		TradistaGUIUtil.fillBookComboBox(bookComboBox);
		bookComboBox.getItems().add(0, BlankBook.getInstance());
		bookComboBox.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillCurrencyComboBox(currencyComboBox);
		currencyComboBox.getItems().add(0, BlankCurrency.getInstance());
		currencyComboBox.getSelectionModel().selectFirst();

		openPositionsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					CashInventoryReportController.this.valueDateToDatePicker.setValue(null);
					CashInventoryReportController.this.valueDateToDatePicker.setDisable(true);
				} else {
					CashInventoryReportController.this.valueDateToDatePicker.setDisable(false);
				}

			}
		});

		cashInventoryBusinessDelegate = new CashInventoryBusinessDelegate();

	}

	@FXML
	protected void load() {
		if (currencyComboBox.getValue().equals(BlankCurrency.getInstance())
				&& bookComboBox.getValue().equals(BlankBook.getInstance()) && valueDateFromDatePicker.getValue() == null
				&& valueDateToDatePicker.getValue() == null && !openPositionsCheckBox.isSelected()) {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Load Cash Inventories");
			confirmation.setHeaderText("Load Cash Inventories");
			confirmation.setContentText(
					"You are loading all the cash inventories present in the system, it can take time. Are you sure to continue?");
			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fillReport();
			}
		} else {
			fillReport();
		}
	}

	private void fillReport() {
		ObservableList<CashInventory> data = null;
		Set<CashInventory> inventories;
		long currencyId = 0;
		long bookId = 0;

		try {
			if (!currencyComboBox.getValue().equals(BlankCurrency.getInstance())) {
				currencyId = currencyComboBox.getValue().getId();
			}
			if (!bookComboBox.getValue().equals(BlankBook.getInstance())) {
				bookId = bookComboBox.getValue().getId();
			}
			inventories = cashInventoryBusinessDelegate.getCashInventories(valueDateFromDatePicker.getValue(),
					valueDateToDatePicker.getValue(), currencyId, bookId, openPositionsCheckBox.isSelected());

			if (inventories != null) {
				data = FXCollections.observableArrayList(inventories);
			} else {
				data = FXCollections.emptyObservableList();
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
			TradistaGUIUtil.export(report, "CashInventories", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}