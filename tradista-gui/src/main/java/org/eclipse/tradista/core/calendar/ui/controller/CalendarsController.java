package org.eclipse.tradista.core.calendar.ui.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/*
 * Copyright 2015 Olivier Asuncion
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

public class CalendarsController extends TradistaControllerAdapter {

	@FXML
	private TextField code;

	@FXML
	private Label codeLabel;

	@FXML
	private TextField name;

	@FXML
	private CheckBox monday, tuesday, wednesday, thursday, friday, saturday, sunday;

	@FXML
	private ListView<LocalDate> holidaysList;

	@FXML
	private DatePicker holiday;

	private CalendarBusinessDelegate calendarBusinessDelegate;

	@FXML
	private ComboBox<Calendar> load;

	private Calendar calendar;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		calendarBusinessDelegate = new CalendarBusinessDelegate();
		TradistaGUIUtil.fillComboBox(calendarBusinessDelegate.getAllCalendars(), load);
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Calendar");
		confirmation.setHeaderText("Save Calendar");
		confirmation.setContentText("Do you want to save this Calendar?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			Set<DayOfWeek> weekEnd = new HashSet<>();
			if (monday.isSelected()) {
				weekEnd.add(DayOfWeek.MONDAY);
			}
			if (tuesday.isSelected()) {
				weekEnd.add(DayOfWeek.TUESDAY);
			}
			if (wednesday.isSelected()) {
				weekEnd.add(DayOfWeek.WEDNESDAY);
			}
			if (thursday.isSelected()) {
				weekEnd.add(DayOfWeek.THURSDAY);
			}
			if (friday.isSelected()) {
				weekEnd.add(DayOfWeek.FRIDAY);
			}
			if (saturday.isSelected()) {
				weekEnd.add(DayOfWeek.SATURDAY);
			}
			if (sunday.isSelected()) {
				weekEnd.add(DayOfWeek.SUNDAY);
			}
			Set<LocalDate> holidays = new HashSet<>();

			holidays.addAll(holidaysList.getItems());

			try {
				if (code.isVisible()) {
					calendar = new Calendar(code.getText());
					codeLabel.setText(code.getText());
				}
				calendar.setName(name.getText());
				calendar.setWeekEnd(weekEnd);
				calendar.setHolidays(holidays);
				calendar.setId(calendarBusinessDelegate.saveCalendar(calendar));
				code.setVisible(false);
				codeLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		TradistaTextInputDialog dialog = new TradistaTextInputDialog();
		dialog.setTitle("Calendar Copy");
		dialog.setHeaderText("Do you want to copy this Calendar ?");
		dialog.setContentText("Please enter the code of the new Calendar:");
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			Set<DayOfWeek> weekEnd = new HashSet<>();
			if (monday.isSelected()) {
				weekEnd.add(DayOfWeek.MONDAY);
			}
			if (tuesday.isSelected()) {
				weekEnd.add(DayOfWeek.TUESDAY);
			}
			if (wednesday.isSelected()) {
				weekEnd.add(DayOfWeek.WEDNESDAY);
			}
			if (thursday.isSelected()) {
				weekEnd.add(DayOfWeek.THURSDAY);
			}
			if (friday.isSelected()) {
				weekEnd.add(DayOfWeek.FRIDAY);
			}
			if (saturday.isSelected()) {
				weekEnd.add(DayOfWeek.SATURDAY);
			}
			if (sunday.isSelected()) {
				weekEnd.add(DayOfWeek.SUNDAY);
			}
			Set<LocalDate> holidays = new HashSet<>();
			holidays.addAll(holidaysList.getItems());
			try {
				Calendar copyCalendar = new Calendar(result.get());
				copyCalendar.setName(name.getText());
				copyCalendar.setWeekEnd(weekEnd);
				copyCalendar.setHolidays(holidays);
				copyCalendar.setId(calendarBusinessDelegate.saveCalendar(copyCalendar));
				calendar = copyCalendar;
				code.setVisible(false);
				codeLabel.setVisible(true);
				codeLabel.setText(calendar.getCode());
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		Calendar calendar = null;
		String calendarCode = null;
		try {

			if (load.getValue() != null) {
				calendarCode = load.getValue().getCode();
			} else {
				throw new TradistaBusinessException("Please specify a code.");
			}

			calendar = calendarBusinessDelegate.getCalendarByCode(calendarCode);

			if (calendar == null) {
				throw new TradistaBusinessException(
						String.format("The calendar %s doesn't exist in the system.", load.getValue().getCode()));
			}

			load(calendar);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Calendar calendar) {
		this.calendar = calendar;
		name.setText(calendar.getName());
		code.setVisible(false);
		codeLabel.setText(calendar.getCode());
		codeLabel.setVisible(true);
		if (calendar.getHolidays() != null) {
			holidaysList.getItems().addAll(calendar.getHolidays());
		}
		if (calendar.getWeekEnd() != null) {
			friday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.FRIDAY));
			monday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.MONDAY));
			saturday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.SATURDAY));
			sunday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.SUNDAY));
			thursday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.THURSDAY));
			tuesday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.TUESDAY));
			wednesday.setSelected(calendar.getWeekEnd().contains(DayOfWeek.WEDNESDAY));
		}
	}

	@FXML
	protected void addHoliday() {
		if (holiday.getValue() == null) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, "A holiday date must be selected.");
			alert.showAndWait();
		} else {
			if (!holidaysList.getItems().contains(holiday.getValue())) {
				holidaysList.getItems().add(holiday.getValue());
			}
		}
	}

	@FXML
	protected void deleteHoliday() {
		int index = holidaysList.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			holidaysList.getItems().remove(index);
			holidaysList.getSelectionModel().clearSelection();
		}
	}

	@Override
	@FXML
	public void clear() {
		calendar = null;
		code.clear();
		codeLabel.setText("");
		code.setVisible(true);
		codeLabel.setVisible(false);
		name.clear();
		monday.setSelected(false);
		tuesday.setSelected(false);
		wednesday.setSelected(false);
		thursday.setSelected(false);
		friday.setSelected(false);
		saturday.setSelected(false);
		sunday.setSelected(false);
		holiday.setValue(null);
		holidaysList.getItems().clear();
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillComboBox(calendarBusinessDelegate.getAllCalendars(), load);
	}

}