package org.eclipse.tradista.ai.reasoning.common.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.ai.reasoning.common.model.Formula;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

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

public class FormulaSQL {

	public static long saveFormula(Formula formula) {
		long formulaId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFolFormula = (formula.getId() == 0)
						? con.prepareStatement("INSERT INTO FORMULA(FORMULA) VALUES (?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE FORMULA SET FORMULA=? WHERE ID = ? ")) {
			if (formula.getId() != 0) {
				stmtSaveFolFormula.setLong(3, formula.getId());
			}
			stmtSaveFolFormula.setString(1, formula.getFormula());
			stmtSaveFolFormula.executeUpdate();

			if (formula.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveFolFormula.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						formulaId = generatedKeys.getLong(1);
						formula.setId(formulaId);
					} else {
						throw new SQLException("Creating formula failed, no generated key obtained.");
					}
				}
			} else {
				formulaId = formula.getId();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return formulaId;
	}

	public static List<Formula> getAllFormulas() {
		List<Formula> formulas = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFormulas = con.prepareStatement("SELECT * FROM FORMULA");
				ResultSet results = stmtGetFormulas.executeQuery()) {
			while (results.next()) {
				if (formulas == null) {
					formulas = new ArrayList<Formula>();
				}
				long id = results.getLong("id");
				String formula = results.getString("formula");
				Formula frm = new Formula(formula, id);
				formulas.add(frm);
			}

		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return formulas;
	}

	public static boolean saveFormulas(Formula... formulas) {
		boolean saved = true;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFormula = con.prepareStatement("INSERT INTO FORMULA(FORMULA) VALUES (?) ",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtUpdateFormula = con
						.prepareStatement("UPDATE FORMULA SET FORMULA=? WHERE ID = ? ")) {
			for (Formula formula : formulas) {
				if (formula.getId() == 0) {
					stmtSaveFormula.setString(1, formula.getFormula());
					stmtSaveFormula.addBatch();
				} else {
					stmtUpdateFormula.setString(1, formula.getFormula());
					stmtUpdateFormula.setLong(2, formula.getId());
					stmtUpdateFormula.addBatch();
				}
			}

			stmtSaveFormula.executeBatch();
			stmtUpdateFormula.executeBatch();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			saved = false;
			throw new TradistaTechnicalException(sqle);
		}
		return saved;

	}

}