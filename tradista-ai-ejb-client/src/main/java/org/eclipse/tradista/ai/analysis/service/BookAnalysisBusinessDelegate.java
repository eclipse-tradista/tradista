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
package org.eclipse.tradista.ai.analysis.service;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

public class BookAnalysisBusinessDelegate {

	private BookAnalysisService bookAnalysisService;

	public BookAnalysisBusinessDelegate() {
		bookAnalysisService = TradistaServiceLocator.getInstance().getBookAnalysisService();
	}

	public String analyseBook(Book book) throws TradistaBusinessException {
		if (book == null) {
			throw new TradistaBusinessException("The book is mandatory.");
		}
		return SecurityUtil.runEx(() -> bookAnalysisService.analyseBook(book));
	}

}