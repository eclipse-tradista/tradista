package org.eclipse.tradista.ir.irswapoption.ui.controller;

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
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.ui.view.TradistaIndexComboBox;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaVolatilitySurfaceComboBox;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
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
	private TableView<IRSwapOptionVolatilitySurfaceProperty> irSwapOptionVolatilitySurfaceTable;

	@FXML
	private TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index> irSwapOptionVolatilitySurfaceIndex;

	@FXML
	private TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> irSwapOptionVolatilitySurface;

	@FXML
	private TradistaIndexComboBox irSwapOptionVolatilitySurfaceIndexComboBox;

	@FXML
	private ComboBox<SwaptionVolatilitySurface> irSwapOptionVolatilitySurfaceComboBox;

	private SwaptionVolatilitySurfaceBusinessDelegate swaptionVolatilitySurfaceBusinessDelegate;

	@FXML
	private Button addIRSwapOptionVolatilitySurfaceButton;

	private Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		swaptionVolatilitySurfaceBusinessDelegate = new SwaptionVolatilitySurfaceBusinessDelegate();

		Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index>, TableCell<IRSwapOptionVolatilitySurfaceProperty, Index>> irSwapOptionVolatilitySurfaceIndexCellFactory = new Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index>, TableCell<IRSwapOptionVolatilitySurfaceProperty, Index>>() {
			public TableCell<IRSwapOptionVolatilitySurfaceProperty, Index> call(
					TableColumn<IRSwapOptionVolatilitySurfaceProperty, Index> p) {
				return new SwaptionVolatilitySurfaceIndexEditingCell();
			}
		};

		Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>, TableCell<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>> irSwapOptionVolatilitySurfaceCellFactory = new Callback<TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>, TableCell<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>>() {
			public TableCell<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> call(
					TableColumn<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> p) {
				return new SwaptionVolatilitySurfaceEditingCell();
			}
		};

		irSwapOptionVolatilitySurfaceIndex
				.setCellValueFactory(new PropertyValueFactory<IRSwapOptionVolatilitySurfaceProperty, Index>("index"));

		irSwapOptionVolatilitySurfaceIndex.setCellFactory(irSwapOptionVolatilitySurfaceIndexCellFactory);

		irSwapOptionVolatilitySurfaceIndex
				.setOnEditCommit(new EventHandler<CellEditEvent<IRSwapOptionVolatilitySurfaceProperty, Index>>() {
					@Override
					public void handle(CellEditEvent<IRSwapOptionVolatilitySurfaceProperty, Index> t) {
						((IRSwapOptionVolatilitySurfaceProperty) t.getTableView().getItems()
								.get(t.getTablePosition().getRow())).setIndex(t.getNewValue());
					}
				});

		irSwapOptionVolatilitySurface.setCellValueFactory(
				new PropertyValueFactory<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>(
						"volatilitySurface"));

		irSwapOptionVolatilitySurface.setCellFactory(irSwapOptionVolatilitySurfaceCellFactory);

		irSwapOptionVolatilitySurface.setOnEditCommit(
				new EventHandler<CellEditEvent<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface>>() {
					@Override
					public void handle(
							CellEditEvent<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> t) {
						((IRSwapOptionVolatilitySurfaceProperty) t.getTableView().getItems()
								.get(t.getTablePosition().getRow())).setVolatilitySurface(t.getNewValue());
					}
				});

		irSwapOptionVolatilitySurfaceIndexComboBox.setPromptText("Index");
		irSwapOptionVolatilitySurfaceComboBox.setPromptText("IR Swap Option Volatility Surface");

		try {
			TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
					irSwapOptionVolatilitySurfaceComboBox);
		} catch (TradistaTechnicalException tte) {
			errors = new HashMap<String, List<String>>();
			List<String> err = new ArrayList<>(1);
			err.add("swaption volatility surfaces");
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

			if (errMsg.length() > 0) {
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

	private class SwaptionVolatilitySurfaceIndexEditingCell
			extends TableCell<IRSwapOptionVolatilitySurfaceProperty, Index> {

		private TradistaIndexComboBox indexComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createIndexComboBox();
			Index index = indexComboBox.getValue();
			if (index != null) {
				setText(index.toString());
			}
			setGraphic(indexComboBox);
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
		public void updateItem(Index item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (indexComboBox != null) {
						indexComboBox.setValue(getItem());
					}
					setGraphic(indexComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createIndexComboBox() {
			indexComboBox = new TradistaIndexComboBox();
			indexComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Index>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
					if (!changing && newIndex != null && oldIndex != null && !oldIndex.equals(newIndex)) {
						StringBuilder errMsg = new StringBuilder();
						if (irSwapOptionVolatilitySurfaceTable.getItems()
								.contains(new IRSwapOptionVolatilitySurfaceProperty(newIndex, null))) {
							errMsg.append(String.format("The Index %s is already in the list.%n", newIndex));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								indexComboBox.setValue(oldIndex);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				indexComboBox.setValue(getItem());
			}
			indexComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			indexComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!irSwapOptionVolatilitySurfaceTable.getItems()
								.contains(new IRSwapOptionVolatilitySurfaceProperty(indexComboBox.getValue(), null))) {
							commitEdit(indexComboBox.getValue());
						}
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class SwaptionVolatilitySurfaceEditingCell
			extends TableCell<IRSwapOptionVolatilitySurfaceProperty, SwaptionVolatilitySurface> {

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
		public void updateItem(SwaptionVolatilitySurface item, boolean empty) {
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
			volatilitySurfaceComboBox = new TradistaVolatilitySurfaceComboBox("IR");
			if (getItem() != null) {
				volatilitySurfaceComboBox.setValue(getItem());
			}
			volatilitySurfaceComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			volatilitySurfaceComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit((SwaptionVolatilitySurface) volatilitySurfaceComboBox.getValue());
					}
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
			if (mod instanceof PricingParameterVolatilitySurfaceModule) {
				module = (PricingParameterVolatilitySurfaceModule) mod;
				break;
			}
		}

		if (module != null) {

			List<IRSwapOptionVolatilitySurfaceProperty> IRSwapOptionVolatilitySurfacePropertyList = new ArrayList<IRSwapOptionVolatilitySurfaceProperty>();

			for (Map.Entry<Index, SwaptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				IRSwapOptionVolatilitySurfacePropertyList
						.add(new IRSwapOptionVolatilitySurfaceProperty(entry.getKey(), entry.getValue()));
			}

			Collections.sort(IRSwapOptionVolatilitySurfacePropertyList);

			irSwapOptionVolatilitySurfaceTable
					.setItems(FXCollections.observableArrayList(IRSwapOptionVolatilitySurfacePropertyList));
		} else {
			irSwapOptionVolatilitySurfaceTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterVolatilitySurfaceModule param = new PricingParameterVolatilitySurfaceModule();
		for (IRSwapOptionVolatilitySurfaceProperty prop : irSwapOptionVolatilitySurfaceTable.getItems()) {
			param.getVolatilitySurfaces().put((Index) prop.getIndex(),
					(SwaptionVolatilitySurface) prop.getVolatilitySurface());
		}
		return param;
	}

	protected class IRSwapOptionVolatilitySurfaceProperty implements Comparable<IRSwapOptionVolatilitySurfaceProperty> {

		private final SimpleObjectProperty index;
		private final SimpleObjectProperty volatilitySurface;

		private IRSwapOptionVolatilitySurfaceProperty(Object index, Object volatilitySurface) {
			this.index = new SimpleObjectProperty(index);
			this.volatilitySurface = new SimpleObjectProperty(volatilitySurface);
		}

		public Object getIndex() {
			return index.get();
		}

		public void setIndex(Object index) {
			this.index.set(index);
		}

		public Object getVolatilitySurface() {
			return volatilitySurface.get();
		}

		public void setVolatilitySurface(Object volatilitySurface) {
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
			TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
					irSwapOptionVolatilitySurfaceComboBox);
			if (errors != null) {
				errors.clear();
			}
		} catch (TradistaTechnicalException tte) {
			if (errors == null) {
				errors = new HashMap<String, List<String>>();
			} else {
				errors.clear();
			}
			List<String> err = new ArrayList<>(1);
			err.add("swaption volatility surfaces");
			errors.put("get", err);
		}
		TradistaGUIUtil.fillIndexComboBox(irSwapOptionVolatilitySurfaceIndexComboBox);

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