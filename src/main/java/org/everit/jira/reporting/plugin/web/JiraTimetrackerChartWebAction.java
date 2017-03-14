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
package org.everit.jira.reporting.plugin.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.util.PermissionUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.ReportingQueryParameters;
import org.everit.jira.settings.dto.ReportingUserSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The Timetracker chart report action support class.
 */
public class JiraTimetrackerChartWebAction extends JiraWebActionSupport {

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private AnalyticsDTO analyticsDTO;

  private String contextPath;

  /**
   * The formated date.
   */
  private Long dateFromFormated;

  /**
   * The formated date.
   */
  private Long dateToFormated;

  private String display = "pie";

  private String groupBy = "project";

  public boolean hasBrowseUsersPermission = true;

  private String issueCollectorSrc;

  /**
   * The message.
   */

  private PluginCondition pluginCondition;

  private ReportingCondition reportingCondition;

  private TimeTrackerSettingsHelper settingsHelper;

  /**
   * Simple constructor.
   */
  public JiraTimetrackerChartWebAction(final TimeTrackerSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
    reportingCondition = new ReportingCondition(settingsHelper);
    pluginCondition = new PluginCondition(settingsHelper);
  }

  private void beforeAction() {
    normalizeContextPath();
    loadIssueCollectorSrc();

    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInUser(), settingsHelper);

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_CHART_SITEID,
        settingsHelper);
  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    return null;
  }

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    beforeAction();

    loadDataFromUserSettings();
    initDatesIfNecessary();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException, GenericEntityException {
    return doDefault();
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Long getDateFromFormated() {
    return dateFromFormated;
  }

  public Long getDateToFormated() {
    return dateToFormated;
  }

  public String getDisplay() {
    return display;
  }

  /**
   * Get end date for date picker.
   */
  public String getEndDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(dateToFormated));
  }

  /**
   * Get from date for date picker.
   */
  public String getFromDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(dateFromFormated));
  }

  public String getGroupBy() {
    return groupBy;
  }

  public boolean getHasBrowseUsersPermission() {
    return hasBrowseUsersPermission;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  private void initDatesIfNecessary() {
    if (dateFromFormated == null) {
      DateTime dateTimeFrom = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
      dateTimeFrom = dateTimeFrom.minusWeeks(1);
      dateFromFormated = DateTimeConverterUtil.convertDateTimeToDate(dateTimeFrom).getTime();
    }
    if (dateToFormated == null) {
      DateTime dateTimeTo = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
      dateToFormated = DateTimeConverterUtil.convertDateTimeToDate(dateTimeTo).getTime();
    }
  }

  private void loadDataFromUserSettings() {
    ReportingUserSettings loadReportingUserSettings = settingsHelper.loadReportingUserSettings();
    ReportingQueryParameters chartReportData = loadReportingUserSettings.getChartReportData();

    // TODO zs.cz save correct values to session!
    // currentUser = chartReportData.currentUser;
    dateFromFormated = chartReportData.dateFrom;
    dateToFormated = chartReportData.dateTo;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Decide render or not the update information bar.
   *
   * @return true if bar should be render
   */
  public boolean renderUpdateNotifier() {
    return new UpdateNotifier(settingsHelper)
        .isShowUpdater();
  }

  public void setGroupBy(final String groupby) {
    groupBy = groupby;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
