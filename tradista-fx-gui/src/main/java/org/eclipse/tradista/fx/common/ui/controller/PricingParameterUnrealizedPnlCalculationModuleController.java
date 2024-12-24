package org.eclipse.tradista.fx.common.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.ui.view.TradistaBookComboBox;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.ui.controller.TradistaControllerAdapter;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.ui.view.TradistaAlert;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.ui.controller.PricingParameterModuleController;
import org.eclipse.tradista.core.product.ui.view.TradistaProductTypeComboBox;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.BookProductTypePair;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.UnrealizedPnlCalculation;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterUnrealizedPnlCalculationModuleController extends TradistaControllerAdapter
		implements PricingParameterModuleController {

	@FXML
	private TableView<UnrealizedPnlCalculationProperty> unrealizedPnlCalculationTable;

	@FXML
	private TableColumn<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation> unrealizedPnlCalculation;

	@FXML
	private TableColumn<UnrealizedPnlCalculationProperty, String> fxProductType;

	@FXML
	private TableColumn<UnrealizedPnlCalculationProperty, Book> book;

	@FXML
	private TradistaProductTypeComboBox fxProductTypeComboBox;

	@FXML
	private TradistaBookComboBox bookComboBox;

	@FXML
	private ComboBox<UnrealizedPnlCalculation> unrealizedPnlCalculationComboBox;

	// This method is called by the FXMLLoader when initialization is complete
	public void initialize() {

		Callback<TableColumn<UnrealizedPnlCalculationProperty, String>, TableCell<UnrealizedPnlCalculationProperty, String>> unrealizedPnlCalculationProductTypeCellFactory = new Callback<TableColumn<UnrealizedPnlCalculationProperty, String>, TableCell<UnrealizedPnlCalculationProperty, String>>() {
			public TableCell<UnrealizedPnlCalculationProperty, String> call(
					TableColumn<UnrealizedPnlCalculationProperty, String> p) {
				return new UnrealizedPnlCalculationProductTypeEditingCell();
			}
		};

		Callback<TableColumn<UnrealizedPnlCalculationProperty, Book>, TableCell<UnrealizedPnlCalculationProperty, Book>> unrealizedPnlCalculationBookCellFactory = new Callback<TableColumn<UnrealizedPnlCalculationProperty, Book>, TableCell<UnrealizedPnlCalculationProperty, Book>>() {
			public TableCell<UnrealizedPnlCalculationProperty, Book> call(
					TableColumn<UnrealizedPnlCalculationProperty, Book> p) {
				return new UnrealizedPnlCalculationBookEditingCell();
			}
		};

		Callback<TableColumn<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>, TableCell<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>> unrealizedPnlCalculationCellFactory = new Callback<TableColumn<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>, TableCell<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>>() {
			public TableCell<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation> call(
					TableColumn<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation> p) {
				return new UnrealizedPnlCalculationEditingCell();
			}
		};

		fxProductType
				.setCellValueFactory(productType -> new ReadOnlyStringWrapper(productType.getValue().getProductType()));

		fxProductType.setCellFactory(unrealizedPnlCalculationProductTypeCellFactory);

		fxProductType.setOnEditCommit(new EventHandler<CellEditEvent<UnrealizedPnlCalculationProperty, String>>() {
			@Override
			public void handle(CellEditEvent<UnrealizedPnlCalculationProperty, String> t) {
				((UnrealizedPnlCalculationProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setProductType(t.getNewValue());
			}
		});

		book.setCellValueFactory(new PropertyValueFactory<UnrealizedPnlCalculationProperty, Book>("book"));

		book.setCellFactory(unrealizedPnlCalculationBookCellFactory);

		book.setOnEditCommit(new EventHandler<CellEditEvent<UnrealizedPnlCalculationProperty, Book>>() {
			@Override
			public void handle(CellEditEvent<UnrealizedPnlCalculationProperty, Book> t) {
				((UnrealizedPnlCalculationProperty) t.getTableView().getItems().get(t.getTablePosition().getRow()))
						.setBook(t.getNewValue());
			}
		});

		unrealizedPnlCalculation.setCellValueFactory(
				new PropertyValueFactory<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>(
						"unrealizedPnlCalculation"));

		unrealizedPnlCalculation.setCellFactory(unrealizedPnlCalculationCellFactory);

		unrealizedPnlCalculation.setOnEditCommit(
				new EventHandler<CellEditEvent<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation>>() {
					@Override
					public void handle(CellEditEvent<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation> t) {
						((UnrealizedPnlCalculationProperty) t.getTableView().getItems()
								.get(t.getTablePosition().getRow())).setUnrealizedPnlCalculation(t.getNewValue());
					}
				});
		fxProductTypeComboBox.setPromptText("Product type");
		bookComboBox.setPromptText("Book");
		unrealizedPnlCalculationComboBox.setPromptText("Unrealized PNL calculation");
		unrealizedPnlCalculationComboBox.setItems(FXCollections.observableArrayList(UnrealizedPnlCalculation.values()));
	}

	@FXML
	protected void deleteUnrealizedPnlCalculation() {
		int index = unrealizedPnlCalculationTable.getSelectionModel().getSelectedIndex();
		if (index >= 0) {
			unrealizedPnlCalculationTable.getItems().remove(index);
			unrealizedPnlCalculationTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	protected void addUnrealizedPnlCalculation() {
		try {
			StringBuilder errMsg = new StringBuilder();
			if (fxProductTypeComboBox.getValue() == null) {
				errMsg.append(String.format("Please select a Product Type.%n"));
			} else {
				if (bookComboBox.getValue() == null) {
					errMsg.append(String.format("Please select a Book.%n"));
				} else {
					if (unrealizedPnlCalculationTable.getItems().contains(new UnrealizedPnlCalculationProperty(
							bookComboBox.getValue(), fxProductTypeComboBox.getValue(), null))) {
						errMsg.append(String.format(
								"An unrealzed Pnl calculation is already in the list for book %s and product type %s.%n",
								bookComboBox.getValue(), fxProductTypeComboBox.getValue()));
					}
				}
			}
			if (unrealizedPnlCalculationComboBox.getValue() == null) {
				errMsg.append(String.format("Please select an unrealized pnl calculation.%n"));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}

			unrealizedPnlCalculationTable.getItems().add(new UnrealizedPnlCalculationProperty(bookComboBox.getValue(),
					fxProductTypeComboBox.getValue(), unrealizedPnlCalculationComboBox.getValue()));
			fxProductTypeComboBox.getSelectionModel().clearSelection();
			bookComboBox.getSelectionModel().clearSelection();
			unrealizedPnlCalculationComboBox.getSelectionModel().clearSelection();
		} catch (TradistaBusinessException abe) {
			TradistaAlert alert = new TradistaAlert(AlertType.ERROR, abe.getMessage());
			alert.showAndWait();
		}
	}

	private class UnrealizedPnlCalculationProductTypeEditingCell
			extends TableCell<UnrealizedPnlCalculationProperty, String> {

		private TradistaProductTypeComboBox productTypeComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createProductTypeComboBox();
			String productType = productTypeComboBox.getValue();
			if (productType != null) {
				setText(productType.toString());
			}
			setGraphic(productTypeComboBox);
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
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (productTypeComboBox != null) {
						productTypeComboBox.setValue(getItem());
					}
					setGraphic(productTypeComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createProductTypeComboBox() {
			productTypeComboBox = new TradistaProductTypeComboBox();
			productTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends String> observableValue, String oldProductType,
						String newProductType) {
					if (!changing && newProductType != null && oldProductType != null
							&& !oldProductType.equals(newProductType)) {
						StringBuilder errMsg = new StringBuilder();
						if (unrealizedPnlCalculationTable.getItems().contains(new UnrealizedPnlCalculationProperty(
								((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getBook(), newProductType,

								null))) {
							errMsg.append(String.format("The Product Type %s / Book %s Pair is already in the list.%n",
									newProductType,
									((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getBook()));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								productTypeComboBox.setValue(oldProductType);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				productTypeComboBox.setValue(getItem());
			}
			productTypeComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			productTypeComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!unrealizedPnlCalculationTable.getItems()
								.contains(new UnrealizedPnlCalculationProperty(
										((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getBook(),
										productTypeComboBox.getValue(), null))) {
							commitEdit(productTypeComboBox.getValue());
						}

					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class UnrealizedPnlCalculationBookEditingCell extends TableCell<UnrealizedPnlCalculationProperty, Book> {

		private TradistaBookComboBox bookComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createBookComboBox();
			Book book = bookComboBox.getValue();
			if (book != null) {
				setText(book.toString());
			}
			setGraphic(bookComboBox);
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
		public void updateItem(Book item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (bookComboBox != null) {
						bookComboBox.setValue(getItem());
					}
					setGraphic(bookComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createBookComboBox() {
			bookComboBox = new TradistaBookComboBox();
			bookComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Book>() {

				private boolean changing;

				@Override
				public void changed(ObservableValue<? extends Book> observableValue, Book oldBook, Book newBook) {
					if (!changing && newBook != null && oldBook != null && !oldBook.equals(newBook)) {
						StringBuilder errMsg = new StringBuilder();
						if (unrealizedPnlCalculationTable.getItems()
								.contains(new UnrealizedPnlCalculationProperty(newBook,
										((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getProductType(),
										null))) {
							errMsg.append(String.format("The Product Type %s / Book %s Pair is already in the list.%n",
									((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getProductType(),
									newBook));
						}
						if (errMsg.length() > 0) {
							changing = true;
							TradistaAlert alert = new TradistaAlert(AlertType.ERROR, errMsg.toString());
							alert.showAndWait();
							Platform.runLater(() -> {
								bookComboBox.setValue(oldBook);
								changing = false;
							});
						}
					}

				}
			});
			if (getItem() != null) {
				bookComboBox.setValue(getItem());
			}
			bookComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			bookComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						if (!unrealizedPnlCalculationTable.getItems()
								.contains(new UnrealizedPnlCalculationProperty(bookComboBox.getValue(),
										((UnrealizedPnlCalculationProperty) getTableRow().getItem()).getProductType(),
										null))) {
							commitEdit(bookComboBox.getValue());
						}

					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	private class UnrealizedPnlCalculationEditingCell
			extends TableCell<UnrealizedPnlCalculationProperty, UnrealizedPnlCalculation> {

		private ComboBox<UnrealizedPnlCalculation> unrealizedPnlCalculationComboBox;

		@Override
		public void startEdit() {
			super.startEdit();
			createUnrealizedPnlCalculationComboBox();
			UnrealizedPnlCalculation curve = unrealizedPnlCalculationComboBox.getValue();
			if (curve != null) {
				setText(curve.toString());
			}
			setGraphic(unrealizedPnlCalculationComboBox);
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
		public void updateItem(UnrealizedPnlCalculation item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (unrealizedPnlCalculationComboBox != null) {
						unrealizedPnlCalculationComboBox.setValue(getItem());
					}
					setGraphic(unrealizedPnlCalculationComboBox);

					setText(null);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createUnrealizedPnlCalculationComboBox() {
			unrealizedPnlCalculationComboBox = new ComboBox<UnrealizedPnlCalculation>();
			if (getItem() != null) {
				unrealizedPnlCalculationComboBox.setValue(getItem());
			}
			unrealizedPnlCalculationComboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			unrealizedPnlCalculationComboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(unrealizedPnlCalculationComboBox.getValue());
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? StringUtils.EMPTY : getItem().toString();
		}
	}

	public void load(PricingParameter pricingParam) {

		PricingParameterUnrealizedPnlCalculationModule module = null;
		for (PricingParameterModule mod : pricingParam.getModules()) {
			if (mod instanceof PricingParameterUnrealizedPnlCalculationModule) {
				module = (PricingParameterUnrealizedPnlCalculationModule) mod;
				break;
			}
		}

		if (module != null) {

			List<UnrealizedPnlCalculationProperty> unrealizedPnlCalculationPropertyList = new ArrayList<UnrealizedPnlCalculationProperty>();

			for (Map.Entry<BookProductTypePair, UnrealizedPnlCalculation> entry : module.getUnrealizedPnlCalculations()
					.entrySet()) {
				unrealizedPnlCalculationPropertyList.add(new UnrealizedPnlCalculationProperty(entry.getKey().getBook(),
						entry.getKey().getProductType(), entry.getValue()));
			}

			Collections.sort(unrealizedPnlCalculationPropertyList);

			unrealizedPnlCalculationTable
					.setItems(FXCollections.observableArrayList(unrealizedPnlCalculationPropertyList));
		} else {
			unrealizedPnlCalculationTable.getItems().clear();
		}
	}

	public PricingParameterModule buildModule() {
		PricingParameterUnrealizedPnlCalculationModule param = new PricingParameterUnrealizedPnlCalculationModule();
		for (UnrealizedPnlCalculationProperty prop : unrealizedPnlCalculationTable.getItems()) {
			param.getUnrealizedPnlCalculations().put(
					new BookProductTypePair((Book) prop.getBook(), prop.getProductType()),
					(UnrealizedPnlCalculation) prop.getUnrealizedPnlCalculation());
		}
		return param;
	}

	protected class UnrealizedPnlCalculationProperty implements Comparable<UnrealizedPnlCalculationProperty> {

		private final SimpleObjectProperty book;
		private final SimpleStringProperty productType;
		private final SimpleObjectProperty unrealizedPnlCalculation;

		private UnrealizedPnlCalculationProperty(Object book, String productType, Object unrealizedPnlCalculation) {
			this.book = new SimpleObjectProperty(book);
			this.productType = new SimpleStringProperty(productType);
			this.unrealizedPnlCalculation = new SimpleObjectProperty(unrealizedPnlCalculation);
		}

		public Object getBook() {
			return book.get();
		}

		public void setBook(Object book) {
			this.book.set(book);
		}

		public String getProductType() {
			return productType.get();
		}

		public void setProductType(String productType) {
			this.productType.set(productType);
		}

		public Object getUnrealizedPnlCalculation() {
			return unrealizedPnlCalculation.get();
		}

		public void setUnrealizedPnlCalculation(Object curve) {
			this.unrealizedPnlCalculation.set(curve);
		}

		@Override
		public int compareTo(UnrealizedPnlCalculationProperty o) {
			return (getBook().toString() + getProductType()).compareTo(o.getBook().toString() + o.getProductType());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getBook() == null) ? 0 : getBook().hashCode());
			result = prime * result + ((getProductType() == null) ? 0 : getProductType().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UnrealizedPnlCalculationProperty other = (UnrealizedPnlCalculationProperty) obj;
			if (getBook() == null) {
				if (other.getBook() != null)
					return false;
			} else if (!getBook().equals(other.getBook()))
				return false;
			if (getProductType() == null) {
				if (other.getProductType() != null)
					return false;
			} else if (!getProductType().equals(other.getProductType()))
				return false;
			return true;
		}

	}

	@Override
	public void clear() {
		unrealizedPnlCalculationTable.setItems(null);
	}

	@Override
	@FXML
	public void refresh() {
		TradistaGUIUtil.fillBookComboBox(bookComboBox);
	}

}