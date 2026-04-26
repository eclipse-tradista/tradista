package org.eclipse.tradista.core.common.ui.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.common.util.TradistaProperties;
import org.eclipse.tradista.core.common.util.TradistaUtil;
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
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;
import org.eclipse.tradista.core.marketdata.service.CurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.FXCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.FeedBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
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
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.springframework.lang.NonNull;

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
import javafx.scene.control.ListCell;
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
import javafx.util.Callback;

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

public final class TradistaGUIUtil {

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

	private static FeedBusinessDelegate feedBusinessDelegate = new FeedBusinessDelegate();

	private static InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();

	private static FXCurveBusinessDelegate fxCurveBusinessDelegate = new FXCurveBusinessDelegate();

	private static UserBusinessDelegate userBusinessDelegate = new UserBusinessDelegate();

	private TradistaGUIUtil() {
	}

	public static void resizeComponents(Window window) {
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
		fontSize = Math.clamp(fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
		window.getProperties().put(FONT_SIZE, fontSize);
		if (window.getProperties().containsKey(COMPONENTS_WIDTH)) {
			componentsWidth = (double) window.getProperties().get(COMPONENTS_WIDTH);
			if (oldWidth.doubleValue() != 0) {
				componentsWidth = componentsWidth * (screen.getWidth() / oldWidth.doubleValue());
			}
		} else {
			componentsWidth = screen.getWidth() / GOLDEN_RATIO_WIDTH;
		}
		componentsWidth = Math.clamp(componentsWidth, MIN_COMPONENT_WIDTH, MAX_COMPONENT_WIDTH);
		window.getProperties().put(COMPONENTS_WIDTH, componentsWidth);
		List<Node> nodes = getAllNodesInWindow(window);
		if (nodes != null && !nodes.isEmpty()) {
			String style = "-fx-font-size: " + fontSize + "px;";
			for (Node n : nodes) {
				if (n instanceof Labeled labeled) {
					labeled.setStyle(style);
				}
				if (n instanceof ComboBox<?> comboBox) {
					comboBox.setPrefWidth(componentsWidth);
					comboBox.setStyle(style);
				}
				if (n instanceof TextField textField) {
					textField.setFont(Font.font(fontSize));
					textField.setPrefWidth(componentsWidth);
				}
				if (n instanceof TextArea textArea) {
					textArea.setFont(Font.font(fontSize));
					textArea.setPrefWidth(componentsWidth * 2);
				}
				if (n instanceof ListView<?> listView) {
					listView.setPrefWidth(componentsWidth);
				}
				if (n instanceof DatePicker datePicker) {
					datePicker.setPrefWidth(componentsWidth);
					datePicker.setStyle(style);
				}
				if (n instanceof CheckBox checkBox) {
					checkBox.setFont(Font.font(fontSize));
				}
				if (n instanceof Button button) {
					button.setStyle(style);
				}
				if (n instanceof TableView<?> tableView) {
					for (TableColumn<?, ?> tc : tableView.getColumns()) {
						tc.setPrefWidth(componentsWidth);
						tc.setStyle(style);
					}
				}
				if (n instanceof TabPane tabPane) {
					for (Tab tab : tabPane.getTabs()) {
						tab.setStyle(style);
					}
				}
				if (n instanceof PieChart pieChart) {
					pieChart.setPrefWidth(componentsWidth * 3);
				}
				if (n instanceof MenuBar menuBar) {
					menuBar.setStyle(style);
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
			errMsg.append("Style is mandatory.");
		}
		if (!errMsg.isEmpty()) {
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
			errMsg.append("Style is mandatory.");
		}
		if (!errMsg.isEmpty()) {
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
		fontSize = Math.clamp(fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
		window.getProperties().put(FONT_SIZE, fontSize);
		if (window.getProperties().containsKey(COMPONENTS_HEIGHT)) {
			componentsHeight = (double) window.getProperties().get(COMPONENTS_HEIGHT);
			if (oldHeight.doubleValue() != 0) {
				componentsHeight = componentsHeight * (screen.getHeight() / oldHeight.doubleValue());
			}
		} else {
			componentsHeight = screen.getHeight() / GOLDEN_RATIO_HEIGHT;
		}
		componentsHeight = Math.clamp(componentsHeight, MIN_COMPONENT_HEIGHT, MAX_COMPONENT_HEIGHT);
		window.getProperties().put(COMPONENTS_HEIGHT, componentsHeight);
		List<Node> nodes = getAllNodesInWindow(window);
		if (nodes != null && !nodes.isEmpty()) {
			String style = "-fx-font-size: " + fontSize + "px;";
			for (Node n : nodes) {
				if (n instanceof Labeled labeled) {
					labeled.setStyle(style);
				}
				if (n instanceof ComboBox<?> comboBox) {
					comboBox.setPrefHeight(componentsHeight);
					comboBox.setStyle(style);
				}
				if (n instanceof TextField textField) {
					textField.setPrefHeight(componentsHeight);
					textField.setFont(Font.font(fontSize));
				}
				if (n instanceof TextArea textArea) {
					textArea.setFont(Font.font(fontSize));
					textArea.setPrefHeight(componentsHeight * 2);
				}
				if (n instanceof DatePicker datePicker) {
					datePicker.setPrefHeight(componentsHeight);
					datePicker.setStyle(style);
				}
				if (n instanceof ListView<?> listView) {
					listView.setPrefHeight(componentsHeight * 5);
				}
				if (n instanceof CheckBox checkBox) {
					checkBox.setFont(Font.font(fontSize));
				}
				if (n instanceof Button button) {
					button.setStyle(style);
				}
				if (n instanceof TableView<?> tableView) {
					tableView.setPrefHeight(screen.getHeight() / 4);
					for (TableColumn<?, ?> tc : tableView.getColumns()) {
						tc.setStyle(style);
					}
				}
				if (n instanceof TabPane tabPane) {
					for (Tab tab : tabPane.getTabs()) {
						tab.setStyle(style);
					}
				}
				if (n instanceof PieChart pieChart) {
					pieChart.setPrefHeight(screen.getHeight() / 3);
				}
				if (n instanceof MenuBar menuBar) {
					menuBar.setStyle(style);
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
		if (parent instanceof ScrollPane scrollPane) {
			children = FXCollections.observableArrayList();
			children.add(scrollPane.getContent());
		} else {
			children = parent.getChildrenUnmodifiable();
		}
		for (Node child : children) {
			ret.add(child);
			if (child instanceof TabPane tabPane) {
				ObservableList<Tab> tabs = tabPane.getTabs();
				if (tabs != null && !tabs.isEmpty()) {
					for (Tab tab : tabs) {
						ret.addAll(getAllNodesInParent((Parent) tab.getContent()));
					}
				}
			} else if (child instanceof ScrollPane scrollPane) {
				ret.addAll(getAllNodesInParent((Parent) scrollPane.getContent()));
			}

			else if (child instanceof Parent p) {
				ret.addAll(getAllNodesInParent(p));
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
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
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
		List<String> statusStrings = data.stream().map(Object::toString).toList();
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
		List<String> days = new ArrayList<>();
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
		Set<PositionDefinition> posDefs = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				posDefs = positionDefinitionBusinessDelegate
						.getPositionDefinitionsByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			posDefs = positionDefinitionBusinessDelegate.getAllPositionDefinitions();
		}

		ObservableList<PositionDefinition> data = null;
		if (posDefs != null && !posDefs.isEmpty()) {
			data = FXCollections.observableArrayList(posDefs);
		} else {
			data = FXCollections.observableArrayList();
		}

		if (addBlank) {
			data.add(0, BlankPositionDefinition.getInstance());
		}

		Callback<ListView<PositionDefinition>, ListCell<PositionDefinition>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(PositionDefinition posDef, boolean empty) {
				super.updateItem(posDef, empty);
				if (empty || posDef == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					if (posDef instanceof BlankPositionDefinition) {
						setText(posDef.toString());
					} else {
						setText(posDef.getName() + " [" + posDef.getProcessingOrg().getShortName() + "]");
					}
				} else {
					setText(posDef.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<PositionDefinition> cb : comboBoxes) {
				PositionDefinition element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillFeedConfigComboBox(ComboBox<FeedConfig>... comboBoxes) {
		fillFeedConfigComboBox(false, comboBoxes);
	}

	@SafeVarargs
	public static void fillFeedConfigComboBox(boolean addBlank, ComboBox<FeedConfig>... comboBoxes) {
		Set<FeedConfig> feedConfigs = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				feedConfigs = feedBusinessDelegate.getFeedConfigsByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			feedConfigs = feedBusinessDelegate.getAllFeedConfigs();
		}

		ObservableList<FeedConfig> data = null;
		if (feedConfigs != null && !feedConfigs.isEmpty()) {
			data = FXCollections.observableArrayList(feedConfigs);
		} else {
			data = FXCollections.observableArrayList();
		}

		if (addBlank) {
			data.add(0, null);
		}

		// cellFactory: controls how items are rendered in the open dropdown list
		Callback<ListView<FeedConfig>, ListCell<FeedConfig>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(FeedConfig fc, boolean empty) {
				super.updateItem(fc, empty);
				if (empty || fc == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = fc.getProcessingOrg() == null ? "Global" : fc.getProcessingOrg().getShortName();
					setText(fc.getName() + " [" + poSuffix + "]");
				} else {
					setText(fc.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<FeedConfig> cb : comboBoxes) {
				FeedConfig element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillPricingParameterComboBox(ComboBox<PricingParameter>... comboBoxes) {
		fillPricingParameterComboBox(false, comboBoxes);
	}

	/**
	 * Fills a Quote Set ComboBox. Disambiguates names for global administrators.
	 * 
	 * @param comboBox the ComboBox to fill.
	 */
	public static void fillQuoteSetComboBox(ComboBox<QuoteSet> comboBox) {
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		Set<QuoteSet> quoteSets = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				quoteSets = quoteBusinessDelegate.getQuoteSetsByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			quoteSets = quoteBusinessDelegate.getAllQuoteSets();
		}
		quoteSets = (quoteSets == null) ? Collections.emptySet() : quoteSets;

		List<QuoteSet> quoteSetsList = new ArrayList<>(quoteSets);
		Collections.sort(quoteSetsList);

		// cellFactory: controls how items are rendered in the open dropdown list
		Callback<ListView<QuoteSet>, ListCell<QuoteSet>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(QuoteSet qs, boolean empty) {
				super.updateItem(qs, empty);
				if (empty || qs == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = qs.getProcessingOrg() == null ? "Global" : qs.getProcessingOrg().getShortName();
					setText(qs.getName() + " [" + poSuffix + "]");
				} else {
					setText(qs.getName());
				}
			}
		};

		comboBox.setCellFactory(cellFactory);
		comboBox.setButtonCell(cellFactory.call(null));

		TradistaGUIUtil.fillComboBox(quoteSetsList, comboBox);
	}

	/**
	 * Fills a Quote Set ComboBox where items are identified by their names.
	 * Disambiguates names for global administrators.
	 * 
	 * @param comboBox the ComboBox to fill.
	 */
	public static void fillQuoteSetComboBox(QuoteSet currentQuoteSet, ComboBox<String> comboBox) {
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		Set<QuoteSet> quoteSets = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				quoteSets = quoteBusinessDelegate.getQuoteSetsByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			quoteSets = quoteBusinessDelegate.getAllQuoteSets();
		}
		quoteSets = (quoteSets == null) ? Collections.emptySet() : quoteSets;

		List<QuoteSet> quoteSetsList = new ArrayList<>(quoteSets);
		Collections.sort(quoteSetsList);

		List<String> names = quoteSetsList.stream().map(QuoteSet::getName).distinct().toList();

		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
			comboBox.setCellFactory(_ -> new ListCell<String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (item != null && !empty) {
						List<QuoteSet> matches = quoteSetsList.stream().filter(qs -> qs.getName().equals(item))
								.toList();
						if (matches.size() > 1) {
							// If there are multiple quote sets with the same name, we can't easily
							// disambiguate in a String-only combobox without changing the values.
							// However, we can show the PO in the list cell.
							// Note: selecting one will only set the String name as the value.
							setText(item + " (Multiple POs)");
						} else if (!matches.isEmpty()) {
							QuoteSet qs = matches.get(0);
							String poSuffix = qs.getProcessingOrg() == null ? "Global"
									: qs.getProcessingOrg().getShortName();
							setText(item + " [" + poSuffix + "]");
						} else {
							setText(item);
						}
					} else {
						setText(null);
					}
				}
			});
			// Note: Button cell is not set here because it would be confusing if the value
			// is just a String.
		}

		TradistaGUIUtil.fillComboBox(names, comboBox);

		if (currentQuoteSet != null) {
			comboBox.setValue(currentQuoteSet.getName());
		}
	}

	@SafeVarargs
	public static void fillPricingParameterComboBox(boolean addBlank, ComboBox<PricingParameter>... comboBoxes) {
		Set<PricingParameter> pps = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				pps = pricerBusinessDelegate.getPricingParametersByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			pps = pricerBusinessDelegate.getAllPricingParameters();
		}

		ObservableList<PricingParameter> data = null;
		if (pps != null && !pps.isEmpty()) {
			data = FXCollections.observableArrayList(pps);
		} else {
			data = FXCollections.observableArrayList();
		}

		// cellFactory: controls how items are rendered in the open dropdown list
		Callback<ListView<PricingParameter>, ListCell<PricingParameter>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(PricingParameter pp, boolean empty) {
				super.updateItem(pp, empty);
				if (empty || pp == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = pp.getProcessingOrg() == null ? "Global" : pp.getProcessingOrg().getShortName();
					setText(pp.getName() + " [" + poSuffix + "]");
				} else {
					setText(pp.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<PricingParameter> cb : comboBoxes) {
				PricingParameter element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
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
	public static void fillInterestRateCurveComboBox(ComboBox<InterestRateCurve>... comboBoxes) {
		Set<InterestRateCurve> curves = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			curves = interestRateCurveBusinessDelegate
					.getInterestRateCurvesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
		} else {
			curves = interestRateCurveBusinessDelegate.getAllInterestRateCurves();
		}
		ObservableList<InterestRateCurve> data = null;
		if (curves != null && !curves.isEmpty()) {
			data = FXCollections.observableArrayList(curves);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<InterestRateCurve>, ListCell<InterestRateCurve>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(InterestRateCurve curve, boolean empty) {
				super.updateItem(curve, empty);
				if (empty || curve == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = curve.getProcessingOrg() == null ? "Global"
							: curve.getProcessingOrg().getShortName();
					setText(curve.getName() + " [" + poSuffix + "]");
				} else {
					setText(curve.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<InterestRateCurve> cb : comboBoxes) {
				InterestRateCurve element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillZeroCouponCurveComboBox(ComboBox<ZeroCouponCurve>... comboBoxes) {
		Set<ZeroCouponCurve> curves = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			curves = interestRateCurveBusinessDelegate
					.getZeroCouponCurvesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
		} else {
			curves = interestRateCurveBusinessDelegate.getAllZeroCouponCurves();
		}
		ObservableList<ZeroCouponCurve> data = null;
		if (curves != null && !curves.isEmpty()) {
			data = FXCollections.observableArrayList(curves);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<ZeroCouponCurve>, ListCell<ZeroCouponCurve>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(ZeroCouponCurve curve, boolean empty) {
				super.updateItem(curve, empty);
				if (empty || curve == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = curve.getProcessingOrg() == null ? "Global"
							: curve.getProcessingOrg().getShortName();
					setText(curve.getName() + " [" + poSuffix + "]");
				} else {
					setText(curve.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<ZeroCouponCurve> cb : comboBoxes) {
				ZeroCouponCurve element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillFXCurveComboBox(ComboBox<FXCurve>... comboBoxes) {
		Set<FXCurve> curves = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			curves = fxCurveBusinessDelegate.getFXCurvesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
		} else {
			curves = fxCurveBusinessDelegate.getAllFXCurves();
		}
		ObservableList<FXCurve> data = null;
		if (curves != null && !curves.isEmpty()) {
			data = FXCollections.observableArrayList(curves);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<FXCurve>, ListCell<FXCurve>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(FXCurve curve, boolean empty) {
				super.updateItem(curve, empty);
				if (empty || curve == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = curve.getProcessingOrg() == null ? "Global"
							: curve.getProcessingOrg().getShortName();
					setText(curve.getName() + " [" + poSuffix + "]");
				} else {
					setText(curve.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<FXCurve> cb : comboBoxes) {
				FXCurve element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillCurveComboBox(ComboBox<Curve<? extends LocalDate, ? extends BigDecimal>>... comboBoxes) {
		Set<Curve<? extends LocalDate, ? extends BigDecimal>> curves = null;
		CurveBusinessDelegate curveBusinessDelegate = new CurveBusinessDelegate();
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			curves = curveBusinessDelegate.getCurvesByPoId(ClientUtil.getCurrentProcessingOrg().getId());
		} else {
			curves = curveBusinessDelegate.getAllCurves();
		}
		ObservableList<Curve<? extends LocalDate, ? extends BigDecimal>> data = null;
		if (curves != null && !curves.isEmpty()) {
			data = FXCollections.observableArrayList(curves);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<Curve<? extends LocalDate, ? extends BigDecimal>>, ListCell<Curve<? extends LocalDate, ? extends BigDecimal>>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(Curve<? extends LocalDate, ? extends BigDecimal> curve, boolean empty) {
				super.updateItem(curve, empty);
				if (empty || curve == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poSuffix = curve.getProcessingOrg() == null ? "Global"
							: curve.getProcessingOrg().getShortName();
					setText(curve.getName() + " [" + poSuffix + "]");
				} else {
					setText(curve.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<Curve<? extends LocalDate, ? extends BigDecimal>> cb : comboBoxes) {
				Curve<? extends LocalDate, ? extends BigDecimal> element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
				cb.setItems(data);
				if (element != null && data.contains(element)) {
					cb.getSelectionModel().select(data.get(data.indexOf(element)));
				} else {
					cb.getSelectionModel().selectFirst();
				}
			}
		}
	}

	@SafeVarargs
	public static void fillBookComboBox(ComboBox<Book>... comboBoxes) {
		Set<Book> books = null;
		if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
			try {
				books = bookBusinessDelegate.getBooksByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
		} else {
			books = bookBusinessDelegate.getAllBooks();
		}

		ObservableList<Book> data = null;
		if (books != null && !books.isEmpty()) {
			data = FXCollections.observableArrayList(books);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<Book>, ListCell<Book>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(Book book, boolean empty) {
				super.updateItem(book, empty);
				if (empty || book == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					setText(book.getName() + " [" + book.getProcessingOrg().getShortName() + "]");
				} else {
					setText(book.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<Book> cb : comboBoxes) {
				Book element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
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
	public static void fillUserComboBox(ComboBox<User>... comboBoxes) {
		Set<User> users = userBusinessDelegate.getAllUsers();
		ObservableList<User> data = null;
		if (users != null && !users.isEmpty()) {
			data = FXCollections.observableArrayList(users);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<User>, ListCell<User>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(User user, boolean empty) {
				super.updateItem(user, empty);
				if (empty || user == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poName = user.getProcessingOrg() != null
							? " [" + user.getProcessingOrg().getShortName() + "]"
							: " [Admin]";
					setText(user.getSurname() + poName);
				} else {
					setText(user.getSurname());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<User> cb : comboBoxes) {
				User element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
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
	public static void fillJobInstanceComboBox(ComboBox<TradistaJobInstance>... comboBoxes) {
		Set<TradistaJobInstance> jobInstances = null;
		BatchBusinessDelegate batchBusinessDelegate = new BatchBusinessDelegate();
		try {
			if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() != null) {
				jobInstances = batchBusinessDelegate
						.getAllJobInstances(ClientUtil.getCurrentProcessingOrg().getShortName());
			} else {
				jobInstances = batchBusinessDelegate.getAllJobInstances(null);
			}
		} catch (TradistaBusinessException _) {
			// Not expected here
		}

		ObservableList<TradistaJobInstance> data = null;
		if (jobInstances != null && !jobInstances.isEmpty()) {
			data = FXCollections.observableArrayList(jobInstances);
		} else {
			data = FXCollections.observableArrayList();
		}

		Callback<ListView<TradistaJobInstance>, ListCell<TradistaJobInstance>> cellFactory = _ -> new ListCell<>() {
			@Override
			protected void updateItem(TradistaJobInstance job, boolean empty) {
				super.updateItem(job, empty);
				if (empty || job == null) {
					setText(null);
				} else if (ClientUtil.currentUserIsAdmin() && ClientUtil.getCurrentProcessingOrg() == null) {
					String poName = job.getProcessingOrg() != null ? " [" + job.getProcessingOrg().getShortName() + "]"
							: " [Global]";
					setText(job.getName() + poName);
				} else {
					setText(job.getName());
				}
			}
		};

		if (comboBoxes.length > 0) {
			for (ComboBox<TradistaJobInstance> cb : comboBoxes) {
				TradistaJobInstance element = cb.getValue();
				cb.setCellFactory(cellFactory);
				cb.setButtonCell(cellFactory.call(null));
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
		List<VolatilitySurface<?, ?, ?>> surfaces = null;
		try {
			surfaces = surfaceBusinessDelegate.getSurfaces(surfaceType);
		} catch (TradistaBusinessException _) {
			// Not expected here
		}
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
		} catch (TradistaBusinessException _) {
			// Not expected here.
		}
		pane.getStyleClass().add("root");
		TradistaGUIUtil.setTradistaIcons(dialog);
		pBar.progressProperty().bind(task.progressProperty());
		pBar.minWidthProperty().bind(scene.widthProperty());
		task.setOnSucceeded(_ -> dialog.close());
		task.setOnFailed(_ -> task.getException().printStackTrace());

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
		} catch (ParseException _) {
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
		} catch (ParseException _) {
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
		return amounts.stream().map(TradistaGUIUtil::formatAmount).toList();
	}

	public static List<BigDecimal> parseAmounts(List<String> amounts, String dataName)
			throws TradistaBusinessException {
		if (amounts == null) {
			return null;
		}
		if (amounts.isEmpty()) {
			return Collections.emptyList();
		}
		List<BigDecimal> parsedAmounts = new ArrayList<>(amounts.size());
		for (String amount : amounts) {
			parsedAmounts.add(TradistaGUIUtil.parseAmount(amount, dataName));
		}
		return parsedAmounts;
	}

	public static void export(TableView<?> tv, String fileName, Window window) {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet spreadsheet = workbook.createSheet("sample");
			Row row = spreadsheet.createRow(0);
			final FileChooser fileChooser = new FileChooser();
			ExtensionFilter ef = new ExtensionFilter("*.xlsx", "*.xlsx");
			fileChooser.getExtensionFilters().add(ef);
			fileChooser.setInitialFileName(
					fileName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
			File file;

			for (int j = 0; j < tv.getColumns().size(); j++) {
				row.createCell(j).setCellValue(((TableColumn<?, ?>) tv.getColumns().get(j)).getText());
			}

			for (int i = 0; i < tv.getItems().size(); i++) {
				row = spreadsheet.createRow(i + 1);
				for (int j = 0; j < tv.getColumns().size(); j++) {
					if (((TableColumn<?, ?>) tv.getColumns().get(j)).getCellData(i) != null) {
						row.createCell(j)
								.setCellValue(((TableColumn<?, ?>) tv.getColumns().get(j)).getCellData(i).toString());
					} else {
						row.createCell(j).setCellValue(StringUtils.EMPTY);
					}
				}
			}

			file = fileChooser.showSaveDialog(window);
			if (file != null) {
				try (FileOutputStream fileOutput = new FileOutputStream(file)) {
					workbook.write(fileOutput);
				}
			}

		} catch (IOException ioe) {
			// Manage logs
			ioe.printStackTrace();
			throw new TradistaTechnicalException(ioe.getMessage());
		}
	}

	/**
	 * Warm-up cache on client side. Add here what is time consuming. To be used
	 * when Tradista client is started.
	 * 
	 * @see MainEntry
	 */
	public static void warmUpCache() {
		CompletableFuture.supplyAsync(TradistaUtil::getAllErrorTypes);
	}

	public static void browse(String page) {
		new Thread(() -> {
			try {
				URI uri = URI.create(
						TradistaProperties.getTradistaAppProtocol() + "://" + TradistaProperties.getTradistaAppServer()
								+ ":" + TradistaProperties.getTradistaAppPort() + "/web/pages/" + page + ".xhtml");

				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					Desktop.getDesktop().browse(uri);
					return;
				}

				String os = System.getProperty("os.name").toLowerCase();
				String[] command;

				if (os.contains("win")) {
					command = new String[] { "rundll32", "url.dll,FileProtocolHandler", uri.toString() };
				} else if (os.contains("mac")) {
					command = new String[] { "open", uri.toString() };
				} else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
					command = new String[] { "xdg-open", uri.toString() };
				} else {
					// Unsupported OS: cannot open URL
					return;
				}

				Process process = Runtime.getRuntime().exec(command);
				process.getInputStream().close();
				process.getErrorStream().close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
}