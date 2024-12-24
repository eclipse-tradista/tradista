package org.eclipse.tradista.core.common.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.parsing.parser.TradistaParser;

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

public class ParserUtil {

	public static List<? extends TradistaObject> parse(File file, String objectName, Map<String, String> config)
			throws TradistaTechnicalException {
		String extension;
		if (file == null) {
			throw new TradistaTechnicalException("The file cannot be null.");
		}
		if (!file.exists()) {
			throw new TradistaTechnicalException("The file doesn't exist.");
		}
		if (objectName == null || objectName.isEmpty()) {
			throw new TradistaTechnicalException("The object name cannot be null or empty.");
		}
		extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
		TradistaParser parser = TradistaUtil.getInstance(TradistaParser.class,
				"org.eclipse.tradista.core.parser." + extension + "." + extension.toUpperCase() + "Parser");
		parser.setConfig(config);

		return parser.parseFile(file, objectName);
	}
}