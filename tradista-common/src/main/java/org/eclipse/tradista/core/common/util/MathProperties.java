package org.eclipse.tradista.core.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

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

public class MathProperties {

	private static short scale;

	private static RoundingMode roundingMode;

	private static DecimalFormat uiDecimalFormat;

	public static DecimalFormat getUIDecimalFormat() {
		return uiDecimalFormat;
	}

	public static void setUIDecimalFormat(DecimalFormat decimalFormat) {
		MathProperties.uiDecimalFormat = decimalFormat;
	}

	public static short getScale() {
		return scale;
	}

	public void setScale(short scl) {
		scale = scl;
	}

	public static RoundingMode getRoundingMode() {
		return roundingMode;
	}

	public void setRoundingMode(String rMode) {
		roundingMode = RoundingMode.valueOf(rMode);
	}

}