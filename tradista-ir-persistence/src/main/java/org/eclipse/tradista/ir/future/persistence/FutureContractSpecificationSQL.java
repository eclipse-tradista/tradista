package org.eclipse.tradista.ir.future.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daterule.service.DateRuleSQL;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class FutureContractSpecificationSQL {

	public static FutureContractSpecification getFutureContractSpecificationById(long id) {

		FutureContractSpecification spec = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureContractSpecificationById = con
						.prepareStatement("SELECT * FROM FUTURE_CONTRACT_SPECIFICATION WHERE ID = ?")) {
			stmtGetFutureContractSpecificationById.setLong(1, id);
			try (ResultSet results = stmtGetFutureContractSpecificationById.executeQuery()) {
				while (results.next()) {
					if (spec == null) {
						spec = new FutureContractSpecification(results.getString("name"));
					}

					spec.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					spec.setId(results.getLong("id"));
					spec.setNotional(results.getBigDecimal("notional"));
					spec.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
					spec.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
					spec.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
					spec.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
					spec.setMaturityDatesDateRule(
							DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return spec;
	}

	public static FutureContractSpecification getFutureContractSpecificationByName(String name) {

		FutureContractSpecification spec = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureContractSpecificationByName = con
						.prepareStatement("SELECT * FROM FUTURE_CONTRACT_SPECIFICATION WHERE NAME = ?")) {
			stmtGetFutureContractSpecificationByName.setString(1, name);
			try (ResultSet results = stmtGetFutureContractSpecificationByName.executeQuery()) {
				while (results.next()) {
					if (spec == null) {
						spec = new FutureContractSpecification(results.getString("name"));
					}

					spec.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					spec.setId(results.getLong("id"));
					spec.setNotional(results.getBigDecimal("notional"));
					spec.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
					spec.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
					spec.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
					spec.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
					spec.setMaturityDatesDateRule(
							DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return spec;
	}

	public static Set<FutureContractSpecification> getAllFutureContractSpecifications() {

		Set<FutureContractSpecification> specs = new TreeSet<FutureContractSpecification>();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFutureContractSpecifications = con
						.prepareStatement("SELECT * FROM FUTURE_CONTRACT_SPECIFICATION");
				ResultSet results = stmtGetAllFutureContractSpecifications.executeQuery()) {
			while (results.next()) {

				FutureContractSpecification spec = new FutureContractSpecification(results.getString("name"));

				spec.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				spec.setId(results.getLong("id"));
				spec.setNotional(results.getBigDecimal("notional"));
				spec.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
				spec.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
				spec.setDayCountConvention(
						DayCountConventionSQL.getDayCountConventionById(results.getLong("day_count_convention_id")));
				spec.setExchange(ExchangeSQL.getExchangeById(results.getLong("exchange_id")));
				spec.setMaturityDatesDateRule(
						DateRuleSQL.getDateRuleById(results.getLong("maturity_dates_date_rule_id")));
				specs.add(spec);

			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return specs;
	}

	public static long saveFutureContractSpecification(FutureContractSpecification fcs) {
		long fcsId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFutureContractSpecification = (fcs.getId() == 0) ? con.prepareStatement(
						"INSERT INTO FUTURE_CONTRACT_SPECIFICATION(REFERENCE_RATE_INDEX_ID, REFERENCE_RATE_INDEX_TENOR, DAY_COUNT_CONVENTION_ID, NOTIONAL, CURRENCY_ID, NAME, EXCHANGE_ID, MATURITY_DATES_DATE_RULE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE FUTURE_CONTRACT_SPECIFICATION SET REFERENCE_RATE_INDEX_ID=?,  REFERENCE_RATE_INDEX_TENOR=?, DAY_COUNT_CONVENTION_ID=?, NOTIONAL=?, CURRENCY_ID=?, NAME=?, EXCHANGE_ID=?, MATURITY_DATES_DATE_RULE_ID=? WHERE ID=?")) {
			if (fcs.getId() != 0) {
				stmtSaveFutureContractSpecification.setLong(9, fcs.getId());
			}
			stmtSaveFutureContractSpecification.setLong(1, fcs.getReferenceRateIndex().getId());
			stmtSaveFutureContractSpecification.setString(2, fcs.getReferenceRateIndexTenor().name());
			stmtSaveFutureContractSpecification.setLong(3, fcs.getDayCountConvention().getId());
			stmtSaveFutureContractSpecification.setBigDecimal(4, fcs.getNotional());
			stmtSaveFutureContractSpecification.setLong(5, fcs.getCurrency().getId());
			stmtSaveFutureContractSpecification.setString(6, fcs.getName());
			stmtSaveFutureContractSpecification.setLong(7, fcs.getExchange().getId());
			stmtSaveFutureContractSpecification.setLong(8, fcs.getMaturityDatesDateRule().getId());
			stmtSaveFutureContractSpecification.executeUpdate();

			if (fcs.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveFutureContractSpecification.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						fcsId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating currency failed, no generated key obtained.");
					}
				}
			} else {
				fcsId = fcs.getId();
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		fcs.setId(fcsId);
		return fcsId;
	}

}