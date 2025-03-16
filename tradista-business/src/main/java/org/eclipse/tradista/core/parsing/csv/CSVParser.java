package org.eclipse.tradista.core.parsing.csv;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.parsing.parser.TradistaObjectParser;
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

public class CSVParser extends TradistaParser {

	@SuppressWarnings("unchecked")
	@Override
	public List<TradistaObject> parseFile(File file, String objectName) {
		TradistaObjectParser<TradistaObject, CSVRecord> objectParser;
		List<TradistaObject> objects = new ArrayList<>();
		org.apache.commons.csv.CSVParser parser = null;
		try {
			CSVFormat csvFormat = CSVFormat.RFC4180;
			if (config != null) {
				String fieldSeparator = config.get("fieldSeparator");
				if (fieldSeparator != null && !fieldSeparator.isEmpty()) {
					csvFormat.withRecordSeparator(fieldSeparator.substring(0, 1));
				}
			}
			parser = org.apache.commons.csv.CSVParser.parse(file, StandardCharsets.ISO_8859_1, csvFormat);
			objectParser = TradistaUtil.getInstance(TradistaObjectParser.class,
					"org.eclipse.tradista.core.parsing.csv.CSV" + objectName + "Parser");
		} catch (IOException ioe) {
			throw new TradistaTechnicalException(ioe);
		}
		for (CSVRecord csvRecord : parser) {
			objects.add(objectParser.parse(csvRecord));
		}

		return objects;
	}

}