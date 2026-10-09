package org.eclipse.tradista.core.common.ui.view;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

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
	private final Function<TradistaComboBoxTableCell<S, T>, ComboBox<T>> comboBoxSupplier;
	private final BiConsumer<S, T> onCommit;

	public TradistaComboBoxTableCell(Supplier<ComboBox<T>> comboBoxSupplier) {
		this(cell -> {
			ComboBox<T> cb = comboBoxSupplier.get();
			cell.configureDefaultComboBox(cb);
			return cb;
		}, null);
	}

	public TradistaComboBoxTableCell(Supplier<ComboBox<T>> comboBoxSupplier, BiConsumer<S, T> onCommit) {
		this(cell -> {
			ComboBox<T> cb = comboBoxSupplier.get();
			cell.configureDefaultComboBox(cb);
			return cb;
		}, onCommit);
	}

	public TradistaComboBoxTableCell(Function<TradistaComboBoxTableCell<S, T>, ComboBox<T>> comboBoxSupplier) {
		this(comboBoxSupplier, null);
	}

	public TradistaComboBoxTableCell(Function<TradistaComboBoxTableCell<S, T>, ComboBox<T>> comboBoxSupplier,
			BiConsumer<S, T> onCommit) {
		this.comboBoxSupplier = comboBoxSupplier;
		this.onCommit = onCommit;
	}

	public static <S, T> TradistaComboBoxTableCell<S, T> of(ObservableList<T> items) {
		return new TradistaComboBoxTableCell<>(cell -> {
			ComboBox<T> cb = new ComboBox<>(items);
			cell.configureDefaultComboBox(cb);
			return cb;
		});
	}

	public static <S, T> TradistaComboBoxTableCell<S, T> of(ObservableList<T> items, BiConsumer<S, T> onCommit) {
		return new TradistaComboBoxTableCell<>(cell -> {
			ComboBox<T> cb = new ComboBox<>(items);
			cell.configureDefaultComboBox(cb);
			return cb;
		}, onCommit);
	}

	public void configureDefaultComboBox(ComboBox<T> cb) {
		cb.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		cb.focusedProperty().addListener((_, _, isFocused) -> {
			if (Boolean.FALSE.equals(isFocused)) {
				T val = cb.getValue();
				commitEdit(val);
				S rowItem = getTableRow() != null ? getTableRow().getItem() : null;
				if (rowItem != null && onCommit != null) {
					onCommit.accept(rowItem, val);
				}
			}
		});
	}

	@Override
	public void startEdit() {
		super.startEdit();
		if (comboBox == null) {
			comboBox = comboBoxSupplier.apply(this);
		}
		if (getItem() != null) {
			comboBox.setValue(getItem());
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