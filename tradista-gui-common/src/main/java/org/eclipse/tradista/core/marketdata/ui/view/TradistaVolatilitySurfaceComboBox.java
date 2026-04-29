package org.eclipse.tradista.core.marketdata.ui.view;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

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

public class TradistaVolatilitySurfaceComboBox extends ComboBox<VolatilitySurface<?, ?, ?>> {

	public TradistaVolatilitySurfaceComboBox() {
		this(null);
	}

	public TradistaVolatilitySurfaceComboBox(@NamedArg("surfaceType") String surfaceType) {
		SurfaceBusinessDelegate surfaceBusinessDelegate = new SurfaceBusinessDelegate();
		List<VolatilitySurface<?, ?, ?>> allSurfaces = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				allSurfaces = surfaceBusinessDelegate.getSurfacesByTypeAndPoId(surfaceType,
						ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			try {
				allSurfaces = surfaceBusinessDelegate.getSurfaces(surfaceType);
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		}

		if (allSurfaces != null && !allSurfaces.isEmpty()) {
			setItems(FXCollections.observableArrayList(allSurfaces));
		}

		Callback<ListView<VolatilitySurface<?, ?, ?>>, ListCell<VolatilitySurface<?, ?, ?>>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(VolatilitySurface<?, ?, ?> surface, boolean empty) {
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

		setCellFactory(cellFactory);
		setButtonCell(cellFactory.call(null));
	}

}