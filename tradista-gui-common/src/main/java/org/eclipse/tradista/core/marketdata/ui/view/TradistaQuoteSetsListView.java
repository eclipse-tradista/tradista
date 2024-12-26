package org.eclipse.tradista.core.marketdata.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

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

public class TradistaQuoteSetsListView extends ListView<QuoteSet> {

	public TradistaQuoteSetsListView() {
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		Set<QuoteSet> quoteSets = quoteBusinessDelegate.getAllQuoteSets();
		ObservableList<QuoteSet> quoteSetsObservableList = null;
		if (quoteSets == null) {
			quoteSetsObservableList = FXCollections.emptyObservableList();
		} else {
			quoteSetsObservableList = FXCollections.observableArrayList(quoteSets);
		}
		setItems(quoteSetsObservableList);
	}

	private FeedConfig model;

	public FeedConfig getModel() {
		return model;
	}

	public void setModel(FeedConfig model) {
		this.model = model;
	}

}