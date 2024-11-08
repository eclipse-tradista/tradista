package org.eclipse.tradista.core.marketdata.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.GenerableCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.service.FXCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.ui.controller.TradistaGenerableCurveController;
import org.eclipse.tradista.core.marketdata.ui.view.FXCurveCreatorDialog;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

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

public class FXCurvesController extends TradistaGenerableCurveController {

	@FXML
	private TableView<RatePointProperty> pointsTable;

	@FXML
	private LineChart<Number, Number> pointsChart;

	@FXML
	private TableColumn<RatePointProperty, String> pointDate;

	@FXML
	private TableColumn<RatePointProperty, String> pointRate;

	@FXML
	private ComboBox<Currency> primaryCurrency;

	@FXML
	private ComboBox<Currency> quoteCurrency;

	@FXML
	private ComboBox<InterestRateCurve> primaryCurrencyIRCurve;

	@FXML
	private ComboBox<InterestRateCurve> quoteCurrencyIRCurve;

	private FXCurveBusinessDelegate fxCurveBusinessDelegate;

	private InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate;

	private TextField rateTextField = new TextField();

	private DatePicker pointDatePicker = new DatePicker();

	protected boolean canGetInterestRateCurve = true;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		super.initialize();

		fxCurveBusinessDelegate = new FXCurveBusinessDelegate();

		quoteBusinessDelegate = new QuoteBusinessDelegate();

		interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();

		Callback<TableColumn<RatePointProperty, String>, TableCell<RatePointProperty, String>> cellFactory = new Callback<TableColumn<RatePointProperty, String>, TableCell<RatePointProperty, String>>() {
			public TableCell<RatePointProperty, String> call(TableColumn<RatePointProperty, String> p) {
				return new EditingCell();
			}
		};

		pointDate.setCellValueFactory(cellData -> cellData.getValue().getDate());

		pointRate.setCellFactory(cellFactory);

		pointRate.setOnEditCommit(new EventHandler<CellEditEvent<RatePointProperty, String>>() {
			@Override
			public void handle(CellEditEvent<RatePointProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Rate");
					((RatePointProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setRate(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				pointsTable.refresh();
			}
		});

		pointRate.setCellValueFactory(cellData -> cellData.getValue().getRate());

		VBox rateGraphic = new VBox();
		Label rateLabel = new Label("Rate");
		rateLabel.setMaxWidth(100);
		rateTextField.setMaxWidth(100);
		rateGraphic.setAlignment(Pos.CENTER);
		rateGraphic.getChildren().addAll(rateLabel, rateTextField);
		pointRate.setGraphic(rateGraphic);

		VBox dateGraphic = new VBox();
		Label dateLabel = new Label("Date");
		dateLabel.setMaxWidth(130);
		pointDatePicker.setMaxWidth(130);
		dateGraphic.setAlignment(Pos.CENTER);
		dateGraphic.getChildren().addAll(dateLabel, pointDatePicker);
		pointDate.setGraphic(dateGraphic);

		curveComboBox.valueProperty().addListener(new ChangeListener<GenerableCurve>() {
			@Override
			public void changed(ObservableValue<? extends GenerableCurve> ov, GenerableCurve oldC, GenerableCurve c) {
				if (c != null) {
					FXCurve fxc = (FXCurve) c;
					primaryCurrency.setValue(fxc.getPrimaryCurrency());
					quoteCurrency.setValue(fxc.getQuoteCurrency());
					primaryCurrencyIRCurve.setValue(fxc.getPrimaryCurrencyIRCurve());
					quoteCurrencyIRCurve.setValue(fxc.getQuoteCurrencyIRCurve());

					List<RatePoint> ratePoints = fxCurveBusinessDelegate.getFXCurvePointsByCurveId(fxc.getId());
					List<RatePointProperty> properties = FXCollections
							.observableArrayList(toRatePointPropertyList(ratePoints));

					// 1. Wrap the ObservableList in a FilteredList
					// (initially display all
					// data).
					FilteredList<RatePointProperty> filteredData = new FilteredList<>(
							FXCollections.observableArrayList(properties));

					// 3. Wrap the FilteredList in a SortedList.
					SortedList<RatePointProperty> sortedData = new SortedList<>(filteredData);

					// 4. Bind the SortedList comparator to the
					// TableView
					// comparator.
					sortedData.comparatorProperty().bind(pointsTable.comparatorProperty());

					// 2. Set the filter Predicate whenever the filter
					// changes.
					rateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty,
							// display all
							// persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getRate().toString().toUpperCase().contains(newValue.toUpperCase());
						});
					});

					pointDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty,
							// display all
							// persons.
							if (newValue == null) {
								return true;
							}
							return newValue.equals(LocalDate
									.from(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(point.getDate().getValue())));
						});
					});

					rateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is empty, display
							// all
							// persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getRate().toString().contains(newValue.toString());
						});
					});

					pointsTable.setItems(sortedData);
					pointsTable.refresh();
					XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
					series.setName(fxc.getName());
					pointsChart.getData().clear();
					if (ratePoints != null && !ratePoints.isEmpty()) {
						long lowerBound = ratePoints.get(0).getDate().toEpochDay();
						for (RatePoint point : ratePoints) {
							if (point.getRate() != null) {
								series.getData().add(new XYChart.Data<Number, Number>(
										point.getDate().toEpochDay() - lowerBound, point.getRate()));
							}
						}
						pointsChart.getXAxis().setTickLabelRotation(90);
						((ValueAxis<Number>) pointsChart.getXAxis()).setMinorTickVisible(false);
						((ValueAxis<Number>) pointsChart.getXAxis())
								.setTickLabelFormatter(new StringConverter<Number>() {

									@Override
									public String toString(Number number) {
										return LocalDate.ofEpochDay(number.longValue() + lowerBound).toString();
									}

									@Override
									public Number fromString(String string) {
										return LocalDate.parse(string, DateTimeFormatter.ISO_DATE).toEpochDay()
												+ lowerBound;
									}

								});
						pointsChart.getXAxis().setLabel("Date");
						pointsChart.getYAxis().setLabel("Rate");
						pointsChart.setCreateSymbols(false);
						pointsChart.getData().clear();
						pointsChart.getData().add(series);
					}
				} else {

					pointsTable.setItems(null);
					primaryCurrency.getSelectionModel().clearSelection();
					quoteCurrency.getSelectionModel().clearSelection();
					primaryCurrencyIRCurve.getSelectionModel().clearSelection();
					quoteCurrencyIRCurve.getSelectionModel().clearSelection();

				}
			}
		});
		try {
			TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllGenerationAlgorithms(), algorithmComboBox);
		} catch (TradistaTechnicalException tte) {
			canGetGenerationAlgorithms = false;
		}
		try {
			TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllInterpolators(), interpolatorComboBox);
		} catch (TradistaTechnicalException tte) {
			canGetInterpolators = false;
		}
		TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllInstances(), instanceComboBox);
		TradistaGUIUtil.fillCurrencyComboBox(primaryCurrency, quoteCurrency);
		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterestRateCurves(),
					primaryCurrencyIRCurve, quoteCurrencyIRCurve);
		} catch (TradistaTechnicalException tte) {
			canGetInterestRateCurve = false;
		}
		try {
			Set<FXCurve> curves = fxCurveBusinessDelegate.getAllFXCurves();
			TradistaGUIUtil.fillComboBox(curves, curveComboBox);
			canGetCurve = true;
			curveExists = (curves != null && !curves.isEmpty());
		} catch (TradistaTechnicalException tte) {
			canGetCurve = false;
		}

		updateWindow();

	}

	private void buildCurve(FXCurve curve) throws TradistaBusinessException {
		Map<LocalDate, BigDecimal> ratePoints = toRatePointsMap(pointsTable.getItems());
		curve.setPoints(ratePoints);

		curve.setQuoteSet(quoteSet.getValue());

		if (isGeneratedCheckBox.isSelected()) {
			curve.setInstance(instanceComboBox.getValue());
			curve.setInterpolator(interpolatorComboBox.getValue());
			curve.setAlgorithm(algorithmComboBox.getValue());
		}

		curve.setQuoteDate(quoteDate.getValue());

		((FXCurve) curve).setPrimaryCurrency(primaryCurrency.getValue());
		((FXCurve) curve).setQuoteCurrency(quoteCurrency.getValue());
		((FXCurve) curve).setPrimaryCurrencyIRCurve(primaryCurrencyIRCurve.getValue());
		((FXCurve) curve).setQuoteCurrencyIRCurve(quoteCurrencyIRCurve.getValue());
	}

	@FXML
	protected void save() {
		try {
			buildCurve((FXCurve) curve);
			curve.setId(fxCurveBusinessDelegate.saveFXCurve(((FXCurve) curve)));
			TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), curveComboBox);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		try {
			if (curve == null) {
				throw new TradistaBusinessException("Please load a FX curve.");
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
		try {
			TradistaTextInputDialog dialog = new TradistaTextInputDialog();
			dialog.setTitle("Curve name");
			dialog.setHeaderText("Curve name selection");
			dialog.setContentText("Please choose a Curve name:");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				FXCurve copyCurve = new FXCurve(result.get(), ClientUtil.getCurrentUser().getProcessingOrg());
				buildCurve(copyCurve);
				copyCurve.setId(fxCurveBusinessDelegate.saveFXCurve(copyCurve));
				curve = copyCurve;
				TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), curveComboBox);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			if (curve == null) {
				throw new TradistaBusinessException("No curve name has been selected.");
			}

			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete FX Curve");
			confirmation.setHeaderText("Delete FX Curve");
			confirmation.setContentText("Do you want to delete this FX Curve?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fxCurveBusinessDelegate.deleteFXCurve(curve.getId());
				curve = null;
				TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), curveComboBox);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void generate() {
		List<RatePoint> ratePoints;
		try {
			ratePoints = fxCurveBusinessDelegate.generate(algorithmComboBox.getValue(), interpolatorComboBox.getValue(),
					instanceComboBox.getValue(), quoteDate.getValue(), quoteSet.getValue(), primaryCurrency.getValue(),
					quoteCurrency.getValue(), primaryCurrencyIRCurve.getValue(), quoteCurrencyIRCurve.getValue());
			// Update the points table
			pointsTable.setItems(FXCollections.observableArrayList(toRatePointPropertyList(ratePoints)));
			pointsTable.refresh();
			// Update the graph
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			series.setName(curveComboBox.getSelectionModel().getSelectedItem().getName());

			long lowerBound = ratePoints.get(0).getDate().toEpochDay();
			for (RatePoint point : ratePoints) {
				if (point.getRate() != null) {
					series.getData().add(new XYChart.Data<Number, Number>(point.getDate().toEpochDay() - lowerBound,
							point.getRate()));
				}
			}
			pointsChart.getXAxis().setTickLabelRotation(90);
			((ValueAxis<Number>) pointsChart.getXAxis()).setMinorTickVisible(false);
			((ValueAxis<Number>) pointsChart.getXAxis()).setTickLabelFormatter(new StringConverter<Number>() {

				@Override
				public String toString(Number number) {
					return LocalDate.ofEpochDay(number.longValue() + lowerBound).toString();
				}

				@Override
				public Number fromString(String string) {
					return LocalDate.parse(string, DateTimeFormatter.ISO_DATE).toEpochDay() + lowerBound;
				}

			});
			pointsChart.getXAxis().setLabel("Date");
			pointsChart.getYAxis().setLabel("Rate");
			pointsChart.setCreateSymbols(false);
			pointsChart.getData().clear();
			pointsChart.getData().add(series);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void create() {
		try {
			FXCurveCreatorDialog dialog = new FXCurveCreatorDialog();
			Optional<FXCurve> result = dialog.showAndWait();

			if (result.isPresent()) {
				FXCurve fxCurve = result.get();
				fxCurveBusinessDelegate.saveFXCurve(fxCurve);
				TradistaGUIUtil.fillComboBox(fxCurveBusinessDelegate.getAllFXCurves(), curveComboBox);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	class EditingCell extends TableCell<RatePointProperty, String> {

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

	private List<RatePointProperty> toRatePointPropertyList(List<RatePoint> data) {
		List<RatePointProperty> ratePointPropertyList = new ArrayList<RatePointProperty>();
		if (data != null) {
			for (RatePoint point : data) {
				ratePointPropertyList
						.add(new RatePointProperty(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(point.getDate()),
								point.getRate() == null ? "" : TradistaGUIUtil.formatAmount(point.getRate())));
			}
		}
		return ratePointPropertyList;
	}

	private Map<LocalDate, BigDecimal> toRatePointsMap(List<RatePointProperty> data) throws TradistaBusinessException {
		Map<LocalDate, BigDecimal> ratePointsMap = new HashMap<LocalDate, BigDecimal>();
		for (RatePointProperty point : data) {
			try {
				ratePointsMap.put(
						LocalDate.from(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(point.getDate().getValue())),
						point.getRate().getValue().isEmpty() ? null
								: TradistaGUIUtil.parseAmount(point.getRate().getValue(), "Rate"));
			} catch (DateTimeParseException e) {
				// TODO Add a WARN log and continue
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ratePointsMap;
	}

	public static class RatePointProperty {

		private final StringProperty date;
		private final StringProperty rate;

		private RatePointProperty(String date, String rate) {
			this.date = new SimpleStringProperty(date);
			this.rate = new SimpleStringProperty(rate);
		}

		public StringProperty getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date.set(date);
		}

		public StringProperty getRate() {
			return rate;
		}

		public void setRate(String rate) {
			this.rate.set(rate);
		}

	}

	public static class QuoteProperty {

		private final SimpleLongProperty id;
		private final SimpleStringProperty name;

		private QuoteProperty(String name, long id) {
			this.name = new SimpleStringProperty(name);
			this.id = new SimpleLongProperty(id);
		}

		public String getName() {
			return name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public long getId() {
			return id.get();
		}

		public void setId(long id) {
			this.id.set(id);
		}
	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		try {
			Set<FXCurve> curves = fxCurveBusinessDelegate.getAllFXCurves();
			TradistaGUIUtil.fillComboBox(curves, curveComboBox);
			canGetCurve = true;
			canSaveCurve = true;
			canCopyCurve = true;
			canGenerateCurve = true;
			canDeleteCurve = true;
			canCreateCurve = true;
			curveExists = (curves != null && !curves.isEmpty());
		} catch (TradistaTechnicalException tte) {
			canGetCurve = false;
			canSaveCurve = false;
			canCopyCurve = false;
			canGenerateCurve = false;
			canDeleteCurve = false;
			canCreateCurve = false;
		}
		TradistaGUIUtil.fillCurrencyComboBox(primaryCurrency, quoteCurrency);
		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterestRateCurves(),
					primaryCurrencyIRCurve, quoteCurrencyIRCurve);
			canGetInterestRateCurve = true;
		} catch (TradistaTechnicalException tte) {
			canGetInterestRateCurve = false;
		}
		if (!canGetGenerationAlgorithms) {
			try {
				TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllGenerationAlgorithms(),
						algorithmComboBox);
				canGetGenerationAlgorithms = true;
			} catch (TradistaTechnicalException tte) {
				canGetGenerationAlgorithms = false;
			}
		}
		if (!canGetInterpolators) {
			try {
				TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterpolators(),
						interpolatorComboBox);
				canGetInterpolators = true;
			} catch (TradistaTechnicalException tte) {
				canGetInterpolators = false;
			}
		}
		updateWindow();
	}

	public void buildErrorMap() {
		super.buildErrorMap();
		if (!canGetInterestRateCurve) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("ir curves");
			errors.put("get", err);
		}
	}

	public void updateComponents() {
		super.updateComponents();
		primaryCurrency.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		primaryCurrencyIRCurve.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		quoteCurrency.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		quoteCurrencyIRCurve.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		quoteSet.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators || !canGetInterestRateCurve);
		algorithmComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators || !canGetInterestRateCurve);
		interpolatorComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		saveButton.setDisable(!canGetCurve || !canSaveCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		copyButton.setDisable(!canGetCurve || !canCopyCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		generateButton.setDisable(!canGetQuote || !canGenerateCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetCurve || !canGetGenerationAlgorithms || !canGetInterpolators || !canGetInterestRateCurve);
		instanceComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators || !canGetInterestRateCurve);
		isGeneratedCheckBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators || !canGetInterestRateCurve);
		quoteDate.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators || !canGetInterestRateCurve);
	}

}