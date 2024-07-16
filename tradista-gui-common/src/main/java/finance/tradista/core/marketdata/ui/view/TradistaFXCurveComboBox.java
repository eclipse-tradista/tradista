package finance.tradista.core.marketdata.ui.view;

import java.util.Set;

import finance.tradista.core.marketdata.model.FXCurve;
import finance.tradista.core.marketdata.service.FXCurveBusinessDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

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

public class TradistaFXCurveComboBox extends ComboBox<FXCurve> {

	public TradistaFXCurveComboBox() {
		FXCurveBusinessDelegate fxCurveBusinessDelegate = new FXCurveBusinessDelegate();
		Set<FXCurve> allFxCurves = fxCurveBusinessDelegate.getAllFXCurves();
		ObservableList<FXCurve> fxCurvesObservableList = null;
		if (allFxCurves == null) {
			fxCurvesObservableList = FXCollections.emptyObservableList();
		} else {
			fxCurvesObservableList = FXCollections.observableArrayList(allFxCurves);
		}
		setItems(fxCurvesObservableList);
	}

}