package org.eclipse.tradista.ir.irswapoption.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaComboBoxTableCell;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.ir.common.ui.util.TradistaIRGUIUtil;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;

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
	private TableView<IRSwapOptionVolatilitySurfaceProperty> irSwapOptionVolatilitySurfaceTable;

	@FXML
	private TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index> irSwapOptionVolatilitySurfaceIndex;

	@FXML
	private TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> irSwapOptionVolatilitySurface;

	@FXML
	private ComboBox<Index> irSwapOptionVolatilitySurfaceIndexComboBox;

	@FXML
	private ComboBox<SwaptionVolatilitySurface> irSwapOptionVolatilitySurfaceComboBox;

	@FXML
	private Button addIRSwapOptionVolatilitySurfaceButton;

	private Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index>, TableCell<IRSwapOptionVolatilitySurfaceProperty, Index>> irSwapOptionVolatilitySurfaceIndexCellFactory = _ -> new TradistaComboBoxTableCell<IRSwapOptionVolatilitySurfaceProperty, Index>(
				TradistaGUIUtil::createIndexComboBox, (row, newIndex) -> {
					if (newIndex != null && !newIndex.equals(row.getIndex()) && irSwapOptionVolatilitySurfaceTable
							.getItems().contains(new IRSwapOptionVolatilitySurfaceProperty(newIndex, null))) {
						return String.format("The Index %s is already in the list.%n", newIndex);
					}
					return null;
				});

		Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>, TableCell<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>> irSwapOptionVolatilitySurfaceCellFactory = _ -> new TradistaComboBoxTableCell<>(
				TradistaIRGUIUtil::createSwaptionVolatilitySurfaceComboBox);

		irSwapOptionVolatilitySurfaceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		irSwapOptionVolatilitySurfaceIndex.setCellValueFactory(cellData -> cellData.getValue().getIndexProperty());

		irSwapOptionVolatilitySurfaceIndex.setCellFactory(irSwapOptionVolatilitySurfaceIndexCellFactory);

		irSwapOptionVolatilitySurfaceIndex.setOnEditCommit(
				t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setIndex(t.getNewValue()));

		irSwapOptionVolatilitySurface
				.setCellValueFactory(cellData -> cellData.getValue().getVolatilitySurfaceProperty());

		irSwapOptionVolatilitySurface.setCellFactory(irSwapOptionVolatilitySurfaceCellFactory);

		irSwapOptionVolatilitySurface.setOnEditCommit(t -> t.getTableView().getItems()
				.get(t.getTablePosition().getRow()).setVolatilitySurface(t.getNewValue()));

		irSwapOptionVolatilitySurfaceIndexComboBox.setPromptText("Index");
		irSwapOptionVolatilitySurfaceComboBox.setPromptText("IR Swap Option Volatility Surface");

		try {
			TradistaIRGUIUtil.fillSwaptionVolatilitySurfaceComboBox(irSwapOptionVolatilitySurfaceComboBox);
			TradistaGUIUtil.fillIndexComboBox(irSwapOptionVolatilitySurfaceIndexComboBox);
		} catch (TradistaTechnicalException _) {
			errors = new HashMap<>();
			List<String> err = new ArrayList<>(1);
			err.add("swaption volatility surfaces");
			err.add("indices");
			errors.put("get", err);
		}

		updateWindow();
	}

	@FXML
	protected void deleteIRSwapOptionVolatilitySurface() {
		int index = irSwapOptionVolatilitySurfaceTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			irSwapOptionVolatilitySurfaceTable.getItems().remove(index);
			irSwapOptionVolatilitySurfaceTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void addIRSwapOptionVolatilitySurface() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (irSwapOptionVolatilitySurfaceIndexComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an Index.%n"));
			} else {
				if (irSwapOptionVolatilitySurfaceComboBox.getValue() == null) {
					errMsg.append(String.format("Please select an IR Swap Option Volatility Surface.%n"));
				} else {
					if (irSwapOptionVolatilitySurfaceTable.getItems()
							.contains(new IRSwapOptionVolatilitySurfaceProperty(
									irSwapOptionVolatilitySurfaceIndexComboBox.getValue(), null))) {
						errMsg.append(String.format(
								"An IR Swap Option Volatility Surface is already in the list for this Index %s.%n",
								irSwapOptionVolatilitySurfaceIndexComboBox.getValue()));
					}
				}
			}

			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			irSwapOptionVolatilitySurfaceTable.getItems()
					.add(new IRSwapOptionVolatilitySurfaceProperty(
							irSwapOptionVolatilitySurfaceIndexComboBox.getValue(),
							irSwapOptionVolatilitySurfaceComboBox.getValue()));
			irSwapOptionVolatilitySurfaceIndexComboBox.getSelectionModel().clearSelection();
			irSwapOptionVolatilitySurfaceTable.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	public void load(PricingParameter pricingParam) {

		PricingParameterVolatilitySurfaceModule module = null;
		for (PricingParameterModule mod : pricingParam.getModules()) {
			if (mod instanceof PricingParameterVolatilitySurfaceModule vsm) {
				module = vsm;
				break;
			}
		}

		if (module != null) {

			List<IRSwapOptionVolatilitySurfaceProperty> irSwapOptionVolatilitySurfacePropertyList = new ArrayList<>();

			for (Map.Entry<Index, SwaptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				irSwapOptionVolatilitySurfacePropertyList
						.add(new IRSwapOptionVolatilitySurfaceProperty(entry.getKey(), entry.getValue()));
			}

			Collections.sort(irSwapOptionVolatilitySurfacePropertyList);

			irSwapOptionVolatilitySurfaceTable
					.setItems(FXCollections.observableArrayList(irSwapOptionVolatilitySurfacePropertyList));
		} else {
			irSwapOptionVolatilitySurfaceTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterVolatilitySurfaceModule param = new PricingParameterVolatilitySurfaceModule();
		for (IRSwapOptionVolatilitySurfaceProperty prop : irSwapOptionVolatilitySurfaceTable.getItems()) {
			param.addVolatilitySurface(prop.getIndex(), prop.getVolatilitySurface());
		}
		return param;
	}

	protected class IRSwapOptionVolatilitySurfaceProperty implements Comparable<IRSwapOptionVolatilitySurfaceProperty> {

		private final SimpleObjectProperty<Index> index;
		private final SimpleObjectProperty<SwaptionVolatilitySurface> volatilitySurface;

		private IRSwapOptionVolatilitySurfaceProperty(Index index, SwaptionVolatilitySurface volatilitySurface) {
			this.index = new SimpleObjectProperty<>(index);
			this.volatilitySurface = new SimpleObjectProperty<>(volatilitySurface);
		}

		public Index getIndex() {
			return index.get();
		}

		public SimpleObjectProperty<Index> getIndexProperty() {
			return index;
		}

		public void setIndex(Index index) {
			this.index.set(index);
		}

		public SwaptionVolatilitySurface getVolatilitySurface() {
			return volatilitySurface.get();
		}

		public SimpleObjectProperty<SwaptionVolatilitySurface> getVolatilitySurfaceProperty() {
			return volatilitySurface;
		}

		public void setVolatilitySurface(SwaptionVolatilitySurface volatilitySurface) {
			this.volatilitySurface.set(volatilitySurface);
		}

		@Override
		public int compareTo(IRSwapOptionVolatilitySurfaceProperty o) {
			return (getIndex().toString()).compareTo(o.getIndex().toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getIndex() == null) ? 0 : getIndex().hashCode());
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
			IRSwapOptionVolatilitySurfaceProperty other = (IRSwapOptionVolatilitySurfaceProperty) obj;
			if (getIndex() == null) {
				if (other.getIndex() != null)
					return false;
			} else if (!getIndex().equals(other.getIndex()))
				return false;
			return true;
		}

	}

	@Override
	public void clear() {
		irSwapOptionVolatilitySurfaceTable.setItems(null);
	}

	@Override
	@FXML
	public void refresh() {
		try {
			TradistaIRGUIUtil.fillSwaptionVolatilitySurfaceComboBox(irSwapOptionVolatilitySurfaceComboBox);
			TradistaGUIUtil.fillIndexComboBox(irSwapOptionVolatilitySurfaceIndexComboBox);
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
			err.add("swaption volatility surfaces");
			err.add("indices");
			errors.put("get", err);
		}

		updateWindow();
	}

	@Override
	public Map<String, List<String>> getErrors() {
		return errors;
	}

	protected void updateWindow() {
		irSwapOptionVolatilitySurfaceIndexComboBox.setDisable(errors != null && !errors.isEmpty());
		irSwapOptionVolatilitySurfaceComboBox.setDisable(errors != null && !errors.isEmpty());
		addIRSwapOptionVolatilitySurfaceButton.setDisable(errors != null && !errors.isEmpty());
	}

}