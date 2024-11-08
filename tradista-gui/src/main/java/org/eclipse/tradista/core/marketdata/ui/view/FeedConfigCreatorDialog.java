package org.eclipse.tradista.core.marketdata.ui.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.FeedType;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

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

public class FeedConfigCreatorDialog extends TradistaDialog<FeedConfig> {

	public FeedConfigCreatorDialog() {
		super();
		setTitle("Feed Configuration Creation");
		setHeaderText("Please specify a name and a feed type for the Feed Configuration to create.");
		Label nameLabel = new Label("Name: ");
		Label typeLabel = new Label("Feed Type: ");
		TextField nameTextField = new TextField();
		ComboBox<FeedType> feedTypeComboBox = new ComboBox<FeedType>();
		feedTypeComboBox.setItems(FXCollections.observableArrayList(FeedType.values()));
		feedTypeComboBox.getSelectionModel().selectFirst();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(typeLabel, 1, 2);
		grid.add(feedTypeComboBox, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<ButtonType, FeedConfig>() {
			@Override
			public FeedConfig call(ButtonType b) {
				if (b == buttonTypeOk) {
					return new FeedConfig(nameTextField.getText(), feedTypeComboBox.getValue(),
							ClientUtil.getCurrentUser().getProcessingOrg());
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents((Stage) getDialogPane().getScene().getWindow(), 0);
	}

}
