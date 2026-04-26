package org.eclipse.tradista.core.marketdata.ui.view;

import java.util.Set;

import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.eclipse.tradista.core.common.util.ClientUtil;

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

public class TradistaInterestRateCurveComboBox extends ComboBox<InterestRateCurve> {

	public TradistaInterestRateCurveComboBox() {
		InterestRateCurveBusinessDelegate interesteRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();
		Set<InterestRateCurve> allInterestRateCurves = interesteRateCurveBusinessDelegate.getAllInterestRateCurves();
		ObservableList<InterestRateCurve> interesteRateCurvesObservableList = null;
		if (allInterestRateCurves == null) {
			interesteRateCurvesObservableList = FXCollections.emptyObservableList();
		} else {
			interesteRateCurvesObservableList = FXCollections.observableArrayList(allInterestRateCurves);
		}
		setItems(interesteRateCurvesObservableList);

		Callback<ListView<InterestRateCurve>, ListCell<InterestRateCurve>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(InterestRateCurve curve, boolean empty) {
				super.updateItem(curve, empty);
				if (empty || curve == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = curve.getProcessingOrg() == null ? "Global" : curve.getProcessingOrg().getShortName();
					setText(curve.getName() + " [" + poSuffix + "]");
				} else {
					setText(curve.getName());
				}
			}
		};

		setCellFactory(cellFactory);
		setButtonCell(cellFactory.call(null));
	}

}