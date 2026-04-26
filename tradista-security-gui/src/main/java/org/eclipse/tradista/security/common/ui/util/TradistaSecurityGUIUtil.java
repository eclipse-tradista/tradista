package org.eclipse.tradista.security.common.ui.util;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equityoption.model.BlankEquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.service.EquityOptionBusinessDelegate;
import org.eclipse.tradista.security.equityoption.service.EquityOptionContractSpecificationBusinessDelegate;
import org.eclipse.tradista.security.equityoption.service.EquityOptionVolatilitySurfaceBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

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

public final class TradistaSecurityGUIUtil {

	private static EquityOptionContractSpecificationBusinessDelegate equityOptionContractSpecificationBusinessDelegate = new EquityOptionContractSpecificationBusinessDelegate();

	private static EquityBusinessDelegate equityBusinessDelegate = new EquityBusinessDelegate();

	private static EquityOptionBusinessDelegate equityOptionBusinessDelegate = new EquityOptionBusinessDelegate();

	private static EquityOptionVolatilitySurfaceBusinessDelegate equityOptionVolatilitySurfaceBusinessDelegate = new EquityOptionVolatilitySurfaceBusinessDelegate();

	@SafeVarargs
	public static void fillEquityOptionContractSpecificationComboBox(
			ComboBox<EquityOptionContractSpecification>... comboBoxes) {
		Set<EquityOptionContractSpecification> specifications = equityOptionContractSpecificationBusinessDelegate
				.getAllEquityOptionContractSpecifications();
		ObservableList<EquityOptionContractSpecification> data = null;
		if (specifications != null && !specifications.isEmpty()) {
			data = FXCollections.observableArrayList(specifications);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<EquityOptionContractSpecification> cb : comboBoxes) {
				EquityOptionContractSpecification element = cb.getValue();
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
	public static void fillEquityComboBox(ComboBox<Equity>... comboBoxes) {
		Set<Equity> equities = equityBusinessDelegate.getAllEquities();
		ObservableList<Equity> data = null;
		if (equities != null && !equities.isEmpty()) {
			data = FXCollections.observableArrayList(equities);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<Equity> cb : comboBoxes) {
				Equity element = cb.getValue();
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
	public static void fillEquityOptionComboBox(ComboBox<EquityOption>... comboBoxes) {
		fillEquityOptionComboBox(false, comboBoxes);
	}

	@SafeVarargs
	public static void fillEquityOptionComboBox(boolean addBlank, ComboBox<EquityOption>... comboBoxes) {
		Set<EquityOption> equtyOptions = equityOptionBusinessDelegate.getAllEquityOptions();
		ObservableList<EquityOption> data = null;
		if (equtyOptions != null && !equtyOptions.isEmpty()) {
			data = FXCollections.observableArrayList(equtyOptions);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (addBlank) {
			data.add(0, BlankEquityOption.getInstance());
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<EquityOption> cb : comboBoxes) {
				EquityOption element = cb.getValue();
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
	public static void fillEquityOptionVolatilitySurfaceComboBox(
			ComboBox<EquityOptionVolatilitySurface>... comboBoxes) {
		Set<EquityOptionVolatilitySurface> surfaces = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				surfaces = equityOptionVolatilitySurfaceBusinessDelegate
						.getEquityOptionVolatilitySurfacesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			surfaces = equityOptionVolatilitySurfaceBusinessDelegate.getAllEquityOptionVolatilitySurfaces();
		}

		ObservableList<EquityOptionVolatilitySurface> data = null;
		if (surfaces != null && !surfaces.isEmpty()) {
			data = FXCollections.observableArrayList(surfaces);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<EquityOptionVolatilitySurface>, ListCell<EquityOptionVolatilitySurface>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(EquityOptionVolatilitySurface surface, boolean empty) {
				super.updateItem(surface, empty);
				if (empty || surface == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = surface.getProcessingOrg() == null ? "Global"
							: surface.getProcessingOrg().getShortName();
					setText(surface.getName() + " [" + poSuffix + "]");
				} else {
					setText(surface.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<EquityOptionVolatilitySurface> cb : comboBoxes) {
				EquityOptionVolatilitySurface element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

}