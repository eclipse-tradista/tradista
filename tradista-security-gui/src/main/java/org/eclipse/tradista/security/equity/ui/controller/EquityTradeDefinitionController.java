package org.eclipse.tradista.security.equity.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
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
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityPricerBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityTradeBusinessDelegate;

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

public class EquityTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private ComboBox<Equity> equity;

	@FXML
	private TextField quantity;

	@FXML
	private TextField tradePrice;

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
	private Label result;

	@FXML
	private Label pricerLabel;

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

	@FXML
	private Label cfPricingDate;

	@FXML
	private Label cfDiscountCurve;

	@FXML
	private Button generate;

	private EquityBusinessDelegate equityBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private EquityTradeBusinessDelegate equityTradeBusinessDelegate;

	private EquityPricerBusinessDelegate equityPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	@FXML
	private Label tradeType;

	private EquityTrade trade;

	@FXML
	private TradistaBookPieChart bookChartPane;

	private ChangeListener<Equity> equityListener;

	private ChangeListener<LocalDate> tradeDateListener;

	private ChangeListener<Trade.Direction> directionListener;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		tradeType.setText("Equity Trade");
		quoteValues = Collections.synchronizedSet(HashSet.newHashSet(1));

		equityBusinessDelegate = new EquityBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		equityTradeBusinessDelegate = new EquityTradeBusinessDelegate();
		equityPricerBusinessDelegate = new EquityPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		tradeDate.setValue(LocalDate.now());

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

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> observableValue, QuoteSet oldValue,
					QuoteSet newValue) {
				if (newValue != null && equity.getValue() != null && selectedQuoteDate.getValue() != null) {
					String quoteName = Equity.EQUITY + "." + equity.getValue().getIsin() + "."
							+ equity.getValue().getExchange();
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), quoteName);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				Equity eq = equity.getValue();
				if (eq != null) {
					String quoteName = Equity.EQUITY + "." + equity.getValue().getIsin() + "."
							+ equity.getValue().getExchange();
					fillQuotesTable(selectedQuoteSet.getValue(), newValue, quoteName);
				}
			}
		});

		equity.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Equity> observableValue, Equity oldValue, Equity newValue) {
				LocalDate quoteDate = selectedQuoteDate.getValue();
				if (newValue != null) {
					if (quoteDate != null) {
						String quoteName = Equity.EQUITY + "." + newValue.getIsin() + "." + newValue.getExchange();
						fillQuotesTable(selectedQuoteSet.getValue(), quoteDate, quoteName);
					}
					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue()
								.getDiscountCurve(newValue.getCurrency());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									pricingParameter.getValue().getName(), newValue.getCurrency()));
							TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
						}
					}
				}
			}
		});

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
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

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> observableValue,
					PricingParameter oldPricingParam, PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(Equity.EQUITY, newPricingParam);
					} catch (TradistaBusinessException abe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());

					if (equity.getValue() != null) {
						InterestRateCurve discountCurve = newPricingParam
								.getDiscountCurve(equity.getValue().getCurrency());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), equity.getValue().getCurrency()));
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

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					private boolean isAvailable(LocalDate date) {
						EquityTrade equityTrade = new EquityTrade();
						equityTrade.setProduct(equity.getValue());
						if (equityTrade.getProduct() != null) {
							try {
								return equityTradeBusinessDelegate.isBusinessDay(equityTrade, date);
							} catch (TradistaBusinessException tbe) {
								// TODO Auto-generated catch block
								tbe.printStackTrace();
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

		tradeDate.setDayCellFactory(dayCellFactory);
		selectedQuoteDate.setDayCellFactory(dayCellFactory);

		tradeDateListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null && equity.getValue() != null && selectedQuoteSet.getValue() != null) {

					if (!newDate.isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(newDate, buySell.getValue(), equity.getValue(),
											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Equity %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													equity.getValue(), newDate, selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											tradePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get equity %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												equity.getValue(), newDate, selectedQuoteSet.getValue()));
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

		directionListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Trade.Direction> arg0, Trade.Direction arg1,
					Trade.Direction newDirection) {
				if (tradeDate.getValue() != null && equity.getValue() != null && selectedQuoteSet.getValue() != null) {

					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(tradeDate.getValue(), newDirection,
											equity.getValue(),

											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Equity %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													equity.getValue(), tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											tradePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException _) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get equity %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												equity.getValue(), tradeDate.getValue(), selectedQuoteSet.getValue()));
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

		equityListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Equity> arg0, Equity arg1, Equity newEquity) {
				if (tradeDate.getValue() != null && newEquity != null && selectedQuoteSet.getValue() != null) {

					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getTradePrice(tradeDate.getValue(), buySell.getValue(),
											newEquity, selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(priceWarning);
											TradistaGUIUtil.applyWarningStyle(priceWarning);
											priceWarning.setVisible(true);
											priceWarning.setText(String.format(
													"Equity %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													newEquity, tradeDate.getValue(), selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											tradePrice.setText(TradistaGUIUtil.formatAmount(price));
											priceWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException _) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(priceWarning);
										TradistaGUIUtil.unapplyWarningStyle(priceWarning);
										priceWarning.setText(String.format(
												"Cannot get equity %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												newEquity, tradeDate.getValue(), selectedQuoteSet.getValue()));
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
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
	}

	private void addListeners() {
		tradeDate.valueProperty().addListener(tradeDateListener);
		buySell.valueProperty().addListener(directionListener);
		equity.valueProperty().addListener(equityListener);
	}

	private BigDecimal getTradePrice(LocalDate tradeDate, Trade.Direction direction, Equity equity, long quoteSetId) {
		QuoteValue qv = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId,
				Equity.EQUITY + "." + equity.getIsin() + "." + equity.getExchange(), QuoteType.EQUITY_PRICE, tradeDate);
		BigDecimal equityPrice = null;
		if (qv == null) {
			return null;
		}
		if (qv.getClose() != null) {
			equityPrice = qv.getClose();
		}
		if (direction.equals(Trade.Direction.BUY)) {
			if (qv.getAsk() != null) {
				equityPrice = qv.getAsk();
			}
		} else {
			if (qv.getBid() != null) {
				equityPrice = qv.getBid();
			}
		}
		if (equityPrice == null) {
			equityPrice = qv.getLast();
		}
		if (equityPrice != null) {
			return equityPrice;
		}
		return null;
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new EquityTrade();
		}
		try {
			if (!tradePrice.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(tradePrice.getText(), "Trade Price"));
			}
			if (!quantity.getText().isEmpty()) {
				trade.setQuantity(TradistaGUIUtil.parseAmount(quantity.getText(), "Quantity"));
			}
			trade.setProduct(equity.getValue());
			trade.setTradeDate(tradeDate.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
		} catch (TradistaBusinessException _) {
			// Should not happen at this stage.
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

				trade.setId(equityTradeBusinessDelegate.saveEquityTrade(trade));
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
				trade.setId(equityTradeBusinessDelegate.saveEquityTrade(trade));
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
		EquityTrade equityTrade;
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

			equityTrade = equityTradeBusinessDelegate.getEquityTradeById(tradeId);
			if (equityTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", Equity.EQUITY, load.getText()));
			}
			load(equityTrade);
		} catch (TradistaBusinessException abe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(EquityTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		equity.setValue((Equity) trade.getProduct());
		quantity.setText(TradistaGUIUtil.formatAmount(trade.getQuantity()));
		settlementDate.setValue(trade.getSettlementDate());
		tradePrice.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = equityPricerBusinessDelegate.generateCashFlows(trade,
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
		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
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
									if (qv.getQuote().getName().equals(Equity.EQUITY + "." + equity.getValue().getIsin()
											+ "." + equity.getValue().getExchange())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (EquityTradeDefinitionController.this.quoteValues.contains(qv)) {
													EquityTradeDefinitionController.this.quoteValues.remove(qv);
												}
												EquityTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(EquityTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(tradePrice.getText(), "Trade Price");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(quantity.getText(), "Quantity");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		settlementDate.setValue(null);
		tradePrice.clear();
		quantity.clear();
	}

}