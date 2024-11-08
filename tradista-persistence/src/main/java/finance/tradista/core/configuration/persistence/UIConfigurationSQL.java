package org.eclipse.tradista.core.configuration.persistence;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.model.UIConfiguration;
import org.eclipse.tradista.core.user.model.User;

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

public class UIConfigurationSQL {

	public static UIConfiguration getUIConfiguration(User user) {
		UIConfiguration uiConfiguration = null;
		DecimalFormat uiDecimalFormat = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUIConfiguration = con
						.prepareStatement("SELECT * FROM UI_CONFIGURATION WHERE USER_ID = ?")) {
			stmtGetUIConfiguration.setLong(1, user.getId());
			try (ResultSet results = stmtGetUIConfiguration.executeQuery()) {
				while (results.next()) {
					uiConfiguration = new UIConfiguration(user);
					uiDecimalFormat = new DecimalFormat();
					uiDecimalFormat.setParseBigDecimal(true);
					DecimalFormatSymbols dfs = new DecimalFormatSymbols();
					String decimalSeparator = results.getString("decimal_separator");
					dfs.setDecimalSeparator(decimalSeparator.charAt(0));
					String groupingSeparator = results.getString("grouping_separator");
					dfs.setGroupingSeparator(groupingSeparator.charAt(0));
					uiDecimalFormat.setDecimalFormatSymbols(dfs);
					short decimalDigits = results.getShort("decimal_digits");
					if (decimalDigits != 0) {
						uiDecimalFormat.setMaximumFractionDigits(decimalDigits);
					}
					String roundingMode = results.getString("rounding_mode");
					if (roundingMode != null) {
						uiDecimalFormat.setRoundingMode(RoundingMode.valueOf(roundingMode));
					}
					uiConfiguration.setDecimalFormat(uiDecimalFormat);
					uiConfiguration.setStyle(results.getString("style"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return uiConfiguration;
	}

	public static void saveUIConfiguration(UIConfiguration uiConfiguration) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtCountUIConfiguration = con
						.prepareStatement("SELECT COUNT(1) FROM UI_CONFIGURATION");
				ResultSet results = stmtCountUIConfiguration.executeQuery()) {
			short count = 0;
			while (results.next()) {
				count = results.getShort(1);
			}
			try (PreparedStatement stmtSaveUIConfiguration = (count > 0) ? con.prepareStatement(
					"UPDATE UI_CONFIGURATION SET DECIMAL_SEPARATOR=?, GROUPING_SEPARATOR=?, ROUNDING_MODE=?, DECIMAL_DIGITS=?, STYLE=?, USER_ID=?")
					: con.prepareStatement(
							"INSERT INTO UI_CONFIGURATION(DECIMAL_SEPARATOR, GROUPING_SEPARATOR, ROUNDING_MODE, DECIMAL_DIGITS, STYLE, USER_ID) VALUES (?, ?, ?, ?, ?, ?) ")) {
				stmtSaveUIConfiguration.setString(1, String
						.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator()));
				stmtSaveUIConfiguration.setString(2, String
						.valueOf(uiConfiguration.getDecimalFormat().getDecimalFormatSymbols().getGroupingSeparator()));
				if (uiConfiguration.getDecimalFormat().getRoundingMode() != null) {
					stmtSaveUIConfiguration.setString(3, uiConfiguration.getDecimalFormat().getRoundingMode().name());
				} else {
					stmtSaveUIConfiguration.setNull(3, java.sql.Types.VARCHAR);
				}
				if (uiConfiguration.getDecimalFormat().getMaximumFractionDigits() != 0) {
					stmtSaveUIConfiguration.setShort(4,
							(short) uiConfiguration.getDecimalFormat().getMaximumFractionDigits());
				} else {
					stmtSaveUIConfiguration.setNull(4, java.sql.Types.VARCHAR);
				}
				stmtSaveUIConfiguration.setString(5, uiConfiguration.getStyle());
				stmtSaveUIConfiguration.setLong(6, uiConfiguration.getUser().getId());
				stmtSaveUIConfiguration.executeUpdate();
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

}