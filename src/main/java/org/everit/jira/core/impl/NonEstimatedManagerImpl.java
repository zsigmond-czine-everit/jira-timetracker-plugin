/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.core.impl;

import java.util.Date;

import org.everit.jira.core.NonEstimatedReminderManager;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.timetracker.plugin.IssueEstimatedTimeChecker2;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

/**
 * Implementation of {@link NonEstimatedReminderManager} interface.
 *
 */
public class NonEstimatedManagerImpl implements NonEstimatedReminderManager {

  private static JobRunnerKey jobRunerKey = JobRunnerKey.of("issueEstimatedTimeChecker");

  private static final long ONE_DAY_IN_MILISEC = 86400000L;

  private SchedulerService schedulerService;

  private TimeTrackerSettingsHelper settingsHelper;

  public NonEstimatedManagerImpl(final SchedulerService schedulerService,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.schedulerService = schedulerService;
    this.settingsHelper = settingsHelper;
  }

  @Override
  public void registerNonEstimatedReminder() throws SchedulerServiceException {
    int nonEstimatedRemindTime = settingsHelper.loadGlobalSettings().getNonEstimatedRemindTime();
    JobId jobId = JobId.of("issueEstimatedTimeCheckerJobId");

    schedulerService.registerJobRunner(jobRunerKey,
        new IssueEstimatedTimeChecker2(settingsHelper));
    long calculatedFirstScheduleTime = DateTimeConverterUtil
        .calculateTimeBetweenTimeAndDayTime(System.currentTimeMillis(), nonEstimatedRemindTime);
    schedulerService.scheduleJob(jobId,
        JobConfig.forJobRunnerKey(jobRunerKey)
            .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
            .withSchedule(Schedule.forInterval(ONE_DAY_IN_MILISEC,
                new Date(System.currentTimeMillis() + calculatedFirstScheduleTime))));

  }

  @Override
  public void unregisterNonEstimatedReminder() {
    schedulerService.unregisterJobRunner(jobRunerKey);
  }

}
