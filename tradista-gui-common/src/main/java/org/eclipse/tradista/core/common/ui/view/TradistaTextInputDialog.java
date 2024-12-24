package org.eclipse.tradista.core.common.ui.view;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;

import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

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

public class TradistaTextInputDialog extends TextInputDialog {

	public TradistaTextInputDialog() {
		super();
		TradistaGUIUtil.setTradistaIcons((Stage) getDialogPane().getScene().getWindow());
		setResizable(false);
		try {
			getDialogPane().getStylesheets().add(
					"/" + new ConfigurationBusinessDelegate().getUIConfiguration(ClientUtil.getCurrentUser()).getStyle()
							+ "Style.css");
		} catch (TradistaBusinessException tbe) {
		}
		getDialogPane().getStyleClass().add("root");
	}

}