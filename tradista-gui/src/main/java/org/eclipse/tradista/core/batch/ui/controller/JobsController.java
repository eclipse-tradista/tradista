package org.eclipse.tradista.core.batch.ui.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.ui.view.TradistaCalendarComboBox;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.error.ui.view.TradistaErrorTypeComboBox;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaFeedConfigComboBox;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaQuoteSetComboBox;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaQuoteSetsListView;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.ui.view.TradistaPositionDefinitionComboBox;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.StackPane;

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

public class JobsController extends TradistaControllerAdapter {

	@FXML
	private TableView<JobPropertyProperty> jobPropertiesTable;

	@FXML
	private TableView<JobExecutionProperty> jobExecutionsTable;

	@FXML
	private TableColumn<JobPropertyProperty, String> propertyName;

	@FXML
	private TableColumn<JobPropertyProperty, Object> propertyValue;

	@FXML
	private TableColumn<JobExecutionProperty, Long> executionId;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionJobInstanceName;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionJobType;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionStartTime;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionEndTime;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionStatus;

	@FXML
	private TableColumn<JobExecutionProperty, String> executionErrorCause;

	@FXML
	private TableColumn<JobExecutionProperty, List<Button>> executionActions;

	@FXML
	private ComboBox<TradistaJobInstance> jobInstance;

	@FXML
	private Label jobType;

	@FXML
	private Label jobName;

	@FXML
	private Label jobInstanceName;

	private TradistaJobInstance currentJobInstance;

	private BatchBusinessDelegate batchBusinessDelegate;

	@FXML
	private DatePicker jobExecutionDate;

	private String po;

	private static final String DATE_PATTERN = "dd/MM/yyyy";

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		batchBusinessDelegate = new BatchBusinessDelegate();

		propertyName.setCellValueFactory(cellData -> cellData.getValue().getName());

		propertyValue.setCellFactory(tc -> new EditingCell());

		propertyValue.setOnEditCommit(
				cee -> cee.getTableView().getItems().get(cee.getTablePosition().getRow()).setValue(cee.getNewValue()));

		propertyValue.setCellValueFactory(cellData -> cellData.getValue().getValue());

		executionId.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());

		executionJobInstanceName.setCellValueFactory(cellData -> cellData.getValue().getJobInstanceName());

		executionJobType.setCellValueFactory(cellData -> cellData.getValue().getJobType());

		executionStartTime.setCellValueFactory(cellData -> cellData.getValue().getStartTime());

		executionEndTime.setCellValueFactory(cellData -> cellData.getValue().getEndTime());

		executionStatus.setCellValueFactory(cellData -> cellData.getValue().getStatus());

		executionErrorCause.setCellValueFactory(cellData -> cellData.getValue().getErrorCause());

		executionActions.setCellValueFactory(cellData -> cellData.getValue().getActions());

		executionActions.setCellFactory(tc -> new ExecutionActionsCellFactory());

		jobExecutionDate.setValue(LocalDate.now());

		po = ClientUtil.getCurrentUser().getProcessingOrg() != null
				? ClientUtil.getCurrentUser().getProcessingOrg().getShortName()
				: null;

		try {
			TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

		jobPropertiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

	}

	private class ExecutionActionsCellFactory extends TableCell<JobExecutionProperty, List<Button>> {

		/** places a button in the row only if the row is not empty. */
		@Override
		protected void updateItem(List<Button> item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) {
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				StackPane stackPane = new StackPane();
				stackPane.getChildren().addAll(item);
				setGraphic(stackPane);
			} else {
				setGraphic(null);
			}
		}
	}

	@FXML
	protected void load() {
		try {
			if (jobInstance.getValue() == null) {
				throw new TradistaBusinessException("A job instance must be selected.");
			}
			currentJobInstance = jobInstance.getValue();
			ObservableList<JobPropertyProperty> data = buildTableContent(currentJobInstance);
			jobType.setText(currentJobInstance.getJobType());
			jobName.setText(currentJobInstance.getName());
			jobInstanceName.setText(currentJobInstance.getName());
			jobPropertiesTable.setItems(data);
			jobPropertiesTable.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void retry(String jobInstance, String po) {
		try {
			batchBusinessDelegate.runJobInstance(jobInstance, po);
			Thread.sleep(1000);
			refreshJobExecutionTable();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	@FXML
	protected void stop(String jobExecutionId) {
		try {
			batchBusinessDelegate.stopJobExecution(jobExecutionId);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void save() {
		try {
			if (currentJobInstance == null) {
				throw new TradistaBusinessException("A job instance must be loaded before saving it.");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Save Job Instance");
			confirmation.setHeaderText("Save Job Instance");
			confirmation.setContentText("Do you want to save this Job Instance?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				Map<String, Object> properties = toMap(jobPropertiesTable.getItems());
				currentJobInstance.setProperties(properties);
				batchBusinessDelegate.saveJobInstance(currentJobInstance);
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		try {
			if (currentJobInstance == null) {
				throw new TradistaBusinessException("A job instance must be loaded before copying it.");
			}
			Map<String, Object> properties = toMap(jobPropertiesTable.getItems());
			StringBuilder jobName = new StringBuilder();
			TradistaTextInputDialog dialog = new TradistaTextInputDialog();
			dialog.setTitle("Job instance name");
			dialog.setHeaderText("Job instance name selection");
			dialog.setContentText("Please choose a Job instance name:");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				jobName.append(result.get());
				TradistaJobInstance job = new TradistaJobInstance(jobName.toString(), currentJobInstance.getJobType(),
						ClientUtil.getCurrentUser().getProcessingOrg());
				job.setProperties(properties);
				batchBusinessDelegate.saveJobInstance(job);

				TradistaJobInstance jobInst = jobInstance.getValue();
				TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
				if (jobInst != null && !jobInst.equals(jobInstance.getValue())) {
					jobPropertiesTable.setItems(null);
					jobType.setText(null);
					this.jobName.setText(null);
				}
				this.jobName.setText(jobName.toString());
				jobInstanceName.setText(jobName.toString());
				currentJobInstance = batchBusinessDelegate.getJobInstanceByNameAndPo(jobName.toString(), po);

			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void loadExecution(ActionEvent event) {
		Set<TradistaJobExecution> jobExecutions;
		try {
			jobExecutions = batchBusinessDelegate.getJobExecutions(jobExecutionDate.getValue(), po);
			ObservableList<JobExecutionProperty> data = buildJobExecutionsTableContent(jobExecutions);
			jobExecutionsTable.setItems(data);
			jobExecutionsTable.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void run() {
		try {
			if (currentJobInstance == null) {
				throw new TradistaBusinessException("Please load a job instance.");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Run Job Instance");
			confirmation.setHeaderText("Run Job Instance");
			confirmation.setContentText("Do you want to run this Job Instance?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				batchBusinessDelegate.runJobInstance(currentJobInstance.getName(), po);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			if (currentJobInstance == null) {
				throw new TradistaBusinessException("Please load a job instance.");
			}
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Job Instance");
			confirmation.setHeaderText("Delete Job Instance");
			confirmation.setContentText(
					String.format("Do you want to delete this Job Instance %s ?", currentJobInstance.getName()));

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {

				batchBusinessDelegate.deleteJobInstance(currentJobInstance.getName(), po);

				TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
				if (!jobInstance.getItems().contains(currentJobInstance)) {
					jobPropertiesTable.setItems(null);
					jobType.setText(null);
					jobName.setText(null);
					jobInstanceName.setText(null);
					currentJobInstance = null;
				}

			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void create(ActionEvent event) {
		JobInstanceCreatorDialog dialog = new JobInstanceCreatorDialog();
		Optional<TradistaJobInstance> result = dialog.showAndWait();

		if (result.isPresent()) {
			try {
				TradistaJobInstance job = result.get();
				batchBusinessDelegate.saveJobInstance(job);

				TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
				if (currentJobInstance != null && !jobInstance.getItems().contains(currentJobInstance)) {
					jobPropertiesTable.setItems(null);
					jobType.setText(null);
					jobName.setText(null);
					currentJobInstance = null;
				}
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
				alert.showAndWait();
			}
		}
	}

	class EditingCell extends TableCell<JobPropertyProperty, Object> {

		private TextField textField;

		private TradistaCalendarComboBox calendarComboBox;

		private TradistaPositionDefinitionComboBox positionDefinitionComboBox;

		private TradistaQuoteSetComboBox quoteSetComboBox;

		private TradistaFeedConfigComboBox feedConfigComboBox;

		private TradistaQuoteSetsListView quoteSetsListView;

		private DatePicker datePicker;

		private CheckBox checkBox;

		private TradistaErrorTypeComboBox errorTypeComboBox;

		private ComboBox<String> errorStatusComboBox;

		private JobPropertyProperty model;

		public EditingCell() {
			createCalendarComboBox();
			createPositionDefinitionComboBox();
			createQuoteSetComboBox();
			createFeedConfigComboBox();
			createQuoteSetsListView();
			createDatePicker();
			createTextField();
			createCheckBox();
			createErrorTypeComboBox();
			createErrorStatusComboBox();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			model = getTableRow().getItem();
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (model != null) {
					if (model.getType().get().equals("Calendar")) {
						if (calendarComboBox != null) {
							calendarComboBox.setValue((Calendar) item);
						}
						setGraphic(calendarComboBox);
					} else if (model.getType().get().equals("PositionDefinition")) {
						if (positionDefinitionComboBox != null) {
							positionDefinitionComboBox.setValue((PositionDefinition) item);
						}
						setGraphic(positionDefinitionComboBox);
					} else if (model.getType().get().equals("QuoteSet")) {
						if (quoteSetComboBox != null) {
							quoteSetComboBox.setValue((QuoteSet) item);
						}
						setGraphic(quoteSetComboBox);
					} else if (model.getType().get().equals("FeedConfig")) {
						if (feedConfigComboBox != null) {
							feedConfigComboBox.setValue((FeedConfig) item);
						}
						setGraphic(feedConfigComboBox);
					} else if (model.getType().get().equals("QuoteSetSet")) {
						if (quoteSetsListView != null) {
							if (item != null && !((HashSet<QuoteSet>) item).isEmpty()) {
								for (QuoteSet qs : (HashSet<QuoteSet>) item) {
									quoteSetsListView.getSelectionModel().select(qs);
								}
							}
						}
						setGraphic(quoteSetsListView);
					} else if (model.getType().get().equals("Date")) {
						if (datePicker != null) {
							if (item != null) {
								datePicker.setValue(
										LocalDate.parse(getString(), DateTimeFormatter.ofPattern(DATE_PATTERN)));
							}
						}
						setGraphic(datePicker);
					} else if (model.getType().get().equals("Boolean")) {
						if (checkBox != null) {
							Boolean value = item == null ? Boolean.FALSE : (Boolean) item;
							checkBox.setSelected(value);
						}
						setGraphic(checkBox);
					} else if (model.getType().get().equals("ErrorType")) {
						if (errorTypeComboBox != null) {
							errorTypeComboBox.setValue(getString());
						}
						setGraphic(errorTypeComboBox);
					} else if (model.getType().get().equals("ErrorStatus")) {
						if (errorStatusComboBox != null) {
							errorStatusComboBox.setValue(getString());
						}
						setGraphic(errorStatusComboBox);
					} else {
						if (textField != null) {
							textField.setText(getString());
						}
						setGraphic(textField);
					}
					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField();
			if (getItem() != null) {
				textField.setText(getItem().toString());
			}
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(textField.getText());
					commitEdit(textField.getText());
				}
			});
			textField.setMaxWidth(Double.MAX_VALUE);
		}

		private void createCalendarComboBox() {
			calendarComboBox = new TradistaCalendarComboBox();
			if (getItem() != null) {
				calendarComboBox.setValue((Calendar) getItem());
			}
			calendarComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			calendarComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(calendarComboBox.getValue());
					commitEdit(calendarComboBox.getValue());
				}
			});
			calendarComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		private void createPositionDefinitionComboBox() {
			positionDefinitionComboBox = new TradistaPositionDefinitionComboBox();
			if (getItem() != null) {
				positionDefinitionComboBox.setValue((PositionDefinition) getItem());
			}
			positionDefinitionComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			positionDefinitionComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(positionDefinitionComboBox.getValue());
					commitEdit(positionDefinitionComboBox.getValue());
				}
			});
			positionDefinitionComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		private void createQuoteSetComboBox() {
			quoteSetComboBox = new TradistaQuoteSetComboBox();
			if (getItem() != null) {
				quoteSetComboBox.setValue((QuoteSet) getItem());
			}
			quoteSetComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			quoteSetComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(quoteSetComboBox.getValue());
					commitEdit(quoteSetComboBox.getValue());
				}
			});
			quoteSetComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		@SuppressWarnings("unchecked")
		private void createQuoteSetsListView() {
			final String MAIN_STYLE = "-fx-background-color: white; -fx-text-fill: black;";
			final String SECONDARY_STYLE = "-fx-background-color: -secondary-color; -fx-text-fill: -secondary-font-color;";
			quoteSetsListView = new TradistaQuoteSetsListView();
			if (getItem() instanceof QuoteSet qs) {
				quoteSetsListView.getSelectionModel().select(qs);
			} else if (getItem() instanceof HashSet hs) {
				if (!hs.isEmpty()) {
					for (QuoteSet qs : (HashSet<QuoteSet>) hs) {
						quoteSetsListView.getSelectionModel().select(qs);
					}
				}
			}
			quoteSetsListView.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			quoteSetsListView.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					Set<QuoteSet> quoteSets = null;
					if (quoteSetsListView.getSelectionModel().getSelectedItems() != null) {
						quoteSets = new HashSet<>(quoteSetsListView.getSelectionModel().getSelectedItems());
					}
					model.setValue(quoteSets);
					commitEdit(quoteSets);
				}
			});
			focusedProperty().addListener((b, ov, nv) -> {
				if (isSelected()) {
					quoteSetsListView.setStyle(SECONDARY_STYLE);
				} else {
					quoteSetsListView.setStyle(MAIN_STYLE);
				}
			});
			hoverProperty().addListener((b, ov, nv) -> {
				if (Boolean.TRUE.equals(nv)) {
					quoteSetsListView.setStyle(SECONDARY_STYLE);
				}
			});
			quoteSetsListView.setMaxWidth(Double.MAX_VALUE);
			quoteSetsListView.setMaxHeight(100);
		}

		private void createFeedConfigComboBox() {
			feedConfigComboBox = new TradistaFeedConfigComboBox();
			if (getItem() != null) {
				feedConfigComboBox.setValue((FeedConfig) getItem());
			}
			feedConfigComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			feedConfigComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(feedConfigComboBox.getValue());
					commitEdit(feedConfigComboBox.getValue());
				}
			});
			feedConfigComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		private void createDatePicker() {
			datePicker = new DatePicker();
			if (getItem() != null) {
				datePicker.setValue(LocalDate.parse(getString(), DateTimeFormatter.ofPattern(DATE_PATTERN)));
			}
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			datePicker.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					LocalDate date = datePicker.getValue();
					model.setValue(date);
					commitEdit(date);
				}
			});
			datePicker.setMaxWidth(Double.MAX_VALUE);
		}

		private void createCheckBox() {
			checkBox = new CheckBox();
			if (getItem() != null) {
				checkBox.setSelected((Boolean) getItem());
			}
			checkBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			checkBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(checkBox.isSelected());
					commitEdit(checkBox.isSelected());
				}
			});
			checkBox.setMaxWidth(Double.MAX_VALUE);
		}

		private void createErrorTypeComboBox() {
			errorTypeComboBox = new TradistaErrorTypeComboBox();
			if (getItem() != null) {
				errorTypeComboBox.setValue((String) getItem());
			}
			errorTypeComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			errorTypeComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(errorTypeComboBox.getValue());
					commitEdit(errorTypeComboBox.getValue());
				}
			});
			errorTypeComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		private void createErrorStatusComboBox() {
			errorStatusComboBox = new ComboBox<>();
			TradistaGUIUtil.fillErrorStatusComboBox(errorStatusComboBox);
			if (getItem() != null) {
				errorStatusComboBox.setValue((String) getItem());
			}
			errorStatusComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			errorStatusComboBox.focusedProperty().addListener((b, ov, nv) -> {
				if (Boolean.FALSE.equals(nv)) {
					model.setValue(errorStatusComboBox.getValue());
					commitEdit(errorStatusComboBox.getValue());
				}
			});
			errorStatusComboBox.setMaxWidth(Double.MAX_VALUE);
		}

		private String getString() {
			JobPropertyProperty model = getTableRow().getItem();
			return getItem() == null ? StringUtils.EMPTY
					: (model != null && model.getType().get().equals("Date"))
							? ((LocalDate) getItem()).format(DateTimeFormatter.ofPattern(DATE_PATTERN))
							: getItem().toString();
		}
	}

	private ObservableList<JobPropertyProperty> buildTableContent(TradistaJobInstance jobInstance)
			throws TradistaBusinessException {

		List<JobPropertyProperty> jobPropertyProperty = new ArrayList<>();

		for (String name : batchBusinessDelegate.getAllJobPropertyNames(jobInstance)) {
			if (!name.equals("ProcessingOrg")) {
				jobPropertyProperty.add(new JobPropertyProperty(name, jobInstance.getJobPropertyValue(name),
						batchBusinessDelegate.getPropertyType(jobInstance, name)));
			}
		}

		Collections.sort(jobPropertyProperty);

		return FXCollections.observableArrayList(jobPropertyProperty);

	}

	private ObservableList<JobExecutionProperty> buildJobExecutionsTableContent(
			Set<TradistaJobExecution> jobExecutions) {

		List<JobExecutionProperty> jobExecutionProperties = new ArrayList<>();

		if (jobExecutions != null) {
			for (TradistaJobExecution jobExecution : jobExecutions) {
				jobExecutionProperties.add(new JobExecutionProperty(jobExecution.getId(), jobExecution.getName(),
						jobExecution.getStatus(), jobExecution.getStartTime(), jobExecution.getEndTime(),
						jobExecution.getErrorCause(), jobExecution.getJobInstanceName(), jobExecution.getJobType(),
						jobExecution.getJobInstance() != null));
			}
		}

		Collections.sort(jobExecutionProperties);

		return FXCollections.observableArrayList(jobExecutionProperties);

	}

	private Map<String, Object> toMap(List<JobPropertyProperty> data) {
		Map<String, Object> properties = new HashMap<>();
		for (JobPropertyProperty prop : data) {
			Object value = prop.getValue();
			if (value != null) {
				properties.put(prop.getName().get(), prop.getValue().get());
			}
		}

		return properties;
	}

	public static class JobPropertyProperty implements Comparable<JobPropertyProperty> {

		private final StringProperty name;
		private final ObjectProperty<Object> value;
		private final StringProperty type;

		private JobPropertyProperty(String name, Object value, String type) {
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleObjectProperty<>(value);
			this.type = new SimpleStringProperty(type);
		}

		public StringProperty getName() {
			return name;
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public ObjectProperty<Object> getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value.set(value);
		}

		public StringProperty getType() {
			return type;
		}

		public void setType(String type) {
			this.type.set(type);
		}

		@Override
		public int compareTo(JobPropertyProperty j) {
			return getName().get().compareTo(j.getName().get());
		}

	}

	public class JobExecutionProperty implements Comparable<JobExecutionProperty> {

		private final StringProperty name;
		private final StringProperty status;
		private final StringProperty startTime;
		private StringProperty endTime;
		private final StringProperty errorCause;
		private final StringProperty jobInstanceName;
		private final StringProperty jobType;
		private final LongProperty id;
		private final ObjectProperty<List<Button>> actions;

		private JobExecutionProperty(long id, final String name, String status, LocalDateTime startTime,
				LocalDateTime endTime, String errorCause, final String jobInstanceName, String jobType,
				boolean jobInstanceStillExists) {
			this.id = new SimpleLongProperty(id);
			this.name = new SimpleStringProperty(name);
			this.status = new SimpleStringProperty(status);
			this.startTime = new SimpleStringProperty(startTime.toString());
			if (endTime != null) {
				this.endTime = new SimpleStringProperty(endTime.toString());
			}
			this.errorCause = new SimpleStringProperty(errorCause);
			this.jobInstanceName = new SimpleStringProperty(jobInstanceName);
			this.jobType = new SimpleStringProperty(jobType);
			List<Button> buttons = new ArrayList<>();

			if (status.equals("FAILED") && jobInstanceStillExists) {
				Button retryButton = new Button("Retry");
				retryButton.setOnAction(ae -> JobsController.this.retry(jobInstanceName, po));
				buttons.add(retryButton);
			}
			if (status.equals("IN PROGRESS")) {
				Button stopButton = new Button("Stop");
				stopButton.setOnAction(ae -> JobsController.this.stop(name));
				buttons.add(stopButton);
			}

			if (status.equals("PAUSED")) {
				Button stopButton = new Button("Stop");
				stopButton.setOnAction(ae -> JobsController.this.stop(name));
				buttons.add(stopButton);
			}
			actions = new SimpleObjectProperty<>(buttons);
		}

		public StringProperty getName() {
			return name;
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public StringProperty getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status.set(status);
		}

		public StringProperty getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime.set(startTime);
		}

		public StringProperty getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime.set(endTime);
		}

		public StringProperty getErrorCause() {
			return errorCause;
		}

		public void setErrorCause(String errorCause) {
			this.errorCause.set(errorCause);
		}

		public StringProperty getJobInstanceName() {
			return jobInstanceName;
		}

		public void setJobInstanceName(String jobInstanceName) {
			this.jobInstanceName.set(jobInstanceName);
		}

		public StringProperty getJobType() {
			return jobType;
		}

		public void setJobType(String jobType) {
			this.jobType.set(jobType);
		}

		public LongProperty getId() {
			return id;
		}

		public void setId(long id) {
			this.id.set(id);
		}

		public ObjectProperty<List<Button>> getActions() {
			return actions;
		}

		public void setActions(List<Button> buttons) {
			actions.set(buttons);
		}

		@Override
		public int compareTo(JobExecutionProperty o) {
			if (getId().get() < o.getId().get()) {
				return -1;
			}
			if (getId().get() == o.getId().get()) {
				return 0;
			}
			if (getId().get() > o.getId().get()) {
				return 1;
			}

			// Make the compiler happy
			return 0;
		}

	}

	private void refreshJobExecutionTable() {
		Platform.runLater(() -> {
			Set<TradistaJobExecution> jobExecutions;
			try {
				jobExecutions = batchBusinessDelegate.getJobExecutions(jobExecutionDate.getValue(), po);
				ObservableList<JobExecutionProperty> data = buildJobExecutionsTableContent(jobExecutions);
				jobExecutionsTable.setItems(data);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		});
	}

	@Override
	@FXML
	public void refresh() {
		TradistaJobInstance jobInst = jobInstance.getValue();
		try {
			TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
		if (jobInst != null && !jobInst.equals(jobInstance.getValue())) {
			jobType.setText(null);
			jobName.setText(null);
			jobPropertiesTable.setItems(null);
		}
	}

}