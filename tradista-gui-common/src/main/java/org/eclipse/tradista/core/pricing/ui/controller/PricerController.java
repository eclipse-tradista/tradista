package org.eclipse.tradista.core.pricing.ui.controller;

import java.io.IOException;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

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

public class PricerController extends TradistaControllerAdapter implements TradistaController {

	@FXML
	private ComboBox<LegalEntity> counterparty;

	@FXML
	private ComboBox<PricingParameter> pricingParameter;

	@FXML
	private ComboBox<PricerMeasure> pricingMeasure;

	@FXML
	private ComboBox<String> pricingMethod;

	@FXML
	private ComboBox<Currency> pricingCurrency;

	@FXML
	private DatePicker pricingDate;

	@FXML
	private Label result;

	public PricerController() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Pricer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void initialize() {
		TradistaGUIUtil.fillComboBox(new PricerBusinessDelegate().getAllPricingParameters(), pricingParameter);
		try {
			Pricer pricer = new PricerBusinessDelegate().getPricer("FX", pricingParameter.getValue());
			TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
		} catch (TradistaBusinessException tbe) {
			// Will never happen in this case.
		}

		pricingMeasure.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PricerMeasure>() {
			@Override
			public void changed(ObservableValue<? extends PricerMeasure> arg0, PricerMeasure arg1, PricerMeasure arg2) {
				TradistaGUIUtil.fillComboBox(new PricerBusinessDelegate().getAllPricingMethods(arg2), pricingMethod);
			}
		});

	}

	public void initPricerMeasures(String productType) {
		try {
			Pricer pricer = new PricerBusinessDelegate().getPricer(productType, pricingParameter.getValue());
			TradistaGUIUtil.fillComboBox(pricer.getPricerMeasures(), pricingMeasure);
		} catch (TradistaBusinessException tbe) {
			// Will never happen in this case.
		}
	}

	@FXML
	protected void price() {
	}

}