package org.eclipse.tradista.ai.reasoning.common.util;

/********************************************************************************
 * Copyright (c) 2021 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import java.util.Properties;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

public class TradistaAIProperties {

	private TradistaAIProperties() {
	}

	private static String solverPath;

	public static void load(Properties prop) throws TradistaBusinessException {
		if (prop == null) {
			throw new TradistaBusinessException("The properties cannot be null.");
		}
		solverPath = prop.getProperty("solver.path");
	}

	public static String getSolverPath() {
		return solverPath;
	}

}