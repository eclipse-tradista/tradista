package org.eclipse.tradista.core.configuration.ui.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.configuration.model.UIConfiguration;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

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

public class UIConfigurationController extends TradistaControllerAdapter {

	@FXML
	private TextField decimalSeparator;

	@FXML
	private TextField groupingSeparator;

	@FXML
	private ComboBox<RoundingMode> roundingMode;

	@FXML
	private ComboBox<Short> decimalDigits;

	@FXML
	private ComboBox<String> styles;

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		UIConfiguration uiConfiguration = null;
		try {
			uiConfiguration = configurationBusinessDelegate.getUIConfiguration(ClientUtil.getCurrentUser());
		} catch (TradistaBusinessException tbe) {
		}
		decimalDigits
				.setItems(FXCollections.observableArrayList((short) 1, (short) 2, (short) 3, (short) 4, (short) 5));
		roundingMode.setItems(FXCollections.observableArrayList(RoundingMode.values()));
		roundingMode.getItems().remove(RoundingMode.UNNECESSARY);

		if (uiConfiguration.getDecimalFormat().getDecimalFormatSymbols() != null) {
			decimalSeparator.setText(
					String.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator()));
			groupingSeparator.setText(String
					.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getGroupingSeparator()));
		}
		roundingMode.getSelectionModel().select(uiConfiguration.getDecimalFormat().getRoundingMode());
		decimalDigits.getSelectionModel()
				.select(Short.valueOf((short) uiConfiguration.getDecimalFormat().getMaximumFractionDigits()));
		styles.setItems(FXCollections.observableArrayList(configurationBusinessDelegate.getAllStyles()));
		styles.setValue(uiConfiguration.getStyle());
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save UI Configuration");
		confirmation.setHeaderText("Save UI Configuration");
		confirmation.setContentText("Do you want to save this UI Configuration?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			DecimalFormat uiDecimalFormat = new DecimalFormat();
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			UIConfiguration uiConfiguration = new UIConfiguration(ClientUtil.getCurrentUser());
			if (!StringUtils.isEmpty(decimalSeparator.getText())) {
				dfs.setGroupingSeparator(decimalSeparator.getText().charAt(0));
			} else {
				dfs.setGroupingSeparator(Character.MIN_VALUE);
			}
			if (!StringUtils.isEmpty(groupingSeparator.getText())) {
				dfs.setGroupingSeparator(groupingSeparator.getText().charAt(0));
			} else {
				dfs.setGroupingSeparator(Character.MIN_VALUE);
			}
			uiDecimalFormat.setDecimalFormatSymbols(dfs);
			if (roundingMode.getValue() != null) {
				uiDecimalFormat.setRoundingMode(roundingMode.getValue());
			}
			if (decimalDigits.getValue() != null) {
				uiDecimalFormat.setMaximumFractionDigits(decimalDigits.getValue());
			}
			uiConfiguration.setDecimalFormat(uiDecimalFormat);
			uiConfiguration.setStyle(styles.getValue());
			try {
				configurationBusinessDelegate.saveUIConfiguration(uiConfiguration);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@Override
	@FXML
	public void refresh() {
		UIConfiguration uiConfiguration = null;
		try {
			uiConfiguration = configurationBusinessDelegate.getUIConfiguration(ClientUtil.getCurrentUser());
		} catch (TradistaBusinessException abe) {
		}

		if (uiConfiguration.getDecimalFormat().getDecimalFormatSymbols() != null) {
			decimalSeparator.setText(
					String.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator()));
			groupingSeparator.setText(String
					.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getGroupingSeparator()));
		}
		roundingMode.getSelectionModel().select(uiConfiguration.getDecimalFormat().getRoundingMode());
		decimalDigits.getSelectionModel()
				.select(Short.valueOf((short) uiConfiguration.getDecimalFormat().getMaximumFractionDigits()));
		styles.setValue(uiConfiguration.getStyle());
	}

}