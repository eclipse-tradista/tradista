package org.eclipse.tradista.core.book.ui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

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

public class TradistaBookPieChart extends GridPane {

	@FXML
	private Label bookLabel;

	@FXML
	private PieChart cashBookChart;

	@FXML
	private PieChart productBookChart;

	@FXML
	private Label cashBookIsEmpty;

	@FXML
	private Label productBookIsEmpty;

	@FXML
	private RadioButton cashRadioButton;

	@FXML
	private RadioButton productRadioButton;

	private BookBusinessDelegate bookBusinessDelegate;

	public TradistaBookPieChart() {
		bookBusinessDelegate = new BookBusinessDelegate();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/BookPieChart.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new TradistaTechnicalException(exception);
		}
	}

	public boolean isTitleVisible() {
		return bookLabel.isVisible();
	}

	public void setTitleVisible(boolean isVisible) {
		bookLabel.setVisible(isVisible);
	}

	public void initialize() {
		final ToggleGroup group = new ToggleGroup();
		cashRadioButton.setToggleGroup(group);
		productRadioButton.setToggleGroup(group);

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				if (newToggle != null) {
					RadioButton rb = ((RadioButton) newToggle);
					if (rb.getText().equals("Cash")) {
						updateView(true);
					} else {
						updateView(false);
					}
				}
			}
		});
	}

	private void updateView(boolean isCash) {
		if (isCash) {
			productBookChart.setVisible(false);
			productBookIsEmpty.setVisible(false);
			if (cashBookChart.getData().isEmpty()) {
				cashBookIsEmpty.setVisible(true);
				cashBookChart.setVisible(false);
			} else {
				cashBookIsEmpty.setVisible(false);
				cashBookChart.setVisible(true);
			}
		} else {
			cashBookChart.setVisible(false);
			cashBookIsEmpty.setVisible(false);
			if (productBookChart.getData().isEmpty()) {
				productBookIsEmpty.setVisible(true);
				productBookChart.setVisible(false);
			} else {
				productBookIsEmpty.setVisible(false);
				productBookChart.setVisible(true);
			}
		}
	}

	public void updateBookChart(Book book) {
		cashRadioButton.setVisible(true);
		productRadioButton.setVisible(true);
		Map<String, Map<String, BigDecimal>> bookContent;
		try {
			bookContent = bookBusinessDelegate.getBookContent(book.getId());
			List<PieChart.Data> cashData = new ArrayList<PieChart.Data>();
			List<PieChart.Data> productData = new ArrayList<PieChart.Data>();
			bookLabel.setText(book.getName() + " book content");
			if (bookContent != null & !bookContent.isEmpty()) {
				Map<String, BigDecimal> currencies = bookContent.get("Currency");
				if (currencies != null && !currencies.isEmpty()) {
					for (Map.Entry<String, BigDecimal> entry : currencies.entrySet()) {
						cashData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
					}
				}
				Map<String, BigDecimal> products = bookContent.get("Product");
				if (products != null && !products.isEmpty()) {
					for (Map.Entry<String, BigDecimal> entry : products.entrySet()) {
						productData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
					}
				}
			}
			if (!cashData.isEmpty()) {
				cashBookChart.setData(FXCollections.observableArrayList(cashData));
			} else {
				cashBookChart.setData(FXCollections.emptyObservableList());
			}
			if (!productData.isEmpty()) {
				productBookChart.setData(FXCollections.observableArrayList(productData));
			} else {
				productBookChart.setData(FXCollections.emptyObservableList());
			}
			cashData.forEach(d -> {
				Tooltip tip = new Tooltip();
				tip.setText(TradistaGUIUtil.formatAmount(d.getPieValue()) + StringUtils.SPACE + d.getName());
				Tooltip.install(d.getNode(), tip);
			});

			productData.forEach(d -> {
				Tooltip tip = new Tooltip();
				tip.setText(TradistaGUIUtil.formatAmount(d.getPieValue()) + StringUtils.SPACE + d.getName());
				Tooltip.install(d.getNode(), tip);
			});
			updateView(cashRadioButton.isSelected());
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	public void clear() {
		cashRadioButton.setVisible(false);
		productRadioButton.setVisible(false);
		cashBookIsEmpty.setVisible(false);
		productBookIsEmpty.setVisible(false);
		bookLabel.setText(null);
		cashRadioButton.setSelected(true);
		cashBookChart.setData(FXCollections.emptyObservableList());
		productBookChart.setData(FXCollections.emptyObservableList());
	}

}