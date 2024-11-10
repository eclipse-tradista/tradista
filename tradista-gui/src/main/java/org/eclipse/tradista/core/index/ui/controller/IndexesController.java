package org.eclipse.tradista.core.index.ui.controller;

import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.model.Index.Fixing;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class IndexesController extends TradistaControllerAdapter {

	@FXML
	private TextField name;

	@FXML
	private Label nameLabel;

	@FXML
	private TextArea description;

	@FXML
	private ComboBox<Fixing> fixing;

	private IndexBusinessDelegate indexBusinessDelegate;

	@FXML
	private ComboBox<Index> load;

	private Index index;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {
		indexBusinessDelegate = new IndexBusinessDelegate();
		TradistaGUIUtil.fillComboBox(indexBusinessDelegate.getAllIndexes(), load);
		fixing.setItems(FXCollections.observableArrayList(Fixing.values()));
		fixing.setValue(Fixing.PREFIXED);
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Index");
		confirmation.setHeaderText("Save Index");
		confirmation.setContentText("Do you want to save this Index?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				if (name.isVisible()) {
					new Index(name.getText());
					nameLabel.setText(name.getText());
				}
				index.setDescription(description.getText());
				index.setPrefixed(fixing.getValue().equals(Fixing.PREFIXED));
				index.setId(indexBusinessDelegate.saveIndex(index));
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
			dialog.setTitle("Index Copy");
			dialog.setHeaderText("Do you want to copy this Index ?");
			dialog.setContentText("Please enter the name of the new Index:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				Index copyIndex = new Index(result.get());
				copyIndex.setDescription(description.getText());
				copyIndex.setPrefixed(fixing.getValue().equals(Fixing.PREFIXED));
				copyIndex.setId(indexBusinessDelegate.saveIndex(copyIndex));
				index = copyIndex;
				name.setVisible(false);
				nameLabel.setVisible(true);
				nameLabel.setText(index.getName());
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void load() {
		Index index = null;
		String indexName = null;
		try {

			if (load.getValue() != null) {
				indexName = load.getValue().getName();
			} else {
				throw new TradistaBusinessException("Please specify a name.");
			}

			index = indexBusinessDelegate.getIndexByName(indexName);

			if (index == null) {
				throw new TradistaBusinessException(
						String.format("The index %s doesn't exist in the system.", load.getValue().getName()));
			}

			load(index);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Index index) {
		this.index = index;
		description.setText(index.getDescription());
		fixing.setValue(index.isPrefixed() ? Fixing.PREFIXED : Fixing.POSTFIXED);
		name.setVisible(false);
		nameLabel.setText(index.getName());
		nameLabel.setVisible(true);
	}

	@Override
	@FXML
	public void clear() {
		index = null;
		name.clear();
		description.clear();
		nameLabel.setText("");
		name.setVisible(true);
		nameLabel.setVisible(false);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillComboBox(indexBusinessDelegate.getAllIndexes(), load);
	}

}