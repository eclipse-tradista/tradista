package org.eclipse.tradista.core.batch.ui.controller;

import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
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

public class JobInstanceCreatorDialog extends TradistaDialog<TradistaJobInstance> {

	public JobInstanceCreatorDialog() {
		super();

		setTitle("Job Instance Creation");

		setHeaderText("Please specify a name and a job type for the Job Instance to create.");
		setResizable(false);
		Label nameLabel = new Label("Name: ");
		Label quoteSetLabel = new Label("Job type: ");
		TextField nameTextField = new TextField();
		ComboBox<String> jobTypeComboBox = new ComboBox<String>();
		ObservableList<String> jobTypes = FXCollections
				.observableArrayList(new BatchBusinessDelegate().getAllJobTypes());
		jobTypeComboBox.setItems(jobTypes);
		jobTypeComboBox.getSelectionModel().selectFirst();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(quoteSetLabel, 1, 2);
		grid.add(jobTypeComboBox, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, TradistaJobInstance>() {
			@Override
			public TradistaJobInstance call(ButtonType b) {
				if (b == buttonTypeOk) {
					return new TradistaJobInstance(nameTextField.getText(), jobTypeComboBox.getValue(),
							ClientUtil.getCurrentUser().getProcessingOrg());
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}
