package org.eclipse.tradista.core.pricing.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

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

public class PricingParameterCreatorDialog extends TradistaDialog<PricingParameter> {

	public PricingParameterCreatorDialog() {
		super();
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		setTitle("Pricing Parameter Creation");
		setHeaderText("Please specify a name and a quote set for the Pricing Parameter Set to create.");
		Label nameLabel = new Label("Name: ");
		Label quoteSetLabel = new Label("Quote set: ");
		TextField nameTextField = new TextField();
		ComboBox<QuoteSet> quoteSetComboBox = new ComboBox<>();
		Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
		ObservableList<QuoteSet> qs;
		if (quoteSets == null || quoteSets.isEmpty()) {
			qs = FXCollections.emptyObservableList();
		} else {
			qs = FXCollections.observableArrayList(quoteSets);
		}
		quoteSetComboBox.setItems(qs);
		quoteSetComboBox.getSelectionModel().selectFirst();
		GridPane grid = new GridPane();
		grid.setStyle("-fx-padding: 20; -fx-hgap: 20; -fx-vgap: 20;");
		grid.add(nameLabel, 1, 1);
		grid.add(nameTextField, 2, 1);
		grid.add(quoteSetLabel, 1, 2);
		grid.add(quoteSetComboBox, 2, 2);
		getDialogPane().setContent(grid);
		ButtonType buttonTypeOk = new ButtonType("Create", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		getDialogPane().getButtonTypes().add(buttonTypeCancel);
		setResultConverter(new Callback<>() {
			@Override
			public PricingParameter call(ButtonType b) {
				if (b == buttonTypeOk) {
					return new PricingParameter(nameTextField.getText(), quoteSetComboBox.getValue(),
							ClientUtil.getCurrentUser().getProcessingOrg());
				}
				return null;
			}
		});
		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

}
