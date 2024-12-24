package org.eclipse.tradista.ai.reasoning.fol.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.ai.reasoning.fol.model.FolFormula;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

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

public class FolFormulaSQL {

	public static long saveFolFormula(FolFormula formula) {
		long formulaId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFolFormula = (formula.getId() == 0)
						? con.prepareStatement("INSERT INTO FOL_FORMULA(FORMULA) VALUES (?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE FOL_FORMULA SET FORMULA=? WHERE ID = ? ")) {
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
						throw new SQLException("Creating fol formula failed, no generated key obtained.");
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

	public static List<FolFormula> getAllFolFormulas() {
		List<FolFormula> folFormulas = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFolFormulas = con.prepareStatement("SELECT * FROM FOL_FORMULA");
				ResultSet results = stmtGetFolFormulas.executeQuery()) {
			while (results.next()) {
				if (folFormulas == null) {
					folFormulas = new ArrayList<FolFormula>();
				}
				long id = results.getLong("id");
				String formula = results.getString("formula");
				FolFormula folFormula = new FolFormula(formula, id);
				folFormulas.add(folFormula);
			}

		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return folFormulas;
	}

	public static boolean saveFolFormulas(FolFormula... formulas) {
		boolean saved = true;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFolFormula = con.prepareStatement(
						"INSERT INTO FOL_FORMULA(FORMULA) VALUES (?) ", Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtUpdateFolFormula = con
						.prepareStatement("UPDATE FOL_FORMULA SET FORMULA=? WHERE ID = ? ")) {
			for (FolFormula formula : formulas) {
				if (formula.getId() == 0) {
					stmtSaveFolFormula.setString(1, formula.getFormula());
					stmtSaveFolFormula.addBatch();
				} else {
					stmtUpdateFolFormula.setString(1, formula.getFormula());
					stmtUpdateFolFormula.setLong(2, formula.getId());
					stmtUpdateFolFormula.addBatch();
				}
			}

			stmtSaveFolFormula.executeBatch();
			stmtUpdateFolFormula.executeBatch();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			saved = false;
			throw new TradistaTechnicalException(sqle);
		}
		return saved;

	}

}