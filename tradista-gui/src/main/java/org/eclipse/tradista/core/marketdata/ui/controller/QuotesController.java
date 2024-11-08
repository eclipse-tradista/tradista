package org.eclipse.tradista.core.marketdata.ui.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.ui.controller.QuoteProperty;
import org.eclipse.tradista.core.marketdata.ui.view.QuoteCreatorDialog;
import org.eclipse.tradista.core.marketdata.ui.view.QuoteSetCreatorDialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class QuotesController extends TradistaControllerAdapter {

	@FXML
	private TableView<QuoteProperty> quotesTable;

	@FXML
	private LineChart<Number, Number> quotesChart;

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
	private ComboBox<String> quote;

	@FXML
	private ComboBox<QuoteSet> quoteSet;

	@FXML
	private ComboBox<Year> year;

	@FXML
	private ComboBox<Month> month;

	@FXML
	private ComboBox<QuoteType> typeComboBox;

	@FXML
	private Button deleteQuoteSetButton;

	@FXML
	private Button deleteQuoteButton;

	@FXML
	private Label marketDataMessage;

	@FXML
	private Button load;

	@FXML
	private Button save;

	@FXML
	private Button addQuoteSet;

	@FXML
	private Button addQuote;

	private String currentQuoteName;

	private QuoteSet currentQuoteSet;

	private QuoteType currentQuoteType;

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private boolean quoteSetExists = false;

	private boolean quoteExists = false;

	private boolean canGetQuoteSet = true;

	private boolean canGetQuote = true;

	private boolean canGetQuoteValue = true;

	private boolean canDeleteQuoteSet = true;

	private boolean canDeleteQuote = true;

	private boolean canSaveQuoteValue = true;

	private boolean canAddQuoteSet = true;

	private boolean canAddQuote = true;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		quoteBusinessDelegate = new QuoteBusinessDelegate();

		Callback<TableColumn<QuoteProperty, String>, TableCell<QuoteProperty, String>> cellFactory = new Callback<TableColumn<QuoteProperty, String>, TableCell<QuoteProperty, String>>() {
			public TableCell<QuoteProperty, String> call(TableColumn<QuoteProperty, String> p) {
				return new EditingCell();
			}
		};

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

		quoteBid.setCellFactory(cellFactory);
		quoteAsk.setCellFactory(cellFactory);
		quoteOpen.setCellFactory(cellFactory);
		quoteClose.setCellFactory(cellFactory);
		quoteHigh.setCellFactory(cellFactory);
		quoteLow.setCellFactory(cellFactory);
		quoteLast.setCellFactory(cellFactory);
		quoteSourceName.setCellFactory(cellFactory);

		quoteBid.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Bid");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setBid(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteAsk.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Ask");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setAsk(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteOpen.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Open");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setOpen(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteClose.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Close");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setClose(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteHigh.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "High");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setHigh(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteLow.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Low");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setLow(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteLast.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Last");
					((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setLast(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				quotesTable.refresh();
			}
		});

		quoteSourceName.setOnEditCommit(new EventHandler<CellEditEvent<QuoteProperty, String>>() {
			@Override
			public void handle(CellEditEvent<QuoteProperty, String> t) {
				((QuoteProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setSourceName(t.getNewValue());
			}
		});

		List<Year> years = new ArrayList<Year>();

		for (int i = 1900; i < 2101; i++) {
			years.add(Year.of(i));
		}

		try {
			List<String> quoteNames = quoteBusinessDelegate.getAllQuoteNames();
			TradistaGUIUtil.fillComboBox(quoteNames, quote);
			quoteExists = (quoteNames != null && !quoteNames.isEmpty());
		} catch (TradistaTechnicalException tte) {
			canGetQuote = false;
		}

		try {
			Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
			TradistaGUIUtil.fillComboBox(quoteSets, quoteSet);
			quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
		}

		year.setItems(FXCollections.observableArrayList(years));
		year.getSelectionModel().select(Year.now());

		month.setItems(FXCollections.observableArrayList(Month.values()));
		month.getSelectionModel().select(LocalDate.now().getMonth());

		TradistaGUIUtil.fillQuoteTypeComboBox(typeComboBox);

		updateWindow();
	}

	@FXML
	protected void load() {

		currentQuoteName = quote.getSelectionModel().getSelectedItem();
		currentQuoteSet = quoteSet.getSelectionModel().getSelectedItem();
		currentQuoteType = typeComboBox.getSelectionModel().getSelectedItem();

		List<QuoteValue> quoteValues = null;
		try {
			if (currentQuoteSet == null) {
				throw new TradistaBusinessException("The QuoteSet must be selected.");
			}
			try {
				quoteValues = quoteBusinessDelegate.getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(
						currentQuoteSet.getId(), currentQuoteName, currentQuoteType,
						year.getSelectionModel().getSelectedItem(), month.getSelectionModel().getSelectedItem());
				canGetQuoteValue = true;
			} catch (TradistaTechnicalException tte) {
				canGetQuoteValue = false;
				throw tte;
			}
			ObservableList<QuoteProperty> data = QuoteProperty.buildTableContent(quoteValues,
					month.getSelectionModel().getSelectedItem(), year.getSelectionModel().getSelectedItem(),
					currentQuoteName, currentQuoteType);

			quotesTable.setItems(data);
			quotesTable.refresh();
			XYChart.Series<Number, Number> seriesAsk = new XYChart.Series<Number, Number>();
			seriesAsk.setName(currentQuoteName + " - Ask");

			XYChart.Series<Number, Number> seriesBid = new XYChart.Series<Number, Number>();
			seriesBid.setName(currentQuoteName + " - Bid");

			XYChart.Series<Number, Number> seriesOpen = new XYChart.Series<Number, Number>();
			seriesOpen.setName(currentQuoteName + " - Open");

			XYChart.Series<Number, Number> seriesClose = new XYChart.Series<Number, Number>();
			seriesClose.setName(currentQuoteName + " - Close");

			XYChart.Series<Number, Number> seriesLast = new XYChart.Series<Number, Number>();
			seriesLast.setName(currentQuoteName + " - Last");

			XYChart.Series<Number, Number> seriesHigh = new XYChart.Series<Number, Number>();
			seriesHigh.setName(currentQuoteName + " - High");

			XYChart.Series<Number, Number> seriesLow = new XYChart.Series<Number, Number>();
			seriesLow.setName(currentQuoteName + " - Low");

			if (quoteValues != null && !quoteValues.isEmpty()) {
				int monthLength = quoteValues.get(0).getDate().lengthOfMonth();
				for (QuoteValue qv : quoteValues) {
					if (qv.getAsk() != null) {
						seriesAsk.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getAsk()));
					}
					if (qv.getBid() != null) {
						seriesBid.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getBid()));
					}
					if (qv.getOpen() != null) {
						seriesOpen.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getOpen()));
					}
					if (qv.getClose() != null) {
						seriesClose.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getClose()));
					}
					if (qv.getLast() != null) {
						seriesLast.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getLast()));
					}
					if (qv.getHigh() != null) {
						seriesHigh.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getHigh()));
					}
					if (qv.getLow() != null) {
						seriesLow.getData()
								.add(new XYChart.Data<Number, Number>(qv.getDate().getDayOfMonth(), qv.getLow()));
					}
				}
				quotesChart.getXAxis().setAutoRanging(false);
				((ValueAxis<Number>) quotesChart.getXAxis()).setLowerBound(1);
				((ValueAxis<Number>) quotesChart.getXAxis()).setUpperBound(monthLength);
				((NumberAxis) quotesChart.getXAxis()).setTickUnit(1);
				((ValueAxis<Number>) quotesChart.getXAxis()).setMinorTickVisible(false);
				quotesChart.getXAxis().setLabel("Day");
				quotesChart.getYAxis().setLabel("Rate");
				quotesChart.setCreateSymbols(false);
				quotesChart.getData().clear();
				if (!seriesAsk.getData().isEmpty()) {
					quotesChart.getData().add(seriesAsk);
				}
				if (!seriesBid.getData().isEmpty()) {
					quotesChart.getData().add(seriesBid);
				}
				if (!seriesClose.getData().isEmpty()) {
					quotesChart.getData().add(seriesClose);
				}
				if (!seriesOpen.getData().isEmpty()) {
					quotesChart.getData().add(seriesOpen);
				}
				if (!seriesHigh.getData().isEmpty()) {
					quotesChart.getData().add(seriesHigh);
				}
				if (!seriesLow.getData().isEmpty()) {
					quotesChart.getData().add(seriesLow);
				}
				if (!seriesLast.getData().isEmpty()) {
					quotesChart.getData().add(seriesLast);
				}
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}

	}

	@FXML
	protected void save() {
		try {
			List<QuoteValue> quoteValues = toQuoteValueList(quotesTable.getItems());
			if (quoteSet.getValue() == null) {
				throw new TradistaBusinessException("The QuoteSet must be selected.");
			}
			try {
				quoteBusinessDelegate.saveQuoteValues(quoteSet.getValue().getId(), quote.getValue(),
						this.typeComboBox.getValue(), quoteValues, year.getSelectionModel().getSelectedItem(),
						month.getSelectionModel().getSelectedItem());
				canSaveQuoteValue = true;
			} catch (TradistaTechnicalException tte) {
				canSaveQuoteValue = false;
				throw tte;
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}
	}

	@FXML
	protected void createQuoteSet(ActionEvent event) {
		try {
			QuoteSetCreatorDialog dialog = new QuoteSetCreatorDialog();
			Optional<QuoteSet> result = dialog.showAndWait();
			if (result.isPresent()) {
				QuoteSet qs = result.get();
				try {
					quoteBusinessDelegate.saveQuoteSet(qs);
					canAddQuoteSet = true;
				} catch (TradistaTechnicalException tte) {
					canAddQuoteSet = false;
					throw tte;
				}
				QuoteSet selectedQS = quoteSet.getValue();
				try {
					Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
					TradistaGUIUtil.fillComboBox(quoteSets, quoteSet);
					canGetQuoteSet = true;
					quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
				} catch (TradistaTechnicalException tte) {
					canGetQuoteSet = false;
					throw tte;
				}
				if (selectedQS != null && !selectedQS.equals(quoteSet.getValue())
						&& selectedQS.equals(currentQuoteSet)) {
					quotesTable.setItems(null);
					quotesChart.getData().clear();
					currentQuoteSet = null;
					currentQuoteType = null;
					currentQuoteName = null;
				}
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}
	}

	@FXML
	protected void createQuote() {
		try {
			QuoteCreatorDialog dialog = new QuoteCreatorDialog();
			Optional<Quote> result = dialog.showAndWait();

			if (result.isPresent()) {
				Quote q = result.get();
				try {
					quoteBusinessDelegate.saveQuote(q);
					canAddQuote = true;
				} catch (TradistaTechnicalException tte) {
					canAddQuote = false;
					throw tte;
				}
				String selectedQuoteName = quote.getValue();
				try {
					List<String> quoteNames = quoteBusinessDelegate.getAllQuoteNames();
					TradistaGUIUtil.fillComboBox(quoteNames, quote);
					canGetQuote = true;
					quoteExists = (quoteNames != null && !quoteNames.isEmpty());
				} catch (TradistaTechnicalException tte) {
					canGetQuote = false;
					throw tte;
				}
				if (selectedQuoteName != null && !selectedQuoteName.equals(quote.getValue())
						&& selectedQuoteName.equals(currentQuoteName)) {
					quotesTable.setItems(null);
					quotesChart.getData().clear();
					currentQuoteSet = null;
					currentQuoteType = null;
					currentQuoteName = null;
				}
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}
	}

	@FXML
	protected void deleteQuote() {
		try {
			if (quote.getValue() == null) {
				throw new TradistaBusinessException("Please select a quote name.");
			}

			if (typeComboBox.getValue() == null) {
				throw new TradistaBusinessException("Please select a quote type.");
			}

			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Quote");
			confirmation.setHeaderText("Delete Quote");
			confirmation.setContentText(String.format("Do you want to delete this Quote %s of type %s ?",
					quote.getValue(), typeComboBox.getValue()));

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				String quoteName = quote.getValue();
				QuoteType quoteType = typeComboBox.getValue();
				try {
					quoteBusinessDelegate.deleteQuote(quoteName, quoteType);
					canDeleteQuote = true;
				} catch (TradistaTechnicalException tte) {
					canDeleteQuote = false;
					throw tte;
				}
				try {
					List<String> quoteNames = quoteBusinessDelegate.getAllQuoteNames();
					TradistaGUIUtil.fillComboBox(quoteNames, quote);
					canGetQuote = true;
					quoteExists = (quoteNames != null && !quoteNames.isEmpty());
				} catch (TradistaTechnicalException tte) {
					canGetQuote = false;
					throw tte;
				}
				if (currentQuoteName != null && currentQuoteName.equals(quoteName) && currentQuoteType != null
						&& currentQuoteType.equals(quoteType)) {
					quotesTable.setItems(null);
					quotesChart.getData().clear();
					currentQuoteSet = null;
					currentQuoteType = null;
					currentQuoteName = null;
				}
			}

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}
	}

	@FXML
	protected void deleteQuoteSet() {
		try {
			if (quoteSet.getValue() == null) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "Please select a quote set name.");
				alert.showAndWait();
			}

			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Quote Set");
			confirmation.setHeaderText("Delete Quote Set");
			confirmation
					.setContentText(String.format("Do you want to delete this Quote Set %s ?", quoteSet.getValue()));

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				QuoteSet qs = quoteSet.getValue();
				try {
					quoteBusinessDelegate.deleteQuoteSet(qs.getId());
					canDeleteQuoteSet = true;
				} catch (TradistaTechnicalException tte) {
					canDeleteQuoteSet = false;
					throw tte;
				}
				try {
					Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
					TradistaGUIUtil.fillComboBox(quoteSets, quoteSet);
					canGetQuoteSet = true;
					quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
				} catch (TradistaTechnicalException tte) {
					canGetQuoteSet = false;
					throw tte;
				}

				if (qs.equals(currentQuoteSet)) {
					quotesTable.setItems(null);
					quotesChart.getData().clear();
					currentQuoteSet = null;
					currentQuoteType = null;
					currentQuoteName = null;
				}
			}

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		} finally {
			updateWindow();
		}
	}

	class EditingCell extends TableCell<QuoteProperty, String> {

		private TextField textField;

		public EditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && textField.getText() != null && !textField.getText().equals("")) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(textField.getText());
					}
				}
			});

		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private List<QuoteValue> toQuoteValueList(List<QuoteProperty> data) throws TradistaBusinessException {
		List<QuoteValue> quoteValueList = new ArrayList<QuoteValue>();
		for (QuoteProperty quoteValue : data) {

			if (valueExists(quoteValue)) {
				try {
					QuoteValue qv = new QuoteValue(
							LocalDate.from(
									DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(quoteValue.getDate().getValue())),
							quoteValue.getBid().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getBid().getValue(), "Bid"),
							quoteValue.getAsk().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getAsk().getValue(), "Ask"),
							quoteValue.getOpen().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getOpen().getValue(), "Open"),
							quoteValue.getClose().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getClose().getValue(), "Close"),
							quoteValue.getHigh().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getHigh().getValue(), "High"),
							quoteValue.getLow().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getLow().getValue(), "Low"),
							quoteValue.getLast().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(quoteValue.getLast().getValue(), "Last"),
							quoteValue.getSourceName().getValue().isEmpty() ? null
									: quoteValue.getSourceName().getValue(),
							quoteBusinessDelegate.getQuoteByNameAndType(quoteValue.getName().getValue(),
									QuoteType.getQuoteType(quoteValue.getType().getValue())),
							LocalDate.now(), quoteSet.getValue());

					quoteValueList.add(qv);

				} catch (DateTimeException dte) {

				}
			}
		}

		return quoteValueList;
	}

	private boolean valueExists(QuoteProperty quoteValue) {
		return (!quoteValue.getAsk().getValue().isEmpty() || !quoteValue.getBid().getValue().isEmpty()
				|| !quoteValue.getClose().getValue().isEmpty() || !quoteValue.getHigh().getValue().isEmpty()
				|| !quoteValue.getLast().getValue().isEmpty() || !quoteValue.getLow().getValue().isEmpty()
				|| !quoteValue.getOpen().getValue().isEmpty());
	}

	@Override
	@FXML
	public void refresh() {
		try {
			List<String> quoteNames = quoteBusinessDelegate.getAllQuoteNames();
			quoteExists = (quoteNames != null && !quoteNames.isEmpty());
			canGetQuote = true;
			canAddQuote = true;
			canDeleteQuote = true;
			TradistaGUIUtil.fillComboBox(quoteNames, quote);
		} catch (TradistaTechnicalException tte) {
			canGetQuote = false;
			canAddQuote = false;
			canDeleteQuote = false;
		}
		try {
			Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
			quoteSetExists = (quoteSets != null && !quoteSets.isEmpty());
			canGetQuoteSet = true;
			canAddQuoteSet = true;
			canDeleteQuoteSet = true;
			TradistaGUIUtil.fillComboBox(quoteSets, quoteSet);
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
			canAddQuote = false;
			canDeleteQuote = false;
		}
		if (!canGetQuoteValue || !canSaveQuoteValue) {
			try {
				quoteBusinessDelegate.getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(1, "Ping", QuoteType.BETA,
						Year.now(), Month.JANUARY);
				canGetQuoteValue = true;
				canSaveQuoteValue = true;
			} catch (TradistaTechnicalException tte) {
				canGetQuoteValue = false;
				canSaveQuoteValue = false;
			} catch (TradistaBusinessException tbe) {
				// Should not happen here.
			}
		}

		updateWindow();
	}

	protected void updateWindow() {
		List<String> errors = new ArrayList<String>();
		String errMsg = "Cannot ";
		boolean isError = false;

		quoteSet.setDisable(!quoteSetExists || !canGetQuoteSet || !canDeleteQuoteSet);
		deleteQuoteSetButton.setDisable(!quoteSetExists || !canGetQuoteSet || !canDeleteQuoteSet);
		quote.setDisable(!quoteExists || !canGetQuote || !canDeleteQuote);
		typeComboBox.setDisable(!quoteExists || !canGetQuote || !canDeleteQuote);
		deleteQuoteButton.setDisable(!quoteExists || !canGetQuote || !canDeleteQuote);
		year.setDisable(!quoteSetExists || !canGetQuoteSet || !quoteExists || !canGetQuote || !canGetQuoteValue
				|| !canSaveQuoteValue);
		month.setDisable(!quoteSetExists || !canGetQuoteSet || !quoteExists || !canGetQuote || !canGetQuoteValue
				|| !canSaveQuoteValue);
		load.setDisable(!quoteSetExists || !canGetQuoteSet || !quoteExists || !canGetQuote || !canGetQuoteValue
				|| !canSaveQuoteValue);
		save.setDisable(!quoteSetExists || !canGetQuoteSet || !quoteExists || !canGetQuote || !canGetQuoteValue
				|| !canSaveQuoteValue);
		addQuoteSet.setDisable(!canGetQuoteSet || !canAddQuoteSet);
		addQuote.setDisable(!canGetQuote || !canAddQuote);

		if (!quoteSetExists) {
			TradistaGUIUtil.unapplyErrorStyle(marketDataMessage);
			TradistaGUIUtil.applyWarningStyle(marketDataMessage);
			if (quoteExists) {
				marketDataMessage.setText("There is no quote set, please create one.");
			} else {
				marketDataMessage.setText("There is no quote set and no quote, please create some.");
			}
		} else {
			if (!quoteExists) {
				TradistaGUIUtil.unapplyErrorStyle(marketDataMessage);
				TradistaGUIUtil.applyWarningStyle(marketDataMessage);
				marketDataMessage.setText("There is no quote, please create one.");
			}
		}

		if (!canGetQuoteSet) {
			errors.add("get quote sets");
		}
		if (!canDeleteQuoteSet) {
			errors.add("delete quote sets");
		}
		if (!canAddQuoteSet) {
			errors.add("add quote sets");
		}
		if (!canGetQuote) {
			errors.add("get quotes");
		}
		if (!canDeleteQuote) {
			errors.add("delete quotes");
		}
		if (!canAddQuote) {
			errors.add("add quotes");
		}
		if (!canGetQuoteValue) {
			errors.add("get quote values");
		}
		if (!canSaveQuoteValue) {
			errors.add("save quote values");
		}
		isError = !errors.isEmpty();
		for (String err : errors) {
			errMsg += err;
			errMsg += ", ";
		}
		errMsg += "please contact support.";

		if (isError) {
			TradistaGUIUtil.unapplyWarningStyle(marketDataMessage);
			TradistaGUIUtil.applyErrorStyle(marketDataMessage);
			marketDataMessage.setText(errMsg);
		}

		marketDataMessage.setVisible(isError || !quoteExists || !quoteSetExists);

	}

}