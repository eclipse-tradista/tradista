package org.eclipse.tradista.fx.common.ui.util;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public final class TradistaFXGUIUtil {

	private TradistaFXGUIUtil() {
		/* This utility class should not be instantiated */
	}

	private static FXVolatilitySurfaceBusinessDelegate fxVolatilitySurfaceBusinessDelegate = new FXVolatilitySurfaceBusinessDelegate();

	@SafeVarargs
	public static void fillFXVolatilitySurfaceComboBox(ComboBox<FXVolatilitySurface>... comboBoxes) {
		Set<FXVolatilitySurface> surfaces = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				surfaces = fxVolatilitySurfaceBusinessDelegate
						.getFXVolatilitySurfacesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			surfaces = fxVolatilitySurfaceBusinessDelegate.getAllFXVolatilitySurfaces();
		}

		ObservableList<FXVolatilitySurface> data = null;
		if (surfaces != null && !surfaces.isEmpty()) {
			data = FXCollections.observableArrayList(surfaces);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<FXVolatilitySurface>, ListCell<FXVolatilitySurface>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(FXVolatilitySurface surface, boolean empty) {
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
			for (ComboBox<FXVolatilitySurface> cb : comboBoxes) {
				FXVolatilitySurface element = cb.getValue();
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
