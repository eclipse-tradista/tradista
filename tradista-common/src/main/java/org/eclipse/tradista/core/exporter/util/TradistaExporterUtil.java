package org.eclipse.tradista.core.exporter.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.exporter.model.OutgoingMessageManager;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

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

public final class TradistaExporterUtil {

	private static Map<String, OutgoingMessageManager<?, ?>> outgoingMessageManagerCache = new ConcurrentHashMap<>();

	private TradistaExporterUtil() {
	}

	public static OutgoingMessageManager<?, ?> getOutgoingMessageManager(String productType, String messageType)
			throws TradistaBusinessException {
		if (!outgoingMessageManagerCache.containsKey(productType)) {
			String className = "org.eclipse.tradista." + new ProductBusinessDelegate().getProductFamily(productType)
					+ "." + productType.toLowerCase() + "." + messageType.toLowerCase() + "." + productType
					+ messageType + "OutgoingMessageManager";
			outgoingMessageManagerCache.put(productType,
					TradistaUtil.getInstance(OutgoingMessageManager.class, className));
		}
		return outgoingMessageManagerCache.get(productType);
	}

}