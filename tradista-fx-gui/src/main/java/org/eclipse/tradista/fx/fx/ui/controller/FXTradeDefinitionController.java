package org.eclipse.tradista.fx.fx.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.ui.controller.CashFlowProperty;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
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
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXPricerBusinessDelegate;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
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

public class FXTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private TextField amountOne;

	@FXML
	private TextField amountTwo;

	@FXML
	private ComboBox<Currency> currencyOne;

	@FXML
	private ComboBox<Currency> currencyTwo;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<Book> book;

	@FXML
	private Label exchangeRateWarning;

	@FXML
	private Label marketDataMessage;

	@FXML
	private TextField exchangeRate;

	// SHOULD BE EMBEDDED IN A SEPARATE CUSTOM COMPONENT -> WAIT FOR SCENE
	// BUILDER 3.0 TO CORRECTLY HANDLE CUSTOM COMPONENTS
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

	@FXML
	private Label pricerQuoteSetLabel;
	// END

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

	@FXML
	private Label tradeType;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private FXTradeBusinessDelegate fxTradeBusinessDelegate;

	private FXPricerBusinessDelegate fxPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FXTrade trade;

	@FXML
	private Label cfPricingDate;

	@FXML
	private Label cfPrimaryLegDiscountCurve;

	@FXML
	private Label cfQuoteLegDiscountCurve;

	@FXML
	private Button generate;

	@FXML
	private TradistaBookPieChart bookChartPane;

	private ChangeListener<String> amountTwoListener;

	private ChangeListener<String> exchangeRateListener;

	private ChangeListener<Currency> currencyOneListener;

	private ChangeListener<Currency> currencyTwoListener;

	private ChangeListener<LocalDate> tradeDateListener;

	private ChangeListener<Trade.Direction> directionListener;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
		fxPricerBusinessDelegate = new FXPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(2));
		tradeType.setText("FX Trade");
		final Calendar calendar = fxTradeBusinessDelegate.getFXExchange().getCalendar();
		LocalDate now = LocalDate.now();
		ExecutorService exec = Executors.newSingleThreadExecutor();

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<QuoteSet>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> observableValue, QuoteSet oldValue,
					QuoteSet newValue) {
				if (newValue != null && currencyOne.getValue() != null && currencyTwo.getValue() != null
						&& selectedQuoteDate.getValue() != null) {
					String currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					String currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), currencyOneCurrencyTwoExchangeRate,
							currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

		try {
			if (calendar.isBusinessDay(now)) {
				tradeDate.setValue(now);
				selectedQuoteDate.setValue(now);
			} else {
				LocalDate nextBusinessDay = DateUtil.nextBusinessDay(now, calendar);
				tradeDate.setValue(nextBusinessDay);
				selectedQuoteDate.setValue(nextBusinessDay);
			}
		} catch (TradistaBusinessException tbe) {
			// Won't happen because 'now' cannot be null
		}

		// Quotes initialization
		quoteName.setCellValueFactory(cellName -> cellName.getValue().getName());
		quoteDate.setCellValueFactory(cellDate -> cellDate.getValue().getDate());
		quoteType.setCellValueFactory(cellType -> cellType.getValue().getType());

		quoteBid.setCellValueFactory(cellBid -> cellBid.getValue().getBid());
		quoteAsk.setCellValueFactory(cellAsk -> cellAsk.getValue().getAsk());
		quoteOpen.setCellValueFactory(cellOpen -> cellOpen.getValue().getOpen());
		quoteClose.setCellValueFactory(cellClose -> cellClose.getValue().getClose());
		quoteHigh.setCellValueFactory(cellHigh -> cellHigh.getValue().getHigh());
		quoteLow.setCellValueFactory(cellLow -> cellLow.getValue().getLow());
		quoteLast.setCellValueFactory(cellLast -> cellLast.getValue().getLast());
		quoteEnteredDate.setCellValueFactory(cellDate -> cellDate.getValue().getEnteredDate());
		quoteSourceName.setCellValueFactory(cellSourceName -> cellSourceName.getValue().getSourceName());

		// CashFlows table
		cfDate.setCellValueFactory(cellDate -> cellDate.getValue().getDate());
		cfAmount.setCellValueFactory(cellAmount -> cellAmount.getValue().getAmount());
		cfCurrency.setCellValueFactory(cellCurrency -> cellCurrency.getValue().getCurrency());
		cfPurpose.setCellValueFactory(cellPurpose -> cellPurpose.getValue().getPurpose());
		cfDirection.setCellValueFactory(cellDirection -> cellDirection.getValue().getDirection());
		cfDiscountedAmount
				.setCellValueFactory(cellDiscountAmount -> cellDiscountAmount.getValue().getDiscountedAmount());
		cfDiscountFactor.setCellValueFactory(cellDiscountFactor -> cellDiscountFactor.getValue().getDiscountFactor());

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				if (newValue != null && currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					String currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					String currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), newValue, currencyOneCurrencyTwoExchangeRate,
							currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		currencyOne.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency oldCurrency, Currency newCurrency) {

				if (newCurrency != null) {

					if (selectedQuoteDate.getValue() != null && currencyTwo.getValue() != null) {
						String currencyOneCurrencyTwoExchangeRate = "FX." + newCurrency.getIsoCode() + "."
								+ currencyTwo.getValue().getIsoCode() + "%";
						String currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
								+ newCurrency.getIsoCode() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
								currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
					}

					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(newCurrency);
						if (discountCurve != null) {
							cfPrimaryLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfPrimaryLegDiscountCurve);
						} else {
							cfPrimaryLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newCurrency));
							TradistaGUIUtil.applyWarningStyle(cfPrimaryLegDiscountCurve);
						}
					}
				}
			}
		});

		currencyTwo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency oldCurrency, Currency newCurrency) {
				if (newCurrency != null) {
					if (selectedQuoteDate.getValue() != null && currencyOne.getValue() != null) {
						String currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
								+ newCurrency.getIsoCode() + "%";
						String currencyTwoCurrencyOneExchangeRate = "FX." + newCurrency.getIsoCode() + "."
								+ currencyOne.getValue().getIsoCode() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
								currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
					}
					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(newCurrency);
						if (discountCurve != null) {
							cfQuoteLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfQuoteLegDiscountCurve);
						} else {
							cfQuoteLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newCurrency));
							TradistaGUIUtil.applyWarningStyle(cfQuoteLegDiscountCurve);
						}
					}
				}
			}
		});

		exchangeRateListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!StringUtils.isEmpty(amountTwo.getText()) && !StringUtils.isEmpty(newValue)) {
					try {
						BigDecimal amtTwo = TradistaGUIUtil.parseAmount(amountTwo.getText(), null);
						BigDecimal exchRate = TradistaGUIUtil.parseAmount(newValue, null);
						if (exchRate.compareTo(BigDecimal.ZERO) != 0) {
							amountOne.setText(TradistaGUIUtil
									.formatAmount(amtTwo.divide(exchRate, configurationBusinessDelegate.getScale(),
											configurationBusinessDelegate.getRoundingMode())));
						}
					} catch (TradistaBusinessException tbe) {
					}
				}
			}
		};

		amountTwoListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!StringUtils.isEmpty(newValue) && !StringUtils.isEmpty(exchangeRate.getText())) {
					try {
						BigDecimal amtTwo = TradistaGUIUtil.parseAmount(newValue, null);
						BigDecimal exchRate = TradistaGUIUtil.parseAmount(exchangeRate.getText(), null);
						amountOne.setText(TradistaGUIUtil
								.formatAmount(amtTwo.divide(exchRate, configurationBusinessDelegate.getScale(),
										configurationBusinessDelegate.getRoundingMode())));
					} catch (TradistaBusinessException tbe) {
					}
				}
			}
		};

		currencyTwoListener = new ChangeListener<Currency>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency oldCurrency, Currency newCurrency) {
				if (newCurrency != null) {
					if (currencyOne.getValue() != null && !newCurrency.equals(currencyOne.getValue())
							&& selectedQuoteSet.getValue() != null) {
						if (tradeDate.getValue() != null && !tradeDate.getValue().isAfter(LocalDate.now())) {
							Task<Void> task = new Task<Void>() {
								@Override
								public Void call() {
									try {
										BigDecimal exchRate = getExchangeRate(tradeDate.getValue(), buySell.getValue(),
												currencyOne.getValue(), newCurrency,
												selectedQuoteSet.getValue().getId());
										isQuoteSetServiceError = false;
										if (exchRate == null) {
											Platform.runLater(() -> {
												TradistaGUIUtil.unapplyErrorStyle(exchangeRateWarning);
												TradistaGUIUtil.applyWarningStyle(exchangeRateWarning);
												exchangeRateWarning.setVisible(true);
												exchangeRateWarning.setText(String.format(
														"Exchange rate between %s and %s is not available as of Trade Date (%tD) in QuoteSet %s.",
														currencyOne.getValue(), newCurrency, tradeDate.getValue(),
														selectedQuoteSet.getValue()));
											});
										} else {
											Platform.runLater(() -> {
												exchangeRate.setText(TradistaGUIUtil.formatAmount(exchRate));
												exchangeRateWarning.setVisible(false);
											});
										}
									} catch (TradistaTechnicalException tte) {
										isQuoteSetServiceError = true;
										Platform.runLater(() -> {
											TradistaGUIUtil.applyErrorStyle(exchangeRateWarning);
											TradistaGUIUtil.unapplyWarningStyle(exchangeRateWarning);
											exchangeRateWarning.setText(String.format(
													"Cannot get exchange rate between %s and %s as of Trade Date (%tD) in QuoteSet %s, please contact support.",
													currencyOne.getValue(), newCurrency, tradeDate.getValue(),
													selectedQuoteSet.getValue()));
											exchangeRateWarning.setVisible(true);
										});
									}
									return null;
								}
							};
							exec.submit(task);
						}
					}
					if (currencyOne.getValue() != null && newCurrency.equals(currencyOne.getValue())) {
						exchangeRateWarning.setText(StringUtils.EMPTY);
						exchangeRate.setText(StringUtils.EMPTY);
					}
				}
			}
		};

		currencyOneListener = new ChangeListener<Currency>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency oldCurrency, Currency newCurrency) {
				if (newCurrency != null) {
					if (currencyTwo.getValue() != null && !newCurrency.equals(currencyTwo.getValue())
							&& selectedQuoteSet.getValue() != null) {
						if (tradeDate.getValue() != null && !tradeDate.getValue().isAfter(LocalDate.now())) {
							Task<Void> task = new Task<Void>() {
								@Override
								public Void call() {
									try {
										BigDecimal exchRate = getExchangeRate(tradeDate.getValue(), buySell.getValue(),
												newCurrency, currencyTwo.getValue(),
												selectedQuoteSet.getValue().getId());
										isQuoteSetServiceError = false;
										if (exchRate == null) {
											Platform.runLater(() -> {
												TradistaGUIUtil.unapplyErrorStyle(exchangeRateWarning);
												TradistaGUIUtil.applyWarningStyle(exchangeRateWarning);
												exchangeRateWarning.setVisible(true);
												exchangeRateWarning.setText(String.format(
														"Exchange rate between %s and %s is not available as of Trade Date (%tD) in QuoteSet %s.",
														newCurrency, currencyTwo.getValue(), tradeDate.getValue(),
														selectedQuoteSet.getValue()));
											});
										} else {
											Platform.runLater(() -> {
												exchangeRate.setText(TradistaGUIUtil.formatAmount(exchRate));
												exchangeRateWarning.setVisible(false);
											});
										}
									} catch (TradistaTechnicalException tte) {
										isQuoteSetServiceError = true;
										Platform.runLater(() -> {
											TradistaGUIUtil.applyErrorStyle(exchangeRateWarning);
											TradistaGUIUtil.unapplyWarningStyle(exchangeRateWarning);
											exchangeRateWarning.setText(String.format(
													"Cannot get exchange rate between %s and %s as of Trade Date (%tD) in QuoteSet %s, please contact support.",
													newCurrency, currencyTwo.getValue(), tradeDate.getValue(),
													selectedQuoteSet.getValue()));
											exchangeRateWarning.setVisible(true);
										});
									}
									return null;
								}
							};
							exec.submit(task);
						}
					}

					if (currencyTwo.getValue() != null && newCurrency.equals(currencyTwo.getValue())) {
						exchangeRateWarning.setText(StringUtils.EMPTY);
						exchangeRate.setText(StringUtils.EMPTY);
					}
				}
			}
		};

		tradeDateListener = new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null && currencyTwo.getValue() != null && currencyOne.getValue() != null
						&& !currencyOne.getValue().equals(currencyTwo.getValue())
						&& selectedQuoteSet.getValue() != null) {
					if (!newDate.isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal exchRate = getExchangeRate(newDate, buySell.getValue(),
											currencyOne.getValue(), currencyTwo.getValue(),
											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (exchRate == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(exchangeRateWarning);
											TradistaGUIUtil.applyWarningStyle(exchangeRateWarning);
											exchangeRateWarning.setVisible(true);
											exchangeRateWarning.setText(String.format(
													"Exchange rate between %s and %s is not available as of Trade Date (%tD) in QuoteSet %s.",
													currencyOne.getValue(), currencyTwo.getValue(), newDate,
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											exchangeRate.setText(TradistaGUIUtil.formatAmount(exchRate));
											exchangeRateWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(exchangeRateWarning);
										TradistaGUIUtil.unapplyWarningStyle(exchangeRateWarning);
										exchangeRateWarning.setText(String.format(
												"Cannot get exchange rate between %s and %s as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												currencyOne.getValue(), currencyTwo.getValue(), newDate,
												selectedQuoteSet.getValue()));
										exchangeRateWarning.setVisible(true);
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
				if (tradeDate.getValue() != null && currencyTwo.getValue() != null && currencyOne.getValue() != null
						&& !currencyOne.getValue().equals(currencyTwo.getValue())
						&& selectedQuoteSet.getValue() != null) {
					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal exchRate = getExchangeRate(tradeDate.getValue(), newDirection,
											currencyOne.getValue(), currencyTwo.getValue(),
											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (exchRate == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(exchangeRateWarning);
											TradistaGUIUtil.applyWarningStyle(exchangeRateWarning);
											exchangeRateWarning.setVisible(true);
											exchangeRateWarning.setText(String.format(
													"Exchange rate between %s and %s is not available as of Trade Date (%tD) in QuoteSet %s.",
													currencyOne.getValue(), currencyTwo.getValue(),
													tradeDate.getValue(), selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											exchangeRate.setText(TradistaGUIUtil.formatAmount(exchRate));
											exchangeRateWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(exchangeRateWarning);
										TradistaGUIUtil.unapplyWarningStyle(exchangeRateWarning);
										exchangeRateWarning.setText(String.format(
												"Cannot get exchange rate between %s and %s as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												currencyOne.getValue(), currencyTwo.getValue(), tradeDate.getValue(),
												selectedQuoteSet.getValue()));
										exchangeRateWarning.setVisible(true);
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

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricerMeasure>() {
			@Override
			public void changed(ObservableValue<? extends PricerMeasure> arg0, PricerMeasure arg1,
					PricerMeasure newPricerMeasure) {
				// newPricerMeasure is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricerMeasure != null) {
					TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingMethods(newPricerMeasure),
							pricingMethod);
				}
			}
		});

		// SHOULD BE EMBEDDED IN A SEPARATE CUSTOM COMPONENT -> WAIT FOR SCENE
		// BUILDER 3.0 TO CORRECTLY HANDLE CUSTOM COMPONENTS

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricingParameter>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> arg0, PricingParameter arg1,
					PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(FXTrade.FX, newPricingParam);
					} catch (TradistaBusinessException abe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());

					if (currencyOne.getValue() != null) {
						InterestRateCurve discountCurve = newPricingParam.getDiscountCurve(currencyOne.getValue());
						if (discountCurve != null) {
							cfPrimaryLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfPrimaryLegDiscountCurve);
						} else {
							cfPrimaryLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), currencyOne.getValue()));
							TradistaGUIUtil.applyWarningStyle(cfPrimaryLegDiscountCurve);
						}
					}

					if (currencyTwo.getValue() != null) {
						InterestRateCurve discountCurve = newPricingParam.getDiscountCurve(currencyTwo.getValue());
						if (discountCurve != null) {
							cfQuoteLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfQuoteLegDiscountCurve);
						} else {
							cfQuoteLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), currencyTwo.getValue()));
							TradistaGUIUtil.applyWarningStyle(cfQuoteLegDiscountCurve);
						}
					}

				}
			}
		});

		pricingDate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				cfPricingDate.setText(pricingDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
			}
		});

		pricingDate.setValue(LocalDate.now());
		// END
		cfPricingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		tradeDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (settlementDate.getValue() != null) {
					FXTrade fxTrade = new FXTrade();
					fxTrade.setCurrency(currencyTwo.getValue());
					fxTrade.setCurrencyOne(currencyOne.getValue());
					fxTrade.setTradeDate(newDate);
					fxTrade.setSettlementDate(settlementDate.getValue());
					try {
						if (fxTradeBusinessDelegate.determinateType(fxTrade).equals(FXTrade.Type.FX_FORWARD)) {
							tradeType.setText("FX Forward Trade");
						} else {
							tradeType.setText("FX Spot Trade");
						}
					} catch (TradistaBusinessException tbe) {
						// if there is a null value leading to this abe, we don
						// nothing.
					}
				}
			}
		});

		settlementDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null && tradeDate.getValue() != null) {
					FXTrade fxTrade = new FXTrade();
					fxTrade.setCurrency(currencyTwo.getValue());
					fxTrade.setCurrencyOne(currencyOne.getValue());
					fxTrade.setTradeDate(tradeDate.getValue());
					fxTrade.setSettlementDate(newDate);
					try {
						if (fxTradeBusinessDelegate.determinateType(fxTrade).equals(FXTrade.Type.FX_FORWARD)) {
							tradeType.setText("FX Forward Trade");
						} else {
							tradeType.setText("FX Spot Trade");
						}
					} catch (TradistaBusinessException tbe) {
						// if there is a null value leading to this exception, we do
						// nothing.
					}
				}
			}
		});

		final Callback<DatePicker, DateCell> settlementDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXTrade fxTrade;

					private boolean isAvailable(LocalDate date) {
						if (fxTrade == null) {
							fxTrade = new FXTrade();
							fxTrade.setCurrencyOne(currencyOne.getValue());
							fxTrade.setCurrency(currencyTwo.getValue());
						}
						try {
							return fxTradeBusinessDelegate.isBusinessDay(fxTrade, date);
						} catch (TradistaBusinessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return false;
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

		final Callback<DatePicker, DateCell> tradingDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXTrade fxTrade;

					private boolean isAvailable(LocalDate date) {
						boolean isAvailable = true;
						if (fxTrade == null) {
							fxTrade = new FXTrade();
							fxTrade.setCurrencyOne(currencyOne.getValue());
							fxTrade.setCurrency(currencyTwo.getValue());
						}
						try {
							isAvailable = fxTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(date);
						} catch (TradistaBusinessException tbe) {
							// Won't happen because 'date' cannot be null
						}

						return isAvailable;
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
		settlementDate.setDayCellFactory(settlementDayCellFactory);
		tradeDate.setDayCellFactory(tradingDayCellFactory);
		selectedQuoteDate.setDayCellFactory(settlementDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);
	}

	private void addListeners() {
		exchangeRate.textProperty().addListener(exchangeRateListener);
		amountTwo.textProperty().addListener(amountTwoListener);
		currencyOne.valueProperty().addListener(currencyOneListener);
		currencyTwo.valueProperty().addListener(currencyTwoListener);
		tradeDate.valueProperty().addListener(tradeDateListener);
		buySell.valueProperty().addListener(directionListener);
	}

	private void removeListeners() {
		exchangeRate.textProperty().removeListener(exchangeRateListener);
		amountTwo.textProperty().removeListener(amountTwoListener);
		currencyOne.valueProperty().removeListener(currencyOneListener);
		currencyTwo.valueProperty().removeListener(currencyTwoListener);
		tradeDate.valueProperty().removeListener(tradeDateListener);
		buySell.valueProperty().removeListener(directionListener);
	}

	private BigDecimal getExchangeRate(LocalDate tradeDate, Trade.Direction direction, Currency currencyOne,
			Currency currencyTwo, long quoteSetId) {
		QuoteValue qv = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId,
				"FX." + currencyOne + "." + currencyTwo, QuoteType.EXCHANGE_RATE, tradeDate);
		if (qv == null) {
			return null;
		}
		if (qv.getClose() != null) {
			return qv.getClose();
		}
		if (direction.equals(Trade.Direction.BUY)) {
			if (qv.getAsk() != null) {
				return qv.getAsk();
			}
		} else {
			if (qv.getBid() != null) {
				return qv.getBid();
			}
		}
		return qv.getLast();
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

				trade.setId(fxTradeBusinessDelegate.saveFXTrade(trade));
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
				trade.setId(fxTradeBusinessDelegate.saveFXTrade(trade));
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
		FXTrade fxTrade;
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

			fxTrade = fxTradeBusinessDelegate.getFXTradeById(tradeId);
			if (fxTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", FXTrade.FX, load.getText()));
			}
			load(fxTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FXTrade trade) {
		removeListeners();
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		amountOne.setText(TradistaGUIUtil.formatAmount(trade.getAmountOne()));
		amountTwo.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		currencyOne.setValue(trade.getCurrencyOne());
		currencyTwo.setValue(trade.getCurrency());
		settlementDate.setValue(trade.getSettlementDate());
		if (trade.getSettlementDate().isAfter(trade.getTradeDate().plus(2, ChronoUnit.DAYS))) {
			tradeType.setText("FX Forward Trade");
		} else {
			tradeType.setText("FX Spot Trade");
		}
		try {
			exchangeRate.setText(TradistaGUIUtil.formatAmount(fxTradeBusinessDelegate.getExchangeRate(trade)));
		} catch (TradistaBusinessException tbe) {
		}
		addListeners();
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = fxPricerBusinessDelegate.generateCashFlows(trade, pricingParameter.getValue(),
					pricingDate.getValue());
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

	@Override
	@FXML
	public void clear() {
		this.trade = null;
		settlementDate.setValue(null);
		tradeId.setText(StringUtils.EMPTY);
		amountOne.clear();
		amountTwo.clear();
		exchangeRate.clear();
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FXTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			if (!amountOne.getText().isEmpty()) {
				trade.setAmountOne(TradistaGUIUtil.parseAmount(amountOne.getText(), "Amount One"));
			}
			if (!amountTwo.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(amountTwo.getText(), "Amount Two"));
			}
			trade.setCurrencyOne(this.currencyOne.getSelectionModel().getSelectedItem());
			trade.setCurrency(this.currencyTwo.getSelectionModel().getSelectedItem());
		} catch (TradistaBusinessException tbe) {
			// Should not happen at this stage.
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
	public void refresh() {
		super.refresh();
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);
		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
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
											.equals("FX." + currencyOne.getValue() + "." + currencyTwo.getValue())
											|| qv.getQuote().getName().equals(
													"FX." + currencyTwo.getValue() + "." + currencyOne.getValue())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (FXTradeDefinitionController.this.quoteValues.contains(qv)) {
													FXTradeDefinitionController.this.quoteValues.remove(qv);
												}
												FXTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FXTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(amountOne.getText(), "Amount One");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountTwo.getText(), "Amount Two");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}