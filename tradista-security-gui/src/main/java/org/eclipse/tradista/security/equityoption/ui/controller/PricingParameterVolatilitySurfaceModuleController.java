package org.eclipse.tradista.security.equityoption.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaComboBoxTableCell;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class PricingParameterVolatilitySurfaceModuleController extends TradistaControllerAdapter
		implements PricingParameterModuleController {

	@FXML
	private TableView<EquityOptionVolatilitySurfaceProperty> equityOptionVolatilitySurfaceTable;

	@FXML
	private TableColumn<EquityOptionVolatilitySurfaceProperty, Equity> equityOptionVolatilitySurfaceEquity;

	@FXML
	private TableColumn<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface> equityOptionVolatilitySurface;

	@FXML
	private ComboBox<Equity> equityOptionVolatilitySurfaceEquityComboBox;

	@FXML
	private ComboBox<EquityOptionVolatilitySurface> equityOptionVolatilitySurfaceComboBox;

	@FXML
	private Button addEquityOptionVolatilitySurfaceButton;

	private Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		Callback<TableColumn<EquityOptionVolatilitySurfaceProperty, Equity>, TableCell<EquityOptionVolatilitySurfaceProperty, Equity>> equityOptionVolatilitySurfaceEquityCellFactory = _ -> new TradistaComboBoxTableCell<EquityOptionVolatilitySurfaceProperty, Equity>(
				TradistaSecurityGUIUtil::createEquityComboBox, (row, newEquity) -> {
					if (newEquity != null && !newEquity.equals(row.getEquity()) && equityOptionVolatilitySurfaceTable
							.getItems().contains(new EquityOptionVolatilitySurfaceProperty(newEquity, null))) {
						return String.format("The Equity %s is already in the list.%n", newEquity);
					}
					return null;
				});
		Callback<TableColumn<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface>, TableCell<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface>> equityOptionVolatilitySurfaceCellFactory = _ -> new TradistaComboBoxTableCell<>(
				TradistaSecurityGUIUtil::createEquityOptionVolatilitySurfaceComboBox);

		equityOptionVolatilitySurfaceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		equityOptionVolatilitySurfaceEquity.setCellValueFactory(cellData -> cellData.getValue().getEquityProperty());
		equityOptionVolatilitySurfaceEquity.setCellFactory(equityOptionVolatilitySurfaceEquityCellFactory);
		equityOptionVolatilitySurfaceEquity.setOnEditCommit(
				t -> (t.getTableView().getItems().get(t.getTablePosition().getRow())).setEquity(t.getNewValue()));

		equityOptionVolatilitySurface
				.setCellValueFactory(cellData -> cellData.getValue().getVolatilitySurfaceProperty());
		equityOptionVolatilitySurface.setCellFactory(equityOptionVolatilitySurfaceCellFactory);
		equityOptionVolatilitySurface
				.setOnEditCommit(t -> (t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setVolatilitySurface(t.getNewValue()));

		equityOptionVolatilitySurfaceEquityComboBox.setPromptText("Equity");
		equityOptionVolatilitySurfaceComboBox.setPromptText("Equity Option Volatility Surface");

		try {
			TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(equityOptionVolatilitySurfaceComboBox);
			TradistaSecurityGUIUtil.fillEquityComboBox(equityOptionVolatilitySurfaceEquityComboBox);
		} catch (TradistaTechnicalException _) {
			errors = new HashMap<>();
			List<String> err = new ArrayList<>(1);
			err.add("equity option volatility surfaces");
			err.add("equities");
			errors.put("get", err);
		}

		updateWindow();
	}

	@FXML
	protected void deleteEquityOptionVolatilitySurface() {
		int index = equityOptionVolatilitySurfaceTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			equityOptionVolatilitySurfaceTable.getItems().remove(index);
			equityOptionVolatilitySurfaceTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void addEquityOptionVolatilitySurface() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (equityOptionVolatilitySurfaceEquityComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an Equity Option Volatility Surface.%n"));
			} else {
				if (equityOptionVolatilitySurfaceComboBox.getValue() == null) {
					errMsg.append(String.format("Please select an Equity Option Volatility Surface.%n"));
				} else {
					if (equityOptionVolatilitySurfaceTable.getItems()
							.contains(new EquityOptionVolatilitySurfaceProperty(
									equityOptionVolatilitySurfaceEquityComboBox.getValue(), null))) {
						errMsg.append(String.format(
								"An Equity Option Volatility Surface is already in the list for this Equity %s.%n",
								equityOptionVolatilitySurfaceEquityComboBox.getValue()));
					}
				}
			}

			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			equityOptionVolatilitySurfaceTable.getItems()
					.add(new EquityOptionVolatilitySurfaceProperty(
							equityOptionVolatilitySurfaceEquityComboBox.getValue(),
							equityOptionVolatilitySurfaceComboBox.getValue()));
			equityOptionVolatilitySurfaceEquityComboBox.getSelectionModel().clearSelection();
			equityOptionVolatilitySurfaceTable.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	public void load(PricingParameter pricingParam) {

		PricingParameterVolatilitySurfaceModule module = null;
		for (PricingParameterModule mod : pricingParam.getModules()) {
			if (mod instanceof PricingParameterVolatilitySurfaceModule ppvsm) {
				module = ppvsm;
				break;
			}
		}

		if (module != null) {

			List<EquityOptionVolatilitySurfaceProperty> equityOptionVolatilitySurfacePropertyList = new ArrayList<>();

			for (Map.Entry<Equity, EquityOptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				equityOptionVolatilitySurfacePropertyList
						.add(new EquityOptionVolatilitySurfaceProperty(entry.getKey(), entry.getValue()));
			}

			Collections.sort(equityOptionVolatilitySurfacePropertyList);

			equityOptionVolatilitySurfaceTable
					.setItems(FXCollections.observableArrayList(equityOptionVolatilitySurfacePropertyList));
		} else {
			equityOptionVolatilitySurfaceTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterVolatilitySurfaceModule param = new PricingParameterVolatilitySurfaceModule();
		for (EquityOptionVolatilitySurfaceProperty prop : equityOptionVolatilitySurfaceTable.getItems()) {
			param.addVolatilitySurface(prop.getEquity(), prop.getVolatilitySurface());
		}
		return param;
	}

	protected class EquityOptionVolatilitySurfaceProperty implements Comparable<EquityOptionVolatilitySurfaceProperty> {

		private final SimpleObjectProperty<Equity> equity;
		private final SimpleObjectProperty<EquityOptionVolatilitySurface> volatilitySurface;

		private EquityOptionVolatilitySurfaceProperty(Equity equity, EquityOptionVolatilitySurface volatilitySurface) {
			this.equity = new SimpleObjectProperty<>(equity);
			this.volatilitySurface = new SimpleObjectProperty<>(volatilitySurface);
		}

		public Equity getEquity() {
			return equity.get();
		}

		public SimpleObjectProperty<Equity> getEquityProperty() {
			return equity;
		}

		public void setEquity(Equity equity) {
			this.equity.set(equity);
		}

		public EquityOptionVolatilitySurface getVolatilitySurface() {
			return volatilitySurface.get();
		}

		public SimpleObjectProperty<EquityOptionVolatilitySurface> getVolatilitySurfaceProperty() {
			return volatilitySurface;
		}

		public void setVolatilitySurface(EquityOptionVolatilitySurface volatilitySurface) {
			this.volatilitySurface.set(volatilitySurface);
		}

		@Override
		public int compareTo(EquityOptionVolatilitySurfaceProperty o) {
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
			EquityOptionVolatilitySurfaceProperty other = (EquityOptionVolatilitySurfaceProperty) obj;
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
		equityOptionVolatilitySurfaceTable.setItems(null);
	}

	@Override
	@FXML
	public void refresh() {
		try {
			TradistaSecurityGUIUtil.fillEquityComboBox(equityOptionVolatilitySurfaceEquityComboBox);
			TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(equityOptionVolatilitySurfaceComboBox);
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
			err.add("equity option volatility surfaces");
			err.add("equities");
			errors.put("get", err);
		}

		updateWindow();
	}

	@Override
	public Map<String, List<String>> getErrors() {
		return errors;
	}

	protected void updateWindow() {
		equityOptionVolatilitySurfaceComboBox.setDisable(errors != null && !errors.isEmpty());
		equityOptionVolatilitySurfaceEquityComboBox.setDisable(errors != null && !errors.isEmpty());
		addEquityOptionVolatilitySurfaceButton.setDisable(errors != null && !errors.isEmpty());
	}

}