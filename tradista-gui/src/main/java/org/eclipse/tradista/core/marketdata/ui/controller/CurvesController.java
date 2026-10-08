package org.eclipse.tradista.core.marketdata.ui.controller;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaEditingCell;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.service.CurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.ui.view.CurveCreatorDialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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

public class CurvesController extends TradistaControllerAdapter {

	@FXML
	private TableView<RatePointProperty> pointsTable;

	@FXML
	private LineChart<Number, Number> pointsChart;

	@FXML
	private TableColumn<RatePointProperty, String> pointDate;

	@FXML
	private TableColumn<RatePointProperty, String> pointRate;

	@FXML
	private ComboBox<Curve<? extends LocalDate, ? extends BigDecimal>> curve;

	@FXML
	private ComboBox<Year> year;

	@FXML
	private ComboBox<Month> month;

	@FXML
	private Button deleteButton;

	@FXML
	private Button loadButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button createButton;

	@FXML
	private Label marketDataMessage;

	private CurveBusinessDelegate curveBusinessDelegate;

	private boolean canGetCurve = true;

	private boolean canDeleteCurve = true;

	private boolean curveExists = false;

	private boolean canGetCurvePoint = true;

	private boolean canSaveCurvePoint = true;

	private boolean canAddCurve = true;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		curveBusinessDelegate = new CurveBusinessDelegate();

		pointDate.setCellValueFactory(cellData -> cellData.getValue().getDate());

		pointRate.setCellFactory(_ -> new TradistaEditingCell<>());

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

		pointsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		List<Year> years = new ArrayList<>();

		for (int i = 1900; i < 2101; i++) {
			years.add(Year.of(i));
		}

		try {
			TradistaGUIUtil.fillCurveComboBox(curve);
			curveExists = (curve.getItems() != null && !curve.getItems().isEmpty());
		} catch (TradistaTechnicalException _) {
			canGetCurve = false;
		}

		year.setItems(FXCollections.observableArrayList(years));
		year.getSelectionModel().select(Year.now());

		month.setItems(FXCollections.observableArrayList(Month.values()));
		month.getSelectionModel().select(LocalDate.now().getMonth());

		updateWindow();
	}

	@FXML
	protected void load() {
		List<RatePoint> ratePoints;

		try {
			ratePoints = curveBusinessDelegate.getCurvePointsByCurveAndDate(curve.getValue(),
					year.getSelectionModel().getSelectedItem(), month.getSelectionModel().getSelectedItem());
			ObservableList<RatePointProperty> data = buildTableContent(ratePoints);
			pointsTable.setItems(data);
			pointsTable.refresh();

			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName(curve.getSelectionModel().getSelectedItem().getName() + StringUtils.SPACE
					+ month.getSelectionModel().getSelectedItem() + StringUtils.SPACE
					+ year.getSelectionModel().getSelectedItem());

			if (ratePoints != null) {
				int monthLength = ratePoints.getFirst().getDate().lengthOfMonth();
				for (RatePoint point : ratePoints) {
					if (point.getRate() != null) {
						series.getData().add(new XYChart.Data<>(point.getDate().getDayOfMonth(), point.getRate()));
					}
				}
				pointsChart.getXAxis().setAutoRanging(false);
				((ValueAxis<Number>) pointsChart.getXAxis()).setLowerBound(1);
				((ValueAxis<Number>) pointsChart.getXAxis()).setUpperBound(monthLength);
				((NumberAxis) pointsChart.getXAxis()).setTickUnit(1);
				((ValueAxis<Number>) pointsChart.getXAxis()).setMinorTickVisible(false);
				pointsChart.getXAxis().setLabel("Day");
				pointsChart.getYAxis().setLabel("Rate");
				pointsChart.setCreateSymbols(false);
			}
			pointsChart.getData().clear();
			pointsChart.getData().add(series);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	@FXML
	protected void save() {
		try {
			List<RatePoint> ratePoints = toRatePointList(pointsTable.getItems());
			curveBusinessDelegate.saveCurvePoints(curve.getValue(), ratePoints,
					year.getSelectionModel().getSelectedItem(), month.getSelectionModel().getSelectedItem());
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void create() {
		try {
			CurveCreatorDialog dialog = new CurveCreatorDialog();
			Optional<Curve<LocalDate, BigDecimal>> result = dialog.showAndWait();

			if (result.isPresent()) {
				Curve<LocalDate, BigDecimal> curveToBeSaved = result.get();
				curveBusinessDelegate.saveCurve(curveToBeSaved);
				Curve<? extends LocalDate, ? extends BigDecimal> selectedCurve = curve.getValue();
				TradistaGUIUtil.fillCurveComboBox(curve);
				if (selectedCurve == null || (selectedCurve.getId() != curve.getValue().getId())) {
					pointsTable.setItems(null);
				}
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			if (curve.getValue() == null) {
				throw new TradistaBusinessException("Please select a curve name.");
			}

			if (TradistaAlert.showConfirmationDialog("Delete Curve",
					String.format("Do you want to delete this Curve %s ?", curve.getValue().getName()))) {
				curveBusinessDelegate.deleteCurve(curve.getValue());
				Curve<? extends LocalDate, ? extends BigDecimal> selectedCurve = curve.getValue();
				TradistaGUIUtil.fillCurveComboBox(curve);
				if (selectedCurve == null || curve.getValue() == null
						|| (selectedCurve.getId() != curve.getValue().getId())) {
					pointsTable.setItems(null);
				}
			}

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	private ObservableList<RatePointProperty> buildTableContent(List<RatePoint> data) {

		if (data == null) {
			data = new ArrayList<>();
		} else {
			data = new ArrayList<>(data);
		}
		// Get the number of days in that month
		int daysInMonth = month.getValue().length(year.getValue().isLeap());

		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate cal = LocalDate.of(year.getSelectionModel().getSelectedItem().getValue(),
					month.getSelectionModel().getSelectedItem(), i);
			// If data doesn't contain a RatePoint at this date, put an empty value
			RatePoint point = new RatePoint(cal, null);

			if (!data.contains(point)) {
				data.add(point);
			}
		}

		Collections.sort(data);
		return FXCollections.observableArrayList(toRatePointPropertyList(data));
	}

	private List<RatePointProperty> toRatePointPropertyList(List<RatePoint> data) {
		List<RatePointProperty> ratePointPropertyList = new ArrayList<>();
		for (RatePoint point : data) {
			ratePointPropertyList.add(new RatePointProperty(
					DateTimeFormatter.ofPattern("yyyy-MM-dd").format(point.getDate()),
					point.getRate() == null ? StringUtils.EMPTY : TradistaGUIUtil.formatAmount(point.getRate())));
		}

		return ratePointPropertyList;
	}

	private List<RatePoint> toRatePointList(List<RatePointProperty> data) throws TradistaBusinessException {
		List<RatePoint> ratePointList = new ArrayList<>();
		for (RatePointProperty point : data) {
			try {
				ratePointList.add(new RatePoint(
						LocalDate.from(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(point.getDate().getValue())),
						point.getRate().getValue().isEmpty() ? null
								: TradistaGUIUtil.parseAmount(point.getRate().getValue(), "Rate")));
			} catch (DateTimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ratePointList;
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

	@Override
	@FXML
	public void refresh() {
		Curve<? extends LocalDate, ? extends BigDecimal> irCurve = curve.getValue();
		try {
			TradistaGUIUtil.fillCurveComboBox(curve);
			curveExists = (curve.getItems() != null && !curve.getItems().isEmpty());
			canGetCurve = true;
			canAddCurve = true;
			canDeleteCurve = true;
		} catch (TradistaTechnicalException _) {
			canGetCurve = false;
			canAddCurve = false;
			canDeleteCurve = false;
		}
		if (irCurve == null || !irCurve.equals(curve.getValue())) {
			pointsTable.setItems(null);
		}

		if (!canGetCurvePoint || !canSaveCurvePoint) {
			try {
				// Call only used to check connection to server and DB
				curveBusinessDelegate.getCurveById(1);
				canGetCurvePoint = true;
				canSaveCurvePoint = true;
			} catch (TradistaBusinessException _) {
			} catch (TradistaTechnicalException _) {
				canGetCurvePoint = false;
				canSaveCurvePoint = false;
			}
		}

		updateWindow();
	}

	protected void updateWindow() {
		List<String> errors = new ArrayList<>();
		String errMsg = "Cannot ";
		boolean isError = false;

		curve.setDisable(!curveExists || !canGetCurve || !canDeleteCurve);
		deleteButton.setDisable(!curveExists || !canGetCurve || !canDeleteCurve);
		loadButton.setDisable(!canGetCurvePoint || !canGetCurve || !curveExists || !canSaveCurvePoint);
		year.setDisable(!canGetCurvePoint || !curveExists || !canGetCurve || !canSaveCurvePoint);
		month.setDisable(!canGetCurvePoint || !curveExists || !canGetCurve || !canSaveCurvePoint);
		saveButton.setDisable(!canGetCurvePoint || !canGetCurve || !curveExists || !canSaveCurvePoint);
		createButton.setDisable(!canAddCurve || !canGetCurve);

		if (!curveExists) {
			TradistaGUIUtil.unapplyErrorStyle(marketDataMessage);
			TradistaGUIUtil.applyWarningStyle(marketDataMessage);
			marketDataMessage.setText("There is no quote set, please create one.");
		}

		if (!canGetCurve) {
			errors.add("get curves");
		}
		if (!canDeleteCurve) {
			errors.add(" delete curves");
		}
		if (!canGetCurvePoint) {
			errors.add("load curve points");
		}
		if (!canSaveCurvePoint) {
			errors.add("save curve points");
		}
		if (!canAddCurve) {
			errors.add("create curves");
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

		marketDataMessage.setVisible(isError || !curveExists);
	}

}