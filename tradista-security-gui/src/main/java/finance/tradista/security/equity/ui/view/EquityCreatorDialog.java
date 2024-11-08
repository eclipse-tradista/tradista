package org.eclipse.tradista.security.equity.ui.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.equity.model.Equity;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
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

public class EquityCreatorDialog extends TradistaDialog<Equity> {

	public EquityCreatorDialog(Exchange exchange) {
		super();
		setTitle("Equity Copy");
		setHeaderText("Please specify ISIN and exchange for the new Equity.");
		Label isinLabel = new Label("ISIN: ");
		TextField isinTextField = new TextField();
		Label exchangeLabel = new Label("Exchange: ");
		ComboBox<Exchange> exchangeComboBox = new ComboBox<Exchange>();
		TradistaGUIUtil.fillExchangeComboBox(exchangeComboBox);
		exchangeComboBox.setValue(exchange);
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(isinLabel, 1, 1);
		grid.add(isinTextField, 2, 1);
		grid.add(exchangeLabel, 1, 2);
		grid.add(exchangeComboBox, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Copy", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, Equity>() {
			@Override
			public Equity call(ButtonType b) {
				if (b == buttonTypeOk) {
					Equity equity = new Equity(exchangeComboBox.getValue(), isinTextField.getText());
					return equity;
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}