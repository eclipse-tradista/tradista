package org.eclipse.tradista.core.book.ui.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ColorUtil;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.xdev.chartjs.model.charts.DoughnutChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.DoughnutData;
import software.xdev.chartjs.model.dataset.DoughnutDataset;
import software.xdev.chartjs.model.options.DoughnutOptions;

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

@Named
@ViewScoped
public class BookController implements Serializable {

	private static final long serialVersionUID = -7107650532883558795L;

	private String productDonutModel;

	private String cashDonutModel;

	private BookBusinessDelegate bookBusinessDelegate;

	@PostConstruct
	public void init() {
		bookBusinessDelegate = new BookBusinessDelegate();
		loadBook();
	}

	public String getProductDonutModel() {
		return productDonutModel;
	}

	public void setProductDonutModel(String productDonutModel) {
		this.productDonutModel = productDonutModel;
	}

	public String getCashDonutModel() {
		return cashDonutModel;
	}

	public void setCashDonutModel(String cashDonutModel) {
		this.cashDonutModel = cashDonutModel;
	}

	public void loadBook() {
		try {
			refresh(bookBusinessDelegate.getBookByName("Demo Book"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void refresh(Book book) {
		Map<String, Map<String, BigDecimal>> bookContent = null;

		List<Number> productValues = new ArrayList<>();
		List<Number> currencyValues = new ArrayList<>();

		List<RGBAColor> blueColors = new ArrayList<>();
		blueColors.addAll(ColorUtil.getBlueColorsList());

		List<RGBAColor> redColors = new ArrayList<>();
		redColors.addAll(ColorUtil.getRedColorsList());

		List<String> productLabels = new ArrayList<>();
		List<String> currencyLabels = new ArrayList<>();

		try {
			bookContent = bookBusinessDelegate.getBookContent(book.getId());
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

		if (bookContent != null && !bookContent.isEmpty()) {
			Map<String, BigDecimal> products = bookContent.get("Product");
			if (products != null && !products.isEmpty()) {
				for (Map.Entry<String, BigDecimal> entry : products.entrySet()) {
					productLabels.add(entry.getKey());
					productValues.add(entry.getValue().doubleValue());
				}
			}
			Map<String, BigDecimal> currencies = bookContent.get("Currency");
			if (currencies != null && !currencies.isEmpty()) {
				for (Map.Entry<String, BigDecimal> entry : currencies.entrySet()) {
					currencyLabels.add(entry.getKey());
					currencyValues.add(entry.getValue().doubleValue());
				}
			}
		}

		cashDonutModel = new DoughnutChart()
				.setData(new DoughnutData()
						.addDataset(new DoughnutDataset().setData(currencyValues)
								.addBackgroundColors((Object[]) redColors.toArray(new RGBAColor[0])))
						.setLabels(currencyLabels))
				.setOptions(new DoughnutOptions().setMaintainAspectRatio(Boolean.FALSE)).toJson();

		productDonutModel = new DoughnutChart()
				.setData(new DoughnutData()
						.addDataset(new DoughnutDataset().setData(productValues)
								.addBackgroundColors((Object[]) blueColors.toArray(new RGBAColor[0])))
						.setLabels(productLabels))
				.setOptions(new DoughnutOptions().setMaintainAspectRatio(Boolean.FALSE)).toJson();
	}

}