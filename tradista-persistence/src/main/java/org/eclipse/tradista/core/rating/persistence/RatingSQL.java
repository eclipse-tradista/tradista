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
package org.eclipse.tradista.core.rating.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.rating.model.RatedObject;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.model.RatingAssignment;

public class RatingSQL {

	private static final Field RATING_AGENCY_ID_FIELD = new Field(TradistaDBConstants.ID);
	private static final Field RATING_AGENCY_NAME_FIELD = new Field(TradistaDBConstants.NAME);
	private static final Field RATING_AGENCY_ACTIVE_FIELD = new Field(TradistaDBConstants.ACTIVE);

	public static final Table RATING_AGENCY_TABLE = new Table("RATING_AGENCY",
			new Field[] { RATING_AGENCY_ID_FIELD, RATING_AGENCY_NAME_FIELD, RATING_AGENCY_ACTIVE_FIELD });
	private static final String SELECT_RATING_AGENCY_QUERY = TradistaDBUtil.buildSelectQuery(RATING_AGENCY_TABLE);

	private static final Field RATING_ID_FIELD = new Field(TradistaDBConstants.ID);
	private static final Field RATING_CODE_FIELD = new Field(TradistaDBConstants.CODE);
	private static final Field RATING_DESCRIPTION_FIELD = new Field(TradistaDBConstants.DESCRIPTION);
	private static final Field RATING_AGENCY_FK_FIELD = new Field("RATING_AGENCY_ID");

	public static final Table RATING_TABLE = new Table("RATING",
			new Field[] { RATING_ID_FIELD, RATING_CODE_FIELD, RATING_DESCRIPTION_FIELD, RATING_AGENCY_FK_FIELD });
	private static final String SELECT_RATING_QUERY = TradistaDBUtil.buildSelectQuery(RATING_TABLE);

	private static final Field ASSIGNMENT_ID_FIELD = new Field(TradistaDBConstants.ID);
	private static final Field RATED_OBJECT_ID_FIELD = new Field("RATED_OBJECT_ID");
	private static final Field RATED_OBJECT_TYPE_FIELD = new Field("RATED_OBJECT_TYPE");
	private static final Field ASSIGNMENT_RATING_ID_FIELD = new Field("RATING_ID");
	private static final Field VALID_FROM_FIELD = new Field("VALID_FROM");
	private static final Field VALID_TO_FIELD = new Field("VALID_TO");

	public static final Table RATING_ASSIGNMENT_TABLE = new Table("RATING_ASSIGNMENT",
			new Field[] { ASSIGNMENT_ID_FIELD, RATED_OBJECT_ID_FIELD, RATED_OBJECT_TYPE_FIELD,
					ASSIGNMENT_RATING_ID_FIELD, VALID_FROM_FIELD, VALID_TO_FIELD });
	private static final String SELECT_RATING_ASSIGNMENT_QUERY = TradistaDBUtil
			.buildSelectQuery(RATING_ASSIGNMENT_TABLE);

	public static long saveRatingAgency(RatingAgency ratingAgency) {
		long id = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveRatingAgency = (ratingAgency.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, RATING_AGENCY_TABLE,
								new Field[] { RATING_AGENCY_NAME_FIELD, RATING_AGENCY_ACTIVE_FIELD })
						: TradistaDBUtil.buildUpdatePreparedStatement(con, RATING_AGENCY_ID_FIELD, RATING_AGENCY_TABLE,
								new Field[] { RATING_AGENCY_NAME_FIELD, RATING_AGENCY_ACTIVE_FIELD })) {

			if (ratingAgency.getId() != 0) {
				stmtSaveRatingAgency.setLong(3, ratingAgency.getId());
			}
			stmtSaveRatingAgency.setString(1, ratingAgency.getName());
			stmtSaveRatingAgency.setBoolean(2, ratingAgency.isActive());

			stmtSaveRatingAgency.executeUpdate();

			if (ratingAgency.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveRatingAgency.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						id = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating rating agency failed, no generated key obtained.");
					}
				}
			} else {
				id = ratingAgency.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		ratingAgency.setId(id);
		return id;
	}

	public static boolean deleteRatingAgency(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = TradistaDBUtil.buildUpdatePreparedStatement(con, RATING_AGENCY_ID_FIELD,
						RATING_AGENCY_TABLE, new Field[] { RATING_AGENCY_ACTIVE_FIELD })) {
			stmt.setBoolean(1, false);
			stmt.setLong(2, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static boolean hardDeleteRatingAgency(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = TradistaDBUtil.buildDeletePreparedStatement(con, RATING_AGENCY_TABLE,
						RATING_AGENCY_ID_FIELD)) {
			stmt.setLong(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<RatingAgency> getAllRatingAgencies() {
		Set<RatingAgency> ratingAgencies = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllRatingAgencies = con.prepareStatement(SELECT_RATING_AGENCY_QUERY);
				ResultSet results = stmtGetAllRatingAgencies.executeQuery()) {
			while (results.next()) {
				if (ratingAgencies == null) {
					ratingAgencies = new HashSet<RatingAgency>();
				}
				RatingAgency ratingAgency = new RatingAgency(results.getString(RATING_AGENCY_NAME_FIELD.getName()));
				ratingAgency.setId(results.getLong(RATING_AGENCY_ID_FIELD.getName()));
				ratingAgency.setActive(results.getBoolean(RATING_AGENCY_ACTIVE_FIELD.getName()));
				ratingAgencies.add(ratingAgency);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ratingAgencies;
	}

	public static RatingAgency getRatingAgencyById(long id) {
		RatingAgency ratingAgency = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_AGENCY_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATING_AGENCY_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetRatingAgencyById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetRatingAgencyById.setLong(1, id);
			try (ResultSet results = stmtGetRatingAgencyById.executeQuery()) {
				while (results.next()) {
					ratingAgency = new RatingAgency(results.getString(RATING_AGENCY_NAME_FIELD.getName()));
					ratingAgency.setId(results.getLong(RATING_AGENCY_ID_FIELD.getName()));
					ratingAgency.setActive(results.getBoolean(RATING_AGENCY_ACTIVE_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ratingAgency;
	}

	public static RatingAgency getRatingAgencyByName(String name) {
		RatingAgency ratingAgency = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_AGENCY_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATING_AGENCY_NAME_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetRatingAgencyByName = con.prepareStatement(sqlQuery.toString())) {
			stmtGetRatingAgencyByName.setString(1, name);
			try (ResultSet results = stmtGetRatingAgencyByName.executeQuery()) {
				while (results.next()) {
					ratingAgency = new RatingAgency(results.getString(RATING_AGENCY_NAME_FIELD.getName()));
					ratingAgency.setId(results.getLong(RATING_AGENCY_ID_FIELD.getName()));
					ratingAgency.setActive(results.getBoolean(RATING_AGENCY_ACTIVE_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ratingAgency;
	}

	public static long saveRating(Rating rating) {
		long id = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveRating = (rating.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, RATING_TABLE,
								new Field[] { RATING_CODE_FIELD, RATING_DESCRIPTION_FIELD, RATING_AGENCY_FK_FIELD })
						: TradistaDBUtil.buildUpdatePreparedStatement(con, RATING_ID_FIELD, RATING_TABLE,
								new Field[] { RATING_CODE_FIELD, RATING_DESCRIPTION_FIELD, RATING_AGENCY_FK_FIELD })) {

			if (rating.getId() != 0) {
				stmtSaveRating.setLong(4, rating.getId());
			}

			stmtSaveRating.setString(1, rating.getCode());
			if (rating.getDescription() != null) {
				stmtSaveRating.setString(2, rating.getDescription());
			} else {
				stmtSaveRating.setNull(2, Types.VARCHAR);
			}
			stmtSaveRating.setLong(3, rating.getAgency().getId());

			stmtSaveRating.executeUpdate();

			if (rating.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveRating.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						id = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating rating failed, no generated key obtained.");
					}
				}
			} else {
				id = rating.getId();
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		rating.setId(id);
		return id;
	}

	public static boolean deleteRating(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = TradistaDBUtil.buildDeletePreparedStatement(con, RATING_TABLE,
						RATING_ID_FIELD)) {
			stmt.setLong(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<Rating> getAllRatings() {
		Set<Rating> ratings = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllRatings = con.prepareStatement(SELECT_RATING_QUERY);
				ResultSet results = stmtGetAllRatings.executeQuery()) {
			while (results.next()) {
				if (ratings == null) {
					ratings = new HashSet<Rating>();
				}
				RatingAgency agency = getRatingAgencyById(results.getLong(RATING_AGENCY_FK_FIELD.getName()));
				Rating rating = new Rating(results.getString(RATING_CODE_FIELD.getName()), agency);
				rating.setId(results.getLong(RATING_ID_FIELD.getName()));
				rating.setDescription(results.getString(RATING_DESCRIPTION_FIELD.getName()));
				ratings.add(rating);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ratings;
	}

	public static Set<Rating> getRatingsByAgencyId(long agencyId) {
		Set<Rating> ratings = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATING_AGENCY_FK_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetRatings = con.prepareStatement(sqlQuery.toString())) {
			stmtGetRatings.setLong(1, agencyId);
			try (ResultSet results = stmtGetRatings.executeQuery()) {
				while (results.next()) {
					if (ratings == null) {
						ratings = new HashSet<Rating>();
					}
					RatingAgency agency = getRatingAgencyById(results.getLong(RATING_AGENCY_FK_FIELD.getName()));
					Rating rating = new Rating(results.getString(RATING_CODE_FIELD.getName()), agency);
					rating.setId(results.getLong(RATING_ID_FIELD.getName()));
					rating.setDescription(results.getString(RATING_DESCRIPTION_FIELD.getName()));
					ratings.add(rating);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ratings;
	}

	public static Rating getRatingById(long id) {
		Rating rating = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATING_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetRatingById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetRatingById.setLong(1, id);
			try (ResultSet results = stmtGetRatingById.executeQuery()) {
				while (results.next()) {
					RatingAgency agency = getRatingAgencyById(results.getLong(RATING_AGENCY_FK_FIELD.getName()));
					rating = new Rating(results.getString(RATING_CODE_FIELD.getName()), agency);
					rating.setId(results.getLong(RATING_ID_FIELD.getName()));
					rating.setDescription(results.getString(RATING_DESCRIPTION_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return rating;
	}

	public static long saveRatingAssignment(RatingAssignment ratingAssignment) {
		long id = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSave = (ratingAssignment.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, RATING_ASSIGNMENT_TABLE,
								new Field[] { RATED_OBJECT_ID_FIELD, RATED_OBJECT_TYPE_FIELD,
										ASSIGNMENT_RATING_ID_FIELD, VALID_FROM_FIELD, VALID_TO_FIELD })
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ASSIGNMENT_ID_FIELD, RATING_ASSIGNMENT_TABLE,
								new Field[] { RATED_OBJECT_ID_FIELD, RATED_OBJECT_TYPE_FIELD,
										ASSIGNMENT_RATING_ID_FIELD, VALID_FROM_FIELD, VALID_TO_FIELD })) {

			if (ratingAssignment.getId() != 0) {
				stmtSave.setLong(6, ratingAssignment.getId());
			}

			stmtSave.setLong(1, ratingAssignment.getRatedObject().getId());
			stmtSave.setString(2, ratingAssignment.getRatedObject().getClass().getSimpleName());
			stmtSave.setLong(3, ratingAssignment.getRating().getId());
			stmtSave.setDate(4, Date.valueOf(ratingAssignment.getValidFrom()));
			if (ratingAssignment.getValidTo() != null) {
				stmtSave.setDate(5, Date.valueOf(ratingAssignment.getValidTo()));
			} else {
				stmtSave.setNull(5, java.sql.Types.DATE);
			}

			stmtSave.executeUpdate();

			if (ratingAssignment.getId() == 0) {
				try (ResultSet generatedKeys = stmtSave.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						id = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating rating assignment failed, no generated key obtained.");
					}
				}
			} else {
				id = ratingAssignment.getId();
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		ratingAssignment.setId(id);
		return id;
	}

	public static boolean deleteRatingAssignment(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = TradistaDBUtil.buildDeletePreparedStatement(con, RATING_ASSIGNMENT_TABLE,
						ASSIGNMENT_ID_FIELD)) {
			stmt.setLong(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<RatingAssignment> getRatingAssignmentsByRatedObjectId(long ratedObjectId,
			String ratedObjectType) {
		Set<RatingAssignment> assignments = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_ASSIGNMENT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATED_OBJECT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, RATED_OBJECT_TYPE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sqlQuery.toString())) {
			stmt.setLong(1, ratedObjectId);
			stmt.setString(2, ratedObjectType);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (assignments == null) {
						assignments = new HashSet<RatingAssignment>();
					}
					RatingAssignment assignment = new RatingAssignment();
					assignment.setId(results.getLong(ASSIGNMENT_ID_FIELD.getName()));
					assignment.setRating(getRatingById(results.getLong(ASSIGNMENT_RATING_ID_FIELD.getName())));
					assignment.setValidFrom(results.getDate(VALID_FROM_FIELD.getName()).toLocalDate());
					java.sql.Date validToDate = results.getDate(VALID_TO_FIELD.getName());
					if (validToDate != null) {
						assignment.setValidTo(validToDate.toLocalDate());
					}

					RatedObject ratedObject = (RatedObject) ProductSQL.getProductById(ratedObjectId);
					assignment.setRatedObject(ratedObject);

					assignments.add(assignment);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return assignments;
	}

	public static Set<RatingAssignment> getRatingAssignmentsByRatingId(long ratingId) {
		Set<RatingAssignment> assignments = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_RATING_ASSIGNMENT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, ASSIGNMENT_RATING_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sqlQuery.toString())) {
			stmt.setLong(1, ratingId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (assignments == null) {
						assignments = new HashSet<RatingAssignment>();
					}
					RatingAssignment assignment = new RatingAssignment();
					assignment.setId(results.getLong(ASSIGNMENT_ID_FIELD.getName()));
					assignment.setRating(getRatingById(results.getLong(ASSIGNMENT_RATING_ID_FIELD.getName())));
					assignment.setValidFrom(results.getDate(VALID_FROM_FIELD.getName()).toLocalDate());
					java.sql.Date validToDate = results.getDate(VALID_TO_FIELD.getName());
					if (validToDate != null) {
						assignment.setValidTo(validToDate.toLocalDate());
					}

					RatedObject ratedObject = (RatedObject) ProductSQL
							.getProductById(results.getLong(RATED_OBJECT_ID_FIELD.getName()));
					assignment.setRatedObject(ratedObject);

					assignments.add(assignment);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return assignments;
	}
}
