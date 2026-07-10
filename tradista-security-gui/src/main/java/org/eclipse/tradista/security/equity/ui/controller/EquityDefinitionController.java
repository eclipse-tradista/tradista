package org.eclipse.tradista.security.equity.ui.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaController;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaChoiceDialog;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.model.RatingAssignment;
import org.eclipse.tradista.core.rating.service.RatingBusinessDelegate;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equity.ui.view.EquityCreatorDialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class EquityDefinitionController implements TradistaController {

	@FXML
	private TextField tradingSize;

	@FXML
	private DatePicker activeFrom;

	@FXML
	private DatePicker activeTo;

	@FXML
	private TextField totalIssued;

	@FXML
	private ComboBox<LegalEntity> issuer;

	@FXML
	private TextField isin;

	@FXML
	private ComboBox<Currency> currency;

	@FXML
	private CheckBox payDividend;

	@FXML
	private ComboBox<Tenor> dividendFrequency;

	@FXML
	private ComboBox<Currency> dividendCurrency;

	@FXML
	private Label dividendFrequencyLabel;

	@FXML
	private Label dividendCurrencyLabel;

	@FXML
	private DatePicker issueDate;

	@FXML
	private TextField issuePrice;

	@FXML
	private ComboBox<Exchange> exchange;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private EquityBusinessDelegate equityBusinessDelegate;

	@FXML
	private ComboBox<String> loadingCriterion;

	@FXML
	private TextField load;

	private Equity equity;

	@FXML
	private Label productId;

	@FXML
	private Label productType;

	@FXML
	private Label isinLabel;

	@FXML
	private Label exchangeLabel;

	@FXML
	private TableView<RatingAssignment> ratingsTable;

	@FXML
	private TableColumn<RatingAssignment, String> agencyCol;

	@FXML
	private TableColumn<RatingAssignment, String> ratingCol;

	@FXML
	private TableColumn<RatingAssignment, String> fromCol;

	@FXML
	private TableColumn<RatingAssignment, String> toCol;

	@FXML
	private ComboBox<RatingAgency> agencyComboBox;

	@FXML
	private ComboBox<Rating> ratingComboBox;

	@FXML
	private DatePicker validFromPicker;

	@FXML
	private DatePicker validToPicker;

	@FXML
	private Button addRatingButton;

	@FXML
	private Button removeRatingButton;

	private RatingBusinessDelegate ratingBusinessDelegate;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		productType.setText("Equity");

		loadingCriterion.getItems().add("id");
		loadingCriterion.getItems().add("ISIN");
		loadingCriterion.getSelectionModel().selectFirst();
		equityBusinessDelegate = new EquityBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		ratingBusinessDelegate = new RatingBusinessDelegate();

		addRatingButton.setDisable(true);
		removeRatingButton.setDisable(true);

		agencyCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getRating().getAgency().getName()));
		ratingCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getRating().getCode()));
		fromCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValidFrom().toString()));
		toCol.setCellValueFactory(p -> new SimpleStringProperty(
				p.getValue().getValidTo() != null ? p.getValue().getValidTo().toString() : StringUtils.EMPTY));

		Set<RatingAgency> agencies = ratingBusinessDelegate.getAllRatingAgencies();
		if (agencies != null) {
			List<RatingAgency> activeAgencies = new ArrayList<>();
			for (RatingAgency ag : agencies) {
				if (ag.isActive()) {
					activeAgencies.add(ag);
				}
			}
			Collections.sort(activeAgencies);
			agencyComboBox.setItems(FXCollections.observableArrayList(activeAgencies));
		}

		agencyComboBox.valueProperty().addListener((_, _, newVal) -> {
			if (newVal != null) {
				try {
					Set<Rating> ratings = ratingBusinessDelegate.getRatingsByAgencyId(newVal.getId());
					ratingComboBox.setItems(FXCollections.observableArrayList(ratings));
				} catch (TradistaBusinessException _) {
					ratingComboBox.getItems().clear();
				}
			} else {
				ratingComboBox.getItems().clear();
			}
		});

		payDividend.selectedProperty().addListener((_, _, newVal) -> {
			dividendCurrency.setVisible(newVal);
			dividendFrequency.setVisible(newVal);
			dividendCurrencyLabel.setVisible(newVal);
			dividendFrequencyLabel.setVisible(newVal);
		});

		TradistaGUIUtil.fillTenorComboBox(dividendFrequency);
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), issuer);
		TradistaGUIUtil.fillCurrencyComboBox(currency, dividendCurrency);
		TradistaGUIUtil.fillExchangeComboBox(exchange);

	}

	private void buildProduct(Equity equity) {
		try {
			equity.setActiveFrom(activeFrom.getValue());
			equity.setActiveTo(activeTo.getValue());
			equity.setCurrency(currency.getValue());
			if (payDividend.isSelected()) {
				equity.setDividendCurrency(dividendCurrency.getValue());
				equity.setDividendFrequency(dividendFrequency.getValue());
			}
			equity.setIssueDate(issueDate.getValue());
			if (!issuePrice.getText().isEmpty()) {

				equity.setIssuePrice(TradistaGUIUtil.parseAmount(issuePrice.getText(), "Issue Price"));
			}
			equity.setIssuer(issuer.getValue());
			equity.setPayDividend(payDividend.isSelected());
			if (!totalIssued.getText().isEmpty()) {
				equity.setTotalIssued(Long.parseLong(totalIssued.getText()));
			}
			if (!tradingSize.getText().isEmpty()) {
				equity.setTradingSize(Long.parseLong(tradingSize.getText()));
			}
		} catch (TradistaBusinessException _) {
			// Should not appear here.
		}
	}

	@FXML
	protected void save() {
		TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
		confirmation.setTitle("Save Equity");
		confirmation.setHeaderText("Save Equity");
		confirmation.setContentText("Do you want to save this Equity?");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				checkAmounts();

				if (isin.isVisible()) {
					equity = new Equity(exchange.getValue(), isin.getText());
					equity.setCreationDate(LocalDate.now(ZoneId.systemDefault()));
				}

				buildProduct(equity);

				equity.setId(equityBusinessDelegate.saveEquity(equity));
				productId.setText(String.valueOf(equity.getId()));
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
		EquityCreatorDialog dialog = new EquityCreatorDialog(exchange.getValue());
		Optional<Equity> result = dialog.showAndWait();

		if (result.isPresent()) {
			try {
				Equity copyEquity = null;
				checkAmounts();
				copyEquity = new Equity(result.get().getExchange(), result.get().getIsin());
				buildProduct(copyEquity);
				copyEquity.setId(equityBusinessDelegate.saveEquity(copyEquity));
				equity = copyEquity;
				productId.setText(String.valueOf(equity.getId()));
				isin.setText(equity.getIsin());
				exchange.setValue(equity.getExchange());
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
		Set<Equity> equities = null;
		long equityId = 0;
		String equityIsin = null;
		try {
			try {
				if (!load.getText().isEmpty()) {
					if (loadingCriterion.getValue().equals("id")) {
						equityId = Long.parseLong(load.getText());
					} else {
						equityIsin = load.getText();
					}
				} else {
					throw new TradistaBusinessException("Please specify a product id or ISIN.");
				}
			} catch (NumberFormatException _) {
				throw new TradistaBusinessException(String.format("The product id is incorrect: %s", load.getText()));
			}

			if (loadingCriterion.getValue().equals("id")) {
				equities = HashSet.newHashSet(1);
				Equity equity = equityBusinessDelegate.getEquityById(equityId);
				if (equity != null) {
					equities.add(equity);
				}
			} else {
				equities = equityBusinessDelegate.getEquitiesByIsin(equityIsin);
			}
			if (equities == null || equities.isEmpty()) {
				throw new TradistaBusinessException(
						String.format("The equity %s doesn't exist in the system.", load.getText()));
			}

			if (equities.size() > 1) {
				TradistaChoiceDialog<Equity> dialog = new TradistaChoiceDialog<Equity>((Equity) equities.toArray()[0],
						equities);
				dialog.setTitle("Equity Selection");
				dialog.setHeaderText("Please choose an Equity");
				dialog.setContentText("Selected Equity:");

				Optional<Equity> result = dialog.showAndWait();
				result.ifPresent(this::load);
			} else {
				load((Equity) equities.toArray()[0]);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	private void load(Equity equity) {
		this.equity = equity;
		productId.setText(Long.toString(equity.getId()));
		exchange.setValue(equity.getExchange());
		activeFrom.setValue(equity.getActiveFrom());
		activeTo.setValue(equity.getActiveTo());
		currency.setValue(equity.getCurrency());
		dividendCurrency.setValue(equity.getDividendCurrency());
		dividendFrequency.setValue(equity.getDividendFrequency());
		isin.setText(equity.getIsin());
		issueDate.setValue(equity.getIssueDate());
		issuePrice.setText(TradistaGUIUtil.formatAmount(equity.getIssuePrice()));
		issuer.setValue(equity.getIssuer());
		payDividend.setSelected(equity.isPayDividend());
		totalIssued.setText(Long.toString(equity.getTotalIssued()));
		tradingSize.setText(Long.toString(equity.getTradingSize()));
		isinLabel.setText(equity.getIsin());
		exchangeLabel.setText(equity.getExchange().toString());
		isin.setVisible(false);
		exchange.setVisible(false);
		isinLabel.setVisible(true);
		exchangeLabel.setVisible(true);

		loadRatings();

		addRatingButton.setDisable(false);
		removeRatingButton.setDisable(false);
	}

	@Override
	@FXML
	public void clear() {
		equity = null;
		productId.setText(StringUtils.EMPTY);
		tradingSize.clear();
		totalIssued.clear();
		issuePrice.clear();
		isin.clear();
		activeFrom.setValue(null);
		activeTo.setValue(null);
		isinLabel.setText(StringUtils.EMPTY);
		isin.setVisible(true);
		isinLabel.setVisible(false);
		exchangeLabel.setText(StringUtils.EMPTY);
		exchange.setVisible(true);
		exchangeLabel.setVisible(false);

		if (ratingsTable.getItems() != null) {
			ratingsTable.getItems().clear();
		}
		agencyComboBox.setValue(null);
		ratingComboBox.setValue(null);
		validFromPicker.setValue(null);
		validToPicker.setValue(null);
		addRatingButton.setDisable(true);
		removeRatingButton.setDisable(true);
	}

	@FXML
	protected void addRating() {
		if (equity == null || equity.getId() == 0) {
			new TradistaAlert(AlertType.ERROR, "Please save or load an equity first.").showAndWait();
			return;
		}
		if (ratingComboBox.getValue() == null || validFromPicker.getValue() == null) {
			new TradistaAlert(AlertType.ERROR, "Rating and Valid From are mandatory").showAndWait();
			return;
		}
		RatingAssignment assignment = new RatingAssignment(equity, ratingComboBox.getValue(),
				validFromPicker.getValue());
		assignment.setValidTo(validToPicker.getValue());

		try {
			long id = ratingBusinessDelegate.saveRatingAssignment(assignment);
			assignment.setId(id);
			ratingsTable.getItems().add(assignment);
		} catch (TradistaBusinessException ex) {
			new TradistaAlert(AlertType.ERROR, ex.getMessage()).showAndWait();
		}
	}

	@FXML
	protected void removeRating() {
		RatingAssignment selected = ratingsTable.getSelectionModel().getSelectedItem();
		if (selected != null) {
			try {
				ratingBusinessDelegate.deleteRatingAssignment(selected.getId());
				ratingsTable.getItems().remove(selected);
			} catch (TradistaBusinessException ex) {
				new TradistaAlert(AlertType.ERROR, ex.getMessage()).showAndWait();
			}
		}
	}

	private void loadRatings() {
		if (ratingsTable.getItems() != null) {
			ratingsTable.getItems().clear();
		}
		if (equity != null && equity.getId() != 0) {
			try {
				Set<RatingAssignment> assignments = ratingBusinessDelegate
						.getRatingAssignmentsByRatableId(equity.getId(), Equity.EQUITY);
				if (assignments != null) {
					ratingsTable.setItems(FXCollections.observableArrayList(assignments));
				}
			} catch (TradistaBusinessException e) {
				new TradistaAlert(AlertType.ERROR, e.getMessage()).showAndWait();
			}
		}
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillComboBox(legalEntityBusinessDelegate.getAllLegalEntities(), issuer);
		TradistaGUIUtil.fillCurrencyComboBox(currency, dividendCurrency);
		TradistaGUIUtil.fillExchangeComboBox(exchange);
	}

	@Override
	public void checkAmounts() throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		try {
			if (!tradingSize.getText().isEmpty()) {
				Long.parseLong(tradingSize.getText());
			}
		} catch (NumberFormatException _) {
			errMsg.append(String.format("The trading size is incorrect: %s.%n", tradingSize.getText()));
		}
		try {
			if (!totalIssued.getText().isEmpty()) {
				Long.parseLong(totalIssued.getText());
			}
		} catch (NumberFormatException _) {
			errMsg.append(String.format("The total issued is incorrect: %s.%n", totalIssued.getText()));
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