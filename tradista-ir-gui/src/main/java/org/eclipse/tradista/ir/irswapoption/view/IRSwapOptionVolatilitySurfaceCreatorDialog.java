package org.eclipse.tradista.ir.irswapoption.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
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

public class IRSwapOptionVolatilitySurfaceCreatorDialog extends TradistaDialog<SwaptionVolatilitySurface> {

	public IRSwapOptionVolatilitySurfaceCreatorDialog() {
		super();
		setTitle("Swaption Volatility Surface Creation");
		setHeaderText("Please specify a name for the Swaption Volatility Surface to create.");
		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		getDialogPane().setContent(grid);

		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, SwaptionVolatilitySurface>() {
			@Override
			public SwaptionVolatilitySurface call(ButtonType b) {
				if (b == buttonTypeOk) {
					SwaptionVolatilitySurface surface = new SwaptionVolatilitySurface(nameTextField.getText(),
							ClientUtil.getCurrentUser().getProcessingOrg());
					return surface;
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}