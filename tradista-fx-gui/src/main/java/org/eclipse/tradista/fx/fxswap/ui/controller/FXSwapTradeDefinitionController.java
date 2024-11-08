package org.eclipse.tradista.fx.fxswap.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.ui.controller.CashFlowProperty;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.DateUtil;
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
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.service.FXSwapPricerBusinessDelegate;
import org.eclipse.tradista.fx.fxswap.service.FXSwapTradeBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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

public class FXSwapTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDateSpot;

	@FXML
	private DatePicker settlementDateForward;

	@FXML
	private TextField amountOneSpot;

	@FXML
	private TextField amountTwoSpot;

	@FXML
	private TextField amountOneForward;

	@FXML
	private TextField amountTwoForward;

	@FXML
	private ComboBox<Currency> currencyOne;

	@FXML
	private ComboBox<Currency> currencyTwo;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<Book> book;

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

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private FXSwapTradeBusinessDelegate fxSwapTradeBusinessDelegate;

	private FXSwapPricerBusinessDelegate fxSwapPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FXSwapTrade trade;

	@FXML
	private Label tradeType;

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

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		fxSwapTradeBusinessDelegate = new FXSwapTradeBusinessDelegate();
		fxSwapPricerBusinessDelegate = new FXSwapPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(2));
		tradeType.setText("FX Swap Trade");

		final Calendar calendar = fxSwapTradeBusinessDelegate.getFXExchange().getCalendar();
		LocalDate now = LocalDate.now();

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
			public void changed(ObservableValue<? extends Currency> observableValue, Currency oldValue,
					Currency newValue) {
				if (newValue != null) {
					if (selectedQuoteDate.getValue() != null && currencyTwo.getValue() != null) {
						String currencyOneCurrencyTwoExchangeRate = "FX." + newValue.getIsoCode() + "."
								+ currencyTwo.getValue().getIsoCode() + "%";
						String currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
								+ newValue.getIsoCode() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
								currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
					}
					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(newValue);
						if (discountCurve != null) {
							cfPrimaryLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfPrimaryLegDiscountCurve);
						} else {
							cfPrimaryLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newValue));
							TradistaGUIUtil.applyWarningStyle(cfPrimaryLegDiscountCurve);
						}
					}
				}
			}
		});

		currencyTwo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {
			@Override
			public void changed(ObservableValue<? extends Currency> observableValue, Currency oldValue,
					Currency newValue) {
				if (newValue != null) {
					if (selectedQuoteDate.getValue() != null && currencyOne.getValue() != null) {
						String currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
								+ newValue.getIsoCode() + "%";
						String currencyTwoCurrencyOneExchangeRate = "FX." + newValue.getIsoCode() + "."
								+ currencyOne.getValue().getIsoCode() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
								currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
					}
					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(newValue);
						if (discountCurve != null) {
							cfQuoteLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfQuoteLegDiscountCurve);
						} else {
							cfQuoteLegDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newValue));
							TradistaGUIUtil.applyWarningStyle(cfQuoteLegDiscountCurve);
						}
					}
				}
			}
		});

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

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricingParameter>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> arg0, PricingParameter arg1,
					PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(FXSwapTrade.FX_SWAP, newPricingParam);
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

		cfPricingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

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

		final Callback<DatePicker, DateCell> tradingDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXSwapTrade fxSwapTrade;

					private boolean isAvailable(LocalDate date) {
						boolean isAvailable = true;
						if (fxSwapTrade == null) {
							fxSwapTrade = new FXSwapTrade();
							fxSwapTrade.setCurrency(currencyTwo.getValue());
							fxSwapTrade.setCurrencyOne(currencyOne.getValue());
						}
						try {
							isAvailable = fxSwapTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(date);
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

		final Callback<DatePicker, DateCell> settlementDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXSwapTrade fxSwapTrade;

					private boolean isAvailable(LocalDate date) {
						if (fxSwapTrade == null) {
							fxSwapTrade = new FXSwapTrade();
							fxSwapTrade.setCurrency(currencyTwo.getValue());
							fxSwapTrade.setCurrencyOne(currencyOne.getValue());
						}
						try {
							return fxSwapTradeBusinessDelegate.isBusinessDay(fxSwapTrade, date);
						} catch (TradistaBusinessException tbe) {
							// TODO Auto-generated catch block
							tbe.printStackTrace();
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
		settlementDateForward.setDayCellFactory(settlementDayCellFactory);
		settlementDateSpot.setDayCellFactory(settlementDayCellFactory);
		tradeDate.setDayCellFactory(tradingDayCellFactory);
		selectedQuoteDate.setDayCellFactory(settlementDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);

	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(amountOneSpot.getText(), "Spot Amount One");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountTwoSpot.getText(), "Spot Amount Two");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountOneForward.getText(), "Forward Amount One");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountTwoForward.getText(), "Forward Amount Two");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
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

				trade.setId(fxSwapTradeBusinessDelegate.saveFXSwapTrade(trade));
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
				trade.setId(fxSwapTradeBusinessDelegate.saveFXSwapTrade(trade));
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
		FXSwapTrade fxSwapTrade;
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

			fxSwapTrade = fxSwapTradeBusinessDelegate.getFXSwapTradeById(tradeId);
			if (fxSwapTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", FXSwapTrade.FX_SWAP, load.getText()));
			}
			load(fxSwapTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FXSwapTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		currencyOne.setValue(trade.getCurrency());
		amountOneForward.setText(TradistaGUIUtil.formatAmount(trade.getAmountOneForward()));
		amountOneSpot.setText(TradistaGUIUtil.formatAmount(trade.getAmountOneSpot()));
		amountTwoForward.setText(TradistaGUIUtil.formatAmount(trade.getAmountTwoForward()));
		amountTwoSpot.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		currencyOne.setValue(trade.getCurrencyOne());
		currencyTwo.setValue(trade.getCurrency());
		settlementDateForward.setValue(trade.getSettlementDateForward());
		settlementDateSpot.setValue(trade.getSettlementDate());
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = fxSwapPricerBusinessDelegate.generateCashFlows(trade,
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

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		amountOneForward.clear();
		amountOneSpot.clear();
		amountTwoForward.clear();
		amountTwoSpot.clear();
		settlementDateForward.setValue(null);
		settlementDateSpot.setValue(null);
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FXSwapTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setBuySell(buySell.getValue().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			trade.setCurrency(currencyTwo.getValue());
			trade.setCurrencyOne(currencyOne.getValue());
			if (!amountOneSpot.getText().isEmpty()) {
				trade.setAmountOneSpot(TradistaGUIUtil.parseAmount(amountOneSpot.getText(), "Spot Amount One"));
			}
			if (!amountOneForward.getText().isEmpty()) {
				trade.setAmountOneForward(
						TradistaGUIUtil.parseAmount(amountOneForward.getText(), "Forward Amount One"));
			}
			if (!amountTwoSpot.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(amountTwoSpot.getText(), "Spot Amount Two"));
			}
			if (!amountOneForward.getText().isEmpty()) {
				trade.setAmountTwoForward(
						TradistaGUIUtil.parseAmount(amountTwoForward.getText(), "Forward Amount Two"));
			}
			trade.setSettlementDate(settlementDateSpot.getValue());
			trade.setSettlementDateForward(settlementDateForward.getValue());
		} catch (TradistaBusinessException tbe) {
			// Should not happen here.
		}
	}

	@FXML
	protected void price() {
		try {
			checkAmounts();

			buildTrade();
			result.setText(
					pricerBusinessDelegate
							.calculate(trade, pricingParameter.getValue(), pricingCurrency.getValue(),
									pricingDate.getValue(), pricingMeasure.getValue(), pricingMethod.getValue())
							.toString());

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
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
												if (FXSwapTradeDefinitionController.this.quoteValues.contains(qv)) {
													FXSwapTradeDefinitionController.this.quoteValues.remove(qv);
												}
												FXSwapTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FXSwapTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

}