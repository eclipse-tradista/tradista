package org.eclipse.tradista.core.common.ui.view;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

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

public class TradistaEditingCell<S> extends TableCell<S, String> {

	private TextField textField;

	public TradistaEditingCell() {
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
		setText(getString());
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
		textField.setMaxWidth(Double.MAX_VALUE);
		textField.focusedProperty().addListener((_, _, isFocused) -> {
			if (Boolean.FALSE.equals(isFocused)) {
				commitEdit(textField.getText());
			}
		});
	}

	private String getString() {
		return getItem() == null ? StringUtils.EMPTY : getItem();
	}

}
