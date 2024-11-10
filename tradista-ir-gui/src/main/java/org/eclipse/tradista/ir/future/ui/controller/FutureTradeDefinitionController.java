package org.eclipse.tradista.ir.future.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.ui.controller.CashFlowProperty;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.ui.controller.QuoteProperty;
import org.eclipse.tradista.core.marketdata.ui.publisher.MarketDataPublisher;
import org.eclipse.tradista.core.pricing.pricer.Parameterizable;
import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.eclipse.tradista.ir.future.model.FutureTrade;
import org.eclipse.tradista.ir.future.service.FutureBusinessDelegate;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationBusinessDelegate;
import org.eclipse.tradista.ir.future.service.FuturePricerBusinessDelegate;
import org.eclipse.tradista.ir.future.service.FutureTradeBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class FutureTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private DatePicker maturityDate;

	@FXML
	private TextField quantity;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<Book> book;

	@FXML
	private Label notionalAmount;

	@FXML
	private TextField futurePrice;

	@FXML
	private Label currency;

	@FXML
	private Label future;

	@FXML
	private Label referenceRate;

	@FXML
	private ComboBox<FutureContractSpecification> name;

	@FXML
	private ComboBox<String> symbol;

	@FXML
	private Label dayCountConvention;

	@FXML
	private Label pricerQuoteSetLabel;

	@FXML
	private Label priceWarning;

	// Quotes

	@FXML
	private TableColumn<QuoteProperty, String> quoteName;

	@FXML
	private TableColumn<QuoteProperty, String> quoteDate;

	@FXML
	private TableColumn<QuoteProperty, String> quoteType;

	@FXML
	private TableColumn<QuoteProperty, String> quoteBid;

	@FXML
	private TableColumn<QuoteProperty, String> quoteAsk;

	@FXML
	private TableColumn<QuoteProperty, String> quoteOpen;

	@FXML
	private TableColumn<QuoteProperty, String> quoteClose;

	@FXML
	private TableColumn<QuoteProperty, String> quoteHigh;

	@FXML
	private TableColumn<QuoteProperty, String> quoteLow;

	@FXML
	private TableColumn<QuoteProperty, String> quoteLast;

	@FXML
	private TableColumn<QuoteProperty, String> quoteEnteredDate;

	@FXML
	private TableColumn<QuoteProperty, String> quoteSourceName;

	@FXML
	private TableView<CashFlowProperty> cashFlowsTable;

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

	// Pricer

	@FXML
	private ComboBox<PricingParameter> pricingParameter;

	@FXML
	private ComboBox<PricerMeasure> pricingMeasure;

	@FXML
	private ComboBox<String> pricingMethod;

	@FXML
	private ComboBox<Currency> pricingCurrency;

	@FXML
	private DatePicker pricingDate;

	@FXML
	private Label pricerLabel;

	@FXML
	private Label result;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private FutureContractSpecificationBusinessDelegate futureContractSpecificationBusinessDelegate;

	private FutureTradeBusinessDelegate futureTradeBusinessDelegate;

	private FuturePricerBusinessDelegate futurePricerBusinessDelegate;

	private FutureBusinessDelegate futureBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FutureTrade trade;

	@FXML
	private Label tradeType;

	@FXML
	private Label cfPricingDate;

	@FXML
	private Label cfDiscountCurve;

	@FXML
	private Button generate;

	@FXML
	private TradistaBookPieChart bookChartPane;

	private ChangeListener<String> symbolListener;

	private ChangeListener<FutureContractSpecification> fcsListener;

	private ChangeListener<LocalDate> tradeDateListener;

	private ChangeListener<Trade.Direction> directionListener;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(1));
		tradeType.setText("Future Trade");

		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		futureTradeBusinessDelegate = new FutureTradeBusinessDelegate();
		futurePricerBusinessDelegate = new FuturePricerBusinessDelegate();
		futureBusinessDelegate = new FutureBusinessDelegate();
		futureContractSpecificationBusinessDelegate = new FutureContractSpecificationBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

		// Quotes initialization
		quoteName.setCellValueFactory(cellData -> cellData.getValue().getName());
		quoteDate.setCellValueFactory(cellData -> cellData.getValue().getDate());
		quoteType.setCellValueFactory(cellData -> cellData.getValue().getType());

		quoteBid.setCellValueFactory(cellData -> cellData.getValue().getBid());
		quoteAsk.setCellValueFactory(cellData -> cellData.getValue().getAsk());
		quoteOpen.setCellValueFactory(cellData -> cellData.getValue().getOpen());
		quoteClose.setCellValueFactory(cellData -> cellData.getValue().getClose());
		quoteHigh.setCellValueFactory(cellData -> cellData.getValue().getHigh());
		quoteLow.setCellValueFactory(cellData -> cellData.getValue().getLow());
		quoteLast.setCellValueFactory(cellData -> cellData.getValue().getLast());
		quoteEnteredDate.setCellValueFactory(cellData -> cellData.getValue().getEnteredDate());
		quoteSourceName.setCellValueFactory(cellData -> cellData.getValue().getSourceName());

		// CashFlows table
		cfDate.setCellValueFactory(cellData -> cellData.getValue().getDate());
		cfAmount.setCellValueFactory(cellData -> cellData.getValue().getAmount());
		cfCurrency.setCellValueFactory(cellData -> cellData.getValue().getCurrency());
		cfPurpose.setCellValueFactory(cellData -> cellData.getValue().getPurpose());
		cfDirection.setCellValueFactory(cellData -> cellData.getValue().getDirection());
		cfDiscountedAmount.setCellValueFactory(cellData -> cellData.getValue().getDiscountedAmount());
		cfDiscountFactor.setCellValueFactory(cellData -> cellData.getValue().getDiscountFactor());

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<QuoteSet>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> observableValue, QuoteSet oldValue,
					QuoteSet newValue) {
				if (newValue != null && name.getValue() != null && symbol.getValue() != null
						&& selectedQuoteDate.getValue() != null) {
					String futureRate = Future.FUTURE + "." + name.getValue().getName() + "." + symbol.getValue();
					String futureReferenceRate = Index.INDEX + "." + name.getValue().getReferenceRateIndex().getName()
							+ "." + name.getValue().getReferenceRateIndexTenor();
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), futureRate, futureReferenceRate);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				if (newValue != null && name.getValue() != null) {
					String futureRate = Future.FUTURE + "." + name.getValue().getName() + "." + symbol.getValue();
					String futureReferenceRate = Index.INDEX + "." + name.getValue().getReferenceRateIndex().getName()
							+ "." + name.getValue().getReferenceRateIndexTenor();
					fillQuotesTable(selectedQuoteSet.getValue(), newValue, futureRate, futureReferenceRate);
				}
			}
		});

		pricingDate.setValue(LocalDate.now());

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricerMeasure>() {
			@Override
			public void changed(ObservableValue<? extends PricerMeasure> observableValue,
					PricerMeasure oldPricerMeasure, PricerMeasure newPricerMeasure) {
				// newPricerMeasure is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricerMeasure != null) {
					TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingMethods(newPricerMeasure),
							pricingMethod);
				}
			}
		});

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricingParameter>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> observableValue,
					PricingParameter oldPricingParam, PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(Future.FUTURE, newPricingParam);
					} catch (TradistaBusinessException tbe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());

					if (name.getValue().getCurrency() != null) {
						InterestRateCurve discountCurve = newPricingParam
								.getDiscountCurve(name.getValue().getCurrency());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), name.getValue().getCurrency()));
							TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
						}
					}
				}
			}
		});

		symbol.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldSymbol, String newSymbol) {
				if (newSymbol != null) {
					try {
						maturityDate.setValue(futureContractSpecificationBusinessDelegate
								.getSymbolMaturityDate(name.getValue(), newSymbol));
					} catch (TradistaBusinessException tbe) {
						// Should never happen here.
					}
				}
			}
		});

		TradistaGUIUtil.fillComboBox(futureContractSpecificationBusinessDelegate.getAllFutureContractSpecifications(),
				name);
		fillSymbol();
		loadContractSpecification(name.getValue());
		name.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FutureContractSpecification>() {
			@Override
			public void changed(ObservableValue<? extends FutureContractSpecification> observableValue,
					FutureContractSpecification oldSpec, FutureContractSpecification newSpec) {
				if (newSpec != null) {
					FutureContractSpecification spec = futureContractSpecificationBusinessDelegate
							.getFutureContractSpecificationById(newSpec.getId());
					loadContractSpecification(spec);
					fillSymbol();
					if (selectedQuoteDate.getValue() != null) {
						String futureRate = Future.FUTURE + "." + newSpec.getName() + "." + symbol.getValue();
						String futureReferenceRate = Index.INDEX + "." + newSpec.getReferenceRateIndex().getName() + "."
								+ name.getValue().getReferenceRateIndexTenor();
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), futureRate,
								futureReferenceRate);
					}
					settlementDate.setValue(null);

					final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<DatePicker, DateCell>() {
						public DateCell call(final DatePicker datePicker) {
							return new DateCell() {

								private boolean isAvailable(LocalDate date) {
									if (name.getValue() != null) {
										try {
											return futureContractSpecificationBusinessDelegate.isBusinessDay(newSpec,
													date);
										} catch (TradistaBusinessException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										return false;
									} else {
										return true;
									}
								}

								@Override
								public void updateItem(LocalDate item, boolean empty) {
									super.updateItem(item, empty);
									if (!isAvailable(item)) {
										setDisable(true);
									}
								}
							};
						}
					};

					tradeDate.setDayCellFactory(businessDayCellFactory);
					selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

					tradeDate.setValue(null);
					selectedQuoteDate.setValue(null);

					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue()
								.getDiscountCurve(newSpec.getCurrency());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newSpec.getCurrency()));
							TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
						}
					}
				}
			}
		});

		tradeDate.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				if (tradeDate.getValue() != null) {
					settlementDate.setValue(tradeDate.getValue());
				}
			}
		});

		tradeDateListener = new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null && !symbol.getValue().isEmpty() && selectedQuoteSet.getValue() != null
						&& name.getValue() != null) {
					if (!newDate.isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(newDate, buySell.getValue(),
											name.getValue().getName(), symbol.getValue(),

											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Future %s price (%s) is not available as of Trade Date (%tD) in QuoteSet %s.",
													name.getValue(), symbol.getValue(), newDate,
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											futurePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get future %s (%s) price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												name.getValue(), symbol.getValue(), newDate,
												selectedQuoteSet.getValue()));
										priceWarning.setVisible(true);
									});
								}
								return null;
							}
						};
						exec.submit(task);
					}
				}
			}
		};

		directionListener = new ChangeListener<Trade.Direction>() {
			@Override
			public void changed(ObservableValue<? extends Trade.Direction> arg0, Trade.Direction arg1,
					Trade.Direction newDirection) {
				if (tradeDate.getValue() != null && name.getValue() != null && !symbol.getValue().isEmpty()
						&& selectedQuoteSet.getValue() != null) {
					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(tradeDate.getValue(), newDirection,
											name.getValue().getName(), symbol.getValue(),
											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Future %s price (%s) is not available as of Trade Date (%tD) in QuoteSet %s.",
													name.getValue(), symbol.getValue(), tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											futurePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get future %s price (%s) as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												name.getValue(), symbol.getValue(), tradeDate.getValue(),
												selectedQuoteSet.getValue()));
										priceWarning.setVisible(true);
									});
								}
								return null;
							}
						};
						exec.submit(task);
					}

				}
			}
		};

		symbolListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String newSymbol) {
				if (tradeDate.getValue() != null && name.getValue() != null && newSymbol != null
						&& selectedQuoteSet.getValue() != null) {
					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(tradeDate.getValue(), buySell.getValue(),
											name.getValue().getName(), newSymbol, selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Future %s price (%s) is not available as of Trade Date (%tD) in QuoteSet %s.",
													name.getValue(), newSymbol, tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											futurePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get future %s (%s) price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												name.getValue(), newSymbol, tradeDate.getValue(),
												selectedQuoteSet.getValue()));
										priceWarning.setVisible(true);
									});
								}
								return null;
							}
						};
						exec.submit(task);
					}

				}
			}
		};

		fcsListener = new ChangeListener<FutureContractSpecification>() {
			@Override
			public void changed(ObservableValue<? extends FutureContractSpecification> arg0,
					FutureContractSpecification arg1, FutureContractSpecification newFcs) {
				if (tradeDate.getValue() != null && name.getValue() != null && newFcs != null
						&& !symbol.getValue().isEmpty() && selectedQuoteSet.getValue() != null) {
					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(tradeDate.getValue(), buySell.getValue(),
											newFcs.getName(), symbol.getValue(),

											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Future %s price (%s) is not available as of Trade Date (%tD) in QuoteSet %s.",
													newFcs, symbol.getValue(), tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											futurePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get future %s (%s) price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												newFcs, symbol.getValue(), tradeDate.getValue(),
												selectedQuoteSet.getValue()));
										priceWarning.setVisible(true);
									});
								}
								return null;
							}
						};
						exec.submit(task);
					}

				}
			}
		};

		addListeners();

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
	}

	private void addListeners() {
		tradeDate.valueProperty().addListener(tradeDateListener);
		buySell.valueProperty().addListener(directionListener);
		name.valueProperty().addListener(fcsListener);
		symbol.valueProperty().addListener(symbolListener);
	}

	private BigDecimal getTradePrice(LocalDate tradeDate, Trade.Direction direction, String fcName, String symbol,
			long quoteSetId) {
		QuoteValue qv = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId,
				Future.FUTURE + "." + fcName + "." + symbol, QuoteType.FUTURE_PRICE, tradeDate);
		BigDecimal futurePrice = null;
		if (qv == null) {
			return null;
		}
		if (qv.getClose() != null) {
			futurePrice = qv.getClose();
		}
		if (direction.equals(Trade.Direction.BUY)) {
			if (qv.getAsk() != null) {
				futurePrice = qv.getAsk();
			}
		} else {
			if (qv.getBid() != null) {
				futurePrice = qv.getBid();
			}
		}
		if (futurePrice == null) {
			futurePrice = qv.getLast();
		}
		if (futurePrice != null) {
			return futurePrice;
		}
		return null;
	}

	private void fillSymbol() {
		LocalDate tradeDate;
		if (this.tradeDate.getValue() != null) {
			tradeDate = this.tradeDate.getValue();
		} else {
			tradeDate = LocalDate.now();
		}
		Set<LocalDate> maturityDates = new DateRuleBusinessDelegate()
				.generateDates(name.getValue().getMaturityDatesDateRule(), tradeDate, Period.ofYears(100));
		Set<String> symbols = new LinkedHashSet<String>(maturityDates.size());
		if (maturityDates != null && !maturityDates.isEmpty()) {
			for (LocalDate date : maturityDates) {
				symbols.add(Month.of(date.getMonthValue()).toString().substring(0, 3)
						+ Year.of(date.getYear()).toString().substring(2));
			}
		}
		TradistaGUIUtil.fillComboBox(symbols, symbol);
	}

	private void loadContractSpecification(FutureContractSpecification spec) {
		currency.setText(spec.getCurrency().getIsoCode());
		referenceRate.setText(spec.getReferenceRateIndex().getName() + " " + spec.getReferenceRateIndexTenor());
		dayCountConvention.setText(spec.getDayCountConvention().getName());
		notionalAmount.setText(TradistaGUIUtil.formatAmount(spec.getNotional()));
		this.future.setText(spec.getName());
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Trade");
		confirmation.setHeaderText("Save Trade");
		confirmation.setContentText("Do you want to save this Trade?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();
				buildTrade();
				trade.setId(futureTradeBusinessDelegate.saveFutureTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Copy Trade");
		confirmation.setHeaderText("Copy Trade");
		confirmation.setContentText("Do you want to copy this Trade?");
		long oldTradeId = 0;
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();
				buildTrade();
				oldTradeId = trade.getId();
				trade.setId(0);
				trade.setId(futureTradeBusinessDelegate.saveFutureTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				trade.setId(oldTradeId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		FutureTrade futureTrade;
		long tradeId = 0;
		try {
			try {
				if (!load.getText().isEmpty()) {
					tradeId = Long.parseLong(load.getText());
				} else {
					throw new TradistaBusinessException("Please specify a trade id.");
				}
			} catch (NumberFormatException nfe) {
				throw new TradistaBusinessException(String.format("The trade id is incorrect: %s", load.getText()));
			}

			futureTrade = futureTradeBusinessDelegate.getFutureTradeById(tradeId);
			if (futureTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", Future.FUTURE, load.getText()));
			}
			load(futureTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FutureTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		maturityDate.setValue(trade.getMaturityDate());
		currency.setText(trade.getProduct().getCurrency().toString());
		dayCountConvention.setText(trade.getProduct().getDayCountConvention().toString());
		future.setText(trade.getProduct().getContractSpecification().getName());
		name.setValue(trade.getProduct().getContractSpecification());
		symbol.getSelectionModel().select(trade.getProduct().getSymbol());
		futurePrice.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		referenceRate.setText(trade.getReferenceRateIndex().toString() + " " + trade.getReferenceRateIndexTenor());
		notionalAmount
				.setText(TradistaGUIUtil.formatAmount(trade.getProduct().getContractSpecification().getNotional()));
		tradeDate.setValue(trade.getTradeDate());
		quantity.setText(TradistaGUIUtil.formatAmount(trade.getQuantity()));
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = futurePricerBusinessDelegate.generateCashFlows(trade,
					pricingParameter.getValue(), pricingDate.getValue());
			if (cashFlows != null) {
				cashFlowsTable.setItems(
						FXCollections.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cashFlows)));
				generate.setText("Refresh");
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FutureTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			if (!futurePrice.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(futurePrice.getText(), "Future Price"));
			}
			trade.setSettlementDate(settlementDate.getValue());

			trade.setBook(book.getValue());
			trade.setCounterparty(counterparty.getValue());

			if (!quantity.getText().isEmpty()) {
				trade.setQuantity(TradistaGUIUtil.parseAmount(quantity.getText(), "Quantity"));
			}

			// Construct the future product. If it doesn't exist in the system,
			// it
			// will be created.
			Future future = null;
			future = futureBusinessDelegate.getFutureByContractSpecificationAndSymbol(name.getValue().getName(),
					symbol.getValue());
			if (future == null) {
				future = new Future(symbol.getValue(), name.getValue());
				future.setCreationDate(LocalDate.now());
				future.setMaturityDate(maturityDate.getValue());
			}
			trade.setProduct(future);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void price() {
		try {
			checkAmounts();

			buildTrade();

			result.setText(TradistaGUIUtil.formatAmount(
					pricerBusinessDelegate.calculate(trade, pricingParameter.getValue(), pricingCurrency.getValue(),
							pricingDate.getValue(), pricingMeasure.getValue(), pricingMethod.getValue())));

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		tradeDate.setValue(null);
		settlementDate.setValue(null);
		futurePrice.clear();
		quantity.clear();
	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
		FutureContractSpecification fcs = name.getValue();
		LocalDate tDate = tradeDate.getValue();
		LocalDate qDate = selectedQuoteDate.getValue();
		TradistaGUIUtil.fillComboBox(futureContractSpecificationBusinessDelegate.getAllFutureContractSpecifications(),
				name);
		if (fcs != null && fcs.equals(name.getValue())) {
			tradeDate.setValue(tDate);
			settlementDate.setValue(tDate);
			selectedQuoteDate.setValue(qDate);
		}
	}

	@Override
	public void update(TradistaPublisher publisher) {
		super.update(publisher);
		if (publisher instanceof MarketDataPublisher) {
			if (!publisher.isError()) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Set<QuoteValue> quoteValues = ((MarketDataPublisher) publisher).getQuoteValues();
						if (quoteValues != null && !quoteValues.isEmpty()) {
							for (QuoteValue qv : quoteValues) {
								if (qv.getQuoteSet().equals(selectedQuoteSet.getValue())) {
									if (qv.getQuote().getName()
											.equals(Future.FUTURE + "." + name.getValue() + "." + symbol.getValue())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (FutureTradeDefinitionController.this.quoteValues.contains(qv)) {
													FutureTradeDefinitionController.this.quoteValues.remove(qv);
												}
												FutureTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FutureTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(futurePrice.getText(), "Future Price");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(quantity.getText(), "Quantity");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}