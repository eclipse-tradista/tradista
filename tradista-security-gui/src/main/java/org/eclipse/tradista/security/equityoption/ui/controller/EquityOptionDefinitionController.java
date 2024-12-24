package org.eclipse.tradista.security.equityoption.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaChoiceDialog;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;
import org.eclipse.tradista.security.equityoption.service.EquityOptionBusinessDelegate;
import org.eclipse.tradista.security.equityoption.service.EquityOptionContractSpecificationBusinessDelegate;
import org.eclipse.tradista.security.equityoption.ui.view.EquityOptionCreatorDialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class EquityOptionDefinitionController implements TradistaController {

	@FXML
	private TextField code;

	@FXML
	private ComboBox<OptionTrade.Type> type;

	@FXML
	private TextField strike;

	@FXML
	private ComboBox<Equity> equity;

	@FXML
	private ComboBox<EquityOptionContractSpecification> contractSpecification;

	private EquityBusinessDelegate equityBusinessDelegate;

	private EquityOptionBusinessDelegate equityOptionBusinessDelegate;

	private EquityOptionContractSpecificationBusinessDelegate equityOptionContractSpecificationBusinessDelegate;

	@FXML
	private ComboBox<String> loadingCriterion;

	@FXML
	private TextField load;

	private EquityOption equityOption;

	@FXML
	private Label equityOptionId;

	@FXML
	private DatePicker maturityDate;

	@FXML
	private Label productType;

	@FXML
	private Label codeLabel;

	@FXML
	private Label typeLabel;

	@FXML
	private Label strikeLabel;

	@FXML
	private Label contractSpecificationLabel;

	@FXML
	private Label maturityDateLabel;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		productType.setText("Equity Option");
		loadingCriterion.getItems().add("id");
		loadingCriterion.getItems().add("code");
		loadingCriterion.getSelectionModel().selectFirst();

		equityBusinessDelegate = new EquityBusinessDelegate();
		equityOptionBusinessDelegate = new EquityOptionBusinessDelegate();
		equityOptionContractSpecificationBusinessDelegate = new EquityOptionContractSpecificationBusinessDelegate();

		contractSpecification.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EquityOptionContractSpecification>() {
					@Override
					public void changed(ObservableValue<? extends EquityOptionContractSpecification> arg0,
							EquityOptionContractSpecification arg1, EquityOptionContractSpecification newEocs) {
						if (newEocs != null) {
							maturityDate.setValue(null);
							LocalDate startDate = LocalDate.now().minusYears(10);
							Set<LocalDate> maturityDates = new DateRuleBusinessDelegate()
									.generateDates(newEocs.getMaturityDatesDateRule(), startDate, Period.ofYears(100));
							final Callback<DatePicker, DateCell> maturityDayCellFactory = new Callback<DatePicker, DateCell>() {
								public DateCell call(final DatePicker datePicker) {
									return new DateCell() {

										private boolean isAvailable(LocalDate date) {
											if (contractSpecification.getValue() != null) {
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
							maturityDate.setDayCellFactory(maturityDayCellFactory);
						}
					}
				});

		TradistaGUIUtil.fillOptionTypeComboBox(type);
		TradistaSecurityGUIUtil.fillEquityOptionContractSpecificationComboBox(contractSpecification);
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
	}

	private void buildProduct(EquityOption equityOption) {
		equityOption.setUnderlying(equity.getValue());
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Equity Option");
		confirmation.setHeaderText("Save Equity Option");
		confirmation.setContentText("Do you want to save this Equity Option?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				if (code.isVisible()) {
					BigDecimal strikeBigDecimal = null;
					if (!strike.getText().isEmpty()) {
						try {
							strikeBigDecimal = TradistaGUIUtil.parseAmount(strike.getText(), "Strike");
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					}
					equityOption = new EquityOption(code.getText(), type.getValue(), strikeBigDecimal,
							maturityDate.getValue(), contractSpecification.getValue());
					equityOption.setCreationDate(LocalDate.now());
				}

				buildProduct(equityOption);

				equityOption.setId(equityOptionBusinessDelegate.saveEquityOption(equityOption));
				equityOptionId.setText(String.valueOf(equityOption.getId()));

				codeLabel.setText(code.getText());
				typeLabel.setText(type.getValue().toString());
				strikeLabel.setText(strike.getText());
				maturityDateLabel.setText(maturityDate.getValue().toString());
				contractSpecificationLabel.setText(contractSpecification.getValue().toString());
				code.setVisible(false);
				type.setVisible(false);
				strike.setVisible(false);
				maturityDate.setVisible(false);
				contractSpecification.setVisible(false);
				codeLabel.setVisible(true);
				typeLabel.setVisible(true);
				strikeLabel.setVisible(true);
				maturityDateLabel.setVisible(true);
				contractSpecificationLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		EquityOptionCreatorDialog dialog = new EquityOptionCreatorDialog(equityOption);
		Optional<EquityOption> result = dialog.showAndWait();

		if (result.isPresent()) {
			try {
				EquityOption copyEquityOption = new EquityOption(result.get().getCode(), result.get().getType(),
						result.get().getStrike(), result.get().getMaturityDate(),
						result.get().getEquityOptionContractSpecification());
				buildProduct(copyEquityOption);
				copyEquityOption.setId(equityOptionBusinessDelegate.saveEquityOption(copyEquityOption));
				equityOption = copyEquityOption;
				equityOptionId.setText(String.valueOf(equityOption.getId()));
				code.setText(equityOption.getCode());
				type.setValue(equityOption.getType());
				contractSpecification.setValue(equityOption.getEquityOptionContractSpecification());
				maturityDate.setValue(equityOption.getMaturityDate());
				strike.setText(TradistaGUIUtil.formatAmount(equityOption.getStrike()));

				codeLabel.setText(code.getText());
				typeLabel.setText(type.getValue().toString());
				strikeLabel.setText(strike.getText());
				maturityDateLabel.setText(maturityDate.getValue().toString());
				contractSpecificationLabel.setText(contractSpecification.getValue().toString());
				code.setVisible(false);
				type.setVisible(false);
				strike.setVisible(false);
				maturityDate.setVisible(false);
				contractSpecification.setVisible(false);
				codeLabel.setVisible(true);
				typeLabel.setVisible(true);
				strikeLabel.setVisible(true);
				maturityDateLabel.setVisible(true);
				contractSpecificationLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		Set<EquityOption> equityOptions = null;
		long equityOptionId = 0;
		String equityOptionCode = null;
		try {
			try {
				if (!load.getText().isEmpty()) {
					if (loadingCriterion.getValue().equals("id")) {
						equityOptionId = Long.parseLong(load.getText());
					} else {
						equityOptionCode = load.getText();
					}
				} else {
					throw new TradistaBusinessException("Please specify a product id or code.");
				}
			} catch (NumberFormatException nfe) {
				throw new TradistaBusinessException(String.format("The product id is incorrect: %s", load.getText()));
			}

			if (loadingCriterion.getValue().equals("id")) {
				equityOptions = new HashSet<EquityOption>(1);
				EquityOption equityOption = equityOptionBusinessDelegate.getEquityOptionById(equityOptionId);
				if (equityOption != null) {
					equityOptions.add(equityOption);
				}
			} else {
				equityOptions = equityOptionBusinessDelegate.getEquityOptionsByCode(equityOptionCode);
			}
			if (equityOptions == null || equityOptions.isEmpty()) {
				throw new TradistaBusinessException(
						String.format("The equity option %s doesn't exist in the system.", load.getText()));
			}

			if (equityOptions.size() > 1) {
				TradistaChoiceDialog<EquityOption> dialog = new TradistaChoiceDialog<EquityOption>(
						(EquityOption) equityOptions.toArray()[0], equityOptions);
				dialog.setTitle("Equity Option Selection");
				dialog.setHeaderText("Please choose an Equity Option");
				dialog.setContentText("Selected Equity Option:");

				Optional<EquityOption> result = dialog.showAndWait();
				result.ifPresent(equityOption -> load(equityOption));
			} else {
				load((EquityOption) equityOptions.toArray()[0]);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(EquityOption equityOption) {
		this.equityOption = equityOption;
		equityOptionId.setText(Long.toString(equityOption.getId()));
		equity.setValue(equityOption.getUnderlying());
		contractSpecification.setValue(equityOption.getEquityOptionContractSpecification());
		type.setValue(equityOption.getType());
		code.setText(equityOption.getCode());
		strike.setText(TradistaGUIUtil.formatAmount(equityOption.getStrike()));
		maturityDate.setValue(equityOption.getMaturityDate());
		codeLabel.setText(equityOption.getCode());
		typeLabel.setText(equityOption.getType().toString());
		strikeLabel.setText(TradistaGUIUtil.formatAmount(equityOption.getStrike()));
		maturityDateLabel.setText(equityOption.getMaturityDate().toString());
		contractSpecificationLabel.setText(equityOption.getEquityOptionContractSpecification().toString());
		code.setVisible(false);
		type.setVisible(false);
		strike.setVisible(false);
		maturityDate.setVisible(false);
		contractSpecification.setVisible(false);
		codeLabel.setVisible(true);
		typeLabel.setVisible(true);
		strikeLabel.setVisible(true);
		maturityDateLabel.setVisible(true);
		contractSpecificationLabel.setVisible(true);
	}

	@Override
	@FXML
	public void clear() {
		equityOption = null;
		equityOptionId.setText(StringUtils.EMPTY);
		code.clear();
		strike.clear();
		maturityDate.setValue(null);
		codeLabel.setText(StringUtils.EMPTY);
		code.setVisible(true);
		codeLabel.setVisible(false);
		typeLabel.setText(StringUtils.EMPTY);
		type.setVisible(true);
		typeLabel.setVisible(false);
		strikeLabel.setText(StringUtils.EMPTY);
		strike.setVisible(true);
		strikeLabel.setVisible(false);
		maturityDateLabel.setText(StringUtils.EMPTY);
		maturityDate.setVisible(true);
		maturityDateLabel.setVisible(false);
		contractSpecificationLabel.setText(StringUtils.EMPTY);
		contractSpecification.setVisible(true);
		contractSpecificationLabel.setVisible(false);
	}

	@Override
	@FXML
	public void refresh() {
		LocalDate matDate = maturityDate.getValue();
		TradistaGUIUtil.fillComboBox(
				equityOptionContractSpecificationBusinessDelegate.getAllEquityOptionContractSpecifications(),
				contractSpecification);
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
		maturityDate.setValue(matDate);
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(strike.getText(), "Strike");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}