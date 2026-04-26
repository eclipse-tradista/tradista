package org.eclipse.tradista.security.equityoption.ui.view;

import static org.eclipse.tradista.security.equityoption.ui.util.EquityOptionUIConstants.STRIKE_PRICE_RATIO;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.BlankLegalEntity;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.ui.controller.EquityOptionVolatilitySurfacesController;
import org.eclipse.tradista.security.equityoption.ui.controller.EquityOptionVolatilitySurfacesController.StrikeProperty;

import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

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

public class EquityOptionVolatilitySurfaceCreatorDialog extends TradistaDialog<EquityOptionVolatilitySurface> {

	public EquityOptionVolatilitySurfaceCreatorDialog() {
		super();
		String entityName = "Equity Option Volatility Surface";
		setTitle(String.format("%s Creation", entityName));
		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField();
		Label addDeltaLabel = new Label("Add a Strike/Price ratio: ");
		TextField addStrikePriceRatioTextField = new TextField();
		TableView<StrikeProperty> selectedStrikePriceRatios = new TableView<>();
		TableColumn<StrikeProperty, String> strikePriceRatioValue = new TableColumn<>();
		selectedStrikePriceRatios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		strikePriceRatioValue.setText(STRIKE_PRICE_RATIO);
		selectedStrikePriceRatios.getColumns().add(strikePriceRatioValue);
		strikePriceRatioValue.setCellValueFactory(cellData -> cellData.getValue().getValue());

		Label poLabel = new Label("Processing Org: ");
		ComboBox<LegalEntity> poComboBox = new ComboBox<>();
		TradistaGUIUtil.fillProcessingOrgComboBox(poComboBox);
		boolean isAdmin = ClientUtil.currentUserIsAdmin();
		if (!isAdmin) {
			poComboBox.getItems().add(0, BlankLegalEntity.getInstance());
		}
		poComboBox.getSelectionModel().selectFirst();

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
		grid.add(addDeltaLabel, 1, 3);
		grid.add(addStrikePriceRatioTextField, 2, 3);
		grid.add(selectedStrikePriceRatios, 1, 4);

		if (isAdmin) {
			grid.add(poLabel, 1, 5);
			grid.add(poComboBox, 2, 5);
		}

		GridPane buttonsGrid = new GridPane();
		Button add = new Button("Add");
		Button delete = new Button("Delete");
		buttonsGrid.add(add, 1, 1);
		buttonsGrid.add(delete, 1, 2);
		buttonsGrid.setStyle("-fx-vgap: 20;");
		grid.add(buttonsGrid, 2, 4);
		getDialogPane().setContent(grid);
		getDialogPane().setMinWidth(500);

		delete.setOnAction(_ -> selectedStrikePriceRatios.getItems()
				.remove(selectedStrikePriceRatios.getSelectionModel().getSelectedItem()));

		add.setOnAction(_ -> {
			try {
				BigDecimal strikePriceRatio;

				strikePriceRatio = TradistaGUIUtil.parseAmount(addStrikePriceRatioTextField.getText(),
						STRIKE_PRICE_RATIO);

				boolean strikeExists = false;
				if (selectedStrikePriceRatios.getItems() != null && !selectedStrikePriceRatios.getItems().isEmpty()) {
					for (StrikeProperty prop : selectedStrikePriceRatios.getItems()) {
						if (TradistaGUIUtil.parseAmount(prop.getValue().getValue(), STRIKE_PRICE_RATIO)
								.compareTo(TradistaGUIUtil.parseAmount(addStrikePriceRatioTextField.getText(),
										STRIKE_PRICE_RATIO)) == 0) {
							strikeExists = true;
							break;
						}
					}
				}
				if (!strikeExists) {
					selectedStrikePriceRatios.getItems()
							.add(new StrikeProperty(TradistaGUIUtil.formatAmount(strikePriceRatio)));
				}
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();

			}
		});

		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(b -> {
			if (b == buttonTypeOk) {
				try {
					LegalEntity po = (poComboBox.getValue() instanceof BlankLegalEntity) ? null : poComboBox.getValue();
					EquityOptionVolatilitySurface surface = new EquityOptionVolatilitySurface(nameTextField.getText(),
							po);
					surface.setStrikes(EquityOptionVolatilitySurfacesController
							.toStrikeList(selectedStrikePriceRatios.getItems()));
					return surface;
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
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