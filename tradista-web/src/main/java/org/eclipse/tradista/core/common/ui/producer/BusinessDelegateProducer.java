package org.eclipse.tradista.core.common.ui.producer;

import org.eclipse.tradista.ai.analysis.service.BookAnalysisBusinessDelegate;
import org.eclipse.tradista.ai.analysis.service.CashflowsAnalysisBusinessDelegate;
import org.eclipse.tradista.ai.analysis.service.CollateralOptimizationBusinessDelegate;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.messaging.service.MessagingConfigurationBusinessDelegate;
import org.eclipse.tradista.core.common.service.InformationBusinessDelegate;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.exporter.service.ExporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.mapping.service.MappingBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.MarketDataConfigurationBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.message.service.ExportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.rating.service.RatingBusinessDelegate;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.common.service.SecurityBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityBusinessDelegate;
import org.eclipse.tradista.security.equity.service.EquityTradeBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.service.GCBasketBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.service.GCRepoPricerBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationBusinessDelegate;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoPricerBusinessDelegate;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoTradeBusinessDelegate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

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

@ApplicationScoped
public class BusinessDelegateProducer {

	@Produces
	@ApplicationScoped
	public BookAnalysisBusinessDelegate produceBookAnalysisBusinessDelegate() {
		return new BookAnalysisBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public CashflowsAnalysisBusinessDelegate produceCashflowsAnalysisBusinessDelegate() {
		return new CashflowsAnalysisBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public CollateralOptimizationBusinessDelegate produceCollateralOptimizationBusinessDelegate() {
		return new CollateralOptimizationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public BookBusinessDelegate produceBookBusinessDelegate() {
		return new BookBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public MessagingConfigurationBusinessDelegate produceMessagingConfigurationBusinessDelegate() {
		return new MessagingConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public InformationBusinessDelegate produceInformationBusinessDelegate() {
		return new InformationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ConfigurationBusinessDelegate produceConfigurationBusinessDelegate() {
		return new ConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public CurrencyBusinessDelegate produceCurrencyBusinessDelegate() {
		return new CurrencyBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ExporterConfigurationBusinessDelegate produceExporterConfigurationBusinessDelegate() {
		return new ExporterConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ImporterConfigurationBusinessDelegate produceImporterConfigurationBusinessDelegate() {
		return new ImporterConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public IndexBusinessDelegate produceIndexBusinessDelegate() {
		return new IndexBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public MappingBusinessDelegate produceMappingBusinessDelegate() {
		return new MappingBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public MarketDataConfigurationBusinessDelegate produceMarketDataConfigurationBusinessDelegate() {
		return new MarketDataConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public QuoteBusinessDelegate produceQuoteBusinessDelegate() {
		return new QuoteBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ExportErrorBusinessDelegate produceExportErrorBusinessDelegate() {
		return new ExportErrorBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ImportErrorBusinessDelegate produceImportErrorBusinessDelegate() {
		return new ImportErrorBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public MessageBusinessDelegate produceMessageBusinessDelegate() {
		return new MessageBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public PricerBusinessDelegate producePricerBusinessDelegate() {
		return new PricerBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ProcessingOrgDefaultsBusinessDelegate produceProcessingOrgDefaultsBusinessDelegate() {
		return new ProcessingOrgDefaultsBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public ProductInventoryBusinessDelegate produceProductInventoryBusinessDelegate() {
		return new ProductInventoryBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public RatingBusinessDelegate produceRatingBusinessDelegate() {
		return new RatingBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public TradeBusinessDelegate produceTradeBusinessDelegate() {
		return new TradeBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public UserBusinessDelegate produceUserBusinessDelegate() {
		return new UserBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public WorkflowBusinessDelegate produceWorkflowBusinessDelegate() {
		return new WorkflowBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public LegalEntityBusinessDelegate produceLegalEntityBusinessDelegate() {
		return new LegalEntityBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public SecurityBusinessDelegate produceSecurityBusinessDelegate() {
		return new SecurityBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public EquityBusinessDelegate produceEquityBusinessDelegate() {
		return new EquityBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public EquityTradeBusinessDelegate produceEquityTradeBusinessDelegate() {
		return new EquityTradeBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public GCBasketBusinessDelegate produceGCBasketBusinessDelegate() {
		return new GCBasketBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public GCRepoPricerBusinessDelegate produceGCRepoPricerBusinessDelegate() {
		return new GCRepoPricerBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public GCRepoTradeBusinessDelegate produceGCRepoTradeBusinessDelegate() {
		return new GCRepoTradeBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public AllocationConfigurationBusinessDelegate produceAllocationConfigurationBusinessDelegate() {
		return new AllocationConfigurationBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public SpecificRepoPricerBusinessDelegate produceSpecificRepoPricerBusinessDelegate() {
		return new SpecificRepoPricerBusinessDelegate();
	}

	@Produces
	@ApplicationScoped
	public SpecificRepoTradeBusinessDelegate produceSpecificRepoTradeBusinessDelegate() {
		return new SpecificRepoTradeBusinessDelegate();
	}
}
