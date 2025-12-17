package org.eclipse.tradista.core.common.ui.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.InformationBusinessDelegate;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.common.util.TradistaProperties;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

public class MainEntry extends Application {

	private Menu menuProduct;

	private Menu menuReports;

	private Menu menuTrade;

	private Menu menuPricing;

	private Menu menuMarketData;

	private Menu menuPosition;

	private Menu menuConfiguration;

	private MenuItem tradeReport;

	private MenuItem transferReport;

	private MenuItem cashFlowReport;

	private MenuItem legalEntityReport;

	private MenuItem positionReport;

	private MenuItem positionCalculationErrorReport;

	private MenuItem fixingErrorReport;

	private MenuItem productInventoryReport;

	private MenuItem cashInventoryReport;

	private MenuItem dailyPnlReport;

	private MenuItem pricingParameters;

	private MenuItem positionDefinitions;

	private MenuItem legalEntities;

	private MenuItem currencies;

	private MenuItem calendars;

	private MenuItem exchanges;

	private MenuItem books;

	private MenuItem users;

	private MenuItem indexes;

	private MenuItem dateRules;

	private MenuItem fxCurves;

	private MenuItem zeroCouponCurves;

	private MenuItem quotes;

	private MenuItem curves;

	private MenuItem feedConfigs;

	private MenuItem jobs;

	private MenuItem about;

	private MenuItem version;

	private MenuItem uiConfiguration;

	private MenuItem processingOrgDefaults;

	private MenuItem allocationConfiguration;

	private static Pane currentWindow;

	public static Pane getCurrentWindow() {
		return currentWindow;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void initialization() {
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws InterruptedException {

				// Mock tasks
				Thread.sleep(750);
				TradistaGUIUtil.warmUpCache();
				updateProgress(1, 3);
				Thread.sleep(750);
				ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();
				try {
					MathProperties.setUIDecimalFormat(configurationBusinessDelegate
							.getUIConfiguration(ClientUtil.getCurrentUser()).getDecimalFormat());
				} catch (TradistaBusinessException tbe) {
					// Cannot appear here.
				}
				updateProgress(2, 3);
				Thread.sleep(750);
				Properties properties = new Properties();
				InputStream in = getClass().getResourceAsStream("/configuration.properties");
				try {
					properties.load(in);
					in.close();
				} catch (IOException ioe) {
					// should not happen here.
				}
				try {
					TradistaProperties.load(properties);
				} catch (TradistaBusinessException tbe) {
					// should not happen here.
				}
				updateProgress(3, 3);
				Thread.sleep(750);
				return null;
			}
		};
		TradistaGUIUtil.processTaskAndDisplayLoadingDialog(task);
	}

	private void login() {
		Stage stage = new Stage();
		Pane pane = null;
		try {
			pane = FXMLLoader.load(getClass().getResource("/Login.fxml"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		TradistaGUIUtil.setTradistaIcons(stage);
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		stage.setTitle("Login");
		Group root = new Group();
		root.getChildren().add(pane);
		root.getStylesheets().add("/" + new ConfigurationBusinessDelegate().getDefaultStyle() + "Style.css");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		TradistaGUIUtil.resizeComponentHeights(primScreenBounds, stage, 0);
		TradistaGUIUtil.resizeComponentWidths(primScreenBounds, stage, 0);
		stage.sizeToScene();
		stage.initStyle(StageStyle.UTILITY);
		stage.setResizable(false);
		stage.setOnCloseRequest(e -> System.exit(0));
		stage.showAndWait();
		stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

		pane.setPrefHeight(stage.getHeight());
		pane.setPrefWidth(stage.getWidth());

	}

	@Override
	public void start(Stage primaryStage) {

		login();

		// Initialization time
		initialization();

		TradistaGUIUtil.setTradistaIcons(primaryStage);

		primaryStage.setTitle("Main Entry - " + ClientUtil.getCurrentUser().getFirstName() + " "
				+ ClientUtil.getCurrentUser().getSurname() + " - " + ClientUtil.getCurrentUser().getProcessingOrg());

		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setX(0);
		primaryStage.setY(0);

		primaryStage.setWidth(primScreenBounds.getWidth());
		primaryStage.setHeight(75);

		menuProduct = new Menu("Product");
		// Menu reports
		menuReports = new Menu("Reports");

		// --- Menu File
		menuTrade = new Menu("Trades");
		menuPricing = new Menu("Pricing");
		pricingParameters = new MenuItem("Pricing parameters");
		menuPricing.getItems().add(pricingParameters);
		tradeReport = new MenuItem("Trades");
		transferReport = new MenuItem("Transfers");
		cashFlowReport = new MenuItem("Cash Flows");
		legalEntityReport = new MenuItem("Legal Entities");
		positionReport = new MenuItem("Positions");
		positionCalculationErrorReport = new MenuItem("Position Calculation Errors");
		fixingErrorReport = new MenuItem("Fixing Errors");
		productInventoryReport = new MenuItem("Product Inventories");
		cashInventoryReport = new MenuItem("Cash Inventories");
		dailyPnlReport = new MenuItem("Daily Pnls");
		menuMarketData = new Menu("Market Data");
		positionDefinitions = new MenuItem("Position Definitions");
		legalEntities = new MenuItem("Legal Entities");
		dateRules = new MenuItem("Date Rules");
		currencies = new MenuItem("Currencies");
		calendars = new MenuItem("Calendars");
		exchanges = new MenuItem("Exchanges");
		books = new MenuItem("Books");
		users = new MenuItem("Users");
		indexes = new MenuItem("Indexes");
		fxCurves = new MenuItem("FX Curves");
		zeroCouponCurves = new MenuItem("Zero Coupon Curves");
		quotes = new MenuItem("Quotes");
		curves = new MenuItem("Curves");
		feedConfigs = new MenuItem("Feed Configurations");
		jobs = new MenuItem("Jobs Management");
		about = new MenuItem("About");
		version = new MenuItem("Version");

		menuConfiguration = new Menu("Configuration");
		uiConfiguration = new MenuItem("UI Configuration");
		processingOrgDefaults = new MenuItem("Processing Org Defaults");
		allocationConfiguration = new MenuItem("Allocation Configuration");

		setupWindows();

		MenuBar menuBar = new MenuBar();

		menuReports.getItems().addAll(tradeReport, transferReport, cashFlowReport, legalEntityReport, positionReport,
				positionCalculationErrorReport, fixingErrorReport, productInventoryReport, cashInventoryReport,
				dailyPnlReport);

		menuPosition = new Menu("Positions");

		menuPosition.getItems().add(positionDefinitions);

		Menu menuReferential = new Menu("Referential");

		Menu menuMore = new Menu("More..");
		setupAboutMenuItem();
		setupVersionMenuItem();

		menuMore.getItems().addAll(about);
		menuMore.getItems().addAll(version);
		Menu menuBatch = new Menu("Batch");
		menuBatch.getItems().add(jobs);

		menuMarketData.getItems().add(quotes);
		menuMarketData.getItems().add(curves);
		menuMarketData.getItems().add(zeroCouponCurves);
		menuMarketData.getItems().add(fxCurves);
		menuMarketData.getItems().add(feedConfigs);

		menuReferential.getItems().addAll(legalEntities, currencies, calendars, exchanges, books, users, indexes);

		menuConfiguration.getItems().addAll(uiConfiguration, dateRules, processingOrgDefaults);

		menuBar.getMenus().addAll(menuTrade, menuProduct, menuPosition, menuPricing, menuMarketData, menuReports,
				menuReferential, menuConfiguration, menuBatch, menuMore);

		StackPane root = new StackPane();
		root.setAlignment(Pos.TOP_CENTER);
		try {
			root.getStylesheets().add(
					"/" + new ConfigurationBusinessDelegate().getUIConfiguration(ClientUtil.getCurrentUser()).getStyle()
							+ "Style.css");
		} catch (TradistaBusinessException tbe) {
			// Cannot appear here.
		}
		List<Node> nodes = new ArrayList<>();
		nodes.add(menuBar);
		root.getChildren().addAll(nodes);
		primaryStage.setScene(new Scene(root, 1200, 400));

		TradistaGUIUtil.resizeComponentHeights(primScreenBounds, primaryStage, 0);
		TradistaGUIUtil.resizeComponentWidths(primScreenBounds, primaryStage, 0);

		primaryStage.setOnCloseRequest(e -> {
			TradistaAlert alert = new TradistaAlert(AlertType.CONFIRMATION);
			alert.setTitle("Exit Tradista");
			alert.setHeaderText("Exit Tradista");
			alert.setContentText("Do you want to exit Tradista?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				Platform.exit();
			} else {
				e.consume();
			}
		});
		primaryStage.show();

	}

	/**
	 * This method defines how windows must be started when a menu item is clicked.
	 * The scope of products is based on product.properties.
	 */
	private void setupWindows() {
		Set<String> products = new ProductBusinessDelegate().getAvailableProductTypes();

		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

		setupMenuItem(legalEntities, "Legal Entities", "LegalEntities", primScreenBounds);
		setupMenuItem(currencies, "Currencies", "Currencies", primScreenBounds);
		setupMenuItem(calendars, "Calendars", "Calendars", primScreenBounds);
		setupMenuItem(exchanges, "Exchanges", "Exchanges", primScreenBounds);
		setupMenuItem(books, "Books", "Books", primScreenBounds);
		setupMenuItem(users, "Users", "Users", primScreenBounds);
		setupMenuItem(indexes, "Indexes", "Indexes", primScreenBounds);
		setupMenuItem(dateRules, "Date Rules", "DateRules", primScreenBounds);
		setupMenuItem(fxCurves, "FX Curves", "FXCurves", primScreenBounds);
		setupMenuItem(zeroCouponCurves, "Zero Coupon Curves", "ZeroCouponCurves", primScreenBounds);
		setupMenuItem(quotes, "Quotes", "Quotes", primScreenBounds);
		setupMenuItem(curves, "Curves", "Curves", primScreenBounds);
		setupMenuItem(feedConfigs, "Feed Configurations", "FeedConfig", primScreenBounds);
		setupMenuItem(tradeReport, "Trade Report", "TradeReport", primScreenBounds);
		setupMenuItem(transferReport, "Transfer Report", "TransferReport", primScreenBounds);
		setupMenuItem(cashFlowReport, "Cash Flow Report", "CashFlowReport", primScreenBounds);
		setupMenuItem(legalEntityReport, "Legal Entity Report", "LegalEntityReport", primScreenBounds);
		setupMenuItem(positionReport, "Position Report", "PositionReport", primScreenBounds);
		setupMenuItem(positionCalculationErrorReport, "Position Calculation Error Report",
				"PositionCalculationErrorReport", primScreenBounds);
		setupMenuItem(fixingErrorReport, "Fixing Error Report", "FixingErrorReport", primScreenBounds);
		setupMenuItem(productInventoryReport, "Product Inventory Report", "ProductInventoryReport", primScreenBounds);
		setupMenuItem(cashInventoryReport, "Cash Inventory Report", "CashInventoryReport", primScreenBounds);
		setupMenuItem(dailyPnlReport, "Daily Pnl Report", "DailyPnlReport", primScreenBounds);
		setupMenuItem(pricingParameters, "Pricing Parameters Set", "PricingParameters", primScreenBounds);
		setupMenuItem(positionDefinitions, "Position Definitions", "PositionDefinitions", primScreenBounds);
		setupMenuItem(jobs, "Jobs", "Jobs", primScreenBounds);
		setupMenuItem(uiConfiguration, "UI Configuration", "UIConfiguration", primScreenBounds);
		setupMenuItem(processingOrgDefaults, "Processing Org Defaults", "ProcessingOrgDefaults", primScreenBounds);

		for (String product : products) {
			// How to manage exceptions like FX trades ?
			if (!product.equals("FX") && !product.equals("FXSwap") && !product.equals("FXOption")
					&& !product.equals("FXNDF") && !product.equals("IRSwap") && !product.equals("CcySwap")
					&& !product.equals("IRSwapOption") && !product.equals("LoanDeposit")
					&& !product.equals("IRCapFloorCollar") && !product.equals("FRA") && !product.equals("Future")
					&& !product.equals("SpecificRepo") && !product.equals("GCRepo")) {
				MenuItem productMenuItem = new MenuItem(product);
				menuProduct.getItems().add(productMenuItem);
				setupMenuItem(productMenuItem, product, product + "Product", primScreenBounds);

				MenuItem report = new MenuItem(product);
				menuReports.getItems().add(report);
				setupMenuItem(report, product + " Report", product + "Report", primScreenBounds);
			}

			if (product.equals("GCRepo")) {
				MenuItem gcBasket = new MenuItem("GCBasket");
				menuProduct.getItems().add(gcBasket);
				setupMenuItem(gcBasket, product, product + "Product", primScreenBounds);
			}

			if ((product.equals("GCRepo")) || (product.equals("SpecificRepo"))) {
				if (!menuConfiguration.getItems().contains(allocationConfiguration)) {
					menuConfiguration.getItems().add(allocationConfiguration);
					setupMenuItem(allocationConfiguration, "Allocation Configuration", "AllocationConfiguration",
							primScreenBounds);
				}
			}

			if (product.equals("FX") || product.equals("EquityOption") || product.equals("IRSwapOption")) {
				MenuItem surfaceGeneration = new MenuItem(product + " Volatility Surfaces Generation");
				menuMarketData.getItems().add(surfaceGeneration);
				setupMenuItem(surfaceGeneration, product + " Volatility Surface Generator",
						product + "VolatilitySurfacesGenerator", primScreenBounds);
			}

			if (product.equals("Future") || product.equals("EquityOption")) {
				MenuItem contractSpecification = new MenuItem(product + " Contract Specification");
				menuProduct.getItems().add(contractSpecification);
				setupMenuItem(contractSpecification, product + " Contract Specification",
						product + "ContractSpecification", primScreenBounds);
			}

			MenuItem trade = new MenuItem(product);
			menuTrade.getItems().add(trade);

			setupMenuItem(trade, product + " Trade", product + "Trade", primScreenBounds);
		}
	}

	private void setupMenuItem(MenuItem menuItem, String title, String templateName, Rectangle2D primScreenBounds) {
		menuItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				// Repo trade, Allocation Configuration, GC Baskets, Processing Org Defaults
				// windows are web based.
				if (templateName.equals("GCRepoTrade") || templateName.equals("SpecificRepoTrade")
						|| templateName.equals("ProcessingOrgDefaults")
						|| templateName.equals("AllocationConfiguration")) {
					TradistaGUIUtil.browse(templateName.toLowerCase());
				} else if (templateName.equals("GCRepoProduct")) {
					TradistaGUIUtil.browse("gcbasket");
				} else {
					Pane pane = null;
					try {
						pane = FXMLLoader.load(getClass().getResource("/" + templateName + ".fxml"));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					ScrollPane sPane = new ScrollPane(pane);
					sPane.setVbarPolicy(ScrollBarPolicy.NEVER);
					sPane.setHbarPolicy(ScrollBarPolicy.NEVER);
					sPane.setPannable(true);

					Stage stage = new Stage();
					TradistaGUIUtil.setTradistaIcons(stage);
					stage.setTitle(title);
					Group root = new Group();
					root.getChildren().add(sPane);
					try {
						root.getStylesheets().add("/" + new ConfigurationBusinessDelegate()
								.getUIConfiguration(ClientUtil.getCurrentUser()).getStyle() + "Style.css");
					} catch (TradistaBusinessException tbe) {
						// Cannot appear here.
					}
					Scene scene = new Scene(root);
					stage.setScene(scene);
					TradistaGUIUtil.resizeComponentHeights(primScreenBounds, stage, 0);
					TradistaGUIUtil.resizeComponentWidths(primScreenBounds, stage, 0);

					stage.sizeToScene();

					stage.setResizable(false);
					stage.show();
					stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
					stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

					sPane.setPrefHeight(stage.getHeight());
					sPane.setPrefWidth(stage.getWidth());
				}
			}
		});
	}

	private void setupAboutMenuItem() {
		about.setOnAction(e -> {
			GridPane pane = new GridPane();
			pane.getStyleClass().add("root");
			Image image = new Image("tradista-logo.png");
			ImageView iv = new ImageView(image);
			iv.setFitHeight(300);
			iv.setFitWidth(300);
			iv.setPreserveRatio(true);
			iv.setSmooth(true);
			iv.setCache(true);
			Hyperlink websiteLink = new Hyperlink("www.tradista.finance");
			websiteLink.setOnAction(le -> getHostServices().showDocument(websiteLink.getText()));
			pane.add(iv, 0, 0, 2, 1);
			GridPane.setHalignment(iv, HPos.CENTER);
			pane.add(new Label("Web Site"), 0, 1);
			pane.add(websiteLink, 1, 1);
			pane.add(new Label("E-Mail"), 0, 2);
			Hyperlink mailLink = new Hyperlink("contact@tradista.finance");
			mailLink.setOnAction(me -> getHostServices().showDocument("mailto:" + mailLink.getText()));
			pane.setStyle("-fx-padding: 10; -fx-hgap: 10; -fx-vgap: 10;");
			pane.add(mailLink, 1, 2);

			Stage stage = new Stage();
			TradistaGUIUtil.setTradistaIcons(stage);
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			stage.setTitle("About");
			Group root = new Group();
			root.getChildren().add(pane);
			try {
				root.getStylesheets().add("/"
						+ new ConfigurationBusinessDelegate().getUIConfiguration(ClientUtil.getCurrentUser()).getStyle()
						+ "Style.css");
			} catch (TradistaBusinessException tbe) {
				// Should not appear here.
			}
			Scene scene = new Scene(root);

			stage.setScene(scene);

			TradistaGUIUtil.resizeComponentHeights(primScreenBounds, stage, 0);
			TradistaGUIUtil.resizeComponentWidths(primScreenBounds, stage, 0);

			stage.sizeToScene();
			stage.setResizable(false);
			stage.show();
			stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - stage.getWidth()) / 2);
			stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - stage.getHeight()) / 2);

		});
	}

	private void setupVersionMenuItem() {
		version.setOnAction(e -> {
			GridPane pane = new GridPane();
			pane.setStyle("-fx-padding: 10; -fx-hgap: 10; -fx-vgap: 10;");
			pane.getStyleClass().add("root");
			Map<String, String> modules = new InformationBusinessDelegate().getModules();
			if (modules != null && !modules.isEmpty()) {
				int i = 0;
				Label moduleLabel = new Label("Module");
				moduleLabel.setStyle("-fx-font-weight: bold;");
				pane.add(moduleLabel, 0, i);
				Label versionLabel = new Label("Version");
				versionLabel.setStyle("-fx-font-weight: bold;");
				pane.add(versionLabel, 1, i);
				i++;
				for (Map.Entry<String, String> entry : modules.entrySet()) {
					pane.add(new Label(entry.getKey()), 0, i, 1, 1);
					pane.add(new Label(entry.getValue()), 1, i, 1, 1);
					i++;
				}
			}
			Stage stage = new Stage();
			TradistaGUIUtil.setTradistaIcons(stage);
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			stage.setTitle("Version");
			Group root = new Group();
			root.getChildren().add(pane);
			try {
				root.getStylesheets().add("/"
						+ new ConfigurationBusinessDelegate().getUIConfiguration(ClientUtil.getCurrentUser()).getStyle()
						+ "Style.css");
			} catch (TradistaBusinessException tbe) {
				// Cannot appear here.
			}
			Scene scene = new Scene(root);

			stage.setScene(scene);

			TradistaGUIUtil.resizeComponentHeights(primScreenBounds, stage, 0);
			TradistaGUIUtil.resizeComponentWidths(primScreenBounds, stage, 0);

			stage.sizeToScene();

			stage.setResizable(false);
			stage.show();
			stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - stage.getWidth()) / 2);
			stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - stage.getHeight()) / 2);
		});
	}

}