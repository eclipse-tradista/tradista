package org.eclipse.tradista.core.common.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.daycountconvention.service.DayCountConventionBusinessDelegate;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.exchange.service.ExchangeBusinessDelegate;
import org.eclipse.tradista.core.index.model.BlankIndex;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;
import org.eclipse.tradista.core.position.model.BlankPositionDefinition;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

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

public class TradistaGUIUtil {

	private static final int GOLDEN_RATIO_WIDTH = 16;

	private static final int GOLDEN_RATIO_HEIGHT = 38;

	private static final int GOLDEN_RATIO_FONT_WIDTH = 132;

	private static final String COMPONENTS_WIDTH = "COMPONENTS_WIDTH";

	private static final String COMPONENTS_HEIGHT = "COMPONENTS_HEIGHT";

	private static final String FONT_SIZE = "FONT_SIZE";

	private static final int MIN_FONT_SIZE = 12;

	private static final int MAX_FONT_SIZE = 18;

	private static final int MIN_COMPONENT_WIDTH = 110;

	private static final int MAX_COMPONENT_WIDTH = 165;

	private static final int MIN_COMPONENT_HEIGHT = 26;

	private static final int MAX_COMPONENT_HEIGHT = 39;

	private static final String WARNING_CSS_CLASS = "labelWarning";

	private static final String ERROR_CSS_CLASS = "labelError";

	private static IndexBusinessDelegate indexBusinessDelegate = new IndexBusinessDelegate();

	private static DayCountConventionBusinessDelegate dayCountConventionBusinessDelegate = new DayCountConventionBusinessDelegate();

	private static CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();

	private static BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

	private static ExchangeBusinessDelegate exchangeBusinessDelegate = new ExchangeBusinessDelegate();

	private static DateRuleBusinessDelegate dateRuleBusinessDelegate = new DateRuleBusinessDelegate();

	private static PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();

	private static PricerBusinessDelegate pricerBusinessDelegate = new PricerBusinessDelegate();

	private static LegalEntityBusinessDelegate legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();

	private static SurfaceBusinessDelegate surfaceBusinessDelegate = new SurfaceBusinessDelegate();

	public static void resizeComponents(Window window, Number oldWidth) {
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		TradistaGUIUtil.resizeComponentHeights(primScreenBounds, window, 0);
		TradistaGUIUtil.resizeComponentWidths(primScreenBounds, window, 0);
	}

	public static void resizeComponentWidths(Rectangle2D screen, Window window, Number oldWidth) {
		double fontSize;
		double componentsWidth;
		if (window.getProperties().containsKey(FONT_SIZE)) {
			fontSize = (double) window.getProperties().get(FONT_SIZE);
			if (oldWidth.doubleValue() != 0) {
				fontSize = screen.getWidth() / oldWidth.doubleValue();
			}
		} else {
			fontSize = screen.getWidth() / GOLDEN_RATIO_FONT_WIDTH;
		}
		fontSize = Math.min(MAX_FONT_SIZE, Math.max(MIN_FONT_SIZE, fontSize));
		window.getProperties().put(FONT_SIZE, fontSize);
		if (window.getProperties().containsKey(COMPONENTS_WIDTH)) {
			componentsWidth = (double) window.getProperties().get(COMPONENTS_WIDTH);
			if (oldWidth.doubleValue() != 0) {
				componentsWidth = componentsWidth * (screen.getWidth() / oldWidth.doubleValue());
			}
		} else {
			componentsWidth = screen.getWidth() / GOLDEN_RATIO_WIDTH;
		}
		componentsWidth = Math.min(MAX_COMPONENT_WIDTH, Math.max(MIN_COMPONENT_WIDTH, componentsWidth));
		window.getProperties().put(COMPONENTS_WIDTH, componentsWidth);
		List<Node> nodes = getAllNodesInWindow(window);
		if (nodes != null && !nodes.isEmpty()) {
			String style = "-fx-font-size: " + fontSize + "px;";
			for (Node n : nodes) {
				if (n instanceof Labeled) {
					Labeled labeled = ((Labeled) n);
					labeled.setStyle(style);
				}
				if (n instanceof ComboBox) {
					ComboBox<?> comboBox = ((ComboBox<?>) n);
					comboBox.setPrefWidth(componentsWidth);
					comboBox.setStyle(style);
				}
				if (n instanceof TextField) {
					TextField textField = ((TextField) n);
					textField.setFont(Font.font(fontSize));
					textField.setPrefWidth(componentsWidth);
				}
				if (n instanceof TextArea) {
					TextArea textArea = ((TextArea) n);
					textArea.setFont(Font.font(fontSize));
					textArea.setPrefWidth(componentsWidth * 2);
				}
				if (n instanceof ListView) {
					ListView<?> listView = ((ListView<?>) n);
					listView.setPrefWidth(componentsWidth);
				}
				if (n instanceof DatePicker) {
					((DatePicker) n).setPrefWidth(componentsWidth);
					((DatePicker) n).setStyle(style);
				}
				if (n instanceof CheckBox) {
					((CheckBox) n).setFont(Font.font(fontSize));
				}
				if (n instanceof Button) {
					((Button) n).setStyle(style);
				}
				if (n instanceof TableView) {
					for (Object o : ((TableView<?>) n).getColumns()) {
						TableColumn<?, ?> tc = (TableColumn<?, ?>) o;
						tc.setPrefWidth(componentsWidth);
						tc.setStyle(style);
					}
				}
				if (n instanceof TabPane) {
					for (Tab tab : ((TabPane) n).getTabs()) {
						tab.setStyle(style);
					}
				}
				if (n instanceof PieChart) {
					((PieChart) n).setPrefWidth(componentsWidth * 3);
				}
				if (n instanceof MenuBar) {
					((MenuBar) n).setStyle(style);
				}
			}
		}
	}

	public static void applyWarningStyle(Node node) {
		applyStyle(node, WARNING_CSS_CLASS);
	}

	public static void unapplyWarningStyle(Node node) {
		unapplyStyle(node, WARNING_CSS_CLASS);
	}

	public static void applyErrorStyle(Node node) {
		applyStyle(node, ERROR_CSS_CLASS);
	}

	public static void unapplyErrorStyle(Node node) {
		unapplyStyle(node, ERROR_CSS_CLASS);
	}

	public static void applyStyle(Node node, String style) throws TradistaTechnicalException {
		StringBuilder errMsg = new StringBuilder();
		if (node == null) {
			errMsg.append(String.format("Node is mandatory.%n"));
		}
		if (StringUtils.isEmpty(style)) {
			errMsg.append(String.format("Style is mandatory.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		if (!node.getStyleClass().contains(style)) {
			node.getStyleClass().add(style);
		}
	}

	public static void unapplyStyle(Node node, String style) {
		StringBuilder errMsg = new StringBuilder();
		if (node == null) {
			errMsg.append(String.format("Node is mandatory.%n"));
		}
		if (StringUtils.isEmpty(style)) {
			errMsg.append(String.format("Style is mandatory.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		node.getStyleClass().remove(style);
	}

	public static void resizeComponentHeights(Rectangle2D screen, Window window, Number oldHeight) {
		double fontSize;
		double componentsHeight;
		if (window.getProperties().containsKey(FONT_SIZE)) {
			fontSize = (double) window.getProperties().get(FONT_SIZE);
			if (oldHeight.doubleValue() != 0) {
				fontSize = fontSize * (screen.getHeight() / oldHeight.doubleValue());
			}
		} else {
			fontSize = screen.getWidth() / GOLDEN_RATIO_FONT_WIDTH;
		}
		fontSize = Math.min(MAX_FONT_SIZE, Math.max(MIN_FONT_SIZE, fontSize));
		window.getProperties().put(FONT_SIZE, fontSize);
		if (window.getProperties().containsKey(COMPONENTS_HEIGHT)) {
			componentsHeight = (double) window.getProperties().get(COMPONENTS_HEIGHT);
			if (oldHeight.doubleValue() != 0) {
				componentsHeight = componentsHeight * (screen.getHeight() / oldHeight.doubleValue());
			}
		} else {
			componentsHeight = screen.getHeight() / GOLDEN_RATIO_HEIGHT;
		}
		componentsHeight = Math.min(MAX_COMPONENT_HEIGHT, Math.max(MIN_COMPONENT_HEIGHT, componentsHeight));
		window.getProperties().put(COMPONENTS_HEIGHT, componentsHeight);
		List<Node> nodes = getAllNodesInWindow(window);
		if (nodes != null && !nodes.isEmpty()) {
			String style = "-fx-font-size: " + fontSize + "px;";
			for (Node n : nodes) {
				if (n instanceof Labeled) {
					Labeled labeled = ((Labeled) n);
					labeled.setStyle(style);
				}
				if (n instanceof ComboBox) {
					ComboBox<?> comboBox = ((ComboBox<?>) n);
					comboBox.setPrefHeight(componentsHeight);
					comboBox.setStyle(style);
				}
				if (n instanceof TextField) {
					TextField textField = ((TextField) n);
					textField.setPrefHeight(componentsHeight);
					textField.setFont(Font.font(fontSize));
				}
				if (n instanceof TextArea) {
					TextArea textArea = ((TextArea) n);
					textArea.setFont(Font.font(fontSize));
					textArea.setPrefHeight(componentsHeight * 2);
				}
				if (n instanceof DatePicker) {
					((DatePicker) n).setPrefHeight(componentsHeight);
					((DatePicker) n).setStyle(style);
				}
				if (n instanceof ListView) {
					ListView<?> listView = ((ListView<?>) n);
					listView.setPrefHeight(componentsHeight * 5);
				}
				if (n instanceof CheckBox) {
					((CheckBox) n).setFont(Font.font(fontSize));
				}
				if (n instanceof Button) {
					((Button) n).setStyle(style);
				}
				if (n instanceof TableView) {
					TableView<?> tableView = ((TableView<?>) n);
					tableView.setPrefHeight(screen.getHeight() / 4);
					for (Object o : ((TableView<?>) n).getColumns()) {
						TableColumn<?, ?> tc = (TableColumn<?, ?>) o;
						tc.setStyle(style);
					}
				}
				if (n instanceof TabPane) {
					for (Tab tab : ((TabPane) n).getTabs()) {
						tab.setStyle(style);
					}
				}
				if (n instanceof PieChart) {
					((PieChart) n).setPrefHeight(screen.getHeight() / 3);
				}
				if (n instanceof MenuBar) {
					((MenuBar) n).setStyle(style);
				}
			}
		}
	}

	public static List<Node> getAllNodesInWindow(Window window) {
		Parent root = Optional.of(window).map(w -> w.getScene()).map(s -> s.getRoot()).get();
		if (root == null) {
			return new ArrayList<>();
		} else {
			List<Node> ret = new ArrayList<>();
			ret.add(root);
			ret.addAll(getAllNodesInParent(root));
			return ret;
		}
	}

	public static List<Node> getAllNodesInParent(Parent parent) {
		List<Node> ret = new ArrayList<>();
		ObservableList<Node> children;
		if (parent instanceof ScrollPane) {
			children = FXCollections.observableArrayList();
			children.add(((ScrollPane) parent).getContent());
		} else {
			children = parent.getChildrenUnmodifiable();
		}
		for (Node child : children) {
			ret.add(child);
			if (child instanceof TabPane) {
				ObservableList<Tab> tabs = ((TabPane) child).getTabs();
				if (tabs != null && !tabs.isEmpty()) {
					for (Tab tab : tabs) {
						ret.addAll(getAllNodesInParent((Parent) tab.getContent()));
					}
				}
			} else if (child instanceof ScrollPane) {
				ret.addAll(getAllNodesInParent((Parent) ((ScrollPane) child).getContent()));
			}

			else if (child instanceof Parent) {
				ret.addAll(getAllNodesInParent((Parent) child));
			}
		}
		return ret;
	}

	@SafeVarargs
	public static <T> void fillComboBox(Collection<? extends T> collection, ComboBox<T>... comboBoxes) {
		ObservableList<T> data;
		if (collection != null && !collection.isEmpty()) {
			data = FXCollections.observableArrayList(collection);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<T> cb : comboBoxes) {
				T element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}

	}

	@SafeVarargs
	public static void fillTradeDirectionComboBox(ComboBox<Trade.Direction>... comboBoxes) {
		ObservableList<Trade.Direction> data = FXCollections.observableArrayList(Trade.Direction.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<Trade.Direction> cb : comboBoxes) {
				Trade.Direction element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillOptionStyleComboBox(ComboBox<VanillaOptionTrade.Style>... comboBoxes) {
		ObservableList<VanillaOptionTrade.Style> data = FXCollections
				.observableArrayList(VanillaOptionTrade.Style.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<VanillaOptionTrade.Style> cb : comboBoxes) {
				VanillaOptionTrade.Style element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillOptionTypeComboBox(ComboBox<OptionTrade.Type>... comboBoxes) {
		ObservableList<OptionTrade.Type> data = FXCollections.observableArrayList(OptionTrade.Type.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<OptionTrade.Type> cb : comboBoxes) {
				OptionTrade.Type element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillOptionSettlementTypeComboBox(ComboBox<OptionTrade.SettlementType>... comboBoxes) {
		ObservableList<OptionTrade.SettlementType> data = FXCollections
				.observableArrayList(OptionTrade.SettlementType.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<OptionTrade.SettlementType> cb : comboBoxes) {
				OptionTrade.SettlementType element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillQuoteTypeComboBox(ComboBox<QuoteType>... comboBoxes) {
		ObservableList<QuoteType> data = FXCollections.observableArrayList(QuoteType.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<QuoteType> cb : comboBoxes) {
				QuoteType element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillErrorStatusComboBox(ComboBox<String>... comboBoxes) {
		List<org.eclipse.tradista.core.error.model.Error.Status> data = Arrays
				.asList(org.eclipse.tradista.core.error.model.Error.Status.values());
		List<String> statusStrings = data.stream().map(it -> it.toString()).collect(Collectors.toList());
		if (comboBoxes.length > 0) {
			for (ComboBox<String> cb : comboBoxes) {
				String element = cb.getValue();
				cb.setItems(FXCollections.observableArrayList(statusStrings));
				cb.getItems().add(0, StringUtils.EMPTY);
				if (element != null && statusStrings.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillTenorComboBox(ComboBox<Tenor>... comboBoxes) {
		fillTenorComboBox(false, comboBoxes);
	}

	@SafeVarargs
	public static void fillTenorComboBox(boolean includeNoTenor, ComboBox<Tenor>... comboBoxes) {
		ObservableList<Tenor> data = FXCollections.observableArrayList(Tenor.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<Tenor> cb : comboBoxes) {
				Tenor element = cb.getValue();
				cb.setItems(data);
				if (!includeNoTenor) {
					cb.getItems().remove(Tenor.NO_TENOR);
				}
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillDateRollingConventionComboBox(ComboBox<DateRollingConvention>... comboBoxes) {
		ObservableList<DateRollingConvention> data = FXCollections.observableArrayList(DateRollingConvention.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<DateRollingConvention> cb : comboBoxes) {
				DateRollingConvention element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillInterestPaymentComboBox(ComboBox<InterestPayment>... comboBoxes) {
		ObservableList<InterestPayment> data = FXCollections.observableArrayList(InterestPayment.values());
		if (comboBoxes.length > 0) {
			for (ComboBox<InterestPayment> cb : comboBoxes) {
				InterestPayment element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillDayComboBox(boolean withAny, ComboBox<String>... comboBoxes) {
		List<String> days = new ArrayList<String>();
		if (withAny) {
			days.add("Any");
		}
		for (DayOfWeek d : DayOfWeek.values()) {
			days.add(d.toString());
		}

		ObservableList<String> data = FXCollections.observableArrayList(days);
		if (comboBoxes.length > 0) {
			for (ComboBox<String> cb : comboBoxes) {
				String element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillIndexComboBox(ComboBox<Index>... comboBoxes) {
		fillIndexComboBox(false, comboBoxes);
	}

	@SafeVarargs
	public static void fillIndexComboBox(boolean addBlank, ComboBox<Index>... comboBoxes) {
		Set<Index> indexes = indexBusinessDelegate.getAllIndexes();
		ObservableList<Index> data = null;
		if (indexes != null && !indexes.isEmpty()) {
			data = FXCollections.observableArrayList(indexes);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (addBlank) {
			data.add(0, BlankIndex.getInstance());
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<Index> cb : comboBoxes) {
				Index element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillPositionDefinitionComboBox(ComboBox<PositionDefinition>... comboBoxes) {
		fillPositionDefinitionComboBox(false, comboBoxes);
	}

	@SafeVarargs
	public static void fillPositionDefinitionComboBox(boolean addBlank, ComboBox<PositionDefinition>... comboBoxes) {
		Set<PositionDefinition> posDefs = positionDefinitionBusinessDelegate.getAllPositionDefinitions();
		ObservableList<PositionDefinition> data = null;
		if (posDefs != null && !posDefs.isEmpty()) {
			data = FXCollections.observableArrayList(posDefs);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (addBlank) {
			data.add(0, BlankPositionDefinition.getInstance());
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<PositionDefinition> cb : comboBoxes) {
				PositionDefinition element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillPricingParameterComboBox(ComboBox<PricingParameter>... comboBoxes) {
		Set<PricingParameter> pp = pricerBusinessDelegate.getAllPricingParameters();
		ObservableList<PricingParameter> data = null;
		if (pp != null && !pp.isEmpty()) {
			data = FXCollections.observableArrayList(pp);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<PricingParameter> cb : comboBoxes) {
				PricingParameter element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillDayCountConventionComboBox(ComboBox<DayCountConvention>... comboBoxes) {
		Set<DayCountConvention> dayCountConventions = dayCountConventionBusinessDelegate.getAllDayCountConventions();
		ObservableList<DayCountConvention> data = null;
		if (dayCountConventions != null && !dayCountConventions.isEmpty()) {
			data = FXCollections.observableArrayList(dayCountConventions);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<DayCountConvention> cb : comboBoxes) {
				DayCountConvention element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillCurrencyComboBox(ComboBox<Currency>... comboBoxes) {
		Set<Currency> currencies = currencyBusinessDelegate.getAllCurrencies();
		ObservableList<Currency> data = null;
		if (currencies != null && !currencies.isEmpty()) {
			data = FXCollections.observableArrayList(currencies);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<Currency> cb : comboBoxes) {
				Currency element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillProcessingOrgComboBox(ComboBox<LegalEntity>... comboBoxes) {
		Set<LegalEntity> pos = legalEntityBusinessDelegate.getAllProcessingOrgs();
		ObservableList<LegalEntity> data = null;
		if (pos != null && !pos.isEmpty()) {
			data = FXCollections.observableArrayList(pos);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<LegalEntity> cb : comboBoxes) {
				LegalEntity element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillBookComboBox(ComboBox<Book>... comboBoxes) {
		Set<Book> books = bookBusinessDelegate.getAllBooks();
		ObservableList<Book> data = null;
		if (books != null && !books.isEmpty()) {
			data = FXCollections.observableArrayList(books);
		} else {
			data = FXCollections.observableArrayList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<Book> cb : comboBoxes) {
				Book element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillExchangeComboBox(ComboBox<Exchange>... comboBoxes) {
		Set<Exchange> exchanges = exchangeBusinessDelegate.getAllExchanges();
		ObservableList<Exchange> data = null;
		if (exchanges != null && !exchanges.isEmpty()) {
			data = FXCollections.observableArrayList(exchanges);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<Exchange> cb : comboBoxes) {
				Exchange element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillSurfaceComboBox(String surfaceType, ComboBox<VolatilitySurface<?, ?, ?>>... comboBoxes) {
		List<VolatilitySurface<?, ?, ?>> surfaces = surfaceBusinessDelegate.getSurfaces(surfaceType);
		ObservableList<VolatilitySurface<?, ?, ?>> data = null;
		if (surfaces != null && !surfaces.isEmpty()) {
			data = FXCollections.observableArrayList(surfaces);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<VolatilitySurface<?, ?, ?>> cb : comboBoxes) {
				VolatilitySurface<?, ?, ?> element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillDateRuleComboBox(ComboBox<DateRule>... comboBoxes) {
		Set<DateRule> dateRules = dateRuleBusinessDelegate.getAllDateRules();
		ObservableList<DateRule> data = null;
		if (dateRules != null && !dateRules.isEmpty()) {
			data = FXCollections.observableArrayList(dateRules);
		} else {
			data = FXCollections.emptyObservableList();
		}
		if (comboBoxes.length > 0) {
			for (ComboBox<DateRule> cb : comboBoxes) {
				DateRule element = cb.getValue();
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(element);
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	public static void setTradistaIcons(Stage stage) {
		stage.getIcons().add(new Image("tradista-icon-16x16.png"));
		stage.getIcons().add(new Image("tradista-icon-32x32.png"));
		stage.getIcons().add(new Image("tradista-icon-48x48.png"));
		stage.getIcons().add(new Image("tradista-icon-64x64.png"));
	}

	public static void processTaskAndDisplayLoadingDialog(Task<Void> task) {
		ProgressBar pBar = new ProgressBar();
		Media media = null;
		try {
			media = new Media(TradistaGUIUtil.class.getResource("/tradista-animation.mp4").toURI().toString());
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		}
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		Thread thread;
		Group root;
		Stage dialog = new Stage();
		GridPane pane = new GridPane();
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		Label label = new Label("Now loading...");
		mediaPlayer.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
		mediaPlayer.play();
		MediaView mediaView = new MediaView(mediaPlayer);
		Scene scene;

		mediaView.setViewport(new Rectangle2D(700, 370, 450, 330));
		mediaView.setPreserveRatio(true);
		mediaView.setSmooth(true);
		mediaView.setCache(true);

		mediaView.fitWidthProperty().bind(dialog.widthProperty());
		pane.add(mediaView, 0, 0);
		label.setStyle("-fx-font-size: 20;");
		pane.add(label, 0, 1);
		GridPane.setHalignment(label, HPos.CENTER);
		pane.add(pBar, 0, 2);
		root = new Group();
		root.getChildren().add(pane);
		scene = new Scene(root);
		dialog.initStyle(StageStyle.UNDECORATED);
		dialog.setScene(scene);
		try {
			User currentUser = ClientUtil.getCurrentUser();
			String styleSheetLocation = null;
			if (currentUser != null) {
				styleSheetLocation = "/"
						+ new ConfigurationBusinessDelegate().getUIConfiguration(currentUser).getStyle() + "Style.css";
			} else {
				styleSheetLocation = "/" + new ConfigurationBusinessDelegate().getDefaultStyle() + "Style.css";
			}
			pane.getStylesheets().add(styleSheetLocation);
		} catch (TradistaBusinessException tbe) {
		}
		pane.getStyleClass().add("root");
		TradistaGUIUtil.setTradistaIcons(dialog);
		pBar.progressProperty().bind(task.progressProperty());
		pBar.minWidthProperty().bind(scene.widthProperty());
		task.setOnSucceeded(event -> {
			dialog.close();
		});
		task.setOnFailed(event -> task.getException().printStackTrace());

		dialog.sizeToScene();
		dialog.setResizable(false);

		thread = new Thread(task);
		thread.start();

		dialog.setOnCloseRequest(WindowEvent::consume);
		dialog.showAndWait();
		dialog.setX((primScreenBounds.getWidth() - dialog.getWidth()) / 2);
		dialog.setY((primScreenBounds.getHeight() - dialog.getHeight()) / 2);
	}

	public static void checkAmount(String amount, String fieldName) throws TradistaBusinessException {

		ParsePosition position = new ParsePosition(0);

		try {
			if (!amount.isEmpty()) {
				MathProperties.getUIDecimalFormat().parse(amount, position);
				if (position.getIndex() != amount.length()) {
					throw new ParseException("failed to parse entire string: " + amount, position.getIndex());
				}
			}
		} catch (ParseException pe) {
			throw new TradistaBusinessException(String.format("The %s is incorrect: %s.%n", fieldName, amount));
		}
	}

	public static BigDecimal parseAmount(String amount, String fieldName) throws TradistaBusinessException {

		ParsePosition position = new ParsePosition(0);
		BigDecimal n = null;
		try {
			if (!amount.isEmpty()) {
				n = (BigDecimal) MathProperties.getUIDecimalFormat().parse(amount, position);
				if (position.getIndex() != amount.length()) {
					throw new ParseException("failed to parse entire string: " + amount, position.getIndex());
				}
			}
		} catch (ParseException pe) {
			throw new TradistaBusinessException(String.format("The %s is incorrect: %s.%n", fieldName, amount));
		}
		return n;
	}

	public static String formatAmount(Object amount) {
		return MathProperties.getUIDecimalFormat().format(amount);
	}

	public static List<String> formatAmounts(List<BigDecimal> amounts) {
		if (amounts == null) {
			return null;
		}
		if (amounts.isEmpty()) {
			return Collections.emptyList();
		}
		return amounts.stream().map(it -> TradistaGUIUtil.formatAmount(it)).collect(Collectors.toList());
	}

	public static List<BigDecimal> parseAmounts(List<String> amounts, String dataName)
			throws TradistaBusinessException {
		if (amounts == null) {
			return null;
		}
		if (amounts.isEmpty()) {
			return Collections.emptyList();
		}
		List<BigDecimal> parsedAmounts = new ArrayList<BigDecimal>(amounts.size());
		for (String amount : amounts) {
			parsedAmounts.add(TradistaGUIUtil.parseAmount(amount, dataName));
		}
		return parsedAmounts;
	}

	public static void export(TableView tv, String fileName, Window window) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet spreadsheet = workbook.createSheet("sample");
		Row row = spreadsheet.createRow(0);
		final FileChooser fileChooser = new FileChooser();
		ExtensionFilter ef = new ExtensionFilter("*.xlsx", "*.xlsx");
		fileChooser.getExtensionFilters().add(ef);
		fileChooser.setInitialFileName(
				fileName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
		File file;
		FileOutputStream fileOutput;

		for (int j = 0; j < tv.getColumns().size(); j++) {
			row.createCell(j).setCellValue(((TableColumn) tv.getColumns().get(j)).getText());
		}

		for (int i = 0; i < tv.getItems().size(); i++) {
			row = spreadsheet.createRow(i + 1);
			for (int j = 0; j < tv.getColumns().size(); j++) {
				if (((TableColumn) tv.getColumns().get(j)).getCellData(i) != null) {
					row.createCell(j).setCellValue(((TableColumn) tv.getColumns().get(j)).getCellData(i).toString());
				} else {
					row.createCell(j).setCellValue(StringUtils.EMPTY);
				}
			}
		}

		try {
			file = fileChooser.showSaveDialog(window);
			if (file != null) {
				fileOutput = new FileOutputStream(file);
				workbook.write(fileOutput);
				fileOutput.close();
			}
		} catch (IOException ioe) {
			// Manage logs
			ioe.printStackTrace();
			throw new TradistaTechnicalException(ioe.getMessage());
		}
	}

}