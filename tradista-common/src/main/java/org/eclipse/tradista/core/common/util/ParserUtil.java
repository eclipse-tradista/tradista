package org.eclipse.tradista.core.common.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

public final class ParserUtil {

	private ParserUtil() {
	}

	public static List<? extends TradistaObject> parse(File file, String objectName, Map<String, String> config)
			throws TradistaTechnicalException {
		String extension = null;
		StringBuilder errMsg = new StringBuilder();
		if (file == null) {
			errMsg.append("The file cannot be null.\n");
		} else {
			errMsg.append(isValidFile(file, true));
		}
		if (objectName == null || objectName.isEmpty()) {
			errMsg.append("The object name cannot be null or empty.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
		TradistaParser parser = TradistaUtil.getInstance(TradistaParser.class, "org.eclipse.tradista.core.parsing."
				+ extension.toLowerCase() + "." + extension.toUpperCase() + "Parser");
		parser.setConfig(config);

		return parser.parseFile(file, objectName);
	}

	/**
	 * Checks if the file is valid (ie: if the file exists and is not a directory)
	 * 
	 * @param file          a file
	 * @param withExtension boolean to indicate if the file should have an extension
	 * @return a string with error messages. If it is empty, the file is valid.
	 */
	public static String isValidFile(File file, boolean withExtension) {
		StringBuilder errMsg = new StringBuilder();
		if (!file.exists()) {
			errMsg.append("The file doesn't exist.\n");
		} else {
			if (file.isDirectory()) {
				errMsg.append(String.format("The file (%s) should not be a directory.%n", file.getPath()));
			} else {
				if (withExtension) {
					String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
					if (!file.getName().contains(".") || StringUtils.isEmpty(extension)) {
						errMsg.append(
								String.format("The file name (%s) should contain an extension.%n", file.getName()));
					}
				}
			}
		}
		return errMsg.toString();
	}

}