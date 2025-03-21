package org.eclipse.tradista.core.marketdata.model;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.legalentity.model.BlankLegalEntity;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public final class BlankQuoteSet extends QuoteSet {

	private static final long serialVersionUID = -2783448783739367552L;
	private static final BlankQuoteSet instance = new BlankQuoteSet();

	private BlankQuoteSet() {
		super(StringUtils.EMPTY, BlankLegalEntity.getInstance());
	}

	public static BlankQuoteSet getInstance() {
		return instance;
	}

}