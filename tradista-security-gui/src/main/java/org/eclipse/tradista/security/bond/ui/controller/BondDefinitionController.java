package org.eclipse.tradista.security.bond.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.service.BondBusinessDelegate;
import org.eclipse.tradista.security.bond.ui.view.BondCreatorDialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class BondDefinitionController implements TradistaController {

	@FXML
	private TextField coupon;

	@FXML
	private DatePicker maturity;

	@FXML
	private TextField principal;

	@FXML
	private ComboBox<LegalEntity> issuer;

	@FXML
	private TextField isin;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private DatePicker datedDate;

	@FXML
	private ComboBox<String> couponType;

	@FXML
	private ComboBox<Tenor> couponFrequency;

	@FXML
	private TextField redemptionPrice;

	@FXML
	private ComboBox<Currency> redemptionCurrency;

	@FXML
	private DatePicker issueDate;

	@FXML
	private TextField issuePrice;

	@FXML
	private ComboBox<Exchange> exchange;

	@FXML
	private ComboBox<String> loadingCriterion;

	@FXML
	private TextField load;

	private Bond bond;;

	@FXML
	private Label bondId;

	@FXML
	private Label productType;

	@FXML
	private ComboBox<Index> referenceRateIndex;

	@FXML
	private TextField cap;

	@FXML
	private TextField floor;

	@FXML
	private Label capTitle;

	@FXML
	private Label floorTitle;

	@FXML
	private TextField spread;

	@FXML
	private TextField leverageFactor;

	@FXML
	private Label spreadTitle;

	@FXML
	private Label leverageFactorTitle;

	@FXML
	private ComboBox<Bond.CapFloorCollar> capFloorCollar;

	@FXML
	private Label capFloorCollarTitle;

	@FXML
	private Label referenceRateIndexTitle;

	@FXML
	private Label couponTitle;

	@FXML
	private Label isinLabel;

	@FXML
	private Label exchangeLabel;

	private BondBusinessDelegate bondBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		bondBusinessDelegate = new BondBusinessDelegate();

		productType.setText("Bond");

		loadingCriterion.getItems().add("id");
		loadingCriterion.getItems().add("ISIN");
		loadingCriterion.getSelectionModel().selectFirst();

		couponType.setItems(FXCollections.observableArrayList("Fixed", "Float"));
		couponType.getSelectionModel().selectFirst();

		TradistaGUIUtil.fillTenorComboBox(couponFrequency);
		TradistaGUIUtil.fillComboBox(new LegalEntityBusinessDelegate().getAllLegalEntities(), issuer);
		TradistaGUIUtil.fillCurrencyComboBox(currency, redemptionCurrency);
		TradistaGUIUtil.fillExchangeComboBox(exchange);
		TradistaGUIUtil.fillIndexComboBox(referenceRateIndex);

		capFloorCollar.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Bond.CapFloorCollar> observableValue,
					Bond.CapFloorCollar oldValue, Bond.CapFloorCollar newValue) {
				if (newValue != null) {
					switch (newValue) {
					case CAP: {
						capTitle.setVisible(true);
						cap.setVisible(true);
						floorTitle.setVisible(false);
						floor.setVisible(false);
						break;
					}
					case FLOOR: {
						capTitle.setVisible(false);
						cap.setVisible(false);
						floorTitle.setVisible(true);
						floor.setVisible(true);
						break;
					}
					case COLLAR: {
						capTitle.setVisible(true);
						cap.setVisible(true);
						floorTitle.setVisible(true);
						floor.setVisible(true);
						break;
					}
					case NONE:
						capTitle.setVisible(false);
						cap.setVisible(false);
						floorTitle.setVisible(false);
						floor.setVisible(false);
						break;
					default:
						break;
					}
				}
			}
		});

		couponType.valueProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				boolean isFloat = (newValue != null && newValue.equals("Float"));
				capTitle.setVisible(isFloat);
				cap.setVisible(isFloat);
				floorTitle.setVisible(isFloat);
				floor.setVisible(isFloat);
				capFloorCollarTitle.setVisible(isFloat);
				capFloorCollar.setVisible(isFloat);
				spreadTitle.setVisible(isFloat);
				spread.setVisible(isFloat);
				leverageFactorTitle.setVisible(isFloat);
				leverageFactor.setVisible(isFloat);
				referenceRateIndexTitle.setVisible(isFloat);
				referenceRateIndex.setVisible(isFloat);
				coupon.setVisible(!isFloat);
				couponTitle.setVisible(!isFloat);
				capFloorCollar.getSelectionModel().clearSelection();
				capFloorCollar.getSelectionModel().selectFirst();
			}
		});

		capFloorCollar.setItems(FXCollections.observableArrayList(Bond.CapFloorCollar.values()));

	}

	private void buildProduct(Bond bond) {
		try {
			if (couponType.getValue().equals("Fixed")) {
				if (!coupon.getText().isEmpty()) {
					bond.setCoupon(TradistaGUIUtil.parseAmount(coupon.getText(), "Coupon"));
				}
			} else {
				bond.setReferenceRateIndex(referenceRateIndex.getValue());
				if (capFloorCollar.getValue().equals(Bond.CapFloorCollar.CAP)
						|| capFloorCollar.getValue().equals(Bond.CapFloorCollar.COLLAR)) {
					if (!cap.getText().isEmpty()) {
						bond.setCap(TradistaGUIUtil.parseAmount(cap.getText(), "Cap"));
					}
				}
				if (capFloorCollar.getValue().equals(Bond.CapFloorCollar.FLOOR)
						|| capFloorCollar.getValue().equals(Bond.CapFloorCollar.COLLAR)) {
					if (!floor.getText().isEmpty()) {
						bond.setFloor(TradistaGUIUtil.parseAmount(floor.getText(), "Floor"));
					}
				}
				if (!coupon.getText().isEmpty()) {
					bond.setCoupon(TradistaGUIUtil.parseAmount(coupon.getText(), "Coupon"));
				}
				if (!spread.getText().isEmpty()) {
					bond.setSpread(TradistaGUIUtil.parseAmount(spread.getText(), "Spread"));
				}
				if (!leverageFactor.getText().isEmpty()) {
					bond.setLeverageFactor(TradistaGUIUtil.parseAmount(leverageFactor.getText(), "Leverage Factor"));
				}
			}
			bond.setCouponFrequency(couponFrequency.getValue());
			bond.setCouponType(couponType.getValue());
			bond.setCurrency(currency.getValue());
			bond.setDatedDate(datedDate.getValue());
			bond.setIssueDate(issueDate.getValue());
			if (!issuePrice.getText().isEmpty()) {
				bond.setIssuePrice(TradistaGUIUtil.parseAmount(issuePrice.getText(), "Issue Price"));
			}
			bond.setIssuer(issuer.getValue());
			bond.setMaturityDate(maturity.getValue());
			if (!principal.getText().isEmpty()) {
				bond.setPrincipal(TradistaGUIUtil.parseAmount(principal.getText(), "Principal"));
			}
			if (!redemptionPrice.getText().isEmpty()) {
				bond.setRedemptionPrice(TradistaGUIUtil.parseAmount(redemptionPrice.getText(), "Redemption Price"));
				bond.setRedemptionCurrency(redemptionCurrency.getValue());
			}
		} catch (TradistaBusinessException _) {
			// Should not appear here.
		}
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Bond");
		confirmation.setHeaderText("Save Bond");
		confirmation.setContentText("Do you want to save this Bond?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				if (isin.isVisible()) {
					bond = new Bond(exchange.getValue(), isin.getText());
					bond.setCreationDate(LocalDate.now());
				}

				buildProduct(bond);

				bond.setId(bondBusinessDelegate.saveBond(bond));
				bondId.setText(String.valueOf(bond.getId()));
				isinLabel.setText(isin.getText());
				exchangeLabel.setText(exchange.getValue().toString());
				isin.setVisible(false);
				exchange.setVisible(false);
				isinLabel.setVisible(true);
				exchangeLabel.setVisible(true);

			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void copy() {
		BondCreatorDialog dialog = new BondCreatorDialog(exchange.getValue());
		Optional<Bond> result = dialog.showAndWait();
		if (result.isPresent()) {
			try {
				Bond copyBond;
				checkAmounts();
				copyBond = new Bond(result.get().getExchange(), result.get().getIsin());
				buildProduct(copyBond);
				copyBond.setId(bondBusinessDelegate.saveBond(copyBond));
				bond = copyBond;
				bondId.setText(String.valueOf(bond.getId()));
				exchange.setValue(bond.getExchange());
				isin.setText(bond.getIsin());
				isinLabel.setText(isin.getText());
				exchangeLabel.setText(exchange.getValue().toString());
				isin.setVisible(false);
				exchange.setVisible(false);
				isinLabel.setVisible(true);
				exchangeLabel.setVisible(true);
			} catch (TradistaBusinessException tbe) {
				TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
				alert.showAndWait();
			}
		}
	}

	@FXML
	protected void load() {
		Set<Bond> bonds = null;
		long bondId = 0;
		String bondIsin = null;
		try {
			try {
				if (!load.getText().isEmpty()) {
					if (loadingCriterion.getValue().equals("id")) {
						bondId = Long.parseLong(load.getText());
					} else {
						bondIsin = load.getText();
					}
				} else {
					throw new TradistaBusinessException("Please specify a product id or ISIN.");
				}
			} catch (NumberFormatException _) {
				throw new TradistaBusinessException(String.format("The product id is incorrect: %s", load.getText()));
			}

			if (loadingCriterion.getValue().equals("id")) {
				bonds = HashSet.newHashSet(1);
				Bond bond = bondBusinessDelegate.getBondById(bondId);
				if (bond != null) {
					bonds.add(bond);
				}
			} else {
				bonds = bondBusinessDelegate.getBondsByIsin(bondIsin);
			}
			if (bonds == null || bonds.isEmpty()) {
				throw new TradistaBusinessException(
						String.format("The bond %s doesn't exist in the system.", load.getText()));
			}

			if (bonds.size() > 1) {
				ChoiceDialog<Bond> dialog = new ChoiceDialog<>((Bond) bonds.toArray()[0], bonds);
				dialog.setTitle("Bond Selection");
				dialog.setHeaderText("Please choose a Bond");
				dialog.setContentText("Selected Bond:");

				Optional<Bond> result = dialog.showAndWait();
				result.ifPresent(this::load);
			} else {
				load((Bond) bonds.toArray()[0]);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Bond bond) {
		this.bond = bond;
		bondId.setText(Long.toString(bond.getId()));
		if (bond.getCoupon() != null) {
			coupon.setText(TradistaGUIUtil.formatAmount(bond.getCoupon()));
		}
		couponFrequency.setValue(bond.getCouponFrequency());
		couponType.setValue(bond.getCouponType());
		currency.setValue(bond.getCurrency());
		datedDate.setValue(bond.getDatedDate());
		exchange.setValue(bond.getExchange());
		isin.setText(bond.getIsin());
		issueDate.setValue(bond.getIssueDate());
		issuePrice.setText(TradistaGUIUtil.formatAmount(bond.getIssuePrice()));
		issuer.setValue(bond.getIssuer());
		maturity.setValue(bond.getMaturityDate());
		principal.setText(TradistaGUIUtil.formatAmount(bond.getPrincipal()));
		redemptionCurrency.setValue(bond.getRedemptionCurrency());
		referenceRateIndex.setValue(bond.getReferenceRateIndex());
		if (bond.getSpread() != null) {
			spread.setText(TradistaGUIUtil.formatAmount(bond.getSpread()));
		}
		if (bond.getLeverageFactor() != null) {
			leverageFactor.setText(TradistaGUIUtil.formatAmount(bond.getLeverageFactor()));
		}
		if (bond.isCap()) {
			cap.setText(TradistaGUIUtil.formatAmount(bond.getCap()));
			capFloorCollar.setValue(Bond.CapFloorCollar.CAP);
		}
		if (bond.isFloor()) {
			floor.setText(TradistaGUIUtil.formatAmount(bond.getFloor()));
			capFloorCollar.setValue(Bond.CapFloorCollar.FLOOR);
		}
		if (bond.isCollar()) {
			cap.setText(TradistaGUIUtil.formatAmount(bond.getCap()));
			floor.setText(TradistaGUIUtil.formatAmount(bond.getFloor()));
			capFloorCollar.setValue(Bond.CapFloorCollar.COLLAR);
		}
		if (!bond.isCollar() && !bond.isCap() && !bond.isFloor()) {
			capFloorCollar.setValue(Bond.CapFloorCollar.NONE);
		}
		BigDecimal redemptionPrice = bond.getRedemptionPrice();
		if (redemptionPrice != null) {
			this.redemptionPrice.setText(TradistaGUIUtil.formatAmount(redemptionPrice));
		}
		isinLabel.setText(bond.getIsin());
		exchangeLabel.setText(bond.getExchange().toString());
		isin.setVisible(false);
		exchange.setVisible(false);
		isinLabel.setVisible(true);
		exchangeLabel.setVisible(true);
	}

	@Override
	@FXML
	public void clear() {
		bond = null;
		bondId.setText(StringUtils.EMPTY);
		datedDate.setValue(null);
		redemptionPrice.clear();
		isin.clear();
		principal.clear();
		coupon.clear();
		maturity.setValue(null);
		issueDate.setValue(null);
		issuePrice.clear();
		isinLabel.setText(StringUtils.EMPTY);
		isin.setVisible(true);
		isinLabel.setVisible(false);
		exchangeLabel.setText(StringUtils.EMPTY);
		exchange.setVisible(true);
		exchangeLabel.setVisible(false);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillComboBox(new LegalEntityBusinessDelegate().getAllLegalEntities(), issuer);
		TradistaGUIUtil.fillCurrencyComboBox(currency, redemptionCurrency);
		TradistaGUIUtil.fillExchangeComboBox(exchange);
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			TradistaGUIUtil.checkAmount(redemptionPrice.getText(), "Redemption Price");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(principal.getText(), "Principal");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(coupon.getText(), "Coupon");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		try {
			TradistaGUIUtil.checkAmount(issuePrice.getText(), "Issue Price");
		} catch (TradistaBusinessException abe) {
			errMsg.append(abe.getMessage());
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}