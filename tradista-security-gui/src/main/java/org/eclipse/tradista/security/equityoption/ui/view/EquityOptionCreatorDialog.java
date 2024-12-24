package org.eclipse.tradista.security.equityoption.ui.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
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

public class EquityOptionCreatorDialog extends TradistaDialog<EquityOption> {

	public EquityOptionCreatorDialog(EquityOption equityOption) {
		super();
		setTitle("Equity Option Copy");
		setHeaderText("Please specify the information of the new Equity Option.");
		Label codeLabel = new Label("Code: ");
		TextField codeTextField = new TextField();
		codeTextField.setText(equityOption.getCode());
		Label contractSpecificationLabel = new Label("Contract Specification: ");
		ComboBox<EquityOptionContractSpecification> contractSpecificationComboBox = new ComboBox<EquityOptionContractSpecification>();
		TradistaSecurityGUIUtil.fillEquityOptionContractSpecificationComboBox(contractSpecificationComboBox);
		DatePicker maturityDateDatePicker = new DatePicker();
		contractSpecificationComboBox.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EquityOptionContractSpecification>() {
					@Override
					public void changed(ObservableValue<? extends EquityOptionContractSpecification> arg0,
							EquityOptionContractSpecification arg1, EquityOptionContractSpecification newEocs) {
						if (newEocs != null) {
							maturityDateDatePicker.setValue(null);
							LocalDate startDate = LocalDate.now().minusYears(10);
							Set<LocalDate> maturityDates = new DateRuleBusinessDelegate()
									.generateDates(newEocs.getMaturityDatesDateRule(), startDate, Period.ofYears(100));
							final Callback<DatePicker, DateCell> maturityDayCellFactory = new Callback<DatePicker, DateCell>() {
								public DateCell call(final DatePicker datePicker) {
									return new DateCell() {

										private boolean isAvailable(LocalDate date) {
											if (contractSpecificationComboBox.getValue() != null) {
												return maturityDates.contains(date);
											}
											return true;
										}

										@Override
										public void updateItem(LocalDate item, boolean empty) {
											super.updateItem(item, empty);
											if (!isAvailable(item)) {
												setDisable(true);
											}
										}
									};
								}
							};
							maturityDateDatePicker.setDayCellFactory(maturityDayCellFactory);
						}
					}
				});
		contractSpecificationComboBox.setValue(equityOption.getEquityOptionContractSpecification());
		Label typeLabel = new Label("Type: ");
		ComboBox<OptionTrade.Type> typeComboBox = new ComboBox<OptionTrade.Type>();
		TradistaGUIUtil.fillOptionTypeComboBox(typeComboBox);
		typeComboBox.setValue(equityOption.getType());
		Label maturityDateLabel = new Label("Maturity Date: ");
		maturityDateDatePicker.setValue(equityOption.getMaturityDate());
		Label strikeLabel = new Label("Strike: ");
		TextField strikeTextField = new TextField();
		if (equityOption.getStrike() != null) {
			strikeTextField.setText(TradistaGUIUtil.formatAmount(equityOption.getStrike()));
		}
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(codeLabel, 1, 1);
		grid.add(codeTextField, 2, 1);
		grid.add(contractSpecificationLabel, 1, 2);
		grid.add(contractSpecificationComboBox, 2, 2);
		grid.add(typeLabel, 1, 3);
		grid.add(typeComboBox, 2, 3);
		grid.add(maturityDateLabel, 1, 4);
		grid.add(maturityDateDatePicker, 2, 4);
		grid.add(strikeLabel, 1, 5);
		grid.add(strikeTextField, 2, 5);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Copy", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);

		setResultConverter(new Callback<ButtonType, EquityOption>() {
			@Override
			public EquityOption call(ButtonType b) {
				if (b == buttonTypeOk) {

					BigDecimal strike = null;
					try {
						strike = TradistaGUIUtil.parseAmount(strikeTextField.getText(), "Strike");
					} catch (TradistaBusinessException tbe) {
						Alert error = new Alert(AlertType.ERROR);
						error.setContentText(
								String.format("The strike (%s) is not a valid number.", strikeTextField.getText()));
						error.showAndWait();
						return null;
					}
					return new EquityOption(codeTextField.getText(), typeComboBox.getValue(), strike,
							maturityDateDatePicker.getValue(), contractSpecificationComboBox.getValue());
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}