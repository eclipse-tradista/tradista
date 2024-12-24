package org.eclipse.tradista.core.configuration.service;

import java.math.RoundingMode;

import org.eclipse.tradista.core.configuration.model.UIConfiguration;
import org.eclipse.tradista.core.user.model.User;

import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Remote
public interface ConfigurationService {

	short getScale();

	RoundingMode getRoundingMode();

	UIConfiguration getUIConfiguration(User user);

	void saveUIConfiguration(UIConfiguration uiConfiguration);

}