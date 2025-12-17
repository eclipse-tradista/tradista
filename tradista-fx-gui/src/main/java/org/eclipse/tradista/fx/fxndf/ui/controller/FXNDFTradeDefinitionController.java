package org.eclipse.tradista.fx.fxndf.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
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
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
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
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.eclipse.tradista.fx.fxndf.service.FXNDFPricerBusinessDelegate;
import org.eclipse.tradista.fx.fxndf.service.FXNDFTradeBusinessDelegate;
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

public class FXNDFTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private DatePicker fixingDate;

	@FXML
	private TextField notionalAmount;

	@FXML
	private TextField ndfRate;

	@FXML
	private ComboBox<Currency> settlementCurrency;

	@FXML
	private ComboBox<Currency> nonDeliverableCurrency;

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

	@FXML
	private Label cfPricingDate;

	@FXML
	private Label cfDiscountCurve;

	@FXML
	private Button generate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private FXNDFTradeBusinessDelegate fxNDFTradeBusinessDelegate;

	private FXNDFPricerBusinessDelegate fxNDFPricerBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FXNDFTrade trade;

	@FXML
	private Label tradeType;

	@FXML
	private TradistaBookPieChart bookChartPane;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		fxNDFTradeBusinessDelegate = new FXNDFTradeBusinessDelegate();
		fxNDFPricerBusinessDelegate = new FXNDFPricerBusinessDelegate();
		quoteValues = Collections.synchronizedSet(HashSet.newHashSet(2));
		tradeType.setText("FX NDF Trade");

		final Calendar calendar = fxNDFTradeBusinessDelegate.getFXExchange().getCalendar();
		LocalDate now = LocalDate.now();

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

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

		// CashFlows table
		cfDate.setCellValueFactory(cellDate -> cellDate.getValue().getDate());
		cfAmount.setCellValueFactory(cellAmount -> cellAmount.getValue().getAmount());
		cfCurrency.setCellValueFactory(cellCurrency -> cellCurrency.getValue().getCurrency());
		cfPurpose.setCellValueFactory(cellPurpose -> cellPurpose.getValue().getPurpose());
		cfDirection.setCellValueFactory(cellDirection -> cellDirection.getValue().getDirection());
		cfDiscountedAmount.setCellValueFactory(cellDiscount -> cellDiscount.getValue().getDiscountedAmount());
		cfDiscountFactor.setCellValueFactory(cellFactor -> cellFactor.getValue().getDiscountFactor());

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

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> arg0, QuoteSet oldValue, QuoteSet newValue) {
				if (newValue != null && settlementCurrency.getValue() != null
						&& nonDeliverableCurrency.getValue() != null && selectedQuoteDate.getValue() != null) {
					String settlementCurrencyNonDeliverableCurrencyExchangeRate = "FX."
							+ settlementCurrency.getValue().getIsoCode() + "."
							+ nonDeliverableCurrency.getValue().getIsoCode() + "%";
					String nonDeliverableCurrencySettlementCurrencyExchangeRate = "FX."
							+ nonDeliverableCurrency.getValue().getIsoCode() + "."
							+ settlementCurrency.getValue().getIsoCode() + "%";
					fillQuotesTable(newValue, selectedQuoteDate.getValue(),
							settlementCurrencyNonDeliverableCurrencyExchangeRate,
							nonDeliverableCurrencySettlementCurrencyExchangeRate);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				if (newValue != null && settlementCurrency.getValue() != null
						&& nonDeliverableCurrency.getValue() != null) {
					String settlementNonDeliverableExchangeRate = "FX." + settlementCurrency.getValue().getIsoCode()
							+ "." + nonDeliverableCurrency.getValue().getIsoCode() + "%";
					String nonDeliverableSettlementExchangeRate = "FX." + nonDeliverableCurrency.getValue().getIsoCode()
							+ "." + settlementCurrency.getValue().getIsoCode() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), newValue, settlementNonDeliverableExchangeRate,
							nonDeliverableSettlementExchangeRate);
				}
			}
		});

		settlementCurrency.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency oldValue, Currency newValue) {
				if (newValue != null) {
					if (selectedQuoteDate.getValue() != null && nonDeliverableCurrency.getValue() != null) {
						String settlementNonDeliverableExchangeRate = "FX." + newValue.getIsoCode() + "."
								+ nonDeliverableCurrency.getValue().getIsoCode() + "%";
						String nonDeliverableSettlementExchangeRate = "FX."
								+ nonDeliverableCurrency.getValue().getIsoCode() + "." + newValue.getIsoCode() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
								settlementNonDeliverableExchangeRate, nonDeliverableSettlementExchangeRate);
					}
					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(newValue);
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newValue));
							TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
						}
					}
				}
			}
		});

		nonDeliverableCurrency.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency arg1, Currency arg2) {
				if (selectedQuoteDate.getValue() != null && settlementCurrency.getValue() != null && arg2 != null) {
					String settlementNonDeliverableExchangeRate = "FX." + settlementCurrency.getValue().getIsoCode()
							+ "." + arg2.getIsoCode() + "%";
					String nonDeliverableSettlementExchangeRate = "FX." + arg2.getIsoCode() + "."
							+ settlementCurrency.getValue().getIsoCode() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
							settlementNonDeliverableExchangeRate, nonDeliverableSettlementExchangeRate);
				}
			}
		});

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
						pricer = pricerBusinessDelegate.getPricer(FXNDFTrade.FX_NDF, newPricingParam);
					} catch (TradistaBusinessException abe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());

					if (settlementCurrency.getValue() != null) {
						InterestRateCurve discountCurve = newPricingParam
								.getDiscountCurve(settlementCurrency.getValue());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), settlementCurrency.getValue()));
							TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
						}
					}
				}
			}
		});

		pricingDate.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				cfPricingDate.setText(pricingDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
			}
		});

		pricingDate.setValue(LocalDate.now());
		cfPricingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		nonDeliverableCurrency.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> arg0, Currency arg1, Currency newValue) {
				if (settlementDate.getValue() != null) {
					FXNDFTrade fxNdfTrade = new FXNDFTrade();
					fxNdfTrade.setCurrency(settlementCurrency.getValue());
					fxNdfTrade.setNonDeliverableCurrency(newValue);
					fxNdfTrade.setSettlementDate(settlementDate.getValue());
					try {
						fixingDate.setValue(fxNDFTradeBusinessDelegate.getFixingDate(fxNdfTrade));
					} catch (TradistaBusinessException tbe) {
						// If there is a null value leading to an
						// exception, we do nothing.
					}
				}
			}
		});

		settlementDate.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null) {
					FXNDFTrade fxNdfTrade = new FXNDFTrade();
					fxNdfTrade.setCurrency(settlementCurrency.getValue());
					fxNdfTrade.setNonDeliverableCurrency(nonDeliverableCurrency.getValue());
					fxNdfTrade.setSettlementDate(newDate);
					try {
						fixingDate.setValue(fxNDFTradeBusinessDelegate.getFixingDate(fxNdfTrade));
					} catch (TradistaBusinessException tbe) {
						// If there is a null value leading to an
						// exception, we do nothing.
					}
				}
			}
		});

		final Callback<DatePicker, DateCell> tradingDayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FXNDFTrade fxNdfTrade;

					private boolean isAvailable(LocalDate date) {
						boolean isAvailable = true;

						if (fxNdfTrade == null) {
							fxNdfTrade = new FXNDFTrade();
							fxNdfTrade.setCurrency(settlementCurrency.getValue());
							fxNdfTrade.setNonDeliverableCurrency(nonDeliverableCurrency.getValue());
						}

						try {
							isAvailable = fxNDFTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(date);
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

					FXNDFTrade fxNdfTrade;

					private boolean isAvailable(LocalDate date) {
						if (fxNdfTrade == null) {
							fxNdfTrade = new FXNDFTrade();
							fxNdfTrade.setCurrency(settlementCurrency.getValue());
							fxNdfTrade.setNonDeliverableCurrency(nonDeliverableCurrency.getValue());
						}
						try {
							return fxNDFTradeBusinessDelegate.isBusinessDay(fxNdfTrade, date);
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

		settlementDate.setDayCellFactory(settlementDayCellFactory);
		fixingDate.setDayCellFactory(settlementDayCellFactory);
		tradeDate.setDayCellFactory(tradingDayCellFactory);
		selectedQuoteDate.setDayCellFactory(settlementDayCellFactory);

		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillComboBox(currencyBusinessDelegate.getDeliverableCurrencies(), settlementCurrency);
		TradistaGUIUtil.fillComboBox(currencyBusinessDelegate.getNonDeliverableCurrencies(), nonDeliverableCurrency);
		TradistaGUIUtil.fillCurrencyComboBox(pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
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
				trade.setId(fxNDFTradeBusinessDelegate.saveFXNDFTrade(trade));
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
		LocalDate oldCreationDate = null;
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();
				buildTrade();
				oldTradeId = trade.getId();
				oldCreationDate = trade.getCreationDate();
				trade.setId(0);
				trade.setCreationDate(LocalDate.now());
				trade.setId(fxNDFTradeBusinessDelegate.saveFXNDFTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				trade.setId(oldTradeId);
				trade.setCreationDate(oldCreationDate);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		FXNDFTrade fxNdfTrade;
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

			fxNdfTrade = fxNDFTradeBusinessDelegate.getFXNDFTradeById(tradeId);
			if (fxNdfTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", FXNDFTrade.FX_NDF, load.getText()));
			}
			load(fxNdfTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FXNDFTrade trade) throws TradistaBusinessException {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		settlementDate.setValue(trade.getSettlementDate());
		fixingDate.setValue(fxNDFTradeBusinessDelegate.getFixingDate(trade));
		ndfRate.setText(TradistaGUIUtil.formatAmount(trade.getNdfRate()));
		nonDeliverableCurrency.setValue(trade.getNonDeliverableCurrency());
		notionalAmount.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		settlementCurrency.setValue(trade.getCurrency());
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = fxNDFPricerBusinessDelegate.generateCashFlows(trade, pricingParameter.getValue(),
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
		trade = null;
		tradeId.setText("");
		settlementDate.setValue(null);
		notionalAmount.clear();
		fixingDate.setValue(null);
		ndfRate.clear();
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FXNDFTrade();
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			if (!notionalAmount.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(notionalAmount.getText(), "Notional Amount"));
			}
			if (!ndfRate.getText().isEmpty()) {
				trade.setNdfRate(TradistaGUIUtil.parseAmount(ndfRate.getText(), "NDF Rate"));
			}
			trade.setCurrency(settlementCurrency.getSelectionModel().getSelectedItem());
			trade.setNonDeliverableCurrency(nonDeliverableCurrency.getSelectionModel().getSelectedItem());
			trade.setBook(book.getValue());
			trade.setCounterparty(counterparty.getValue());
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
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillComboBox(currencyBusinessDelegate.getDeliverableCurrencies(), settlementCurrency);
		TradistaGUIUtil.fillComboBox(currencyBusinessDelegate.getNonDeliverableCurrencies(), nonDeliverableCurrency);
		TradistaGUIUtil.fillCurrencyComboBox(pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
	}

	@Override
	public void update(TradistaPublisher publisher) {
		super.update(publisher);
		if (publisher instanceof MarketDataPublisher marketDataPublisher) {
			if (!publisher.isError()) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Set<QuoteValue> quoteValues = marketDataPublisher.getQuoteValues();
						if (quoteValues != null && !quoteValues.isEmpty()) {
							for (QuoteValue qv : quoteValues) {
								if (qv.getQuoteSet().equals(selectedQuoteSet.getValue())) {
									if (qv.getQuote().getName()
											.equals("FX." + settlementCurrency.getValue() + "."
													+ nonDeliverableCurrency.getValue())
											|| qv.getQuote().getName().equals("FX." + nonDeliverableCurrency.getValue()
													+ "." + settlementCurrency.getValue())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (FXNDFTradeDefinitionController.this.quoteValues.contains(qv)) {
													FXNDFTradeDefinitionController.this.quoteValues.remove(qv);
												}
												FXNDFTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FXNDFTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(notionalAmount.getText(), "Notional Amount");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(ndfRate.getText(), "NDF Rate");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}