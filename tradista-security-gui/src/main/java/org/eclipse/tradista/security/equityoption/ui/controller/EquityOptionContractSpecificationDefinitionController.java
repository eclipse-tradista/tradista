package org.eclipse.tradista.security.equityoption.ui.controller;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;
import org.eclipse.tradista.security.equityoption.service.EquityOptionContractSpecificationBusinessDelegate;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

public class EquityOptionContractSpecificationDefinitionController implements TradistaController {

	@FXML
	private TextField quantity, name;

	@FXML
	private Label nameLabel;

	@FXML
	private ComboBox<VanillaOptionTrade.Style> style;

	@FXML
	private ComboBox<Exchange> exchange;

	@FXML
	private ComboBox<OptionTrade.SettlementType> settlementType;

	@FXML
	private TextField settlementDateOffset;

	private EquityOptionContractSpecificationBusinessDelegate equityOptionSpecificationBusinessDelegate;

	private DateRuleBusinessDelegate dateRuleBusinessDelegate;

	private EquityOptionContractSpecification equityOptionContractSpecification;

	@FXML
	private ComboBox<DateRule> maturityDatesDateRule;

	@FXML
	private Label productType;

	@FXML
	private ComboBox<EquityOptionContractSpecification> load;

	@FXML
	private TextField multiplier;

	@FXML
	private ComboBox<Currency> premiumCurrency;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		productType.setText("Equity Option Contract Specification");

		equityOptionSpecificationBusinessDelegate = new EquityOptionContractSpecificationBusinessDelegate();

		dateRuleBusinessDelegate = new DateRuleBusinessDelegate();

		TradistaGUIUtil.fillComboBox(dateRuleBusinessDelegate.getAllDateRules(), maturityDatesDateRule);
		TradistaGUIUtil.fillOptionStyleComboBox(style);
		TradistaGUIUtil.fillOptionSettlementTypeComboBox(settlementType);
		TradistaGUIUtil.fillExchangeComboBox(exchange);
		TradistaGUIUtil.fillComboBox(
				equityOptionSpecificationBusinessDelegate.getAllEquityOptionContractSpecifications(), load);
		TradistaGUIUtil.fillCurrencyComboBox(premiumCurrency);
	}

	private void buildSpecification(EquityOptionContractSpecification equityOptionContractSpecification) {
		try {
			equityOptionContractSpecification.setExchange(exchange.getValue());
			if (!quantity.getText().isEmpty()) {
				equityOptionContractSpecification
						.setQuantity(TradistaGUIUtil.parseAmount(quantity.getText(), "Quantity"));
			}
			equityOptionContractSpecification.setStyle(style.getValue());
			if (name.isVisible()) {
				equityOptionContractSpecification = new EquityOptionContractSpecification(name.getText());
				nameLabel.setText(name.getText());
			}
			if (!settlementDateOffset.getText().isEmpty()) {
				equityOptionContractSpecification
						.setSettlementDateOffset(Short.parseShort(settlementDateOffset.getText()));
			}
			equityOptionContractSpecification.setSettlementType(settlementType.getValue());
			equityOptionContractSpecification.setMaturityDatesDateRule(maturityDatesDateRule.getValue());
			if (!multiplier.getText().isEmpty()) {
				equityOptionContractSpecification
						.setMultiplier(TradistaGUIUtil.parseAmount(multiplier.getText(), "Multiplier"));
			}
			equityOptionContractSpecification.setPremiumCurrency(premiumCurrency.getValue());
		} catch (TradistaBusinessException tbe) {
			// Should not appear at this stage.
		}
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Equity Option Contract Specification");
		confirmation.setHeaderText("Save Equity Option Contract Specification");
		confirmation.setContentText("Do you want to save this Equity Option Contract Specification?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				if (name.isVisible()) {
					equityOptionContractSpecification = new EquityOptionContractSpecification(name.getText());
				}
				buildSpecification(equityOptionContractSpecification);

				equityOptionContractSpecification.setId(equityOptionSpecificationBusinessDelegate
						.saveEquityOptionContractSpecification(equityOptionContractSpecification));
				name.setVisible(false);
				nameLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		TradistaTextInputDialog dialog = new TradistaTextInputDialog();
		dialog.setTitle("Equity Option Contract Specification Copy");
		dialog.setHeaderText("Do you want to copy this Equity Option Contract Specification ?");
		dialog.setContentText("Please enter the name of the new Equity Option Contract Specification:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			try {
				checkAmounts();
				EquityOptionContractSpecification copyEquityOptionContractSpecification = new EquityOptionContractSpecification(
						result.get());
				buildSpecification(copyEquityOptionContractSpecification);
				copyEquityOptionContractSpecification.setId(equityOptionSpecificationBusinessDelegate
						.saveEquityOptionContractSpecification(copyEquityOptionContractSpecification));
				equityOptionContractSpecification = copyEquityOptionContractSpecification;
				name.setVisible(false);
				nameLabel.setVisible(true);
				nameLabel.setText(equityOptionContractSpecification.getName());
				TradistaGUIUtil.fillComboBox(
						equityOptionSpecificationBusinessDelegate.getAllEquityOptionContractSpecifications(), load);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		EquityOptionContractSpecification eos = null;
		String eosName = null;

		try {

			if (load.getValue() != null) {
				eosName = load.getValue().getName();
			} else {
				throw new TradistaBusinessException("Please specify a name.");
			}

			eos = equityOptionSpecificationBusinessDelegate.getEquityOptionContractSpecificationByName(eosName);

			if (eos == null) {
				throw new TradistaBusinessException(String
						.format("The equity option contract specification %s doesn't exist in the system.", eosName));
			}

			load(eos);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	private void load(EquityOptionContractSpecification equityOptionContractSpecification) {
		this.equityOptionContractSpecification = equityOptionContractSpecification;
		exchange.setValue(equityOptionContractSpecification.getExchange());
		quantity.setText(TradistaGUIUtil.formatAmount(equityOptionContractSpecification.getQuantity()));
		settlementDateOffset.setText(Short.toString(equityOptionContractSpecification.getSettlementDateOffset()));
		settlementType.setValue(equityOptionContractSpecification.getSettlementType());
		style.setValue(equityOptionContractSpecification.getStyle());
		name.setVisible(false);
		nameLabel.setText(equityOptionContractSpecification.getName());
		nameLabel.setVisible(true);
		maturityDatesDateRule.setValue(equityOptionContractSpecification.getMaturityDatesDateRule());
		multiplier.setText(TradistaGUIUtil.formatAmount(equityOptionContractSpecification.getMultiplier()));
		premiumCurrency.setValue(equityOptionContractSpecification.getPremiumCurrency());
	}

	@Override
	@FXML
	public void clear() {
		equityOptionContractSpecification = null;
		quantity.clear();
		name.clear();
		settlementDateOffset.clear();
		nameLabel.setText("");
		name.setVisible(true);
		nameLabel.setVisible(false);
		multiplier.clear();
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillExchangeComboBox(exchange);
		TradistaGUIUtil.fillComboBox(
				equityOptionSpecificationBusinessDelegate.getAllEquityOptionContractSpecifications(), load);
		TradistaGUIUtil.fillComboBox(dateRuleBusinessDelegate.getAllDateRules(), maturityDatesDateRule);
		TradistaGUIUtil.fillCurrencyComboBox(premiumCurrency);
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(quantity.getText(), "Quantity");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(multiplier.getText(), "Multiplier");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			if (!settlementDateOffset.getText().isEmpty()) {
				Short.parseShort(settlementDateOffset.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(
					String.format("The settlement date offset is incorrect: %s.%n", settlementDateOffset.getText()));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}