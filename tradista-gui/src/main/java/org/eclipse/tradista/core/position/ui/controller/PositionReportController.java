package org.eclipse.tradista.core.position.ui.controller;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.position.model.BlankPositionDefinition;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
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

public class PositionReportController extends TradistaControllerAdapter {

	@FXML
	private DatePicker valueDateFromDatePicker;

	@FXML
	private DatePicker valueDateToDatePicker;

	@FXML
	private ComboBox<PositionDefinition> positionDefinitionComboBox;

	@FXML
	private TableView<Position> report;

	@FXML
	private TableColumn<Position, String> dateTime;

	@FXML
	private TableColumn<Position, String> book;

	@FXML
	private TableColumn<Position, String> productType;

	@FXML
	private TableColumn<Position, String> productId;

	@FXML
	private TableColumn<Position, String> counterparty;

	@FXML
	private TableColumn<Position, String> pnl;

	@FXML
	private TableColumn<Position, String> realizedPnl;

	@FXML
	private TableColumn<Position, String> unrealizedPnl;

	@FXML
	private TableColumn<Position, String> quantity;

	@FXML
	private TableColumn<Position, String> averagePrice;

	private PositionBusinessDelegate positionBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		positionBusinessDelegate = new PositionBusinessDelegate();

		dateTime.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().getValueDateTime().toString());
			}
		});

		book.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().getPositionDefinition().getBook().getName());
			}
		});

		productType.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().getPositionDefinition().getProductType());
			}
		});

		productId.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				Product product = p.getValue().getPositionDefinition().getProduct();
				if (product != null) {
					return new ReadOnlyObjectWrapper<String>(Long.toString(product.getId()));
				}
				return new ReadOnlyObjectWrapper<String>("");
			}
		});

		counterparty.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				String shortName = "";
				LegalEntity cpty = p.getValue().getPositionDefinition().getCounterparty();
				if (cpty != null) {
					shortName = cpty.getShortName();
				}
				return new ReadOnlyObjectWrapper<String>(shortName);
			}
		});

		pnl.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(TradistaGUIUtil.formatAmount(p.getValue().getPnl()));
			}
		});

		realizedPnl.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(TradistaGUIUtil.formatAmount(p.getValue().getRealizedPnl()));
			}
		});

		unrealizedPnl.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				return new ReadOnlyObjectWrapper<String>(TradistaGUIUtil.formatAmount(p.getValue().getUnrealizedPnl()));
			}
		});

		quantity.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				String quantity = StringUtils.EMPTY;
				if (p.getValue().getQuantity() != null) {
					quantity = TradistaGUIUtil.formatAmount(p.getValue().getQuantity());
				}
				return new ReadOnlyObjectWrapper<String>(quantity);
			}
		});

		averagePrice.setCellValueFactory(new Callback<CellDataFeatures<Position, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Position, String> p) {
				String averagePrice = StringUtils.EMPTY;
				if (p.getValue().getQuantity() != null) {
					averagePrice = TradistaGUIUtil.formatAmount(p.getValue().getAveragePrice());
				}
				return new ReadOnlyObjectWrapper<String>(averagePrice);
			}
		});

		TradistaGUIUtil.fillPositionDefinitionComboBox(true, positionDefinitionComboBox);
	}

	@FXML
	protected void load() {
		if (positionDefinitionComboBox.getValue().equals(BlankPositionDefinition.getInstance())
				&& valueDateFromDatePicker.getValue() == null && valueDateToDatePicker.getValue() == null) {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Load Positions");
			confirmation.setHeaderText("Load Positions");
			confirmation.setContentText(
					"You are loading all the positions present in the system, it can take time. Are you sure to continue?");

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				fillReport();
			}
		} else {
			fillReport();
		}
	}

	private void fillReport() {
		try {
			ObservableList<Position> data = null;
			List<Position> positions;
			positions = positionBusinessDelegate.getPositionsByDefinitionIdAndValueDates(
					positionDefinitionComboBox.getValue().equals(BlankPositionDefinition.getInstance()) ? 0
							: positionDefinitionComboBox.getValue().getId(),
					valueDateFromDatePicker.getValue(), valueDateToDatePicker.getValue());
			if (positions != null) {
				data = FXCollections.observableArrayList(positions);
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
			TradistaGUIUtil.export(report, "Positions", report.getScene().getWindow());
		} catch (TradistaTechnicalException tte) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tte.getMessage());
			alert.showAndWait();
		}
	}

}