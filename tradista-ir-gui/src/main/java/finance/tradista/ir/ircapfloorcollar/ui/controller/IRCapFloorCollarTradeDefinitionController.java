package finance.tradista.ir.ircapfloorcollar.ui.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import finance.tradista.core.book.model.Book;
import finance.tradista.core.book.service.BookBusinessDelegate;
import finance.tradista.core.book.ui.controller.TradistaBookPieChart;
import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.ui.controller.TradistaTradeBookingController;
import finance.tradista.core.common.ui.publisher.TradistaPublisher;
import finance.tradista.core.common.ui.util.TradistaGUIUtil;
import finance.tradista.core.common.ui.view.TradistaAlert;
import finance.tradista.core.currency.model.Currency;
import finance.tradista.core.daycountconvention.model.DayCountConvention;
import finance.tradista.core.index.model.Index;
import finance.tradista.core.interestpayment.model.InterestPayment;
import finance.tradista.core.legalentity.model.LegalEntity;
import finance.tradista.core.marketdata.model.QuoteSet;
import finance.tradista.core.marketdata.model.QuoteType;
import finance.tradista.core.marketdata.model.QuoteValue;
import finance.tradista.core.marketdata.ui.controller.QuoteProperty;
import finance.tradista.core.marketdata.ui.publisher.MarketDataPublisher;
import finance.tradista.core.pricing.pricer.Parameterizable;
import finance.tradista.core.pricing.pricer.Pricer;
import finance.tradista.core.pricing.pricer.PricerMeasure;
import finance.tradista.core.pricing.pricer.PricingParameter;
import finance.tradista.core.pricing.service.PricerBusinessDelegate;
import finance.tradista.core.product.model.Product;
import finance.tradista.core.tenor.model.Tenor;
import finance.tradista.core.trade.model.Trade;
import finance.tradista.ir.common.util.TradistaIRGUIUtil;
import finance.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import finance.tradista.ir.ircapfloorcollar.service.IRCapFloorCollarTradeBusinessDelegate;
import finance.tradista.ir.irforward.model.IRForwardTrade;
import finance.tradista.ir.irswap.model.IRSwapTrade;
import finance.tradista.legalentity.service.LegalEntityBusinessDelegate;
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

public class IRCapFloorCollarTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private ComboBox<IRCapFloorCollarTrade.Type> capFloorCollar;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker maturityDate;

	@FXML
	private TextField premium;

	@FXML
	private ComboBox<Currency> premiumCurrency;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<Book> book;

	// Underlying properties

	@FXML
	private TextField notionalAmount;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private ComboBox<Tenor> frequency;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private ComboBox<Tenor> referenceRateIndexTenor;

	@FXML
	private ComboBox<DayCountConvention> dayCountConvention;

	@FXML
	private DatePicker settlementDate;

	// Pricer

	@FXML
	private Label pricerLabel;

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
	private TextField capStrike;

	@FXML
	private TextField floorStrike;

	@FXML
	private Label capStrikeTitle;

	@FXML
	private Label floorStrikeTitle;

	@FXML
	private Label pricerQuoteSetLabel;

	@FXML
	private ComboBox<InterestPayment> interestPayment;

	@FXML
	private ComboBox<InterestPayment> interestFixing;

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

	private IRCapFloorCollarTradeBusinessDelegate irCapFloorCollarTradeBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private IRCapFloorCollarTrade trade;

	@FXML
	private Label tradeType;

	@FXML
	private TradistaBookPieChart bookChartPane;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(1));
		tradeType.setText("IR Cap / Floor / Collar Trade");

		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		irCapFloorCollarTradeBusinessDelegate = new IRCapFloorCollarTradeBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

		TradistaGUIUtil.fillTenorComboBox(frequency);

		TradistaIRGUIUtil.fillIRCapFloorCollarTypeComboBox(capFloorCollar);

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

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<QuoteSet>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> observableValue, QuoteSet oldValue,
					QuoteSet newValue) {
				if (newValue != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						fillQuotesTable(newValue, selectedQuoteDate.getValue(), irSwapRate, irSwapReferenceRate);
					}
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				if (newValue != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), newValue, irSwapRate, irSwapReferenceRate);
					}

				}
			}
		});

		referenceRateIndex.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Index>() {
			@Override
			public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					if (newIndex != null) {
						interestFixing.setValue(newIndex.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
								: InterestPayment.END_OF_PERIOD);
						if (referenceRateIndexTenor.getValue() != null) {
							irSwapRate = IRSwapTrade.IR_SWAP + "." + newIndex.getName() + "."
									+ referenceRateIndexTenor.getValue() + "%";
							irSwapReferenceRate = Index.INDEX + "." + newIndex.getName() + "."
									+ referenceRateIndexTenor.getValue() + "%";
							fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
									irSwapReferenceRate);
						}
					}
				}
			}
		});

		referenceRateIndexTenor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tenor>() {
			@Override
			public void changed(ObservableValue<? extends Tenor> observableValue, Tenor oldTenor, Tenor newTenor) {
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					if (newTenor != null && referenceRateIndex.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ newTenor + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ newTenor + "%";
						fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
								irSwapReferenceRate);
					}

				}
			}
		});

		pricingDate.setValue(LocalDate.now());

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
				// newPricingParameter is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(IRCapFloorCollarTrade.IR_CAP_FLOOR_COLLAR,
								newPricingParam);
					} catch (TradistaBusinessException tbe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
				}
			}
		});

		capFloorCollar.valueProperty().addListener(new ChangeListener<IRCapFloorCollarTrade.Type>() {
			@Override
			public void changed(ObservableValue<? extends IRCapFloorCollarTrade.Type> observableValue,
					IRCapFloorCollarTrade.Type oldValue, IRCapFloorCollarTrade.Type newValue) {
				switch (newValue) {
				case CAP: {
					capStrikeTitle.setVisible(true);
					capStrike.setVisible(true);
					floorStrikeTitle.setVisible(false);
					floorStrike.setVisible(false);
					break;
				}
				case FLOOR: {
					capStrikeTitle.setVisible(false);
					capStrike.setVisible(false);
					floorStrikeTitle.setVisible(true);
					floorStrike.setVisible(true);
					break;
				}
				case COLLAR: {
					capStrikeTitle.setVisible(true);
					capStrike.setVisible(true);
					floorStrikeTitle.setVisible(true);
					floorStrike.setVisible(true);
					break;
				}
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					IRCapFloorCollarTrade irCapFloorCollarTrade;

					private boolean isAvailable(LocalDate date) {
						if (irCapFloorCollarTrade == null) {
							irCapFloorCollarTrade = new IRCapFloorCollarTrade();
							IRForwardTrade<Product> irForwardTrade = new IRForwardTrade<Product>();
							irForwardTrade.setCurrency(currency.getValue());
							irCapFloorCollarTrade.setIrForwardTrade(irForwardTrade);
						}

						try {
							return irCapFloorCollarTradeBusinessDelegate.isBusinessDay(irCapFloorCollarTrade, date);
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

		settlementDate.setDayCellFactory(businessDayCellFactory);
		maturityDate.setDayCellFactory(businessDayCellFactory);
		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(currency, premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(dayCountConvention);
		TradistaGUIUtil.fillTenorComboBox(referenceRateIndexTenor);
		TradistaGUIUtil.fillInterestPaymentComboBox(interestPayment, interestFixing);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);

		interestPayment.setValue(InterestPayment.END_OF_PERIOD);

		isRealTime.setDisable(selectedQuoteSet.getValue() == null);
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

				trade.setId(irCapFloorCollarTradeBusinessDelegate.saveIRCapFloorCollarTrade(trade));
				IRCapFloorCollarTrade existingTrade = irCapFloorCollarTradeBusinessDelegate
						.getIRCapFloorCollarTradeById(trade.getId());
				if (existingTrade.getIrForwardTrade() != null) {
					trade.getIrForwardTrade().setId(existingTrade.getIrForwardTrade().getId());
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
		long oldIrForwardTradeId = 0;
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {

			try {
				checkAmounts();

				buildTrade();
				oldTradeId = trade.getId();
				oldIrForwardTradeId = trade.getIrForwardTrade().getId();
				trade.setId(0);
				trade.setId(irCapFloorCollarTradeBusinessDelegate.saveIRCapFloorCollarTrade(trade));
				IRCapFloorCollarTrade existingTrade = irCapFloorCollarTradeBusinessDelegate
						.getIRCapFloorCollarTradeById(trade.getId());
				if (existingTrade.getIrForwardTrade() != null) {
					trade.getIrForwardTrade().setId(existingTrade.getIrForwardTrade().getId());
				}
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				trade.setId(oldTradeId);
				trade.getIrForwardTrade().setId(oldIrForwardTradeId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		IRCapFloorCollarTrade irCapFloorCollarTrade;
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

			irCapFloorCollarTrade = irCapFloorCollarTradeBusinessDelegate.getIRCapFloorCollarTradeById(tradeId);
			if (irCapFloorCollarTrade == null) {
				throw new TradistaBusinessException(String.format("The %s trade %s was not found.",
						IRCapFloorCollarTrade.IR_CAP_FLOOR_COLLAR, load.getText()));
			}
			load(irCapFloorCollarTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(IRCapFloorCollarTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		premiumCurrency.setValue(trade.getCurrency());
		premium.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		if (trade.isCap()) {
			capFloorCollar.setValue(IRCapFloorCollarTrade.Type.CAP);
			capStrike.setText(TradistaGUIUtil.formatAmount(trade.getCapStrike()));
			capStrike.setVisible(true);
			floorStrike.setVisible(false);
		}
		if (trade.isFloor()) {
			capFloorCollar.setValue(IRCapFloorCollarTrade.Type.FLOOR);
			floorStrike.setText(TradistaGUIUtil.formatAmount(trade.getFloorStrike()));
			floorStrike.setVisible(true);
			capStrike.setVisible(false);
		}
		if (trade.isCollar()) {
			capFloorCollar.setValue(IRCapFloorCollarTrade.Type.COLLAR);
			floorStrike.setText(TradistaGUIUtil.formatAmount(trade.getFloorStrike()));
			capStrike.setText(TradistaGUIUtil.formatAmount(trade.getCapStrike()));
			floorStrike.setVisible(true);
			capStrike.setVisible(true);
		}
		notionalAmount.setText(TradistaGUIUtil.formatAmount(trade.getIrForwardTrade().getAmount()));
		currency.setValue(trade.getIrForwardTrade().getCurrency());
		frequency.setValue(trade.getIrForwardTrade().getFrequency());
		referenceRateIndex.setValue(trade.getIrForwardTrade().getReferenceRateIndex());
		referenceRateIndexTenor.setValue(trade.getIrForwardTrade().getReferenceRateIndexTenor());
		dayCountConvention.setValue(trade.getIrForwardTrade().getDayCountConvention());
		maturityDate.setValue(trade.getIrForwardTrade().getMaturityDate());
		settlementDate.setValue(trade.getIrForwardTrade().getSettlementDate());
		interestPayment.setValue(trade.getIrForwardTrade().getInterestPayment());
		interestFixing.setValue(trade.getIrForwardTrade().getInterestFixing());
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		premium.clear();
		capStrike.clear();
		floorStrike.clear();
		notionalAmount.clear();
		maturityDate.setValue(null);
		settlementDate.setValue(null);
	}

	private IRCapFloorCollarTrade buildTrade() {
		if (this.trade == null) {
			trade = new IRCapFloorCollarTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCurrency(premiumCurrency.getValue());
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			if (!premium.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(premium.getText(), "Premium"));
			}
			if (!capStrike.getText().isEmpty()) {
				trade.setCapStrike(TradistaGUIUtil.parseAmount(capStrike.getText(), "Cap Strike"));
			}
			if (!floorStrike.getText().isEmpty()) {
				trade.setFloorStrike(TradistaGUIUtil.parseAmount(floorStrike.getText(), "Floor Strike"));
			}

			// Building the ir forward trade
			if (trade.getIrForwardTrade() == null) {
				trade.setIrForwardTrade(new IRForwardTrade<Product>());
				trade.getIrForwardTrade().setCreationDate(LocalDate.now());
			}

			if (!notionalAmount.getText().isEmpty()) {
				trade.getIrForwardTrade()
						.setAmount(TradistaGUIUtil.parseAmount(notionalAmount.getText(), "Notional Amount"));
			}

			trade.getIrForwardTrade()
					.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.getIrForwardTrade().setCurrency(currency.getValue());
			trade.getIrForwardTrade().setFrequency(frequency.getValue());
			trade.getIrForwardTrade().setMaturityDate(maturityDate.getValue());
			trade.getIrForwardTrade().setSettlementDate(settlementDate.getValue());
			trade.getIrForwardTrade().setDayCountConvention(dayCountConvention.getValue());
			trade.getIrForwardTrade().setReferenceRateIndex(referenceRateIndex.getValue());
			trade.getIrForwardTrade().setReferenceRateIndexTenor(referenceRateIndexTenor.getValue());
			trade.getIrForwardTrade().setBook(book.getValue());
			trade.getIrForwardTrade().setCounterparty(counterparty.getValue());
			trade.getIrForwardTrade().setInterestPayment(interestPayment.getValue());
			trade.getIrForwardTrade().setInterestFixing(interestFixing.getValue());
		} catch (TradistaBusinessException tbe) {
			// Should not happen here.
		}

		return trade;
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
		TradistaGUIUtil.fillCurrencyComboBox(currency, premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);
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
											.equals(IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName()
													+ "." + referenceRateIndexTenor.getValue() + "%")) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (IRCapFloorCollarTradeDefinitionController.this.quoteValues
														.contains(qv)) {
													IRCapFloorCollarTradeDefinitionController.this.quoteValues
															.remove(qv);
												}
												IRCapFloorCollarTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}

							quotesTable.setItems(FXCollections.observableArrayList(QuoteProperty
									.toQuotePropertyList(IRCapFloorCollarTradeDefinitionController.this.quoteValues)));
						}
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
			TradistaGUIUtil.checkAmount(premium.getText(), "Premium");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(capStrike.getText(), "Cap Strike");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(floorStrike.getText(), "Floor Strike");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}