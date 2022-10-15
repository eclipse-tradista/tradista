package finance.tradista.fx.fxoption.ui.view;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.ui.util.TradistaGUIUtil;
import finance.tradista.core.common.ui.view.TradistaAlert;
import finance.tradista.core.common.ui.view.TradistaDialog;
import finance.tradista.core.common.util.ClientUtil;
import finance.tradista.fx.fxoption.model.FXVolatilitySurface;
import finance.tradista.fx.fxoption.ui.controller.FXVolatilitySurfacesController;
import finance.tradista.fx.fxoption.ui.controller.FXVolatilitySurfacesController.DeltaProperty;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/*
 * Copyright 2016 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class FXVolatilitySurfaceCreatorDialog extends TradistaDialog<FXVolatilitySurface> {

	public FXVolatilitySurfaceCreatorDialog() {
		super();
		setTitle("FX Volatility Surface Creation");
		setHeaderText("Please specify a name and delta ratios for the FX Volatility Surface to create.");
		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField();
		Label addDeltaLabel = new Label("Add a delta: ");
		TextField addDeltaTextField = new TextField();
		TableView<DeltaProperty> selectedDeltas = new TableView<DeltaProperty>();
		TableColumn<DeltaProperty, String> deltaValue = new TableColumn<DeltaProperty, String>();
		selectedDeltas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		deltaValue.setText("Delta");
		selectedDeltas.getColumns().add(deltaValue);
		deltaValue.setCellValueFactory(cellData -> cellData.getValue().getValue());
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(addDeltaLabel, 1, 2);
		grid.add(addDeltaTextField, 2, 2);
		grid.add(selectedDeltas, 1, 3);

		GridPane buttonsGrid = new GridPane();
		Button add = new Button("Add");
		Button delete = new Button("Delete");
		buttonsGrid.add(add, 1, 1);
		buttonsGrid.add(delete, 1, 2);
		buttonsGrid.setStyle("-fx-vgap: 20;");
		grid.add(buttonsGrid, 2, 3);
		getDialogPane().setContent(grid);

		delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				selectedDeltas.getItems().remove(selectedDeltas.getSelectionModel().getSelectedItem());
			}
		});

		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					if (!StringUtils.isBlank(addDeltaTextField.getText())) {
						BigDecimal delta = TradistaGUIUtil.parseAmount(addDeltaTextField.getText(), "Delta");
						boolean deltaExists = false;
						if (selectedDeltas.getItems() != null && !selectedDeltas.getItems().isEmpty()) {
							for (DeltaProperty prop : selectedDeltas.getItems()) {
								if (TradistaGUIUtil.parseAmount(prop.getValue(), "Delta").compareTo(delta) == 0) {
									deltaExists = true;
									break;
								}
							}
						}
						if (!deltaExists) {
							selectedDeltas.getItems().add(new DeltaProperty(addDeltaTextField.getText()));
						}
					}
				} catch (TradistaBusinessException abe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
					alert.showAndWait();
				}
			}
		});

		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, FXVolatilitySurface>() {
			@Override
			public FXVolatilitySurface call(ButtonType b) {
				if (b == buttonTypeOk) {
					try {
						FXVolatilitySurface surface = new FXVolatilitySurface(nameTextField.getText());
						surface.setProcessingOrg(ClientUtil.getCurrentUser().getProcessingOrg());
						surface.setDeltas(FXVolatilitySurfacesController.toDeltaList(selectedDeltas.getItems()));

						return surface;
					} catch (TradistaBusinessException abe) {
						TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
						alert.showAndWait();
					}
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}