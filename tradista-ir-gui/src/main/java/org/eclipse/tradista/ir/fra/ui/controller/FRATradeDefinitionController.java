package org.eclipse.tradista.ir.fra.ui.controller;

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
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.ui.controller.CashFlowProperty;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
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
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.ir.fra.service.FRAPricerBusinessDelegate;
import org.eclipse.tradista.ir.fra.service.FRATradeBusinessDelegate;
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

public class FRATradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker startDate;

	@FXML
	private DatePicker endDate;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private TextField notionalAmount;

	@FXML
	private TextField fixedRate;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private ComboBox<Book> book;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private ComboBox<Tenor> referenceRateIndexTenor;

	@FXML
	private ComboBox<DayCountConvention> dayCountConvention;

	@FXML
	private DatePicker paymentDate;

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

	private FRATradeBusinessDelegate fraTradeBusinessDelegate;

	private FRAPricerBusinessDelegate fraPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private FRATrade trade;

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

	private boolean startDateIsChanging = false;

	private boolean paymentDateIsChanging = false;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();
		quoteValues = Collections.synchronizedSet(HashSet.newHashSet(1));
		tradeType.setText("Forward Rate Agreement");

		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		fraTradeBusinessDelegate = new FRATradeBusinessDelegate();
		fraPricerBusinessDelegate = new FRAPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
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
				if (newValue != null && referenceRateIndex.getValue() != null
						&& referenceRateIndexTenor.getValue() != null) {
					String fraRate = FRATrade.FRA + "." + referenceRateIndex.getValue().getName() + "." + "%."
							+ referenceRateIndexTenor.getValue() + "%";
					String fraReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), fraRate, fraReferenceRate);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				if (selectedQuoteDate.getValue() != null && referenceRateIndex.getValue() != null
						&& referenceRateIndexTenor.getValue() != null) {
					String fraRate = FRATrade.FRA + "." + referenceRateIndex.getValue().getName() + "." + "%."
							+ referenceRateIndexTenor.getValue() + "%";
					String fraReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), fraRate,
							fraReferenceRate);
				}
			}
		});

		referenceRateIndex.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
				if (newIndex != null) {
					if (selectedQuoteDate.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						String fraRate = FRATrade.FRA + "." + newIndex + "." + "%." + referenceRateIndexTenor.getValue()
								+ "%";
						String fraReferenceRate = FRATrade.FRA + "." + newIndex.getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), fraRate,
								fraReferenceRate);
					}
				}
			}
		});

		referenceRateIndexTenor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Tenor> observableValue, Tenor oldValue, Tenor newTenor) {
				if (newTenor != null) {
					if (selectedQuoteDate.getValue() != null && referenceRateIndex.getValue() != null) {
						String fraRate = FRATrade.FRA + "." + referenceRateIndex.getValue().getName() + "." + "%."
								+ newTenor + "%";
						String fraReferenceRate = FRATrade.FRA + "." + referenceRateIndex.getValue().getName() + "."
								+ newTenor + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), fraRate,
								fraReferenceRate);
					}
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

		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> arg0, PricingParameter arg1,
					PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(FRATrade.FRA, newPricingParam);
					} catch (TradistaBusinessException abe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());

					if (currency.getValue() != null) {
						InterestRateCurve discountCurve = newPricingParam.getDiscountCurve(currency.getValue());
						if (discountCurve != null) {
							cfDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
						} else {
							cfDiscountCurve.setText(String.format(
									"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
									newPricingParam.getName(), currency.getValue()));
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

		currency.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Currency> observableValue, Currency oldValue,
					Currency newValue) {
				// newValue is null on first call to refresh.
				if (newValue != null) {
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

		referenceRateIndexTenor.valueProperty().addListener(new ChangeListener<>() {
			public void changed(ObservableValue<? extends Tenor> ov, Tenor oldValue, Tenor newValue) {
				if (newValue != null) {
					if (startDate.getValue() != null) {
						try {
							endDate.setValue(DateUtil.addTenor(startDate.getValue(), newValue));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					} else {
						endDate.setValue(null);
					}
				}
			}
		});

		currency.valueProperty().addListener(new ChangeListener<>() {
			public void changed(ObservableValue<? extends Currency> ov, Currency oldValue, Currency newValue) {
				if (newValue != null) {
					if (paymentDate.getValue() != null) {
						try {
							startDate.setValue(DateUtil.addBusinessDay(paymentDate.getValue(),
									currency.getValue().getCalendar(), -2));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					}
				}
			}
		});

		paymentDate.valueProperty().addListener(new ChangeListener<>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					paymentDateIsChanging = true;
					try {
						if (!startDateIsChanging) {
							startDate
									.setValue(DateUtil.addBusinessDay(newValue, currency.getValue().getCalendar(), -2));
						}
					} catch (TradistaBusinessException tbe) {
						// Should not appear here.
					}
					paymentDateIsChanging = false;
				}
			}
		});

		startDate.valueProperty().addListener(new ChangeListener<>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					startDateIsChanging = true;
					try {
						endDate.setValue(DateUtil.addTenor(newValue, referenceRateIndexTenor.getValue()));
						if (!paymentDateIsChanging) {
							paymentDate
									.setValue(DateUtil.addBusinessDay(newValue, currency.getValue().getCalendar(), 2));
						}
					} catch (TradistaBusinessException tbe) {
						// Should not appear here.
					}
					startDateIsChanging = false;
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					FRATrade fraTrade;

					private boolean isAvailable(LocalDate date) {
						if (fraTrade == null) {
							fraTrade = new FRATrade();
							fraTrade.setCurrency(currency.getValue());
						}

						try {
							return fraTradeBusinessDelegate.isBusinessDay(fraTrade, date);
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

		paymentDate.setDayCellFactory(businessDayCellFactory);
		endDate.setDayCellFactory(businessDayCellFactory);
		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(dayCountConvention);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex, referenceRateIndex);
		TradistaGUIUtil.fillTenorComboBox(referenceRateIndexTenor);
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

				trade.setId(fraTradeBusinessDelegate.saveFRATrade(trade));
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
				trade.setId(fraTradeBusinessDelegate.saveFRATrade(trade));
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
		FRATrade fraTrade;
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

			fraTrade = fraTradeBusinessDelegate.getFRATradeById(tradeId);
			if (fraTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", FRATrade.FRA, load.getText()));
			}
			load(fraTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(FRATrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		endDate.setValue(trade.getMaturityDate());
		currency.setValue(trade.getCurrency());
		dayCountConvention.setValue(trade.getDayCountConvention());
		fixedRate.setText(TradistaGUIUtil.formatAmount(trade.getFixedRate()));
		notionalAmount.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		referenceRateIndex.setValue(trade.getReferenceRateIndex());
		referenceRateIndexTenor.setValue(trade.getReferenceRateIndexTenor());
		paymentDate.setValue(trade.getSettlementDate());
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = fraPricerBusinessDelegate.generateCashFlows(trade, pricingParameter.getValue(),
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
		paymentDate.setValue(null);
		notionalAmount.clear();
		fixedRate.clear();
		endDate.setValue(null);
		startDate.setValue(null);
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new FRATrade();
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCurrency(currency.getValue());
			if (!notionalAmount.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(notionalAmount.getText(), "Notional Amount"));
			}
			trade.setMaturityDate(endDate.getValue());
			trade.setPaymentDate(paymentDate.getValue());
			trade.setStartDate(startDate.getValue());
			trade.setDayCountConvention(dayCountConvention.getValue());
			trade.setReferenceRateIndex(referenceRateIndex.getValue());
			trade.setReferenceRateIndexTenor(referenceRateIndexTenor.getValue());
			if (!fixedRate.getText().isEmpty()) {
				trade.setFixedRate(TradistaGUIUtil.parseAmount(fixedRate.getText(), "Fixed Rate"));
			}
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
		} catch (TradistaBusinessException _) {
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
		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);
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
											.equals(FRATrade.FRA + "." + referenceRateIndex.getValue().getName() + "."
													+ referenceRateIndexTenor.getValue() + "%")) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (FRATradeDefinitionController.this.quoteValues.contains(qv)) {
													FRATradeDefinitionController.this.quoteValues.remove(qv);
												}
												FRATradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(FRATradeDefinitionController.this.quoteValues)));
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
			TradistaGUIUtil.checkAmount(fixedRate.getText(), "Fixed Rate");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}