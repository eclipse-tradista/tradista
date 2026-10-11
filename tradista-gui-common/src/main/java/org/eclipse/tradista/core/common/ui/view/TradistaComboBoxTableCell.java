package org.eclipse.tradista.core.common.ui.view;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

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

public class TradistaComboBoxTableCell<S, T> extends TableCell<S, T> {

	private ComboBox<T> comboBox;
	private final Supplier<ComboBox<T>> comboBoxSupplier;
	private final BiFunction<S, T, String> validator;

	public TradistaComboBoxTableCell(Supplier<ComboBox<T>> comboBoxSupplier) {
		this(comboBoxSupplier, null);
	}

	public TradistaComboBoxTableCell(Supplier<ComboBox<T>> comboBoxSupplier, BiFunction<S, T, String> validator) {
		this.comboBoxSupplier = comboBoxSupplier;
		this.validator = validator;
	}

	public void configureDefaultComboBox(ComboBox<T> cb) {
		cb.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

		cb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<T>() {
			private boolean changing;

			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				if (!changing && newValue != null && oldValue != null && !oldValue.equals(newValue)) {
					S rowItem = getTableRow() != null ? getTableRow().getItem() : null;
					if (rowItem != null && validator != null) {
						String errorMsg = validator.apply(rowItem, newValue);
						if (!StringUtils.isEmpty(errorMsg)) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errorMsg);
							alert.showAndWait();
							Platform.runLater(() -> {
								cb.setValue(oldValue);
								changing = false;
							});
							return;
						}
					}
					if (isEditing()) {
						commitEdit(newValue);
					}
				}
			}
		});

		cb.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				T val = cb.getValue();
				if (validate(val)) {
					commitEdit(val);
				}
			} else if (event.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});

		cb.focusedProperty().addListener((_, _, isFocused) -> {
			if (Boolean.FALSE.equals(isFocused) && isEditing()) {
				T val = cb.getValue();
				if (validate(val)) {
					commitEdit(val);
				} else {
					cancelEdit();
				}
			}
		});
	}

	private boolean validate(T val) {
		if (validator != null) {
			S rowItem = getTableRow() != null ? getTableRow().getItem() : null;
			if (rowItem != null && !StringUtils.isEmpty(validator.apply(rowItem, val))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void commitEdit(T newValue) {
		if (!isEditing()) {
			TableView<S> table = getTableView();
			if (table != null) {
				TableColumn<S, T> column = getTableColumn();
				TablePosition<S, T> pos = new TablePosition<>(table, getIndex(), column);
				CellEditEvent<S, T> event = new CellEditEvent<>(table, pos, TableColumn.editCommitEvent(), newValue);
				javafx.event.Event.fireEvent(column, event);
			}
		}
		super.commitEdit(newValue);
		setText(newValue == null ? StringUtils.EMPTY : newValue.toString());
		setGraphic(null);
	}

	@Override
	public void startEdit() {
		super.startEdit();
		if (comboBox == null) {
			comboBox = comboBoxSupplier.get();
			if (getItem() != null) {
				comboBox.setValue(getItem());
			}
			configureDefaultComboBox(comboBox);
		} else if (getItem() != null) {
			comboBox.setValue(getItem());
		}
		if (getItem() != null) {
			setText(getItem().toString());
		}
		setGraphic(comboBox);
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		if (getItem() != null) {
			setText(getItem().toString());
		}
		setGraphic(null);
	}

	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (comboBox != null) {
					comboBox.setValue(getItem());
				}
				setGraphic(comboBox);
				setText(null);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private String getString() {
		return getItem() == null ? StringUtils.EMPTY : getItem().toString();
	}
}