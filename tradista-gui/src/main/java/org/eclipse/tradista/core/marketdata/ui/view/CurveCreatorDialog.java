package org.eclipse.tradista.core.marketdata.ui.view;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;
import org.eclipse.tradista.core.marketdata.service.CurveBusinessDelegate;

import javafx.collections.FXCollections;
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

public class CurveCreatorDialog extends TradistaDialog<Curve<LocalDate, BigDecimal>> {

	public CurveCreatorDialog() {
		super();
		setTitle("Curve Creation");
		setHeaderText("Please specify a name and a type for the Curve to create.");
		Label nameLabel = new Label("Name: ");
		TextField nameTextField = new TextField();
		Label typeLabel = new Label("Type: ");
		ComboBox<String> typeComboBox = new ComboBox<String>();
		typeComboBox.setItems(FXCollections.observableArrayList(new CurveBusinessDelegate().getAllCurveTypes()));
		typeComboBox.getSelectionModel().selectFirst();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(typeLabel, 1, 2);
		grid.add(typeComboBox, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, Curve<LocalDate, BigDecimal>>() {
			@Override
			public Curve<LocalDate, BigDecimal> call(ButtonType b) {
				if (b == buttonTypeOk) {
					if (typeComboBox.getValue().equals(InterestRateCurve.INTEREST_RATE_CURVE)) {
						return new InterestRateCurve(nameTextField.getText(),
								ClientUtil.getCurrentUser().getProcessingOrg());
					} else if (typeComboBox.getValue().equals(ZeroCouponCurve.ZERO_COUPON_CURVE)) {
						return new ZeroCouponCurve(nameTextField.getText(),
								ClientUtil.getCurrentUser().getProcessingOrg());
					} else {
						return new FXCurve(nameTextField.getText(), ClientUtil.getCurrentUser().getProcessingOrg());
					}
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}
