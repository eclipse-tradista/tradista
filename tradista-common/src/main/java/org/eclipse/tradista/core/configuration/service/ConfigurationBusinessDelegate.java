package org.eclipse.tradista.core.configuration.service;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.configuration.model.UIConfiguration;
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

public class ConfigurationBusinessDelegate {

	private ConfigurationService configurationService;

	public ConfigurationBusinessDelegate() {
		configurationService = TradistaServiceLocator.getInstance().getConfigurationService();
	}

	public String getDefaultStyle() {
		return "Default";
	}

	public UIConfiguration getUIConfiguration(User user) throws TradistaBusinessException {
		if (user == null) {
			throw new TradistaBusinessException("The user is mandatory.");
		}
		return SecurityUtil.run(() -> configurationService.getUIConfiguration(user));
	}

	public void saveUIConfiguration(UIConfiguration configuration) throws TradistaBusinessException {
		if (configuration == null) {
			throw new TradistaBusinessException("The UI Configuration cannot be null.");
		} else {
			StringBuffer errMsg = new StringBuffer();
			if (configuration.getDecimalFormat() == null) {
				errMsg.append("The Decimal Format cannot be null.%n");
			}
			if (StringUtils.isEmpty(configuration.getStyle())) {
				errMsg.append("The Style is mandatory.");
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
		SecurityUtil.run(() -> configurationService.saveUIConfiguration(configuration));
		configuration.getDecimalFormat().setParseBigDecimal(true);
		MathProperties.setUIDecimalFormat(configuration.getDecimalFormat());
	}

	public Set<String> getAllStyles() {
		Set<String> styles = new HashSet<String>();
		styles.add("Default");
		styles.add("Dark");
		return styles;
	}

	public short getScale() {
		return SecurityUtil.run(() -> configurationService.getScale());
	}

	public RoundingMode getRoundingMode() {
		return SecurityUtil.run(() -> configurationService.getRoundingMode());
	}

}