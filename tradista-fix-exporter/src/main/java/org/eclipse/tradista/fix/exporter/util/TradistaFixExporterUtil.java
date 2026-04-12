package org.eclipse.tradista.fix.exporter.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.service.MappingBusinessDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public final class TradistaFixExporterUtil {

	private static final Logger logger = LoggerFactory.getLogger(TradistaFixExporterUtil.class);

	private static MappingBusinessDelegate mappingBusinessDelegate = new MappingBusinessDelegate();

	private TradistaFixExporterUtil() {
	}

	public static String getFixLegalEntity(String exporterName, String leShortName, long poId)
			throws TradistaBusinessException {
		String mappedLeName;
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(exporterName)) {
			errMsg.append(String.format("the exporter name is mandatory.%n"));
		}
		if (StringUtils.isBlank(leShortName)) {
			errMsg.append(String.format("the counterparty short name is mandatory.%n"));
		}
		if (poId <= 0) {
			errMsg.append(String.format("The processing org id (%d) must be positive.%n", poId));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		try {
			mappedLeName = mappingBusinessDelegate.getMappingValue(exporterName, MappingType.LegalEntity,
					InterfaceMappingSet.Direction.OUTGOING, leShortName, poId);
			if (mappedLeName == null) {
				logger.info(
						"No mapping found for Legal Entity short name: {}, using directly this value in the fix message.",
						leShortName);
				mappedLeName = leShortName;
			}
		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(
					String.format("There was an issue retrieving the legal entity name mapped to %s: %s", leShortName,
							tbe.getMessage()));
		}
		return mappedLeName;
	}

	public static String getFixBook(String importerName, String bookName, long poId) throws TradistaBusinessException {
		String mapppedBookName;
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(importerName)) {
			errMsg.append(String.format("the importer name is mandatory.%n"));
		}
		if (StringUtils.isBlank(bookName)) {
			errMsg.append(String.format("the book name is mandatory.%n"));
		}
		if (poId <= 0) {
			errMsg.append(String.format("The processing org id (%d) must be positive.%n", poId));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		try {
			mapppedBookName = mappingBusinessDelegate.getMappingValue(importerName, MappingType.Book,
					InterfaceMappingSet.Direction.OUTGOING, bookName, poId);
			if (mapppedBookName == null) {
				logger.info("No mapping found for book name: {}, using directly this value in the fix message.",
						bookName);
				mapppedBookName = bookName;
			}
		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(String
					.format("There was an issue retrieving the book mapped to %s: %s", bookName, tbe.getMessage()));
		}
		return mapppedBookName;
	}
}