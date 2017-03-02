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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.NoEstimateUsageChangedEvent;
import org.everit.jira.analytics.event.NonWorkingUsageEvent;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.IssueEstimatedTimeChecker2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

/**
 * Responsible to initialize plugin when activated bean. Furthermore responsible to destroy when
 * deactivated bean.
 */
public class InitializerComponent implements InitializingBean, DisposableBean {

  private static final int DEFAULT_CHECK_TIME_IN_MINUTES = 1200;

  private static JobRunnerKey jobRunerKey = JobRunnerKey.of("issueEstimatedTimeChecker");

  private static final int MINUTES_IN_HOUR = 60;

  private static final long ONE_DAY_IN_MILISEC = 86400000L;

  private static final int ONE_DAY_IN_MINUTES = 1440;

  private static final String UNKNOW_USER_NAME = "UNKNOW_USER_NAME";

  private AnalyticsSender analyticsSender;

  private ScheduledFuture<?> issueEstimatedTimeCheckerFuture;

  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(1);

  private SchedulerService schedulerService;

  private TimeTrackerSettingsHelper settingsHelper;

  public InitializerComponent(final AnalyticsSender analyticsSender,
      final TimeTrackerSettingsHelper settingsHelper, final SchedulerService schedulerService) {
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
    this.schedulerService = schedulerService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // final Runnable issueEstimatedTimeChecker = new IssueEstimatedTimeChecker(
    // settingsHelper);
    // TODO add global settings the time
    JobId jobId = JobId.of("issueEstimatedTimeCheckerJobId");
    schedulerService.registerJobRunner(jobRunerKey,
        new IssueEstimatedTimeChecker2(settingsHelper));
    schedulerService.scheduleJob(jobId,
        JobConfig.forJobRunnerKey(jobRunerKey)
            .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
            .withSchedule(Schedule.forInterval(ONE_DAY_IN_MILISEC,
                // TODO calculate based on global settings
                new Date(System.currentTimeMillis() + ONE_DAY_IN_MILISEC))));
    // issueEstimatedTimeCheckerFuture = scheduledExecutorService
    // .scheduleAtFixedRate(issueEstimatedTimeChecker,
    // calculateInitialDelay(),
    // ONE_DAY_IN_MINUTES, TimeUnit.MINUTES);

    sendNonEstAndNonWorkAnaliticsEvent();
  }

  @Override
  public void destroy() throws Exception {
    issueEstimatedTimeCheckerFuture.cancel(true);
    schedulerService.unregisterJobRunner(jobRunerKey);
  }

  private void sendNonEstAndNonWorkAnaliticsEvent() {
    TimeTrackerGlobalSettings loadGlobalSettings = settingsHelper.loadGlobalSettings();
    List<Pattern> tempIssuePatterns = loadGlobalSettings.getIssuePatterns();
    List<Pattern> tempSummaryFilter = loadGlobalSettings.getNonWorkingIssuePatterns();
    String pluginId = loadGlobalSettings.getPluginUUID();

    NoEstimateUsageChangedEvent analyticsEvent =
        new NoEstimateUsageChangedEvent(pluginId, tempIssuePatterns, UNKNOW_USER_NAME);
    analyticsSender.send(analyticsEvent);
    NonWorkingUsageEvent nonWorkingUsageEvent =
        new NonWorkingUsageEvent(pluginId,
            tempSummaryFilter.isEmpty(), UNKNOW_USER_NAME);
    analyticsSender.send(nonWorkingUsageEvent);
  }

}
