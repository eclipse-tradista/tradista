package finance.tradista.ir.irswapoption.ui.controller;

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
import finance.tradista.core.common.util.DateUtil;
import finance.tradista.core.currency.model.Currency;
import finance.tradista.core.daycountconvention.model.DayCountConvention;
import finance.tradista.core.index.model.BlankIndex;
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
import finance.tradista.core.tenor.model.Tenor;
import finance.tradista.core.trade.model.OptionTrade;
import finance.tradista.core.trade.model.OptionTrade.SettlementType;
import finance.tradista.core.trade.model.Trade;
import finance.tradista.core.trade.model.VanillaOptionTrade;
import finance.tradista.ir.irswap.model.IRSwapTrade;
import finance.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import finance.tradista.ir.irswap.service.IRSwapTradeBusinessDelegate;
import finance.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import finance.tradista.ir.irswapoption.service.IRSwapOptionTradeBusinessDelegate;
import finance.tradista.legalentity.service.LegalEntityBusinessDelegate;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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

public class IRSwapOptionTradeDefinitionController extends TradistaTradeBookingController {

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
	private DatePicker maturityDate;

	@FXML
	private DatePicker exerciseDate;

	@FXML
	private TextField premium;

	@FXML
	private TextField settlementDateOffset;

	@FXML
	private TextField cashSettlementAmount;

	@FXML
	private ComboBox<Index> alternativeCashSettlementReferenceRateIndex;

	@FXML
	private ComboBox<Tenor> alternativeCashSettlementReferenceRateIndexTenor;

	@FXML
	private Label cashSettlementAmountLabel;

	@FXML
	private Label alternativeCashSettlementReferenceRateIndexLabel;

	@FXML
	private Label alternativeCashSettlementReferenceRateIndexTenorLabel;

	@FXML
	private TextField strike;

	@FXML
	private ComboBox<VanillaOptionTrade.Style> style;

	@FXML
	private ComboBox<Currency> premiumCurrency;

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<Book> book;

	// Underlying properties

	@FXML
	private CheckBox interestsToPayFixed;

	@FXML
	private TextField notionalAmount;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private ComboBox<Tenor> paymentFrequency;

	@FXML
	private ComboBox<Tenor> receptionFrequency;

	@FXML
	private TextField fixedInterestRate;

	@FXML
	private ComboBox<Index> paymentReferenceRateIndex;

	@FXML
	private ComboBox<Tenor> paymentReferenceRateIndexTenor;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private ComboBox<Tenor> referenceRateIndexTenor;

	@FXML
	private TextField paymentSpread;

	@FXML
	private TextField receptionSpread;

	@FXML
	private ComboBox<DayCountConvention> paymentDayCountConvention;

	@FXML
	private ComboBox<DayCountConvention> receptionDayCountConvention;

	@FXML
	private DatePicker underlyingMaturityDate;

	@FXML
	private ComboBox<Tenor> underlyingMaturityTenor;

	@FXML
	private Label paymentReferenceRateIndexLabel;

	@FXML
	private Label paymentReferenceRateIndexTenorLabel;

	@FXML
	private Label paymentSpreadLabel;

	@FXML
	private Label paymentFixedInterestRateLabel;

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

	private IRSwapOptionTradeBusinessDelegate irSwapOptionTradeBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private IRSwapOptionTrade trade;

	@FXML
	private Label tradeType;

	@FXML
	private ComboBox<InterestPayment> paymentInterestPayment;

	@FXML
	private ComboBox<InterestPayment> receptionInterestPayment;

	@FXML
	private ComboBox<InterestPayment> paymentInterestFixing;

	@FXML
	private ComboBox<InterestPayment> receptionInterestFixing;

	@FXML
	private Label paymentInterestFixingLabel;

	@FXML
	private TradistaBookPieChart bookChartPane;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(2));
		tradeType.setText("IR Swap Option trade");

		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		irSwapOptionTradeBusinessDelegate = new IRSwapOptionTradeBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
		TradistaGUIUtil.fillTenorComboBox(paymentFrequency, receptionFrequency);
		TradistaGUIUtil.fillOptionStyleComboBox(style);
		TradistaGUIUtil.fillOptionTypeComboBox(callPut);
		TradistaGUIUtil.fillOptionSettlementTypeComboBox(settlementType);

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
					String irSwapPaymentReferenceRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (!interestsToPayFixed.isSelected()) {
						if (paymentReferenceRateIndex.getValue() != null
								&& paymentReferenceRateIndexTenor.getValue() != null) {
							irSwapPaymentReferenceRate = Index.INDEX + "."
									+ paymentReferenceRateIndex.getValue().getName() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
					}
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), irSwapRate, irSwapReferenceRate,
							irSwapPaymentReferenceRate);
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
					String irSwapPaymentReferenceRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (!interestsToPayFixed.isSelected()) {
						if (paymentReferenceRateIndex.getValue() != null
								&& paymentReferenceRateIndexTenor.getValue() != null) {
							irSwapPaymentReferenceRate = Index.INDEX + "."
									+ paymentReferenceRateIndex.getValue().getName() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
					}
					fillQuotesTable(selectedQuoteSet.getValue(), newValue, irSwapRate, irSwapReferenceRate,
							irSwapPaymentReferenceRate);
				}
			}
		});

		referenceRateIndex.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Index>() {
			@Override
			public void changed(ObservableValue<? extends Index> arg0, Index arg1, Index newIndex) {
				if (newIndex != null) {
					receptionInterestFixing.setValue(newIndex.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
							: InterestPayment.END_OF_PERIOD);
				}
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					String irSwapPaymentReferenceRate = null;
					if (newIndex != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + newIndex.getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + newIndex.getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (!interestsToPayFixed.isSelected()) {
						if (paymentReferenceRateIndex.getValue() != null
								&& paymentReferenceRateIndexTenor.getValue() != null) {
							irSwapPaymentReferenceRate = Index.INDEX + "."
									+ paymentReferenceRateIndex.getValue().getName() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
					}
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
							irSwapReferenceRate, irSwapPaymentReferenceRate);
				}
			}
		});

		referenceRateIndexTenor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tenor>() {
			@Override
			public void changed(ObservableValue<? extends Tenor> observableValue, Tenor oldTenor, Tenor newTenor) {
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					String irSwapPaymentReferenceRate = null;
					if (newTenor != null && referenceRateIndex.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ newTenor + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ newTenor + "%";
					}
					if (!interestsToPayFixed.isSelected()) {
						if (paymentReferenceRateIndex.getValue() != null
								&& paymentReferenceRateIndexTenor.getValue() != null) {
							irSwapPaymentReferenceRate = Index.INDEX + "."
									+ paymentReferenceRateIndex.getValue().getName() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
					}
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
							irSwapReferenceRate, irSwapPaymentReferenceRate);
				}
			}
		});

		paymentReferenceRateIndex.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Index>() {
			@Override
			public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
				if (newIndex != null) {
					paymentInterestFixing.setValue(newIndex.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
							: InterestPayment.END_OF_PERIOD);
				}
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					String irSwapPaymentReferenceRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (!interestsToPayFixed.isSelected()) {
						if (newIndex != null) {
							irSwapPaymentReferenceRate = Index.INDEX + "." + newIndex.getName() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
					}
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
							irSwapReferenceRate, irSwapPaymentReferenceRate);
				}
			}
		});

		paymentReferenceRateIndexTenor.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<Tenor>() {
					@Override
					public void changed(ObservableValue<? extends Tenor> observableValue, Tenor oldTenor,
							Tenor newTenor) {
						if (selectedQuoteDate.getValue() != null) {
							String irSwapRate = null;
							String irSwapReferenceRate = null;
							String irSwapPaymentReferenceRate = null;
							if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
								irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
										+ referenceRateIndexTenor.getValue() + "%";
								irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
										+ referenceRateIndexTenor.getValue() + "%";
							}
							if (!interestsToPayFixed.isSelected()) {
								if (paymentReferenceRateIndex != null && newTenor != null) {
									irSwapPaymentReferenceRate = Index.INDEX + "."
											+ paymentReferenceRateIndex.getValue().getName() + "." + newTenor + "%";
								}
							}
							fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
									irSwapReferenceRate, irSwapPaymentReferenceRate);
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
						pricer = pricerBusinessDelegate.getPricer(IRSwapOptionTrade.IR_SWAP_OPTION, newPricingParam);
					} catch (TradistaBusinessException abe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());
				}
			}
		});

		interestsToPayFixed.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				paymentFixedInterestRateLabel.setVisible(new_val);
				fixedInterestRate.setVisible(new_val);
				paymentReferenceRateIndexLabel.setVisible(!new_val);
				paymentReferenceRateIndex.setVisible(!new_val);
				paymentReferenceRateIndexTenor.setVisible(!new_val);
				paymentReferenceRateIndexTenorLabel.setVisible(!new_val);
				paymentSpread.setVisible(!new_val);
				paymentSpreadLabel.setVisible(!new_val);
				paymentInterestFixingLabel.setVisible(!new_val);
				paymentInterestFixing.setVisible(!new_val);
			}
		});

		settlementType.valueProperty().addListener(new ChangeListener<SettlementType>() {
			public void changed(ObservableValue<? extends SettlementType> ov, SettlementType oldValue,
					SettlementType newValue) {
				if (newValue != null) {
					boolean isCash = newValue.equals(SettlementType.CASH);
					cashSettlementAmount.setVisible(isCash);
					alternativeCashSettlementReferenceRateIndex.setVisible(isCash);
					alternativeCashSettlementReferenceRateIndexTenor.setVisible(isCash);
					cashSettlementAmountLabel.setVisible(isCash);
					alternativeCashSettlementReferenceRateIndexLabel.setVisible(isCash);
					alternativeCashSettlementReferenceRateIndexTenorLabel.setVisible(isCash);
				}
			}
		});

		underlyingMaturityTenor.valueProperty().addListener(new ChangeListener<Tenor>() {
			public void changed(ObservableValue<? extends Tenor> ov, Tenor oldValue, Tenor newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!newValue.equals(Tenor.NO_TENOR));
					underlyingMaturityDate.setDisable(tenorIsSpecified);
					if (tenorIsSpecified) {
						LocalDate startingDate = null;
						if (exerciseDate.getValue() != null) {
							startingDate = exerciseDate.getValue();
						}
						if (style.getValue().equals(VanillaOptionTrade.Style.EUROPEAN)) {
							startingDate = maturityDate.getValue();
						}
						if (startingDate != null) {
							try {
								underlyingMaturityDate.setValue(DateUtil.addTenor(startingDate.minusDays(1), newValue));
							} catch (TradistaBusinessException abe) {
								// Should not appear here.
							}
						} else {
							underlyingMaturityDate.setValue(null);
						}
					}
				}
			}
		});

		exerciseDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!underlyingMaturityTenor.getValue().equals(Tenor.NO_TENOR));
					if (tenorIsSpecified) {
						try {
							underlyingMaturityDate.setValue(
									DateUtil.addTenor(newValue.minusDays(1), underlyingMaturityTenor.getValue()));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					}
				}
			}
		});

		maturityDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					if (style.getValue().equals(VanillaOptionTrade.Style.EUROPEAN)) {
						boolean tenorIsSpecified = (!underlyingMaturityTenor.getValue().equals(Tenor.NO_TENOR));
						if (tenorIsSpecified) {
							try {
								underlyingMaturityDate.setValue(
										DateUtil.addTenor(newValue.minusDays(1), underlyingMaturityTenor.getValue()));
							} catch (TradistaBusinessException tbe) {
								// Should not appear here.
							}
						}
					}
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					IRSwapOptionTrade irSwapOptionTrade;

					private boolean isAvailable(LocalDate date) {
						if (irSwapOptionTrade == null) {
							irSwapOptionTrade = new IRSwapOptionTrade();
							SingleCurrencyIRSwapTrade irSwap = new SingleCurrencyIRSwapTrade();
							irSwap.setCurrency(currency.getValue());
							irSwapOptionTrade.setUnderlying(irSwap);
						}

						try {
							return irSwapOptionTradeBusinessDelegate.isBusinessDay(irSwapOptionTrade, date);
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

		maturityDate.setDayCellFactory(businessDayCellFactory);
		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);
		exerciseDate.setDayCellFactory(businessDayCellFactory);
		settlementDate.setDayCellFactory(businessDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(currency, premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(paymentDayCountConvention, receptionDayCountConvention);
		alternativeCashSettlementReferenceRateIndex.getItems().add(0, BlankIndex.getInstance());
		alternativeCashSettlementReferenceRateIndex.setValue(BlankIndex.getInstance());
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
		TradistaGUIUtil.fillTenorComboBox(paymentFrequency, receptionFrequency, paymentReferenceRateIndexTenor,
				referenceRateIndexTenor, alternativeCashSettlementReferenceRateIndexTenor, underlyingMaturityTenor);
		TradistaGUIUtil.fillOptionStyleComboBox(style);
		TradistaGUIUtil.fillOptionTypeComboBox(callPut);
		TradistaGUIUtil.fillOptionSettlementTypeComboBox(settlementType);
		TradistaGUIUtil.fillInterestPaymentComboBox(paymentInterestPayment, receptionInterestPayment,
				paymentInterestFixing, receptionInterestFixing);
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex,
				alternativeCashSettlementReferenceRateIndex);
		paymentInterestPayment.setValue(InterestPayment.END_OF_PERIOD);
		receptionInterestPayment.setValue(InterestPayment.END_OF_PERIOD);

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

				trade.setId(irSwapOptionTradeBusinessDelegate.saveIRSwapOptionTrade(trade));
				IRSwapOptionTrade existingTrade = irSwapOptionTradeBusinessDelegate
						.getIRSwapOptionTradeById(trade.getId());
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
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				buildTrade();
				oldTradeId = trade.getId();
				oldUnderlyingTradeId = trade.getUnderlying().getId();
				trade.setId(0);
				trade.getUnderlying().setId(0);
				trade.setId(irSwapOptionTradeBusinessDelegate.saveIRSwapOptionTrade(trade));
				IRSwapOptionTrade existingTrade = irSwapOptionTradeBusinessDelegate
						.getIRSwapOptionTradeById(trade.getId());
				if (existingTrade.getUnderlying() != null) {
					trade.getUnderlying().setId(existingTrade.getUnderlying().getId());
				}
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException tbe) {
				trade.setId(oldTradeId);
				trade.getUnderlying().setId(oldUnderlyingTradeId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		IRSwapOptionTrade irSwapOptionTrade;
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

			irSwapOptionTrade = irSwapOptionTradeBusinessDelegate.getIRSwapOptionTradeById(tradeId);
			if (irSwapOptionTrade == null) {
				throw new TradistaBusinessException(String.format("The %s trade %s was not found.",
						IRSwapOptionTrade.IR_SWAP_OPTION, load.getText()));
			}
			load(irSwapOptionTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(IRSwapOptionTrade trade) {
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
		exerciseDate.setValue(exerciseDate.getValue());
		currency.setValue(trade.getUnderlying().getCurrency());
		if (trade.getUnderlying().isInterestsToPayFixed()) {
			fixedInterestRate
					.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getPaymentFixedInterestRate()));
		}
		paymentFrequency.setValue(trade.getUnderlying().getPaymentFrequency());
		receptionFrequency.setValue(trade.getUnderlying().getReceptionFrequency());
		interestsToPayFixed.setSelected(trade.getUnderlying().isInterestsToPayFixed());
		notionalAmount.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getAmount()));
		paymentDayCountConvention.setValue(trade.getUnderlying().getPaymentDayCountConvention());
		paymentReferenceRateIndex.setValue(trade.getUnderlying().getPaymentReferenceRateIndex());
		paymentReferenceRateIndexTenor.setValue(trade.getUnderlying().getPaymentReferenceRateIndexTenor());
		if (trade.getUnderlying().getPaymentSpread() != null) {
			paymentSpread.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getPaymentSpread()));
		}
		receptionDayCountConvention.setValue(trade.getUnderlying().getReceptionDayCountConvention());
		referenceRateIndex.setValue(trade.getUnderlying().getReceptionReferenceRateIndex());
		referenceRateIndexTenor.setValue(trade.getUnderlying().getReceptionReferenceRateIndexTenor());
		if (trade.getUnderlying().getReceptionSpread() != null) {
			receptionSpread.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getReceptionSpread()));
		}
		underlyingMaturityDate.setValue(trade.getUnderlying().getMaturityDate());
		if (trade.getUnderlying().getMaturityTenor() != null) {
			underlyingMaturityTenor.setValue(trade.getUnderlying().getMaturityTenor());
		}
		if (trade.getCashSettlementAmount() != null) {
			cashSettlementAmount.setText(TradistaGUIUtil.formatAmount(trade.getCashSettlementAmount()));
		}
		if (trade.getAlternativeCashSettlementReferenceRateIndex() != null) {
			alternativeCashSettlementReferenceRateIndex
					.setValue(trade.getAlternativeCashSettlementReferenceRateIndex());
		} else {
			alternativeCashSettlementReferenceRateIndex.setValue(BlankIndex.getInstance());
		}
		alternativeCashSettlementReferenceRateIndexTenor
				.setValue(trade.getAlternativeCashSettlementReferenceRateIndexTenor());
		paymentInterestPayment.setValue(trade.getUnderlying().getPaymentInterestPayment());
		receptionInterestPayment.setValue(trade.getUnderlying().getReceptionInterestPayment());
		paymentInterestFixing.setValue(trade.getUnderlying().getPaymentInterestFixing());
		receptionInterestFixing.setValue(trade.getUnderlying().getReceptionInterestFixing());
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		strike.clear();
		premium.clear();
		maturityDate.setValue(null);
		settlementDate.setValue(null);
		underlyingMaturityDate.setValue(null);
		settlementDateOffset.clear();
		notionalAmount.clear();
		maturityDate.setValue(null);
		fixedInterestRate.clear();
		paymentSpread.clear();
		receptionSpread.clear();
		cashSettlementAmount.clear();
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new IRSwapOptionTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCurrency(premiumCurrency.getValue());
			if (!premium.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(premium.getText(), "Premium"));
			}
			trade.setMaturityDate(maturityDate.getValue());
			trade.setExerciseDate(exerciseDate.getValue());
			trade.setType(callPut.getValue());
			trade.setStyle(style.getValue());
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			trade.setSettlementType(settlementType.getValue());
			if (!settlementDateOffset.getText().isEmpty()) {
				trade.setSettlementDateOffset(Integer.parseInt(settlementDateOffset.getText()));
			}
			if (settlementType.getValue().equals(SettlementType.CASH)) {
				if (!cashSettlementAmount.getText().isEmpty()) {
					trade.setAmount(TradistaGUIUtil.parseAmount(cashSettlementAmount.getText(), "Cash Settlement"));
				} else {
					if (!alternativeCashSettlementReferenceRateIndex.getValue().equals(BlankIndex.getInstance())) {
						trade.setAlternativeCashSettlementReferenceRateIndex(
								alternativeCashSettlementReferenceRateIndex.getValue());
						trade.setAlternativeCashSettlementReferenceRateIndexTenor(
								alternativeCashSettlementReferenceRateIndexTenor.getValue());
					}
				}
			}
			if (!strike.getText().isEmpty()) {
				trade.setStrike(TradistaGUIUtil.parseAmount(strike.getText(), "Strike"));
			}

			// Building the underlying
			if (trade.getUnderlying() == null) {
				trade.setUnderlying(new SingleCurrencyIRSwapTrade());
				trade.getUnderlying().setCreationDate(LocalDate.now());
			}

			if (!notionalAmount.getText().isEmpty()) {
				trade.getUnderlying()
						.setAmount(TradistaGUIUtil.parseAmount(notionalAmount.getText(), "Notional Amount"));
			}
			trade.getUnderlying().setCurrency(currency.getValue());
			trade.getUnderlying().setPaymentFrequency(paymentFrequency.getValue());
			trade.getUnderlying().setReceptionFrequency(receptionFrequency.getValue());
			trade.getUnderlying().setInterestsToPayFixed(interestsToPayFixed.isSelected());
			trade.getUnderlying().setMaturityDate(underlyingMaturityDate.getValue());
			trade.getUnderlying().setMaturityTenor(underlyingMaturityTenor.getValue());
			trade.getUnderlying().setPaymentDayCountConvention(paymentDayCountConvention.getValue());
			if (trade.getUnderlying().isInterestsToPayFixed()) {
				if (!fixedInterestRate.getText().isEmpty()) {
					trade.getUnderlying().setPaymentFixedInterestRate(
							TradistaGUIUtil.parseAmount(fixedInterestRate.getText(), "Payment Fixed Interest Rate"));
				}
			} else {
				trade.getUnderlying().setPaymentReferenceRateIndex(paymentReferenceRateIndex.getValue());
				trade.getUnderlying().setPaymentReferenceRateIndexTenor(paymentReferenceRateIndexTenor.getValue());
				if (!paymentSpread.getText().isEmpty()) {
					trade.getUnderlying()
							.setPaymentSpread(TradistaGUIUtil.parseAmount(paymentSpread.getText(), "Payment Spread"));
				}
			}
			trade.getUnderlying().setReceptionDayCountConvention(receptionDayCountConvention.getValue());
			trade.getUnderlying().setReceptionReferenceRateIndex(referenceRateIndex.getValue());
			trade.getUnderlying().setReceptionReferenceRateIndexTenor(referenceRateIndexTenor.getValue());
			if (!receptionSpread.getText().isEmpty()) {
				trade.getUnderlying()
						.setReceptionSpread(TradistaGUIUtil.parseAmount(receptionSpread.getText(), "Reception Spread"));
			}
			trade.getUnderlying().setBook(book.getValue());
			trade.getUnderlying().setBuySell((trade.isCall() && trade.isBuy()) || (trade.isPut() && trade.isSell()));
			trade.getUnderlying().setCounterparty(trade.getCounterparty());

			if (trade.getExerciseDate() != null) {
				if (trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
					trade.getUnderlying().setTradeDate(trade.getExerciseDate());
					short offSet = 0;
					if (!settlementDateOffset.getText().isEmpty()) {
						offSet = Short.parseShort(settlementDateOffset.getText());
					}
					LocalDate settlementDate = trade.getExerciseDate().plusDays(offSet);
					while (!new IRSwapTradeBusinessDelegate().isBusinessDay(trade.getUnderlying(), settlementDate)) {
						settlementDate = settlementDate.plusDays(1);
					}
					trade.getUnderlying().setSettlementDate(settlementDate);
				} else {
					trade.getUnderlying().setTradeDate(null);
					trade.getUnderlying().setSettlementDate(null);
				}
			} else {
				trade.getUnderlying().setTradeDate(null);
				trade.getUnderlying().setSettlementDate(null);
			}

			trade.getUnderlying().setPaymentInterestPayment(paymentInterestPayment.getValue());
			trade.getUnderlying().setReceptionInterestPayment(receptionInterestPayment.getValue());

			trade.getUnderlying().setPaymentInterestFixing(paymentInterestFixing.getValue());
			trade.getUnderlying().setReceptionInterestFixing(receptionInterestFixing.getValue());

		} catch (TradistaBusinessException tbe) {
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
		TradistaGUIUtil.fillCurrencyComboBox(currency, premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex);
		TradistaGUIUtil.fillIndexComboBox(true, alternativeCashSettlementReferenceRateIndex);
	}

	@Override
	public void update(TradistaPublisher publisher) {
		super.update(publisher);
		if (!publisher.isError()) {
			if (publisher instanceof MarketDataPublisher) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Set<QuoteValue> quoteValues = ((MarketDataPublisher) publisher).getQuoteValues();
						if (quoteValues != null && !quoteValues.isEmpty()) {
							for (QuoteValue qv : quoteValues) {
								if (qv.getQuoteSet().equals(selectedQuoteSet.getValue())) {
									if (qv.getQuote().getName()
											.equals(IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue() + "."
													+ referenceRateIndexTenor.getValue() + "%")
											|| qv.getQuote().getName()
													.equals(IRSwapTrade.IR_SWAP + "."
															+ paymentReferenceRateIndex.getValue() + "."
															+ paymentReferenceRateIndexTenor.getValue() + "%")) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (IRSwapOptionTradeDefinitionController.this.quoteValues
														.contains(qv)) {
													IRSwapOptionTradeDefinitionController.this.quoteValues.remove(qv);
												}
												IRSwapOptionTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(QuoteProperty
								.toQuotePropertyList(IRSwapOptionTradeDefinitionController.this.quoteValues)));
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
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(notionalAmount.getText(), "Notional Amount");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		if (interestsToPayFixed.isSelected()) {
			try {
				TradistaGUIUtil.checkAmount(fixedInterestRate.getText(), "Payment Fixed Interest Rate");
			} catch (TradistaBusinessException abe) {
				errMsg.append(abe.getMessage());
			}
		}
		if (settlementType.getValue().equals(SettlementType.CASH)) {
			try {
				TradistaGUIUtil.checkAmount(cashSettlementAmount.getText(), "Cash Settlement");
			} catch (TradistaBusinessException abe) {
				errMsg.append(abe.getMessage());
			}
		}
		try {
			if (!settlementDateOffset.getText().isEmpty()) {
				Integer.parseInt(settlementDateOffset.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(
					String.format("The settlement date offset is incorrect: %s.%n", settlementDateOffset.getText()));
		}
		try {
			TradistaGUIUtil.checkAmount(strike.getText(), "Strike");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		if (!interestsToPayFixed.isSelected()) {
			try {
				TradistaGUIUtil.checkAmount(paymentSpread.getText(), "Payment Spread");
			} catch (TradistaBusinessException abe) {
				errMsg.append(abe.getMessage());
			}
		}
		try {
			TradistaGUIUtil.checkAmount(receptionSpread.getText(), "Reception Spread");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}