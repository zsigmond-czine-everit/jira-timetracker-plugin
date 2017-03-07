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

import java.util.List;
import java.util.regex.Pattern;

import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.NoEstimateUsageChangedEvent;
import org.everit.jira.analytics.event.NonWorkingUsageEvent;
import org.everit.jira.core.NonEstimatedReminderManager;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Responsible to initialize plugin when activated bean. Furthermore responsible to destroy when
 * deactivated bean.
 */
public class InitializerComponent implements InitializingBean, DisposableBean {

  private static final String UNKNOW_USER_NAME = "UNKNOW_USER_NAME";

  private AnalyticsSender analyticsSender;

  private NonEstimatedReminderManager nonEstimatedReminderManager;

  private TimeTrackerSettingsHelper settingsHelper;

  /**
   * Default constructor.
   */
  public InitializerComponent(final AnalyticsSender analyticsSender,
      final TimeTrackerSettingsHelper settingsHelper,
      final NonEstimatedReminderManager nonEstimatedReminderManager) {
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
    this.nonEstimatedReminderManager = nonEstimatedReminderManager;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    nonEstimatedReminderManager.registerNonEstimatedReminder();
    sendNonEstAndNonWorkAnaliticsEvent();
  }

  @Override
  public void destroy() throws Exception {
    nonEstimatedReminderManager.unregisterNonEstimatedReminder();
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
