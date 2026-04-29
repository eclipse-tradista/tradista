package org.eclipse.tradista.core.batch.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.quartz.JobKey;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class BatchSQL {

	private static final Field ID_FIELD = new Field("ID");
	private static final Field TRIGGER_FIRE_INSTANCE_ID_FIELD = new Field("TRIGGER_FIRE_INSTANCE_ID");
	private static final Field STATUS_FIELD = new Field("STATUS");
	private static final Field START_TIME_FIELD = new Field("START_TIME");
	private static final Field END_TIME_FIELD = new Field("END_TIME");
	private static final Field ERROR_CAUSE_FIELD = new Field("ERROR_CAUSE");
	private static final Field JOB_INSTANCE_NAME_FIELD = new Field("JOB_INSTANCE_NAME");
	private static final Field JOB_TYPE_FIELD = new Field("JOB_TYPE");
	private static final Field PROCESSING_ORG_FIELD = new Field("PROCESSING_ORG");

	private static final Field[] FIELDS = { ID_FIELD, TRIGGER_FIRE_INSTANCE_ID_FIELD, STATUS_FIELD, START_TIME_FIELD,
			END_TIME_FIELD, ERROR_CAUSE_FIELD, JOB_INSTANCE_NAME_FIELD, JOB_TYPE_FIELD, PROCESSING_ORG_FIELD };

	private static final Field[] FIELDS_FOR_INSERT = { TRIGGER_FIRE_INSTANCE_ID_FIELD, STATUS_FIELD, START_TIME_FIELD,
			JOB_INSTANCE_NAME_FIELD, JOB_TYPE_FIELD, PROCESSING_ORG_FIELD };

	private static final Field[] FIELDS_FOR_UPDATE = { STATUS_FIELD, END_TIME_FIELD, ERROR_CAUSE_FIELD };

	public static final Table TABLE = new Table("JOB_EXECUTION", FIELDS);

	public static long saveJobExecution(String name, String po, String status, LocalDateTime startTime,
			LocalDateTime endTime, String errorCause, String jobInstanceName, String jobType) {
		long jobExecutionId = 0;
		try (Connection con = TradistaDB.getConnection()) {
			if (status.equals("IN PROGRESS")) {
				try (PreparedStatement stmtSaveJobExecution = TradistaDBUtil.buildInsertPreparedStatement(con, TABLE,
						FIELDS_FOR_INSERT)) {
					stmtSaveJobExecution.setString(1, name);
					stmtSaveJobExecution.setString(2, status);
					stmtSaveJobExecution.setTimestamp(3, Timestamp.valueOf(startTime));
					stmtSaveJobExecution.setString(4, jobInstanceName);
					stmtSaveJobExecution.setString(5, jobType);
					stmtSaveJobExecution.setString(6, po);
					stmtSaveJobExecution.executeUpdate();

					try (ResultSet generatedKeys = stmtSaveJobExecution.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							jobExecutionId = generatedKeys.getLong(1);
						} else {
							throw new SQLException("Creating job execution failed, no generated key obtained.");
						}
					}
				}
			} else {
				// Job stopped
				try (PreparedStatement stmtUpdateJobExecution = TradistaDBUtil.buildUpdatePreparedStatement(con,
						TRIGGER_FIRE_INSTANCE_ID_FIELD, TABLE, FIELDS_FOR_UPDATE)) {
					stmtUpdateJobExecution.setString(1, status);
					stmtUpdateJobExecution.setTimestamp(2, Timestamp.valueOf(endTime));
					stmtUpdateJobExecution.setString(3, errorCause);
					stmtUpdateJobExecution.setString(4, name);

					stmtUpdateJobExecution.executeUpdate();
				}
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecutionId;
	}

	private static TradistaJobExecution buildJobExecution(ResultSet results) throws SQLException {
		SimpleTriggerImpl trigger = new SimpleTriggerImpl();
		String po = results.getString(PROCESSING_ORG_FIELD.getName());
		trigger.setEndTime(results.getDate(END_TIME_FIELD.getName()));
		String jobInstanceName = results.getString(JOB_INSTANCE_NAME_FIELD.getName());
		JobKey jobKey = new JobKey(jobInstanceName, po);
		trigger.setJobKey(jobKey);
		trigger.setJobName(jobInstanceName);
		String triggerName = results.getString(TRIGGER_FIRE_INSTANCE_ID_FIELD.getName());
		trigger.setStartTime(results.getDate(START_TIME_FIELD.getName()));
		TradistaJobInstance jobInstance = null;
		try {
			jobInstance = new BatchBusinessDelegate().getJobInstanceByNameAndPo(jobInstanceName, po);
		} catch (TradistaBusinessException _) {
		}
		TradistaJobExecution jobExecution = new TradistaJobExecution(trigger, jobInstance, triggerName);
		jobExecution.setId(results.getLong(ID_FIELD.getName()));
		jobExecution.setStatus(results.getString(STATUS_FIELD.getName()));
		jobExecution.setStartTime(results.getTimestamp(START_TIME_FIELD.getName()).toLocalDateTime());
		jobExecution.setJobType(results.getString(JOB_TYPE_FIELD.getName()));
		Timestamp endTime = results.getTimestamp(END_TIME_FIELD.getName());
		if (endTime != null) {
			jobExecution.setEndTime(endTime.toLocalDateTime());
		}
		jobExecution.setErrorCause(results.getString(ERROR_CAUSE_FIELD.getName()));
		return jobExecution;
	}

	public static Set<TradistaJobExecution> getJobExecutions(LocalDate date, String po) {
		Set<TradistaJobExecution> jobExecutions = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		sqlQuery.append(" WHERE START_TIME BETWEEN ? AND ?");
		if (po != null) {
			TradistaDBUtil.addParameterizedFilter(sqlQuery, PROCESSING_ORG_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetJobExecutions = con.prepareStatement(sqlQuery.toString())) {
			stmtGetJobExecutions.setDate(1, java.sql.Date.valueOf(date));
			stmtGetJobExecutions.setDate(2, java.sql.Date.valueOf(date.plusDays(1)));
			if (po != null) {
				stmtGetJobExecutions.setString(3, po);
			}
			try (ResultSet results = stmtGetJobExecutions.executeQuery()) {
				while (results.next()) {
					TradistaJobExecution jobExecution = buildJobExecution(results);
					if (jobExecutions == null) {
						jobExecutions = new HashSet<>();
					}
					jobExecutions.add(jobExecution);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecutions;
	}

	public static TradistaJobExecution getJobExecutionById(String jobExecutionId) {
		TradistaJobExecution jobExecution = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, TRIGGER_FIRE_INSTANCE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetJobExecution = con.prepareStatement(sqlQuery.toString())) {
			stmtGetJobExecution.setString(1, jobExecutionId);
			try (ResultSet results = stmtGetJobExecution.executeQuery()) {
				while (results.next()) {
					jobExecution = buildJobExecution(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecution;

	}

}