package org.eclipse.tradista.core.importer.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.importer.model.IncomingMessageManager;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public final class TradistaImporterUtil {

	private static Map<String, IncomingMessageManager> incomingMessageManagerCache = new ConcurrentHashMap<>();

	private TradistaImporterUtil() {
	}

	public static IncomingMessageManager getIncomingMessageManager(String productType, String messageType)
			throws TradistaBusinessException {
		if (!incomingMessageManagerCache.containsKey(productType)) {
			String className = "org.eclipse.tradista." + new ProductBusinessDelegate().getProductFamily(productType)
					+ "." + productType.toLowerCase() + "." + messageType.toLowerCase() + "." + productType
					+ messageType + "IncomingMessageManager";
			incomingMessageManagerCache.put(productType,
					TradistaUtil.getInstance(IncomingMessageManager.class, className));
		}
		return incomingMessageManagerCache.get(productType);
	}

}