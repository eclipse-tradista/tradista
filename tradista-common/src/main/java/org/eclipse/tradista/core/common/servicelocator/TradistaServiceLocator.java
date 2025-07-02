package org.eclipse.tradista.core.common.servicelocator;

import static org.eclipse.tradista.core.common.util.TradistaConstants.CORE_PACKAGE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.tradista.ai.agent.service.AssetManagerAgentService;
import org.eclipse.tradista.ai.agent.service.MandateService;
import org.eclipse.tradista.ai.reasoning.common.service.FormulaService;
import org.eclipse.tradista.ai.reasoning.fol.service.FolFormulaService;
import org.eclipse.tradista.core.batch.service.BatchService;
import org.eclipse.tradista.core.book.service.BookService;
import org.eclipse.tradista.core.calendar.service.CalendarService;
import org.eclipse.tradista.core.cashinventory.service.CashInventoryService;
import org.eclipse.tradista.core.common.service.InformationService;
import org.eclipse.tradista.core.configuration.service.ConfigurationService;
import org.eclipse.tradista.core.currency.service.CurrencyService;
import org.eclipse.tradista.core.dailypnl.service.DailyPnlService;
import org.eclipse.tradista.core.daterule.service.DateRuleService;
import org.eclipse.tradista.core.daycountconvention.service.DayCountConventionService;
import org.eclipse.tradista.core.error.service.ErrorService;
import org.eclipse.tradista.core.exchange.service.ExchangeService;
import org.eclipse.tradista.core.index.service.IndexService;
import org.eclipse.tradista.core.legalentity.service.LegalEntityService;
import org.eclipse.tradista.core.marketdata.service.CurveService;
import org.eclipse.tradista.core.marketdata.service.FXCurveService;
import org.eclipse.tradista.core.marketdata.service.FeedService;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveService;
import org.eclipse.tradista.core.marketdata.service.MarketDataConfigurationService;
import org.eclipse.tradista.core.marketdata.service.MarketDataInformationService;
import org.eclipse.tradista.core.marketdata.service.MarketDataService;
import org.eclipse.tradista.core.marketdata.service.QuoteService;
import org.eclipse.tradista.core.marketdata.service.SurfaceService;
import org.eclipse.tradista.core.position.service.PositionCalculationErrorService;
import org.eclipse.tradista.core.position.service.PositionDefinitionService;
import org.eclipse.tradista.core.position.service.PositionService;
import org.eclipse.tradista.core.pricing.service.PricerService;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsService;
import org.eclipse.tradista.core.product.service.ProductService;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryService;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.core.transfer.service.FixingErrorService;
import org.eclipse.tradista.core.transfer.service.TransferService;
import org.eclipse.tradista.core.user.service.UserService;
import org.eclipse.tradista.core.workflow.service.WorkflowService;
import org.eclipse.tradista.fx.common.service.FXInformationService;
import org.eclipse.tradista.fx.fx.service.FXPricerService;
import org.eclipse.tradista.fx.fx.service.FXTradeService;
import org.eclipse.tradista.fx.fxndf.service.FXNDFPricerService;
import org.eclipse.tradista.fx.fxndf.service.FXNDFTradeService;
import org.eclipse.tradista.fx.fxoption.service.FXOptionPricerService;
import org.eclipse.tradista.fx.fxoption.service.FXOptionTradeService;
import org.eclipse.tradista.fx.fxoption.service.FXVolatilitySurfaceService;
import org.eclipse.tradista.fx.fxswap.service.FXSwapPricerService;
import org.eclipse.tradista.fx.fxswap.service.FXSwapTradeService;
import org.eclipse.tradista.ir.ccyswap.service.CcySwapPricerService;
import org.eclipse.tradista.ir.ccyswap.service.CcySwapTradeService;
import org.eclipse.tradista.ir.common.service.IRInformationService;
import org.eclipse.tradista.ir.fra.service.FRAPricerService;
import org.eclipse.tradista.ir.fra.service.FRATradeService;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationService;
import org.eclipse.tradista.ir.future.service.FuturePricerService;
import org.eclipse.tradista.ir.future.service.FutureService;
import org.eclipse.tradista.ir.future.service.FutureTradeService;
import org.eclipse.tradista.ir.ircapfloorcollar.service.IRCapFloorCollarPricerService;
import org.eclipse.tradista.ir.ircapfloorcollar.service.IRCapFloorCollarTradeService;
import org.eclipse.tradista.ir.irswap.service.IRSwapPricerService;
import org.eclipse.tradista.ir.irswap.service.IRSwapTradeService;
import org.eclipse.tradista.ir.irswapoption.service.IRSwapOptionPricerService;
import org.eclipse.tradista.ir.irswapoption.service.IRSwapOptionTradeService;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceService;
import org.eclipse.tradista.mm.common.service.MMInformationService;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositPricerService;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositTradeService;
import org.eclipse.tradista.security.bond.service.BondPricerService;
import org.eclipse.tradista.security.bond.service.BondService;
import org.eclipse.tradista.security.bond.service.BondTradeService;
import org.eclipse.tradista.security.common.service.SecurityInformationService;
import org.eclipse.tradista.security.equity.service.EquityPricerService;
import org.eclipse.tradista.security.equity.service.EquityService;
import org.eclipse.tradista.security.equity.service.EquityTradeService;
import org.eclipse.tradista.security.equityoption.service.EquityOptionContractSpecificationService;
import org.eclipse.tradista.security.equityoption.service.EquityOptionPricerService;
import org.eclipse.tradista.security.equityoption.service.EquityOptionService;
import org.eclipse.tradista.security.equityoption.service.EquityOptionTradeService;
import org.eclipse.tradista.security.equityoption.service.EquityOptionVolatilitySurfaceService;
import org.eclipse.tradista.security.gcrepo.service.GCBasketService;
import org.eclipse.tradista.security.gcrepo.service.GCRepoPricerService;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeService;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationService;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoPricerService;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoTradeService;

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

public class TradistaServiceLocator {

	private static TradistaServiceLocator instance = new TradistaServiceLocator();

	private static final String APP = "app";

	private static final String CORE_EJB = "core-ejb";

	private static final String SECURITY_EJB = "security-ejb";

	private static final String IR_EJB = "ir-ejb";

	private static final String FX_EJB = "fx-ejb";

	private static final String MM_EJB = "mm-ejb";

	private static final String SECURITY_COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.security.common.service";

	private static final String BOND_SERVICE_PACKAGE = "org.eclipse.tradista.security.bond.service";

	private static final String REPO_SERVICE_PACKAGE = "org.eclipse.tradista.security.repo.service";

	private static final String GC_REPO_SERVICE_PACKAGE = "org.eclipse.tradista.security.gcrepo.service";

	private static final String SPECIFIC_REPO_SERVICE_PACKAGE = "org.eclipse.tradista.security.specificrepo.service";

	private static final String EQUITY_SERVICE_PACKAGE = "org.eclipse.tradista.security.equity.service";

	private static final String EQUITY_OPTION_SERVICE_PACKAGE = "org.eclipse.tradista.security.equityoption.service";

	private static final String IR_COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.ir.common.service";

	private static final String CCY_SWAP_SERVICE_PACKAGE = "org.eclipse.tradista.ir.ccyswap.service";

	private static final String FRA_SERVICE_PACKAGE = "org.eclipse.tradista.ir.fra.service";

	private static final String FUTURE_SERVICE_PACKAGE = "org.eclipse.tradista.ir.future.service";

	private static final String IR_CAP_FLOOR_COLLAR_SERVICE_PACKAGE = "org.eclipse.tradista.ir.ircapfloorcollar.service";

	private static final String IR_SWAP_OPTION_SERVICE_PACKAGE = "org.eclipse.tradista.ir.irswapoption.service";

	private static final String IR_SWAP_SERVICE_PACKAGE = "org.eclipse.tradista.ir.irswap.service";

	private static final String FX_SERVICE_PACKAGE = "org.eclipse.tradista.fx.fx.service";

	private static final String FX_COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.fx.common.service";

	private static final String FX_NDF_SERVICE_PACKAGE = "org.eclipse.tradista.fx.fxndf.service";

	private static final String FX_OPTION_SERVICE_PACKAGE = "org.eclipse.tradista.fx.fxoption.service";

	private static final String FX_SWAP_SERVICE_PACKAGE = "org.eclipse.tradista.fx.fxswap.service";

	private static final String LOAN_DEPOSIT_SERVICE_PACKAGE = "org.eclipse.tradista.mm.loandeposit.service";

	private static final String MM_COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.mm.common.service";

	private static final String MARKET_DATA_APP = "marketdata-app";

	private static final String MARKET_DATA_EJB = "marketdata-ejb";

	private static final String MARKET_DATA_SERVICE_PACKAGE = "org.eclipse.tradista.core.marketdata.service";

	private static final String POSITION_APP = "position-app";

	private static final String POSITION_EJB = "position-ejb";

	private static final String POSITION_SERVICE_PACKAGE = "org.eclipse.tradista.core.position.service";

	private static final String TRANSFER_SERVICE_PACKAGE = "org.eclipse.tradista.core.transfer.service";

	private static final String WORKFLOW_SERVICE_PACKAGE = "org.eclipse.tradista.core.workflow.service";

	private static final String PROCESSING_ORG_DEFAULTS_SERVICE_PACKAGE = "org.eclipse.tradista.core.processingorgdefaults.service";

	private static final String CONFIGURATION_SERVICE_PACKAGE = "org.eclipse.tradista.core.configuration.service";

	private static final String DAILY_PNL_SERVICE_PACKAGE = "org.eclipse.tradista.core.dailypnl.service";

	private static final String PRODUCT_INVENTORY_SERVICE_PACKAGE = "org.eclipse.tradista.core.productinventory.service";

	private static final String CASH_INVENTORY_SERVICE_PACKAGE = "org.eclipse.tradista.core.cashinventory.service";

	private static final String COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.core.common.service";

	private static final String AI_APP = "ai-app";

	private static final String AI_EJB = "ai-ejb";

	private static final String FOL_SERVICE_PACKAGE = "org.eclipse.tradista.ai.reasoning.fol.service";

	private static final String AI_COMMON_SERVICE_PACKAGE = "org.eclipse.tradista.ai.reasoning.common.service";

	private static final String AGENT_SERVICE_PACKAGE = "org.eclipse.tradista.ai.agent.service";

	private static final String TRANSFER_APP = "transfer-app";

	private static final String TRANSFER_EJB = "transfer-ejb";

	private static final String EJB_PREFIX = "ejb:";

	private static final String JAVA_GLOBAL_PREFIX = "java:global";

	private static final String USER_SERVICE_PACKAGE = CORE_PACKAGE + ".user.service";

	private static final String CALENDAR_SERVICE_PACKAGE = CORE_PACKAGE + ".calendar.service";

	private static final String DATE_RULE_SERVICE_PACKAGE = CORE_PACKAGE + ".daterule.service";

	private static final String EXCHANGE_SERVICE_PACKAGE = CORE_PACKAGE + ".exchange.service";

	private static final String PRICING_SERVICE_PACKAGE = CORE_PACKAGE + ".pricing.service";

	private static final String BATCH_SERVICE_PACKAGE = CORE_PACKAGE + ".batch.service";

	private static final String LEGAL_ENTITY_SERVICE_PACKAGE = CORE_PACKAGE + ".legalentity.service";

	private static final String DAY_COUNT_CONVENTION_SERVICE_PACKAGE = CORE_PACKAGE + ".daycountconvention.service";

	private static final String INDEX_SERVICE_PACKAGE = CORE_PACKAGE + ".index.service";

	private static final String TRADE_SERVICE_PACKAGE = CORE_PACKAGE + ".trade.service";

	private static final String PRODUCT_SERVICE_PACKAGE = CORE_PACKAGE + ".product.service";

	private static final String ERROR_SERVICE_PACKAGE = CORE_PACKAGE + ".error.service";

	private static final String CURRENCY_SERVICE_PACKAGE = CORE_PACKAGE + ".currency.service";

	private static final String BOOK_SERVICE_PACKAGE = CORE_PACKAGE + ".book.service";

	private Context context;

	private Map<String, Object> services;

	private TradistaServiceLocator() {
		try {
			services = Collections.synchronizedMap(new HashMap<String, Object>());
			context = new InitialContext();
		} catch (NamingException ne) {
			ne.printStackTrace();
		}
	}

	public static TradistaServiceLocator getInstance() {
		return instance;
	}

	public MarketDataService getMarketDataService() {
		return (MarketDataService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE,
				"MarketDataService");
	}

	public QuoteService getQuoteService() {
		return (QuoteService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE, "QuoteService");
	}

	public ConfigurationService getConfigurationService() {
		return (ConfigurationService) getService(APP, CORE_EJB, CONFIGURATION_SERVICE_PACKAGE, "ConfigurationService");
	}

	public FeedService getFeedService() {
		return (FeedService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE, "FeedService");
	}

	public LegalEntityService getLegalEntityService() {
		return (LegalEntityService) getService(APP, CORE_EJB, LEGAL_ENTITY_SERVICE_PACKAGE, "LegalEntityService");
	}

	public DayCountConventionService getDayCountConventionService() {
		return (DayCountConventionService) getService(APP, CORE_EJB, DAY_COUNT_CONVENTION_SERVICE_PACKAGE,
				"DayCountConventionService");
	}

	public IndexService getIndexService() {
		return (IndexService) getService(APP, CORE_EJB, INDEX_SERVICE_PACKAGE, "IndexService");
	}

	public TradeService getTradeService() {
		return (TradeService) getService(APP, CORE_EJB, TRADE_SERVICE_PACKAGE, "TradeService");
	}

	public ProductService getProductService() {
		return (ProductService) getService(APP, CORE_EJB, PRODUCT_SERVICE_PACKAGE, "ProductService");
	}

	public PositionDefinitionService getPositionDefinitionService() {
		return (PositionDefinitionService) getService(POSITION_APP, POSITION_EJB, POSITION_SERVICE_PACKAGE,
				"PositionDefinitionService");
	}

	public PositionCalculationErrorService getPositionCalculationErrorService() {
		return (PositionCalculationErrorService) getService(POSITION_APP, POSITION_EJB, POSITION_SERVICE_PACKAGE,
				"PositionCalculationErrorService");
	}

	public FixingErrorService getFixingErrorService() {
		return (FixingErrorService) getService(TRANSFER_APP, TRANSFER_EJB, TRANSFER_SERVICE_PACKAGE,
				"FixingErrorService");
	}

	public ErrorService getErrorService() {
		return (ErrorService) getService(APP, CORE_EJB, ERROR_SERVICE_PACKAGE, "ErrorService");
	}

	public PositionService getPositionService() {
		return (PositionService) getService(POSITION_APP, POSITION_EJB, POSITION_SERVICE_PACKAGE, "PositionService");
	}

	public FolFormulaService getFolFormulaService() {
		return (FolFormulaService) getService(AI_APP, AI_EJB, FOL_SERVICE_PACKAGE, "FolFormulaService");
	}

	public FormulaService getFormulaService() {
		return (FormulaService) getService(AI_APP, AI_EJB, AI_COMMON_SERVICE_PACKAGE, "FormulaService");
	}

	public MandateService getMandateService() {
		return (MandateService) getService(AI_APP, AI_EJB, AGENT_SERVICE_PACKAGE, "MandateService");
	}

	public AssetManagerAgentService getAssetManagerAgentService() {
		return (AssetManagerAgentService) getService(AI_APP, AI_EJB, AGENT_SERVICE_PACKAGE, "AssetManagerAgentService");
	}

	public DailyPnlService getDailyPnlService() {
		return (DailyPnlService) getService(POSITION_APP, POSITION_EJB, DAILY_PNL_SERVICE_PACKAGE, "DailyPnlService");
	}

	public ProductInventoryService getProductInventoryService() {
		return (ProductInventoryService) getService(POSITION_APP, POSITION_EJB, PRODUCT_INVENTORY_SERVICE_PACKAGE,
				"ProductInventoryService");
	}

	public CurrencyService getCurrencyService() {
		return (CurrencyService) getService(APP, CORE_EJB, CURRENCY_SERVICE_PACKAGE, "CurrencyService");
	}

	public BookService getBookService() {
		return (BookService) getService(APP, CORE_EJB, BOOK_SERVICE_PACKAGE, "BookService");
	}

	public UserService getUserService() {
		return (UserService) getService(APP, CORE_EJB, USER_SERVICE_PACKAGE, "UserService");
	}

	public CalendarService getCalendarService() {
		return (CalendarService) getService(APP, CORE_EJB, CALENDAR_SERVICE_PACKAGE, "CalendarService");
	}

	public DateRuleService getDateRuleService() {
		return (DateRuleService) getService(APP, CORE_EJB, DATE_RULE_SERVICE_PACKAGE, "DateRuleService");
	}

	public ExchangeService getExchangeService() {
		return (ExchangeService) getService(APP, CORE_EJB, EXCHANGE_SERVICE_PACKAGE, "ExchangeService");
	}

	public PricerService getPricerService() {
		return (PricerService) getService(APP, CORE_EJB, PRICING_SERVICE_PACKAGE, "PricerService");
	}

	public InterestRateCurveService getInterestRateCurveService() {
		return (InterestRateCurveService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE,
				"InterestRateCurveService");
	}

	public FXCurveService getFXCurveService() {
		return (FXCurveService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE,
				"FXCurveService");
	}

	public CurveService getCurveService() {
		return (CurveService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE, "CurveService");
	}

	public BondService getBondService() {
		return (BondService) getService(APP, SECURITY_EJB, BOND_SERVICE_PACKAGE, "BondService");
	}

	public EquityService getEquityService() {
		return (EquityService) getService(APP, SECURITY_EJB, EQUITY_SERVICE_PACKAGE, "EquityService");
	}

	public EquityTradeService getEquityTradeService() {
		return (EquityTradeService) getService(APP, SECURITY_EJB, EQUITY_SERVICE_PACKAGE, "EquityTradeService");
	}

	public EquityPricerService getEquityPricerService() {
		return (EquityPricerService) getService(APP, SECURITY_EJB, EQUITY_SERVICE_PACKAGE, "EquityPricerService");
	}

	public EquityOptionTradeService getEquityOptionTradeService() {
		return (EquityOptionTradeService) getService(APP, SECURITY_EJB, EQUITY_OPTION_SERVICE_PACKAGE,
				"EquityOptionTradeService");
	}

	public EquityOptionContractSpecificationService getEquityOptionSpecificationService() {
		return (EquityOptionContractSpecificationService) getService(APP, SECURITY_EJB, EQUITY_OPTION_SERVICE_PACKAGE,
				"EquityOptionContractSpecificationService");
	}

	public EquityOptionService getEquityOptionService() {
		return (EquityOptionService) getService(APP, SECURITY_EJB, EQUITY_OPTION_SERVICE_PACKAGE,
				"EquityOptionService");
	}

	public EquityOptionPricerService getEquityOptionPricerService() {
		return (EquityOptionPricerService) getService(APP, SECURITY_EJB, EQUITY_OPTION_SERVICE_PACKAGE,
				"EquityOptionPricerService");
	}

	public EquityOptionVolatilitySurfaceService getEquityOptionVolatilitySurfaceService() {
		return (EquityOptionVolatilitySurfaceService) getService(APP, SECURITY_EJB, EQUITY_OPTION_SERVICE_PACKAGE,
				"EquityOptionVolatilitySurfaceService");
	}

	public BondTradeService getBondTradeService() {
		return (BondTradeService) getService(APP, SECURITY_EJB, BOND_SERVICE_PACKAGE, "BondTradeService");
	}

	public BondPricerService getBondPricerService() {
		return (BondPricerService) getService(APP, SECURITY_EJB, BOND_SERVICE_PACKAGE, "BondPricerService");
	}

	public GCRepoTradeService getGCRepoTradeService() {
		return (GCRepoTradeService) getService(APP, SECURITY_EJB, GC_REPO_SERVICE_PACKAGE, "GCRepoTradeService");
	}

	public GCBasketService getGCBasketService() {
		return (GCBasketService) getService(APP, SECURITY_EJB, GC_REPO_SERVICE_PACKAGE, "GCBasketService");
	}

	public GCRepoPricerService getGCRepoPricerService() {
		return (GCRepoPricerService) getService(APP, SECURITY_EJB, GC_REPO_SERVICE_PACKAGE, "GCRepoPricerService");
	}

	public SpecificRepoTradeService getSpecificRepoTradeService() {
		return (SpecificRepoTradeService) getService(APP, SECURITY_EJB, SPECIFIC_REPO_SERVICE_PACKAGE,
				"SpecificRepoTradeService");
	}

	public SpecificRepoPricerService getSpecificRepoPricerService() {
		return (SpecificRepoPricerService) getService(APP, SECURITY_EJB, SPECIFIC_REPO_SERVICE_PACKAGE,
				"SpecificRepoPricerService");
	}

	public AllocationConfigurationService getAllocationConfigurationService() {
		return (AllocationConfigurationService) getService(APP, SECURITY_EJB, REPO_SERVICE_PACKAGE,
				"AllocationConfigurationService");
	}

	public CcySwapTradeService getCcySwapTradeService() {
		return (CcySwapTradeService) getService(APP, IR_EJB, CCY_SWAP_SERVICE_PACKAGE, "CcySwapTradeService");
	}

	public CcySwapPricerService getCcySwapPricerService() {
		return (CcySwapPricerService) getService(APP, IR_EJB, CCY_SWAP_SERVICE_PACKAGE, "CcySwapPricerService");
	}

	public FRATradeService getFRATradeService() {
		return (FRATradeService) getService(APP, IR_EJB, FRA_SERVICE_PACKAGE, "FRATradeService");
	}

	public FRAPricerService getFRAPricerService() {
		return (FRAPricerService) getService(APP, IR_EJB, FRA_SERVICE_PACKAGE, "FRAPricerService");
	}

	public IRCapFloorCollarTradeService getIRCapFloorCollarTradeService() {
		return (IRCapFloorCollarTradeService) getService(APP, IR_EJB, IR_CAP_FLOOR_COLLAR_SERVICE_PACKAGE,
				"IRCapFloorCollarTradeService");
	}

	public IRCapFloorCollarPricerService getIRCapFloorCollarPricerService() {
		return (IRCapFloorCollarPricerService) getService(APP, IR_EJB, IR_CAP_FLOOR_COLLAR_SERVICE_PACKAGE,
				"IRCapFloorCollarPricerService");
	}

	public IRSwapOptionTradeService getIRSwapOptionTradeService() {
		return (IRSwapOptionTradeService) getService(APP, IR_EJB, IR_SWAP_OPTION_SERVICE_PACKAGE,
				"IRSwapOptionTradeService");
	}

	public IRSwapOptionPricerService getIRSwapOptionPricerService() {
		return (IRSwapOptionPricerService) getService(APP, IR_EJB, IR_SWAP_OPTION_SERVICE_PACKAGE,
				"IRSwapOptionPricerService");
	}

	public IRSwapTradeService getIRSwapTradeService() {
		return (IRSwapTradeService) getService(APP, IR_EJB, IR_SWAP_SERVICE_PACKAGE, "IRSwapTradeService");
	}

	public IRSwapPricerService getIRSwapPricerService() {
		return (IRSwapPricerService) getService(APP, IR_EJB, IR_SWAP_SERVICE_PACKAGE, "IRSwapPricerService");
	}

	public SwaptionVolatilitySurfaceService getSwaptionVolatilitySurfaceService() {
		return (SwaptionVolatilitySurfaceService) getService(APP, IR_EJB, IR_SWAP_OPTION_SERVICE_PACKAGE,
				"SwaptionVolatilitySurfaceService");
	}

	public FutureService getFutureService() {
		return (FutureService) getService(APP, IR_EJB, FUTURE_SERVICE_PACKAGE, "FutureService");
	}

	public FutureContractSpecificationService getFutureContractSpecificationService() {
		return (FutureContractSpecificationService) getService(APP, IR_EJB, FUTURE_SERVICE_PACKAGE,
				"FutureContractSpecificationService");
	}

	public FutureTradeService getFutureTradeService() {
		return (FutureTradeService) getService(APP, IR_EJB, FUTURE_SERVICE_PACKAGE, "FutureTradeService");
	}

	public FuturePricerService getFuturePricerService() {
		return (FuturePricerService) getService(APP, IR_EJB, FUTURE_SERVICE_PACKAGE, "FuturePricerService");
	}

	public FXNDFTradeService getFXNDFTradeService() {
		return (FXNDFTradeService) getService(APP, FX_EJB, FX_NDF_SERVICE_PACKAGE, "FXNDFTradeService");
	}

	public FXPricerService getFXPricerService() {
		return (FXPricerService) getService(APP, FX_EJB, FX_SERVICE_PACKAGE, "FXPricerService");
	}

	public FXOptionPricerService getFXOptionPricerService() {
		return (FXOptionPricerService) getService(APP, FX_EJB, FX_OPTION_SERVICE_PACKAGE, "FXOptionPricerService");
	}

	public FXNDFPricerService getFXNDFPricerService() {
		return (FXNDFPricerService) getService(APP, FX_EJB, FX_NDF_SERVICE_PACKAGE, "FXNDFPricerService");
	}

	public FXSwapPricerService getFXSwapPricerService() {
		return (FXSwapPricerService) getService(APP, FX_EJB, FX_SWAP_SERVICE_PACKAGE, "FXSwapPricerService");
	}

	public FXOptionTradeService getFXOptionTradeService() {
		return (FXOptionTradeService) getService(APP, FX_EJB, FX_OPTION_SERVICE_PACKAGE, "FXOptionTradeService");
	}

	public FXVolatilitySurfaceService getFXVolatilitySurfaceService() {
		return (FXVolatilitySurfaceService) getService(APP, FX_EJB, FX_OPTION_SERVICE_PACKAGE,
				"FXVolatilitySurfaceService");
	}

	public FXSwapTradeService getFXSwapTradeService() {
		return (FXSwapTradeService) getService(APP, FX_EJB, FX_SWAP_SERVICE_PACKAGE, "FXSwapTradeService");
	}

	public FXTradeService getFXTradeService() {
		return (FXTradeService) getService(APP, FX_EJB, FX_SERVICE_PACKAGE, "FXTradeService");
	}

	public LoanDepositTradeService getLoanDepositService() {
		return (LoanDepositTradeService) getService(APP, MM_EJB, LOAN_DEPOSIT_SERVICE_PACKAGE,
				"LoanDepositTradeService");
	}

	public LoanDepositPricerService getLoanDepositPricerService() {
		return (LoanDepositPricerService) getService(APP, MM_EJB, LOAN_DEPOSIT_SERVICE_PACKAGE,
				"LoanDepositPricerService");
	}

	public MarketDataConfigurationService getMarketDataConfigurationService() {
		return (MarketDataConfigurationService) getService(MARKET_DATA_APP, MARKET_DATA_EJB,
				MARKET_DATA_SERVICE_PACKAGE, "MarketDataConfigurationService");
	}

	public BatchService getBatchService() {
		return (BatchService) getService(APP, CORE_EJB, BATCH_SERVICE_PACKAGE, "BatchService");
	}

	public SurfaceService getSurfaceService() {
		return (SurfaceService) getService(MARKET_DATA_APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE,
				"SurfaceService");
	}

	public InformationService getInformationService() {
		return (InformationService) getService(APP, CORE_EJB, COMMON_SERVICE_PACKAGE, "InformationService");
	}

	public MarketDataInformationService getMarketDataInformationService() {
		return (MarketDataInformationService) getService(APP, MARKET_DATA_EJB, MARKET_DATA_SERVICE_PACKAGE,
				"MarketDataInformationService");
	}

	public FXInformationService getFXInformationService() {
		return (FXInformationService) getService(APP, FX_EJB, FX_COMMON_SERVICE_PACKAGE, "FXInformationService");
	}

	public MMInformationService getMMInformationService() {
		return (MMInformationService) getService(APP, MM_EJB, MM_COMMON_SERVICE_PACKAGE, "MMInformationService");
	}

	public IRInformationService getIRInformationService() {
		return (IRInformationService) getService(APP, IR_EJB, IR_COMMON_SERVICE_PACKAGE, "IRInformationService");
	}

	public SecurityInformationService getSecurityInformationService() {
		return (SecurityInformationService) getService(APP, SECURITY_EJB, SECURITY_COMMON_SERVICE_PACKAGE,
				"SecurityInformationService");
	}

	public CashInventoryService getCashInventoryService() {
		return (CashInventoryService) getService(POSITION_APP, POSITION_EJB, CASH_INVENTORY_SERVICE_PACKAGE,
				"CashInventoryService");
	}

	public TransferService getTransferService() {
		return (TransferService) getService(TRANSFER_APP, TRANSFER_EJB, TRANSFER_SERVICE_PACKAGE, "TransferService");
	}

	private Object getService(String application, String module, String packageName, String serviceName) {

		Object service;

		if (services.containsKey(serviceName)) {
			return services.get(serviceName);
		}

		// First, we use the ejb prefix which is optimized
		String ejbString = EJB_PREFIX + application + "/" + module + "/" + serviceName + "Bean!" + packageName + "."
				+ serviceName;
		try {
			service = context.lookup(ejbString);
			services.put(serviceName, service);
			return services.get(serviceName);
		} catch (NamingException ne) {
			// TODO Have a log instead
			ne.printStackTrace();
		}

		// If the previous lookup fails, we try the global prefix
		ejbString = JAVA_GLOBAL_PREFIX + "/" + application + "/" + module + "/" + serviceName + "Bean!" + packageName
				+ "." + serviceName;
		try {
			service = context.lookup(ejbString);
			services.put(serviceName, service);
			return services.get(serviceName);
		} catch (NamingException ne) {
			// TODO Have a log instead
			ne.printStackTrace();
		}

		return null;
	}

	public WorkflowService getWorkflowService() {
		return (WorkflowService) getService(APP, CORE_EJB, WORKFLOW_SERVICE_PACKAGE, "WorkflowService");
	}

	public ProcessingOrgDefaultsService getProcessingOrgDefaultsService() {
		return (ProcessingOrgDefaultsService) getService(APP, CORE_EJB, PROCESSING_ORG_DEFAULTS_SERVICE_PACKAGE,
				"ProcessingOrgDefaultsService");
	}

}