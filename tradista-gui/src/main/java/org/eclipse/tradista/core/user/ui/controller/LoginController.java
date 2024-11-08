package org.eclipse.tradista.core.user.ui.controller;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class LoginController extends TradistaControllerAdapter {

	@FXML
	private TextField login;

	@FXML
	private PasswordField password;

	private UserBusinessDelegate userBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		userBusinessDelegate = new UserBusinessDelegate();
	}

	@FXML
	protected void login() {
		try {
			User user = userBusinessDelegate.login(login.getText(), password.getText());
			((Stage) login.getScene().getWindow()).close();
		} catch (TradistaBusinessException | TradistaTechnicalException e) {
			login.clear();
			password.clear();
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, e.getMessage(), "Default");
			alert.showAndWait();
		}
	}

	@FXML
	protected void cancel() {
		System.exit(0);
	}

}