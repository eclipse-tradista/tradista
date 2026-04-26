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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.ui.view.ZeroCouponCurveCreatorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class ZeroCouponCurvesController extends TradistaGenerableCurveController {

	private static final String YYYY_MM_DD = "yyyy-MM-dd";

	private static final Logger logger = LoggerFactory.getLogger(ZeroCouponCurvesController.class);

	@FXML
	private TableView<RatePointProperty> pointsTable;

	@FXML
	private LineChart<Number, Number> pointsChart;

	@FXML
	private TableColumn<RatePointProperty, String> pointDate;

	@FXML
	private TableColumn<RatePointProperty, String> pointRate;

	@FXML
	private Button includeButton, excludeButton;

	@FXML
	private ListView<Quote> quotesList, selectedQuotesList;

	@FXML
	protected TextField quoteNameTextField;

	@FXML
	protected Button searchButton;

	private InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate;

	private TextField rateTextField = new TextField();

	private DatePicker pointDatePicker = new DatePicker();

	// This method is called by the FXMLLoader when initialization is complete
	@SuppressWarnings("unchecked")
	public void initialize() {

		super.initialize();

		interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();

		Callback<TableColumn<RatePointProperty, String>, TableCell<RatePointProperty, String>> cellFactory = _ -> new EditingCell();

		pointDate.setCellValueFactory(cellData -> cellData.getValue().getDate());

		pointRate.setCellFactory(cellFactory);

		pointRate.setOnEditCommit(t -> {
			try {
				TradistaGUIUtil.parseAmount(t.getNewValue(), "Rate");
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setRate(t.getNewValue());
			} catch (TradistaBusinessException abe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
				alert.showAndWait();
			}
			pointsTable.refresh();
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

		quoteDate.setValue(LocalDate.now());

		curveComboBox.valueProperty().addListener((_, _, zc) -> {
			if (zc != null) {
				List<RatePoint> ratePoints = interestRateCurveBusinessDelegate
						.getInterestRateCurvePointsByCurveId(zc.getId());
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
				rateTextField.textProperty().addListener((_, _, newValue) -> filteredData.setPredicate(point -> {
					// If filter text is
					// empty,
					// display all
					// persons.
					if (newValue == null || newValue.isEmpty()) {
						return true;
					}
					return point.getRate().getValue().toUpperCase().contains(newValue.toUpperCase());
				}));

				pointDatePicker.valueProperty().addListener((_, _, newValue) -> filteredData.setPredicate(point -> {
					// If filter text is
					// empty,
					// display all
					// persons.
					if (newValue == null) {
						return true;
					}
					return newValue.equals(LocalDate
							.from(DateTimeFormatter.ofPattern(YYYY_MM_DD).parse(point.getDate().getValue())));
				}));

				rateTextField.textProperty().addListener((_, _, newValue) -> filteredData.setPredicate(point -> {
					// If filter text is empty, display
					// all
					// persons.
					if (newValue == null || newValue.isEmpty()) {
						return true;
					}
					return point.getRate().getValue().contains(newValue);
				}));

				List<Quote> quotes = quoteBusinessDelegate.getQuotesByCurveId(zc.getId());
				if (quotes != null) {
					selectedQuotesList.setItems(FXCollections.observableArrayList(quotes));
				}

				pointsTable.setItems(sortedData);
				pointsTable.refresh();
				XYChart.Series<Number, Number> series = new XYChart.Series<>();
				series.setName(zc.getName());

				pointsChart.getData().clear();
				if (ratePoints != null && !ratePoints.isEmpty()) {
					long lowerBound = ratePoints.getFirst().getDate().toEpochDay();
					for (RatePoint point : ratePoints) {
						if (point.getRate() != null) {
							series.getData().add(
									new XYChart.Data<>(point.getDate().toEpochDay() - lowerBound, point.getRate()));
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
					pointsChart.getData().add(series);
				}
			} else {
				selectedQuotesList.getItems().clear();
				pointsTable.setItems(null);

			}
		});
		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllGenerationAlgorithms(),
					algorithmComboBox);
			canGetGenerationAlgorithms = true;
		} catch (TradistaTechnicalException _) {
			canGetGenerationAlgorithms = false;
		}
		try {
			TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterpolators(), interpolatorComboBox);
			canGetInterpolators = true;
		} catch (TradistaTechnicalException _) {
			canGetInterpolators = false;
		}
		TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInstances(), instanceComboBox);
		try {
			TradistaGUIUtil.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
			canGetCurve = true;
			curveExists = (curveComboBox.getItems() != null && !curveComboBox.getItems().isEmpty());
		} catch (TradistaTechnicalException _) {
			canGetCurve = false;
		}

		updateWindow();
	}

	private void buildCurve(ZeroCouponCurve curve) throws TradistaBusinessException {
		List<Quote> quotes = selectedQuotesList.getItems();
		if (quotes != null && !quotes.isEmpty()) {
			curve.setQuotes(quotes);
		}

		Map<LocalDate, BigDecimal> ratePoints = toRatePointsMap(pointsTable.getItems());
		curve.setPoints(ratePoints);

		curve.setQuoteSet(quoteSet.getValue());

		if (isGeneratedCheckBox.isSelected()) {
			curve.setInstance(instanceComboBox.getValue());
			curve.setInterpolator(interpolatorComboBox.getValue());
			curve.setAlgorithm(algorithmComboBox.getValue());
		}
		curve.setQuoteDate(quoteDate.getValue());
	}

	@SuppressWarnings("unchecked")
	@FXML
	protected void save() {
		try {
			boolean isNew = (curve.getId() == 0);
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin() && isNew) {
				TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog("Zero Coupon Curve",
						ClientUtil.getCurrentProcessingOrg(), false);
				Optional<LegalEntity> result = dialog.showAndWait();
				if (result.isPresent()) {
					po = result.get();
					proceed = true;
				}
			} else {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Save Zero Coupon Curve");
				confirmation.setHeaderText("Save Zero Coupon Curve");
				confirmation.setContentText("Do you want to save this Zero Coupon Curve?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					proceed = true;
					po = curve.getProcessingOrg();
				}
			}

			if (proceed) {
				if (isNew) {
					curve = new ZeroCouponCurve(curve.getName(), po);
				}
				buildCurve((ZeroCouponCurve) curve);
				curve.setId(interestRateCurveBusinessDelegate.saveInterestRateCurve((InterestRateCurve) curve));
				try {
					TradistaGUIUtil
							.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
				} catch (TradistaTechnicalException tte) {
					canSaveCurve = false;
					throw tte;
				}
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@SuppressWarnings("unchecked")
	@FXML
	protected void copy() {
		try {
			String copyName = null;
				LegalEntity po = null;
				boolean proceed = false;

				if (ClientUtil.currentUserIsAdmin()) {
					TradistaCopyDialog dialog = new TradistaCopyDialog("Zero Coupon Curve", curve.getProcessingOrg(),
							curve.getName(), false);
					Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
					if (result.isPresent()) {
						copyName = result.get().getName();
						po = result.get().getProcessingOrg();
						proceed = true;
					}
				} else {
					TradistaTextInputDialog dialog = new TradistaTextInputDialog();
					dialog.setTitle("Curve name");
					dialog.setHeaderText("Curve name selection");
					dialog.setContentText("Please choose a Curve name:");

					Optional<String> result = dialog.showAndWait();
					if (result.isPresent()) {
						copyName = result.get();
						po = ClientUtil.getCurrentUser().getProcessingOrg();
						proceed = true;
					}
				}

				if (proceed) {
					ZeroCouponCurve copyCurve = new ZeroCouponCurve(copyName, po);
					buildCurve(copyCurve);
					try {
						copyCurve.setId(interestRateCurveBusinessDelegate.saveInterestRateCurve(copyCurve));
					} catch (TradistaTechnicalException tte) {
						canCopyCurve = false;
						throw tte;
					}
					curve = copyCurve;
					TradistaGUIUtil
							.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
				}
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
				alert.showAndWait();
			}
		}

	@SuppressWarnings("unchecked")
	@FXML
	protected void delete() {
		try {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Zero Coupon Curve");
			confirmation.setHeaderText("Delete Zero Coupon Curve");
			confirmation.setContentText("Do you want to delete this Zero Coupon Curve?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				try {
					interestRateCurveBusinessDelegate.deleteInterestRateCurve(curve.getId());
				} catch (TradistaTechnicalException tte) {
					canDeleteCurve = false;
					throw tte;
				}
				curve = null;
				TradistaGUIUtil.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
			}

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void include() {
		selectedQuotesList.getItems().addAll(quotesList.getSelectionModel().getSelectedItems());
		quotesList.getItems().removeAll(quotesList.getSelectionModel().getSelectedItems());
	}

	@FXML
	protected void exclude() {
		quotesList.getItems().addAll(selectedQuotesList.getSelectionModel().getSelectedItems());
		selectedQuotesList.getItems().removeAll(selectedQuotesList.getSelectionModel().getSelectedItems());
	}

	@FXML
	protected void search() {
		String search = quoteNameTextField.getText();
		List<Quote> quotes = quoteBusinessDelegate.getQuotesByName(search);
		if (quotes != null && !quotes.isEmpty() && !selectedQuotesList.getItems().isEmpty()) {
			List<Quote> notAlreadySelectedQuotes = new ArrayList<>();
			Set<String> productTypes = interestRateCurveBusinessDelegate.getBootstrapableProductTypes();
			for (Quote quote : quotes) {
				String product = quote.getName().split("//.")[0];
				if (!selectedQuotesList.getItems().contains(quote) && productTypes.contains(product)) {
					notAlreadySelectedQuotes.add(quote);
				}
			}
			quotes = notAlreadySelectedQuotes;
		}
		if (quotes != null) {
			quotesList.setItems(FXCollections.observableArrayList(quotes));
		}
	}

	@FXML
	protected void generate() {
		List<Long> quoteIds = toQuoteIdList(selectedQuotesList.getItems());
		List<RatePoint> ratePoints;
		try {
			try {
				ratePoints = interestRateCurveBusinessDelegate.generate(algorithmComboBox.getValue(),
						interpolatorComboBox.getValue(), instanceComboBox.getValue(), quoteDate.getValue(),
						quoteSet.getValue(), quoteIds);
			} catch (TradistaTechnicalException tte) {
				canGenerateCurve = false;
				throw tte;
			}
			// Update the points table
			pointsTable.setItems(FXCollections.observableArrayList(toRatePointPropertyList(ratePoints)));
			pointsTable.refresh();
			// Update the graph
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName(curveComboBox.getSelectionModel().getSelectedItem().getName());

			long lowerBound = ratePoints.getFirst().getDate().toEpochDay();
			for (RatePoint point : ratePoints) {
				if (point.getRate() != null) {
					series.getData()
							.add(new XYChart.Data<>(point.getDate().toEpochDay() - lowerBound, point.getRate()));
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
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@SuppressWarnings("unchecked")
	@FXML
	protected void create() {
		try {
			ZeroCouponCurveCreatorDialog dialog = new ZeroCouponCurveCreatorDialog();
			Optional<ZeroCouponCurve> result = dialog.showAndWait();

			if (result.isPresent()) {
				ZeroCouponCurve zcCurve = result.get();
				try {
					interestRateCurveBusinessDelegate.saveInterestRateCurve(zcCurve);
				} catch (TradistaTechnicalException tte) {
					canCreateCurve = false;
					throw tte;
				}
				TradistaGUIUtil.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	class EditingCell extends TableCell<RatePointProperty, String> {

		private TextField textField;

		public EditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && !StringUtils.isEmpty(textField.getText())) {
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
			textField.focusedProperty().addListener((_, _, isFocused) -> {
				if (Boolean.FALSE.equals(isFocused)) {
					commitEdit(textField.getText());
				}
			});

		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private List<RatePointProperty> toRatePointPropertyList(List<RatePoint> data) {
		List<RatePointProperty> ratePointPropertyList = new ArrayList<>();
		if (data != null) {
			for (RatePoint point : data) {
				ratePointPropertyList.add(new RatePointProperty(
						DateTimeFormatter.ofPattern(YYYY_MM_DD).format(point.getDate()),
						point.getRate() == null ? StringUtils.EMPTY : TradistaGUIUtil.formatAmount(point.getRate())));
			}
		}
		return ratePointPropertyList;
	}

	private Map<LocalDate, BigDecimal> toRatePointsMap(List<RatePointProperty> data) throws TradistaBusinessException {
		Map<LocalDate, BigDecimal> ratePointsMap = new HashMap<>();
		for (RatePointProperty point : data) {
			try {
				ratePointsMap.put(
						LocalDate.from(DateTimeFormatter.ofPattern(YYYY_MM_DD).parse(point.getDate().getValue())),
						point.getRate().getValue().isEmpty() ? null
								: TradistaGUIUtil.parseAmount(point.getRate().getValue(), "Rate"));
			} catch (DateTimeParseException dtpe) {
				logger.warn(dtpe.getMessage());
			}
		}

		return ratePointsMap;
	}

	private List<Long> toQuoteIdList(List<Quote> data) {
		List<Long> idList = new ArrayList<>();
		for (Quote quote : data) {
			idList.add(quote.getId());
		}

		return idList;
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

	@SuppressWarnings("unchecked")
	@Override
	@FXML
	public void refresh() {
		super.refresh();
		try {
			TradistaGUIUtil.fillZeroCouponCurveComboBox((ComboBox<ZeroCouponCurve>) (ComboBox<?>) curveComboBox);
			canGetCurve = true;
			canSaveCurve = true;
			canCopyCurve = true;
			canGenerateCurve = true;
			canDeleteCurve = true;
			canCreateCurve = true;
			curveExists = (curveComboBox.getItems() != null && !curveComboBox.getItems().isEmpty());
		} catch (TradistaTechnicalException _) {
			canGetCurve = false;
			canSaveCurve = false;
			canCopyCurve = false;
			canGenerateCurve = false;
			canDeleteCurve = false;
			canCreateCurve = false;
		}
		if (!canGetGenerationAlgorithms) {
			try {
				TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllGenerationAlgorithms(),
						algorithmComboBox);
				canGetGenerationAlgorithms = true;
			} catch (TradistaTechnicalException _) {
				canGetGenerationAlgorithms = false;
			}
		}
		if (!canGetInterpolators) {
			try {
				TradistaGUIUtil.fillComboBox(interestRateCurveBusinessDelegate.getAllInterpolators(),
						interpolatorComboBox);
				canGetInterpolators = true;
			} catch (TradistaTechnicalException _) {
				canGetInterpolators = false;
			}
		}
		updateWindow();
	}

	@Override
	public void updateComponents() {
		super.updateComponents();
		excludeButton.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		includeButton.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		quoteNameTextField.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		searchButton.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetQuote || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
	}

}