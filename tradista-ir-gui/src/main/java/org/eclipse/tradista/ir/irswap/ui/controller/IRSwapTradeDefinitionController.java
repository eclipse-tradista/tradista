package org.eclipse.tradista.ir.irswap.ui.controller;

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
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaTradeBookingController;
import org.eclipse.tradista.core.common.ui.publisher.TradistaPublisher;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
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
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerBusinessDelegate;
import org.eclipse.tradista.ir.irswap.service.IRSwapTradeBusinessDelegate;
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
import javafx.scene.control.CheckBox;
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

public class IRSwapTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private DatePicker maturityDate;

	@FXML
	private ComboBox<Tenor> maturityTenor;

	@FXML
	private ComboBox<Tenor> paymentFrequency;

	@FXML
	private ComboBox<Tenor> receptionFrequency;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private ComboBox<Tenor> referenceRateIndexTenor;

	@FXML
	private TextField receptionSpread;

	@FXML
	private ComboBox<DayCountConvention> paymentDayCountConvention;

	@FXML
	private ComboBox<DayCountConvention> receptionDayCountConvention;

	@FXML
	private ComboBox<InterestPayment> paymentInterestPayment;

	@FXML
	private ComboBox<InterestPayment> receptionInterestPayment;

	@FXML
	private ComboBox<InterestPayment> paymentInterestFixing;

	@FXML
	private Label paymentInterestFixingLabel;

	@FXML
	private ComboBox<InterestPayment> receptionInterestFixing;

	@FXML
	private CheckBox interestsToPayFixed;

	@FXML
	private TextField fixedInterestRate;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private TextField notionalAmount;

	@FXML
	private ComboBox<Currency> currency;

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
	private Label paymentReferenceRateIndexLabel;

	@FXML
	private Label paymentReferenceRateIndexTenorLabel;

	@FXML
	private Label paymentSpreadLabel;

	@FXML
	private Label paymentFixedInterestRateLabel;

	@FXML
	private ComboBox<Index> paymentReferenceRateIndex;

	@FXML
	private ComboBox<Tenor> paymentReferenceRateIndexTenor;

	@FXML
	private TextField paymentSpread;

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

	private IRSwapTradeBusinessDelegate irSwapTradeBusinessDelegate;

	private IRSwapPricerBusinessDelegate irSwapPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private SingleCurrencyIRSwapTrade trade;

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

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();

		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(2));
		tradeType.setText("IR Swap trade");

		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		irSwapTradeBusinessDelegate = new IRSwapTradeBusinessDelegate();
		irSwapPricerBusinessDelegate = new IRSwapPricerBusinessDelegate();
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
							irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue() + "."
									+ paymentReferenceRateIndexTenor.getValue() + "%";
						}
						fillQuotesTable(newValue, selectedQuoteDate.getValue(), irSwapRate, irSwapReferenceRate,
								irSwapPaymentReferenceRate);
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
							irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue() + "."
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
			public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
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
									+ paymentReferenceRateIndex.getValue().getName() + "+"
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
						if (newIndex != null && paymentReferenceRateIndexTenor.getValue() != null) {
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
								if (paymentReferenceRateIndex.getValue() != null && newTenor != null) {
									irSwapPaymentReferenceRate = Index.INDEX + "."
											+ paymentReferenceRateIndex.getValue().getName() + "." + newTenor + "%";
								}
							}
							fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
									irSwapReferenceRate, irSwapPaymentReferenceRate);
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

		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);

		pricingParameter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricingParameter>() {
			@Override
			public void changed(ObservableValue<? extends PricingParameter> observableValue,
					PricingParameter oldPricingParam, PricingParameter newPricingParam) {
				// newPricingParam is null when we do "setItems" in
				// the first call of the refresh method
				if (newPricingParam != null) {
					Pricer pricer = null;
					try {
						pricer = pricerBusinessDelegate.getPricer(IRSwapTrade.IR_SWAP, newPricingParam);
					} catch (TradistaBusinessException tbe) {
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

		currency.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Currency>() {
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

		interestsToPayFixed.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				paymentFixedInterestRateLabel.setVisible(newValue);
				fixedInterestRate.setVisible(newValue);
				paymentReferenceRateIndexLabel.setVisible(!newValue);
				paymentReferenceRateIndex.setVisible(!newValue);
				paymentReferenceRateIndexTenor.setVisible(!newValue);
				paymentReferenceRateIndexTenorLabel.setVisible(!newValue);
				paymentSpread.setVisible(!newValue);
				paymentSpreadLabel.setVisible(!newValue);
				paymentInterestFixing.setVisible(!newValue);
				paymentInterestFixingLabel.setVisible(!newValue);
			}
		});

		maturityTenor.valueProperty().addListener(new ChangeListener<Tenor>() {
			public void changed(ObservableValue<? extends Tenor> ov, Tenor oldValue, Tenor newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!newValue.equals(Tenor.NO_TENOR));
					maturityDate.setDisable(tenorIsSpecified);
					if (tenorIsSpecified) {
						if (settlementDate.getValue() != null) {
							try {
								maturityDate
										.setValue(DateUtil.addTenor(settlementDate.getValue().minusDays(1), newValue));
							} catch (TradistaBusinessException tbe) {
								// Should not appear here.
							}
						} else {
							maturityDate.setValue(null);
						}
					}
				}
			}
		});

		settlementDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!maturityTenor.getValue().equals(Tenor.NO_TENOR));
					if (tenorIsSpecified) {
						try {
							maturityDate.setValue(DateUtil.addTenor(newValue.minusDays(1), maturityTenor.getValue()));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					}
				}
			}
		});

		referenceRateIndex.valueProperty().addListener(new ChangeListener<Index>() {
			public void changed(ObservableValue<? extends Index> ov, Index oldValue, Index newValue) {
				if (newValue != null) {
					receptionInterestFixing.setValue(newValue.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
							: InterestPayment.END_OF_PERIOD);
				}
			}
		});

		paymentReferenceRateIndex.valueProperty().addListener(new ChangeListener<Index>() {
			public void changed(ObservableValue<? extends Index> ov, Index oldValue, Index newValue) {
				if (newValue != null) {
					paymentInterestFixing.setValue(newValue.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
							: InterestPayment.END_OF_PERIOD);
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					SingleCurrencyIRSwapTrade irSwapTrade;

					private boolean isAvailable(LocalDate date) {
						if (irSwapTrade == null) {
							irSwapTrade = new SingleCurrencyIRSwapTrade();
							irSwapTrade.setCurrency(currency.getValue());
						}

						try {
							return irSwapTradeBusinessDelegate.isBusinessDay(irSwapTrade, date);
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

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(paymentDayCountConvention, receptionDayCountConvention);
		TradistaGUIUtil.fillInterestPaymentComboBox(paymentInterestPayment, receptionInterestPayment,
				paymentInterestFixing, receptionInterestFixing);
		paymentInterestPayment.setValue(InterestPayment.END_OF_PERIOD);
		receptionInterestPayment.setValue(InterestPayment.END_OF_PERIOD);
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex);
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
		TradistaGUIUtil.fillTenorComboBox(paymentFrequency, receptionFrequency, maturityTenor);
		TradistaGUIUtil.fillTenorComboBox(false, paymentReferenceRateIndexTenor, referenceRateIndexTenor);
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

				trade.setId(irSwapTradeBusinessDelegate.saveIRSwapTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
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
				trade.setId(irSwapTradeBusinessDelegate.saveIRSwapTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				trade.setId(oldTradeId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		SingleCurrencyIRSwapTrade irSwapTrade;
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

			irSwapTrade = irSwapTradeBusinessDelegate.getIRSwapTradeById(tradeId);
			if (irSwapTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", IRSwapTrade.IR_SWAP, load.getText()));
			}
			load(irSwapTrade);
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	private void load(SingleCurrencyIRSwapTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		maturityDate.setValue(trade.getMaturityDate());
		if (trade.getMaturityTenor() != null) {
			maturityTenor.setValue(trade.getMaturityTenor());
		}
		currency.setValue(trade.getCurrency());
		if (trade.getPaymentFixedInterestRate() != null) {
			fixedInterestRate.setText(TradistaGUIUtil.formatAmount(trade.getPaymentFixedInterestRate()));
		}
		paymentFrequency.setValue(trade.getPaymentFrequency());
		receptionFrequency.setValue(trade.getReceptionFrequency());
		interestsToPayFixed.setSelected(trade.isInterestsToPayFixed());
		notionalAmount.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		paymentDayCountConvention.setValue(trade.getPaymentDayCountConvention());
		paymentReferenceRateIndex.setValue(trade.getPaymentReferenceRateIndex());
		paymentReferenceRateIndexTenor.setValue(trade.getPaymentReferenceRateIndexTenor());
		if (trade.getPaymentSpread() != null) {
			paymentSpread.setText(TradistaGUIUtil.formatAmount(trade.getPaymentSpread()));
		}
		receptionDayCountConvention.setValue(trade.getReceptionDayCountConvention());
		referenceRateIndex.setValue(trade.getReceptionReferenceRateIndex());
		referenceRateIndexTenor.setValue(trade.getReceptionReferenceRateIndexTenor());
		if (trade.getReceptionSpread() != null) {
			receptionSpread.setText(TradistaGUIUtil.formatAmount(trade.getReceptionSpread()));
		}
		settlementDate.setValue(trade.getSettlementDate());
		paymentInterestPayment.setValue(trade.getPaymentInterestPayment());
		receptionInterestPayment.setValue(trade.getReceptionInterestPayment());
		paymentInterestFixing.setValue(trade.getPaymentInterestFixing());
		receptionInterestFixing.setValue(trade.getReceptionInterestFixing());
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = irSwapPricerBusinessDelegate.generateCashFlows(trade,
					pricingParameter.getValue(), pricingDate.getValue());
			if (cashFlows != null) {
				cashFlowsTable.setItems(
						FXCollections.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cashFlows)));
				generate.setText("Refresh");
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
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
		maturityDate.setValue(null);
		fixedInterestRate.clear();
		paymentSpread.clear();
		receptionSpread.clear();
	}

	private void buildTrade() {
		if (this.trade == null) {
			trade = new SingleCurrencyIRSwapTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			if (!notionalAmount.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(notionalAmount.getText(), "Notional Amount"));
			}
			trade.setCurrency(currency.getValue());
			trade.setPaymentFrequency(paymentFrequency.getValue());
			trade.setReceptionFrequency(receptionFrequency.getValue());
			trade.setInterestsToPayFixed(interestsToPayFixed.isSelected());
			trade.setMaturityDate(maturityDate.getValue());
			trade.setMaturityTenor(maturityTenor.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setPaymentDayCountConvention(paymentDayCountConvention.getValue());
			if (trade.isInterestsToPayFixed()) {
				if (!fixedInterestRate.getText().isEmpty()) {
					trade.setPaymentFixedInterestRate(
							TradistaGUIUtil.parseAmount(fixedInterestRate.getText(), "Payment Fixed Interest Rate"));
				}
			} else {
				trade.setPaymentReferenceRateIndex(paymentReferenceRateIndex.getValue());
				trade.setPaymentReferenceRateIndexTenor(paymentReferenceRateIndexTenor.getValue());
				if (!paymentSpread.getText().isEmpty()) {
					trade.setPaymentSpread(TradistaGUIUtil.parseAmount(paymentSpread.getText(), "Payment Spread"));
				}
			}
			trade.setReceptionDayCountConvention(receptionDayCountConvention.getValue());
			trade.setReceptionReferenceRateIndex(referenceRateIndex.getValue());
			trade.setReceptionReferenceRateIndexTenor(referenceRateIndexTenor.getValue());
			if (!receptionSpread.getText().isEmpty()) {
				trade.setReceptionSpread(TradistaGUIUtil.parseAmount(receptionSpread.getText(), "Reception Spread"));
			}
			trade.setBook(book.getValue());
			trade.setBuySell(buySell.getValue().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setCreationDate(LocalDate.now());
			trade.setTradeDate(tradeDate.getValue());
			trade.setPaymentInterestPayment(paymentInterestPayment.getValue());
			trade.setReceptionInterestPayment(receptionInterestPayment.getValue());
			trade.setPaymentInterestFixing(paymentInterestFixing.getValue());
			trade.setReceptionInterestFixing(receptionInterestFixing.getValue());
		} catch (TradistaBusinessException tbe) {
			// Should not happen at this stage
		}
	}

	@FXML
	protected void price() {
		try {
			checkAmounts();

			buildTrade();

			result.setText(
					TradistaGUIUtil.formatAmount(pricerBusinessDelegate
							.calculate(trade, pricingParameter.getValue(), pricingCurrency.getValue(),
									pricingDate.getValue(), pricingMeasure.getValue(), pricingMethod.getValue())
							.doubleValue()));

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
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
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex);
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
											.equals(IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue() + "."
													+ referenceRateIndexTenor.getValue() + "%")
											|| qv.getQuote().getName()
													.equals(IRSwapTrade.IR_SWAP + "."
															+ paymentReferenceRateIndex.getValue() + "."
															+ paymentReferenceRateIndexTenor.getValue() + "%")) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (IRSwapTradeDefinitionController.this.quoteValues.contains(qv)) {
													IRSwapTradeDefinitionController.this.quoteValues.remove(qv);
												}
												IRSwapTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(
								QuoteProperty.toQuotePropertyList(IRSwapTradeDefinitionController.this.quoteValues)));
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

		if (interestsToPayFixed.isSelected()) {
			try {
				TradistaGUIUtil.checkAmount(fixedInterestRate.getText(), "Payment Fixed Interest Rate");
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		} else {
			try {
				TradistaGUIUtil.checkAmount(paymentSpread.getText(), "Payment Spread");
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
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