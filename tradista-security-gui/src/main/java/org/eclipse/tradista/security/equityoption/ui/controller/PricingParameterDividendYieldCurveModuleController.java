package org.eclipse.tradista.security.equityoption.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaInterestRateCurveComboBox;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.ui.view.TradistaEquityComboBox;
import org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterDividendYieldCurveModuleController extends TradistaControllerAdapter
		implements PricingParameterModuleController {

	@FXML
	private TableView<DividendYieldCurveProperty> dividendYieldCurveTable;

	@FXML
	private TableColumn<DividendYieldCurveProperty, Equity> equity;

	@FXML
	private TableColumn<DividendYieldCurveProperty, InterestRateCurve> dividendYieldCurve;

	@FXML
	private TradistaEquityComboBox equityComboBox;

	@FXML
	private ComboBox<InterestRateCurve> dividendYieldCurveComboBox;

	@FXML
	private Button addDividendYieldCurveButton;

	private Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		Callback<TableColumn<DividendYieldCurveProperty, Equity>, TableCell<DividendYieldCurveProperty, Equity>> dividendYieldCurveEquityCellFactory = _ -> new DividendYieldCurveEquityEditingCell();
		Callback<TableColumn<DividendYieldCurveProperty, InterestRateCurve>, TableCell<DividendYieldCurveProperty, InterestRateCurve>> dividendYieldCurveCellFactory = _ -> new DividendYieldCurveEditingCell();

		equity.setCellValueFactory(new PropertyValueFactory<>("equity"));
		equity.setCellFactory(dividendYieldCurveEquityCellFactory);
		equity.setOnEditCommit(
				t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setEquity(t.getNewValue()));

		dividendYieldCurve.setCellValueFactory(new PropertyValueFactory<>("curve"));
		dividendYieldCurve.setCellFactory(dividendYieldCurveCellFactory);
		dividendYieldCurve.setOnEditCommit(
				t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setCurve(t.getNewValue()));

		equityComboBox.setPromptText("Equity");
		dividendYieldCurveComboBox.setPromptText("Dividend Yield Curve");
		TradistaSecurityGUIUtil.fillEquityComboBox(equityComboBox);
		try {
			TradistaGUIUtil.fillInterestRateCurveComboBox(dividendYieldCurveComboBox);
		} catch (TradistaTechnicalException _) {
			errors = new HashMap<>();
			List<String> err = new ArrayList<>(1);
			err.add("dividend yield curves");
			errors.put("get", err);
		}

		updateWindow();
	}

	@FXML
	protected void deleteDividendYieldCurve() {
		int index = dividendYieldCurveTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			dividendYieldCurveTable.getItems().remove(index);
			dividendYieldCurveTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void addDividendYieldCurve() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (equityComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an Equity.%n"));
			} else {
				if (dividendYieldCurveComboBox.getValue() == null) {
					errMsg.append(String.format("Please select a Dividend Yield Curve.%n"));
				} else {
					if (dividendYieldCurveTable.getItems()
							.contains(new DividendYieldCurveProperty(equityComboBox.getValue(), null))) {
						errMsg.append(
								String.format("A Dividend Yield Curve is already in the list for this Equity %s.%n",
										equityComboBox.getValue()));
					}
				}
			}

			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			dividendYieldCurveTable.getItems().add(
					new DividendYieldCurveProperty(equityComboBox.getValue(), dividendYieldCurveComboBox.getValue()));
			equityComboBox.getSelectionModel().clearSelection();
			dividendYieldCurveComboBox.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private class DividendYieldCurveEquityEditingCell extends TableCell<DividendYieldCurveProperty, Equity> {

		private TradistaEquityComboBox equityComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createEquityComboBox();
			Equity equity = equityComboBox.getValue();
			if (equity != null) {
				setText(equity.toString());
			}
			setGraphic(equityComboBox);
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			if (getItem() != null) {
				setText(getItem().toString());
			}
			setGraphic(null);
		}

		@Override
		public void updateItem(Equity item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (equityComboBox != null) {
						equityComboBox.setValue(getItem());
					}
					setGraphic(equityComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createEquityComboBox() {
			equityComboBox = new TradistaEquityComboBox();
			equityComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Equity>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends Equity> observableValue, Equity oldEquity,
						Equity newEquity) {
					if (!changing && newEquity != null && oldEquity != null && !oldEquity.equals(newEquity)) {
						StringBuilder errMsg = new StringBuilder();
						if (dividendYieldCurveTable.getItems()
								.contains(new DividendYieldCurveProperty(newEquity, null))) {
							errMsg.append(String.format("The Equity %s is already in the list.%n", newEquity));
						}
						if (!errMsg.isEmpty()) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								equityComboBox.setValue(oldEquity);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				equityComboBox.setValue(getItem());
			}
			equityComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			equityComboBox.focusedProperty().addListener((_, _, isFocused) -> {
				if (Boolean.FALSE.equals(isFocused)) {
					if (!dividendYieldCurveTable.getItems()
							.contains(new DividendYieldCurveProperty(equityComboBox.getValue(), null))) {
						commitEdit(equityComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class DividendYieldCurveEditingCell extends TableCell<DividendYieldCurveProperty, InterestRateCurve> {

		private TradistaInterestRateCurveComboBox interestRateCurveComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createInterestRateCurveComboBox();
			InterestRateCurve curve = interestRateCurveComboBox.getValue();
			if (curve != null) {
				setText(curve.toString());
			}
			setGraphic(interestRateCurveComboBox);
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			if (getItem() != null) {
				setText(getItem().toString());
			}
			setGraphic(null);
		}

		@Override
		public void updateItem(InterestRateCurve item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (interestRateCurveComboBox != null) {
						interestRateCurveComboBox.setValue(getItem());
					}
					setGraphic(interestRateCurveComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createInterestRateCurveComboBox() {
			interestRateCurveComboBox = new TradistaInterestRateCurveComboBox();
			if (getItem() != null) {
				interestRateCurveComboBox.setValue(getItem());
			}
			interestRateCurveComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			interestRateCurveComboBox.focusedProperty().addListener((_, _, isFocused) -> {
				if (Boolean.FALSE.equals(isFocused)) {
					commitEdit(interestRateCurveComboBox.getValue());
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	public void load(PricingParameter pricingParam) {

		PricingParameterDividendYieldCurveModule module = null;
		for (PricingParameterModule mod : pricingParam.getModules()) {
			if (mod instanceof PricingParameterDividendYieldCurveModule ppdycm) {
				module = ppdycm;
				break;
			}
		}

		if (module != null) {

			List<DividendYieldCurveProperty> dividendYieldCurvePropertyList = new ArrayList<>();

			for (Map.Entry<Equity, InterestRateCurve> entry : module.getDividendYieldCurves().entrySet()) {
				dividendYieldCurvePropertyList.add(new DividendYieldCurveProperty(entry.getKey(), entry.getValue()));
			}

			Collections.sort(dividendYieldCurvePropertyList);

			dividendYieldCurveTable.setItems(FXCollections.observableArrayList(dividendYieldCurvePropertyList));
		} else {
			dividendYieldCurveTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterDividendYieldCurveModule param = new PricingParameterDividendYieldCurveModule();
		for (DividendYieldCurveProperty prop : dividendYieldCurveTable.getItems()) {
			param.getDividendYieldCurves().put((Equity) prop.getEquity(), (InterestRateCurve) prop.getCurve());
		}
		return param;
	}

	protected class DividendYieldCurveProperty implements Comparable<DividendYieldCurveProperty> {

		private final SimpleObjectProperty<Object> equity;
		private final SimpleObjectProperty<Object> curve;

		private DividendYieldCurveProperty(Object equity, Object curve) {
			this.equity = new SimpleObjectProperty<>(equity);
			this.curve = new SimpleObjectProperty<>(curve);
		}

		public Object getEquity() {
			return equity.get();
		}

		public void setEquity(Object equity) {
			this.equity.set(equity);
		}

		public Object getCurve() {
			return curve.get();
		}

		public void setCurve(Object curve) {
			this.curve.set(curve);
		}

		@Override
		public int compareTo(DividendYieldCurveProperty o) {
			return (getEquity().toString()).compareTo(o.getEquity().toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getEquity() == null) ? 0 : getEquity().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DividendYieldCurveProperty other = (DividendYieldCurveProperty) obj;
			if (getEquity() == null) {
				if (other.getEquity() != null)
					return false;
			} else if (!getEquity().equals(other.getEquity()))
				return false;
			return true;
		}

	}

	@Override
	public void clear() {
		dividendYieldCurveTable.setItems(null);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaSecurityGUIUtil.fillEquityComboBox(equityComboBox);
		try {
			TradistaGUIUtil.fillInterestRateCurveComboBox(dividendYieldCurveComboBox);
			if (errors != null) {
				errors.clear();
			}
		} catch (TradistaTechnicalException _) {
			if (errors == null) {
				errors = new HashMap<>();
			} else {
				errors.clear();
			}
			List<String> err = new ArrayList<>(1);
			err.add("dividend yield curves");
			errors.put("get", err);
		}

		updateWindow();
	}

	@Override
	public Map<String, List<String>> getErrors() {
		return errors;
	}

	protected void updateWindow() {
		equityComboBox.setDisable(errors != null && !errors.isEmpty());
		dividendYieldCurveComboBox.setDisable(errors != null && !errors.isEmpty());
		addDividendYieldCurveButton.setDisable(errors != null && !errors.isEmpty());
	}

}