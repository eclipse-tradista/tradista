package org.eclipse.tradista.security.equityoption.ui.controller;

import static org.eclipse.tradista.core.common.ui.util.TradistaGUIConstants.VOLATILITY;
import static org.eclipse.tradista.security.equityoption.ui.util.EquityOptionUIConstants.STRIKE_PRICE_RATIO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.ui.controller.TradistaVolatilitySurfaceController;
import org.eclipse.tradista.security.common.ui.util.TradistaSecurityGUIUtil;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.service.EquityOptionVolatilitySurfaceBusinessDelegate;
import org.eclipse.tradista.security.equityoption.ui.view.EquityOptionVolatilitySurfaceCreatorDialog;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

public class EquityOptionVolatilitySurfacesController extends TradistaVolatilitySurfaceController {

	@FXML
	private TableView<SurfacePointProperty> pointsTable;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointOptionExpiry;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointStrike;

	@FXML
	private TableColumn<SurfacePointProperty, String> pointVolatility;

	@FXML
	private ComboBox<EquityOptionVolatilitySurface> volatilitySurface;

	@FXML
	private Button saveButton;

	@FXML
	private Button generateButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button copyButton;

	@FXML
	private Button addButton;

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
	private Button addStrike;

	@FXML
	private Button removeStrike;

	@FXML
	private ListView<String> selectedStrikes;

	@FXML
	private TextField strikeToAdd;

	private EquityOptionVolatilitySurfaceBusinessDelegate equityOptionVolatilitySurfaceBusinessDelegate;

	private TextField optionExpiryTextField = new TextField();

	private TextField strikeTextField = new TextField();

	private TextField volatilityTextField = new TextField();

	private EquityOptionVolatilitySurface surface;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		super.initialize();

		saveButton.setDisable(true);
		copyButton.setDisable(true);
		deleteButton.setDisable(true);
		equityOptionVolatilitySurfaceBusinessDelegate = new EquityOptionVolatilitySurfaceBusinessDelegate();

		Callback<TableColumn<SurfacePointProperty, String>, TableCell<SurfacePointProperty, String>> stringCellFactory = _ -> new StringEditingCell();

		pointOptionExpiry.setCellValueFactory(cellData -> cellData.getValue().getOptionExpiry());

		pointStrike.setCellValueFactory(cellData -> cellData.getValue().getStrike());

		pointVolatility.setCellFactory(stringCellFactory);

		pointVolatility.setOnEditCommit(t -> {
			try {
				TradistaGUIUtil.parseAmount(t.getNewValue(), VOLATILITY);
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setVolatility(t.getNewValue());
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		});

		pointVolatility.setCellValueFactory(cellData -> cellData.getValue().getVolatility());

		VBox optionExpiryGraphic = new VBox();
		Label optionExpiryLabel = new Label("Option Expiry");
		optionExpiryLabel.setMaxWidth(100);
		optionExpiryGraphic.setAlignment(Pos.CENTER);
		optionExpiryGraphic.getChildren().addAll(optionExpiryLabel, optionExpiryTextField);
		pointOptionExpiry.setGraphic(optionExpiryGraphic);

		VBox strikeGraphic = new VBox();
		Label strikeLabel = new Label("Strike");
		strikeLabel.setMaxWidth(100);
		strikeGraphic.setAlignment(Pos.CENTER);
		strikeGraphic.getChildren().addAll(strikeLabel, strikeTextField);
		pointStrike.setGraphic(strikeGraphic);

		VBox volatilityGraphic = new VBox();
		Label volatilityLabel = new Label(VOLATILITY);
		volatilityLabel.setMaxWidth(100);
		volatilityGraphic.setAlignment(Pos.CENTER);
		volatilityGraphic.getChildren().addAll(volatilityLabel, volatilityTextField);
		pointVolatility.setGraphic(volatilityGraphic);

		optionExpiryTextField.setMaxWidth(100);
		strikeTextField.setMaxWidth(100);
		volatilityTextField.setMaxWidth(100);

		quoteDate.setValue(LocalDate.now());

		volatilitySurface.valueProperty().addListener((_, _, newSurf) -> {
			if (newSurf != null) {
				surface = newSurf;
				isGeneratedCheckBox.setSelected(newSurf.isGenerated());
				interpolatorComboBox.setValue(newSurf.getInterpolator());
				algorithmComboBox.setValue(newSurf.getAlgorithm());
				instanceComboBox.setValue(newSurf.getInstance());
				saveButton.setDisable(false);
				copyButton.setDisable(false);
				deleteButton.setDisable(false);
				selectedStrikes.setItems(
						FXCollections.observableArrayList(TradistaGUIUtil.formatAmounts(newSurf.getStrikes())));

				List<SurfacePointProperty> properties = null;
				try {
					properties = buildTableContent(newSurf.getPoints());
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
				optionExpiryTextField.textProperty().addListener((_, _, newText) -> filteredData.setPredicate(point -> {
					// If filter text is
					// empty,
					// display all
					// persons.
					if (newText == null || newText.isEmpty()) {
						return true;
					}
					return point.getOptionExpiry().getValue().toUpperCase().contains(newText.toUpperCase());
				}));

				strikeTextField.textProperty().addListener((_, _, newText) -> filteredData.setPredicate(point -> {
					// If filter text is
					// empty,
					// display all
					// persons.
					if (newText == null || newText.isEmpty()) {
						return true;
					}
					return point.getStrike().getValue().toUpperCase().contains(newText.toUpperCase());
				}));

				volatilityTextField.textProperty().addListener((_, _, newText) -> filteredData.setPredicate(point -> {
					// If filter text is
					// empty,
					// display all
					// persons.
					if (newText == null || newText.isEmpty()) {
						return true;
					}
					return point.getVolatility().getValue().contains(newText);
				}));

				pointsTable.setItems(sortedData);
				pointsTable.refresh();

				quoteDate.setValue(newSurf.getQuoteDate());
				quotesList.getItems().clear();
				if (newSurf.getQuotes() != null) {
					selectedQuotesList.setItems(FXCollections.observableArrayList(newSurf.getQuotes()));
				}
				quoteSet.setValue(newSurf.getQuoteSet());
			} else {
				algorithmComboBox.getSelectionModel().clearSelection();
				interpolatorComboBox.getSelectionModel().clearSelection();
				instanceComboBox.getSelectionModel().clearSelection();
				selectedQuotesList.getItems().clear();
				quoteDate.setValue(null);
				selectedStrikes.getItems().clear();
				pointsTable.setItems(null);
			}
		});

		TradistaGUIUtil.fillComboBox(equityOptionVolatilitySurfaceBusinessDelegate.getAllGenerationAlgorithms(),
				algorithmComboBox);
		TradistaGUIUtil.fillComboBox(equityOptionVolatilitySurfaceBusinessDelegate.getAllInterpolators(),
				interpolatorComboBox);
		TradistaGUIUtil.fillComboBox(equityOptionVolatilitySurfaceBusinessDelegate.getAllInstances(), instanceComboBox);
		TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(volatilitySurface);
	}

	private void buildSurface(EquityOptionVolatilitySurface equityOptionVolatilitySurface)
			throws TradistaBusinessException {
		if (equityOptionVolatilitySurface == null) {
			String name = null;
			if (volatilitySurface.getValue() != null) {
				name = this.volatilitySurface.getValue().getName();
			}
			equityOptionVolatilitySurface = new EquityOptionVolatilitySurface(name,
					ClientUtil.getCurrentUser().getProcessingOrg());
		}
		equityOptionVolatilitySurface.setQuotes(new ArrayList<>(selectedQuotesList.getItems()));

		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = toSurfacePointList(pointsTable.getItems());
		equityOptionVolatilitySurface.setPoints(surfacePoints);

		equityOptionVolatilitySurface.setQuoteSet(quoteSet.getValue());

		List<BigDecimal> strikes = TradistaGUIUtil.parseAmounts(selectedStrikes.getItems(), STRIKE_PRICE_RATIO);
		equityOptionVolatilitySurface.setStrikes(strikes);
		if (isGeneratedCheckBox.isSelected()) {
			equityOptionVolatilitySurface.setInstance(instanceComboBox.getValue());
			equityOptionVolatilitySurface.setInterpolator(interpolatorComboBox.getValue());
			equityOptionVolatilitySurface.setAlgorithm(algorithmComboBox.getValue());
		}
		equityOptionVolatilitySurface.setQuoteDate(quoteDate.getValue());
	}

	@FXML
	protected void save() {
		try {
			boolean isNew = (surface.getId() == 0);
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin() && isNew) {
				TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog(
						"Equity Option Volatility Surface", ClientUtil.getCurrentProcessingOrg(), false);
				Optional<LegalEntity> result = dialog.showAndWait();
				if (result.isPresent()) {
					po = result.get();
					proceed = true;
				}
			} else {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Save EquityOption Volatility Surface");
				confirmation.setHeaderText("Save EquityOption Volatility Surface");
				confirmation.setContentText("Do you want to save this EquityOption Volatility Surface?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					proceed = true;
					po = surface.getProcessingOrg();
				}
			}

			if (proceed) {
				if (isNew) {
					surface = new EquityOptionVolatilitySurface(surface.getName(), po);
				}
				buildSurface(surface);
				surface.setId(equityOptionVolatilitySurfaceBusinessDelegate.saveEquityOptionVolatilitySurface(surface));
				// Refresh of the surfaces combobox
				TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		try {
			String copyName = null;
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin()) {
				TradistaCopyDialog dialog = new TradistaCopyDialog("Equity Option Volatility Surface",
						surface.getProcessingOrg(), surface.getName(), false);
				Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get().getName();
					po = result.get().getProcessingOrg();
					proceed = true;
				}
			} else {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("Surface name");
				dialog.setHeaderText("Surface name selection");
				dialog.setContentText("Please choose a Surface name:");

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get();
					po = ClientUtil.getCurrentUser().getProcessingOrg();
					proceed = true;
				}
			}

			if (proceed) {
				EquityOptionVolatilitySurface copyEquityOptionVolatilitySurface = new EquityOptionVolatilitySurface(
						copyName, po);
				buildSurface(copyEquityOptionVolatilitySurface);
				copyEquityOptionVolatilitySurface.setId(equityOptionVolatilitySurfaceBusinessDelegate
						.saveEquityOptionVolatilitySurface(copyEquityOptionVolatilitySurface));
				surface = copyEquityOptionVolatilitySurface;
				// Refresh of the surfaces combobox
				TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete EquityOption Volatility Surface");
			confirmation.setHeaderText("Delete EquityOption Volatility Surface");
			confirmation.setContentText("Do you want to delete this EquityOption Volatility Surface?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				equityOptionVolatilitySurfaceBusinessDelegate.deleteEquityOptionVolatilitySurface(surface.getId());
				surface = null;
				TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(volatilitySurface);
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
			search = EquityOption.EQUITY_OPTION + ".%" + search.replace("%", "") + "%";
		}
		fillQuoteNames(search);
	}

	@FXML
	protected void create() {
		try {
			EquityOptionVolatilitySurfaceCreatorDialog dialog = new EquityOptionVolatilitySurfaceCreatorDialog();
			Optional<EquityOptionVolatilitySurface> result = dialog.showAndWait();

			if (result.isPresent()) {
				EquityOptionVolatilitySurface surface = result.get();
				equityOptionVolatilitySurfaceBusinessDelegate.saveEquityOptionVolatilitySurface(surface);
				TradistaSecurityGUIUtil.fillEquityOptionVolatilitySurfaceComboBox(volatilitySurface);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addStrike() {
		try {
			BigDecimal strike = TradistaGUIUtil.parseAmount(strikeToAdd.getText(), STRIKE_PRICE_RATIO);
			if (strike == null) {
				throw new TradistaBusinessException(String.format("The %s is mandatory.", STRIKE_PRICE_RATIO));
			}
			boolean strikeExists = false;
			if (selectedStrikes.getItems() != null && !selectedStrikes.getItems().isEmpty()) {
				for (BigDecimal s : TradistaGUIUtil.parseAmounts(selectedStrikes.getItems(), STRIKE_PRICE_RATIO)) {
					if (s.compareTo(strike) == 0) {
						strikeExists = true;
						break;
					}
				}
			}
			if (!strikeExists) {
				selectedStrikes.getItems().add(strikeToAdd.getText());
				List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(pointsTable.getItems());
				List<SurfacePointProperty> properties = buildTableContent(points);
				pointsTable.setItems(FXCollections.observableArrayList(properties));
				pointsTable.refresh();
			}
		} catch (TradistaBusinessException _) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
					String.format("The %s (%s) is incorrect.", STRIKE_PRICE_RATIO, strikeToAdd.getText()));
			alert.showAndWait();
		}
	}

	@FXML
	protected void removeStrike() {
		String strikePriceRatioToBeRemoved = selectedStrikes.getSelectionModel().getSelectedItem();
		if (strikePriceRatioToBeRemoved == null) {
			return;
		}
		try {
			if (pointsTable.getItems() != null && !pointsTable.getItems().isEmpty()) {
				for (SurfacePointProperty prop : pointsTable.getItems()) {
					if (TradistaGUIUtil.parseAmount(prop.getStrike().getValue(), STRIKE_PRICE_RATIO).compareTo(
							TradistaGUIUtil.parseAmount(strikePriceRatioToBeRemoved, STRIKE_PRICE_RATIO)) == 0) {
						if (!StringUtils.isEmpty(prop.getVolatility().getValue())) {
							TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
							confirmation.setTitle("Remove Strike/Price Ratio");
							confirmation.setHeaderText("Remove Strike/Price Ratio");
							confirmation.setContentText(
									"Some volatilities exist for this Strike/Price Ratio. Do you really want to remove it?");

							Optional<ButtonType> result = confirmation.showAndWait();
							if (result.get() == ButtonType.OK) {
								selectedStrikes.getItems().remove(strikePriceRatioToBeRemoved);
								List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(
										pointsTable.getItems());
								List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toBeRemoved = new ArrayList<>();
								if (!points.isEmpty()) {
									for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
										if (p.getyAxis().compareTo(TradistaGUIUtil
												.parseAmount(strikePriceRatioToBeRemoved, STRIKE_PRICE_RATIO)) == 0) {
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
			selectedStrikes.getItems().remove(strikePriceRatioToBeRemoved);
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = toSurfacePointList(pointsTable.getItems());
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toBeRemoved = new ArrayList<>();
			if (!points.isEmpty()) {
				for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
					if (p.getyAxis().compareTo(
							TradistaGUIUtil.parseAmount(strikePriceRatioToBeRemoved, "Strike/Price Ratio")) == 0) {
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

		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints;
		try {
			List<String> quoteNames = toQuoteStringList(selectedQuotesList.getItems());
			List<BigDecimal> strikes = TradistaGUIUtil.parseAmounts(selectedStrikes.getItems(), STRIKE_PRICE_RATIO);

			surfacePoints = equityOptionVolatilitySurfaceBusinessDelegate.generate(algorithmComboBox.getValue(),
					interpolatorComboBox.getValue(), instanceComboBox.getValue(), quoteDate.getValue(),
					quoteSet.getValue(), quoteNames, strikes);
			// Update the points table
			pointsTable.setItems(FXCollections.observableArrayList(toSurfacePointPropertyList(surfacePoints)));
			pointsTable.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	public static List<BigDecimal> toStrikeList(ObservableList<StrikeProperty> items) throws TradistaBusinessException {
		List<BigDecimal> strikeList = new ArrayList<>();
		for (StrikeProperty strike : items) {
			strikeList.add(TradistaGUIUtil.parseAmount(strike.getValue().getValue(), STRIKE_PRICE_RATIO));
		}
		return strikeList;
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
			setText(getItem());
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
			textField.focusedProperty().addListener((_, _, newVal) -> {
				if (Boolean.FALSE.equals(newVal)) {
					commitEdit(textField.getText());
				}
			});

		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem();
		}
	}

	class StringEditingCell extends TableCell<SurfacePointProperty, String> {

		private TextField textField;

		public StringEditingCell() {
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
			setText(getItem());
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
			textField.focusedProperty().addListener(new ChangeListener<>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (arg2 != null && !arg2) {
						commitEdit(textField.getText());
					}
				}
			});

		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem();
		}
	}

	private List<SurfacePointProperty> buildTableContent(List<SurfacePoint<Integer, BigDecimal, BigDecimal>> data)
			throws TradistaBusinessException {
		if (data != null) {
			Collection<Number> optionLifetimes = equityOptionVolatilitySurfaceBusinessDelegate.getAllOptionExpiries();
			for (Number optionLifetime : optionLifetimes) {
				for (BigDecimal strike : TradistaGUIUtil.parseAmounts(selectedStrikes.getItems(), STRIKE_PRICE_RATIO)) {
					SurfacePoint<Integer, BigDecimal, BigDecimal> point = new SurfacePoint<>((Integer) optionLifetime,
							strike, null);
					if (!data.contains(point)) {
						data.add(point);
					}
				}
			}

			Collections.sort(data);

			return FXCollections.observableArrayList(toSurfacePointPropertyList(data));
		}
		return FXCollections.emptyObservableList();
	}

	private List<SurfacePointProperty> toSurfacePointPropertyList(
			List<SurfacePoint<Integer, BigDecimal, BigDecimal>> data) {
		List<SurfacePointProperty> surfacePointPropertyList = new ArrayList<>();
		if (data != null && !data.isEmpty()) {
			try {
				for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : data) {
					String optionExpiry = equityOptionVolatilitySurfaceBusinessDelegate
							.getOptionExpiryName(point.getxAxis());
					String strike = TradistaGUIUtil.formatAmount(point.getyAxis());
					String volatility = point.getzAxis() == null ? StringUtils.EMPTY
							: TradistaGUIUtil.formatAmount(point.getzAxis());
					if (!optionExpiry.isEmpty()) {
						surfacePointPropertyList.add(new SurfacePointProperty(optionExpiry, strike, volatility));
					}

				}
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}

		return surfacePointPropertyList;
	}

	private List<SurfacePoint<Integer, BigDecimal, BigDecimal>> toSurfacePointList(List<SurfacePointProperty> data) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePointList = new ArrayList<>();
		try {
			for (SurfacePointProperty point : data) {
				try {
					String optionExpiry = point.getOptionExpiry().getValue();
					String volatility = point.getVolatility().getValue();
					String strike = point.getStrike().getValue();
					if (!optionExpiry.isEmpty() && !volatility.isEmpty()) {

						surfacePointList.add(new SurfacePoint<>(
								equityOptionVolatilitySurfaceBusinessDelegate
										.getOptionExpiryValue(point.getOptionExpiry().getValue()),
								TradistaGUIUtil.parseAmount(strike, STRIKE_PRICE_RATIO),
								TradistaGUIUtil.parseAmount(volatility, VOLATILITY)));

					}
				} catch (DateTimeParseException dtpe) {
					// TODO Auto-generated catch block
					dtpe.printStackTrace();
				}
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

		return surfacePointList;
	}

	private List<String> toQuoteStringList(List<Quote> data) {
		List<String> nameList = new ArrayList<>();
		for (Quote quote : data) {
			nameList.add(quote.getName());
		}

		return nameList;
	}

	public static List<BigDecimal> toStrikeList(List<StrikeProperty> data) throws TradistaBusinessException {
		List<BigDecimal> strikeList = new ArrayList<>();
		for (StrikeProperty prop : data) {
			strikeList.add(TradistaGUIUtil.parseAmount(prop.getValue().getValue(), STRIKE_PRICE_RATIO));
		}

		return strikeList;
	}

	public static class SurfacePointProperty {

		private final StringProperty optionExpiry;
		private final StringProperty strike;
		private final StringProperty volatility;

		private SurfacePointProperty(String optionExpiry, String strike, String volatility) {
			this.optionExpiry = new SimpleStringProperty(optionExpiry);
			this.strike = new SimpleStringProperty(strike);
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

		public StringProperty getStrike() {
			return strike;
		}

		public void setStrike(String strike) {
			this.strike.set(strike);
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

	public static class StrikeProperty {

		private final StringProperty value;

		public StrikeProperty(String value) {
			this.value = new SimpleStringProperty(value);
		}

		public StringProperty getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value.set(value);
		}

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof StrikeProperty)) {
				return false;
			}

			return (getValue().getValue().compareTo((((StrikeProperty) o).getValue().getValue())) == 0);
		}

		public int hashCode() {
			return value.hashCode();
		}
	}

	@Override
	@FXML
	public void refresh() {
		super.refresh();
		TradistaGUIUtil.fillComboBox(
				equityOptionVolatilitySurfaceBusinessDelegate.getAllEquityOptionVolatilitySurfaces(),
				volatilitySurface);
	}

}