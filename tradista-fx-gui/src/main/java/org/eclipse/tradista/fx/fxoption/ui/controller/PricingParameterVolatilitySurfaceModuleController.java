package org.eclipse.tradista.fx.fxoption.ui.controller;

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
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.currency.ui.view.TradistaCurrencyComboBox;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaVolatilitySurfaceComboBox;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;

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
	private TableView<FXVolatilitySurfaceProperty> fxVolatilitySurfaceTable;

	@FXML
	private TableColumn<FXVolatilitySurfaceProperty, Currency> fxVolatilitySurfacePrimaryCurrency;

	@FXML
	private TableColumn<FXVolatilitySurfaceProperty, Currency> fxVolatilitySurfaceQuoteCurrency;

	@FXML
	private TableColumn<FXVolatilitySurfaceProperty, FXVolatilitySurface> fxVolatilitySurface;

	@FXML
	private TradistaCurrencyComboBox fxVolatilitySurfacePrimaryCurrencyComboBox;

	@FXML
	private TradistaCurrencyComboBox fxVolatilitySurfaceQuoteCurrencyComboBox;

	@FXML
	private ComboBox<FXVolatilitySurface> fxVolatilitySurfaceComboBox;

	@FXML
	private Button addFXVolatilitySurfaceButton;

	private Map<String, List<String>> errors;

	private FXVolatilitySurfaceBusinessDelegate fxVolatilitySurfaceBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		fxVolatilitySurfaceBusinessDelegate = new FXVolatilitySurfaceBusinessDelegate();

		Callback<TableColumn<FXVolatilitySurfaceProperty, Currency>, TableCell<FXVolatilitySurfaceProperty, Currency>> fxVolatilitySurfacePrimaryCurrencyCellFactory = new Callback<TableColumn<FXVolatilitySurfaceProperty, Currency>, TableCell<FXVolatilitySurfaceProperty, Currency>>() {
			public TableCell<FXVolatilitySurfaceProperty, Currency> call(
					TableColumn<FXVolatilitySurfaceProperty, Currency> p) {
				return new FXVolatilitySurfacePrimaryCurrencyEditingCell();
			}
		};

		Callback<TableColumn<FXVolatilitySurfaceProperty, Currency>, TableCell<FXVolatilitySurfaceProperty, Currency>> fxVolatilitySurfaceQuoteCurrencyCellFactory = new Callback<TableColumn<FXVolatilitySurfaceProperty, Currency>, TableCell<FXVolatilitySurfaceProperty, Currency>>() {
			public TableCell<FXVolatilitySurfaceProperty, Currency> call(
					TableColumn<FXVolatilitySurfaceProperty, Currency> p) {
				return new FXVolatilitySurfaceQuoteCurrencyEditingCell();
			}
		};

		Callback<TableColumn<FXVolatilitySurfaceProperty, FXVolatilitySurface>, TableCell<FXVolatilitySurfaceProperty, FXVolatilitySurface>> fxVolatilitySurfaceCellFactory = new Callback<TableColumn<FXVolatilitySurfaceProperty, FXVolatilitySurface>, TableCell<FXVolatilitySurfaceProperty, FXVolatilitySurface>>() {
			public TableCell<FXVolatilitySurfaceProperty, FXVolatilitySurface> call(
					TableColumn<FXVolatilitySurfaceProperty, FXVolatilitySurface> p) {
				return new FXVolatilitySurfaceEditingCell();
			}
		};

		fxVolatilitySurfacePrimaryCurrency.setCellValueFactory(
				new PropertyValueFactory<FXVolatilitySurfaceProperty, Currency>("primaryCurrency"));

		fxVolatilitySurfacePrimaryCurrency.setCellFactory(fxVolatilitySurfacePrimaryCurrencyCellFactory);

		fxVolatilitySurfacePrimaryCurrency
				.setOnEditCommit(new EventHandler<CellEditEvent<FXVolatilitySurfaceProperty, Currency>>() {
					@Override
					public void handle(CellEditEvent<FXVolatilitySurfaceProperty, Currency> t) {
						((FXVolatilitySurfaceProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
								.setPrimaryCurrency(t.getNewValue());
					}
				});

		fxVolatilitySurfaceQuoteCurrency
				.setCellValueFactory(new PropertyValueFactory<FXVolatilitySurfaceProperty, Currency>("quoteCurrency"));

		fxVolatilitySurfaceQuoteCurrency.setCellFactory(fxVolatilitySurfaceQuoteCurrencyCellFactory);

		fxVolatilitySurfaceQuoteCurrency
				.setOnEditCommit(new EventHandler<CellEditEvent<FXVolatilitySurfaceProperty, Currency>>() {
					@Override
					public void handle(CellEditEvent<FXVolatilitySurfaceProperty, Currency> t) {
						((FXVolatilitySurfaceProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
								.setQuoteCurrency(t.getNewValue());
					}
				});

		fxVolatilitySurface.setCellValueFactory(
				new PropertyValueFactory<FXVolatilitySurfaceProperty, FXVolatilitySurface>("volatilitySurface"));

		fxVolatilitySurface.setCellFactory(fxVolatilitySurfaceCellFactory);

		fxVolatilitySurface
				.setOnEditCommit(new EventHandler<CellEditEvent<FXVolatilitySurfaceProperty, FXVolatilitySurface>>() {
					@Override
					public void handle(CellEditEvent<FXVolatilitySurfaceProperty, FXVolatilitySurface> t) {
						((FXVolatilitySurfaceProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
								.setVolatilitySurface(t.getNewValue());
					}
				});

		fxVolatilitySurfacePrimaryCurrencyComboBox.setPromptText("Primary Currency");
		fxVolatilitySurfaceQuoteCurrencyComboBox.setPromptText("Quote Currency");
		fxVolatilitySurfaceComboBox.setPromptText("FX Volatility Surface");

		try {
			TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
					fxVolatilitySurfaceComboBox);
		} catch (TradistaTechnicalException tte) {
			errors = new HashMap<String, List<String>>();
			List<String> err = new ArrayList<>(1);
			err.add("fx volatility surfaces");
			errors.put("get", err);
		}

		updateWindow();
	}

	@FXML
	protected void deleteFXVolatilitySurface() {
		int index = fxVolatilitySurfaceTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			fxVolatilitySurfaceTable.getItems().remove(index);
			fxVolatilitySurfaceTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void addFXVolatilitySurface() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (fxVolatilitySurfacePrimaryCurrencyComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Primary Currency. %n"));
			} else {
				if (fxVolatilitySurfaceQuoteCurrencyComboBox.getValue() == null) {
					errMsg.append(String.format("Please select a Quote Currency. %n"));
				} else if (fxVolatilitySurfacePrimaryCurrencyComboBox.getValue()
						.equals(fxVolatilitySurfaceQuoteCurrencyComboBox.getValue())) {
					errMsg.append(String.format("Primary and Quote Currencies must be different.%n"));
				} else {
					if (fxVolatilitySurfaceComboBox.getValue() == null) {
						errMsg.append(String.format("Please select a FX Volatility Surface.%n"));
					} else {
						if (fxVolatilitySurfaceTable.getItems()
								.contains(new FXVolatilitySurfaceProperty(
										fxVolatilitySurfacePrimaryCurrencyComboBox.getValue(),
										fxVolatilitySurfaceQuoteCurrencyComboBox.getValue(), null))) {
							errMsg.append(String.format(
									"A FX Volatility Surface is already in the list for this Currency Pair %s.%s.%n",
									fxVolatilitySurfacePrimaryCurrencyComboBox.getValue(),
									fxVolatilitySurfaceQuoteCurrencyComboBox.getValue()));
						}
					}
				}
			}

			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			fxVolatilitySurfaceTable.getItems()
					.add(new FXVolatilitySurfaceProperty(fxVolatilitySurfacePrimaryCurrencyComboBox.getValue(),
							fxVolatilitySurfaceQuoteCurrencyComboBox.getValue(),
							fxVolatilitySurfaceComboBox.getValue()));
			fxVolatilitySurfacePrimaryCurrencyComboBox.getSelectionModel().clearSelection();
			fxVolatilitySurfaceQuoteCurrencyComboBox.getSelectionModel().clearSelection();
			fxVolatilitySurfaceTable.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException abe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
			alert.showAndWait();
		}
	}

	private class FXVolatilitySurfacePrimaryCurrencyEditingCell
			extends TableCell<FXVolatilitySurfaceProperty, Currency> {

		private TradistaCurrencyComboBox currencyComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createCurrencyComboBox();
			Currency currency = currencyComboBox.getValue();
			if (currency != null) {
				setText(currency.toString());
			}
			setGraphic(currencyComboBox);
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
		public void updateItem(Currency item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (currencyComboBox != null) {
						currencyComboBox.setValue(getItem());
					}
					setGraphic(currencyComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createCurrencyComboBox() {
			currencyComboBox = new TradistaCurrencyComboBox();
			currencyComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends Currency> observableValue, Currency oldCurrency,
						Currency newCurrency) {
					if (!changing && newCurrency != null && oldCurrency != null && !oldCurrency.equals(newCurrency)) {
						StringBuilder errMsg = new StringBuilder();
						if (fxVolatilitySurfaceTable.getItems().contains(new FXVolatilitySurfaceProperty(newCurrency,
								((FXVolatilitySurfaceProperty) getTableRow().getItem()).getQuoteCurrency(), null))) {
							errMsg.append(String.format("The Currency Pair %s.%s is already in the list.%n",
									newCurrency,
									((FXVolatilitySurfaceProperty) getTableRow().getItem()).getQuoteCurrency()));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								currencyComboBox.setValue(oldCurrency);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				currencyComboBox.setValue(getItem());
			}
			currencyComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			currencyComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!fxVolatilitySurfaceTable.getItems()
								.contains(new FXVolatilitySurfaceProperty(currencyComboBox.getValue(),
										((FXVolatilitySurfaceProperty) getTableRow().getItem()).getQuoteCurrency(),
										null))) {
							commitEdit(currencyComboBox.getValue());
						}
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class FXVolatilitySurfaceQuoteCurrencyEditingCell extends TableCell<FXVolatilitySurfaceProperty, Currency> {

		private TradistaCurrencyComboBox currencyComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createCurrencyComboBox();
			Currency currency = currencyComboBox.getValue();
			if (currency != null) {
				setText(currency.toString());
			}
			setGraphic(currencyComboBox);
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
		public void updateItem(Currency item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (currencyComboBox != null) {
						currencyComboBox.setValue(getItem());
					}
					setGraphic(currencyComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createCurrencyComboBox() {
			currencyComboBox = new TradistaCurrencyComboBox();
			currencyComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends Currency> observableValue, Currency oldCurrency,
						Currency newCurrency) {
					if (!changing && newCurrency != null && oldCurrency != null && !oldCurrency.equals(newCurrency)) {
						StringBuilder errMsg = new StringBuilder();
						if (fxVolatilitySurfaceTable.getItems()
								.contains(new FXVolatilitySurfaceProperty(
										((FXVolatilitySurfaceProperty) getTableRow().getItem()).getPrimaryCurrency(),
										newCurrency, null))) {
							errMsg.append(String.format("The Currency Pair %s.%s is already in the list.%n",
									((FXVolatilitySurfaceProperty) getTableRow().getItem()).getPrimaryCurrency(),
									newCurrency, null));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								currencyComboBox.setValue(oldCurrency);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				currencyComboBox.setValue(getItem());
			}
			currencyComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			currencyComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!fxVolatilitySurfaceTable.getItems()
								.contains(new FXVolatilitySurfaceProperty(
										((FXVolatilitySurfaceProperty) getTableRow().getItem()).getPrimaryCurrency(),
										currencyComboBox.getValue(), null))) {
							commitEdit(currencyComboBox.getValue());
						}
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class FXVolatilitySurfaceEditingCell extends TableCell<FXVolatilitySurfaceProperty, FXVolatilitySurface> {

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
		public void updateItem(FXVolatilitySurface item, boolean empty) {
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
			volatilitySurfaceComboBox = new TradistaVolatilitySurfaceComboBox("FX");
			if (getItem() != null) {
				volatilitySurfaceComboBox.setValue(getItem());
			}
			volatilitySurfaceComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			volatilitySurfaceComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit((FXVolatilitySurface) volatilitySurfaceComboBox.getValue());
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

			List<FXVolatilitySurfaceProperty> fxVolatilitySurfacePropertyList = new ArrayList<FXVolatilitySurfaceProperty>();

			for (Map.Entry<CurrencyPair, FXVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				fxVolatilitySurfacePropertyList.add(new FXVolatilitySurfaceProperty(entry.getKey().getPrimaryCurrency(),
						entry.getKey().getQuoteCurrency(), entry.getValue()));
			}

			Collections.sort(fxVolatilitySurfacePropertyList);

			fxVolatilitySurfaceTable.setItems(FXCollections.observableArrayList(fxVolatilitySurfacePropertyList));
		} else {
			fxVolatilitySurfaceTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterVolatilitySurfaceModule param = new PricingParameterVolatilitySurfaceModule();
		for (FXVolatilitySurfaceProperty prop : fxVolatilitySurfaceTable.getItems()) {
			param.getVolatilitySurfaces().put(
					new CurrencyPair((Currency) prop.getPrimaryCurrency(), (Currency) prop.getQuoteCurrency()),
					(FXVolatilitySurface) prop.getVolatilitySurface());
		}
		return param;
	}

	protected class FXVolatilitySurfaceProperty implements Comparable<FXVolatilitySurfaceProperty> {

		private final SimpleObjectProperty primaryCurrency;
		private final SimpleObjectProperty quoteCurrency;
		private final SimpleObjectProperty volatilitySurface;

		private FXVolatilitySurfaceProperty(Object primaryCurrency, Object quoteCurrency, Object volatilitySurface) {
			this.primaryCurrency = new SimpleObjectProperty(primaryCurrency);
			this.quoteCurrency = new SimpleObjectProperty(quoteCurrency);
			this.volatilitySurface = new SimpleObjectProperty(volatilitySurface);
		}

		public Object getPrimaryCurrency() {
			return primaryCurrency.get();
		}

		public void setPrimaryCurrency(Object primaryCurrency) {
			this.primaryCurrency.set(primaryCurrency);
		}

		public Object getQuoteCurrency() {
			return quoteCurrency.get();
		}

		public void setQuoteCurrency(Object quoteCurrency) {
			this.quoteCurrency.set(quoteCurrency);
		}

		public Object getVolatilitySurface() {
			return volatilitySurface.get();
		}

		public void setVolatilitySurface(Object volatilitySurface) {
			this.volatilitySurface.set(volatilitySurface);
		}

		@Override
		public int compareTo(FXVolatilitySurfaceProperty o) {
			return (getPrimaryCurrency().toString() + getQuoteCurrency())
					.compareTo(o.getPrimaryCurrency().toString() + o.getQuoteCurrency());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getPrimaryCurrency() == null) ? 0 : getPrimaryCurrency().hashCode());
			result = prime * result + ((getQuoteCurrency() == null) ? 0 : getQuoteCurrency().hashCode());
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
			FXVolatilitySurfaceProperty other = (FXVolatilitySurfaceProperty) obj;
			if (getPrimaryCurrency() == null) {
				if (other.getPrimaryCurrency() != null)
					return false;
			} else if (!getPrimaryCurrency().equals(other.getPrimaryCurrency()))
				return false;
			if (getQuoteCurrency() == null) {
				if (other.getQuoteCurrency() != null)
					return false;
			} else if (!getQuoteCurrency().equals(other.getQuoteCurrency()))
				return false;
			return true;
		}

	}

	@Override
	public void clear() {
		fxVolatilitySurfaceTable.setItems(null);
	}

	@Override
	@FXML
	public void refresh() {
		try {
			TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
					fxVolatilitySurfaceComboBox);
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
			err.add("fx volatility surfaces");
			errors.put("get", err);
		}
		TradistaGUIUtil.fillCurrencyComboBox(fxVolatilitySurfacePrimaryCurrencyComboBox,
				fxVolatilitySurfaceQuoteCurrencyComboBox);

		updateWindow();
	}

	@Override
	public Map<String, List<String>> getErrors() {
		return errors;
	}

	protected void updateWindow() {
		fxVolatilitySurfacePrimaryCurrencyComboBox.setDisable(errors != null && !errors.isEmpty());
		fxVolatilitySurfaceQuoteCurrencyComboBox.setDisable(errors != null && !errors.isEmpty());
		fxVolatilitySurfaceComboBox.setDisable(errors != null && !errors.isEmpty());
		addFXVolatilitySurfaceButton.setDisable(errors != null && !errors.isEmpty());
	}

}