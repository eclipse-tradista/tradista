package org.eclipse.tradista.commercial.commercial.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.commercial.commercial.model.PricerCommercial;
import org.eclipse.tradista.commercial.commercial.service.CommercialPricerBusinessDelegate;
import org.eclipse.tradista.commercial.commercial.service.CommercialTradeBusinessDelegate;
import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.ui.controller.TradistaBookPieChart;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.cashflow.ui.controller.CashFlowProperty;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaEditingCell;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.ui.controller.QuoteProperty;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.core.workflow.model.Workflow;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class CommercialTradeDefinitionController extends TradistaControllerAdapter {

	@FXML
	private TradistaBookPieChart bookChartPane;
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
	private DatePicker quoteDate;
	@FXML
	private ComboBox<QuoteSet> quoteSet;
	@FXML
	private CheckBox realTime;
	@FXML
	private Label cfPricingDate;
	@FXML
	private Label cfDiscountCurve;
	@FXML
	private Label pricerResultLabel;
	@FXML
	private TableView<QuoteProperty> quotesTable;
	@FXML
	private TableColumn<QuoteProperty, String> quoteName;
	@FXML
	private TableColumn<QuoteProperty, String> quoteDateCol;
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
	private TableView<CashFlowProperty> generatedCashFlowsTable;
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
	private TextField loadTradeId;
	@FXML
	private Label tradeIdLabel;
	@FXML
	private ComboBox<Book> book;
	@FXML
	private ComboBox<LegalEntity> counterparty;
	@FXML
	private ComboBox<Currency> currency;
	@FXML
	private ComboBox<Trade.Direction> buySell;
	@FXML
	private TextField documentReference;
	@FXML
	private DatePicker tradeDate;
	@FXML
	private Label statusTitleLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private Label applyActionLabel;

	@FXML
	private ComboBox<String> actionComboBox;

	@FXML
	private TableView<CommercialCashFlowRow> cashFlowsTable;
	@FXML
	private TableColumn<CommercialCashFlowRow, LocalDate> settlementDateCol;
	@FXML
	private TableColumn<CommercialCashFlowRow, String> amountCol;
	@FXML
	private TableColumn<CommercialCashFlowRow, Transfer.Status> transferStatusCol;

	@FXML
	private Button addRowButton;
	@FXML
	private Button removeRowButton;
	@FXML
	private Button generateCashFlowsButton;

	private CommercialTradeBusinessDelegate commercialTradeBusinessDelegate;
	private CommercialPricerBusinessDelegate commercialPricerBusinessDelegate;
	private TransferBusinessDelegate transferBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;
	private CommercialTrade trade;
	private ObservableList<CommercialCashFlowRow> cashFlowRows = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		commercialTradeBusinessDelegate = new CommercialTradeBusinessDelegate();
		commercialPricerBusinessDelegate = new CommercialPricerBusinessDelegate();
		transferBusinessDelegate = new TransferBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
		updateStatusAndActions(null);

		cfDate.setCellValueFactory(cellData -> cellData.getValue().getDate());
		cfPurpose.setCellValueFactory(cellData -> cellData.getValue().getPurpose());
		cfDirection.setCellValueFactory(cellData -> cellData.getValue().getDirection());
		cfAmount.setCellValueFactory(cellData -> cellData.getValue().getAmount());
		cfCurrency.setCellValueFactory(cellData -> cellData.getValue().getCurrency());
		cfDiscountedAmount.setCellValueFactory(cellData -> cellData.getValue().getDiscountedAmount());
		cfDiscountFactor.setCellValueFactory(cellData -> cellData.getValue().getDiscountFactor());

		quoteName.setCellValueFactory(cellData -> cellData.getValue().getName());
		quoteDateCol.setCellValueFactory(cellData -> cellData.getValue().getDate());
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

		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillCounterpartyComboBox(counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);
		TradistaGUIUtil.fillQuoteSetComboBox(quoteSet);
		TradistaGUIUtil.fillPricingParameterComboBox(pricingParameter);
		updateDiscountCurveLabel();
		pricingParameter.valueProperty().addListener((_, _, _) -> updateDiscountCurveLabel());
		currency.valueProperty().addListener((_, _, _) -> {
			updateDiscountCurveLabel();
			fillFxQuotesTable();
		});
		quoteSet.valueProperty().addListener((_, _, _) -> fillFxQuotesTable());
		quoteDate.valueProperty().addListener((_, _, _) -> fillFxQuotesTable());

		TradistaGUIUtil.fillComboBox(new PricerCommercial().getPricerMeasures(), pricingMeasure);
		pricingMeasure.getSelectionModel().selectFirst();
		if (pricingMeasure.getValue() != null) {
			TradistaGUIUtil.fillComboBox(new PricerBusinessDelegate().getAllPricingMethods(pricingMeasure.getValue()),
					pricingMethod);
			pricingMethod.getSelectionModel().selectFirst();
		}
		pricingMeasure.valueProperty().addListener((obs, oldMeasure, newMeasure) -> {
			if (newMeasure != null) {
				TradistaGUIUtil.fillComboBox(new PricerBusinessDelegate().getAllPricingMethods(newMeasure),
						pricingMethod);
				pricingMethod.getSelectionModel().selectFirst();
			}
		});
		pricingDate.setValue(LocalDate.now());
		cfPricingDate.setText(pricingDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		pricingDate.valueProperty().addListener((_, _, newDate) -> cfPricingDate
				.setText(newDate != null ? newDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
		quoteDate.setValue(LocalDate.now());
		fillFxQuotesTable();

		book.valueProperty().addListener((obs, oldBook, newBook) -> {
			if (newBook != null) {
				bookChartPane.updateBookChart(newBook);
			}
		});
		if (book.getValue() != null) {
			bookChartPane.updateBookChart(book.getValue());
		}

		buySell.setItems(FXCollections.observableArrayList(Trade.Direction.BUY, Trade.Direction.SELL));
		buySell.getSelectionModel().selectFirst();
		tradeDate.setValue(LocalDate.now());

		cashFlowsTable.setEditable(true);
		cashFlowsTable.setItems(cashFlowRows);

		settlementDateCol.setCellValueFactory(cellData -> cellData.getValue().settlementDateProperty());
		settlementDateCol.setCellFactory(_ -> new DatePickerCell());
		settlementDateCol
				.setOnEditCommit(event -> event.getRowValue().settlementDateProperty().set(event.getNewValue()));

		amountCol.setCellValueFactory(cellData -> new SimpleStringProperty(
				TradistaGUIUtil.formatAmount(cellData.getValue().amountProperty().get())));
		amountCol.setCellFactory(_ -> new TradistaEditingCell<>());
		amountCol.setOnEditCommit(event -> {
			try {
				event.getRowValue().amountProperty().set(TradistaGUIUtil.parseAmount(event.getNewValue(), "Amount"));
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
				cashFlowsTable.refresh();
			}
		});
		transferStatusCol.setCellValueFactory(cellData -> cellData.getValue().transferStatusProperty());
		transferStatusCol.setCellFactory(_ -> new ComboBoxTableCell<>(Transfer.Status.KNOWN, Transfer.Status.SETTLED) {
			@Override
			public void startEdit() {
				CommercialCashFlowRow row = getTableRow() != null ? getTableRow().getItem() : null;
				if (row != null && row.getTransferStatus() == Transfer.Status.SETTLED) {
					return;
				}
				super.startEdit();
			}
		});
		transferStatusCol.setOnEditCommit(event -> {
			CommercialCashFlowRow row = event.getRowValue();
			row.setTransferStatus(event.getNewValue());
			if (row.getTransfer() != null) {
				row.getTransfer().setStatus(event.getNewValue());
				try {
					transferBusinessDelegate.saveTransfers(List.of(row.getTransfer()));
				} catch (TradistaBusinessException _) {
					// Handle error in status bar / alert
				}
			}
			cashFlowsTable.refresh();
		});
		cashFlowsTable.setRowFactory(_ -> new javafx.scene.control.TableRow<>() {
			@Override
			protected void updateItem(CommercialCashFlowRow item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.getTransferStatus() == null) {
					setStyle("");
					return;
				}
				switch (item.getTransferStatus()) {
				case KNOWN -> setStyle("-fx-background-color:rgba(144, 238, 144, .7)");
				case UNKNOWN -> setStyle("-fx-background-color:rgba(255, 193, 77, .7)");
				case CANCELED -> setStyle("-fx-background-color:rgba(205,92,92,.7)");
				case POTENTIAL -> setStyle("-fx-background-color:rgba(135, 206, 250,.7)");
				case SETTLED -> setStyle("-fx-background-color:rgba(60, 179, 113, .7)");
				}
			}
		});
	}

	private static class DatePickerCell extends TableCell<CommercialCashFlowRow, LocalDate> {

		private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		private DatePicker datePicker;

		@Override
		public void startEdit() {
			if (!isEmpty()) {
				super.startEdit();
				createDatePicker();
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(datePicker);
			}
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			setContentDisplay(ContentDisplay.TEXT_ONLY);
			setText(getItem() != null ? getItem().format(DATE_FORMATTER) : null);
			setGraphic(null);
		}

		@Override
		protected void updateItem(LocalDate item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else if (isEditing()) {
				if (datePicker != null) {
					datePicker.setValue(item);
				}
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(datePicker);
			} else {
				setContentDisplay(ContentDisplay.TEXT_ONLY);
				setText(item != null ? item.format(DATE_FORMATTER) : null);
				setGraphic(null);
			}
		}

		private void createDatePicker() {
			datePicker = new DatePicker();
			if (getItem() != null) {
				datePicker.setValue(getItem());
			}
			datePicker.focusedProperty().addListener((_, _, nv) -> {
				if (Boolean.FALSE.equals(nv) && isEditing()) {
					commitEdit(datePicker.getValue());
				}
			});
			datePicker.setMaxWidth(Double.MAX_VALUE);
		}
	}

	@FXML
	private void addEmptyRow() {
		CommercialCashFlowRow row = new CommercialCashFlowRow(LocalDate.now().plusDays(30), BigDecimal.ZERO,
				Transfer.Status.KNOWN, null);
		cashFlowRows.add(row);
	}

	@FXML
	private void removeSelectedRow() {
		CommercialCashFlowRow selected = cashFlowsTable.getSelectionModel().getSelectedItem();
		if (selected != null) {
			cashFlowRows.remove(selected);
		}
	}

	@FXML
	private void generateCashFlows() {
		try {
			buildTrade();
			LocalDate date = pricingDate.getValue() != null ? pricingDate.getValue() : LocalDate.now();
			List<CashFlow> cashFlows = commercialPricerBusinessDelegate.generateCashFlows(trade,
					pricingParameter.getValue(), date);
			if (cashFlows != null) {
				generatedCashFlowsTable.setItems(
						FXCollections.observableArrayList(CashFlowProperty.toCashFlowPropertyList(cashFlows)));
				generateCashFlowsButton.setText("Refresh");
			}
		} catch (TradistaBusinessException | TradistaTechnicalException e) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	private void price() {
		try {
			buildTrade();
			BigDecimal npv = commercialPricerBusinessDelegate.npvDiscountedCashFlow(pricingParameter.getValue(), trade,
					pricingCurrency.getValue(), pricingDate.getValue());
			pricerResultLabel.setText(TradistaGUIUtil.formatAmount(npv));
		} catch (TradistaBusinessException | TradistaTechnicalException e) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	private void save() {
		String selectedAction = (trade == null || trade.getId() <= 0) ? ActionConstants.NEW
				: (actionComboBox != null && actionComboBox.getValue() != null && !actionComboBox.getValue().isEmpty()
						? actionComboBox.getValue()
						: null);
		saveWithAction(selectedAction);
	}

	private void saveWithAction(String action) {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Trade");
		confirmation.setHeaderText("Save Trade");
		confirmation.setContentText("Do you want to save this Trade?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				buildTrade();
				long id = commercialTradeBusinessDelegate.saveCommercialTrade(trade, action);
				trade = commercialTradeBusinessDelegate.getCommercialTradeById(id);
				tradeIdLabel.setText(String.valueOf(id));
				updateStatusAndActions(trade);
				loadTransfersForTrade(id);
			} catch (TradistaBusinessException | TradistaTechnicalException e) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void load() {
		long id = 0;
		try {
			try {
				if (loadTradeId.getText() != null && !loadTradeId.getText().isBlank()) {
					id = Long.parseLong(loadTradeId.getText().trim());
				} else {
					throw new TradistaBusinessException("Please specify a trade id.");
				}
			} catch (NumberFormatException _) {
				throw new TradistaBusinessException(
						String.format("The trade id is incorrect: %s", loadTradeId.getText()));
			}

			CommercialTrade loadedTrade = commercialTradeBusinessDelegate.getCommercialTradeById(id);
			if (loadedTrade == null) {
				throw new TradistaBusinessException(String.format("The %s trade %s was not found.",
						CommercialTrade.COMMERCIAL, loadTradeId.getText()));
			}
			trade = loadedTrade;
			tradeIdLabel.setText(String.valueOf(trade.getId()));
			book.setValue(trade.getBook());
			counterparty.setValue(trade.getCounterparty());
			currency.setValue(trade.getCurrency());
			buySell.setValue(trade.isBuy() ? Trade.Direction.BUY : Trade.Direction.SELL);
			documentReference.setText(trade.getDocumentReference());
			tradeDate.setValue(trade.getTradeDate());
			updateStatusAndActions(trade);
			loadTransfersForTrade(trade.getId());
		} catch (TradistaBusinessException | TradistaTechnicalException e) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	private void copy() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Copy Trade");
		confirmation.setHeaderText("Copy Trade");
		confirmation.setContentText("Do you want to copy this Trade?");
		long oldTradeId = 0;
		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				buildTrade();
				oldTradeId = trade.getId();
				trade.setId(0);
				trade.setStatus(workflowBusinessDelegate.getInitialStatus(CommercialTrade.COMMERCIAL));
				long id = commercialTradeBusinessDelegate.saveCommercialTrade(trade, ActionConstants.NEW);
				trade = commercialTradeBusinessDelegate.getCommercialTradeById(id);
				tradeIdLabel.setText(String.valueOf(id));
				updateStatusAndActions(trade);
				loadTransfersForTrade(id);
			} catch (TradistaBusinessException | TradistaTechnicalException e) {
				if (trade != null) {
					trade.setId(oldTradeId);
				}
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	public void refresh() {
		TradistaGUIUtil.fillBookComboBox(book);
		TradistaGUIUtil.fillCounterpartyComboBox(counterparty);
		TradistaGUIUtil.fillCurrencyComboBox(currency, pricingCurrency);
		TradistaGUIUtil.fillQuoteSetComboBox(quoteSet);
		if (book.getValue() != null) {
			bookChartPane.updateBookChart(book.getValue());
		}
	}

	@FXML
	@Override
	public void clear() {
		trade = null;
		loadTradeId.clear();
		tradeIdLabel.setText("");
		book.getSelectionModel().selectFirst();
		counterparty.getSelectionModel().selectFirst();
		currency.getSelectionModel().selectFirst();
		buySell.getSelectionModel().selectFirst();
		documentReference.clear();
		tradeDate.setValue(LocalDate.now());
		pricingDate.setValue(LocalDate.now());
		cfPricingDate.setText(pricingDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		pricerResultLabel.setText("");
		updateStatusAndActions(null);
		cashFlowRows.clear();
		generatedCashFlowsTable.getItems().clear();
		generateCashFlowsButton.setText("Generate");
	}

	private void updateStatusAndActions(CommercialTrade currentTrade) {
		if (actionComboBox != null) {
			actionComboBox.getItems().clear();
		}
		boolean isSaved = currentTrade != null && currentTrade.getId() > 0 && currentTrade.getStatus() != null;
		setNodeVisible(statusTitleLabel, isSaved);
		setNodeVisible(statusLabel, isSaved);
		setNodeVisible(applyActionLabel, false);
		setNodeVisible(actionComboBox, false);
		if (!isSaved) {
			statusLabel.setText("");
			return;
		}
		statusLabel.setText(currentTrade.getStatus().getName());
		try {
			Workflow workflow = workflowBusinessDelegate.getWorkflowByName(CommercialTrade.COMMERCIAL);
			if (workflow != null) {
				Set<String> availableActions = workflowBusinessDelegate
						.getAvailableActionsFromStatus(workflow.getName(), currentTrade.getStatus());
				if (availableActions != null && !availableActions.isEmpty() && actionComboBox != null) {
					actionComboBox.getItems().add("");
					actionComboBox.getItems().addAll(availableActions);
					actionComboBox.getSelectionModel().selectFirst();
					actionComboBox.setDisable(false);
					setNodeVisible(applyActionLabel, true);
					setNodeVisible(actionComboBox, true);
				}
			}
		} catch (Exception e) {
			// Ignore if workflow is not configured
		}
	}

	private void fillFxQuotesTable() {
		// TODO: Use a PO-level default currency once available to display FX quotes
		// between the trade currency and the PO default currency.
		if (quotesTable != null && quotesTable.getItems() != null) {
			quotesTable.getItems().clear();
		}
	}

	private void updateDiscountCurveLabel() {
		PricingParameter pp = pricingParameter.getValue();
		Currency curr = currency.getValue();
		if (pp != null && curr != null && pp.getDiscountCurves() != null) {
			InterestRateCurve curve = pp.getDiscountCurves().get(curr);
			if (curve != null) {
				cfDiscountCurve.setText(curve.getName());
				TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
				return;
			}
			cfDiscountCurve.setText(
					String.format("Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.",
							pp.getName(), curr));
			TradistaGUIUtil.applyWarningStyle(cfDiscountCurve);
		} else {
			cfDiscountCurve.setText("");
			TradistaGUIUtil.unapplyWarningStyle(cfDiscountCurve);
		}
	}

	private void setNodeVisible(javafx.scene.Node node, boolean visible) {
		if (node != null) {
			node.setVisible(visible);
			node.setManaged(visible);
		}
	}

	private void buildTrade() {
		CommercialTrade.Builder builder = (trade != null) ? trade.toBuilder() : CommercialTrade.builder();
		builder.book(book.getValue()).counterparty(counterparty.getValue()).currency(currency.getValue())
				.buySell(buySell.getValue() == Trade.Direction.BUY).documentReference(documentReference.getText())
				.tradeDate(tradeDate.getValue()).workflow(CommercialTrade.COMMERCIAL);
		if (trade == null || trade.getStatus() == null) {
			try {
				builder.status(workflowBusinessDelegate.getInitialStatus(CommercialTrade.COMMERCIAL));
			} catch (TradistaBusinessException e) {
				// Ignore if workflow is not configured
			}
		}

		List<CommercialCashFlow> items = new ArrayList<>();
		for (CommercialCashFlowRow row : cashFlowRows) {
			CommercialCashFlow item = new CommercialCashFlow();
			item.setSettlementDate(row.getSettlementDate());
			item.setAmount(row.getAmount());
			item.setSettled(row.getTransferStatus() == Transfer.Status.SETTLED);
			items.add(item);
		}
		builder.items(items);
		trade = builder.build();
	}

	private void loadTransfersForTrade(long tradeId) {
		cashFlowRows.clear();
		List<Transfer> transfers = null;
		try {
			transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(tradeId,
					TransferPurpose.CASH_SETTLEMENT, false);
		} catch (TradistaBusinessException e) {
			// Handle error
		}
		if (trade != null && trade.getItems() != null) {
			for (int i = 0; i < trade.getItems().size(); i++) {
				CommercialCashFlow item = trade.getItems().get(i);
				Transfer matchedTransfer = (transfers != null && i < transfers.size()) ? transfers.get(i) : null;
				Transfer.Status status = item.isSettled() ? Transfer.Status.SETTLED
						: (matchedTransfer != null ? matchedTransfer.getStatus() : Transfer.Status.KNOWN);
				cashFlowRows.add(
						new CommercialCashFlowRow(item.getSettlementDate(), item.getAmount(), status, matchedTransfer));
			}
		}
	}

	public static class CommercialCashFlowRow {
		private final ObjectProperty<LocalDate> settlementDate = new SimpleObjectProperty<>();
		private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
		private final ObjectProperty<Transfer.Status> transferStatus = new SimpleObjectProperty<>();
		private Transfer transfer;

		public CommercialCashFlowRow(LocalDate settlementDate, BigDecimal amount, Transfer.Status transferStatus,
				Transfer transfer) {
			this.settlementDate.set(settlementDate);
			this.amount.set(amount);
			this.transferStatus.set(transferStatus);
			this.transfer = transfer;
		}

		public LocalDate getSettlementDate() {
			return settlementDate.get();
		}

		public ObjectProperty<LocalDate> settlementDateProperty() {
			return settlementDate;
		}

		public BigDecimal getAmount() {
			return amount.get();
		}

		public ObjectProperty<BigDecimal> amountProperty() {
			return amount;
		}

		public Transfer.Status getTransferStatus() {
			return transferStatus.get();
		}

		public void setTransferStatus(Transfer.Status status) {
			this.transferStatus.set(status);
		}

		public ObjectProperty<Transfer.Status> transferStatusProperty() {
			return transferStatus;
		}

		public Transfer getTransfer() {
			return transfer;
		}
	}

}