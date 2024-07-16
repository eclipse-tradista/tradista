package finance.tradista.mm.common.ui.util;

import finance.tradista.mm.loandeposit.model.LoanDepositTrade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public final class TradistaMMGUIUtil {

	@SafeVarargs
	public static void fillLoanDepositTypeComboBox(ComboBox<LoanDepositTrade.Direction>... comboBoxes) {
		ObservableList<LoanDepositTrade.Direction> data = FXCollections
				.observableArrayList(LoanDepositTrade.Direction.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<LoanDepositTrade.Direction> cb : comboBoxes) {
				LoanDepositTrade.Direction element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillLoanDepositInterestTypeComboBox(ComboBox<LoanDepositTrade.InterestType>... comboBoxes) {
		ObservableList<LoanDepositTrade.InterestType> data = FXCollections
				.observableArrayList(LoanDepositTrade.InterestType.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<LoanDepositTrade.InterestType> cb : comboBoxes) {
				LoanDepositTrade.InterestType element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

}