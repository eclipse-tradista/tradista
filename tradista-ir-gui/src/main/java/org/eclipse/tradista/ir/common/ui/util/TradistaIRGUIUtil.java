package org.eclipse.tradista.ir.common.ui.util;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;

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

public final class TradistaIRGUIUtil {

	private static SwaptionVolatilitySurfaceBusinessDelegate swaptionVolatilitySurfaceBusinessDelegate = new SwaptionVolatilitySurfaceBusinessDelegate();

	@SafeVarargs
	public static void fillIRCapFloorCollarTypeComboBox(ComboBox<IRCapFloorCollarTrade.Type>... comboBoxes) {
		ObservableList<IRCapFloorCollarTrade.Type> data = FXCollections
				.observableArrayList(IRCapFloorCollarTrade.Type.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<IRCapFloorCollarTrade.Type> cb : comboBoxes) {
				IRCapFloorCollarTrade.Type element = cb.getValue();
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
	public static void fillSwaptionVolatilitySurfaceComboBox(ComboBox<SwaptionVolatilitySurface>... comboBoxes) {
		Set<SwaptionVolatilitySurface> surfaces = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				surfaces = swaptionVolatilitySurfaceBusinessDelegate
						.getSwaptionVolatilitySurfacesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			surfaces = swaptionVolatilitySurfaceBusinessDelegate.getAllSwaptionVolatilitySurfaces();
		}

		ObservableList<SwaptionVolatilitySurface> data = null;
		if (surfaces != null && !surfaces.isEmpty()) {
			data = FXCollections.observableArrayList(surfaces);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<SwaptionVolatilitySurface>, ListCell<SwaptionVolatilitySurface>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(SwaptionVolatilitySurface surface, boolean empty) {
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
			for (ComboBox<SwaptionVolatilitySurface> cb : comboBoxes) {
				SwaptionVolatilitySurface element = cb.getValue();
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