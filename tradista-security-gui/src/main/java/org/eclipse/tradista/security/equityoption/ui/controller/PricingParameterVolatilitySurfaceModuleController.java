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
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaVolatilitySurfaceComboBox;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.ui.view.TradistaEquityComboBox;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule;

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

public class PricingParameterVolatilitySurfaceModuleController extends TradistaControllerAdapter
		implements PricingParameterModuleController {

	@FXML
	private TableView<EquityOptionVolatilitySurfaceProperty> equityOptionVolatilitySurfaceTable;

	@FXML
	private TableColumn<EquityOptionVolatilitySurfaceProperty, Equity> equityOptionVolatilitySurfaceEquity;

	@FXML
	private TableColumn<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface> equityOptionVolatilitySurface;

	@FXML
	private TradistaEquityComboBox equityOptionVolatilitySurfaceEquityComboBox;

	@FXML
	private ComboBox<EquityOptionVolatilitySurface> equityOptionVolatilitySurfaceComboBox;

	@FXML
	private Button addEquityOptionVolatilitySurfaceButton;

	private Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		Callback<TableColumn<EquityOptionVolatilitySurfaceProperty, Equity>, TableCell<EquityOptionVolatilitySurfaceProperty, Equity>> equityOptionVolatilitySurfaceEquityCellFactory = _ -> new EquityOptionVolatilitySurfaceEquityEditingCell();
		Callback<TableColumn<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface>, TableCell<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface>> equityOptionVolatilitySurfaceCellFactory = _ -> new EquityOptionVolatilitySurfaceEditingCell();

		equityOptionVolatilitySurfaceEquity.setCellValueFactory(new PropertyValueFactory<>("equity"));
		equityOptionVolatilitySurfaceEquity.setCellFactory(equityOptionVolatilitySurfaceEquityCellFactory);
		equityOptionVolatilitySurfaceEquity.setOnEditCommit(t -> ((EquityOptionVolatilitySurfaceProperty) t
				.getTableView().getItems().get(t.getTablePosition().getRow())).setEquity(t.getNewValue()));

		equityOptionVolatilitySurface.setCellValueFactory(new PropertyValueFactory<>("volatilitySurface"));
		equityOptionVolatilitySurface.setCellFactory(equityOptionVolatilitySurfaceCellFactory);
		equityOptionVolatilitySurface.setOnEditCommit(t -> ((EquityOptionVolatilitySurfaceProperty) t.getTableView()
				.getItems().get(t.getTablePosition().getRow())).setVolatilitySurface(t.getNewValue()));

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

	private class EquityOptionVolatilitySurfaceEquityEditingCell
			extends TableCell<EquityOptionVolatilitySurfaceProperty, Equity> {

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
						if (equityOptionVolatilitySurfaceTable.getItems()
								.contains(new EquityOptionVolatilitySurfaceProperty(newEquity, null))) {
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
					if (!equityOptionVolatilitySurfaceTable.getItems()
							.contains(new EquityOptionVolatilitySurfaceProperty(equityComboBox.getValue(), null))) {
						commitEdit(equityComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class EquityOptionVolatilitySurfaceEditingCell
			extends TableCell<EquityOptionVolatilitySurfaceProperty, EquityOptionVolatilitySurface> {

		private TradistaVolatilitySurfaceComboBox volatilitySurfaceComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createVolatilitySurfaceComboBox();
			VolatilitySurface<?, ?, ?> surface = volatilitySurfaceComboBox.getValue();
			if (surface != null) {
				setText(surface.toString());
			}
			setGraphic(volatilitySurfaceComboBox);
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
		public void updateItem(EquityOptionVolatilitySurface item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (volatilitySurfaceComboBox != null) {
						volatilitySurfaceComboBox.setValue(getItem());
					}
					setGraphic(volatilitySurfaceComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createVolatilitySurfaceComboBox() {
			volatilitySurfaceComboBox = new TradistaVolatilitySurfaceComboBox("EquityOption");
			if (getItem() != null) {
				volatilitySurfaceComboBox.setValue(getItem());
			}
			volatilitySurfaceComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			volatilitySurfaceComboBox.focusedProperty().addListener((_, _, isFocused) -> {
				if (Boolean.FALSE.equals(isFocused)) {
					commitEdit((EquityOptionVolatilitySurface) volatilitySurfaceComboBox.getValue());
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
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

			List<EquityOptionVolatilitySurfaceProperty> EquityOptionVolatilitySurfacePropertyList = new ArrayList<>();

			for (Map.Entry<Equity, EquityOptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				EquityOptionVolatilitySurfacePropertyList
						.add(new EquityOptionVolatilitySurfaceProperty(entry.getKey(), entry.getValue()));
			}

			Collections.sort(EquityOptionVolatilitySurfacePropertyList);

			equityOptionVolatilitySurfaceTable
					.setItems(FXCollections.observableArrayList(EquityOptionVolatilitySurfacePropertyList));
		} else {
			equityOptionVolatilitySurfaceTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterVolatilitySurfaceModule param = new PricingParameterVolatilitySurfaceModule();
		for (EquityOptionVolatilitySurfaceProperty prop : equityOptionVolatilitySurfaceTable.getItems()) {
			param.getVolatilitySurfaces().put((Equity) prop.getEquity(),
					(EquityOptionVolatilitySurface) prop.getVolatilitySurface());
		}
		return param;
	}

	protected class EquityOptionVolatilitySurfaceProperty implements Comparable<EquityOptionVolatilitySurfaceProperty> {

		private final SimpleObjectProperty<Object> equity;
		private final SimpleObjectProperty<Object> volatilitySurface;

		private EquityOptionVolatilitySurfaceProperty(Object equity, Object volatilitySurface) {
			this.equity = new SimpleObjectProperty<>(equity);
			this.volatilitySurface = new SimpleObjectProperty<>(volatilitySurface);
		}

		public Object getEquity() {
			return equity.get();
		}

		public void setEquity(Object equity) {
			this.equity.set(equity);
		}

		public Object getVolatilitySurface() {
			return volatilitySurface.get();
		}

		public void setVolatilitySurface(Object volatilitySurface) {
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