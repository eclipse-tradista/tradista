package finance.tradista.fx.fxoption.ui.controller;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.ui.util.TradistaGUIUtil;
import finance.tradista.core.common.ui.view.TradistaAlert;
import finance.tradista.core.common.ui.view.TradistaTextInputDialog;
import finance.tradista.core.common.util.ClientUtil;
import finance.tradista.core.marketdata.model.Quote;
import finance.tradista.core.marketdata.model.SurfacePoint;
import finance.tradista.core.marketdata.ui.controller.TradistaVolatilitySurfaceController;
import finance.tradista.fx.fxoption.model.FXOptionTrade;
import finance.tradista.fx.fxoption.model.FXVolatilitySurface;
import finance.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;
import finance.tradista.fx.fxoption.ui.view.FXVolatilitySurfaceCreatorDialog;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/*
 * Copyright 2016 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class FXVolatilitySurfacesController extends TradistaVolatilitySurfaceController {

	@FXML
	private TableView<SurfacePointProperty> pointsTable;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointOptionExpiry;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointDelta;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointVolatility;

	@FXML
	private ComboBox<FXVolatilitySurface> volatilitySurface;

	@FXML
	private Button saveButton;

	@FXML
	private Button generateButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button includeButton;

	@FXML
	private Button excludeButton;

	@FXML
	private CheckBox isGeneratedCheckBox;

	@FXML
	private ComboBox<String> algorithmComboBox;

	@FXML
	private ComboBox<String> interpolatorComboBox;

	@FXML
	private ComboBox<String> instanceComboBox;

	@FXML
	private DatePicker quoteDate;

	@FXML
	private Button addDelta;

	@FXML
	private Button removeDelta;

	@FXML
	private ListView<String> selectedDeltas;

	@FXML
	private TextField deltaToAdd;

	private FXVolatilitySurfaceBusinessDelegate fxVolatilitySurfaceBusinessDelegate;

	private TextField optionExpiryTextField = new TextField();

	private TextField deltaTextField = new TextField();

	private TextField volatilityTextField = new TextField();

	private FXVolatilitySurface surface;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();
		fxVolatilitySurfaceBusinessDelegate = new FXVolatilitySurfaceBusinessDelegate();

		Callback<TableColumn<SurfacePointProperty, String>, TableCell<SurfacePointProperty, String>> stringCellFactory = new Callback<TableColumn<SurfacePointProperty, String>, TableCell<SurfacePointProperty, String>>() {
			public TableCell<SurfacePointProperty, String> call(TableColumn<SurfacePointProperty, String> p) {
				return new StringEditingCell();
			}
		};

		pointOptionExpiry.setCellValueFactory(new PropertyValueFactory<SurfacePointProperty, String>("optionExpiry"));

		pointDelta.setCellValueFactory(new PropertyValueFactory<SurfacePointProperty, String>("delta"));

		pointVolatility.setCellFactory(stringCellFactory);

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

		pointVolatility.setCellValueFactory(new PropertyValueFactory<SurfacePointProperty, String>("volatility"));

		VBox optionExpiryGraphic = new VBox();
		Label optionExpiryLabel = new Label("Option Expiry");
		optionExpiryLabel.setMaxWidth(100);
		optionExpiryGraphic.setAlignment(Pos.CENTER);
		optionExpiryGraphic.getChildren().addAll(optionExpiryLabel, optionExpiryTextField);
		pointOptionExpiry.setGraphic(optionExpiryGraphic);

		VBox deltaGraphic = new VBox();
		Label deltaLabel = new Label("Delta");
		deltaLabel.setMaxWidth(100);
		deltaGraphic.setAlignment(Pos.CENTER);
		deltaGraphic.getChildren().addAll(deltaLabel, deltaTextField);
		pointDelta.setGraphic(deltaGraphic);

		VBox volatilityGraphic = new VBox();
		Label volatilityLabel = new Label("Volatility");
		volatilityLabel.setMaxWidth(100);
		volatilityGraphic.setAlignment(Pos.CENTER);
		volatilityGraphic.getChildren().addAll(volatilityLabel, volatilityTextField);
		pointVolatility.setGraphic(volatilityGraphic);

		optionExpiryTextField.setMaxWidth(100);
		deltaTextField.setMaxWidth(100);
		volatilityTextField.setMaxWidth(100);

		volatilitySurface.valueProperty().addListener(new ChangeListener<FXVolatilitySurface>() {
			@Override
			public void changed(ObservableValue<? extends FXVolatilitySurface> ov, FXVolatilitySurface oldSurf,
					FXVolatilitySurface surf) {
				if (surf != null) {
					surface = surf;
					isGeneratedCheckBox.setSelected(surf.isGenerated());
					interpolatorComboBox.setValue(surf.getInterpolator());
					algorithmComboBox.setValue(surf.getAlgorithm());
					instanceComboBox.setValue(surf.getInstance());
					selectedDeltas.setItems(
							FXCollections.observableArrayList(TradistaGUIUtil.formatAmounts(surf.getDeltas())));

					List<SurfacePointProperty> properties = null;
					try {
						properties = buildTableContent(surf.getPoints());
					} catch (TradistaBusinessException tbe) {
						TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
						alert.showAndWait();
					}

					// 1. Wrap the ObservableList in a FilteredList
					// (initially display all
					// data).
					FilteredList<SurfacePointProperty> filteredData = new FilteredList<>(
							FXCollections.observableArrayList(properties));

					// 3. Wrap the FilteredList in a SortedList.
					SortedList<SurfacePointProperty> sortedData = new SortedList<>(filteredData);

					// 4. Bind the SortedList comparator to the
					// TableView
					// comparator.
					sortedData.comparatorProperty().bind(pointsTable.comparatorProperty());

					// 2. Set the filter Predicate whenever the filter
					// changes.
					optionExpiryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty,
							// display all
							// option expiries.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getOptionExpiry().toUpperCase().contains(newValue.toUpperCase());
						});
					});

					deltaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is
							// empty,
							// display all
							// option expiries.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getDelta().toUpperCase().contains(newValue.toUpperCase());
						});
					});

					volatilityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(point -> {
							// If filter text is empty, display
							// all
							// option expiries.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							return point.getVolatility().contains(newValue);
						});
					});

					pointsTable.setItems(sortedData);
					pointsTable.refresh();

					quoteDate.setValue(surf.getQuoteDate());
					quotesList.getItems().clear();
					if (surf.getQuotes() != null) {
						selectedQuotesList.setItems(FXCollections.observableArrayList(surf.getQuotes()));
					}
					quoteSet.setValue(surf.getQuoteSet());

				} else {
					surface = null;
					algorithmComboBox.getSelectionModel().clearSelection();
					interpolatorComboBox.getSelectionModel().clearSelection();
					instanceComboBox.getSelectionModel().clearSelection();
					selectedQuotesList.getItems().clear();
					quoteDate.setValue(null);
					selectedDeltas.getItems().clear();
					pointsTable.setItems(null);
				}
			}

		});

		TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllGenerationAlgorithms(),
				algorithmComboBox);
		TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllInterpolators(), interpolatorComboBox);
		TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllInstances(), instanceComboBox);
		TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
				volatilitySurface);

	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
				volatilitySurface);
	}

	private void buildSurface() throws TradistaBusinessException {
		if (surface == null) {
			surface = new FXVolatilitySurface();
		}
		surface.setQuotes(new ArrayList<Quote>(selectedQuotesList.getItems()));

		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = toSurfacePointList(pointsTable.getItems());
		surface.setPoints(surfacePoints);

		surface.setQuoteSet(quoteSet.getValue());

		List<BigDecimal> deltas = TradistaGUIUtil.parseAmounts(selectedDeltas.getItems(), "Delta");
		surface.setDeltas(deltas);
		if (isGeneratedCheckBox.isSelected()) {
			surface.setInstance(instanceComboBox.getValue());
			surface.setInterpolator(interpolatorComboBox.getValue());
			surface.setAlgorithm(algorithmComboBox.getValue());
		}
		if (volatilitySurface.getValue() != null) {
			surface.setName(this.volatilitySurface.getValue().getName());
		}
		surface.setProcessingOrg(ClientUtil.getCurrentUser().getProcessingOrg());
		surface.setQuoteDate(quoteDate.getValue());

	}

	@FXML
	protected void save() {
		try {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Save FX Volatility Surface");
			confirmation.setHeaderText("Save FX Volatility Surface");
			confirmation.setContentText("Do you want to save this FX Volatility Surface?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				buildSurface();
				surface.setId(fxVolatilitySurfaceBusinessDelegate.saveFXVolatilitySurface(surface));
				TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
						volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		long oldSurfaceId = 0;
		boolean surfaceLoaded = (surface != null);
		if (!surfaceLoaded) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "Please select a volatility Surface.");
			alert.showAndWait();
		} else {
			try {
				buildSurface();
				oldSurfaceId = surface.getId();
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("Surface name");
				dialog.setHeaderText("Surface name selection");
				dialog.setContentText("Please choose a Surface name:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				// The Java 8 way to get the response value (with lambda
				// expression).
				result.ifPresent(name -> surface.setName(name));
				if (result.isPresent()) {
					surface.setId(0);
					surface.setId(fxVolatilitySurfaceBusinessDelegate.saveFXVolatilitySurface(surface));
					TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
							volatilitySurface);
				}
			} catch (TradistaBusinessException tbe) {
				surface.setId(oldSurfaceId);
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void delete() {
		try {
			if (surface == null) {
				throw new TradistaBusinessException("Please select a FX Volatility Surface");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete FX Volatility Surface");
			confirmation.setHeaderText("Delete FX Volatility Surface");
			confirmation.setContentText("Do you want to delete this FX Volatility Surface?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fxVolatilitySurfaceBusinessDelegate.deleteFXVolatilitySurface(surface.getId());
				surface = null;
				TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
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
			search = FXOptionTrade.FX_OPTION + ".%" + search.replaceAll("%", "") + "%";
		}
		fillQuoteNames(search);
	}

	@FXML
	protected void addDelta() {
		try {
			BigDecimal delta = TradistaGUIUtil.parseAmount(deltaToAdd.getText(), "Delta");
			boolean deltaExists = false;
			if (selectedDeltas.getItems() != null && !selectedDeltas.getItems().isEmpty()) {
				for (BigDecimal d : TradistaGUIUtil.parseAmounts(selectedDeltas.getItems(), "Delta")) {
					if (d.compareTo(delta) == 0) {
						deltaExists = true;
						break;
					}
				}
			}
			if (!deltaExists) {
				selectedDeltas.getItems().add(deltaToAdd.getText());
				List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(pointsTable.getItems());
				List<SurfacePointProperty> properties = buildTableContent(points);
				pointsTable.setItems(FXCollections.observableArrayList(properties));
				pointsTable.refresh();
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void removeDelta() {
		try {
			if (pointsTable.getItems() != null && !pointsTable.getItems().isEmpty()) {
				for (SurfacePointProperty prop : pointsTable.getItems()) {
					if (TradistaGUIUtil.parseAmount(prop.getDelta(), "Delta").compareTo(TradistaGUIUtil
							.parseAmount(selectedDeltas.getSelectionModel().getSelectedItem(), "Delta")) == 0) {
						if (!StringUtils.isEmpty(prop.getVolatility())) {
							TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
							confirmation.setTitle("Remove Delta");
							confirmation.setHeaderText("Remove Delta");
							confirmation.setContentText(
									"Some volatilities exist for this delta. Do you really want to remove it?");

							Optional<ButtonType> result = confirmation.showAndWait();
							if (result.get() == ButtonType.OK) {
								String deltaToBeRemoved = selectedDeltas.getSelectionModel().getSelectedItem();
								selectedDeltas.getItems().remove(deltaToBeRemoved);
								List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(
										pointsTable.getItems());
								List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toBeRemoved = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
								if (!points.isEmpty()) {
									for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
										if (p.getyAxis().compareTo(
												TradistaGUIUtil.parseAmount(deltaToBeRemoved, "Delta")) == 0) {
											toBeRemoved.add(p);
										}
									}
								}
								points.removeAll(toBeRemoved);
								List<SurfacePointProperty> properties = buildTableContent(points);
								pointsTable.setItems(FXCollections.observableArrayList(properties));
								pointsTable.refresh();
							}
							return;
						}
					}
				}
			}
			String deltaToBeRemoved = selectedDeltas.getSelectionModel().getSelectedItem();
			selectedDeltas.getItems().remove(deltaToBeRemoved);
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(pointsTable.getItems());
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toBeRemoved = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
			if (!points.isEmpty()) {
				for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
					if (p.getyAxis().compareTo(TradistaGUIUtil.parseAmount(deltaToBeRemoved, "Delta")) == 0) {
						toBeRemoved.add(p);
					}
				}
			}
			points.removeAll(toBeRemoved);
			List<SurfacePointProperty> properties = buildTableContent(points);
			pointsTable.setItems(FXCollections.observableArrayList(properties));
			pointsTable.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void generate() {
		List<String> quoteNames = toQuoteStringList(selectedQuotesList.getItems());
		try {
			List<BigDecimal> deltas = TradistaGUIUtil.parseAmounts(selectedDeltas.getItems(), "Delta");
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints;
			surfacePoints = fxVolatilitySurfaceBusinessDelegate.generate(algorithmComboBox.getValue(),
					interpolatorComboBox.getValue(), instanceComboBox.getValue(), quoteDate.getValue(),
					quoteSet.getValue(), quoteNames, deltas);
			// Update the points table
			pointsTable.setItems(FXCollections.observableArrayList(toSurfacePointPropertyList(surfacePoints)));
			pointsTable.refresh();

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void create() {
		try {
			FXVolatilitySurfaceCreatorDialog dialog = new FXVolatilitySurfaceCreatorDialog();
			Optional<FXVolatilitySurface> result = dialog.showAndWait();

			if (result.isPresent()) {
				FXVolatilitySurface surface = result.get();
				fxVolatilitySurfaceBusinessDelegate.saveFXVolatilitySurface(surface);
				TradistaGUIUtil.fillComboBox(fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces(),
						volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	public static List<BigDecimal> toDeltaList(ObservableList<DeltaProperty> items) throws TradistaBusinessException {
		List<BigDecimal> deltaList = new ArrayList<BigDecimal>();
		for (DeltaProperty delta : items) {
			deltaList.add(TradistaGUIUtil.parseAmount(delta.getValue().toString(), "Delta"));
		}

		return deltaList;
	}

	class StringEditingCell extends TableCell<SurfacePointProperty, String> {

		private TextField textField;

		public StringEditingCell() {
		}

		@Override
		public void startEdit() {
			// if (!isEmpty()) {
			if (textField != null && textField.getText() != null && !textField.getText().equals("")) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
			// }
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

	private List<SurfacePointProperty> buildTableContent(List<SurfacePoint<Integer, BigDecimal, BigDecimal>> data)
			throws TradistaBusinessException {
		if (data != null) {
			Collection<Number> optionExpiries = fxVolatilitySurfaceBusinessDelegate.getAllOptionExpiries();
			List<BigDecimal> deltas = TradistaGUIUtil.parseAmounts(selectedDeltas.getItems(), "Delta");

			for (Number optionExpiry : optionExpiries) {
				for (BigDecimal delta : deltas) {
					SurfacePoint<Integer, BigDecimal, BigDecimal> point = new SurfacePoint<Integer, BigDecimal, BigDecimal>(
							(Integer) optionExpiry, delta, null);
					if (!data.contains(point)) {
						data.add(point);
					}
				}
			}

			Collections.sort(data);

			return FXCollections.observableArrayList(toSurfacePointPropertyList(data));
		} else
			return FXCollections.emptyObservableList();
	}

	private List<SurfacePointProperty> toSurfacePointPropertyList(
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> data) {
		List<SurfacePointProperty> surfacePointPropertyList = new ArrayList<SurfacePointProperty>();
		for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : data) {
			String optionExpiry = toPeriodString(point.getxAxis());
			String delta = TradistaGUIUtil.formatAmount(point.getyAxis());
			String volatility = point.getzAxis() == null ? "" : TradistaGUIUtil.formatAmount(point.getzAxis());
			if (!optionExpiry.isEmpty()) {
				surfacePointPropertyList.add(new SurfacePointProperty(optionExpiry, delta, volatility));
			}
		}

		return surfacePointPropertyList;
	}

	private List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toSurfacePointList(List<SurfacePointProperty> data)
			throws TradistaBusinessException {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePointList = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
		if (data != null && !data.isEmpty()) {
			for (SurfacePointProperty point : data) {
				try {
					String optionExpiry = point.getOptionExpiry();
					if (!optionExpiry.isEmpty()) {
						String volatility = point.getVolatility();
						if (volatility != null && !volatility.equals("")) {
							surfacePointList.add(new SurfacePoint<Integer, BigDecimal, BigDecimal>(
									fxVolatilitySurfaceBusinessDelegate.getOptionExpiryValue(optionExpiry),
									// toPeriodLong(optionExpiry),
									TradistaGUIUtil.parseAmount(point.getDelta(), "Delta"),
									TradistaGUIUtil.parseAmount(point.getVolatility(), "Volatility")));
						}
					}
				} catch (DateTimeParseException dtpe) {
					// TODO Auto-generated catch block
					dtpe.printStackTrace();
				}
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

	private List<String> toQuoteStringList(List<Quote> data) {
		List<String> nameList = new ArrayList<String>();
		for (Quote quote : data) {
			nameList.add(quote.getName());
		}

		return nameList;
	}

	public static class SurfacePointProperty {

		private final SimpleStringProperty optionExpiry;
		private final SimpleStringProperty delta;
		private final SimpleStringProperty volatility;

		private SurfacePointProperty(String optionExpiry, String delta, String volatility) {
			this.optionExpiry = new SimpleStringProperty(optionExpiry);
			this.delta = new SimpleStringProperty(delta);
			this.volatility = new SimpleStringProperty(volatility);
		}

		public String getOptionExpiry() {
			return optionExpiry.get();
		}

		public void setOptionExpiry(String optionExpiry) {
			this.optionExpiry.set(optionExpiry);
		}

		public String getVolatility() {
			return volatility.get();
		}

		public void setVolatility(String volatility) {
			this.volatility.set(volatility);
		}

		public String getDelta() {
			return delta.get();
		}

		public void getDelta(String delta) {
			this.delta.set(delta);
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

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof QuoteProperty)) {
				return false;
			}

			return name.get().equals(((QuoteProperty) o).getName());
		}

		public int hashCode() {
			return name.hashCode();
		}
	}

	public static class DeltaProperty {

		private final StringProperty value;

		public DeltaProperty(String value) {
			this.value = new SimpleStringProperty(value);
		}

		public StringProperty getValue() {
			return value;
		}

		public void setValue(String name) {
			this.value.set(name);
		}

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof DeltaProperty)) {
				return false;
			}

			return (getValue().toString().compareTo(((DeltaProperty) o).getValue().toString()) == 0);
		}

		public int hashCode() {
			return value.hashCode();
		}
	}

}