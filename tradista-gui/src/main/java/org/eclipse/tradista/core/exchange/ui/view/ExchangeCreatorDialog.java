package org.eclipse.tradista.core.exchange.ui.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.exchange.model.Exchange;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class ExchangeCreatorDialog extends TradistaDialog<Exchange> {

	public ExchangeCreatorDialog() {
		super();
		setTitle("Exchange Copy");
		setHeaderText("Please specify code and name for the new Exchange.");
		Label codeLabel = new Label("Code: ");
		TextField codeTextField = new TextField();
		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(codeLabel, 1, 1);
		grid.add(codeTextField, 2, 1);
		grid.add(nameLabel, 1, 2);
		grid.add(nameTextField, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Copy", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<>() {
			@Override
			public Exchange call(ButtonType b) {
				if (b == buttonTypeOk) {
					Exchange exchange = new Exchange(codeTextField.getText());
					exchange.setName(nameTextField.getText());
					return exchange;
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

}