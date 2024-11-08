package org.eclipse.tradista.ir.future.ui.controller;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationBusinessDelegate;
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

public class FutureContractSpecificationDefinitionController implements TradistaController {

	@FXML
	private TextField name;

	@FXML
	private Label nameLabel;

	@FXML
	private TextField notional;

	@FXML
	private ComboBox<Exchange> exchange;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private ComboBox<Tenor> referenceRateIndexTenor;

	@FXML
	private ComboBox<DayCountConvention> dayCountConvention;

	@FXML
	private ComboBox<DateRule> maturityDatesDateRule;

	private FutureContractSpecificationBusinessDelegate futureContractSpecificationBusinessDelegate;

	@FXML
	private ComboBox<FutureContractSpecification> load;

	private FutureContractSpecification futureContractSpecification;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		futureContractSpecificationBusinessDelegate = new FutureContractSpecificationBusinessDelegate();

		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);
		TradistaGUIUtil.fillCurrencyComboBox(currency);
		TradistaGUIUtil.fillTenorComboBox(referenceRateIndexTenor);
		TradistaGUIUtil.fillDayCountConventionComboBox(dayCountConvention);
		TradistaGUIUtil.fillExchangeComboBox(exchange);
		TradistaGUIUtil.fillDateRuleComboBox(maturityDatesDateRule);
		TradistaGUIUtil.fillComboBox(futureContractSpecificationBusinessDelegate.getAllFutureContractSpecifications(),
				load);

	}

	private void buildFutureContractSpecification(FutureContractSpecification futureContractSpecification) {
		try {
			futureContractSpecification.setExchange(exchange.getValue());
			if (!notional.getText().isEmpty()) {
				futureContractSpecification.setNotional(TradistaGUIUtil.parseAmount(notional.getText(), "Notional"));
			}
			futureContractSpecification.setCurrency(currency.getValue());
			futureContractSpecification.setReferenceRateIndex(referenceRateIndex.getValue());
			futureContractSpecification.setReferenceRateIndexTenor(referenceRateIndexTenor.getValue());
			futureContractSpecification.setDayCountConvention(dayCountConvention.getValue());
			futureContractSpecification.setMaturityDatesDateRule(maturityDatesDateRule.getValue());
		} catch (TradistaBusinessException tbe) {
			// Should not appear here.
		}
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Future Contract Specification");
		confirmation.setHeaderText("Save Future Contract Specification");
		confirmation.setContentText("Do you want to save this Future Contract Specification?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				if (name.isVisible()) {
					futureContractSpecification = new FutureContractSpecification(name.getText());
					nameLabel.setText(name.getText());
				}

				buildFutureContractSpecification(futureContractSpecification);

				futureContractSpecification.setId(futureContractSpecificationBusinessDelegate
						.saveFutureContractSpecification(futureContractSpecification));
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
		dialog.setTitle("Future Contract Specification Copy");
		dialog.setHeaderText("Do you want to copy this Future Contract Specification ?");
		dialog.setContentText("Please enter the name of the new Future Contract Specification:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			try {
				checkAmounts();
				FutureContractSpecification copyFutureContractSpecification = new FutureContractSpecification(
						result.get());
				buildFutureContractSpecification(copyFutureContractSpecification);
				copyFutureContractSpecification.setId(futureContractSpecificationBusinessDelegate
						.saveFutureContractSpecification(copyFutureContractSpecification));
				futureContractSpecification = copyFutureContractSpecification;
				name.setVisible(false);
				nameLabel.setVisible(true);
				nameLabel.setText(futureContractSpecification.getName());
				TradistaGUIUtil.fillComboBox(
						futureContractSpecificationBusinessDelegate.getAllFutureContractSpecifications(), load);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}

	}

	@FXML
	protected void load() {
		FutureContractSpecification fcs = null;
		String fcsName = null;

		try {

			if (load.getValue() != null) {
				fcsName = load.getValue().getName();
			} else {
				throw new TradistaBusinessException("Please specify a name.");
			}

			fcs = futureContractSpecificationBusinessDelegate.getFutureContractSpecificationByName(fcsName);

			if (fcs == null) {
				throw new TradistaBusinessException(
						String.format("The future contract specification %s doesn't exist in the system.", fcsName));
			}

			load(fcs);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	private void load(FutureContractSpecification fcs) {
		futureContractSpecification = fcs;
		exchange.setValue(fcs.getExchange());
		referenceRateIndex.setValue(fcs.getReferenceRateIndex());
		referenceRateIndexTenor.setValue(fcs.getReferenceRateIndexTenor());
		currency.setValue(fcs.getCurrency());
		dayCountConvention.setValue(fcs.getDayCountConvention());
		maturityDatesDateRule.setValue(fcs.getMaturityDatesDateRule());
		notional.setText(TradistaGUIUtil.formatAmount(fcs.getNotional()));
		name.setVisible(false);
		nameLabel.setText(fcs.getName());
		nameLabel.setVisible(true);
	}

	@Override
	@FXML
	public void clear() {
		futureContractSpecification = null;
		notional.clear();
		name.clear();
		nameLabel.setText("");
		name.setVisible(true);
		nameLabel.setVisible(false);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillExchangeComboBox(exchange);
		TradistaGUIUtil.fillCurrencyComboBox(currency);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);
		TradistaGUIUtil.fillDateRuleComboBox(maturityDatesDateRule);
		TradistaGUIUtil.fillComboBox(futureContractSpecificationBusinessDelegate.getAllFutureContractSpecifications(),
				load);
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(notional.getText(), "Notional");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}