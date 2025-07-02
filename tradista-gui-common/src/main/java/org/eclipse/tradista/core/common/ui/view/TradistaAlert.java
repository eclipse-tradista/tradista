package org.eclipse.tradista.core.common.ui.view;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class TradistaAlert extends Alert {

	public void init(String style) {
		initStyle(StageStyle.UNDECORATED);
		TradistaGUIUtil.setTradistaIcons((Stage) getDialogPane().getScene().getWindow());
		if (style == null) {
			try {
				style = new ConfigurationBusinessDelegate().getUIConfiguration(ClientUtil.getCurrentUser()).getStyle();
			} catch (TradistaBusinessException _) {
				// Not expected here.
			}
		}
		getDialogPane().getStylesheets().add("/" + style + "Style.css");
		getDialogPane().getStyleClass().add("root");
		TradistaGUIUtil.resizeComponents(getDialogPane().getScene().getWindow());
	}

	public TradistaAlert(AlertType alertType) {
		super(alertType);
		init(null);
	}

	public TradistaAlert(AlertType alertType, String contentText, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		init(null);
	}

	public TradistaAlert(AlertType alertType, String contentText, String style, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		init(style);
	}

}