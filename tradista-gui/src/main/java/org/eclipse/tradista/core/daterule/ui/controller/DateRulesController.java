package org.eclipse.tradista.core.daterule.ui.controller;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class DateRulesController extends TradistaControllerAdapter {

	@FXML
	private TextField name, dateOffset;

	@FXML
	private Label nameLabel;

	@FXML
	private DatePicker startDate;

	@FXML
	private CheckBox january, february, march, april, may, june, july, august, september, october, november, december;

	@FXML
	private ComboBox<DateRollingConvention> drc;

	private DateRuleBusinessDelegate dateRuleBusinessDelegate;

	private CalendarBusinessDelegate calendarBusinessDelegate;

	@FXML
	private ComboBox<DateRule> load, subDateRuleName;

	private DateRule dateRule;

	@FXML
	private CheckBox isSequence;

	@FXML
	private ComboBox<String> position;

	@FXML
	private ComboBox<String> day;

	@FXML
	private TableColumn<DateRuleDurationProperty, String> dateRuleName;

	@FXML
	private TableColumn<DateRuleDurationProperty, String> dateRuleDuration;

	@FXML
	private TableColumn<DateRuleDurationProperty, List<Button>> dateRuleMove;

	@FXML
	private TableView<DateRuleDurationProperty> dateRulesDurations;

	@FXML
	private GridPane singleGrid, sequenceGrid;

	@FXML
	private ComboBox<Integer> subDateRuleYear, subDateRuleMonth, subDateRuleDay;

	@FXML
	private ComboBox<Calendar> calendar;

	@FXML
	private ListView<Calendar> calendarsList;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		dateRuleBusinessDelegate = new DateRuleBusinessDelegate();
		calendarBusinessDelegate = new CalendarBusinessDelegate();
		day.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String newValue) {
				if (newValue != null) {
					if (newValue.equals("Any")) {
						TradistaGUIUtil.fillComboBox(Arrays.asList(DateRule.DAY_POSITIONS), position);
					} else {
						TradistaGUIUtil.fillComboBox(Arrays.asList(DateRule.WEEK_DAY_POSITIONS), position);
					}
				}
			}
		});
		isSequence.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean newValue) {
				if (newValue != null) {
					singleGrid.setVisible(!isSequence.isSelected());
					sequenceGrid.setVisible(isSequence.isSelected());
				}
			}
		});
		dateRuleName.setCellValueFactory(cellData -> cellData.getValue().getDateRuleName());
		dateRuleDuration.setCellValueFactory(cellData -> cellData.getValue().getDateRuleDuration());

		dateRuleMove.setCellValueFactory(new PropertyValueFactory<DateRuleDurationProperty, List<Button>>("moves"));

		dateRuleMove.setCellFactory(
				new Callback<TableColumn<DateRuleDurationProperty, List<Button>>, TableCell<DateRuleDurationProperty, List<Button>>>() {
					@Override
					public TableCell<DateRuleDurationProperty, List<Button>> call(
							TableColumn<DateRuleDurationProperty, List<Button>> dateRuleDurationTableColumn) {
						return new DateRuleDurationMovesCellFactory(dateRulesDurations);
					}
				});

		dateRulesDurations.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<DateRuleDurationProperty>() {

					@Override
					public void changed(ObservableValue<? extends DateRuleDurationProperty> observable,
							DateRuleDurationProperty oldValue, DateRuleDurationProperty newValue) {
						if (newValue != null) {
							Period period = toPeriod(newValue.getDateRuleDuration().getValue());
							subDateRuleDay.setValue(period.getDays());
							subDateRuleMonth.setValue(period.getMonths());
							subDateRuleYear.setValue(period.getYears());
							subDateRuleName.getSelectionModel()
									.select(new DateRule(newValue.getDateRuleName().getValue()));
						}

					}
				});

		startDate.setValue(LocalDate.now());

		Set<Integer> ints = new HashSet<Integer>(100);
		for (int i = 0; i <= 100; i++) {
			ints.add(i);
		}
		TradistaGUIUtil.fillComboBox(ints, subDateRuleYear, subDateRuleMonth, subDateRuleDay);

		TradistaGUIUtil.fillDateRollingConventionComboBox(drc);

		TradistaGUIUtil.fillDayComboBox(true, day);

		TradistaGUIUtil.fillComboBox(dateRuleBusinessDelegate.getAllDateRules(), load, subDateRuleName);

		TradistaGUIUtil.fillComboBox(calendarBusinessDelegate.getAllCalendars(), calendar);

	}

	private class DateRuleDurationMovesCellFactory extends TableCell<DateRuleDurationProperty, List<Button>> {

		DateRuleDurationMovesCellFactory(final TableView<DateRuleDurationProperty> table) {
			super();
		}

		@Override
		protected void updateItem(List<Button> item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) {
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				VBox vBox = new VBox();
				vBox.getChildren().addAll(item);
				setGraphic(vBox);
			} else {
				setGraphic(null);
			}
		}
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Date Rule");
		confirmation.setHeaderText("Save Date Rule");
		confirmation.setContentText("Do you want to save this Date Rule?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (name.isVisible()) {
					dateRule = new DateRule(name.getText());
					nameLabel.setText(name.getText());
				}
				dateRule.setSequence(isSequence.isSelected());
				if (isSequence.isSelected()) {
					Map<DateRule, Period> dateRulesPeriods = toDateRulesPeriods(dateRulesDurations.getItems());
					dateRule.setDateRulesPeriods(dateRulesPeriods);
				} else {
					Set<Month> months = new HashSet<Month>();
					if (january.isSelected()) {
						months.add(Month.JANUARY);
					}
					if (february.isSelected()) {
						months.add(Month.FEBRUARY);
					}
					if (march.isSelected()) {
						months.add(Month.MARCH);
					}
					if (april.isSelected()) {
						months.add(Month.APRIL);
					}
					if (may.isSelected()) {
						months.add(Month.MAY);
					}
					if (june.isSelected()) {
						months.add(Month.JUNE);
					}
					if (july.isSelected()) {
						months.add(Month.JULY);
					}
					if (august.isSelected()) {
						months.add(Month.AUGUST);
					}
					if (september.isSelected()) {
						months.add(Month.SEPTEMBER);
					}
					if (october.isSelected()) {
						months.add(Month.OCTOBER);
					}
					if (november.isSelected()) {
						months.add(Month.NOVEMBER);
					}
					if (december.isSelected()) {
						months.add(Month.DECEMBER);
					}
					dateRule.setDateRollingConvention(drc.getValue());
					dateRule.setMonths(months);
					dateRule.setPosition(position.getValue());
					List<Calendar> calendars = calendarsList.getItems();
					if (!calendars.isEmpty()) {
						dateRule.setCalendars(new HashSet<Calendar>(calendars));
					}
					if (day.getValue() != null && !day.getValue().equals("Any")) {
						dateRule.setDay(DayOfWeek.valueOf(day.getValue()));
					}
					if (!dateOffset.getText().isEmpty()) {
						dateRule.setDateOffset(Short.parseShort(dateOffset.getText()));
					}
				}
				dateRule.setId(dateRuleBusinessDelegate.saveDateRule(dateRule));
				name.setVisible(false);
				nameLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		try {
			TradistaTextInputDialog dialog = new TradistaTextInputDialog();
			dialog.setTitle("Date Rule Copy");
			dialog.setHeaderText("Do you want to copy this Date Rule ?");
			dialog.setContentText("Please enter the name of the new Date Rule:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				DateRule copyDateRule = new DateRule(result.get());
				copyDateRule.setSequence(isSequence.isSelected());
				if (isSequence.isSelected()) {
					Map<DateRule, Period> dateRulesPeriods = toDateRulesPeriods(dateRulesDurations.getItems());
					copyDateRule.setDateRulesPeriods(dateRulesPeriods);
				} else {
					Set<Month> months = new HashSet<Month>();
					if (january.isSelected()) {
						months.add(Month.JANUARY);
					}
					if (february.isSelected()) {
						months.add(Month.FEBRUARY);
					}
					if (march.isSelected()) {
						months.add(Month.MARCH);
					}
					if (april.isSelected()) {
						months.add(Month.APRIL);
					}
					if (may.isSelected()) {
						months.add(Month.MAY);
					}
					if (june.isSelected()) {
						months.add(Month.JUNE);
					}
					if (july.isSelected()) {
						months.add(Month.JULY);
					}
					if (august.isSelected()) {
						months.add(Month.AUGUST);
					}
					if (september.isSelected()) {
						months.add(Month.SEPTEMBER);
					}
					if (october.isSelected()) {
						months.add(Month.OCTOBER);
					}
					if (november.isSelected()) {
						months.add(Month.NOVEMBER);
					}
					if (december.isSelected()) {
						months.add(Month.DECEMBER);
					}
					copyDateRule.setDateRollingConvention(drc.getValue());
					copyDateRule.setMonths(months);
					copyDateRule.setPosition(position.getValue());
					if (day.getValue() != null && !day.getValue().equals("Any")) {
						copyDateRule.setDay(DayOfWeek.valueOf(day.getValue()));
					}
					copyDateRule.setPosition(position.getValue());
					List<Calendar> calendars = calendarsList.getItems();
					if (!calendars.isEmpty()) {
						copyDateRule.setCalendars(new HashSet<Calendar>(calendars));
					}
					if (dateOffset.getText() != null) {
						copyDateRule.setDateOffset(Short.parseShort(dateOffset.getText()));
					}
				}
				copyDateRule.setId(dateRuleBusinessDelegate.saveDateRule(copyDateRule));
				dateRule = copyDateRule;
				name.setVisible(false);
				nameLabel.setVisible(true);
				nameLabel.setText(dateRule.getName());
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}

	}

	@FXML
	protected void load() {
		DateRule dateRule = null;
		String dateRuleName = null;
		try {

			if (load.getValue() != null) {
				dateRuleName = load.getValue().getName();
			} else {
				throw new TradistaBusinessException("Please specify a code.");
			}

			dateRule = dateRuleBusinessDelegate.getDateRuleByName(dateRuleName);

			if (dateRule == null) {
				throw new TradistaBusinessException(
						String.format("The dateRule %s doesn't exist in the system.", dateRuleName));
			}

			load(dateRule);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(DateRule dateRule) {
		this.dateRule = dateRule;
		isSequence.setSelected(dateRule.isSequence());
		if (!dateRule.isSequence()) {
			if (dateRule.getMonths() != null) {
				january.setSelected(dateRule.getMonths().contains(Month.JANUARY));
				february.setSelected(dateRule.getMonths().contains(Month.FEBRUARY));
				march.setSelected(dateRule.getMonths().contains(Month.MARCH));
				april.setSelected(dateRule.getMonths().contains(Month.APRIL));
				may.setSelected(dateRule.getMonths().contains(Month.MAY));
				june.setSelected(dateRule.getMonths().contains(Month.JUNE));
				july.setSelected(dateRule.getMonths().contains(Month.JULY));
				august.setSelected(dateRule.getMonths().contains(Month.AUGUST));
				september.setSelected(dateRule.getMonths().contains(Month.SEPTEMBER));
				october.setSelected(dateRule.getMonths().contains(Month.OCTOBER));
				november.setSelected(dateRule.getMonths().contains(Month.NOVEMBER));
				december.setSelected(dateRule.getMonths().contains(Month.DECEMBER));
			}
			drc.setValue(dateRule.getDateRollingConvention());
			if (dateRule.getDay() == null) {
				day.setValue("Any");
			} else {
				day.setValue(dateRule.getDay().toString());
			}
			position.setValue(dateRule.getPosition());
			Set<Calendar> cals = dateRule.getCalendars();
			if (cals != null) {
				calendarsList.setItems(FXCollections.observableArrayList(cals));
			}
			dateOffset.setText(Integer.toString(dateRule.getDateOffset()));
			dateRulesDurations.getItems().clear();
		} else {
			dateRulesDurations.setItems(
					FXCollections.observableList(toDateRuleDurationProperties(dateRule.getDateRulesPeriods())));
		}
		name.setVisible(false);
		nameLabel.setText(dateRule.getName());
		nameLabel.setVisible(true);

	}

	@FXML
	protected void checkDates() {
		if (startDate.getValue() == null) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A Start date must be selected.");
			alert.showAndWait();
		} else {
			if (dateRule == null) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A date rule must be loaded.");
				alert.showAndWait();
			} else {
				StringBuilder datesString = new StringBuilder();
				datesString.append(String.format("Dates for 1 year starting from %tD:%n", startDate.getValue()));
				Set<LocalDate> dates = dateRuleBusinessDelegate.generateDates(dateRule, startDate.getValue(),
						Period.of(1, 0, 0));
				if (dates != null && !dates.isEmpty()) {
					for (LocalDate d : dates) {
						datesString.append(String.format("%tD%n", d));
					}
				} else {
					datesString.append("No date found.");
				}
				TradistaAlert alert = new TradistaAlert(AlertType.INFORMATION, datesString.toString());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void addDateRule() {
		if (subDateRuleName.getValue() == null) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A date rule must be selected.");
			alert.showAndWait();
		}
		if (subDateRuleName.getValue().getName().equals(name.getText())) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
					"Not possible to add the same date rule as a sub date rule.");
			alert.showAndWait();
		} else {
			if ((subDateRuleYear.getValue() == 0) && (subDateRuleMonth.getValue() == 0)
					&& (subDateRuleDay.getValue() == 0)) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
						"Not possible to add a sub date rule with a duration equal to 0.");
				alert.showAndWait();
			} else {
				if (subDateRuleName.getValue().isSequence()) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
							"Not possible to add a sub date rule that is a sequence.");
					alert.showAndWait();
				} else {
					Period period = Period.of(subDateRuleYear.getValue(), subDateRuleMonth.getValue(),
							subDateRuleDay.getValue());
					DateRuleDurationProperty prop = new DateRuleDurationProperty(subDateRuleName.getValue().getName(),
							toDateRuleDuration(period));
					if (!dateRulesDurations.getItems().contains(prop)) {
						dateRulesDurations.getItems().add(prop);
					}
				}
			}
		}
	}

	@FXML
	protected void deleteDateRule() {
		int index = dateRulesDurations.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			dateRulesDurations.getItems().remove(index);
			dateRulesDurations.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void updateDateRule() {
		int index = dateRulesDurations.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			if (subDateRuleName.getValue() == null) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A date rule must be selected.");
				alert.showAndWait();
			}
			if (subDateRuleName.getValue().getName().equals(name.getText())) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
						"Not possible to update this sub date rule with the parent date rule.");
				alert.showAndWait();
			} else {
				if ((subDateRuleYear.getValue() == 0) && (subDateRuleMonth.getValue() == 0)
						&& (subDateRuleDay.getValue() == 0)) {
					TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
							"Not possible to update this sub date rule with a duration equal to 0.");
					alert.showAndWait();
				} else {
					if (subDateRuleName.getValue().isSequence()) {
						TradistaAlert alert = new TradistaAlert(AlertType.ERROR,
								"Not possible to update this sub date rule with a date rule that is a sequence.");
						alert.showAndWait();
					} else {
						Period period = Period.of(subDateRuleYear.getValue(), subDateRuleMonth.getValue(),
								subDateRuleDay.getValue());
						DateRuleDurationProperty prop = new DateRuleDurationProperty(
								subDateRuleName.getValue().getName(), toDateRuleDuration(period));
						dateRulesDurations.getItems().set(index, prop);
					}
				}
			}
		}
	}

	@FXML
	protected void addCalendar() {
		if (calendar.getValue() == null) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A calendar must be selected.");
			alert.showAndWait();
		} else {
			if (!calendarsList.getItems().contains(calendar.getValue())) {
				calendarsList.getItems().add(calendar.getValue());
			}
		}
	}

	@FXML
	protected void deleteCalendar() {
		int index = calendarsList.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			calendarsList.getItems().remove(index);
			calendarsList.getSelectionModel().clearSelection();
		}
	}

	@Override
	@FXML
	public void clear() {
		dateRule = null;
		name.clear();
		january.setSelected(false);
		february.setSelected(false);
		march.setSelected(false);
		april.setSelected(false);
		may.setSelected(false);
		june.setSelected(false);
		july.setSelected(false);
		august.setSelected(false);
		september.setSelected(false);
		october.setSelected(false);
		november.setSelected(false);
		december.setSelected(false);
		dateRulesDurations.getItems().clear();
		dateOffset.clear();
		calendarsList.getItems().clear();
		startDate.setValue(LocalDate.now());
		isSequence.setSelected(false);
		drc.getSelectionModel().selectFirst();
		position.getSelectionModel().selectFirst();
		day.getSelectionModel().selectFirst();
		calendar.getSelectionModel().selectFirst();
		subDateRuleName.getSelectionModel().selectFirst();
		subDateRuleMonth.getSelectionModel().selectFirst();
		subDateRuleYear.getSelectionModel().selectFirst();
		subDateRuleDay.getSelectionModel().selectFirst();
		nameLabel.setText("");
		name.setVisible(true);
		nameLabel.setVisible(false);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillComboBox(dateRuleBusinessDelegate.getAllDateRules(), load, subDateRuleName);
		TradistaGUIUtil.fillComboBox(calendarBusinessDelegate.getAllCalendars(), calendar);
	}

	public class DateRuleDurationProperty {

		private final StringProperty dateRuleName;
		private final StringProperty dateRuleDuration;
		private List<Button> moves;

		private DateRuleDurationProperty(String dateRuleName, String dateRuleDuration) {
			this.dateRuleName = new SimpleStringProperty(dateRuleName);
			this.dateRuleDuration = new SimpleStringProperty(dateRuleDuration);
			moves = new ArrayList<Button>(2);
			Button up = new Button("Up");
			Button down = new Button("Down");

			up.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					DateRulesController.this.move(true, dateRuleName);
				}
			});
			down.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					DateRulesController.this.move(false, dateRuleName);
				}
			});
			moves.add(up);
			moves.add(down);
		}

		public StringProperty getDateRuleName() {
			return dateRuleName;
		}

		public void setDateRuleName(String dateRuleName) {
			this.dateRuleName.set(dateRuleName);
		}

		public StringProperty getDateRuleDuration() {
			return dateRuleDuration;
		}

		public void setDateRuleDuration(String dateRuleDuration) {
			this.dateRuleDuration.set(dateRuleDuration);
		}

		public List<Button> getMoves() {
			return moves;
		}

		public void setMoves(List<Button> moves) {
			this.moves = moves;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dateRuleName == null) ? 0 : dateRuleName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DateRuleDurationProperty other = (DateRuleDurationProperty) obj;
			if (dateRuleName == null) {
				if (other.dateRuleName != null)
					return false;
			} else if (!dateRuleName.getValue().equals(other.dateRuleName.getValue()))
				return false;
			return true;
		}

	}

	public Map<DateRule, Period> toDateRulesPeriods(ObservableList<DateRuleDurationProperty> items) {
		if (items != null && !items.isEmpty()) {
			Map<DateRule, Period> dateRulesPeriods = new LinkedHashMap<DateRule, Period>(items.size());
			DateRuleBusinessDelegate dateRuleBusinessDelegate = new DateRuleBusinessDelegate();
			for (DateRuleDurationProperty prop : items) {
				DateRule dateRule = dateRuleBusinessDelegate.getDateRuleByName(prop.getDateRuleName().getValue());
				Period period = toPeriod(prop.getDateRuleDuration().getValue());
				dateRulesPeriods.put(dateRule, period);
			}
			return dateRulesPeriods;
		}
		return null;
	}

	public Period toPeriod(String dateRuleDuration) {
		String[] array = dateRuleDuration.split(" - ");
		String yearString = array[0];
		int year = Integer.parseInt(yearString.substring(0, yearString.indexOf("Y")));
		String monthString = array[1];
		int month = Integer.parseInt(monthString.substring(0, monthString.indexOf("M")));
		String dayString = array[2];
		int day = Integer.parseInt(dayString.substring(0, dayString.indexOf("D")));
		return Period.of(year, month, day);
	}

	public String toDateRuleDuration(Period period) {
		return period.getYears() + "Y - " + period.getMonths() + "M - " + period.getDays() + "D";
	}

	protected void move(boolean up, String dateRuleName) {

		int pos = dateRulesDurations.getItems().indexOf(new DateRuleDurationProperty(dateRuleName, null));

		if (up) {
			if (pos > 0) {
				ObservableList<DateRuleDurationProperty> items = dateRulesDurations.getItems();
				DateRuleDurationProperty dr = items.get(pos);
				items.remove(pos);
				items.add(pos - 1, dr);
				dateRulesDurations.setItems(items);
			}
		} else {
			if (pos < dateRulesDurations.getItems().size() - 1) {
				ObservableList<DateRuleDurationProperty> items = dateRulesDurations.getItems();
				DateRuleDurationProperty dr = items.get(pos);
				items.remove(pos);
				items.add(pos + 1, dr);
				dateRulesDurations.setItems(items);
			}
		}
	}

	public List<DateRuleDurationProperty> toDateRuleDurationProperties(Map<DateRule, Period> dateRulesPeriods) {
		if (dateRulesPeriods != null && !dateRulesPeriods.isEmpty()) {
			List<DateRuleDurationProperty> properties = new ArrayList<DateRuleDurationProperty>(
					dateRulesPeriods.size());
			for (Map.Entry<DateRule, Period> entry : dateRulesPeriods.entrySet()) {
				DateRuleDurationProperty prop = new DateRuleDurationProperty(entry.getKey().getName(),
						toDateRuleDuration(entry.getValue()));
				properties.add(prop);
			}
			return properties;
		}
		return null;
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			if (!dateOffset.getText().isEmpty()) {
				new BigDecimal(dateOffset.getText());
			}
		} catch (NumberFormatException nfe) {
			errMsg.append(String.format("The trade price is incorrect: %s.%n", dateOffset.getText()));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}