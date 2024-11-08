package org.eclipse.tradista.core.book.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@FacesConverter("bookConverter")
public class BookConverter implements Serializable, Converter<Book> {

	private static final long serialVersionUID = 3802860683043711768L;
	private BookBusinessDelegate bookBusinessDelegate;

	public BookConverter() {
		bookBusinessDelegate = new BookBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Book book) {
		return book.toString();
	}

	@Override
	public Book getAsObject(FacesContext context, UIComponent component, String value) {
		Book book = null;
		try {
			book = bookBusinessDelegate.getBookByName(value);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert book %s", value), tbe);
		}
		return book;
	}

}