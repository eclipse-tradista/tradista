package org.eclipse.tradista.core.user.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaChoiceDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
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
	private ComboBox<String> load;

	private User user;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		userBusinessDelegate = new UserBusinessDelegate();
		Set<User> users = userBusinessDelegate.getAllUsers();
		List<String> userSurnames = users == null ? new ArrayList<String>()
				: users.stream().map(u -> u.getSurname()).collect(Collectors.toList());
		TradistaGUIUtil.fillComboBox(userSurnames, load);
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save User");
		confirmation.setHeaderText("Save User");
		confirmation.setContentText("Do you want to save this User?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (user == null) {
					user = new User(firstName.getText(), surname.getText(),
							ClientUtil.getCurrentUser().getProcessingOrg());
				}
				user.setLogin(login.getText());
				user.setPassword(password.getText());
				user.setId(userBusinessDelegate.saveUser(user));
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		long oldUserId = 0;
		try {
			TradistaTextInputDialog dialog = new TradistaTextInputDialog();
			dialog.setTitle("User Copy");
			dialog.setHeaderText("Do you want to copy this User ?");
			dialog.setContentText("Please enter the login of the new User:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				if (user == null) {
					user = new User(firstName.getText(), surname.getText(),
							ClientUtil.getCurrentUser().getProcessingOrg());
				}
				user.setLogin(result.get());
				user.setPassword(password.getText());
				oldUserId = user.getId();
				user.setId(0);
				user.setId(userBusinessDelegate.saveUser(user));
				login.setText(user.getLogin());
			}
		} catch (TradistaBusinessException tbe) {
			user.setId(oldUserId);
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		String surname = null;
		Set<User> users = null;
		try {

			if (load.getValue() != null) {
				surname = load.getValue();
			} else {
				throw new TradistaBusinessException("Please specify a surname.");
			}

			users = userBusinessDelegate.getUsersBySurname(surname);

			if (users.size() > 1) {
				TradistaChoiceDialog<User> dialog = new TradistaChoiceDialog<User>((User) users.toArray()[0], users);
				dialog.setTitle("User Selection");
				dialog.setHeaderText("Please choose a User");
				dialog.setContentText("Selected User:");

				Optional<User> result = dialog.showAndWait();
				result.ifPresent(user -> load(user));
			} else {
				load((User) users.toArray()[0]);
			}

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
	}

	@Override
	@FXML
	public void clear() {
		user = null;
		firstName.clear();
		surname.clear();
		login.clear();
		password.clear();
	}

}