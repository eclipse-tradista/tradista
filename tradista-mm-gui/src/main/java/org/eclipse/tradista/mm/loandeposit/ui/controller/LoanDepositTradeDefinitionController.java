package org.eclipse.tradista.mm.loandeposit.ui.controller;

import java.time.LocalDate;
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
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.mm.common.ui.util.TradistaMMGUIUtil;
import org.eclipse.tradista.mm.loandeposit.model.DepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.Direction;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositPricerBusinessDelegate;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositTradeBusinessDelegate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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

public class LoanDepositTradeDefinitionController extends TradistaTradeBookingController {

	@FXML
	private ComboBox<Trade.Direction> buySell;

	@FXML
	private ComboBox<LoanDepositTrade.Direction> direction;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private ComboBox<Index> floatingRateIndex;

	@FXML
	private ComboBox<Tenor> floatingRateIndexTenor;

	@FXML
	private ComboBox<DayCountConvention> dayCountConvention;

	@FXML
	private ComboBox<Tenor> paymentFrequency;

	@FXML
	private ComboBox<InterestPayment> interestPayment;

	@FXML
	private ComboBox<InterestPayment> interestFixing;

	@FXML
	private Label interestFixingLabel;

	@FXML
	private CheckBox interestRateIsFixed;

	@FXML
	private DatePicker startDate;

	@FXML
	private DatePicker endDate;

	@FXML
	private ComboBox<Tenor> maturity;

	@FXML
	private TextField fixedRate;

	@FXML
	private TextField principal;

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
	private Label fixedRateLabel;

	@FXML
	private Label floatingRateIndexLabel;

	@FXML
	private Label floatingRateIndexTenorLabel;

	@FXML
	private Label fixingPeriodLabel;

	@FXML
	private ComboBox<Tenor> fixingPeriod;

	@FXML
	private Label spreadLabel;

	@FXML
	private TextField spread;

	@FXML
	private ComboBox<LoanDepositTrade.InterestType> interestType;

	@FXML
	private Label compoundPeriodLabel;

	@FXML
	private ComboBox<Tenor> compoundPeriod;

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

	private PricerBusinessDelegate pricerBusinessDelegate;

	private LoanDepositTradeBusinessDelegate loanDepositTradeBusinessDelegate;

	private LoanDepositPricerBusinessDelegate loanDepositPricerBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private LoanDepositTrade trade;

	@FXML
	private Label tradeType;

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

	@FXML
	private TradistaBookPieChart bookChartPane;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();

		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(2));
		tradeType.setText("Loan/Deposit Trade");

		pricerBusinessDelegate = new PricerBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		loanDepositTradeBusinessDelegate = new LoanDepositTradeBusinessDelegate();
		loanDepositPricerBusinessDelegate = new LoanDepositPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();

		tradeDate.setValue(LocalDate.now());

		interestRateIsFixed.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				fixedRateLabel.setVisible(newValue);
				fixedRate.setVisible(newValue);
				floatingRateIndexLabel.setVisible(!newValue);
				floatingRateIndexTenorLabel.setVisible(!newValue);
				floatingRateIndex.setVisible(!newValue);
				floatingRateIndexTenor.setVisible(!newValue);
				spreadLabel.setVisible(!newValue);
				spread.setVisible(!newValue);
				fixingPeriodLabel.setVisible(!newValue);
				fixingPeriod.setVisible(!newValue);
				interestFixing.setVisible(!newValue);
				interestFixingLabel.setVisible(!newValue);
			}
		});

		maturity.valueProperty().addListener(new ChangeListener<Tenor>() {
			public void changed(ObservableValue<? extends Tenor> ov, Tenor oldValue, Tenor newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!newValue.equals(Tenor.NO_TENOR));
					endDate.setDisable(tenorIsSpecified);
					if (tenorIsSpecified) {
						if (startDate.getValue() != null) {
							try {
								endDate.setValue(DateUtil.addTenor(startDate.getValue().minusDays(1), newValue));
							} catch (TradistaBusinessException tbe) {
								// Should not appear here.
							}
						} else {
							endDate.setValue(null);
						}
					}
				}
			}
		});

		startDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			public void changed(ObservableValue<? extends LocalDate> ov, LocalDate oldValue, LocalDate newValue) {
				if (newValue != null) {
					boolean tenorIsSpecified = (!maturity.getValue().equals(Tenor.NO_TENOR));
					if (tenorIsSpecified) {
						try {
							endDate.setValue(DateUtil.addTenor(newValue.minusDays(1), maturity.getValue()));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					}
				}
			}
		});

		interestType.valueProperty().addListener(new ChangeListener<InterestType>() {
			public void changed(ObservableValue<? extends InterestType> ov, InterestType oldValue,
					InterestType newValue) {
				if (newValue != null) {
					compoundPeriodLabel.setVisible(newValue.equals(InterestType.COMPOUND));
					compoundPeriod.setVisible(newValue.equals(InterestType.COMPOUND));
				}
			}
		});

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
				if (newValue != null && selectedQuoteDate.getValue() != null && floatingRateIndex.getValue() != null
						&& floatingRateIndexTenor.getValue() != null) {
					String index = Index.INDEX + "." + floatingRateIndex.getValue().getName() + "."
							+ floatingRateIndexTenor.getValue();
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), index);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				fillQuotesTable(selectedQuoteSet.getValue(), newValue, Index.INDEX + "."
						+ floatingRateIndex.getValue().getName() + "." + floatingRateIndexTenor.getValue());
			}
		});

		floatingRateIndex.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Index>() {
			@Override
			public void changed(ObservableValue<? extends Index> observableValue, Index oldIndex, Index newIndex) {
				if (newIndex != null) {
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
							Index.INDEX + "." + newIndex.getName() + "." + floatingRateIndexTenor.getValue());
				}
			}
		});

		floatingRateIndexTenor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tenor>() {
			@Override
			public void changed(ObservableValue<? extends Tenor> observableValue, Tenor oldTenor, Tenor newTenor) {
				if (floatingRateIndex.getValue() != null) {
					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(),
							Index.INDEX + "." + floatingRateIndex.getValue().getName() + "." + newTenor);
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
						pricer = pricerBusinessDelegate.getPricer(LoanDepositTrade.LOAN_DEPOSIT, newPricingParam);
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

		pricingDate.setValue(LocalDate.now());

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<DatePicker, DateCell>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					LoanDepositTrade mmTrade;

					private boolean isAvailable(LocalDate date) {
						if (mmTrade == null) {
							if (direction.getValue().equals(Direction.LOAN)) {
								mmTrade = new LoanTrade();
							} else {
								mmTrade = new DepositTrade();
							}
							mmTrade.setCurrency(currency.getValue());
						}

						try {
							return loanDepositTradeBusinessDelegate.isBusinessDay(mmTrade, date);
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

		floatingRateIndex.valueProperty().addListener(new ChangeListener<Index>() {
			public void changed(ObservableValue<? extends Index> ov, Index oldValue, Index newValue) {
				if (newValue != null) {
					interestFixing.setValue(newValue.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD
							: InterestPayment.END_OF_PERIOD);
				}
			}
		});

		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(dayCountConvention);
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
		TradistaMMGUIUtil.fillLoanDepositTypeComboBox(direction);
		TradistaGUIUtil.fillInterestPaymentComboBox(interestPayment, interestFixing);
		TradistaGUIUtil.fillIndexComboBox(floatingRateIndex);
		TradistaMMGUIUtil.fillLoanDepositInterestTypeComboBox(interestType);
		TradistaGUIUtil.fillTenorComboBox(paymentFrequency, floatingRateIndexTenor, fixingPeriod, compoundPeriod,
				maturity);

		interestPayment.setValue(InterestPayment.END_OF_PERIOD);
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

				trade.setId(loanDepositTradeBusinessDelegate.saveLoanDepositTrade(trade));
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
				trade.setId(loanDepositTradeBusinessDelegate.saveLoanDepositTrade(trade));
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
		LoanDepositTrade loanDepositTrade;
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

			loanDepositTrade = loanDepositTradeBusinessDelegate.getLoanDepositTradeById(tradeId);
			if (loanDepositTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", LoanDepositTrade.LOAN_DEPOSIT, load.getText()));
			}
			load(loanDepositTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(LoanDepositTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		currency.setValue(trade.getCurrency());
		dayCountConvention.setValue(trade.getDayCountConvention());
		direction.setValue(trade instanceof DepositTrade ? Direction.DEPOSIT : Direction.LOAN);
		endDate.setValue(trade.getEndDate());
		if (trade.isFixed()) {
			fixedRate.setText(TradistaGUIUtil.formatAmount(trade.getFixedRate()));
		}
		if (trade.getMaturity() != null) {
			maturity.setValue(trade.getMaturity());
		}
		fixingPeriod.setValue(trade.getFixingPeriod());
		floatingRateIndex.setValue(trade.getFloatingRateIndex());
		floatingRateIndexTenor.setValue(trade.getFloatingRateIndexTenor());
		interestRateIsFixed.setSelected(trade.isFixed());
		paymentFrequency.setValue(trade.getPaymentFrequency());
		principal.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		spread.setText(trade.getSpread() == null ? "" : TradistaGUIUtil.formatAmount(trade.getSpread()));
		startDate.setValue(trade.getSettlementDate());
		interestType.setValue(trade.getInterestType());
		compoundPeriod.setValue(trade.getCompoundPeriod());
		interestPayment.setValue(trade.getInterestPayment());
		if (trade.getInterestFixing() != null) {
			interestPayment.setValue(trade.getInterestFixing());
		}
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = loanDepositPricerBusinessDelegate.generateCashFlows(trade,
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
		startDate.setValue(null);
		endDate.setValue(null);
		principal.clear();
		fixedRate.clear();
		spread.clear();
	}

	private LoanDepositTrade buildTrade() {
		if (this.trade == null) {
			if (direction.getValue().equals(LoanDepositTrade.Direction.LOAN)) {
				trade = new LoanTrade();
			} else {
				trade = new DepositTrade();
			}
			trade.setCreationDate(LocalDate.now());
		}
		try {
			trade.setCreationDate(LocalDate.now());
			if (!principal.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(principal.getText(), "Principal"));
			}
			trade.setTradeDate(tradeDate.getValue());
			trade.setBuySell(buySell.getSelectionModel().getSelectedItem().equals(Trade.Direction.BUY));
			trade.setCounterparty(counterparty.getValue());
			trade.setBook(book.getValue());
			trade.setCurrency(currency.getValue());
			trade.setSettlementDate(startDate.getValue());
			trade.setEndDate(endDate.getValue());
			trade.setMaturity(maturity.getValue());
			trade.setPaymentFrequency(paymentFrequency.getValue());
			trade.setDayCountConvention(dayCountConvention.getValue());
			if (interestRateIsFixed.isSelected()) {
				if (!fixedRate.getText().isEmpty()) {
					trade.setFixedRate(TradistaGUIUtil.parseAmount(fixedRate.getText(), "Fixed Rate"));
				}
				trade.setFloatingRateIndex(null);
				trade.setFloatingRateIndexTenor(null);
				trade.setFixingPeriod(null);
				trade.setSpread(null);
				trade.setInterestFixing(null);
			} else {
				trade.setFloatingRateIndex(floatingRateIndex.getValue());
				trade.setFloatingRateIndexTenor(floatingRateIndexTenor.getValue());
				trade.setFixingPeriod(fixingPeriod.getValue());
				if (!spread.getText().isEmpty()) {
					trade.setSpread(TradistaGUIUtil.parseAmount(spread.getText(), "Spread"));
				}
				trade.setInterestFixing(interestFixing.getValue());
				trade.setFixedRate(null);
			}
			trade.setInterestType(interestType.getValue());
			if (trade.getInterestType().equals(InterestType.COMPOUND)) {
				trade.setCompoundPeriod(compoundPeriod.getValue());
			} else {
				trade.setCompoundPeriod(null);
			}
			trade.setInterestPayment(interestPayment.getValue());

		} catch (TradistaBusinessException tbe) {
			// Should not appear here.
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
		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillIndexComboBox(floatingRateIndex);
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
											.equals(Index.INDEX + "." + floatingRateIndex.getValue().getName() + "."
													+ floatingRateIndexTenor.getValue())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (LoanDepositTradeDefinitionController.this.quoteValues
														.contains(qv)) {
													LoanDepositTradeDefinitionController.this.quoteValues.remove(qv);
												}
												LoanDepositTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(QuoteProperty
								.toQuotePropertyList(LoanDepositTradeDefinitionController.this.quoteValues)));
					}
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(principal.getText(), "Principal");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}

		if (interestRateIsFixed.isSelected()) {
			try {
				TradistaGUIUtil.checkAmount(fixedRate.getText(), "Premium");
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		} else {
			try {
				TradistaGUIUtil.checkAmount(spread.getText(), "Spread");
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}