package org.eclipse.tradista.core.marketdata.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.marketdata.model.GenerableCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

/********************************************************************************
 * Copyright (c) 2020 Olivier Asuncion
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

public abstract class TradistaGenerableCurveController extends TradistaControllerAdapter {

	@FXML
	protected Label marketDataMessage;

	@FXML
	protected ComboBox<QuoteSet> quoteSet;

	@FXML
	protected ComboBox<GenerableCurve> curveComboBox;

	@FXML
	protected CheckBox isGeneratedCheckBox;

	@FXML
	protected ComboBox<String> algorithmComboBox, interpolatorComboBox, instanceComboBox;

	@FXML
	protected DatePicker quoteDate;

	@FXML
	protected Button saveButton, copyButton, generateButton, deleteButton, createButton;

	protected GenerableCurve curve;

	protected boolean canGetCurve = true;

	protected boolean canDeleteCurve = true;

	protected boolean canSaveCurve = true;

	protected boolean canCopyCurve = true;

	protected boolean canGenerateCurve = true;

	protected boolean canCreateCurve = true;

	protected boolean canGetQuoteSet = true;

	protected boolean canGetQuote = true;

	protected boolean quoteSetExists = false;

	protected boolean curveExists = false;

	protected boolean canGetGenerationAlgorithms = true;

	protected boolean canGetInterpolators = true;

	protected QuoteBusinessDelegate quoteBusinessDelegate;

	protected Map<String, List<String>> errors;

	protected void initialize() {

		errors = new HashMap<String, List<String>>();

		quoteBusinessDelegate = new QuoteBusinessDelegate();

		try {
			Set<QuoteSet> qs = quoteBusinessDelegate.getAllQuoteSets();
			TradistaGUIUtil.fillComboBox(qs, quoteSet);
			quoteSetExists = (qs != null && !qs.isEmpty());
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
		}

		curveComboBox.valueProperty().addListener(new ChangeListener<GenerableCurve>() {
			@Override
			public void changed(ObservableValue<? extends GenerableCurve> ov, GenerableCurve oldC, GenerableCurve c) {
				if (c != null) {
					curve = c;
					isGeneratedCheckBox.setSelected(c.isGenerated());
					interpolatorComboBox.setValue(c.getInterpolator());
					algorithmComboBox.setValue(c.getAlgorithm());
					instanceComboBox.setValue(c.getInstance());

					quoteSet.setValue(c.getQuoteSet());
					quoteDate.setValue(c.getQuoteDate());
				} else {

					algorithmComboBox.getSelectionModel().clearSelection();
					interpolatorComboBox.getSelectionModel().clearSelection();
					instanceComboBox.getSelectionModel().clearSelection();
					quoteDate.setValue(null);
				}
			}
		});

	}

	public void refresh() {
		try {
			Set<QuoteSet> qs = quoteBusinessDelegate.getAllQuoteSets();
			TradistaGUIUtil.fillComboBox(qs, quoteSet);
			quoteSetExists = (qs != null && !qs.isEmpty());
			canGetQuoteSet = true;
		} catch (TradistaTechnicalException tte) {
			canGetQuoteSet = false;
		}
		if (!canGetQuote) {
			try {
				quoteBusinessDelegate.getQuotesByName("Ping");
				canGetQuote = true;
			} catch (TradistaTechnicalException tte) {
			}
		}
	}

	protected void updateWindow() {
		errors.clear();
		String errMsg;
		boolean isError = false;

		if (!quoteSetExists) {
			TradistaGUIUtil.unapplyErrorStyle(marketDataMessage);
			TradistaGUIUtil.applyWarningStyle(marketDataMessage);
			marketDataMessage.setText("There is no quote set, please create one.");
		}

		buildErrorMap();
		isError = !errors.isEmpty();
		errMsg = buildErrorMessage();

		if (isError) {
			TradistaGUIUtil.unapplyWarningStyle(marketDataMessage);
			TradistaGUIUtil.applyErrorStyle(marketDataMessage);
			marketDataMessage.setText(errMsg);
		}

		marketDataMessage.setVisible(isError || !quoteSetExists);

		updateComponents();
	}

	public void buildErrorMap() {
		if (!canGetQuoteSet) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("quote sets");
			errors.put("get", err);
		}
		if (!canGetQuote) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("quotes");
			errors.put("get", err);
		}
		if (!canGetGenerationAlgorithms) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("generation algorithms");
			errors.put("get", err);
		}
		if (!canGetInterpolators) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("interpolators");
			errors.put("get", err);
		}
		if (!canGetCurve) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("get", err);
		}
		if (!canSaveCurve) {
			List<String> err = errors.get("save");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("save", err);
		}
		if (!canCopyCurve) {
			List<String> err = errors.get("copy");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("copy", err);
		}
		if (!canGenerateCurve) {
			List<String> err = errors.get("generate");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("generate", err);
		}
		if (!canDeleteCurve) {
			List<String> err = errors.get("delete");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("delete", err);
		}
		if (!canCreateCurve) {
			List<String> err = errors.get("create");
			if (err == null) {
				err = new ArrayList<String>();
			}
			err.add("curves");
			errors.put("create", err);
		}
	}

	public String buildErrorMessage() {
		StringBuilder errMsg = new StringBuilder();
		for (Map.Entry<String, List<String>> errCat : errors.entrySet()) {
			errMsg.append("Cannot ").append(errCat.getKey());
			if (errCat.getValue().size() > 1) {
				errMsg.append(":");
			}
			errMsg.append(" ");
			for (String err : errCat.getValue()) {
				errMsg.append(err + ", ");
			}
			errMsg.delete(errMsg.length() - 2, errMsg.length());
			errMsg.append(".");
			errMsg.append(System.lineSeparator());
		}
		errMsg.append("Please contact support.");
		return errMsg.toString();
	}

	public void updateComponents() {
		quoteSet.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators);
		curveComboBox.setDisable(!canGetCurve);
		algorithmComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators);
		interpolatorComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		saveButton.setDisable(!canGetCurve || !canSaveCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		copyButton.setDisable(!canGetCurve || !canCopyCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetGenerationAlgorithms || !canGetInterpolators);
		deleteButton.setDisable(!canDeleteCurve || !canGetCurve);
		generateButton.setDisable(!canGetQuote || !canGenerateCurve || !quoteSetExists || !canGetQuoteSet
				|| !canGetCurve || !canGetGenerationAlgorithms || !canGetInterpolators);
		createButton.setDisable(!canCreateCurve || !canGetCurve);
		instanceComboBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators);
		isGeneratedCheckBox.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators);
		quoteDate.setDisable(!quoteSetExists || !canGetQuoteSet || !canGetCurve || !canGetGenerationAlgorithms
				|| !canGetInterpolators);

	}

}