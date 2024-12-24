package org.eclipse.tradista.security.equityoption.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daterule.service.DateRuleSQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.trade.model.OptionTrade.SettlementType;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class EquityOptionContractSpecificationSQL {

	public static long saveEquityOptionContractSpecification(EquityOptionContractSpecification eocs) {
		long equityOptionContractSpecificationId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveEquityOptionContractSpecification = (eocs.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO EQUITY_OPTION_CONTRACT_SPECIFICATION(NAME, STYLE, QUANTITY, SETTLEMENT_TYPE, SETTLEMENT_DATE_OFFSET, MATURITY_DATES_DATE_RULE_ID, MULTIPLIER, PREMIUM_CURRENCY_ID, EXCHANGE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE EQUITY_OPTION_CONTRACT_SPECIFICATION SET NAME=?, STYLE=?, QUANTITY=?, SETTLEMENT_TYPE=?, SETTLEMENT_DATE_OFFSET=?, MATURITY_DATES_DATE_RULE_ID=?, MULTIPLIER=?, PREMIUM_CURRENCY_ID=?, EXCHANGE_ID = ? WHERE ID=?")) {
			if (eocs.getId() != 0) {
				stmtSaveEquityOptionContractSpecification.setLong(10, eocs.getId());
			}
			stmtSaveEquityOptionContractSpecification.setString(1, eocs.getName());
			stmtSaveEquityOptionContractSpecification.setString(2, eocs.getStyle().name());
			stmtSaveEquityOptionContractSpecification.setBigDecimal(3, eocs.getQuantity());
			stmtSaveEquityOptionContractSpecification.setString(4, eocs.getSettlementType().name());
			stmtSaveEquityOptionContractSpecification.setShort(5, eocs.getSettlementDateOffset());
			stmtSaveEquityOptionContractSpecification.setLong(6, eocs.getMaturityDatesDateRule().getId());
			stmtSaveEquityOptionContractSpecification.setBigDecimal(7, eocs.getMultiplier());
			stmtSaveEquityOptionContractSpecification.setLong(8, eocs.getPremiumCurrency().getId());
			stmtSaveEquityOptionContractSpecification.setLong(9, eocs.getExchange().getId());

			stmtSaveEquityOptionContractSpecification.executeUpdate();

			if (eocs.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveEquityOptionContractSpecification.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						equityOptionContractSpecificationId = generatedKeys.getLong(1);
					} else {
						throw new SQLException(
								"Creation of equity option contract specification failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		eocs.setId(equityOptionContractSpecificationId);
		return equityOptionContractSpecificationId;
	}

	public static Set<EquityOptionContractSpecification> getAllEquityOptionContractSpecifications() {
		Set<EquityOptionContractSpecification> equityOptionContractSpecifications = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquityOptionContractSpecifications = con
						.prepareStatement("SELECT * FROM EQUITY_OPTION_CONTRACT_SPECIFICATION");
				ResultSet results = stmtGetAllEquityOptionContractSpecifications.executeQuery()) {
			while (results.next()) {
				if (equityOptionContractSpecifications == null) {
					equityOptionContractSpecifications = new HashSet<EquityOptionContractSpecification>();
				}
				EquityOptionContractSpecification equityOptionContractSpecification = new EquityOptionContractSpecification(
						results.getString("name"));
				equityOptionContractSpecification.setId(results.getLong("id"));
				equityOptionContractSpecification.setQuantity(results.getBigDecimal("quantity"));
				equityOptionContractSpecification
						.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
				equityOptionContractSpecification
						.setStyle(VanillaOptionTrade.Style.valueOf(results.getString("style")));
				equityOptionContractSpecification
						.setSettlementType(SettlementType.valueOf(results.getString("settlement_type")));
				equityOptionContractSpecification.setSettlementDateOffset(results.getShort("settlement_date_offset"));
				equityOptionContractSpecification.setMaturityDatesDateRule(
						DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
				equityOptionContractSpecification.setMultiplier(results.getBigDecimal("multiplier"));
				equityOptionContractSpecification
						.setPremiumCurrency(CurrencySQL.getCurrencyById(results.getLong("premium_currency_id")));
				equityOptionContractSpecifications.add(equityOptionContractSpecification);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionContractSpecifications;
	}

	public static EquityOptionContractSpecification getEquityOptionContractSpecificationById(long id) {
		EquityOptionContractSpecification equityOptionContractSpecification = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionContractSpecificationById = con
						.prepareStatement("SELECT * FROM EQUITY_OPTION_CONTRACT_SPECIFICATION WHERE ID = ?")) {
			stmtGetEquityOptionContractSpecificationById.setLong(1, id);
			try (ResultSet results = stmtGetEquityOptionContractSpecificationById.executeQuery()) {
				while (results.next()) {
					equityOptionContractSpecification = new EquityOptionContractSpecification(
							results.getString("name"));
					equityOptionContractSpecification.setId(results.getLong("id"));
					equityOptionContractSpecification.setQuantity(results.getBigDecimal("quantity"));
					equityOptionContractSpecification
							.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
					equityOptionContractSpecification
							.setStyle(VanillaOptionTrade.Style.valueOf(results.getString("style")));
					equityOptionContractSpecification
							.setSettlementType(SettlementType.valueOf(results.getString("settlement_type")));
					equityOptionContractSpecification
							.setSettlementDateOffset(results.getShort("settlement_date_offset"));
					equityOptionContractSpecification.setMaturityDatesDateRule(
							DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
					equityOptionContractSpecification.setMultiplier(results.getBigDecimal("multiplier"));
					equityOptionContractSpecification
							.setPremiumCurrency(CurrencySQL.getCurrencyById(results.getLong("premium_currency_id")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionContractSpecification;
	}

	public static EquityOptionContractSpecification getEquityOptionContractSpecificationByName(String name) {
		EquityOptionContractSpecification equityOptionContractSpecification = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionContractSpecificationByName = con
						.prepareStatement("SELECT * FROM EQUITY_OPTION_CONTRACT_SPECIFICATION WHERE NAME = ?")) {
			stmtGetEquityOptionContractSpecificationByName.setString(1, name);
			try (ResultSet results = stmtGetEquityOptionContractSpecificationByName.executeQuery()) {
				while (results.next()) {
					equityOptionContractSpecification = new EquityOptionContractSpecification(
							results.getString("name"));
					equityOptionContractSpecification.setId(results.getLong("id"));
					equityOptionContractSpecification.setQuantity(results.getBigDecimal("quantity"));
					equityOptionContractSpecification
							.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
					equityOptionContractSpecification
							.setStyle(VanillaOptionTrade.Style.valueOf(results.getString("style")));
					equityOptionContractSpecification
							.setSettlementType(SettlementType.valueOf(results.getString("settlement_type")));
					equityOptionContractSpecification
							.setSettlementDateOffset(results.getShort("settlement_date_offset"));
					equityOptionContractSpecification.setMaturityDatesDateRule(
							DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
					equityOptionContractSpecification.setMultiplier(results.getBigDecimal("multiplier"));
					equityOptionContractSpecification
							.setPremiumCurrency(CurrencySQL.getCurrencyById(results.getLong("premium_currency_id")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionContractSpecification;
	}

}