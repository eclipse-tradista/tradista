package org.eclipse.tradista.core.marketdata.ui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.common.ui.view.TradistaCopyDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaSaveConfirmationDialog;
import org.eclipse.tradista.core.common.ui.view.TradistaTextInputDialog;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.FeedType;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.FeedBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.ui.view.FeedConfigCreatorDialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class FeedConfigController extends TradistaControllerAdapter {

	private static final String FEED_CONFIGURATIONS = "feed configurations";

	@FXML
	private TableView<FeedMappingProperty> feedMappingTable;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingQuoteName;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingQuoteType;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingFieldName;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingBid;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingAsk;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingOpen;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingClose;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingHigh;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingLow;

	@FXML
	private TableColumn<FeedMappingProperty, String> mappingLast;

	@FXML
	private ComboBox<FeedConfig> feedConfig;

	@FXML
	private ComboBox<FeedType> feedType;

	@FXML
	private TextField fieldNameTextField;

	@FXML
	private TextField quoteNameTextField;

	@FXML
	private ComboBox<QuoteType> quoteType;

	@FXML
	private TextField bidTextField;

	@FXML
	private TextField askTextField;

	@FXML
	private TextField openTextField;

	@FXML
	private TextField closeTextField;

	@FXML
	private TextField highTextField;

	@FXML
	private TextField lowTextField;

	@FXML
	private TextField lastTextField;

	@FXML
	private Label feedConfigName;

	@FXML
	protected Label marketDataMessage;

	@FXML
	protected Button loadButton, saveButton, copyButton, deleteButton, addButton;

	private FeedBusinessDelegate feedBusinessDelegate;

	private FeedConfig currentFeedConfig;

	private boolean canGetFeedConfig = true;

	private boolean canSaveFeedConfig = true;

	private boolean canCopyFeedConfig = true;

	private boolean canDeleteFeedConfig = true;

	private boolean canCreateFeedConfig = true;

	protected Map<String, List<String>> errors;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		errors = new HashMap<>();

		feedBusinessDelegate = new FeedBusinessDelegate();

		mappingQuoteName.setCellValueFactory(cellData -> cellData.getValue().getQuoteName());
		mappingQuoteType.setCellValueFactory(cellData -> cellData.getValue().getQuoteType());
		mappingFieldName.setCellValueFactory(cellData -> cellData.getValue().getFieldName());
		mappingBid.setCellValueFactory(cellData -> cellData.getValue().getBid());
		mappingAsk.setCellValueFactory(cellData -> cellData.getValue().getAsk());
		mappingOpen.setCellValueFactory(cellData -> cellData.getValue().getOpen());
		mappingClose.setCellValueFactory(cellData -> cellData.getValue().getClose());
		mappingHigh.setCellValueFactory(cellData -> cellData.getValue().getHigh());
		mappingLow.setCellValueFactory(cellData -> cellData.getValue().getLow());
		mappingLast.setCellValueFactory(cellData -> cellData.getValue().getLast());

		try {
			TradistaGUIUtil.fillFeedConfigComboBox(feedConfig);
			saveButton.setDisable(true);
			copyButton.setDisable(true);
			deleteButton.setDisable(true);
		} catch (TradistaTechnicalException _) {
			canGetFeedConfig = false;
		}

		TradistaGUIUtil.fillComboBox(Arrays.asList(QuoteType.values()), quoteType);
		TradistaGUIUtil.fillComboBox(Arrays.asList(FeedType.values()), feedType);

		feedMappingTable.getSelectionModel().selectedItemProperty().addListener((_, _, prop) -> {
			if (feedMappingTable.getSelectionModel().getSelectedItem() != null) {
				quoteNameTextField.setText(prop.getQuoteName().getValue());
				quoteType.setValue(QuoteType.getQuoteType(prop.getQuoteType().getValue()));
				bidTextField.setText(prop.getBid().getValue());
				fieldNameTextField.setText(prop.getFieldName().getValue());
				askTextField.setText(prop.getAsk().getValue());
				openTextField.setText(prop.getOpen().getValue());
				closeTextField.setText(prop.getClose().getValue());
				highTextField.setText(prop.getHigh().getValue());
				lowTextField.setText(prop.getLow().getValue());
				lastTextField.setText(prop.getLast().getValue());
			}
		});

		updateWindow();
	}

	@FXML
	protected void load() {
		try {
			if (this.feedConfig.getValue() == null) {
				throw new TradistaBusinessException("The feed config must be selected.");
			}
			currentFeedConfig = feedBusinessDelegate.getFeedConfigById(this.feedConfig.getValue().getId());
			feedConfigName.setText(currentFeedConfig.getName());
			feedType.setValue(currentFeedConfig.getFeedType());
			ObservableList<FeedMappingProperty> data = buildTableContent(currentFeedConfig);
			feedMappingTable.setItems(data);
			feedMappingTable.refresh();
			saveButton.setDisable(false);
			copyButton.setDisable(false);
			deleteButton.setDisable(false);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void save() {
		try {
			boolean isNew = (currentFeedConfig.getId() == 0);
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin() && isNew) {
				TradistaSaveConfirmationDialog dialog = new TradistaSaveConfirmationDialog("Feed Configuration",
						ClientUtil.getCurrentProcessingOrg(), false);
				Optional<LegalEntity> result = dialog.showAndWait();
				if (result.isPresent()) {
					po = result.get();
					proceed = true;
				}
			} else {
				TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
				confirmation.setTitle("Save Feed Configuration");
				confirmation.setHeaderText("Save Feed Configuration");
				confirmation.setContentText("Do you want to save this Feed Configuration?");

				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					proceed = true;
					po = currentFeedConfig.getProcessingOrg();
				}
			}

			if (proceed) {
				if (isNew) {
					currentFeedConfig = new FeedConfig(currentFeedConfig.getName(), po);
				}
				buildFeedConfig(currentFeedConfig);
				currentFeedConfig.setId(feedBusinessDelegate.saveFeedConfig(currentFeedConfig));
			}

		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void copy() {
		try {
			String copyName = null;
			LegalEntity po = null;
			boolean proceed = false;

			if (ClientUtil.currentUserIsAdmin()) {
				TradistaCopyDialog dialog = new TradistaCopyDialog("Feed Configuration",
						currentFeedConfig.getProcessingOrg(), currentFeedConfig.getName(), false);
				Optional<TradistaCopyDialog.Result> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get().getName();
					po = result.get().getProcessingOrg();
					proceed = true;
				}
			} else {
				TradistaTextInputDialog dialog = new TradistaTextInputDialog();
				dialog.setTitle("Feed Config name");
				dialog.setHeaderText("Feed Config name selection");
				dialog.setContentText("Please choose a Feed Config name:");

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					copyName = result.get();
					po = ClientUtil.getCurrentUser().getProcessingOrg();
					proceed = true;
				}
			}

			if (proceed) {
				FeedConfig copyFeedConfig = new FeedConfig(copyName, po);
				buildFeedConfig(copyFeedConfig);
				copyFeedConfig.setId(feedBusinessDelegate.saveFeedConfig(copyFeedConfig));
				TradistaGUIUtil.fillFeedConfigComboBox(this.feedConfig);
				feedConfigName.setText(currentFeedConfig.getName());
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void deleteFeedMappingValue() {
		int index = feedMappingTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			feedMappingTable.getItems().remove(index);
			feedMappingTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void create() {
		try {
			FeedConfigCreatorDialog dialog = new FeedConfigCreatorDialog();
			Optional<FeedConfig> result = dialog.showAndWait();

			if (result.isPresent()) {
				FeedConfig fc = result.get();
				feedBusinessDelegate.saveFeedConfig(fc);
				TradistaGUIUtil.fillFeedConfigComboBox(feedConfig);
			}
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void addFeedMappingValue() {
		try {
			if (fieldNameTextField.getText().isEmpty()) {
				throw new TradistaBusinessException("The field name cannot be empty");
			}
			new QuoteBusinessDelegate().validateQuoteName(quoteNameTextField.getText());
			FeedMappingProperty feedMappingPropertyToAdd = new FeedMappingProperty(quoteNameTextField.getText(),
					quoteType.getValue().toString(), fieldNameTextField.getText(), bidTextField.getText(),
					askTextField.getText(), openTextField.getText(), closeTextField.getText(), highTextField.getText(),
					lowTextField.getText(), lastTextField.getText());

			if (feedMappingTable.getItems().contains(feedMappingPropertyToAdd)) {
				feedMappingTable.getItems().remove(feedMappingPropertyToAdd);
			}

			feedMappingTable.getItems().add(feedMappingPropertyToAdd);

			quoteNameTextField.clear();
			bidTextField.clear();
			fieldNameTextField.clear();
			askTextField.clear();
			openTextField.clear();
			closeTextField.clear();
			highTextField.clear();
			lowTextField.clear();
			lastTextField.clear();

			ObservableList<FeedMappingProperty> newList = FXCollections.observableArrayList();
			newList.addAll(feedMappingTable.getItems());
			Collections.sort(newList);

			feedMappingTable.getItems().clear();
			feedMappingTable.getItems().addAll(newList);
		} catch (TradistaBusinessException tbe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, tbe.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	protected void delete() {
		try {
			TradistaAlert confirmation = new TradistaAlert(AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Feed Configuration");
			confirmation.setHeaderText("Delete Feed Configuration");
			confirmation.setContentText(
					String.format("Do you want to delete this Feed Configuration %s ?", currentFeedConfig.getName()));

			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				long id = currentFeedConfig.getId();
				feedBusinessDelegate.deleteFeedConfig(id);
				FeedConfig config = this.feedConfig.getValue();
				TradistaGUIUtil.fillFeedConfigComboBox(this.feedConfig);
				if (!config.equals(this.feedConfig.getValue())) {
					feedMappingTable.setItems(null);
					currentFeedConfig = null;
					feedConfigName.setText(StringUtils.EMPTY);
					saveButton.setDisable(true);
					copyButton.setDisable(true);
					deleteButton.setDisable(true);
				}
			}

		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, te.getMessage());
			alert.showAndWait();
		}
	}

	class EditingCell extends TableCell<FeedMappingProperty, String> {

		private TextField textField;

		public EditingCell() {
		}

		@Override
		public void startEdit() {
			if (textField != null && !StringUtils.isEmpty(textField.getText())) {
				setItem(textField.getText());
			}
			super.startEdit();
			createTextField();
			setText(textField.getText());
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.focusedProperty().addListener((_, _, isFocused) -> {
				if (Boolean.FALSE.equals(isFocused)) {
					commitEdit(textField.getText());
				}
			});

		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private ObservableList<FeedMappingProperty> buildTableContent(FeedConfig data) {

		List<FeedMappingProperty> feedMappingPropertyList = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : data.getFieldsMapping().entrySet()) {
			Quote quote = data.getMapping().get(entry.getKey());
			String bid = null;
			String ask = null;
			String open = null;
			String close = null;
			String high = null;
			String low = null;
			String last = null;
			for (Map.Entry<String, String> fieldEntry : entry.getValue().entrySet()) {
				switch (fieldEntry.getKey()) {
				case QuoteValue.BID: {
					bid = fieldEntry.getValue();
					break;
				}
				case QuoteValue.ASK: {
					ask = fieldEntry.getValue();
					break;
				}
				case QuoteValue.OPEN: {
					open = fieldEntry.getValue();
					break;
				}
				case QuoteValue.CLOSE: {
					close = fieldEntry.getValue();
					break;
				}
				case QuoteValue.HIGH: {
					high = fieldEntry.getValue();
					break;
				}
				case QuoteValue.LOW: {
					low = fieldEntry.getValue();
					break;
				}
				case QuoteValue.LAST: {
					last = fieldEntry.getValue();
					break;
				}
				}
			}
			feedMappingPropertyList.add(new FeedMappingProperty(quote.getName(), quote.getType().toString(),
					entry.getKey(), bid, ask, open, close, high, low, last));
		}

		Collections.sort(feedMappingPropertyList);

		return FXCollections.observableArrayList(feedMappingPropertyList);

	}

	private void buildFeedConfig(FeedConfig feedConfig) {
		if (feedConfig == null) {
			feedConfig = new FeedConfig(this.feedConfig.getValue().getName(),
					ClientUtil.getCurrentUser().getProcessingOrg());
		}
		feedConfig.setFeedType(feedType.getValue());
		if (feedMappingTable.getItems() != null) {
			Map<String, Map<String, String>> fieldsMapping = new HashMap<>();
			Map<String, Quote> mapping = new HashMap<>();

			for (FeedMappingProperty prop : feedMappingTable.getItems()) {
				String quoteName = prop.getQuoteName().getValue();
				QuoteType quoteType = QuoteType.getQuoteType(prop.getQuoteType().getValue());
				Quote quote = new Quote(quoteName, quoteType);
				mapping.put(prop.getFieldName().getValue(), quote);
				Map<String, String> currentField = new HashMap<>();
				currentField.put(QuoteValue.ASK, prop.getAsk().getValue());
				currentField.put(QuoteValue.BID, prop.getBid().getValue());
				currentField.put(QuoteValue.CLOSE, prop.getClose().getValue());
				currentField.put(QuoteValue.HIGH, prop.getHigh().getValue());
				currentField.put(QuoteValue.LAST, prop.getLast().getValue());
				currentField.put(QuoteValue.LOW, prop.getLow().getValue());
				currentField.put(QuoteValue.OPEN, prop.getOpen().getValue());
				fieldsMapping.put(prop.getFieldName().getValue(), currentField);
			}
			feedConfig.setFieldsMapping(fieldsMapping);
			feedConfig.setMapping(mapping);
		}
	}

	public static class FeedMappingProperty implements Comparable<FeedMappingProperty> {

		private final StringProperty quoteName;
		private final StringProperty quoteType;
		private final StringProperty fieldName;
		private final StringProperty bid;
		private final StringProperty ask;
		private final StringProperty open;
		private final StringProperty close;
		private final StringProperty high;
		private final StringProperty low;
		private final StringProperty last;

		private FeedMappingProperty(String quoteName, String quoteType, String fieldName, String bid, String ask,
				String open, String close, String high, String low, String last) {
			this.quoteName = new SimpleStringProperty(quoteName);
			this.quoteType = new SimpleStringProperty(quoteType);
			this.fieldName = new SimpleStringProperty(fieldName);
			this.bid = new SimpleStringProperty(bid);
			this.ask = new SimpleStringProperty(ask);
			this.open = new SimpleStringProperty(open);
			this.close = new SimpleStringProperty(close);
			this.high = new SimpleStringProperty(high);
			this.low = new SimpleStringProperty(low);
			this.last = new SimpleStringProperty(last);
		}

		public StringProperty getQuoteName() {
			return quoteName;
		}

		public void setQuoteName(String quoteName) {
			this.quoteName.set(quoteName);
		}

		public StringProperty getQuoteType() {
			return quoteType;
		}

		public void setQuoteType(String value) {
			this.quoteType.set(value);
		}

		public StringProperty getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName.set(fieldName);
		}

		public StringProperty getBid() {
			return bid;
		}

		public void setBid(String bid) {
			this.bid.set(bid);
		}

		public StringProperty getAsk() {
			return ask;
		}

		public void setAsk(String ask) {
			this.ask.set(ask);
		}

		public StringProperty getOpen() {
			return open;
		}

		public void setOpen(String open) {
			this.open.set(open);
		}

		public StringProperty getClose() {
			return close;
		}

		public void setClose(String close) {
			this.close.set(close);
		}

		public StringProperty getHigh() {
			return high;
		}

		public void setHigh(String high) {
			this.high.set(high);
		}

		public StringProperty getLow() {
			return low;
		}

		public void setLow(String low) {
			this.low.set(low);
		}

		public StringProperty getLast() {
			return last;
		}

		public void setLast(String last) {
			this.last.set(last);
		}

		@Override
		public int compareTo(FeedMappingProperty o) {
			return getQuoteName().toString().compareTo(o.getQuoteName().toString());
		}

		@Override
		public int hashCode() {
			return Objects.hash(quoteName.get(), quoteType.get());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FeedMappingProperty other = (FeedMappingProperty) obj;
			return Objects.equals(quoteName.get(), other.quoteName.get())
					&& Objects.equals(quoteType.get(), other.quoteType.get());
		}

	}

	@Override
	@FXML
	public void refresh() {
		try {
			TradistaGUIUtil.fillFeedConfigComboBox(feedConfig);
			canGetFeedConfig = true;
			canSaveFeedConfig = true;
			canCopyFeedConfig = true;
			canDeleteFeedConfig = true;
			canCreateFeedConfig = true;
		} catch (TradistaTechnicalException _) {
			canGetFeedConfig = false;
			canSaveFeedConfig = false;
			canCopyFeedConfig = false;
			canDeleteFeedConfig = false;
			canCreateFeedConfig = false;
		}

		updateWindow();
	}

	protected void updateWindow() {
		errors.clear();
		String errMsg;
		boolean isError = false;

		buildErrorMap();
		isError = !errors.isEmpty();
		errMsg = buildErrorMessage();

		if (isError) {
			TradistaGUIUtil.unapplyWarningStyle(marketDataMessage);
			TradistaGUIUtil.applyErrorStyle(marketDataMessage);
			marketDataMessage.setText(errMsg);
		}

		marketDataMessage.setVisible(isError);

		updateComponents();
	}

	public void buildErrorMap() {
		if (!canGetFeedConfig) {
			List<String> err = errors.get("get");
			if (err == null) {
				err = new ArrayList<>();
			}
			err.add(FEED_CONFIGURATIONS);
			errors.put("get", err);
		}
		if (!canSaveFeedConfig) {
			List<String> err = errors.get("save");
			if (err == null) {
				err = new ArrayList<>();
			}
			err.add(FEED_CONFIGURATIONS);
			errors.put("save", err);
		}
		if (!canCopyFeedConfig) {
			List<String> err = errors.get("copy");
			if (err == null) {
				err = new ArrayList<>();
			}
			err.add(FEED_CONFIGURATIONS);
			errors.put("copy", err);
		}
		if (!canDeleteFeedConfig) {
			List<String> err = errors.get("delete");
			if (err == null) {
				err = new ArrayList<>();
			}
			err.add(FEED_CONFIGURATIONS);
			errors.put("delete", err);
		}
		if (!canCreateFeedConfig) {
			List<String> err = errors.get("create");
			if (err == null) {
				err = new ArrayList<>();
			}
			err.add(FEED_CONFIGURATIONS);
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
			errMsg.append(StringUtils.SPACE);
			for (String err : errCat.getValue()) {
				errMsg.append(err).append(", ");
			}
			errMsg.delete(errMsg.length() - 2, errMsg.length());
			errMsg.append(".");
			errMsg.append(System.lineSeparator());
		}
		errMsg.append("Please contact support.");
		return errMsg.toString();
	}

	public void updateComponents() {
		feedConfig.setDisable(!canGetFeedConfig);
		loadButton.setDisable(!canGetFeedConfig);
		saveButton.setDisable(!canSaveFeedConfig);
		copyButton.setDisable(!canSaveFeedConfig);
		deleteButton.setDisable(!canSaveFeedConfig);
		addButton.setDisable(!canCreateFeedConfig);
	}

}