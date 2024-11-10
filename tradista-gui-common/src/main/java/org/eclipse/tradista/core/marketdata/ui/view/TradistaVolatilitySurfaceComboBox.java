package org.eclipse.tradista.core.marketdata.ui.view;

import java.util.List;

import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
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

public class TradistaVolatilitySurfaceComboBox extends ComboBox<VolatilitySurface<?, ?, ?>> {

	public TradistaVolatilitySurfaceComboBox() {
		this(null);
	}

	public TradistaVolatilitySurfaceComboBox(@NamedArg("surfaceType") String surfaceType) {
		SurfaceBusinessDelegate surfaceBusinessDelegate = new SurfaceBusinessDelegate();
		List<VolatilitySurface<?, ?, ?>> allSurfaces = surfaceBusinessDelegate.getSurfaces(surfaceType);
		if (allSurfaces != null && !allSurfaces.isEmpty()) {
			setItems(FXCollections.observableArrayList(allSurfaces));
		}
	}

}