package org.eclipse.tradista.ir.ccyswap.ui.controller;

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
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.ccyswap.service.CcySwapPricerBusinessDelegate;
import org.eclipse.tradista.ir.ccyswap.service.CcySwapTradeBusinessDelegate;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.application.Platform;
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

public class CcySwapTradeDefinitionController extends TradistaTradeBookingController {

	private static final String PRICING_PARAMETERS_SET_DOES_NOT_CONTAIN_A_DISCOUNT_CURVE_FOR_CURRENCY = "Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.";

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
	private CheckBox interestsToPayFixed;

	@FXML
	private TextField fixedInterestRate;

	@FXML
	private DatePicker tradeDate;

	@FXML
	private DatePicker settlementDate;

	@FXML
	private TextField notionalAmountOne;

	@FXML
	private ComboBox<Currency> currencyOne;

	@FXML
	private ComboBox<Book> book;

	@FXML
	private TextField notionalAmountTwo;

	@FXML
	private ComboBox<Currency> currencyTwo;

	@FXML
	private ComboBox<LegalEntity> counterparty;

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
	private Label paymentSpreadLabel;

	@FXML
	private Label paymentReferenceRateIndexTenorLabel;

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

	private CcySwapTradeBusinessDelegate ccySwapTradeBusinessDelegate;

	private CcySwapPricerBusinessDelegate ccySwapPricerBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private CcySwapTrade trade;

	@FXML
	private Label tradeType;

	@FXML
	private Label cfPricingDate;

	@FXML
	private Label cfReceptionLegDiscountCurve;

	@FXML
	private Label cfPaymentLegDiscountCurve;

	@FXML
	private Button generate;

	@FXML
	private TradistaBookPieChart bookChartPane;

	public void initialize() {

		super.initialize();
		quoteValues = Collections.synchronizedSet(new HashSet<QuoteValue>(4));
		tradeType.setText("Cross-Currencies Swap trade");

		selectedQuoteSet.valueProperty().addListener((q, ov, nv) -> {
			if (nv != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(nv, selectedQuoteDate.getValue(), irSwapRate, irSwapReferenceRate,
						irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		ccySwapTradeBusinessDelegate = new CcySwapTradeBusinessDelegate();
		ccySwapPricerBusinessDelegate = new CcySwapPricerBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);
		TradistaGUIUtil.fillTenorComboBox(paymentFrequency, receptionFrequency, maturityTenor);
		TradistaGUIUtil.fillTenorComboBox(false, paymentReferenceRateIndexTenor, referenceRateIndexTenor);

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

		selectedQuoteDate.valueProperty().addListener((ld, ov, nv) -> {
			if (nv != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
						irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		referenceRateIndex.getSelectionModel().selectedItemProperty().addListener((i, ov, nv) -> {
			if (selectedQuoteDate.getValue() != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (nv != null) {
					receptionInterestFixing.setValue(
							nv.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD : InterestPayment.END_OF_PERIOD);
				}
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
						irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		paymentReferenceRateIndex.getSelectionModel().selectedItemProperty().addListener((i, ov, nv) -> {
			if (selectedQuoteDate.getValue() != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (nv != null) {
					paymentInterestFixing.setValue(
							nv.isPrefixed() ? InterestPayment.BEGINNING_OF_PERIOD : InterestPayment.END_OF_PERIOD);
				}
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
						irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		referenceRateIndexTenor.getSelectionModel().selectedItemProperty().addListener((t, ov, nv) -> {
			if (selectedQuoteDate.getValue() != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
						irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		paymentReferenceRateIndexTenor.getSelectionModel().selectedItemProperty().addListener((t, ov, nv) -> {
			if (selectedQuoteDate.getValue() != null) {
				String irSwapRate = null;
				String irSwapReferenceRate = null;
				String irSwapPaymentReferenceRate = null;
				String currencyOneCurrencyTwoExchangeRate = null;
				String currencyTwoCurrencyOneExchangeRate = null;
				if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
					irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue().toString() + "%";
					irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
							+ referenceRateIndexTenor.getValue() + "%";
				}
				if (paymentReferenceRateIndex.getValue() != null && paymentReferenceRateIndexTenor.getValue() != null) {
					irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
							+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
				}
				if (currencyOne.getValue() != null && currencyTwo.getValue() != null) {
					currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
							+ currencyTwo.getValue().getIsoCode() + "%";
					currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
							+ currencyOne.getValue().getIsoCode() + "%";
				}
				fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
						irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
						currencyTwoCurrencyOneExchangeRate);
			}
		});

		currencyOne.getSelectionModel().selectedItemProperty().addListener((c, ov, nv) -> {
			if (nv != null) {
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					String irSwapPaymentReferenceRate = null;
					String currencyOneCurrencyTwoExchangeRate = null;
					String currencyTwoCurrencyOneExchangeRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue().toString() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (paymentReferenceRateIndex.getValue() != null
							&& paymentReferenceRateIndexTenor.getValue() != null) {
						irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
								+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
					}
					if (currencyTwo.getValue() != null) {
						currencyOneCurrencyTwoExchangeRate = "FX." + nv.getIsoCode() + "."
								+ currencyTwo.getValue().getIsoCode() + "%";
						currencyTwoCurrencyOneExchangeRate = "FX." + currencyTwo.getValue().getIsoCode() + "."
								+ nv.getIsoCode() + "%";
					}

					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(nv);
						if (discountCurve != null) {
							cfReceptionLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfReceptionLegDiscountCurve);
						} else {
							cfReceptionLegDiscountCurve.setText(
									String.format(PRICING_PARAMETERS_SET_DOES_NOT_CONTAIN_A_DISCOUNT_CURVE_FOR_CURRENCY,
											pricingParameter.getValue().getName(), nv));
							TradistaGUIUtil.applyWarningStyle(cfReceptionLegDiscountCurve);
						}
					}

					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
							irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
							currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		currencyTwo.getSelectionModel().selectedItemProperty().addListener((c, ov, nv) -> {
			if (nv != null) {
				if (selectedQuoteDate.getValue() != null) {
					String irSwapRate = null;
					String irSwapReferenceRate = null;
					String irSwapPaymentReferenceRate = null;
					String currencyOneCurrencyTwoExchangeRate = null;
					String currencyTwoCurrencyOneExchangeRate = null;
					if (referenceRateIndex.getValue() != null && referenceRateIndexTenor.getValue() != null) {
						irSwapRate = IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue().toString() + "%";
						irSwapReferenceRate = Index.INDEX + "." + referenceRateIndex.getValue().getName() + "."
								+ referenceRateIndexTenor.getValue() + "%";
					}
					if (paymentReferenceRateIndex.getValue() != null
							&& paymentReferenceRateIndexTenor.getValue() != null) {
						irSwapPaymentReferenceRate = Index.INDEX + "." + paymentReferenceRateIndex.getValue().getName()
								+ "." + paymentReferenceRateIndexTenor.getValue().toString() + "%";
					}
					if (currencyOne.getValue() != null) {
						currencyOneCurrencyTwoExchangeRate = "FX." + currencyOne.getValue().getIsoCode() + "."
								+ nv.getIsoCode() + "%";
						currencyTwoCurrencyOneExchangeRate = "FX." + nv.getIsoCode() + "."
								+ currencyOne.getValue().getIsoCode() + "%";
					}

					if (pricingParameter.getValue() != null) {
						InterestRateCurve discountCurve = pricingParameter.getValue().getDiscountCurve(nv);
						if (discountCurve != null) {
							cfPaymentLegDiscountCurve.setText(discountCurve.getName());
							TradistaGUIUtil.unapplyWarningStyle(cfPaymentLegDiscountCurve);
						} else {
							cfPaymentLegDiscountCurve.setText(
									String.format(PRICING_PARAMETERS_SET_DOES_NOT_CONTAIN_A_DISCOUNT_CURVE_FOR_CURRENCY,
											pricingParameter.getValue().getName(), nv));
							TradistaGUIUtil.applyWarningStyle(cfPaymentLegDiscountCurve);
						}
					}

					fillQuotesTable(selectedQuoteSet.getValue(), selectedQuoteDate.getValue(), irSwapRate,
							irSwapReferenceRate, irSwapPaymentReferenceRate, currencyOneCurrencyTwoExchangeRate,
							currencyTwoCurrencyOneExchangeRate);
				}
			}
		});

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener((pm, ov, nv) -> {
			// nv is null when we do "setItems" in
			// the first call of the refresh method
			if (nv != null) {
				TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingMethods(nv), pricingMethod);
			}
		});

		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);

		pricingParameter.getSelectionModel().selectedItemProperty().addListener((pp, ov, nv) -> {
			// nv is null when we do "setItems" in
			// the first call of the refresh method
			if (nv != null) {
				Pricer pricer = null;
				try {
					pricer = pricerBusinessDelegate.getPricer(CcySwapTrade.CCY_SWAP, nv);
				} catch (TradistaBusinessException abe) {
					// Will never happen in this case.
				}
				TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
				pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
				pricerQuoteSetLabel.setText(nv.getQuoteSet().getName());

				if (currencyOne.getValue() != null) {
					InterestRateCurve discountCurve = pricingParameter.getValue()
							.getDiscountCurve(currencyOne.getValue());
					if (discountCurve != null) {
						cfReceptionLegDiscountCurve.setText(discountCurve.getName());
						TradistaGUIUtil.unapplyWarningStyle(cfReceptionLegDiscountCurve);
					} else {
						cfReceptionLegDiscountCurve.setText(
								String.format(PRICING_PARAMETERS_SET_DOES_NOT_CONTAIN_A_DISCOUNT_CURVE_FOR_CURRENCY,
										pricingParameter.getValue().getName(), currencyOne.getValue()));
						TradistaGUIUtil.applyWarningStyle(cfReceptionLegDiscountCurve);
					}
				}

				if (currencyTwo.getValue() != null) {
					InterestRateCurve discountCurve = pricingParameter.getValue()
							.getDiscountCurve(currencyTwo.getValue());
					if (discountCurve != null) {
						cfPaymentLegDiscountCurve.setText(discountCurve.getName());
						TradistaGUIUtil.unapplyWarningStyle(cfPaymentLegDiscountCurve);
					} else {
						cfPaymentLegDiscountCurve.setText(
								String.format(PRICING_PARAMETERS_SET_DOES_NOT_CONTAIN_A_DISCOUNT_CURVE_FOR_CURRENCY,
										pricingParameter.getValue().getName(), currencyTwo.getValue()));
						TradistaGUIUtil.applyWarningStyle(cfPaymentLegDiscountCurve);
					}
				}
			}
		});

		pricingDate.setOnAction(
				ae -> cfPricingDate.setText(pricingDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

		pricingDate.setValue(LocalDate.now());
		cfPricingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		book.getSelectionModel().selectedItemProperty().addListener((b, ov, nv) -> {
			if (nv != null) {
				bookChartPane.updateBookChart(nv);
			}
		});

		interestsToPayFixed.selectedProperty().addListener((b, ov, nv) -> {
			paymentFixedInterestRateLabel.setVisible(nv);
			fixedInterestRate.setVisible(nv);
			paymentReferenceRateIndexLabel.setVisible(!nv);
			paymentReferenceRateIndexTenorLabel.setVisible(!nv);
			paymentSpreadLabel.setVisible(!nv);
			paymentSpread.setVisible(!nv);
			paymentReferenceRateIndex.setVisible(!nv);
			paymentReferenceRateIndexTenor.setVisible(!nv);
			paymentInterestFixing.setVisible(!nv);
			paymentInterestFixingLabel.setVisible(!nv);
		});

		maturityTenor.valueProperty().addListener((t, ov, nv) -> {
			if (nv != null) {
				boolean tenorIsSpecified = (!nv.equals(Tenor.NO_TENOR));
				maturityDate.setDisable(tenorIsSpecified);
				if (tenorIsSpecified) {
					if (settlementDate.getValue() != null) {
						try {
							maturityDate.setValue(DateUtil.addTenor(settlementDate.getValue().minusDays(1), nv));
						} catch (TradistaBusinessException tbe) {
							// Should not appear here.
						}
					} else {
						maturityDate.setValue(null);
					}
					maturityDate.setStyle("-fx-opacity: 1");
				}
			}
		});

		settlementDate.valueProperty().addListener((ld, ov, nv) -> {
			if (nv != null) {
				boolean tenorIsSpecified = (!maturityTenor.getValue().equals(Tenor.NO_TENOR));
				if (tenorIsSpecified) {
					try {
						maturityDate.setValue(DateUtil.addTenor(nv.minusDays(1), maturityTenor.getValue()));
					} catch (TradistaBusinessException tbe) {
						// Should not appear here.
					}
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = dp -> new DateCell() {

			CcySwapTrade irSwapTrade;

			private boolean isAvailable(LocalDate date) {
				if (irSwapTrade == null) {
					irSwapTrade = new CcySwapTrade();
					irSwapTrade.setCurrency(currencyOne.getValue());
					irSwapTrade.setCurrencyTwo(currencyTwo.getValue());
				}
				try {
					return ccySwapTradeBusinessDelegate.isBusinessDay(irSwapTrade, date);
				} catch (TradistaBusinessException tbe) {
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

		settlementDate.setDayCellFactory(businessDayCellFactory);
		maturityDate.setDayCellFactory(businessDayCellFactory);
		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

		TradistaGUIUtil.fillComboBox(pricerBusinessDelegate.getAllPricingParameters(), pricingParameter);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillDayCountConventionComboBox(paymentDayCountConvention, receptionDayCountConvention);
		TradistaGUIUtil.fillInterestPaymentComboBox(paymentInterestPayment, receptionInterestPayment,
				paymentInterestFixing, receptionInterestFixing);
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex);
		paymentInterestPayment.setValue(InterestPayment.END_OF_PERIOD);
		receptionInterestPayment.setValue(InterestPayment.END_OF_PERIOD);
	}

	@FXML
	protected void load() {
		CcySwapTrade ccySwapTrade;
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

			ccySwapTrade = ccySwapTradeBusinessDelegate.getCcySwapTradeById(tradeId);
			if (ccySwapTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", CcySwapTrade.CCY_SWAP, load.getText()));
			}
			load(ccySwapTrade);
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	private void load(CcySwapTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		settlementDate.setValue(trade.getSettlementDate());
		currencyOne.setValue(trade.getCurrency());
		currencyTwo.setValue(trade.getCurrencyTwo());
		if (trade.getPaymentFixedInterestRate() != null) {
			fixedInterestRate.setText(TradistaGUIUtil.formatAmount(trade.getPaymentFixedInterestRate()));
		}
		paymentFrequency.setValue(trade.getPaymentFrequency());
		receptionFrequency.setValue(trade.getReceptionFrequency());
		interestsToPayFixed.setSelected(trade.isInterestsToPayFixed());
		maturityDate.setValue(trade.getMaturityDate());
		notionalAmountOne.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		notionalAmountTwo.setText(TradistaGUIUtil.formatAmount(trade.getNotionalAmountTwo()));
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
		paymentInterestPayment.setValue(trade.getPaymentInterestPayment());
		receptionInterestPayment.setValue(trade.getReceptionInterestPayment());
		paymentInterestFixing.setValue(trade.getPaymentInterestFixing());
		receptionInterestFixing.setValue(trade.getReceptionInterestFixing());
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

				trade.setId(ccySwapTradeBusinessDelegate.saveCcySwapTrade(trade));
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
				trade.setId(ccySwapTradeBusinessDelegate.saveCcySwapTrade(trade));
				tradeId.setText(String.valueOf(trade.getId()));
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				trade.setId(oldTradeId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void generate() {
		try {
			checkAmounts();

			buildTrade();

			List<CashFlow> cashFlows = ccySwapPricerBusinessDelegate.generateCashFlows(trade,
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

	private void buildTrade() {
		if (this.trade == null) {
			trade = new CcySwapTrade();
			trade.setCreationDate(LocalDate.now());
		}
		try {
			if (!notionalAmountOne.getText().isEmpty()) {
				trade.setAmount(TradistaGUIUtil.parseAmount(notionalAmountOne.getText(), "Notional Amount One"));
			}
			if (!notionalAmountTwo.getText().isEmpty()) {
				trade.setNotionalAmountTwo(
						TradistaGUIUtil.parseAmount(notionalAmountTwo.getText(), "Notional Amount Two"));
			}
			trade.setCurrency(currencyOne.getValue());
			trade.setPaymentFrequency(paymentFrequency.getValue());
			trade.setReceptionFrequency(receptionFrequency.getValue());
			trade.setInterestsToPayFixed(interestsToPayFixed.isSelected());
			trade.setMaturityDate(maturityDate.getValue());
			trade.setMaturityTenor(maturityTenor.getValue());
			trade.setSettlementDate(settlementDate.getValue());
			trade.setPaymentDayCountConvention(paymentDayCountConvention.getValue());
			trade.setCurrencyTwo(currencyTwo.getValue());
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
				trade.setPaymentInterestFixing(paymentInterestFixing.getValue());
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
			trade.setReceptionInterestFixing(receptionInterestFixing.getValue());
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
		TradistaGUIUtil.fillCurrencyComboBox(currencyOne, currencyTwo, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillComboBox(bookBusinessDelegate.getAllBooks(), book);
		TradistaGUIUtil.fillIndexComboBox(paymentReferenceRateIndex, referenceRateIndex);
	}

	@Override
	public void update(TradistaPublisher publisher) {
		super.update(publisher);
		if (publisher instanceof MarketDataPublisher marketDataPublisher) {
			if (!publisher.isError()) {
				Platform.runLater(() -> {
					Set<QuoteValue> quoteValues = marketDataPublisher.getQuoteValues();
					if (quoteValues != null && !quoteValues.isEmpty()) {
						for (QuoteValue qv : quoteValues) {
							if (qv.getQuoteSet().equals(selectedQuoteSet.getValue())) {
								if (qv.getQuote().getName()
										.equals("FX." + currencyOne.getValue() + "." + currencyTwo.getValue())
										|| qv.getQuote().getName()
												.equals("FX." + currencyTwo.getValue() + "." + currencyOne.getValue())
										|| qv.getQuote().getName()
												.equals(IRSwapTrade.IR_SWAP + "." + paymentReferenceRateIndex.getValue()
														+ "." + paymentReferenceRateIndexTenor.getValue() + "%")
										|| qv.getQuote().getName()
												.equals(IRSwapTrade.IR_SWAP + "." + referenceRateIndex.getValue() + "."
														+ referenceRateIndexTenor.getValue() + "%")) {
									if (qv.getDate().equals(selectedQuoteDate.getValue())) {
										if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
											if (CcySwapTradeDefinitionController.this.quoteValues.contains(qv)) {
												CcySwapTradeDefinitionController.this.quoteValues.remove(qv);
											}
											CcySwapTradeDefinitionController.this.quoteValues.add(qv);
										}
									}
								}
							}
						}
					}
					quotesTable.setItems(FXCollections.observableArrayList(
							QuoteProperty.toQuotePropertyList(CcySwapTradeDefinitionController.this.quoteValues)));
				});
			}
		}
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(notionalAmountOne.getText(), "Notional Amount One");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(notionalAmountTwo.getText(), "Notional Amount Two");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		if (interestsToPayFixed.isSelected()) {
			try {
				TradistaGUIUtil.checkAmount(fixedInterestRate.getText(), "Payment Fixed Interest Rate");
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		}
		if (!interestsToPayFixed.isSelected()) {
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

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		settlementDate.setValue(null);
		maturityDate.setValue(null);
		notionalAmountOne.clear();
		fixedInterestRate.clear();
		notionalAmountTwo.clear();
		paymentSpread.clear();
		receptionSpread.clear();
	}
}