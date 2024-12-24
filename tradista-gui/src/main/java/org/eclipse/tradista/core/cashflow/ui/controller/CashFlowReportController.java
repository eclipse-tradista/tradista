package org.eclipse.tradista.core.cashflow.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.position.model.BlankPositionDefinition;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class CashFlowReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker valueDateDatePicker;

	@FXML
	private ComboBox<PositionDefinition> positionDefinitionComboBox;

	@FXML
	private ComboBox<PricingParameter> ppComboBox;

	@FXML
	private ComboBox<String> aggregationComboBox;

	@FXML
	private TextField tradeIdTextField;

	@FXML
	private TableView<CashFlowProperty> report;

	@FXML
	private TableColumn<CashFlowProperty, String> cfDate;

	@FXML
	private TableColumn<CashFlowProperty, String> cfPurpose;

	@FXML
	private TableColumn<CashFlowProperty, String> cfDirection;

	@FXML
	private TableColumn<CashFlowProperty, String> cfAmount;

	@FXML
	private TableColumn<CashFlowProperty, String> cfCurrency;

	@FXML
	private TableColumn<CashFlowProperty, String> cfDiscountedAmount;

	@FXML
	private TableColumn<CashFlowProperty, String> cfDiscountFactor;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private List<CashFlow> cfsList;

	private ObservableList<CashFlowProperty> cfs;

	private ObservableList<CashFlowProperty> cfsByCurrency;

	private ObservableList<CashFlowProperty> cfsByCurrencyAndPurpose;

	private static final String NONE = "NONE";

	private static final String CURRENCY = "CURRENCY";

	private static final String CURRENCY_PLUS_PURPOSE = "CURRENCY+PURPOSE";

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		pricerBusinessDelegate = new PricerBusinessDelegate();

		String[] aggregationCriteria = new String[3];
		aggregationCriteria[0] = NONE;
		aggregationCriteria[1] = CURRENCY;
		aggregationCriteria[2] = CURRENCY_PLUS_PURPOSE;

		// CashFlows table
		cfDate.setCellValueFactory(cellData -> cellData.getValue().getDate());
		cfAmount.setCellValueFactory(cellData -> cellData.getValue().getAmount());
		cfCurrency.setCellValueFactory(cellData -> cellData.getValue().getCurrency());
		cfPurpose.setCellValueFactory(cellData -> cellData.getValue().getPurpose());
		cfDirection.setCellValueFactory(cellData -> cellData.getValue().getDirection());
		cfDiscountedAmount.setCellValueFactory(cellData -> cellData.getValue().getDiscountedAmount());
		cfDiscountFactor.setCellValueFactory(cellData -> cellData.getValue().getDiscountFactor());

		valueDateDatePicker.setValue(LocalDate.now());

		TradistaGUIUtil.fillPositionDefinitionComboBox(true, positionDefinitionComboBox);
		TradistaGUIUtil.fillPricingParameterComboBox(ppComboBox);
		aggregationComboBox.setItems(FXCollections.observableArrayList(aggregationCriteria));
		aggregationComboBox.setValue(aggregationCriteria[0]);
		aggregationComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldAggCrit,
					String newAggCrit) {
				if (newAggCrit != null) {
					fillReport();
				}
			}
		});
	}

	@FXML
	protected void load() {
		StringBuffer errMsg = new StringBuffer();

		if (valueDateDatePicker.getValue() == null) {
			errMsg.append(String.format("Value date is mandatory.%n"));
		}

		if (ppComboBox.getValue() == null) {
			errMsg.append(String.format("Pricing Parameters Set is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
			alert.showAndWait();
		} else {
			if (!tradeIdTextField.getText().isEmpty()) {
				try {
					cfsList = pricerBusinessDelegate.generateCashFlows(Long.parseLong(tradeIdTextField.getText()),
							ppComboBox.getValue(), valueDateDatePicker.getValue());
					cfs = null;
					cfsByCurrency = null;
					cfsByCurrencyAndPurpose = null;
					fillReport();
				} catch (NumberFormatException e) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
							String.format("The trade id: %s is incorrect.", tradeIdTextField.getText()));
					alert.showAndWait();
				} catch (TradistaBusinessException | TradistaTechnicalException ae) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, ae.getMessage());
					alert.showAndWait();
				}

			} else {
				if (!positionDefinitionComboBox.getValue().equals(BlankPositionDefinition.getInstance())) {
					try {
						cfsList = pricerBusinessDelegate.generateCashFlows(ppComboBox.getValue(),
								valueDateDatePicker.getValue(), positionDefinitionComboBox.getValue().getId());
						cfs = null;
						cfsByCurrency = null;
						cfsByCurrencyAndPurpose = null;
						fillReport();
					} catch (TradistaBusinessException | TradistaTechnicalException te) {
						TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
						alert.showAndWait();
					}
				} else {
					TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
					confirmation.setTitle("Load Cash Flows");
					confirmation.setHeaderText("Load Cash Flows");
					confirmation.setContentText(
							"You are loading all the cash flows present in the system, it can take time. Are you sure to continue?");

					Optional<ButtonType> result = confirmation.showAndWait();
					if (result.get() == ButtonType.OK) {
						try {
							cfsList = pricerBusinessDelegate.generateAllCashFlows(ppComboBox.getValue(),
									valueDateDatePicker.getValue());
							cfs = null;
							cfsByCurrency = null;
							cfsByCurrencyAndPurpose = null;
							fillReport();
						} catch (TradistaBusinessException | TradistaTechnicalException te) {
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
							alert.showAndWait();
						}
					}
				}
			}
		}
	}

	private void fillReport() {
		ObservableList<CashFlowProperty> data = null;

		if (cfsList != null) {
			if (aggregationComboBox.getValue().equals(CURRENCY)) {
				if (cfsByCurrency == null) {
					List<CashFlow> cfsListCopy = new ArrayList<CashFlow>();
					for (CashFlow cf : cfsList) {
						cfsListCopy.add((CashFlow) cf.clone());
					}
					Function<CashFlow, String> compositeKey = cf -> cf.getDate() + cf.getCurrency().toString();
					List<CashFlow> cfsByCurrencyList = (List<CashFlow>) cfsListCopy.stream()
							.collect(Collectors.toMap(compositeKey, Function.identity(), (left, right) -> {
								BigDecimal newAmount;
								BigDecimal newDiscountedAmount;
								if (left.getDirection().equals(CashFlow.Direction.PAY)) {
									newAmount = left.getAmount().negate();
									newDiscountedAmount = (left.getDiscountedAmount() == null) ? null
											: left.getDiscountedAmount().negate();
								} else {
									newAmount = left.getAmount();
									newDiscountedAmount = (left.getDiscountedAmount() == null) ? null
											: left.getDiscountedAmount();
								}
								if (right.getDirection().equals(CashFlow.Direction.PAY)) {
									newAmount = newAmount.subtract(right.getAmount());
									newDiscountedAmount = (right.getDiscountedAmount() == null) ? null
											: newDiscountedAmount.subtract(right.getDiscountedAmount());
								} else {
									newAmount = newAmount.add(right.getAmount());
									newDiscountedAmount = (right.getDiscountedAmount() == null) ? null
											: newDiscountedAmount.add(right.getDiscountedAmount());
								}
								if (newAmount.signum() > 0) {
									left.setAmount(newAmount);
									left.setDiscountedAmount(newDiscountedAmount);
									left.setDirection(CashFlow.Direction.RECEIVE);
								} else {
									if (newAmount.signum() <= 0) {
										left.setAmount(newAmount.negate());
										left.setDiscountedAmount(newDiscountedAmount.negate());
										left.setDirection(CashFlow.Direction.PAY);
									}
								}
								if (left.getPurpose() != null && !left.getPurpose().equals(right.getPurpose())) {
									left.setPurpose(null);
								}
								return left;
							})).values().stream().filter(cf -> cf.getAmount().signum() > 0)
							.collect(Collectors.toList());
					Collections.sort(cfsByCurrencyList);
					cfsByCurrency = FXCollections
							.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cfsByCurrencyList));
				}
				data = cfsByCurrency;
			} else if (aggregationComboBox.getValue().equals(NONE)) {
				if (cfs == null) {
					cfs = FXCollections.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cfsList));
				}
				data = cfs;
			} else if (aggregationComboBox.getValue().equals(CURRENCY_PLUS_PURPOSE)) {
				if (cfsByCurrencyAndPurpose == null) {
					List<CashFlow> cfsListCopy = new ArrayList<CashFlow>();
					for (CashFlow cf : cfsList) {
						cfsListCopy.add((CashFlow) cf.clone());
					}
					Function<CashFlow, String> compositeKey = cf -> cf.getDate() + cf.getCurrency().toString()
							+ cf.getPurpose();
					List<CashFlow> cfsByCurrencyAndPurposeList = (List<CashFlow>) cfsListCopy.stream()
							.collect(Collectors.toMap(compositeKey, Function.identity(), (left, right) -> {
								BigDecimal newAmount;
								BigDecimal newDiscountedAmount;
								if (left.getDirection().equals(CashFlow.Direction.PAY)) {
									newAmount = left.getAmount().negate();
									newDiscountedAmount = (left.getDiscountedAmount() == null) ? null
											: left.getDiscountedAmount().negate();
								} else {
									newAmount = left.getAmount();
									newDiscountedAmount = (left.getDiscountedAmount() == null) ? null
											: left.getDiscountedAmount();
								}
								if (right.getDirection().equals(CashFlow.Direction.PAY)) {
									newAmount = newAmount.subtract(right.getAmount());
									newDiscountedAmount = (right.getDiscountedAmount() == null) ? null
											: newDiscountedAmount.subtract(right.getDiscountedAmount());
								} else {
									newAmount = newAmount.add(right.getAmount());
									newDiscountedAmount = (right.getDiscountedAmount() == null) ? null
											: newDiscountedAmount.add(right.getDiscountedAmount());
								}
								if (newAmount.signum() > 0) {
									left.setAmount(newAmount);
									left.setDiscountedAmount(newDiscountedAmount);
									left.setDirection(CashFlow.Direction.RECEIVE);
								} else {
									if (newAmount.signum() <= 0) {
										left.setAmount(newAmount.negate());
										left.setDiscountedAmount(newDiscountedAmount.negate());
										left.setDirection(CashFlow.Direction.PAY);
									}
								}
								return left;
							})).values().stream().filter(cf -> cf.getAmount().signum() > 0)
							.collect(Collectors.toList());
					Collections.sort(cfsByCurrencyAndPurposeList);
					cfsByCurrencyAndPurpose = FXCollections
							.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cfsByCurrencyAndPurposeList));
				}
				data = cfsByCurrencyAndPurpose;
			}
		}
		report.setItems(data);
		report.refresh();
	}

	@FXML
	protected void export() {
		try {
			TradistaGUIUtil.export(report, "CashFlows", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}