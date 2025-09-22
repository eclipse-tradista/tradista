package org.eclipse.tradista.core.message.util;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;

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

public final class MessageUtil {

	private static Set<String> allObjectTypes;

	private MessageUtil() {
		allObjectTypes = new MessageBusinessDelegate().getAllObjectTypes();
	}

	public static String getObjectType(Class<? extends TradistaObject> klass) {
		if (klass == null) {
			throw new TradistaTechnicalException("Class is mandatory to determine the message object type");
		}
		String classSimpleName = klass.getSimpleName();
		if (allObjectTypes.contains(classSimpleName)) {
			return classSimpleName;
		}
		return null;
	}

}