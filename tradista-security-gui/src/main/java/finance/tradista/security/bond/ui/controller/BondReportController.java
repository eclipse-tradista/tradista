package org.eclipse.tradista.security.bond.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.service.BondBusinessDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class BondReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker creationDateFromDatePicker;

	@FXML
	private DatePicker creationDateToDatePicker;

	@FXML
	private DatePicker maturityDateFromDatePicker;

	@FXML
	private DatePicker maturityDateToDatePicker;

	@FXML
	private TextField idTextField;

	@FXML
	private TextField isinTextField;

	@FXML
	private TableView<BondProperty> report;

	@FXML
	private TableColumn<BondProperty, String> coupon;

	@FXML
	private TableColumn<BondProperty, String> maturity;

	@FXML
	private TableColumn<BondProperty, String> principal;

	@FXML
	private TableColumn<BondProperty, String> creationDate;

	@FXML
	private TableColumn<BondProperty, String> datedDate;

	@FXML
	private TableColumn<BondProperty, Number> id;

	@FXML
	private TableColumn<BondProperty, String> issuer;

	@FXML
	private TableColumn<BondProperty, String> isin;

	private BondBusinessDelegate bondBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		bondBusinessDelegate = new BondBusinessDelegate();

		id.setCellValueFactory(cellData -> cellData.getValue().getId());
		coupon.setCellValueFactory(cellData -> cellData.getValue().getCoupon());
		maturity.setCellValueFactory(cellData -> cellData.getValue().getMaturityDate());
		principal.setCellValueFactory(cellData -> cellData.getValue().getPrincipal());
		creationDate.setCellValueFactory(cellData -> cellData.getValue().getCreationDate());
		datedDate.setCellValueFactory(cellData -> cellData.getValue().getDatedDate());
		issuer.setCellValueFactory(cellData -> cellData.getValue().getIssuer());
		isin.setCellValueFactory(cellData -> cellData.getValue().getIsin());
	}

	@FXML
	protected void load() {
		ObservableList<BondProperty> data = null;
		if (!idTextField.getText().isEmpty()) {
			Bond bond = bondBusinessDelegate.getBondById(Long.parseLong(idTextField.getText()));
			if (bond != null) {
				BondProperty bp = new BondProperty(bond);
				data = FXCollections.observableArrayList(bp);
			}
			report.setItems(data);
			report.refresh();

		} else if (!isinTextField.getText().isEmpty()) {
			Set<Bond> bonds = bondBusinessDelegate.getBondsByIsin(isinTextField.getText());
			if (bonds != null) {
				if (!bonds.isEmpty()) {
					List<BondProperty> bpList = new ArrayList<>(bonds.size());
					for (Bond bond : bonds) {
						bpList.add(new BondProperty(bond));
					}
					data = FXCollections.observableArrayList(bpList);
				}
			}
			report.setItems(data);
			report.refresh();

		} else {
			if (maturityDateFromDatePicker.getValue() == null && maturityDateToDatePicker.getValue() == null
					&& creationDateFromDatePicker.getValue() == null && creationDateToDatePicker.getValue() == null) {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Load Bonds");
				confirmation.setHeaderText("Load Bonds");
				confirmation.setContentText(
						"You are loading all the bonds present in the system, it can take time. Are you sure to continue?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.get() == ButtonType.OK) {
					fillReport();
				}

			} else {
				fillReport();
			}
		}
	}

	private void fillReport() {
		ObservableList<BondProperty> data = null;
		Set<Bond> bonds;
		try {
			bonds = bondBusinessDelegate.getBondsByDates(creationDateFromDatePicker.getValue(),
					creationDateToDatePicker.getValue(), maturityDateFromDatePicker.getValue(),
					maturityDateToDatePicker.getValue());
			if (bonds != null) {
				if (!bonds.isEmpty()) {
					List<BondProperty> bpList = new ArrayList<>(bonds.size());
					for (Bond bond : bonds) {
						bpList.add(new BondProperty(bond));
					}
					data = FXCollections.observableArrayList(bpList);
				}
			}
			report.setItems(data);
			report.refresh();
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void export() {
		try {
			TradistaGUIUtil.export(report, "Bonds", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}
}