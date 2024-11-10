package org.eclipse.tradista.core.exchange.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.tradista.core.calendar.model.BlankCalendar;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.exchange.service.ExchangeBusinessDelegate;
import org.eclipse.tradista.core.exchange.ui.view.ExchangeCreatorDialog;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

public class ExchangesController extends TradistaControllerAdapter {

	@FXML
	private TextField code;

	@FXML
	private Label codeLabel;

	@FXML
	private TextField name;

	@FXML
	private CheckBox isOtc;

	@FXML
	private ComboBox<Calendar> calendar;

	private ExchangeBusinessDelegate exchangeBusinessDelegate;

	private CalendarBusinessDelegate calendarBusinessDelegate;

	@FXML
	private ComboBox<Exchange> load;

	private Exchange exchange;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		exchangeBusinessDelegate = new ExchangeBusinessDelegate();
		calendarBusinessDelegate = new CalendarBusinessDelegate();
		TradistaGUIUtil.fillComboBox(calendarBusinessDelegate.getAllCalendars(), calendar);
		calendar.getItems().add(0, BlankCalendar.getInstance());
		calendar.getSelectionModel().selectFirst();
		TradistaGUIUtil.fillComboBox(exchangeBusinessDelegate.getAllExchanges(), load);
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Exchange");
		confirmation.setHeaderText("Save Exchange");
		confirmation.setContentText("Do you want to save this Exchange?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (code.isVisible()) {
					exchange = new Exchange(code.getText());
					codeLabel.setText(code.getText());
				}
				if (!calendar.getValue().equals(BlankCalendar.getInstance())) {
					exchange.setCalendar(calendar.getValue());
				} else {
					exchange.setCalendar(null);
				}
				exchange.setName(name.getText());
				exchange.setOtc(isOtc.isSelected());
				exchange.setId(exchangeBusinessDelegate.saveExchange(exchange));
				code.setVisible(false);
				codeLabel.setVisible(true);
			} catch (TradistaBusinessException | TradistaTechnicalException te) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		try {
			ExchangeCreatorDialog dialog = new ExchangeCreatorDialog();
			Optional<Exchange> result = dialog.showAndWait();
			if (result.isPresent()) {
				Exchange copyExchange = new Exchange(result.get().getCode());
				copyExchange.setName(result.get().getName());
				if (!calendar.getValue().equals(BlankCalendar.getInstance())) {
					copyExchange.setCalendar(calendar.getValue());
				} else {
					copyExchange.setCalendar(null);
				}
				copyExchange.setOtc(isOtc.isSelected());
				copyExchange.setId(exchangeBusinessDelegate.saveExchange(copyExchange));
				exchange = copyExchange;
				code.setVisible(false);
				codeLabel.setVisible(true);
				codeLabel.setText(exchange.getCode());
				name.setText(exchange.getName());
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		Exchange exchange = null;
		String exchangeCode = null;
		try {

			if (load.getValue() != null) {
				exchangeCode = load.getValue().getCode();
			} else {
				throw new TradistaBusinessException("Please specify a short name.");
			}

			exchange = exchangeBusinessDelegate.getExchangeByCode(exchangeCode);

			if (exchange == null) {
				throw new TradistaBusinessException(
						String.format("The exchange %s doesn't exist in the system.", load.getValue().getCode()));
			}

			load(exchange);
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Exchange exchange) {
		this.exchange = exchange;
		if (exchange.getCalendar() != null) {
			calendar.setValue(exchange.getCalendar());
		} else {
			calendar.setValue(BlankCalendar.getInstance());
		}
		code.setVisible(false);
		codeLabel.setText(exchange.getCode());
		codeLabel.setVisible(true);
		isOtc.setSelected(exchange.isOtc());
		name.setText(exchange.getName());
	}

	@Override
	@FXML
	public void clear() {
		exchange = null;
		code.clear();
		codeLabel.setText("");
		code.setVisible(true);
		codeLabel.setVisible(false);
		name.clear();
	}

	@Override
	@FXML
	public void refresh() {
		try {
			List<Calendar> calendars = new ArrayList<>();
			calendars.add(BlankCalendar.getInstance());
			calendars.addAll(calendarBusinessDelegate.getAllCalendars());
			TradistaGUIUtil.fillComboBox(calendars, calendar);
			TradistaGUIUtil.fillComboBox(exchangeBusinessDelegate.getAllExchanges(), load);
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}