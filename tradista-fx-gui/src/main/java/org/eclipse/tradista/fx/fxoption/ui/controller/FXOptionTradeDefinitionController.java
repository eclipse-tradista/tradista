package org.eclipse.tradista.fx.fxoption.ui.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
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
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.service.FXOptionTradeBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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

public class FXOptionTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private ComboBox<OptionTrade.Type> callPut;

	@FXML
	private ComboBox<OptionTrade.SettlementType> settlementType;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private DatePicker exerciseDate;

	@FXML
	private DatePicker maturityDate;

	@FXML
	private TextField premium;

	@FXML
	private TextField settlementDateOffset;

	@FXML
	private TextField strike;

	@FXML
	private TextField amountOne;

	@FXML
	private TextField amountTwo;

	@FXML
	private ComboBox<VanillaOptionTrade.Style> style;

	@FXML
	private ComboBox<Currency> currencyOne;

	@FXML
	private ComboBox<Currency> currencyTwo;

	@FXML
	private ComboBox<Currency> premiumCurrency;

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
	private Label tradeType;

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

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private FXOptionTradeBusinessDelegate fxOptionTradeBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FXOptionTrade trade;

	@FXML
	private TradistaBookPieChart bookChartPane;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		fxOptionTradeBusinessDelegate = new FXOptionTradeBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		quoteValues = Collections.synchronizedSet(HashSet.newHashSet(2));
		tradeType.setText("FX Option Trade");

		final Calendar calendar = fxOptionTradeBusinessDelegate.getFXExchange().getCalendar();
		LocalDate now = LocalDate.now();

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

		TradistaGUIUtil.fillOptionStyleComboBox(style);

		TradistaGUIUtil.fillOptionTypeComboBox(callPut);

		TradistaGUIUtil.fillOptionSettlementTypeComboBox(settlementType);

		// Quotes initialization
		quoteName.setCellValueFactory(cellData -> cellData.getValue().getName());
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
		quoteSourceName.setCellValueFactory(cellName -> cellName.getValue().getSourceName());

		try {
			if (calendar.isBusinessDay(now)) {
				tradeDate.setValue(now);
				selectedQuoteDate.setValue(now);
			} else {
				LocalDate nextBusinessDay = DateUtil.nextBusinessDay(now, calendar);
				tradeDate.setValue(nextBusinessDay);
				selectedQuoteDate.setValue(nextBusinessDay);
			}
		} catch (TradistaBusinessException _) {
			// Won't happen because 'now' cannot be null
		}

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> arg0, QuoteSet oldValue, QuoteSet newValue) {
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

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<>() {
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

		currencyOne.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> observableValue, Currency oldValue,
					Currency newValue) {
				if (selectedQuoteDate.getValue() != null && newValue != null && currencyTwo.getValue() != null) {
					String currencyOneCurrencyTwoExchangeRate = "FX." + newValue.getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					String currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ newValue.getIsoCode() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
							currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		currencyTwo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> observableValue, Currency oldValue,
					Currency newValue) {
				if (selectedQuoteDate.getValue() != null && currencyOne.getValue() != null && newValue != null) {
					String currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ newValue.getIsoCode() + "%";
					String currencyTwoCurrencyOneExchangeRate = "FX." + newValue.getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
							currencyOneCurrencyTwoExchangeRate, currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		pricingDate.setValue(LocalDate.now());

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
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

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> arg0, PricingParameter arg1,
					PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(FXOptionTrade.FX_OPTION, newPricingParam);
					} catch (TradistaBusinessException tbe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());
				}
			}
		});

		final Callback<DatePicker, DateCell> tradingDayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXOptionTrade fxOptionTrade;

					private boolean isAvailable(LocalDate date) {
						boolean isAvailable = true;

						if (fxOptionTrade == null) {
							fxOptionTrade = new FXOptionTrade();
							FXTrade fxTrade = new FXTrade();
							fxTrade.setCurrency(currencyTwo.getValue());
							fxTrade.setCurrencyOne(currencyOne.getValue());
							fxOptionTrade.setUnderlying(fxTrade);
						}

						try {
							isAvailable = fxOptionTradeBusinessDelegate.getFXExchange().getCalendar()
									.isBusinessDay(date);
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

		final Callback<DatePicker, DateCell> settlementDayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXOptionTrade fxOptionTrade;

					private boolean isAvailable(LocalDate date) {
						if (fxOptionTrade == null) {
							fxOptionTrade = new FXOptionTrade();
							FXTrade fxTrade = new FXTrade();
							fxTrade.setCurrency(currencyTwo.getValue());
							fxTrade.setCurrencyOne(currencyOne.getValue());
							fxOptionTrade.setUnderlying(fxTrade);
						}
						try {
							return fxOptionTradeBusinessDelegate.isBusinessDay(fxOptionTrade, date);
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

		maturityDate.setDayCellFactory(tradingDayCellFactory);
		exerciseDate.setDayCellFactory(tradingDayCellFactory);
		tradeDate.setDayCellFactory(tradingDayCellFactory);
		settlementDate.setDayCellFactory(tradingDayCellFactory);
		selectedQuoteDate.setDayCellFactory(settlementDayCellFactory);

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency, premiumCurrency);
		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);

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
				trade.setId(fxOptionTradeBusinessDelegate.saveFXOptionTrade(trade));
				FXOptionTrade existingTrade = fxOptionTradeBusinessDelegate.getFXOptionTradeById(trade.getId());
				if (existingTrade.getUnderlying() != null) {
					trade.getUnderlying().setId(existingTrade.getUnderlying().getId());
				}
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
		long oldUnderlyingTradeId = 0;
		LocalDate oldCreationDate = null;
		LocalDate oldUnderlyingCreationDate = null;
		FXTrade underlying = null;
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				buildTrade();
				oldTradeId = trade.getId();
				oldUnderlyingTradeId = trade.getUnderlying().getId();
				oldCreationDate = trade.getCreationDate();
				oldUnderlyingCreationDate = trade.getUnderlying().getCreationDate();
				trade.setId(0);
				trade.setCreationDate(LocalDate.now());
				underlying = trade.getUnderlying();
				underlying.setId(0);
				underlying.setCreationDate(LocalDate.now());
				trade.setUnderlying(underlying);
				trade.setId(fxOptionTradeBusinessDelegate.saveFXOptionTrade(trade));
				FXOptionTrade existingTrade = fxOptionTradeBusinessDelegate.getFXOptionTradeById(trade.getId());
				if (existingTrade.getUnderlying() != null) {
					trade.getUnderlying().setId(existingTrade.getUnderlying().getId());
				}
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				trade.setId(oldTradeId);
				trade.setCreationDate(oldCreationDate);
				underlying.setId(oldUnderlyingTradeId);
				underlying.setCreationDate(oldUnderlyingCreationDate);
				trade.setUnderlying(underlying);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		FXOptionTrade fxOptionTrade;
		long tradeId = 0;
		try {
			try {
				if (!load.getText().isEmpty()) {
					tradeId = Long.parseLong(load.getText());
				} else {
					throw new TradistaBusinessException("Please specify a trade id.");
				}
			} catch (NumberFormatException _) {
				throw new TradistaBusinessException(String.format("The trade id is incorrect: %s", load.getText()));
			}

			fxOptionTrade = fxOptionTradeBusinessDelegate.getFXOptionTradeById(tradeId);
			if (fxOptionTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", FXOptionTrade.FX_OPTION, load.getText()));
			}
			load(fxOptionTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FXOptionTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		settlementDate.setValue(trade.getSettlementDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		premium.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		premiumCurrency.setValue(trade.getCurrency());
		callPut.setValue(trade.getType());
		style.setValue(trade.getStyle());
		settlementType.setValue(trade.getSettlementType());
		settlementDateOffset.setText(Integer.toString(trade.getSettlementDateOffset()));
		strike.setText(TradistaGUIUtil.formatAmount(trade.getStrike()));
		maturityDate.setValue(trade.getMaturityDate());
		exerciseDate.setValue(trade.getExerciseDate());
		amountOne.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getAmountOne()));
		amountTwo.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getAmount()));
		currencyOne.setValue(trade.getUnderlying().getCurrencyOne());
		currencyTwo.setValue(trade.getUnderlying().getCurrency());
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		maturityDate.setValue(null);
		settlementDate.setValue(null);
		strike.clear();
		premium.clear();
		settlementDateOffset.clear();
		amountOne.clear();
		amountTwo.clear();
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FXOptionTrade();
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setExerciseDate(exerciseDate.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setBuySell(buySell.getValue().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			trade.setCurrency(premiumCurrency.getValue());
			if (!premium.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(premium.getText(), "Premium"));
			}
			trade.setType(callPut.getValue());
			trade.setStyle(style.getValue());
			trade.setMaturityDate(maturityDate.getValue());
			trade.setSettlementType(settlementType.getValue());
			if (!settlementDateOffset.getText().isEmpty()) {
				trade.setSettlementDateOffset(Integer.parseInt(settlementDateOffset.getText()));
			}
			if (!strike.getText().isEmpty()) {
				trade.setStrike(TradistaGUIUtil.parseAmount(strike.getText(), "Strike"));
			}

			// Building the underlying
			FXTrade underlying = trade.getUnderlying();
			if (trade.getUnderlying() == null) {
				underlying = new FXTrade();
			}
			if (!amountOne.getText().isEmpty()) {
				underlying.setAmountOne(TradistaGUIUtil.parseAmount(amountOne.getText(), "Underlying's Amount One"));
			}
			if (!amountTwo.getText().isEmpty()) {
				underlying.setAmount(TradistaGUIUtil.parseAmount(amountTwo.getText(), "Underlying's Amount Two"));
			}

			underlying.setCurrency(currencyTwo.getValue());
			underlying.setCurrencyOne(currencyOne.getValue());
			underlying.setBuySell((trade.isCall() && trade.isBuy()) || (trade.isPut() && trade.isSell()));
			underlying.setCounterparty(counterparty.getValue());
			underlying.setBook(book.getValue());

			if (trade.getExerciseDate() != null) {
				underlying.setTradeDate(trade.getExerciseDate());
				short offSet = 0;
				if (!settlementDateOffset.getText().isEmpty()) {
					offSet = Short.parseShort(settlementDateOffset.getText());
				}

				if (trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
					LocalDate settlementDate = trade.getExerciseDate().plusDays(offSet);
					while (!new FXTradeBusinessDelegate().isBusinessDay(underlying, settlementDate)) {
						settlementDate = settlementDate.plusDays(1);
					}
					underlying.setSettlementDate(settlementDate);
				} else {
					underlying.setSettlementDate(DateUtil.addBusinessDay(trade.getExerciseDate(),
							fxOptionTradeBusinessDelegate.getFXExchange().getCalendar(), offSet));
				}
			}
			trade.setUnderlying(underlying);
		} catch (TradistaBusinessException _) {
			// Should not appear here.
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
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency, premiumCurrency);
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
												if (FXOptionTradeDefinitionController.this.quoteValues.contains(qv)) {
													FXOptionTradeDefinitionController.this.quoteValues.remove(qv);
												}
												FXOptionTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FXOptionTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(premium.getText(), "Premium");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountOne.getText(), "Underlying's Amount One");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(amountTwo.getText(), "Underlying's Amount Two");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			if (!settlementDateOffset.getText().isEmpty()) {
				Integer.parseInt(settlementDateOffset.getText());
			}
		} catch (NumberFormatException _) {
			errMsg.append(
					String.format("The settlement date offset is incorrect: %s.%n", settlementDateOffset.getText()));
		}
		try {
			TradistaGUIUtil.checkAmount(strike.getText(), "Strike");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}