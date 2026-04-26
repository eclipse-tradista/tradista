package org.eclipse.tradista.core.marketdata.ui.view;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;

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

public class TradistaFeedConfigComboBox extends ComboBox<FeedConfig> {

	public TradistaFeedConfigComboBox() {
		TradistaGUIUtil.fillFeedConfigComboBox(this);
	}

	private FeedConfig model;

	public FeedConfig getModel() {
		return model;
	}

	public void setModel(FeedConfig model) {
		this.model = model;
	}

}