package org.eclipse.tradista.ir.irswapoption.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.ui.controller.TradistaVolatilitySurfaceController;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;
import org.eclipse.tradista.ir.irswapoption.view.IRSwapOptionVolatilitySurfaceCreatorDialog;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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

public class IRSwapOptionVolatilitySurfacesController extends TradistaVolatilitySurfaceController {

	@FXML
	private TableView<SurfacePointProperty> pointsTable;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointOptionExpiry;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointSwapLength;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointVolatility;

	@FXML
	private ComboBox<SwaptionVolatilitySurface> volatilitySurface;

	private SwaptionVolatilitySurface surface;

	@FXML
	private TextField addTextField;

	@FXML
	private Button saveButton, generateButton, deleteButton, addButton, includeButton, excludeButton;

	@FXML
	private CheckBox isGeneratedCheckBox;

	@FXML
	private ComboBox<String> algorithmComboBox, interpolatorComboBox, instanceComboBox;

	@FXML
	private DatePicker quoteDate;

	private SwaptionVolatilitySurfaceBusinessDelegate swaptionVolatilitySurfaceBusinessDelegate;

	private TextField optionExpiryTextField = new TextField();

	private TextField swapLengthTextField = new TextField();

	private TextField volatilityTextField = new TextField();

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		swaptionVolatilitySurfaceBusinessDelegate = new SwaptionVolatilitySurfaceBusinessDelegate();

		Callback<TableColumn<SurfacePointProperty, String>, TableCell<SurfacePointProperty, String>> cellFactory = new Callback<TableColumn<SurfacePointProperty, String>, TableCell<SurfacePointProperty, String>>() {
			public TableCell<SurfacePointProperty, String> call(TableColumn<SurfacePointProperty, String> p) {
				return new EditingCell();
			}
		};

		pointOptionExpiry.setCellValueFactory(cellData -> cellData.getValue().getOptionExpiry());

		pointSwapLength.setCellValueFactory(cellData -> cellData.getValue().getSwapLength());

		pointVolatility.setCellFactory(cellFactory);

		pointVolatility.setOnEditCommit(new EventHandler<CellEditEvent<SurfacePointProperty, String>>() {
			@Override
			public void handle(CellEditEvent<SurfacePointProperty, String> t) {
				try {
					TradistaGUIUtil.parseAmount(t.getNewValue(), "Volatility");
					((SurfacePointProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
							.setVolatility(t.getNewValue());
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
				pointsTable.refresh();
			}
		});

		pointVolatility.setCellValueFactory(cellData -> cellData.getValue().getVolatility());

		VBox optionExpiryGraphic = new VBox();
		Label optionExpiryLabel = new Label("Option Expiry");
		optionExpiryLabel.setMaxWidth(100);
		optionExpiryGraphic.setAlignment(Pos.CENTER);
		optionExpiryGraphic.getChildren().addAll(optionExpiryLabel, optionExpiryTextField);
		pointOptionExpiry.setGraphic(optionExpiryGraphic);

		VBox swapLengthGraphic = new VBox();
		Label swapLengthLabel = new Label("Swap Length");
		swapLengthLabel.setMaxWidth(100);
		swapLengthGraphic.setAlignment(Pos.CENTER);
		swapLengthGraphic.getChildren().addAll(swapLengthLabel, swapLengthTextField);
		pointSwapLength.setGraphic(swapLengthGraphic);

		VBox volatilityGraphic = new VBox();
		Label volatilityLabel = new Label("Volatility");
		volatilityLabel.setMaxWidth(100);
		volatilityGraphic.setAlignment(Pos.CENTER);
		volatilityGraphic.getChildren().addAll(volatilityLabel, volatilityTextField);
		pointVolatility.setGraphic(volatilityGraphic);

		optionExpiryTextField.setMaxWidth(100);
		swapLengthTextField.setMaxWidth(100);
		volatilityTextField.setMaxWidth(100);

		quoteDate.setValue(LocalDate.now());

		volatilitySurface.valueProperty().addListener(new ChangeListener<SwaptionVolatilitySurface>() {
			@Override
			public void changed(ObservableValue<? extends SwaptionVolatilitySurface> ov,
					SwaptionVolatilitySurface oldSurf, SwaptionVolatilitySurface surf) {
				if (surf != null) {
					surface = surf;
					isGeneratedCheckBox.setSelected(surf.isGenerated());
					interpolatorComboBox.setValue(surf.getInterpolator());
					algorithmComboBox.setValue(surf.getAlgorithm());
					instanceComboBox.setValue(surf.getInstance());
					List<Quote> quotes = quoteBusinessDelegate.getQuotesByCurveId(surf.getId());
					if (quotes != null) {
						selectedQuotesList.setItems(FXCollections.observableArrayList(quotes));
					} else {
						selectedQuotesList.setItems(FXCollections.emptyObservableList());
					}
					List<SurfacePoint<Integer, Integer, BigDecimal>> surfacePoints = swaptionVolatilitySurfaceBusinessDelegate
							.getSwaptionVolatilitySurfacePointsBySurfaceId(surf.getId());
					List<SurfacePointProperty> properties = buildTableContent(surfacePoints);
					// 1. Wrap the ObservableList in a FilteredList
					// (initially display all
					// data).
					FilteredList<SurfacePointProperty> filteredData = new FilteredList<>(
							FXCollections.observableArrayList(properties));

					// 3. Wrap the FilteredList in a SortedList.
					SortedList<SurfacePointProperty> sortedData = new SortedList<>(filteredData);

					// 4. Bind the SortedList comparator to the
					// TableView comparator.
					sortedData.comparatorProperty().bind(pointsTable.comparatorProperty());

					// 2. Set the filter Predicate whenever the filter
					// changes.
					optionExpiryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty, display
							// all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getOptionExpiry().toString().toUpperCase().contains(newValue.toUpperCase());
						});
					});

					swapLengthTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty, display
							// all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getSwapLength().toString().toUpperCase().contains(newValue.toUpperCase());
						});
					});

					volatilityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is empty, display
							// all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getVolatility().getValue().contains(newValue);
						});
					});

					pointsTable.setItems(sortedData);
					pointsTable.refresh();

					quoteDate.setValue(surf.getQuoteDate());
					quotesList.getItems().clear();
					quoteSet.setValue(surf.getQuoteSet());
					if (surf.getQuotes() != null) {
						selectedQuotesList.setItems(FXCollections.observableArrayList(surf.getQuotes()));
					}
				} else {
					algorithmComboBox.getSelectionModel().clearSelection();
					interpolatorComboBox.getSelectionModel().clearSelection();
					instanceComboBox.getSelectionModel().clearSelection();
					selectedQuotesList.getItems().clear();
					quoteDate.setValue(null);
					pointsTable.setItems(null);
				}
			}
		});

		TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllGenerationAlgorithms(),
				algorithmComboBox);
		TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllInterpolators(),
				interpolatorComboBox);
		TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllInstances(), instanceComboBox);
		TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
				volatilitySurface);
	}

	private void buildSurface(SwaptionVolatilitySurface surface) throws TradistaBusinessException {
		if (surface == null) {
			String name = null;
			if (volatilitySurface.getValue() != null) {
				name = this.volatilitySurface.getValue().getName();
			}
			surface = new SwaptionVolatilitySurface(name, ClientUtil.getCurrentUser().getProcessingOrg());
		}
		surface.setQuotes(new ArrayList<Quote>(selectedQuotesList.getItems()));

		List<SurfacePoint<Integer, Integer, BigDecimal>> surfacePoints = toSurfacePointList(pointsTable.getItems());
		surface.setPoints(surfacePoints);

		surface.setQuoteSet(quoteSet.getValue());

		if (isGeneratedCheckBox.isSelected()) {
			surface.setInstance(instanceComboBox.getValue());
			surface.setInterpolator(interpolatorComboBox.getValue());
			surface.setAlgorithm(algorithmComboBox.getValue());
		}
		surface.setQuoteDate(quoteDate.getValue());
	}

	@FXML
	protected void save() {
		try {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Save IRSwapOption Volatility Surface");
			confirmation.setHeaderText("Save IRSwapOption Volatility Surface");
			confirmation.setContentText("Do you want to save this IRSwapOption Volatility Surface?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				buildSurface(surface);
				surface.setId(swaptionVolatilitySurfaceBusinessDelegate.saveSwaptionVolatilitySurface(surface));
				TradistaGUIUtil.fillComboBox(
						swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
						volatilitySurface);

			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		boolean surfaceLoaded = (surface != null);
		if (!surfaceLoaded) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "Please select a volatility Surface.");
			alert.showAndWait();
		} else {
			try {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("Surface name");
				dialog.setHeaderText("Surface name selection");
				dialog.setContentText("Please choose a Surface name:");

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					SwaptionVolatilitySurface copySwaptionVolatilitySurface = new SwaptionVolatilitySurface(
							result.get(), ClientUtil.getCurrentUser().getProcessingOrg());
					buildSurface(copySwaptionVolatilitySurface);
					copySwaptionVolatilitySurface.setId(swaptionVolatilitySurfaceBusinessDelegate
							.saveSwaptionVolatilitySurface(copySwaptionVolatilitySurface));
					surface = copySwaptionVolatilitySurface;
					TradistaGUIUtil.fillComboBox(
							swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
							volatilitySurface);
				}
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void create() {
		try {
			IRSwapOptionVolatilitySurfaceCreatorDialog dialog = new IRSwapOptionVolatilitySurfaceCreatorDialog();
			Optional<SwaptionVolatilitySurface> result = dialog.showAndWait();

			if (result.isPresent()) {
				SwaptionVolatilitySurface surface = result.get();
				swaptionVolatilitySurfaceBusinessDelegate.saveSwaptionVolatilitySurface(surface);
				TradistaGUIUtil.fillComboBox(
						swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
						volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			if (surface == null) {
				throw new TradistaBusinessException("Please select a IRSwapOption Volatility Surface");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete IRSwapOption Volatility Surface");
			confirmation.setHeaderText("Delete IRSwapOption Volatility Surface");
			confirmation.setContentText("Do you want to delete this IRSwapOption Volatility Surface?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				swaptionVolatilitySurfaceBusinessDelegate.deleteSwaptionVolatilitySurface(surface.getId());
				surface = null;
				TradistaGUIUtil.fillComboBox(
						swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
						volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
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
		if (quoteNameTextField.getText() != null) {
			search = IRSwapOptionTrade.IR_SWAP_OPTION + ".%" + search.replaceAll("%", "") + "%";
		}
		fillQuoteNames(search);
	}

	@FXML
	protected void generate() {
		List<Long> quoteIds = toQuoteIdList(selectedQuotesList.getItems());
		List<SurfacePoint<Integer, Integer, BigDecimal>> surfacePoints;
		try {
			surfacePoints = swaptionVolatilitySurfaceBusinessDelegate.generate(algorithmComboBox.getValue(),
					interpolatorComboBox.getValue(), instanceComboBox.getValue(), quoteDate.getValue(),
					quoteSet.getValue(), quoteIds);
			// Update the points table
			pointsTable.setItems(FXCollections.observableArrayList(toSurfacePointPropertyList(surfacePoints)));
			pointsTable.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	class EditingCell extends TableCell<SurfacePointProperty, String> {

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

	private List<SurfacePointProperty> buildTableContent(List<SurfacePoint<Integer, Integer, BigDecimal>> data) {
		if (data != null) {
			Collection<Number> optionLifetimes = swaptionVolatilitySurfaceBusinessDelegate.getAllOptionExpiries();
			Collection<Number> swapLifetimes = swaptionVolatilitySurfaceBusinessDelegate.getAllSwapMaturities();

			for (Number optionLifetime : optionLifetimes) {
				for (Number swapLifetime : swapLifetimes) {
					SurfacePoint<Integer, Integer, BigDecimal> point = new SurfacePoint<Integer, Integer, BigDecimal>(
							(Integer) optionLifetime, (Integer) swapLifetime, null);
					if (!data.contains(point)) {
						data.add(point);
					}
				}
			}

			Collections.sort(data);

			return FXCollections.observableArrayList(toSurfacePointPropertyList(data));
		} else {
			return FXCollections.emptyObservableList();
		}
	}

	private List<SurfacePointProperty> toSurfacePointPropertyList(
			List<SurfacePoint<Integer, Integer, BigDecimal>> data) {
		List<SurfacePointProperty> surfacePointPropertyList = new ArrayList<SurfacePointProperty>();
		for (SurfacePoint<Integer, Integer, BigDecimal> point : data) {
			String optionExpiry = toPeriodString(point.getxAxis());
			String swapLength = toPeriodString(point.getyAxis());
			if (!optionExpiry.isEmpty() && !swapLength.isEmpty()) {
				surfacePointPropertyList.add(new SurfacePointProperty(optionExpiry, swapLength,
						point.getzAxis() == null ? "" : TradistaGUIUtil.formatAmount(point.getzAxis())));
			}
		}
		return surfacePointPropertyList;
	}

	private List<SurfacePoint<Integer, Integer, BigDecimal>> toSurfacePointList(List<SurfacePointProperty> data)
			throws TradistaBusinessException {
		List<SurfacePoint<Integer, Integer, BigDecimal>> surfacePointList = new ArrayList<SurfacePoint<Integer, Integer, BigDecimal>>();
		for (SurfacePointProperty point : data) {
			try {
				String optionExpiry = point.getOptionExpiry().getValue();
				String swapLength = point.getSwapLength().getValue();
				if (!optionExpiry.isEmpty() && !swapLength.isEmpty()) {
					surfacePointList.add(new SurfacePoint<Integer, Integer, BigDecimal>(
							toPeriodInteger(point.getOptionExpiry().getValue()),
							toPeriodInteger(point.getSwapLength().getValue()),
							point.getVolatility().getValue().isEmpty() ? null
									: TradistaGUIUtil.parseAmount(point.getVolatility().getValue(), "Volatility")));
				}
			} catch (DateTimeParseException dtpe) {
				// TODO Auto-generated catch block
				dtpe.printStackTrace();
			}
		}

		return surfacePointList;
	}

	private String toPeriodString(Number period) {
		String p = "";
		if (period.longValue() == (30)) {
			p = "1M";
		}
		if (period.longValue() == (91)) {
			p = "3M";
		}
		if (period.longValue() == (183)) {
			p = "6M";
		}
		if (period.longValue() == 365) {
			p = "1Y";
		}
		if (period.longValue() == 700) {
			p = "2Y";
		}
		if (period.longValue() == 1065) {
			p = "3Y";
		}
		if (period.longValue() == 1400) {
			p = "4Y";
		}
		if (period.longValue() == 1765) {
			p = "5Y";
		}
		if (period.longValue() == 2465) {
			p = "7Y";
		}
		if (period.longValue() == 3650) {
			p = "10Y";
		}

		return p;
	}

	private Integer toPeriodInteger(String period) {
		if (period == null) {
			return null;
		}
		switch (period) {
		case "1M":
			return Integer.valueOf(30);
		case "3M":
			return Integer.valueOf(91);
		case "6M":
			return Integer.valueOf(183);
		case "1Y":
			return Integer.valueOf(365);
		case "2Y":
			return Integer.valueOf(700);
		case "3Y":
			return Integer.valueOf(1065);
		case "4Y":
			return Integer.valueOf(1400);
		case "5Y":
			return Integer.valueOf(1765);
		case "7Y":
			return Integer.valueOf(2465);
		case "10Y":
			return Integer.valueOf(3650);
		}
		return null;
	}

	private List<Long> toQuoteIdList(List<Quote> data) {
		List<Long> idList = new ArrayList<Long>();
		for (Quote quote : data) {
			idList.add(quote.getId());
		}

		return idList;
	}

	public static class SurfacePointProperty {

		private final StringProperty optionExpiry;
		private final StringProperty swapLength;
		private final StringProperty volatility;

		private SurfacePointProperty(String optionExpiry, String swapLength, String volatility) {
			this.optionExpiry = new SimpleStringProperty(optionExpiry);
			this.swapLength = new SimpleStringProperty(swapLength);
			this.volatility = new SimpleStringProperty(volatility);
		}

		public StringProperty getOptionExpiry() {
			return optionExpiry;
		}

		public void setOptionExpiry(String optionExpiry) {
			this.optionExpiry.set(optionExpiry);
		}

		public StringProperty getVolatility() {
			return volatility;
		}

		public void setVolatility(String volatility) {
			this.volatility.set(volatility);
		}

		public StringProperty getSwapLength() {
			return swapLength;
		}

		public void getSwapLength(String swapLength) {
			this.swapLength.set(swapLength);
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
		TradistaGUIUtil.fillComboBox(swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces(),
				volatilitySurface);
	}

}