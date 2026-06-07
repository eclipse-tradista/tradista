package org.eclipse.tradista.core.productinventory.ui.controller;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.BlankBook;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.product.model.BlankProduct;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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

public class ProductInventoryReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker valueDateFromDatePicker;

	@FXML
	private DatePicker valueDateToDatePicker;

	@FXML
	private ComboBox<String> productTypeComboBox;

	@FXML
	private ComboBox<Book> bookComboBox;

	@FXML
	private ComboBox<Product> productComboBox;

	@FXML
	private CheckBox openPositionsCheckBox;

	@FXML
	private TableView<ProductInventory> report;

	@FXML
	private TableColumn<ProductInventory, String> productType;

	@FXML
	private TableColumn<ProductInventory, String> book;

	@FXML
	private TableColumn<ProductInventory, String> productId;

	@FXML
	private TableColumn<ProductInventory, String> quantity;

	@FXML
	private TableColumn<ProductInventory, String> from;

	@FXML
	private TableColumn<ProductInventory, String> to;

	@FXML
	private TableColumn<ProductInventory, String> averagePrice;

	private ProductBusinessDelegate productBusinessDelegate;

	private ProductInventoryBusinessDelegate inventoryBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		productBusinessDelegate = new ProductBusinessDelegate();

		inventoryBusinessDelegate = new ProductInventoryBusinessDelegate();

		from.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getFrom().toString()));

		to.setCellValueFactory(cellData -> {
			LocalDate to = cellData.getValue().getTo();
			if (to != null) {
				return new ReadOnlyObjectWrapper<>(to.toString());
			}
			return new ReadOnlyObjectWrapper<>(StringUtils.EMPTY);
		});

		book.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBook().getName()));

		productType.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getProductType()));

		productId.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(Long.toString(cellData.getValue().getProduct().getId())));

		quantity.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
				TradistaGUIUtil.formatAmount(cellData.getValue().getQuantity())));

		averagePrice.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
				TradistaGUIUtil.formatAmount(cellData.getValue().getAveragePrice())));

		TradistaGUIUtil.fillComboBox(productBusinessDelegate.getAvailableListableProductTypes(), productTypeComboBox);
		if (!productTypeComboBox.getItems().isEmpty()) {
			productTypeComboBox.getItems().addFirst(StringUtils.EMPTY);
			productTypeComboBox.getSelectionModel().selectFirst();
		}
		TradistaGUIUtil.fillComboBox(new BookBusinessDelegate().getAllBooks(), bookComboBox);
		bookComboBox.getItems().addFirst(BlankBook.getInstance());
		bookComboBox.getSelectionModel().selectFirst();

		productTypeComboBox.valueProperty().addListener((_, _, v) -> {
			if (v != null) {
				if (StringUtils.isEmpty(v)) {
					productComboBox.getItems().clear();
				} else {
					try {
						Set<? extends Product> products = productBusinessDelegate.getAllProductsByType(v);
						if (products != null) {
							productComboBox.setItems(FXCollections.observableArrayList(products));
							productComboBox.getItems().addFirst(BlankProduct.getInstance());
						} else {
							productComboBox.setItems(FXCollections.emptyObservableList());
						}
						productComboBox.getSelectionModel().selectFirst();
					} catch (TradistaBusinessException _) {
						// Cannot happen here.
					}
				}
			}
		});

		openPositionsCheckBox.selectedProperty().addListener((_, _, v) -> {
			if (v.booleanValue()) {
				ProductInventoryReportController.this.valueDateToDatePicker.setValue(null);
			}
			ProductInventoryReportController.this.valueDateToDatePicker.setDisable(v.booleanValue());
		});
	}

	@FXML
	protected void load() {
		if ((productComboBox.getValue() == null || productComboBox.getValue().equals(BlankProduct.getInstance()))
				&& (bookComboBox.getValue() == null || bookComboBox.getValue().equals(BlankBook.getInstance()))
				&& valueDateFromDatePicker.getValue() == null && valueDateToDatePicker.getValue() == null
				&& !openPositionsCheckBox.isSelected() && StringUtils.isEmpty(productTypeComboBox.getValue())) {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Load Inventories");
			confirmation.setHeaderText("Load Inventories");
			confirmation.setContentText(
					"You are loading all the inventories present in the system, it can take time. Are you sure to continue?");
			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fillReport();
			}
		} else {
			fillReport();
		}
	}

	private void fillReport() {
		ObservableList<ProductInventory> data = null;
		Set<ProductInventory> inventories;
		long productId = 0;
		long bookId = 0;

		try {
			if (productComboBox.getValue() != null) {
				productId = productComboBox.getValue().getId();
			}
			if (bookComboBox.getValue() != null) {
				bookId = bookComboBox.getValue().getId();
			}
			inventories = inventoryBusinessDelegate.getProductInventories(valueDateFromDatePicker.getValue(),
					valueDateToDatePicker.getValue(), productTypeComboBox.getValue(), productId, bookId,
					openPositionsCheckBox.isSelected());

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
			TradistaGUIUtil.export(report, "ProductInventories", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}