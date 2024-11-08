package org.eclipse.tradista.core.configuration.model;

import java.text.DecimalFormat;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.user.model.User;

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

public class UIConfiguration extends TradistaObject {

	private static final long serialVersionUID = 9187245529679559251L;

	private DecimalFormat decimalFormat;

	private String style;

	@Id
	private User user;

	public UIConfiguration(User user) {
		this.user = user;
		decimalFormat = new DecimalFormat();
		decimalFormat.setParseBigDecimal(true);
		style = "Default";
	}

	public DecimalFormat getDecimalFormat() {
		if (decimalFormat == null) {
			return null;
		}
		return (DecimalFormat) decimalFormat.clone();
	}

	public void setDecimalFormat(DecimalFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public User getUser() {
		if (user == null) {
			return null;
		}
		return (User) user.clone();
	}

	@Override
	public UIConfiguration clone() {
		UIConfiguration uiConfiguration = (UIConfiguration) super.clone();
		uiConfiguration.user = TradistaModelUtil.clone(user);
		if (decimalFormat != null) {
			uiConfiguration.decimalFormat = (DecimalFormat) decimalFormat.clone();
		}
		return uiConfiguration;
	}

}