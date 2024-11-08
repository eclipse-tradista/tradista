package org.eclipse.tradista.web.demo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ColorUtil;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.elements.Fill;

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
public class InventoryController implements Serializable {

	private static final long serialVersionUID = 279320826504459625L;

	private String lineModel;

	private BookBusinessDelegate bookBusinessDelegate;

	@PostConstruct
	public void init() {
		bookBusinessDelegate = new BookBusinessDelegate();
		loadInventory();
	}

	public String getLineModel() {
		return lineModel;
	}

	public void setLineModel(String lineModel) {
		this.lineModel = lineModel;
	}

	public void loadInventory() {
		Set<ProductInventory> inventory = null;

		List<String> bgColors = new ArrayList<>();
		bgColors.addAll(ColorUtil.getBlueColorsAsStringList());

		List<String> labels = new ArrayList<>();
		LineData data = new LineData();
		LineChart lineChart = new LineChart();

		Set<LocalDate> daysOfTheWeek = new TreeSet<>();

		for (int i = 0; i < 7; i++) {
			LocalDate date = LocalDate.now().plus(i, ChronoUnit.DAYS);
			daysOfTheWeek.add(date);
			labels.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		}
		Book book = null;

		try {
			book = bookBusinessDelegate.getBookByName("Demo Book");
			inventory = new ProductInventoryBusinessDelegate().getProductInventories(LocalDate.now(),
					LocalDate.now().plusDays(6), "Equity", 0, book.getId(), false);
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

		if (inventory != null && !inventory.isEmpty()) {
			Map<String, Set<ProductInventory>> invMap = new HashMap<>();

			for (ProductInventory inv : inventory) {
				if (!invMap.containsKey(inv.getProduct().toString())) {
					Set<ProductInventory> invSet = new HashSet<>();
					invSet.add(inv);
					invMap.put(inv.getProduct().toString(), invSet);
				} else {
					Set<ProductInventory> invSet = invMap.get(inv.getProduct().toString());
					invSet.add(inv);
					invMap.put(inv.getProduct().toString(), invSet);
				}
			}

			if (invMap != null && !invMap.isEmpty()) {
				int i = 0;
				for (Map.Entry<String, Set<ProductInventory>> entry : invMap.entrySet()) {
					LineDataset dataSet = new LineDataset();
					dataSet.setLabel(entry.getKey());
					dataSet.setFill(new Fill<Boolean>(false));
					dataSet.setLineTension(0.1f);
					dataSet.setBorderColor(ColorUtil.getBlueColorsAsStringList().get(i));
					List<Number> values = new ArrayList<>();
					for (LocalDate d : daysOfTheWeek) {
						for (ProductInventory inv : entry.getValue()) {
							if ((inv.getTo() == null)) {
								if (!inv.getFrom().isAfter(d)) {
									values.add(inv.getQuantity());
								}
							} else {
								if (d.isEqual(inv.getFrom()) || d.isEqual(inv.getTo())
										|| (d.isAfter(inv.getFrom()) && (d.isBefore(inv.getTo()))))
									values.add(inv.getQuantity());
							}
						}
					}
					dataSet.setData(values);
					data.addDataset(dataSet);
					i++;
				}
			}
		}

		data.setLabels(labels);

		lineChart.setData(data).setOptions(new LineOptions().setMaintainAspectRatio(Boolean.FALSE));

		lineModel = lineChart.toJson();

	}

}