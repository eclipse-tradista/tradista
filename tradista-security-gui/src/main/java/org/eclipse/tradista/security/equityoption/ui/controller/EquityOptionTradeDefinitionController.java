package org.eclipse.tradista.security.equityoption.ui.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
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
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equityoption.model.BlankEquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.service.EquityOptionTradeBusinessDelegate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

public class EquityOptionTradeDefinitionController extends TradistaTradeBookingController {

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
	private TextField underlyingPrice;

	@FXML
	private TextField underlyingQuantity, quantity;

	@FXML
	private Label quantityLabel;

	@FXML
	private ComboBox<VanillaOptionTrade.Style> style;

	@FXML
	private ComboBox<Equity> equity;

	@FXML
	private ComboBox<Currency> premiumCurrency;

	@FXML
	private ComboBox<Book> book;

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
	private Label tradeType;

	@FXML
	private ComboBox<EquityOption> equityOption;

	@FXML
	private Label pricerQuoteSetLabel;

	@FXML
	private Label premiumWarning;

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

	private EquityBusinessDelegate equityBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private EquityOptionTradeBusinessDelegate equityOptionTradeBusinessDelegate;

	@FXML
	private TextField load;

	@FXML
	private Label tradeId;

	private EquityOptionTrade trade;

	@FXML
	private TradistaBookPieChart bookChartPane;

	private ChangeListener<LocalDate> tradeDateListener;

	private ChangeListener<EquityOption> equityOptionListener;

	private ChangeListener<Trade.Direction> directionListener;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		quoteValues = Collections.synchronizedSet(HashSet.newHashSet(2));
		tradeType.setText("Equity Option Trade");

		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		equityBusinessDelegate = new EquityBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		equityOptionTradeBusinessDelegate = new EquityOptionTradeBusinessDelegate();

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

		selectedQuoteSet.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends QuoteSet> observableValue, QuoteSet oldValue,
					QuoteSet newValue) {
				if (newValue != null && selectedQuoteDate.getValue() != null) {
					Equity eq = equity.getValue();
					EquityOption eqo = equityOption.getValue();
					String equityQuoteName = null;
					String equityOptionQuoteName = null;
					if (eq != null) {
						equityQuoteName = Equity.EQUITY + "." + eq.getIsin() + "." + eq.getExchange();
					}
					if (eqo != null && !eqo.equals(BlankEquityOption.getInstance())) {
						equityOptionQuoteName = EquityOption.EQUITY_OPTION + "."
								+ eqo.getEquityOptionContractSpecification().getName() + "." + eqo.getCode() + "."
								+ eqo.getType() + "." + eqo.getMaturityDate() + ".%";
					}
					fillQuotesTable(newValue, selectedQuoteDate.getValue(), equityQuoteName, equityOptionQuoteName);
				}
			}
		});

		selectedQuoteDate.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldValue,
					LocalDate newValue) {
				Equity eq = equity.getValue();
				EquityOption eqo = equityOption.getValue();
				String equityQuoteName = null;
				String equityOptionQuoteName = null;
				if (eq != null) {
					equityQuoteName = Equity.EQUITY + "." + eq.getIsin() + "." + eq.getExchange();
				}
				if (eqo != null && !eqo.equals(BlankEquityOption.getInstance())) {
					equityOptionQuoteName = EquityOption.EQUITY_OPTION + "."
							+ eqo.getEquityOptionContractSpecification().getName() + "." + eqo.getCode() + "."
							+ eqo.getType() + "." + eqo.getMaturityDate() + ".%";
				}

				fillQuotesTable(selectedQuoteSet.getValue(), newValue, equityQuoteName, equityOptionQuoteName);
			}

		});

		equity.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Equity> observableValue, Equity oldValue, Equity newValue) {
				LocalDate quoteDate = selectedQuoteDate.getValue();
				if (quoteDate != null) {
					EquityOption eqo = equityOption.getValue();
					String equityQuoteName = null;
					String equityOptionQuoteName = null;
					if (newValue != null) {
						equityQuoteName = Equity.EQUITY + "." + newValue.getIsin() + "." + newValue.getExchange();
					}
					if (eqo != null && !eqo.equals(BlankEquityOption.getInstance())) {
						equityOptionQuoteName = EquityOption.EQUITY_OPTION + "."
								+ eqo.getEquityOptionContractSpecification().getName() + "." + eqo.getCode() + "."
								+ eqo.getType() + "." + eqo.getMaturityDate() + ".%";
					}

					fillQuotesTable(selectedQuoteSet.getValue(), quoteDate, equityQuoteName, equityOptionQuoteName);
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
						pricer = pricerBusinessDelegate.getPricer(EquityOption.EQUITY_OPTION, newPricingParam);
					} catch (TradistaBusinessException tbe) {
						// Will never happen in this case.
					}
					TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
					pricerLabel.setText(pricer.getClass().getAnnotation(Parameterizable.class).name());
					pricerQuoteSetLabel.setText(newPricingParam.getQuoteSet().getName());
				}
			}
		});

		pricingDate.setValue(LocalDate.now());

		book.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Book> arg0, Book oldValue, Book newValue) {
				if (newValue != null) {
					bookChartPane.updateBookChart(newValue);
				}
			}
		});

		maturityDate.setOnMouseClicked(new EventHandler<>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (equity.getValue() == null) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
							"The equity must be selected before selecting a maturity date.");
					alert.showAndWait();
				}
			}
		});

		final Callback<DatePicker, DateCell> businessDayCellFactory = new Callback<>() {
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {

					EquityOptionTrade equityOptionTrade;

					private boolean isAvailable(LocalDate date) {
						if (equity.getValue() == null) {
							return false;
						}
						if (equityOptionTrade == null) {
							equityOptionTrade = new EquityOptionTrade();
							EquityTrade equityTrade = new EquityTrade();
							equityTrade.setProduct(equity.getValue());
							equityOptionTrade.setUnderlying(equityTrade);
							if (!equityOption.getValue().equals(BlankEquityOption.getInstance())) {
								equityOptionTrade.setEquityOption(equityOption.getValue());
							}
						}

						try {
							return equityOptionTradeBusinessDelegate.isBusinessDay(equityOptionTrade, date);
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

		tradeDateListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate newDate) {
				if (newDate != null && equityOption.getValue() != null && selectedQuoteSet.getValue() != null) {

					if (!newDate.isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getPremium(newDate, buySell.getValue(), equityOption.getValue(),
											selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(premiumWarning);
											TradistaGUIUtil.applyWarningStyle(premiumWarning);
											premiumWarning.setVisible(true);
											premiumWarning.setText(String.format(
													"Equity option %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													equityOption.getValue(), newDate, selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											premium.setText(TradistaGUIUtil.formatAmount(price));
											premiumWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(premiumWarning);
										TradistaGUIUtil.unapplyWarningStyle(premiumWarning);
										premiumWarning.setText(String.format(
												"Cannot get equity option %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												equityOption.getValue(), newDate, selectedQuoteSet.getValue()));
										premiumWarning.setVisible(true);
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
				if (tradeDate.getValue() != null && equityOption.getValue() != null
						&& selectedQuoteSet.getValue() != null) {

					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getPremium(tradeDate.getValue(), newDirection,
											equityOption.getValue(), selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(premiumWarning);
											TradistaGUIUtil.applyWarningStyle(premiumWarning);
											premiumWarning.setVisible(true);
											premiumWarning.setText(String.format(
													"Equity option %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													equityOption.getValue(), tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											premium.setText(TradistaGUIUtil.formatAmount(price));
											premiumWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException tte) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(premiumWarning);
										TradistaGUIUtil.unapplyWarningStyle(premiumWarning);
										premiumWarning.setText(String.format(
												"Cannot get equity option %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												equityOption.getValue(), tradeDate.getValue(),
												selectedQuoteSet.getValue()));
										premiumWarning.setVisible(true);
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

		equityOptionListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends EquityOption> arg0, EquityOption arg1,
					EquityOption newEquityOption) {
				if (tradeDate.getValue() != null && newEquityOption != null && selectedQuoteSet.getValue() != null) {

					if (!tradeDate.getValue().isAfter(LocalDate.now())) {
						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() {
								try {
									BigDecimal price = getPremium(tradeDate.getValue(), buySell.getValue(),
											newEquityOption, selectedQuoteSet.getValue().getId());
									isQuoteSetServiceError = false;
									if (price == null) {
										Platform.runLater(() -> {
											TradistaGUIUtil.unapplyErrorStyle(premiumWarning);
											TradistaGUIUtil.applyWarningStyle(premiumWarning);
											premiumWarning.setVisible(true);
											premiumWarning.setText(String.format(
													"Equity option %s price is not available as of Trade Date (%tD) in QuoteSet %s.",
													newEquityOption, tradeDate.getValue(),
													selectedQuoteSet.getValue()));
										});
									} else {
										Platform.runLater(() -> {
											premium.setText(TradistaGUIUtil.formatAmount(price));
											premiumWarning.setVisible(false);
										});
									}
								} catch (TradistaTechnicalException _) {
									isQuoteSetServiceError = true;
									Platform.runLater(() -> {
										TradistaGUIUtil.applyErrorStyle(premiumWarning);
										TradistaGUIUtil.unapplyWarningStyle(premiumWarning);
										premiumWarning.setText(String.format(
												"Cannot get bond %s price as of Trade Date (%tD) in QuoteSet %s, please contact support.",
												newEquityOption, tradeDate.getValue(), selectedQuoteSet.getValue()));
										premiumWarning.setVisible(true);
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

		if (equityOption.getValue() != null) {
			tradeDate.setDayCellFactory(businessDayCellFactory);
		}
		maturityDate.setDayCellFactory(businessDayCellFactory);
		exerciseDate.setDayCellFactory(businessDayCellFactory);
		settlementDate.setDayCellFactory(businessDayCellFactory);
		selectedQuoteDate.setDayCellFactory(businessDayCellFactory);

		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
		TradistaSecurityGUIUtil.fillEquityOptionComboBox(true, equityOption);
		equityOption.getSelectionModel().selectFirst();
		TradistaGUIUtil.fillOptionStyleComboBox(style);
		TradistaGUIUtil.fillOptionTypeComboBox(callPut);
		TradistaGUIUtil.fillOptionSettlementTypeComboBox(settlementType);
		TradistaGUIUtil.fillTradeDirectionComboBox(buySell);

		equityOption.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends EquityOption> observableValue, EquityOption oldValue,
					EquityOption newValue) {
				if (newValue != null && !newValue.equals(BlankEquityOption.getInstance())) {
					style.setValue(newValue.getStyle());
					callPut.setValue(newValue.getType());
					equity.setValue(newValue.getUnderlying());
					underlyingPrice.setText(TradistaGUIUtil.formatAmount(newValue.getStrike()));
					underlyingQuantity.setText(TradistaGUIUtil.formatAmount(newValue.getQuantity()));
					settlementDateOffset.setText(Integer.toString(newValue.getSettlementDateOffset()));
					settlementType.setValue(newValue.getSettlementType().equals(OptionTrade.SettlementType.CASH)
							? OptionTrade.SettlementType.CASH
							: OptionTrade.SettlementType.PHYSICAL);
					maturityDate.setValue(newValue.getMaturityDate());
					premiumCurrency.setValue(newValue.getPremiumCurrency());

					style.setDisable(true);
					callPut.setDisable(true);
					equity.setDisable(true);
					settlementType.setDisable(true);
					style.setStyle("-fx-opacity: 1");
					callPut.setStyle("-fx-opacity: 1");
					equity.setStyle("-fx-opacity: 1");
					settlementType.setStyle("-fx-opacity: 1");
					maturityDate.setStyle("-fx-opacity: 1");
					premiumCurrency.setStyle("-fx-opacity: 1");
					underlyingQuantity.setEditable(false);
					settlementDateOffset.setEditable(false);
					underlyingPrice.setEditable(false);
					maturityDate.setDisable(true);
					premiumCurrency.setDisable(true);
					quantity.setVisible(true);
					quantityLabel.setVisible(true);

					LocalDate quoteDate = selectedQuoteDate.getValue();
					if (quoteDate != null && newValue != null) {
						String equityOptionQuoteName = EquityOption.EQUITY_OPTION + "."
								+ newValue.getEquityOptionContractSpecification().getName() + "." + newValue.getCode()
								+ "." + newValue.getType() + "." + newValue.getMaturityDate() + ".%";

						String equityQuoteName = null;
						if (equity.getValue() != null) {
							equityQuoteName = Equity.EQUITY + "." + equity.getValue().getIsin();
						}
						fillQuotesTable(selectedQuoteSet.getValue(), quoteDate, equityOptionQuoteName, equityQuoteName);
					}
				} else {
					style.setDisable(false);
					callPut.setDisable(false);
					equity.setDisable(false);
					settlementType.setDisable(false);
					underlyingQuantity.setEditable(true);
					underlyingQuantity.clear();
					underlyingPrice.setEditable(true);
					underlyingPrice.clear();
					settlementDateOffset.setEditable(true);
					settlementDateOffset.clear();
					maturityDate.setDisable(false);
					premiumCurrency.setDisable(false);
					quantity.setVisible(false);
					quantityLabel.setVisible(false);
				}

			}
		});
	}

	private void addListeners() {
		tradeDate.valueProperty().addListener(tradeDateListener);
		buySell.valueProperty().addListener(directionListener);
		equityOption.valueProperty().addListener(equityOptionListener);
	}

	private BigDecimal getPremium(LocalDate tradeDate, Trade.Direction direction, EquityOption equityOption,
			long quoteSetId) {
		List<QuoteValue> qvs = quoteBusinessDelegate.getQuoteValuesByQuoteSetIdQuoteNameAndDate(quoteSetId,
				EquityOption.EQUITY_OPTION + "." + equityOption.getEquityOptionContractSpecification().getName() + "."
						+ equityOption.getCode() + "." + equityOption.getType() + "." + equityOption.getMaturityDate()
						+ ".%",
				tradeDate);
		BigDecimal equityPrice = null;
		QuoteValue quoteValue = null;
		if (qvs == null || qvs.isEmpty()) {
			return null;
		}
		for (QuoteValue qv : qvs) {
			String[] quoteNameArray = qv.getQuote().getName().split("\\.");
			String strikeString = quoteNameArray[5];
			BigDecimal strike = null;
			DecimalFormat df = new DecimalFormat();
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator(',');
			df.setDecimalFormatSymbols(dfs);
			try {
				strike = new BigDecimal(df.parse(strikeString).toString());
			} catch (ParseException pe) {
				// Should not happen here, as strikeString is already a well formated decimal
				// with comma as decimal separator
			}
			if (strike.compareTo(equityOption.getStrike()) == 0) {
				quoteValue = qv;
				break;
			}
		}
		if (quoteValue == null) {
			return null;
		}
		if (quoteValue.getClose() != null) {
			equityPrice = quoteValue.getClose();
		}
		if (direction.equals(Trade.Direction.BUY)) {
			if (quoteValue.getAsk() != null) {
				equityPrice = quoteValue.getAsk();
			}
		} else {
			if (quoteValue.getBid() != null) {
				equityPrice = quoteValue.getBid();
			}
		}
		if (equityPrice == null) {
			equityPrice = quoteValue.getLast();
		}
		if (equityPrice != null) {
			return equityPrice.multiply(equityOption.getMultiplier());
		}
		return null;
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

				trade.setId(equityOptionTradeBusinessDelegate.saveEquityOptionTrade(trade));
				EquityOptionTrade existingTrade = equityOptionTradeBusinessDelegate
						.getEquityOptionTradeById(trade.getId());
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
		Optional<ButtonType> result = confirmation.showAndWait();
		long oldTradeId = 0;
		long oldUnderlyingTradeId = 0;
		LocalDate oldCreationDate = null;
		LocalDate oldUnderlyingCreationDate = null;
		EquityTrade underlying = null;
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
				trade.setId(equityOptionTradeBusinessDelegate.saveEquityOptionTrade(trade));
				EquityOptionTrade existingTrade = equityOptionTradeBusinessDelegate
						.getEquityOptionTradeById(trade.getId());
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

	private void buildTrade() {
		if (this.trade == null) {
			trade = new EquityOptionTrade();
		}
		try {
			trade.setTradeDate(tradeDate.getValue());
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
			if (!equityOption.getValue().equals(BlankEquityOption.getInstance())) {
				trade.setEquityOption(equityOption.getValue());
				if (!quantity.getText().isEmpty()) {
					trade.setQuantity(TradistaGUIUtil.parseAmount(quantity.getText(), "Quantity"));
				}
			} else {
				trade.setEquityOption(null);
			}
			if (!underlyingPrice.getText().isEmpty()) {
				trade.setStrike(TradistaGUIUtil.parseAmount(underlyingPrice.getText(), "Underlying Price"));
			}
			trade.setMaturityDate(maturityDate.getValue());
			trade.setExerciseDate(exerciseDate.getValue());

			trade.setSettlementType(settlementType.getValue());
			if (!settlementDateOffset.getText().isEmpty()) {
				trade.setSettlementDateOffset(Integer.parseInt(settlementDateOffset.getText()));
			}

			// Building the underlying
			if (trade.getUnderlying() == null) {
				trade.setUnderlying(new EquityTrade());
			}

			trade.getUnderlying().setProduct(equity.getValue());
			if (!underlyingQuantity.getText().isEmpty()) {
				trade.getUnderlying()
						.setQuantity(TradistaGUIUtil.parseAmount(underlyingQuantity.getText(), "Underlying Quantity"));
			}
			if (!underlyingPrice.getText().isEmpty()) {
				trade.getUnderlying()
						.setAmount(TradistaGUIUtil.parseAmount(underlyingPrice.getText(), "Underlying Price"));
			}
			if (trade.getExerciseDate() != null) {
				if (trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
					short offSet = 0;
					if (!settlementDateOffset.getText().isEmpty()) {
						offSet = Short.parseShort(settlementDateOffset.getText());
					}
					trade.getUnderlying().setTradeDate(trade.getExerciseDate());
					trade.getUnderlying().setSettlementDate(DateUtil.addBusinessDay(trade.getExerciseDate(),
							equity.getValue().getExchange().getCalendar(), offSet));
				} else {
					trade.getUnderlying().setSettlementDate(null);
					trade.getUnderlying().setTradeDate(null);
				}
			} else {
				trade.getUnderlying().setSettlementDate(null);
				trade.getUnderlying().setTradeDate(null);
			}
			trade.getUnderlying().setBuySell((trade.isCall() && trade.isBuy()) || (trade.isPut() && trade.isSell()));
			trade.getUnderlying().setCounterparty(counterparty.getValue());
			trade.getUnderlying().setBook(book.getValue());

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

	@FXML
	protected void load() {
		EquityOptionTrade equityOptionTrade;
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

			equityOptionTrade = equityOptionTradeBusinessDelegate.getEquityOptionTradeById(tradeId);
			if (equityOptionTrade == null) {
				throw new TradistaBusinessException(
						String.format("The %s trade %s was not found.", EquityOption.EQUITY_OPTION, load.getText()));
			}
			load(equityOptionTrade);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(EquityOptionTrade trade) {
		this.trade = trade;
		tradeId.setText(String.valueOf(trade.getId()));
		tradeDate.setValue(trade.getTradeDate());
		settlementDate.setValue(trade.getSettlementDate());
		buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
		counterparty.setValue(trade.getCounterparty());
		book.setValue(trade.getBook());
		if (trade.getProduct() != null) {
			equityOption.setValue(trade.getEquityOption());
		} else {
			equityOption.setValue(BlankEquityOption.getInstance());
		}
		underlyingQuantity.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getQuantity()));
		premium.setText(TradistaGUIUtil.formatAmount(trade.getAmount()));
		premiumCurrency.setValue(trade.getCurrency());
		callPut.setValue(trade.getType());
		style.setValue(trade.getStyle());
		settlementType.setValue(trade.getSettlementType());
		settlementDateOffset.setText(Integer.toString(trade.getSettlementDateOffset()));
		equity.setValue(trade.getUnderlying().getProduct());
		maturityDate.setValue(trade.getMaturityDate());
		exerciseDate.setValue(trade.getExerciseDate());
		underlyingPrice.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getAmount()));
		underlyingQuantity.setText(TradistaGUIUtil.formatAmount(trade.getUnderlying().getAmount()));
		BigDecimal quantity = trade.getQuantity();
		if (quantity != null) {
			this.quantity.setText(TradistaGUIUtil.formatAmount(quantity));
		}
	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		String sDateOffset = settlementDateOffset.getText();
		String uPrice = underlyingPrice.getText();
		String uQty = underlyingQuantity.getText();
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		TradistaGUIUtil.fillCurrencyComboBox(premiumCurrency, pricingCurrency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), counterparty);
		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillComboBox(equityBusinessDelegate.getAllEquities(), equity);
		TradistaSecurityGUIUtil.fillEquityOptionComboBox(true, equityOption);
		settlementDateOffset.setText(sDateOffset);
		underlyingPrice.setText(uPrice);
		underlyingQuantity.setText(uQty);
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
											.equals(EquityOption.EQUITY_OPTION + "."
													+ equityOption.getValue().getEquityOptionContractSpecification()
															.getName()
													+ "." + equityOption.getValue().getCode() + "."
													+ equityOption.getValue().getType() + "."
													+ equityOption.getValue().getMaturityDate() + "."
													+ equityOption.getValue().getStrike())
											|| qv.getQuote().getName()
													.equals(Equity.EQUITY + "." + equity.getValue().getIsin())) {
										if (qv.getDate().equals(selectedQuoteDate.getValue())) {
											if (qv.getQuote().getType().equals(QuoteType.EXCHANGE_RATE)) {
												if (EquityOptionTradeDefinitionController.this.quoteValues
														.contains(qv)) {
													EquityOptionTradeDefinitionController.this.quoteValues.remove(qv);
												}
												EquityOptionTradeDefinitionController.this.quoteValues.add(qv);
											}
										}
									}
								}
							}
						}
						quotesTable.setItems(FXCollections.observableArrayList(QuoteProperty
								.toQuotePropertyList(EquityOptionTradeDefinitionController.this.quoteValues)));
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
			TradistaGUIUtil.checkAmount(underlyingPrice.getText(), "Underlying Price");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(underlyingQuantity.getText(), "Underlying Quantity");
		} catch (TradistaBusinessException tbe) {
			errMsg.append(tbe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(quantity.getText(), "Quantity");
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
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	@FXML
	public void clear() {
		trade = null;
		tradeId.setText("");
		premium.clear();
		settlementDateOffset.clear();
		underlyingQuantity.clear();
		maturityDate.setValue(null);
		settlementDate.setValue(null);
		underlyingPrice.clear();
		quantity.clear();
	}

}