package org.eclipse.tradista.core.book.ui.controller;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

public class BooksController extends TradistaControllerAdapter {

	@FXML
	private TextField name;

	@FXML
	private Label nameLabel;

	@FXML
	private TextArea description;

	private BookBusinessDelegate bookBusinessDelegate;

	@FXML
	private ComboBox<Book> bookComboBox;

	@FXML
	private TradistaBookPieChart bookChartPane;

	@FXML
	private Button saveButton;

	@FXML
	private Button copyButton;

	private Book book;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		bookBusinessDelegate = new BookBusinessDelegate();
		TradistaGUIUtil.fillBookComboBox(bookComboBox);

		copyButton.setDisable(true);
	}

	@FXML
	protected void save() {
		try {
			boolean isNew = (name.isVisible());
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin() && isNew) {
				TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog("Book",
						ClientUtil.getCurrentProcessingOrg(), false);
				Optional<LegalEntity> result = dialog.showAndWait();
				if (result.isPresent()) {
					po = result.get();
					proceed = true;
				}
			} else {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Save Book");
				confirmation.setHeaderText("Save Book");
				confirmation.setContentText("Do you want to save this Book?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					proceed = true;
					if (!isNew) {
						po = book.getProcessingOrg();
					} else {
						po = ClientUtil.getCurrentUser().getProcessingOrg();
					}
				}
			}

			if (proceed) {
				if (isNew) {
					book = new Book(name.getText(), po);
				}
				book.setDescription(description.getText());
				book.setId(bookBusinessDelegate.saveBook(book));
				load(book);
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
				TradistaCopyDialog dialog = new TradistaCopyDialog("Book", book.getProcessingOrg(), book.getName(),
						false);
				Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get().getName();
					po = result.get().getProcessingOrg();
					proceed = true;
				}
			} else {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("Book Copy");
				dialog.setHeaderText("Do you want to copy this Book ?");
				dialog.setContentText("Please enter the name of the new Book:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get();
					po = ClientUtil.getCurrentUser().getProcessingOrg();
					proceed = true;
				}
			}

			if (proceed) {
				Book copyBook = new Book(copyName, po);
				copyBook.setDescription(description.getText());
				copyBook.setId(bookBusinessDelegate.saveBook(copyBook));
				load(copyBook);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		try {
			if (bookComboBox.getValue() == null) {
				throw new TradistaBusinessException("Please select a book.");
			}
			load(bookComboBox.getValue());
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Book book) {
		this.book = book;
		description.setText(book.getDescription());
		name.setVisible(false);
		nameLabel.setText(book.getName());
		nameLabel.setVisible(true);
		bookChartPane.updateBookChart(book);
		copyButton.setDisable(false);
	}

	@Override
	@FXML
	public void clear() {
		book = null;
		name.clear();
		description.clear();
		nameLabel.setText(StringUtils.EMPTY);
		name.setVisible(true);
		nameLabel.setVisible(false);
		bookChartPane.clear();
		copyButton.setDisable(true);
	}

	@Override
	@FXML
	public void refresh() {
		Book b = bookComboBox.getValue();
		TradistaGUIUtil.fillBookComboBox(bookComboBox);
		if (b != null && !bookComboBox.getItems().contains(b)) {
			book = null;
			name.setVisible(true);
			name.clear();
			nameLabel.setVisible(false);
			nameLabel.setText(StringUtils.EMPTY);
			description.clear();
			bookChartPane.clear();
			copyButton.setDisable(true);
		}
	}

}