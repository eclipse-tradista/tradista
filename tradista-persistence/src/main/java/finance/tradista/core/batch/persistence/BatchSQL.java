package org.eclipse.tradista.core.batch.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.quartz.JobKey;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

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

	public static long saveJobExecution(String name, String po, String status, LocalDateTime startTime,
			LocalDateTime endTime, String errorCause, String jobInstanceName, String jobType) {
		long jobExecutionId = 0;
		try (Connection con = TradistaDB.getConnection()) {
			if (status.equals("IN PROGRESS")) {
				try (PreparedStatement stmtSaveJobExecution = con.prepareStatement(
						"INSERT INTO JOB_EXECUTION(TRIGGER_FIRE_INSTANCE_ID, STATUS, START_TIME, JOB_INSTANCE_NAME, JOB_TYPE, PROCESSING_ORG) VALUES(?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)) {
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
				try (PreparedStatement stmtUpdateJobExecution = con.prepareStatement(
						"UPDATE JOB_EXECUTION SET STATUS=?,END_TIME=?, ERROR_CAUSE=? WHERE TRIGGER_FIRE_INSTANCE_ID=? ")) {
					stmtUpdateJobExecution.setString(1, status);
					stmtUpdateJobExecution.setTimestamp(2, Timestamp.valueOf(endTime));
					stmtUpdateJobExecution.setString(3, errorCause);
					stmtUpdateJobExecution.setString(4, name);

					stmtUpdateJobExecution.executeUpdate();
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecutionId;
	}

	public static Set<TradistaJobExecution> getJobExecutions(LocalDate date, String po) {
		Set<TradistaJobExecution> jobExecutions = null;
		String sqlQuery = "SELECT * FROM JOB_EXECUTION WHERE START_TIME BETWEEN ? AND ?";
		if (po != null) {
			sqlQuery += " AND PROCESSING_ORG = ?";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetJobExecutions = con.prepareStatement(sqlQuery)) {
			stmtGetJobExecutions.setDate(1, java.sql.Date.valueOf(date));
			stmtGetJobExecutions.setDate(2, java.sql.Date.valueOf(date.plusDays(1)));
			if (po != null) {
				stmtGetJobExecutions.setString(3, po);
			}
			try (ResultSet results = stmtGetJobExecutions.executeQuery()) {
				while (results.next()) {
					SimpleTriggerImpl trigger = new SimpleTriggerImpl();
					trigger.setEndTime(results.getDate("end_time"));
					String jobInstanceName = results.getString("job_instance_name");
					JobKey jobKey = new JobKey(jobInstanceName, po);
					trigger.setJobKey(jobKey);
					trigger.setJobName(jobInstanceName);
					String triggerName = results.getString("trigger_fire_instance_id");
					trigger.setStartTime(results.getDate("start_time"));
					TradistaJobInstance jobInstance = null;
					try {
						jobInstance = new BatchBusinessDelegate().getJobInstanceByNameAndPo(jobInstanceName, po);
					} catch (TradistaBusinessException tbe) {
					}
					TradistaJobExecution jobExecution = new TradistaJobExecution(trigger, jobInstance, triggerName);
					jobExecution.setId(results.getLong("id"));
					jobExecution.setStatus(results.getString("status"));
					jobExecution.setStartTime(results.getTimestamp("start_time").toLocalDateTime());
					jobExecution.setJobType(results.getString("job_type"));
					Timestamp endTime = results.getTimestamp("end_time");
					if (endTime != null) {
						jobExecution.setEndTime(endTime.toLocalDateTime());
					}
					jobExecution.setErrorCause(results.getString("error_cause"));
					if (jobExecutions == null) {
						jobExecutions = new HashSet<TradistaJobExecution>();
					}
					jobExecutions.add(jobExecution);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecutions;
	}

	public static TradistaJobExecution getJobExecutionById(String jobExecutionId) {
		TradistaJobExecution jobExecution = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetJobExecution = con
						.prepareStatement("SELECT * FROM JOB_EXECUTION WHERE TRIGGER_FIRE_INSTANCE_ID  = ?")) {
			stmtGetJobExecution.setString(1, jobExecutionId);
			try (ResultSet results = stmtGetJobExecution.executeQuery()) {
				while (results.next()) {
					SimpleTriggerImpl trigger = new SimpleTriggerImpl();
					String po = results.getString("processing_org");
					trigger.setEndTime(results.getDate("end_time"));
					String jobInstanceName = results.getString("job_instance_name");
					JobKey jobKey = new JobKey(jobInstanceName, po);
					trigger.setJobKey(jobKey);
					trigger.setJobName(jobInstanceName);
					String triggerName = results.getString("trigger_fire_instance_id");
					trigger.setStartTime(results.getDate("start_time"));
					TradistaJobInstance jobInstance = null;
					try {
						jobInstance = new BatchBusinessDelegate().getJobInstanceByNameAndPo(jobInstanceName, po);
					} catch (TradistaBusinessException tbe) {
					}
					jobExecution = new TradistaJobExecution(trigger, jobInstance, triggerName);
					jobExecution.setId(results.getLong("id"));
					jobExecution.setStatus(results.getString("status"));
					jobExecution.setStartTime(results.getTimestamp("start_time").toLocalDateTime());
					jobExecution.setJobType(results.getString("job_type"));
					Timestamp endTime = results.getTimestamp("end_time");
					if (endTime != null) {
						jobExecution.setEndTime(endTime.toLocalDateTime());
					}
					jobExecution.setErrorCause(results.getString("error_cause"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return jobExecution;

	}

}