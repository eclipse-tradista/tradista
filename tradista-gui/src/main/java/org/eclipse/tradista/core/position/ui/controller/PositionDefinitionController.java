package org.eclipse.tradista.core.position.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.BlankLegalEntity;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.product.model.BlankProduct;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

public class PositionDefinitionController extends TradistaControllerAdapter {

	@FXML
	private TextField name;

	@FXML
	private Label nameLabel;

	@FXML
	private ComboBox<Book> book;

	@FXML
	private ComboBox<String> productType;

	@FXML
	private ComboBox<Product> product;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private CheckBox isRealTime;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private ComboBox<PricingParameter> pricingParameter;

	@FXML
	private ComboBox<PositionDefinition> load;

	@FXML
	private Button copyButton;

	private PositionDefinition positionDefinition;

	private BookBusinessDelegate bookBusinessDelegate;

	private ProductBusinessDelegate productBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PositionDefinitionBusinessDelegate positionBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		bookBusinessDelegate = new BookBusinessDelegate();
		productBusinessDelegate = new ProductBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		positionBusinessDelegate = new PositionDefinitionBusinessDelegate();

		copyButton.setDisable(true);
		List<LegalEntity> legalEntities = new ArrayList<>();
		TradistaGUIUtil.fillComboBox(productBusinessDelegate.getAvailableProductTypes(), productType);
		productType.getItems().add(0, StringUtils.EMPTY);
		productType.getSelectionModel().selectFirst();
		productType.valueProperty().addListener((_, _, newValue) -> {
			if (StringUtils.isEmpty(newValue)) {
				product.getItems().clear();
			} else {
				try {
					Set<? extends Product> products = productBusinessDelegate.getAllProductsByType(newValue);
					if (products != null) {
						product.setItems(FXCollections.observableArrayList(products));
						product.getItems().add(0, BlankProduct.getInstance());
						product.getSelectionModel().selectFirst();
					} else {
						product.getItems().clear();
					}
				} catch (TradistaBusinessException _) {
					// Should not happen as values of product types are good
					// ones.
				}
			}
		});
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		legalEntities.addAll(legalEntityBusinessDelegate.getAllCounterparties());
		legalEntities.add(0, BlankLegalEntity.getInstance());
		TradistaGUIUtil.fillComboBox(legalEntities, counterparty);
		counterparty.getSelectionModel().selectFirst();
		TradistaGUIUtil.fillCurrencyComboBox(currency);
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillPositionDefinitionComboBox(false, load);
	}

	@FXML
	protected void save() {
		boolean isNew = name.isVisible();
		LegalEntity po = null;
		boolean proceed = false;

		if (ClientUtil.currentUserIsAdmin() && isNew) {
			TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog("Position Definition",
					ClientUtil.getCurrentProcessingOrg(), false);
			Optional<LegalEntity> result = dialog.showAndWait();
			if (result.isPresent()) {
				po = result.get();
				proceed = true;
			}
		} else {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Save Position Definition");
			confirmation.setHeaderText("Save Position Definition");
			confirmation.setContentText("Do you want to save this Position Definition?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				proceed = true;
				po = ClientUtil.getCurrentUser().getProcessingOrg();
			}
		}

		if (proceed) {
			try {
				if (isNew) {
					positionDefinition = new PositionDefinition(name.getText(), po);
					nameLabel.setText(name.getText());
				}

				positionDefinition.setBook(book.getValue());
				if (!counterparty.getValue().equals(BlankLegalEntity.getInstance())) {
					positionDefinition.setCounterparty(counterparty.getValue());
				} else {
					positionDefinition.setCounterparty(null);
				}
				positionDefinition.setCurrency(currency.getValue());
				positionDefinition.setPricingParameter(pricingParameter.getValue());
				if (product.getValue() != null && !product.getValue().equals(BlankProduct.getInstance())) {
					positionDefinition.setProduct(product.getValue());
				} else {
					positionDefinition.setProduct(null);
				}
				if (productType.getValue() != null && !productType.getValue().equals(StringUtils.EMPTY)) {
					positionDefinition.setProductType(productType.getValue());
				} else {
					positionDefinition.setProductType(null);
				}
				positionDefinition.setRealTime(isRealTime.isSelected());
				positionDefinition.setId(positionBusinessDelegate.savePositionDefinition(positionDefinition));
				name.setVisible(false);
				nameLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		String copyName = null;
		LegalEntity po = null;
		boolean proceed = false;

		if (ClientUtil.currentUserIsAdmin()) {
			TradistaCopyDialog dialog = new TradistaCopyDialog("Position Definition",
					positionDefinition.getProcessingOrg(), nameLabel.getText(), false);
			Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
			if (result.isPresent()) {
				copyName = result.get().getName();
				po = result.get().getProcessingOrg();
				proceed = true;
			}
		} else {
			TradistaTextInputDialog dialog = new TradistaTextInputDialog();
			dialog.setTitle("Position Definition Copy");
			dialog.setHeaderText("Do you want to copy this Position Definition ?");
			dialog.setContentText("Please enter the name of the new Position Definition:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				copyName = result.get();
				po = ClientUtil.getCurrentUser().getProcessingOrg();
				proceed = true;
			}
		}

		if (proceed) {
			try {
				PositionDefinition copyPositionDefinition = new PositionDefinition(copyName, po);
				copyPositionDefinition.setBook(book.getValue());
				if (!counterparty.getValue().equals(BlankLegalEntity.getInstance())) {
					copyPositionDefinition.setCounterparty(counterparty.getValue());
				}
				copyPositionDefinition.setCurrency(currency.getValue());
				copyPositionDefinition.setPricingParameter(pricingParameter.getValue());
				if (product.getValue() != null && !product.getValue().equals(BlankProduct.getInstance())) {
					copyPositionDefinition.setProduct(product.getValue());
				}
				if (productType.getValue() != null && !productType.getValue().equals(StringUtils.EMPTY)) {
					copyPositionDefinition.setProductType(productType.getValue());
				}
				copyPositionDefinition.setRealTime(isRealTime.isSelected());
				copyPositionDefinition.setId(positionBusinessDelegate.savePositionDefinition(copyPositionDefinition));
				positionDefinition = copyPositionDefinition;
				name.setVisible(false);
				nameLabel.setVisible(true);
				nameLabel.setText(positionDefinition.getName());
				TradistaGUIUtil.fillPositionDefinitionComboBox(false, load);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		try {

			if (load.getValue() != null) {
				load(load.getValue());
			} else {
				throw new TradistaBusinessException("Please choose a name.");
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(PositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
		book.setValue(positionDefinition.getBook());
		if (positionDefinition.getCounterparty() != null) {
			counterparty.setValue(positionDefinition.getCounterparty());
		} else {
			counterparty.setValue(BlankLegalEntity.getInstance());
		}
		currency.setValue(positionDefinition.getCurrency());
		if (positionDefinition.getProductType() != null) {
			productType.setValue(positionDefinition.getProductType());
		} else {
			productType.setValue(StringUtils.EMPTY);
		}
		isRealTime.setSelected(positionDefinition.isRealTime());
		pricingParameter.setValue(positionDefinition.getPricingParameter());
		if (positionDefinition.getProduct() != null) {
			product.setValue(positionDefinition.getProduct());
		} else {
			product.setValue(BlankProduct.getInstance());
		}
		name.setVisible(false);
		nameLabel.setText(positionDefinition.getName());
		nameLabel.setVisible(true);
		copyButton.setDisable(false);
	}

	@Override
	@FXML
	public void clear() {
		positionDefinition = null;
		name.clear();
		book.setValue(null);
		counterparty.setValue(BlankLegalEntity.getInstance());
		currency.setValue(null);
		isRealTime.setSelected(false);
		pricingParameter.setValue(null);
		product.setValue(BlankProduct.getInstance());
		productType.setValue(StringUtils.EMPTY);
		nameLabel.setText(StringUtils.EMPTY);
		name.setVisible(true);
		nameLabel.setVisible(false);
		copyButton.setDisable(true);
	}

	@FXML
	public void refresh() {
		List<LegalEntity> legalEntities = new ArrayList<>();
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		legalEntities.add(BlankLegalEntity.getInstance());
		legalEntities.addAll(legalEntityBusinessDelegate.getAllLegalEntities());
		TradistaGUIUtil.fillComboBox(legalEntities, counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currency);
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillPositionDefinitionComboBox(false, load);
		if (!StringUtils.isEmpty(productType.getValue())) {
			Set<? extends Product> products;
			try {
				products = productBusinessDelegate.getAllProductsByType(productType.getValue());
				if (products != null) {
					List<Product> productsList = new ArrayList<>();
					productsList.add(BlankProduct.getInstance());
					productsList.addAll(products);
					TradistaGUIUtil.fillComboBox(productsList, product);
				} else {
					product.getItems().clear();
				}
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		}
	}

}