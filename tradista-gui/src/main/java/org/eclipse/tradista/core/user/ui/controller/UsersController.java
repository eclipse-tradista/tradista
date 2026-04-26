package org.eclipse.tradista.core.user.ui.controller;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

public class UsersController extends TradistaControllerAdapter {

	@FXML
	private TextField firstName;

	@FXML
	private TextField surname;

	@FXML
	private TextField login;

	@FXML
	private PasswordField password;

	private UserBusinessDelegate userBusinessDelegate;

	@FXML
	private ComboBox<User> userComboBox;

	@FXML
	private Button saveButton;

	@FXML
	private Button copyButton;

	private User user;

	public void initialize() {
		userBusinessDelegate = new UserBusinessDelegate();
		TradistaGUIUtil.fillUserComboBox(userComboBox);

		copyButton.setDisable(true);

		userComboBox.valueProperty().addListener((_, _, u) -> copyButton.setDisable(u == null));
	}

	@FXML
	protected void save() {
		try {
			boolean isNew = (user == null);
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin() && isNew) {
				TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog("User",
						ClientUtil.getCurrentProcessingOrg(), false);
				Optional<LegalEntity> result = dialog.showAndWait();
				if (result.isPresent()) {
					po = result.get();
					proceed = true;
				}
			} else {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Save User");
				confirmation.setHeaderText("Save User");
				confirmation.setContentText("Do you want to save this User?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					proceed = true;
					if (!isNew) {
						po = user.getProcessingOrg();
					} else {
						po = ClientUtil.getCurrentUser().getProcessingOrg();
					}
				}
			}

			if (proceed) {
				if (isNew) {
					user = new User(firstName.getText(), surname.getText(), po);
				}
				user.setLogin(login.getText());
				user.setPassword(password.getText());
				user.setId(userBusinessDelegate.saveUser(user));
				load(user);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		long oldUserId = 0;
		try {
			String copyLogin = null;
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin()) {
				TradistaCopyDialog dialog = new TradistaCopyDialog("User", user.getProcessingOrg(), user.getLogin(),
						false);
				dialog.setContentText("Please enter the login of the new User:");
				Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyLogin = result.get().getName();
					po = result.get().getProcessingOrg();
					proceed = true;
				}
			} else {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("User Copy");
				dialog.setHeaderText("Do you want to copy this User ?");
				dialog.setContentText("Please enter the login of the new User:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyLogin = result.get();
					po = ClientUtil.getCurrentUser().getProcessingOrg();
					proceed = true;
				}
			}

			if (proceed) {
				User copyUser = new User(firstName.getText(), surname.getText(), po);
				copyUser.setLogin(copyLogin);
				copyUser.setPassword(password.getText());
				oldUserId = user.getId();
				copyUser.setId(0);
				copyUser.setId(userBusinessDelegate.saveUser(copyUser));
				user = copyUser;
				login.setText(user.getLogin());
				copyButton.setDisable(false);
			}
		} catch (TradistaBusinessException tbe) {
			if (oldUserId != 0) {
				user.setId(oldUserId);
			}
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		try {
			if (userComboBox.getValue() == null) {
				throw new TradistaBusinessException("Please select a user.");
			}
			load(userComboBox.getValue());
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(User user) {
		this.user = user;
		firstName.setText(user.getFirstName());
		surname.setText(user.getSurname());
		login.setText(user.getLogin());
		password.setText(user.getPassword());
		copyButton.setDisable(false);
	}

	@Override
	@FXML
	public void clear() {
		user = null;
		firstName.clear();
		surname.clear();
		login.clear();
		password.clear();
		copyButton.setDisable(true);
	}

}