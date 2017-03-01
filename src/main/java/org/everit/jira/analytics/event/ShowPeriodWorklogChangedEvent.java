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
package org.everit.jira.analytics.event;

import java.util.Objects;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

/**
 * Period Worklog usage changed (on/off) event.
 */
public class ShowPeriodWorklogChangedEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/TimetrackerUserSettingsWebAction!default.jspa";

  private static final String EVENT_ACTION = "Period";

  private static final String EVENT_CATEGORY = "User";

  private static final String EVENT_NAME_OFF = "periodOff";

  private static final String EVENT_NAME_ON = "periodOn";

  private final String hashUserId;

  private final String pluginId;

  private final boolean showPeriodWorklog;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param showPeriodWorklog
   *          the status of remaining estimate options or not..
   */
  public ShowPeriodWorklogChangedEvent(final String pluginId,
      final boolean showPeriodWorklog) {
    this.pluginId = Objects.requireNonNull(pluginId);
    this.showPeriodWorklog = showPeriodWorklog;
    hashUserId = JiraTimetrackerAnalytics.getUserId();
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addEventName(showPeriodWorklog
                ? EVENT_NAME_ON
                : EVENT_NAME_OFF)
            .buildUrl();
  }

}
