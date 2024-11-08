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
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaFeedConfigComboBox;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaQuoteSetComboBox;
import org.eclipse.tradista.core.marketdata.ui.view.TradistaQuoteSetsListView;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.ui.view.TradistaPositionDefinitionComboBox;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
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
	private TableColumn<JobExecutionProperty, String> executionId;

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

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		batchBusinessDelegate = new BatchBusinessDelegate();
		Callback<TableColumn<JobPropertyProperty, Object>, TableCell<JobPropertyProperty, Object>> cellFactory = new Callback<TableColumn<JobPropertyProperty, Object>, TableCell<JobPropertyProperty, Object>>() {
			public TableCell<JobPropertyProperty, Object> call(TableColumn<JobPropertyProperty, Object> p) {
				return new EditingCell();
			}
		};

		propertyName.setCellValueFactory(cellData -> cellData.getValue().getName());

		propertyValue.setCellFactory(cellFactory);

		propertyValue.setOnEditCommit(new EventHandler<CellEditEvent<JobPropertyProperty, Object>>() {
			@Override
			public void handle(CellEditEvent<JobPropertyProperty, Object> t) {
				((JobPropertyProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setValue(t.getNewValue());
			}
		});

		propertyValue.setCellValueFactory(new PropertyValueFactory<JobPropertyProperty, Object>("value"));

		executionId.setCellValueFactory(new PropertyValueFactory<JobExecutionProperty, String>("id"));

		executionJobInstanceName.setCellValueFactory(cellData -> cellData.getValue().getJobInstanceName());

		executionJobType.setCellValueFactory(cellData -> cellData.getValue().getJobType());

		executionStartTime.setCellValueFactory(cellData -> cellData.getValue().getStartTime());

		executionEndTime.setCellValueFactory(cellData -> cellData.getValue().getEndTime());

		executionStatus.setCellValueFactory(cellData -> cellData.getValue().getStatus());

		executionErrorCause.setCellValueFactory(cellData -> cellData.getValue().getErrorCause());

		executionActions.setCellValueFactory(new PropertyValueFactory<JobExecutionProperty, List<Button>>("actions"));

		executionActions.setCellFactory(
				new Callback<TableColumn<JobExecutionProperty, List<Button>>, TableCell<JobExecutionProperty, List<Button>>>() {
					@Override
					public TableCell<JobExecutionProperty, List<Button>> call(
							TableColumn<JobExecutionProperty, List<Button>> personBooleanTableColumn) {
						return new ExecutionActionsCellFactory(jobExecutionsTable);
					}
				});

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
	}

	private class ExecutionActionsCellFactory extends TableCell<JobExecutionProperty, List<Button>> {

		ExecutionActionsCellFactory(final TableView<JobExecutionProperty> table) {
			super();
		}

		/** places an add button in the row only if the row is not empty. */
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
			this.jobName.setText(currentJobInstance.getName());
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
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshJobExecutionTable();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
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
			if (result.get() == ButtonType.OK) {
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
			// The Java 8 way to get the response value (with lambda
			// expression).
			result.ifPresent(name -> jobName.append(name));
			if (result.isPresent()) {
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
			if (result.get() == ButtonType.OK) {
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
			if (result.get() == ButtonType.OK) {
				batchBusinessDelegate.deleteJobInstance(currentJobInstance.getName(), po);

				TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
				if (currentJobInstance != null && !jobInstance.getItems().contains(currentJobInstance)) {
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
		try {
			JobInstanceCreatorDialog dialog = new JobInstanceCreatorDialog();
			Optional<TradistaJobInstance> result = dialog.showAndWait();

			if (result.isPresent()) {
				TradistaJobInstance job = result.get();
				batchBusinessDelegate.saveJobInstance(job);

				TradistaGUIUtil.fillComboBox(batchBusinessDelegate.getAllJobInstances(po), jobInstance);
				if (currentJobInstance != null && !jobInstance.getItems().contains(currentJobInstance)) {
					jobPropertiesTable.setItems(null);
					jobType.setText(null);
					jobName.setText(null);
					currentJobInstance = null;
				}
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
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

		private JobPropertyProperty model;

		@Override
		public void startEdit() {
			if (textField != null && !StringUtils.isEmpty(textField.getText())) {
				setItem(textField.getText());
			}
			super.startEdit();

			if (model != null) {
				if (model.getType().equals("Calendar")) {
					createCalendarComboBox();
					Calendar calendar = calendarComboBox.getValue();
					if (calendar != null) {
						setText(calendar.toString());
					}
					setGraphic(calendarComboBox);
				} else if (model.getType().equals("PositionDefinition")) {
					createPositionDefinitionComboBox();
					if (positionDefinitionComboBox.getValue() != null) {
						setText(positionDefinitionComboBox.getValue().toString());
					}
					setGraphic(positionDefinitionComboBox);
				} else if (model.getType().equals("QuoteSet")) {
					createQuoteSetComboBox();
					if (quoteSetComboBox.getValue() != null) {
						setText(quoteSetComboBox.getValue().toString());
					}
					setGraphic(quoteSetComboBox);
				} else if (model.getType().equals("FeedConfig")) {
					createFeedConfigComboBox();
					if (feedConfigComboBox.getValue() != null) {
						setText(feedConfigComboBox.getValue().toString());
					}
					setGraphic(feedConfigComboBox);
				} else if (model.getType().equals("QuoteSetSet")) {
					createQuoteSetsListView();
					if (quoteSetsListView.getSelectionModel().getSelectedItems() != null
							&& !quoteSetsListView.getSelectionModel().getSelectedItems().isEmpty()) {
						setText(quoteSetsListView.getSelectionModel().getSelectedItems().get(0).toString());
					}
					setGraphic(quoteSetsListView);
				} else if (model.getType().equals("Date")) {
					createDatePicker();
					LocalDate date = datePicker.getValue();
					if (date != null) {
						setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
					}
					setGraphic(datePicker);
				} else {
					createTextField();
					setText(textField.getText());
					setGraphic(textField);
					textField.selectAll();
				}
			} else {
				createTextField();
				setText(textField.getText());
				setGraphic(textField);
				textField.selectAll();
			}

		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			if (getItem() != null) {
				setText(getItem().toString());
			}
			setGraphic(null);
		}

		@Override
		public void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			model = (JobPropertyProperty) getTableRow().getItem();
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (model != null) {
						if (model.getType().equals("Calendar")) {
							if (calendarComboBox != null) {
								calendarComboBox.setValue((Calendar) getItem());
							}
							setGraphic(calendarComboBox);
						} else if (model.getType().equals("PositionDefinition")) {
							if (positionDefinitionComboBox != null) {
								positionDefinitionComboBox.setValue((PositionDefinition) getItem());
							}
							setGraphic(positionDefinitionComboBox);
						} else if (model.getType().equals("QuoteSet")) {
							if (quoteSetComboBox != null) {
								quoteSetComboBox.setValue((QuoteSet) getItem());
							}
							setGraphic(quoteSetComboBox);
						} else if (model.getType().equals("FeedConfig")) {
							if (feedConfigComboBox != null) {
								feedConfigComboBox.setValue((FeedConfig) getItem());
							}
							setGraphic(feedConfigComboBox);
						} else if (model.getType().equals("QuoteSetSet")) {
							if (quoteSetsListView != null) {
								quoteSetsListView.getSelectionModel().select((QuoteSet) getItem());
							}
							setGraphic(quoteSetsListView);
						} else if (model.getType().equals("Date")) {
							if (datePicker != null) {
								datePicker.setValue(
										LocalDate.parse(getString(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
							}
							setGraphic(datePicker);
						} else {
							if (textField != null) {
								textField.setText(getString());
							}
							setGraphic(textField);
						}
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

		private void createCalendarComboBox() {
			calendarComboBox = new TradistaCalendarComboBox();
			if (getItem() != null) {
				calendarComboBox.setValue((Calendar) getItem());
			}
			calendarComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			calendarComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(calendarComboBox.getValue());
					}
				}
			});
		}

		private void createPositionDefinitionComboBox() {
			positionDefinitionComboBox = new TradistaPositionDefinitionComboBox();
			if (getItem() != null) {
				positionDefinitionComboBox.setValue((PositionDefinition) getItem());
			}
			positionDefinitionComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			positionDefinitionComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(positionDefinitionComboBox.getValue());
					}
				}
			});
		}

		private void createQuoteSetComboBox() {
			quoteSetComboBox = new TradistaQuoteSetComboBox();
			quoteSetComboBox.setValue((QuoteSet) getItem());
			quoteSetComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			quoteSetComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(quoteSetComboBox.getValue());
					}
				}
			});
		}

		private void createQuoteSetsListView() {
			quoteSetsListView = new TradistaQuoteSetsListView();
			if (getItem() instanceof QuoteSet) {
				quoteSetsListView.getSelectionModel().select((QuoteSet) getItem());
			} else if (getItem() instanceof HashSet) {
				if (getItem() != null && !((HashSet<QuoteSet>) getItem()).isEmpty()) {
					for (QuoteSet qs : (HashSet<QuoteSet>) getItem()) {
						quoteSetsListView.getSelectionModel().select(qs);
					}
				}
			}
			quoteSetsListView.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			quoteSetsListView.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						Set<QuoteSet> quoteSets = null;
						if (quoteSetsListView.getSelectionModel().getSelectedItems() != null) {
							quoteSets = new HashSet<QuoteSet>(quoteSetsListView.getSelectionModel().getSelectedItems());
						}
						commitEdit(quoteSets);
					}
				}
			});
		}

		private void createFeedConfigComboBox() {
			feedConfigComboBox = new TradistaFeedConfigComboBox();
			feedConfigComboBox.setValue((FeedConfig) getItem());
			feedConfigComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			feedConfigComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(feedConfigComboBox.getValue());
					}
				}
			});
		}

		private void createDatePicker() {
			datePicker = new DatePicker();
			if (getItem() != null) {
				datePicker.setValue(LocalDate.parse(getString(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
			}
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			datePicker.setEditable(false);
			datePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						LocalDate date = datePicker.getValue();
						if (date != null) {
							String dateString = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
							commitEdit(date);
							setContentDisplay(ContentDisplay.TEXT_ONLY);
							setText(dateString);
						}
					}
				}
			});
		}

		private String getString() {
			JobPropertyProperty model = (JobPropertyProperty) getTableRow().getItem();
			return getItem() == null ? StringUtils.EMPTY
					: (model != null && model.getType().equals("Date"))
							? ((LocalDate) getItem()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
							: getItem().toString();
		}
	}

	private ObservableList<JobPropertyProperty> buildTableContent(TradistaJobInstance jobInstance)
			throws TradistaBusinessException {

		List<JobPropertyProperty> jobPropertyProperty = new ArrayList<JobPropertyProperty>();

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

		List<JobExecutionProperty> jobExecutionProperties = new ArrayList<JobExecutionProperty>();

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
		Map<String, Object> properties = new HashMap<String, Object>();
		for (JobPropertyProperty prop : data) {
			Object value = prop.getValue();
			if (value != null) {
				properties.put(prop.getName().getValue(), prop.getValue());
			}
		}

		return properties;
	}

	public static class JobPropertyProperty implements Comparable<JobPropertyProperty> {

		private final StringProperty name;
		private final SimpleObjectProperty value;
		private final StringProperty type;

		private JobPropertyProperty(String name, Object value, String type) {
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleObjectProperty(value);
			this.type = new SimpleStringProperty(type);
		}

		public StringProperty getName() {
			return name;
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public Object getValue() {
			return value.get();
		}

		public void setValue(Object value) {
			this.value.set(value);
		}

		public StringProperty getType() {
			return type;
		}

		public void setValue(String type) {
			this.type.set(type);
		}

		@Override
		public int compareTo(JobPropertyProperty o) {
			return getName().getValue().compareTo(o.getName().getValue());
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
		private final List<Button> actions;

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
			actions = new ArrayList<Button>();

			if (status.equals("FAILED") && jobInstanceStillExists) {
				Button retryButton = new Button("Retry");
				retryButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						JobsController.this.retry(jobInstanceName, po);
					}
				});

				actions.add(retryButton);
			}
			if (status.equals("IN PROGRESS")) {
				Button stopButton = new Button("Stop");
				stopButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						JobsController.this.stop(name);
					}
				});
				actions.add(stopButton);
			}

			if (status.equals("PAUSED")) {
				Button stopButton = new Button("Stop");
				stopButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						JobsController.this.stop(name);
					}
				});
				actions.add(stopButton);
			}
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
			if (endTime != null) {
				return endTime;
			}
			return null;
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

		public long getId() {
			return id.get();
		}

		public void setId(long id) {
			this.id.set(id);
		}

		public List<Button> getActions() {
			return actions;
		}

		public void setActions(List<Button> buttons) {
			actions.clear();
			this.actions.addAll(buttons);
		}

		@Override
		public int compareTo(JobExecutionProperty o) {
			if (getId() < o.getId()) {
				return -1;
			}
			if (getId() == o.getId()) {
				return 0;
			}
			if (getId() > o.getId()) {
				return 1;
			}

			// Make the compiler happy
			return 0;
		}

	}

	private void refreshJobExecutionTable() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Set<TradistaJobExecution> jobExecutions;
				try {
					jobExecutions = batchBusinessDelegate.getJobExecutions(jobExecutionDate.getValue(), po);
					ObservableList<JobExecutionProperty> data = buildJobExecutionsTableContent(jobExecutions);
					jobExecutionsTable.setItems(data);
				} catch (TradistaBusinessException tbe) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
					alert.showAndWait();
				}
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