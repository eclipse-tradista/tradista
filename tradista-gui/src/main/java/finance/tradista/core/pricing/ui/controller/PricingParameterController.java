package finance.tradista.core.pricing.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.exception.TradistaTechnicalException;
import finance.tradista.core.common.service.InformationBusinessDelegate;
import finance.tradista.core.common.ui.controller.TradistaController;
import finance.tradista.core.common.ui.controller.TradistaControllerAdapter;
import finance.tradista.core.common.ui.util.TradistaGUIUtil;
import finance.tradista.core.common.ui.view.TradistaAlert;
import finance.tradista.core.common.ui.view.TradistaTextInputDialog;
import finance.tradista.core.common.util.ClientUtil;
import finance.tradista.core.currency.model.Currency;
import finance.tradista.core.currency.model.CurrencyPair;
import finance.tradista.core.currency.ui.view.TradistaCurrencyComboBox;
import finance.tradista.core.index.model.Index;
import finance.tradista.core.index.ui.view.TradistaIndexComboBox;
import finance.tradista.core.marketdata.model.FXCurve;
import finance.tradista.core.marketdata.model.InterestRateCurve;
import finance.tradista.core.marketdata.model.QuoteSet;
import finance.tradista.core.marketdata.service.FXCurveBusinessDelegate;
import finance.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import finance.tradista.core.marketdata.service.QuoteBusinessDelegate;
import finance.tradista.core.marketdata.ui.view.TradistaFXCurveComboBox;
import finance.tradista.core.marketdata.ui.view.TradistaInterestRateCurveComboBox;
import finance.tradista.core.pricing.pricer.PricingParameter;
import finance.tradista.core.pricing.service.PricerBusinessDelegate;
import finance.tradista.core.pricing.ui.view.PricingParameterCreatorDialog;
import finance.tradista.core.product.ui.view.TradistaProductTypeComboBox;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/*
 * Copyright 2016 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class PricingParameterController extends TradistaControllerAdapter {

	@FXML
	private TableView<PricingParamProperty> pricingParamTable;

	@FXML
	private TableColumn<PricingParamProperty, String> paramName;

	@FXML
	private TableColumn<PricingParamProperty, String> paramValue;

	@FXML
	private TableView<DiscountCurveProperty> discountCurveTable;

	@FXML
	private TableColumn<DiscountCurveProperty, Currency> currency;

	@FXML
	private TableColumn<DiscountCurveProperty, InterestRateCurve> discountCurve;

	@FXML
	private Button addDiscountCurveButton;

	@FXML
	private TableView<IndexCurveProperty> indexCurveTable;

	@FXML
	private TableColumn<IndexCurveProperty, Index> index;

	@FXML
	private TableColumn<IndexCurveProperty, InterestRateCurve> indexCurve;

	@FXML
	private Button addIndexCurveButton;

	@FXML
	private TableView<FXCurveProperty> fxCurveTable;

	@FXML
	private TableColumn<FXCurveProperty, Currency> primaryCurrency;

	@FXML
	private TableColumn<FXCurveProperty, Currency> quoteCurrency;

	@FXML
	private TableColumn<FXCurveProperty, FXCurve> fxCurve;

	@FXML
	private Button addFXCurveButton;

	@FXML
	private TableView<CustomPricerProperty> customPricerTable;

	@FXML
	private TableColumn<CustomPricerProperty, String> productType;

	@FXML
	private TableColumn<CustomPricerProperty, String> customPricer;

	@FXML
	private ComboBox<PricingParameter> pricingParam;

	@FXML
	private TextField nameTextField;

	@FXML
	private TextField valueTextField;

	@FXML
	private TradistaCurrencyComboBox currencyComboBox;

	@FXML
	private ComboBox<InterestRateCurve> discountCurveComboBox;

	@FXML
	private TradistaIndexComboBox indexComboBox;

	@FXML
	private ComboBox<InterestRateCurve> indexCurveComboBox;

	@FXML
	private TradistaCurrencyComboBox primaryCurrencyComboBox;

	@FXML
	private TradistaCurrencyComboBox quoteCurrencyComboBox;

	@FXML
	private TradistaProductTypeComboBox productTypeComboBox;

	@FXML
	private TextField customPricerTextField;

	@FXML
	private ComboBox<FXCurve> fxCurveComboBox;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate;

	private FXCurveBusinessDelegate fxCurveBusinessDelegate;

	private InformationBusinessDelegate informationBusinessDelegate;

	@FXML
	private Tab unrealizedPnlCalculationTab;

	@FXML
	private Tab dividendYieldCurveTab;

	@FXML
	private Tab fxVolatilitySurfaceTab;

	@FXML
	private Tab equityOptionVolatilitySurfaceTab;

	@FXML
	private Tab irSwapOptionVolatilitySurfaceTab;

	@FXML
	private TabPane pricingParamTabPane;

	@FXML
	private ComboBox<QuoteSet> quoteSetComboBox;

	@FXML
	private Label name;

	@FXML
	private Button createButton;

	@FXML
	private Label marketDataMessage;

	private List<PricingParameterModuleController> pricingParameterModuleControllersList;

	private PricingParameter pricingParameter;

	@FXML
	private Label pricingParameterName;

	private boolean quoteSetExists = false;

	private boolean canGetQuoteSet = true;

	private boolean canGetDiscountCurve = true;

	private boolean canGetIndexCurve = true;

	private boolean canGetFXCurve = true;

	private Map<String, List<String>> modulesErrors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		pricerBusinessDelegate = new PricerBusinessDelegate();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
		interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();
		fxCurveBusinessDelegate = new FXCurveBusinessDelegate();
		informationBusinessDelegate = new InformationBusinessDelegate();

		if (!informationBusinessDelegate.hasFXModule()) {
			pricingParamTabPane.getTabs().remove(unrealizedPnlCalculationTab);
			pricingParamTabPane.getTabs().remove(fxVolatilitySurfaceTab);
		} else {
			FXMLLoader fxmlLoader = new FXMLLoader(
					getClass().getResource("/PricingParameterUnrealizedPnlCalculationModule.fxml"));
			StackPane pane = null;
			if (pricingParameterModuleControllersList == null) {
				pricingParameterModuleControllersList = new ArrayList<PricingParameterModuleController>();
			}
			try {
				pane = fxmlLoader.load();
				pricingParameterModuleControllersList.add(fxmlLoader.getController());
			} catch (IOException exception) {
				throw new TradistaTechnicalException(exception);
			}
			unrealizedPnlCalculationTab.setContent(pane);

			fxmlLoader = new FXMLLoader(getClass().getResource("/PricingParameterFXVolatilitySurfaceModule.fxml"));
			pane = null;
			if (pricingParameterModuleControllersList == null) {
				pricingParameterModuleControllersList = new ArrayList<PricingParameterModuleController>();
			}
			try {
				pane = fxmlLoader.load();
				PricingParameterModuleController ppmController = (PricingParameterModuleController) fxmlLoader
						.getController();
				pricingParameterModuleControllersList.add(ppmController);
				addModulesErrors(ppmController.getErrors());
			} catch (IOException exception) {
				throw new TradistaTechnicalException(exception);
			}
			fxVolatilitySurfaceTab.setContent(pane);
		}

		if (!informationBusinessDelegate.hasSecurityModule()) {
			pricingParamTabPane.getTabs().remove(dividendYieldCurveTab);
			pricingParamTabPane.getTabs().remove(equityOptionVolatilitySurfaceTab);
		} else {
			FXMLLoader fxmlLoader = new FXMLLoader(
					getClass().getResource("/PricingParameterDividendYieldCurveModule.fxml"));
			StackPane pane = null;
			if (pricingParameterModuleControllersList == null) {
				pricingParameterModuleControllersList = new ArrayList<PricingParameterModuleController>();
			}
			try {
				pane = fxmlLoader.load();
				PricingParameterModuleController ppmController = (PricingParameterModuleController) fxmlLoader
						.getController();
				pricingParameterModuleControllersList.add(ppmController);
				addModulesErrors(ppmController.getErrors());
			} catch (IOException exception) {
				throw new TradistaTechnicalException(exception);
			}
			dividendYieldCurveTab.setContent(pane);

			fxmlLoader = new FXMLLoader(
					getClass().getResource("/PricingParameterEquityOptionVolatilitySurfaceModule.fxml"));
			pane = null;
			if (pricingParameterModuleControllersList == null) {
				pricingParameterModuleControllersList = new ArrayList<PricingParameterModuleController>();
			}
			try {
				pane = fxmlLoader.load();
				PricingParameterModuleController ppmController = (PricingParameterModuleController) fxmlLoader
						.getController();
				pricingParameterModuleControllersList.add(ppmController);
				addModulesErrors(ppmController.getErrors());
			} catch (IOException exception) {
				throw new TradistaTechnicalException(exception);
			}
			equityOptionVolatilitySurfaceTab.setContent(pane);
		}

		if (!informationBusinessDelegate.hasIRModule()) {
			pricingParamTabPane.getTabs().remove(irSwapOptionVolatilitySurfaceTab);
		} else {
			FXMLLoader fxmlLoader = new FXMLLoader(
					getClass().getResource("/PricingParameterIRSwapOptionVolatilitySurfaceModule.fxml"));
			StackPane pane = null;
			if (pricingParameterModuleControllersList == null) {
				pricingParameterModuleControllersList = new ArrayList<PricingParameterModuleController>();
			}
			try {
				pane = fxmlLoader.load();
				PricingParameterModuleController ppmController = (PricingParameterModuleController) fxmlLoader
						.getController();
				pricingParameterModuleControllersList.add(ppmController);
				addModulesErrors(ppmController.getErrors());
			} catch (IOException exception) {
				throw new TradistaTechnicalException(exception);
			}
			irSwapOptionVolatilitySurfaceTab.setContent(pane);
		}

		Callback<TableColumn<PricingParamProperty, String>, TableCell<PricingParamProperty, String>> paramNameCellFactory = new Callback<TableColumn<PricingParamProperty, String>, TableCell<PricingParamProperty, String>>() {
			public TableCell<PricingParamProperty, String> call(TableColumn<PricingParamProperty, String> p) {
				return new PricingParamNameEditingCell();
			}
		};

		Callback<TableColumn<PricingParamProperty, String>, TableCell<PricingParamProperty, String>> paramValueCellFactory = new Callback<TableColumn<PricingParamProperty, String>, TableCell<PricingParamProperty, String>>() {
			public TableCell<PricingParamProperty, String> call(TableColumn<PricingParamProperty, String> p) {
				return new PricingParamValueEditingCell();
			}
		};

		paramName.setCellValueFactory(cellData -> cellData.getValue().getName());

		paramName.setCellFactory(paramNameCellFactory);

		paramName.setOnEditCommit(new EventHandler<CellEditEvent<PricingParamProperty, String>>() {
			@Override
			public void handle(CellEditEvent<PricingParamProperty, String> t) {
				((PricingParamProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setName(t.getNewValue());
			}
		});

		paramValue.setCellFactory(paramValueCellFactory);

		paramValue.setOnEditCommit(new EventHandler<CellEditEvent<PricingParamProperty, String>>() {
			@Override
			public void handle(CellEditEvent<PricingParamProperty, String> t) {
				((PricingParamProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setValue(t.getNewValue());
			}
		});

		paramValue.setCellValueFactory(cellData -> cellData.getValue().getValue());

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParam);

		nameTextField.setPromptText("Parameter name");
		valueTextField.setPromptText("Parameter value");

		Callback<TableColumn<DiscountCurveProperty, Currency>, TableCell<DiscountCurveProperty, Currency>> currencyCellFactory = new Callback<TableColumn<DiscountCurveProperty, Currency>, TableCell<DiscountCurveProperty, Currency>>() {
			public TableCell<DiscountCurveProperty, Currency> call(TableColumn<DiscountCurveProperty, Currency> p) {
				return new DiscountCurveCurrencyEditingCell();
			}
		};

		Callback<TableColumn<DiscountCurveProperty, InterestRateCurve>, TableCell<DiscountCurveProperty, InterestRateCurve>> discountCurveCellFactory = new Callback<TableColumn<DiscountCurveProperty, InterestRateCurve>, TableCell<DiscountCurveProperty, InterestRateCurve>>() {
			public TableCell<DiscountCurveProperty, InterestRateCurve> call(
					TableColumn<DiscountCurveProperty, InterestRateCurve> p) {
				return new DiscountCurveInterestRateCurveEditingCell();
			}
		};

		currency.setCellValueFactory(new PropertyValueFactory<DiscountCurveProperty, Currency>("currency"));

		currency.setCellFactory(currencyCellFactory);

		currency.setOnEditCommit(new EventHandler<CellEditEvent<DiscountCurveProperty, Currency>>() {
			@Override
			public void handle(CellEditEvent<DiscountCurveProperty, Currency> t) {
				((DiscountCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setCurrency(t.getNewValue());
			}
		});

		currencyComboBox.setPromptText("Currency");

		discountCurve.setCellFactory(discountCurveCellFactory);

		discountCurve.setOnEditCommit(new EventHandler<CellEditEvent<DiscountCurveProperty, InterestRateCurve>>() {
			@Override
			public void handle(CellEditEvent<DiscountCurveProperty, InterestRateCurve> t) {
				((DiscountCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setCurve(t.getNewValue());
			}
		});

		discountCurve.setCellValueFactory(new PropertyValueFactory<DiscountCurveProperty, InterestRateCurve>("curve"));

		discountCurveComboBox.setPromptText("Discount Curve");

		Callback<TableColumn<IndexCurveProperty, Index>, TableCell<IndexCurveProperty, Index>> indexCellFactory = new Callback<TableColumn<IndexCurveProperty, Index>, TableCell<IndexCurveProperty, Index>>() {
			public TableCell<IndexCurveProperty, Index> call(TableColumn<IndexCurveProperty, Index> p) {
				return new IndexCurveIndexEditingCell();
			}
		};

		Callback<TableColumn<IndexCurveProperty, InterestRateCurve>, TableCell<IndexCurveProperty, InterestRateCurve>> indexCurveCellFactory = new Callback<TableColumn<IndexCurveProperty, InterestRateCurve>, TableCell<IndexCurveProperty, InterestRateCurve>>() {
			public TableCell<IndexCurveProperty, InterestRateCurve> call(
					TableColumn<IndexCurveProperty, InterestRateCurve> p) {
				return new IndexCurveInterestRateCurveEditingCell();
			}
		};

		index.setCellValueFactory(new PropertyValueFactory<IndexCurveProperty, Index>("index"));

		index.setCellFactory(indexCellFactory);

		index.setOnEditCommit(new EventHandler<CellEditEvent<IndexCurveProperty, Index>>() {
			@Override
			public void handle(CellEditEvent<IndexCurveProperty, Index> t) {
				((IndexCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setIndex(t.getNewValue());
			}
		});

		indexCurve.setCellFactory(indexCurveCellFactory);

		indexCurve.setOnEditCommit(new EventHandler<CellEditEvent<IndexCurveProperty, InterestRateCurve>>() {
			@Override
			public void handle(CellEditEvent<IndexCurveProperty, InterestRateCurve> t) {
				((IndexCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setCurve(t.getNewValue());
			}
		});

		indexCurve.setCellValueFactory(new PropertyValueFactory<IndexCurveProperty, InterestRateCurve>("curve"));

		indexComboBox.setPromptText("Index");

		indexCurveComboBox.setPromptText("Index Curve");

		Callback<TableColumn<FXCurveProperty, Currency>, TableCell<FXCurveProperty, Currency>> primaryCurrencyCellFactory = new Callback<TableColumn<FXCurveProperty, Currency>, TableCell<FXCurveProperty, Currency>>() {
			public TableCell<FXCurveProperty, Currency> call(TableColumn<FXCurveProperty, Currency> p) {
				return new FXCurvePrimaryCurrencyEditingCell();
			}
		};

		Callback<TableColumn<FXCurveProperty, Currency>, TableCell<FXCurveProperty, Currency>> quoteCurrencyCellFactory = new Callback<TableColumn<FXCurveProperty, Currency>, TableCell<FXCurveProperty, Currency>>() {
			public TableCell<FXCurveProperty, Currency> call(TableColumn<FXCurveProperty, Currency> p) {
				return new FXCurveQuoteCurrencyEditingCell();
			}
		};

		Callback<TableColumn<FXCurveProperty, FXCurve>, TableCell<FXCurveProperty, FXCurve>> fxCurveCellFactory = new Callback<TableColumn<FXCurveProperty, FXCurve>, TableCell<FXCurveProperty, FXCurve>>() {
			public TableCell<FXCurveProperty, FXCurve> call(TableColumn<FXCurveProperty, FXCurve> p) {
				return new FXCurveFXCurveEditingCell();
			}
		};

		primaryCurrency.setCellValueFactory(new PropertyValueFactory<FXCurveProperty, Currency>("primaryCurrency"));

		primaryCurrency.setCellFactory(primaryCurrencyCellFactory);

		primaryCurrency.setOnEditCommit(new EventHandler<CellEditEvent<FXCurveProperty, Currency>>() {
			@Override
			public void handle(CellEditEvent<FXCurveProperty, Currency> t) {
				((FXCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setPrimaryCurrency(t.getNewValue());
			}
		});

		primaryCurrencyComboBox.setPromptText("Primary Currency");

		fxCurveComboBox.setPromptText("FX Curve");

		quoteCurrency.setCellValueFactory(new PropertyValueFactory<FXCurveProperty, Currency>("quoteCurrency"));

		quoteCurrency.setCellFactory(quoteCurrencyCellFactory);

		quoteCurrency.setOnEditCommit(new EventHandler<CellEditEvent<FXCurveProperty, Currency>>() {
			@Override
			public void handle(CellEditEvent<FXCurveProperty, Currency> t) {
				((FXCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setQuoteCurrency(t.getNewValue());
			}
		});

		quoteCurrencyComboBox.setPromptText("Quote Currency");

		fxCurve.setCellFactory(fxCurveCellFactory);

		fxCurve.setOnEditCommit(new EventHandler<CellEditEvent<FXCurveProperty, FXCurve>>() {
			@Override
			public void handle(CellEditEvent<FXCurveProperty, FXCurve> t) {
				((FXCurveProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setCurve(t.getNewValue());
			}
		});

		fxCurve.setCellValueFactory(new PropertyValueFactory<FXCurveProperty, FXCurve>("curve"));

		Callback<TableColumn<CustomPricerProperty, String>, TableCell<CustomPricerProperty, String>> customPricerCellFactory = new Callback<TableColumn<CustomPricerProperty, String>, TableCell<CustomPricerProperty, String>>() {
			public TableCell<CustomPricerProperty, String> call(TableColumn<CustomPricerProperty, String> p) {
				return new CustomPricerPricerEditingCell();
			}
		};

		Callback<TableColumn<CustomPricerProperty, String>, TableCell<CustomPricerProperty, String>> productTypeCellFactory = new Callback<TableColumn<CustomPricerProperty, String>, TableCell<CustomPricerProperty, String>>() {
			public TableCell<CustomPricerProperty, String> call(TableColumn<CustomPricerProperty, String> p) {
				return new CustomPricerProductTypeEditingCell();
			}
		};

		productType.setCellValueFactory(cellData -> cellData.getValue().getProductType());

		productType.setCellFactory(productTypeCellFactory);

		productType.setOnEditCommit(new EventHandler<CellEditEvent<CustomPricerProperty, String>>() {
			@Override
			public void handle(CellEditEvent<CustomPricerProperty, String> t) {
				((CustomPricerProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setProductType(t.getNewValue());
			}
		});

		customPricer.setCellFactory(customPricerCellFactory);

		customPricer.setOnEditCommit(new EventHandler<CellEditEvent<CustomPricerProperty, String>>() {
			@Override
			public void handle(CellEditEvent<CustomPricerProperty, String> t) {
				((CustomPricerProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setCustomPricer(t.getNewValue());
			}
		});

		customPricer.setCellValueFactory(cellData -> cellData.getValue().getCustomPricer());

		customPricerTextField.setPromptText("Custom Pricer");

		try {
			Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
			TradistaGUIUtil.fillComboBox(quoteSets, quoteSetComboBox);
			quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
			canGetQuoteSet = true;
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
		}

		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterestRateCurves(),
					discountCurveComboBox, indexCurveComboBox);
			canGetDiscountCurve = true;
			canGetIndexCurve = true;
		} catch (TradistaTechnicalException tte) {
			canGetDiscountCurve = false;
			canGetIndexCurve = false;
		}

		try {
			TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), fxCurveComboBox);
			canGetFXCurve = true;
		} catch (TradistaTechnicalException tte) {
			canGetFXCurve = false;
		}

		updateWindow();
	}

	private void addModulesErrors(Map<String, List<String>> errors) {
		if (errors != null && !errors.isEmpty()) {
			if (modulesErrors == null) {
				modulesErrors = new HashMap<>();
			}
			for (Entry<String, List<String>> entry : errors.entrySet()) {
				List<String> errList = modulesErrors.get(entry.getKey());
				if (errList == null) {
					errList = new ArrayList<>();
				}
				errList.addAll(entry.getValue());
				modulesErrors.put(entry.getKey(), errList);
			}
		}
	}

	@FXML
	protected void load() {
		try {
			if (pricingParam.getSelectionModel().getSelectedItem() == null) {
				throw new TradistaBusinessException("Please select a Pricing Parameters Set");
			}

			pricingParameter = pricerBusinessDelegate
					.getPricingParameterById(pricingParam.getSelectionModel().getSelectedItem().getId());
			pricingParameterName.setText(pricingParameter.getName());
			// Pricing parameters loading
			ObservableList<PricingParamProperty> data = buildTableContent(pricingParameter);
			pricingParamTable.setItems(data);
			pricingParamTable.refresh();
			// Discount curves loading
			ObservableList<DiscountCurveProperty> discountCurveData = buildDiscountCurvesTableContent(pricingParameter);
			discountCurveTable.setItems(discountCurveData);
			discountCurveTable.refresh();
			// Index curves loading
			ObservableList<IndexCurveProperty> indexCurveData = buildIndexCurvesTableContent(pricingParameter);
			indexCurveTable.setItems(indexCurveData);
			indexCurveTable.refresh();
			// FX curves loading
			ObservableList<FXCurveProperty> fxCurveData = buildFXCurvesTableContent(pricingParameter);
			fxCurveTable.setItems(fxCurveData);
			fxCurveTable.refresh();
			// FX curves loading
			ObservableList<CustomPricerProperty> customPricerData = buildCustomPricersTableContent(pricingParameter);
			customPricerTable.setItems(customPricerData);
			customPricerTable.refresh();
			name.setText(pricingParameter.getName());
			TradistaGUIUtil.fillComboBox(quoteBusinessDelegate.getAllQuoteSets(), quoteSetComboBox);
			quoteSetComboBox.setValue(pricingParameter.getQuoteSet());
			if (pricingParameterModuleControllersList != null && !pricingParameterModuleControllersList.isEmpty()) {
				for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
					controller.load(pricingParameter);
				}
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		long oldPricingParameterId = 0;
		boolean ppLoaded = (pricingParameter != null);
		if (!ppLoaded) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "Please load a Pricing Parameters Set");
			alert.showAndWait();
		} else {
			try {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("pricing Parameter name");
				dialog.setHeaderText("Pricing Parameter name selection");
				dialog.setContentText("Please choose a Pricing Parameter name:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();

				if (result.isPresent()) {
					buildPricingParameter();
					oldPricingParameterId = pricingParameter.getId();
					pricingParameter.setName(result.get());
					pricingParameter.setId(0);
					pricingParameter.setId(pricerBusinessDelegate.savePricingParameter(pricingParameter));
					name.setText(pricingParameter.getName());
					pricingParameterName.setText(pricingParameter.getName());
					quoteSetComboBox.setValue(pricingParameter.getQuoteSet());

					// Refresh the pricing parameter combo box
					TradistaGUIUtil.fillPricingParameterComboBox(pricingParam);
				}
			} catch (TradistaBusinessException tbe) {
				pricingParameter.setId(oldPricingParameterId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void save() {
		try {
			if (pricingParameter == null) {
				throw new TradistaBusinessException("Please load a Pricing Parameters Set");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Save Pricing Parameters Set");
			confirmation.setHeaderText("Save Pricing Parameters Set");
			confirmation.setContentText("Do you want to save this Pricing Parameters Set?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				buildPricingParameter();
				pricingParameter.setId(pricingParam.getSelectionModel().getSelectedItem().getId());
				pricingParameter.setId(pricerBusinessDelegate.savePricingParameter(pricingParameter));
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void deleteParam() {
		int index = pricingParamTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			pricingParamTable.getItems().remove(index);
			pricingParamTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void deleteDiscountCurve() {
		int index = discountCurveTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			discountCurveTable.getItems().remove(index);
			discountCurveTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void deleteIndexCurve() {
		int index = indexCurveTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			indexCurveTable.getItems().remove(index);
			indexCurveTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void deleteFXCurve() {
		int index = fxCurveTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			fxCurveTable.getItems().remove(index);
			fxCurveTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void deleteCustomPricer() {
		int index = customPricerTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			customPricerTable.getItems().remove(index);
			customPricerTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void create() {
		try {
			PricingParameterCreatorDialog dialog = new PricingParameterCreatorDialog();
			Optional<PricingParameter> result = dialog.showAndWait();

			if (result.isPresent()) {
				PricingParameter pp = result.get();
				pricerBusinessDelegate.savePricingParameter(pp);
				TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParam);
				// Delete the Pricing Param table if the loaded PP doesn't
				// exist anymore.
				if (pricingParameter != null && !pricingParam.getItems().contains(pricingParameter)) {
					pricingParameter = null;
					pricingParamTable.setItems(null);
					discountCurveTable.setItems(null);
					indexCurveTable.setItems(null);
					fxCurveTable.setItems(null);
					customPricerTable.setItems(null);
					if (pricingParameterModuleControllersList != null
							&& !pricingParameterModuleControllersList.isEmpty()) {
						for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
							((TradistaController) controller).clear();
						}
					}
					name.setText(null);
					pricingParameterName.setText(null);
					quoteSetComboBox.setItems(null);
				}
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addParam() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (nameTextField.getText().isEmpty()) {
				errMsg.append(String.format("Please select a Parameter Name.%n"));
			} else {
				if (pricingParamTable.getItems().contains(new PricingParamProperty(nameTextField.getText(), null))) {
					errMsg.append(
							String.format("The Parameter Name %s is already in the list.%n", nameTextField.getText()));
				}
			}
			if (valueTextField.getText().isEmpty()) {
				errMsg.append(String.format("Please select a Parameter Value.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			pricingParamTable.getItems()
					.add(new PricingParamProperty(nameTextField.getText(), valueTextField.getText()));
			nameTextField.clear();
			valueTextField.clear();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addDiscountCurve() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (currencyComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Currency.%n"));
			} else {
				if (discountCurveTable.getItems()
						.contains(new DiscountCurveProperty(currencyComboBox.getValue(), null))) {
					errMsg.append(
							String.format("The Currency %s is already in the list.%n", currencyComboBox.getValue()));
				}
			}
			if (discountCurveComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Discount Curve.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			discountCurveTable.getItems()
					.add(new DiscountCurveProperty(currencyComboBox.getValue(), discountCurveComboBox.getValue()));

			currencyComboBox.getSelectionModel().clearSelection();
			discountCurveComboBox.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addIndexCurve() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (indexComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an Index.%n"));
			} else {
				if (indexCurveTable.getItems().contains(new IndexCurveProperty(indexComboBox.getValue(), null))) {
					errMsg.append(String.format("The Index %s is already in the list.%n", indexComboBox.getValue()));
				}
			}
			if (indexCurveComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an Index Curve.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			indexCurveTable.getItems()
					.add(new IndexCurveProperty(indexComboBox.getValue(), indexCurveComboBox.getValue()));
			indexComboBox.getSelectionModel().clearSelection();
			indexCurveComboBox.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addFXCurve() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (primaryCurrencyComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Primary Currency.%n"));
			} else {
				if (quoteCurrencyComboBox.getValue() == null) {
					errMsg.append(String.format("Please select a Quote Currency.%n"));
				} else if (quoteCurrencyComboBox.getValue().equals(primaryCurrencyComboBox.getValue())) {
					errMsg.append(String.format("Primary and Quote Currencies must be different.%n"));
				} else {
					if (fxCurveTable.getItems().contains(new FXCurveProperty(primaryCurrencyComboBox.getValue(),
							quoteCurrencyComboBox.getValue(), null))) {
						errMsg.append(String.format("The Currency Pair %s.%s is already in the list.%n",
								primaryCurrencyComboBox.getValue(), quoteCurrencyComboBox.getValue()));
					}
				}
			}
			if (fxCurveComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a FX Curve.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			fxCurveTable.getItems().add(new FXCurveProperty(primaryCurrencyComboBox.getValue(),
					quoteCurrencyComboBox.getValue(), fxCurveComboBox.getValue()));
			primaryCurrencyComboBox.getSelectionModel().clearSelection();
			quoteCurrencyComboBox.getSelectionModel().clearSelection();
			fxCurveComboBox.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addCustomPricer() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (productTypeComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Product Type.%n"));
			} else {
				if (customPricerTable.getItems()
						.contains(new CustomPricerProperty(productTypeComboBox.getValue(), null))) {
					errMsg.append(String.format("The Product Type %s is already in the list.%n",
							productTypeComboBox.getValue()));
				}
			}
			if (customPricerTextField.getText().isEmpty()) {
				errMsg.append(String.format("Please select a Custom Pricer.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			customPricerTable.getItems()
					.add(new CustomPricerProperty(productTypeComboBox.getValue(), customPricerTextField.getText()));
			productTypeComboBox.getSelectionModel().clearSelection();
			customPricerTextField.clear();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			if (pricingParameter == null) {
				throw new TradistaBusinessException("Please load a Pricing Parameters Set");
			}

			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Pricing Parameters Set");
			confirmation.setHeaderText("Delete Pricing Parameters Set");
			confirmation.setContentText(String.format("Do you want to delete this Pricing Parameters Set %s ?",
					pricingParameter.getName()));

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {

				pricerBusinessDelegate.deletePricingParameter(pricingParameter.getId());
				TradistaGUIUtil.fillPricingParameterComboBox(pricingParam);
				// Delete the Pricing Param table if the loaded PP doesn't
				// exist anymore.
				if (pricingParameter != null && !pricingParam.getItems().contains(pricingParameter)) {
					pricingParameter = null;
					pricingParamTable.setItems(null);
					discountCurveTable.setItems(null);
					indexCurveTable.setItems(null);
					fxCurveTable.setItems(null);
					customPricerTable.setItems(null);
					if (pricingParameterModuleControllersList != null
							&& !pricingParameterModuleControllersList.isEmpty()) {
						for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
							((TradistaController) controller).clear();
						}
					}
					name.setText(null);
					pricingParameterName.setText(null);
					quoteSetComboBox.setItems(null);
				}
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	private class PricingParamValueEditingCell extends TableCell<PricingParamProperty, String> {

		private TextField textField;

		public PricingParamValueEditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && textField.getText() != null && !textField.getText().equals("")) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {

						commitEdit(textField.getText());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private class PricingParamNameEditingCell extends TableCell<PricingParamProperty, String> {

		private TextField textField;

		public PricingParamNameEditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && textField.getText() != null && !textField.getText().equals("")) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {

				private boolean changing;

				private String oldValue;

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (arg2) {
						oldValue = textField.getText();
					}
					if (!arg2 && !changing && !textField.getText().equals(oldValue)) {
						StringBuilder errMsg = new StringBuilder();
						if (textField.getText().isEmpty()) {
							errMsg.append(String.format("The Name cannot be empty.%n"));
						}
						if (pricingParamTable.getItems()
								.contains(new PricingParamProperty(textField.getText(), null))) {
							errMsg.append(String.format("The Name %s is already in the list.%n", textField.getText()));

						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								textField.setText(oldValue);
								changing = false;
							});
						} else
							commitEdit(textField.getText());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private class DiscountCurveCurrencyEditingCell extends TableCell<DiscountCurveProperty, Currency> {

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
						if (discountCurveTable.getItems().contains(new DiscountCurveProperty(newCurrency, null))) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
									String.format("The Currency %s is already in the list.%n", newCurrency));
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
						if (!discountCurveTable.getItems()
								.contains(new DiscountCurveProperty(currencyComboBox.getValue(), null))) {
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

	private class DiscountCurveInterestRateCurveEditingCell
			extends TableCell<DiscountCurveProperty, InterestRateCurve> {

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
			interestRateCurveComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(interestRateCurveComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class IndexCurveIndexEditingCell extends TableCell<IndexCurveProperty, Index> {

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
						if (indexCurveTable.getItems().contains(new IndexCurveProperty(newIndex, null))) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
									String.format("The Index %s is already in the list.%n", newIndex));
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
						if (!indexCurveTable.getItems()
								.contains(new IndexCurveProperty(indexComboBox.getValue(), null))) {
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

	private class IndexCurveInterestRateCurveEditingCell extends TableCell<IndexCurveProperty, InterestRateCurve> {

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
			interestRateCurveComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(interestRateCurveComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class FXCurvePrimaryCurrencyEditingCell extends TableCell<FXCurveProperty, Currency> {

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
						if (fxCurveTable.getItems().contains(new FXCurveProperty(newCurrency,
								((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency(), null))) {
							errMsg.append(String.format("The Currency Pair %s.%s is already in the list.%n",
									newCurrency, ((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency()));
						}
						if (((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency() != null
								&& ((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency().equals(newCurrency)) {
							errMsg.append(String.format("Primary and Quote Currencies must be different.%n"));
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
						if (!fxCurveTable.getItems().contains(new FXCurveProperty(currencyComboBox.getValue(),
								((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency(), null))) {
							if (((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency() == null
									|| !((FXCurveProperty) getTableRow().getItem()).getQuoteCurrency()
											.equals(currencyComboBox.getValue())) {
								commitEdit(currencyComboBox.getValue());
							}
						}

					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class FXCurveQuoteCurrencyEditingCell extends TableCell<FXCurveProperty, Currency> {

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
						if (fxCurveTable.getItems().contains(new FXCurveProperty(
								((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency(), newCurrency, null))) {
							errMsg.append(String.format("The Currency Pair %s.%s is already in the list.%n",
									((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency(), newCurrency));
						}
						if (((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency() != null
								&& ((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency()
										.equals(newCurrency)) {
							errMsg.append(String.format("Primary and Quote Currencies must be different.%n"));
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
						if (!fxCurveTable.getItems()
								.contains(new FXCurveProperty(
										((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency(),
										currencyComboBox.getValue(), null))) {
							if (((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency() == null
									|| !((FXCurveProperty) getTableRow().getItem()).getPrimaryCurrency()
											.equals(currencyComboBox.getValue())) {
								commitEdit(currencyComboBox.getValue());
							}
						}

					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class FXCurveFXCurveEditingCell extends TableCell<FXCurveProperty, FXCurve> {

		private TradistaFXCurveComboBox fxCurveComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createFXCurveComboBox();
			FXCurve curve = fxCurveComboBox.getValue();
			if (curve != null) {
				setText(curve.toString());
			}
			setGraphic(fxCurveComboBox);
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
		public void updateItem(FXCurve item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (fxCurveComboBox != null) {
						fxCurveComboBox.setValue(getItem());
					}
					setGraphic(fxCurveComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createFXCurveComboBox() {
			fxCurveComboBox = new TradistaFXCurveComboBox();
			if (getItem() != null) {
				fxCurveComboBox.setValue(getItem());
			}
			fxCurveComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			fxCurveComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(fxCurveComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class CustomPricerPricerEditingCell extends TableCell<CustomPricerProperty, String> {

		private TextField textField;

		public CustomPricerPricerEditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && textField.getText() != null && !textField.getText().equals("")) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(textField.getText());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private class CustomPricerProductTypeEditingCell extends TableCell<CustomPricerProperty, String> {

		private TradistaProductTypeComboBox productTypeComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createProductTypeComboBox();
			String productType = productTypeComboBox.getValue();
			if (productType != null) {
				setText(productType.toString());
			}
			setGraphic(productTypeComboBox);
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
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (productTypeComboBox != null) {
						productTypeComboBox.setValue(getItem());
					}
					setGraphic(productTypeComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createProductTypeComboBox() {
			productTypeComboBox = new TradistaProductTypeComboBox();
			productTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends String> observableValue, String oldProductType,
						String newProductType) {
					if (!changing && newProductType != null && oldProductType != null
							&& !oldProductType.equals(newProductType)) {
						StringBuilder errMsg = new StringBuilder();
						if (customPricerTable.getItems().contains(new CustomPricerProperty(newProductType, null))) {
							errMsg.append(String.format("The Custom Pricer %s is already in the list.%n",
									((CustomPricerProperty) getTableRow().getItem()).getProductType(), newProductType));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								productTypeComboBox.setValue(oldProductType);
								changing = false;
							});
						}
					}
				}
			});
			if (getItem() != null) {
				productTypeComboBox.setValue(getItem());
			}
			productTypeComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			productTypeComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!customPricerTable.getItems()
								.contains(new CustomPricerProperty(productTypeComboBox.getValue(), null))) {
							commitEdit(productTypeComboBox.getValue());
						}

					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private ObservableList<PricingParamProperty> buildTableContent(PricingParameter data) {

		List<PricingParamProperty> pricingParamPropertyList = new ArrayList<PricingParamProperty>();

		for (Map.Entry<String, String> entry : data.getParams().entrySet()) {
			pricingParamPropertyList.add(new PricingParamProperty(entry.getKey(), entry.getValue()));
		}

		Collections.sort(pricingParamPropertyList);

		return FXCollections.observableArrayList(pricingParamPropertyList);

	}

	private ObservableList<DiscountCurveProperty> buildDiscountCurvesTableContent(PricingParameter data) {

		List<DiscountCurveProperty> discountCurvesPropertyList = new ArrayList<DiscountCurveProperty>();

		for (Map.Entry<Currency, InterestRateCurve> entry : data.getDiscountCurves().entrySet()) {
			discountCurvesPropertyList.add(new DiscountCurveProperty(entry.getKey(), entry.getValue()));
		}

		Collections.sort(discountCurvesPropertyList);

		return FXCollections.observableArrayList(discountCurvesPropertyList);

	}

	private ObservableList<IndexCurveProperty> buildIndexCurvesTableContent(PricingParameter data) {

		List<IndexCurveProperty> indexCurvesPropertyList = new ArrayList<IndexCurveProperty>();

		for (Map.Entry<Index, InterestRateCurve> entry : data.getIndexCurves().entrySet()) {
			indexCurvesPropertyList.add(new IndexCurveProperty(entry.getKey(), entry.getValue()));
		}

		Collections.sort(indexCurvesPropertyList);

		return FXCollections.observableArrayList(indexCurvesPropertyList);

	}

	private ObservableList<FXCurveProperty> buildFXCurvesTableContent(PricingParameter data) {

		List<FXCurveProperty> fxCurvesPropertyList = new ArrayList<FXCurveProperty>();

		for (Map.Entry<CurrencyPair, FXCurve> entry : data.getFxCurves().entrySet()) {
			fxCurvesPropertyList.add(new FXCurveProperty(entry.getKey().getPrimaryCurrency(),
					entry.getKey().getQuoteCurrency(), entry.getValue()));
		}

		Collections.sort(fxCurvesPropertyList);

		return FXCollections.observableArrayList(fxCurvesPropertyList);

	}

	private ObservableList<CustomPricerProperty> buildCustomPricersTableContent(PricingParameter data) {

		List<CustomPricerProperty> customPricerPropertyList = new ArrayList<CustomPricerProperty>();

		for (Map.Entry<String, String> entry : data.getCustomPricers().entrySet()) {
			customPricerPropertyList.add(new CustomPricerProperty(entry.getKey(), entry.getValue()));
		}

		Collections.sort(customPricerPropertyList);

		return FXCollections.observableArrayList(customPricerPropertyList);

	}

	private void buildPricingParameter() {
		if (pricingParameter == null) {
			pricingParameter = new PricingParameter();
		}
		pricingParameter.setName(pricingParam.getSelectionModel().getSelectedItem().getName());
		pricingParameter.setQuoteSet(quoteSetComboBox.getValue());
		pricingParameter.setProcessingOrg(ClientUtil.getCurrentUser().getProcessingOrg());
		pricingParameter.getParams().clear();
		for (PricingParamProperty prop : pricingParamTable.getItems()) {
			pricingParameter.getParams().put(prop.getName().toString(), prop.getValue().toString());
		}
		pricingParameter.getDiscountCurves().clear();
		for (DiscountCurveProperty prop : discountCurveTable.getItems()) {
			pricingParameter.getDiscountCurves().put((Currency) prop.getCurrency(),
					(InterestRateCurve) prop.getCurve());
		}
		pricingParameter.getIndexCurves().clear();
		for (IndexCurveProperty prop : indexCurveTable.getItems()) {
			pricingParameter.getIndexCurves().put((Index) prop.getIndex(), (InterestRateCurve) prop.getCurve());
		}
		pricingParameter.getFxCurves().clear();
		for (FXCurveProperty prop : fxCurveTable.getItems()) {
			pricingParameter.getFxCurves().put(
					new CurrencyPair((Currency) prop.getPrimaryCurrency(), (Currency) prop.getQuoteCurrency()),
					(FXCurve) prop.getCurve());
		}
		pricingParameter.getCustomPricers().clear();
		for (CustomPricerProperty prop : customPricerTable.getItems()) {
			pricingParameter.getCustomPricers().put(prop.getProductType().toString(),
					prop.getCustomPricer().toString());
		}
		pricingParameter.getModules().clear();
		if (pricingParameterModuleControllersList != null && !pricingParameterModuleControllersList.isEmpty()) {
			for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
				pricingParameter.getModules().add(controller.buildModule());
			}
		}
	}

	protected class PricingParamProperty implements Comparable<PricingParamProperty> {

		private final StringProperty name;
		private final StringProperty value;

		private PricingParamProperty(String name, String value) {
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}

		public StringProperty getName() {
			return name;
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public StringProperty getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value.set(value);
		}

		@Override
		public int compareTo(PricingParamProperty o) {
			return getName().toString().compareTo(o.getName().toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
			PricingParamProperty other = (PricingParamProperty) obj;
			if (getName() == null) {
				if (other.getName() != null)
					return false;
			} else if (!getName().equals(other.getName()))
				return false;
			return true;
		}

	}

	protected class DiscountCurveProperty implements Comparable<DiscountCurveProperty> {

		private final SimpleObjectProperty currency;
		private final SimpleObjectProperty curve;

		private DiscountCurveProperty(Object currency, Object curve) {
			this.currency = new SimpleObjectProperty(currency);
			this.curve = new SimpleObjectProperty(curve);
		}

		public Object getCurrency() {
			return currency.get();
		}

		public void setCurrency(Object currency) {
			this.currency.set(currency);
		}

		public Object getCurve() {
			return curve.get();
		}

		public void setCurve(Object curve) {
			this.curve.set(curve);
		}

		@Override
		public int compareTo(DiscountCurveProperty o) {
			return getCurrency().toString().compareTo(o.getCurrency().toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getCurrency() == null) ? 0 : getCurrency().hashCode());
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
			DiscountCurveProperty other = (DiscountCurveProperty) obj;
			if (getCurrency() == null) {
				if (other.getCurrency() != null)
					return false;
			} else if (!getCurrency().equals(other.getCurrency()))
				return false;
			return true;
		}

	}

	protected class IndexCurveProperty implements Comparable<IndexCurveProperty> {

		private final SimpleObjectProperty index;
		private final SimpleObjectProperty curve;

		private IndexCurveProperty(Object index, Object curve) {
			this.index = new SimpleObjectProperty(index);
			this.curve = new SimpleObjectProperty(curve);
		}

		public Object getIndex() {
			return index.get();
		}

		public void setIndex(Object index) {
			this.index.set(index);
		}

		public Object getCurve() {
			return curve.get();
		}

		public void setCurve(Object curve) {
			this.curve.set(curve);
		}

		@Override
		public int compareTo(IndexCurveProperty o) {
			return getIndex().toString().compareTo(o.getIndex().toString());
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
			IndexCurveProperty other = (IndexCurveProperty) obj;
			if (getIndex() == null) {
				if (other.getIndex() != null)
					return false;
			} else if (!getIndex().equals(other.getIndex()))
				return false;
			return true;
		}

	}

	protected class FXCurveProperty implements Comparable<FXCurveProperty> {

		private final SimpleObjectProperty primaryCurrency;
		private final SimpleObjectProperty quoteCurrency;
		private final SimpleObjectProperty curve;

		private FXCurveProperty(Object primaryCurrency, Object quoteCurrency, Object curve) {
			this.primaryCurrency = new SimpleObjectProperty(primaryCurrency);
			this.quoteCurrency = new SimpleObjectProperty(quoteCurrency);
			this.curve = new SimpleObjectProperty(curve);
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

		public Object getCurve() {
			return curve.get();
		}

		public void setCurve(Object curve) {
			this.curve.set(curve);
		}

		@Override
		public int compareTo(FXCurveProperty o) {
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
			FXCurveProperty other = (FXCurveProperty) obj;
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

	protected class CustomPricerProperty implements Comparable<CustomPricerProperty> {

		private final StringProperty productType;
		private final StringProperty customPricer;

		private CustomPricerProperty(String productType, String customPricer) {
			this.productType = new SimpleStringProperty(productType);
			this.customPricer = new SimpleStringProperty(customPricer);
		}

		public StringProperty getProductType() {
			return productType;
		}

		public void setProductType(String productType) {
			this.productType.set(productType);
		}

		public StringProperty getCustomPricer() {
			return customPricer;
		}

		public void setCustomPricer(String customPricer) {
			this.customPricer.set(customPricer);
		}

		@Override
		public int compareTo(CustomPricerProperty o) {
			return getProductType().toString().compareTo(o.getProductType().toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getProductType() == null) ? 0 : getProductType().hashCode());
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
			CustomPricerProperty other = (CustomPricerProperty) obj;
			if (getProductType() == null) {
				if (other.getProductType() != null)
					return false;
			} else if (!getProductType().equals(other.getProductType()))
				return false;
			return true;
		}

	}

	@Override
	@FXML
	public void refresh() {
		try {
			Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
			TradistaGUIUtil.fillComboBox(quoteSets, quoteSetComboBox);
			quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
			canGetQuoteSet = true;
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
		}
		TradistaGUIUtil.fillCurrencyComboBox(currencyComboBox, primaryCurrencyComboBox, quoteCurrencyComboBox);
		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterestRateCurves(),
					discountCurveComboBox, indexCurveComboBox);
			canGetDiscountCurve = true;
			canGetIndexCurve = true;
		} catch (TradistaTechnicalException tte) {
			canGetDiscountCurve = false;
			canGetIndexCurve = false;
		}

		try {
			TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), fxCurveComboBox);
			canGetFXCurve = true;
		} catch (TradistaTechnicalException tte) {
			canGetFXCurve = false;
		}
		PricingParameter pp = pricingParam.getValue();
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParam);
		if (modulesErrors != null) {
			modulesErrors.clear();
		}
		if (pricingParameterModuleControllersList != null && !pricingParameterModuleControllersList.isEmpty()) {
			for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
				((TradistaController) controller).refresh();
				addModulesErrors(controller.getErrors());
			}
		}

		if (pp != null && !pp.equals(pricingParam.getValue())) {
			pricingParameter = null;
			pricingParamTable.setItems(null);
			discountCurveTable.setItems(null);
			indexCurveTable.setItems(null);
			fxCurveTable.setItems(null);
			customPricerTable.setItems(null);
			if (pricingParameterModuleControllersList != null && !pricingParameterModuleControllersList.isEmpty()) {
				for (PricingParameterModuleController controller : pricingParameterModuleControllersList) {
					((TradistaController) controller).clear();
				}
			}
			name.setText(null);
			pricingParameterName.setText(null);
			quoteSetComboBox.setItems(null);
		}

		updateWindow();
	}

	protected void updateWindow() {
		Map<String, List<String>> errors = new HashMap<String, List<String>>();
		StringBuilder errMsg = new StringBuilder();
		boolean isError = false;

		quoteSetComboBox.setDisable(!quoteSetExists || !canGetQuoteSet);
		createButton.setDisable(!quoteSetExists || !canGetQuoteSet);
		discountCurveComboBox.setDisable(!canGetDiscountCurve);
		indexCurveComboBox.setDisable(!canGetIndexCurve);
		fxCurveComboBox.setDisable(!canGetFXCurve);
		addDiscountCurveButton.setDisable(!canGetDiscountCurve);
		addIndexCurveButton.setDisable(!canGetIndexCurve);
		addFXCurveButton.setDisable(!canGetFXCurve);
		currencyComboBox.setDisable(!canGetDiscountCurve);
		indexComboBox.setDisable(!canGetIndexCurve);
		primaryCurrencyComboBox.setDisable(!canGetFXCurve);
		quoteCurrencyComboBox.setDisable(!canGetFXCurve);

		if (!quoteSetExists) {
			TradistaGUIUtil.unapplyErrorStyle(marketDataMessage);
			TradistaGUIUtil.applyWarningStyle(marketDataMessage);
			marketDataMessage.setText("There is no quote set, please create one.");
		}

		if (!canGetQuoteSet) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("quote sets");
			errors.put("get", err);
		}
		if (!canGetDiscountCurve) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("discount curves");
			errors.put("get", err);
		}
		if (!canGetIndexCurve) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("index curves");
			errors.put("get", err);
		}
		if (!canGetFXCurve) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("fx curves");
			errors.put("get", err);
		}
		if (modulesErrors != null && !modulesErrors.isEmpty()) {
			for (Entry<String, List<String>> entry : modulesErrors.entrySet()) {
				List<String> errList = errors.get(entry.getKey());
				if (errList == null) {
					errList = new ArrayList<>();
				}
				errList.addAll(entry.getValue());
				modulesErrors.put(entry.getKey(), errList);
			}
		}

		isError = !errors.isEmpty();
		for (Map.Entry<String, List<String>> errCat : errors.entrySet()) {
			errMsg.append("Cannot ").append(errCat.getKey());
			if (errCat.getValue().size() > 1) {
				errMsg.append(":");
			}
			errMsg.append(" ");
			for (String err : errCat.getValue()) {
				errMsg.append(err + ", ");
			}
			errMsg.delete(errMsg.length() - 2, errMsg.length());
			errMsg.append(".");
			errMsg.append(System.lineSeparator());
		}
		errMsg.append("Please contact support.");

		if (isError) {
			TradistaGUIUtil.unapplyWarningStyle(marketDataMessage);
			TradistaGUIUtil.applyErrorStyle(marketDataMessage);
			marketDataMessage.setText(errMsg.toString());
		}

		marketDataMessage.setVisible(isError || !quoteSetExists);

	}

}