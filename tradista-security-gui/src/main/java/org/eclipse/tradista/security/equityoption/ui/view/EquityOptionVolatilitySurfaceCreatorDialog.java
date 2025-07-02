package org.eclipse.tradista.security.equityoption.ui.view;

import static org.eclipse.tradista.security.equityoption.ui.util.EquityOptionUIConstants.STRIKE_PRICE_RATIO;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.ui.controller.EquityOptionVolatilitySurfacesController;
import org.eclipse.tradista.security.equityoption.ui.controller.EquityOptionVolatilitySurfacesController.StrikeProperty;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

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
		setTitle("Equity Option Volatility Surface Creation");
		setHeaderText(
				"Please specify a name and Strike/Price ratios for the Equity Option Volatility Surface to create.");
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
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(addDeltaLabel, 1, 2);
		grid.add(addStrikePriceRatioTextField, 2, 2);
		grid.add(selectedStrikePriceRatios, 1, 3);

		GridPane buttonsGrid = new GridPane();
		Button add = new Button("Add");
		Button delete = new Button("Delete");
		buttonsGrid.add(add, 1, 1);
		buttonsGrid.add(delete, 1, 2);
		buttonsGrid.setStyle("-fx-vgap: 20;");
		grid.add(buttonsGrid, 2, 3);
		getDialogPane().setContent(grid);

		delete.setOnAction(_ -> selectedStrikePriceRatios.getItems()
				.remove(selectedStrikePriceRatios.getSelectionModel().getSelectedItem()));

		add.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					BigDecimal strikePriceRatio;

					strikePriceRatio = TradistaGUIUtil.parseAmount(addStrikePriceRatioTextField.getText(),
							STRIKE_PRICE_RATIO);

					boolean strikeExists = false;
					if (selectedStrikePriceRatios.getItems() != null
							&& !selectedStrikePriceRatios.getItems().isEmpty()) {
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
			}
		});

		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<>() {
			@Override
			public EquityOptionVolatilitySurface call(ButtonType b) {
				if (b == buttonTypeOk) {
					try {
						EquityOptionVolatilitySurface surface = new EquityOptionVolatilitySurface(
								nameTextField.getText(), ClientUtil.getCurrentUser().getProcessingOrg());
						surface.setStrikes(EquityOptionVolatilitySurfacesController
								.toStrikeList(selectedStrikePriceRatios.getItems()));
						return surface;
					} catch (TradistaBusinessException tbe) {
						TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
						alert.showAndWait();
					}
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

}