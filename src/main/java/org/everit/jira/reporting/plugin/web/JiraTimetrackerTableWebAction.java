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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.util.PermissionUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.ReportingQueryParameters;
import org.everit.jira.settings.dto.ReportingUserSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.ExceptionUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The Timetracker table report action support class.
 */
public class JiraTimetrackerTableWebAction extends JiraWebActionSupport {

  /**
   * HTTP parameters.
   */
  public static final class Parameter {

    public static final String DATEFROM = "dateFromMil";

    public static final String DATETO = "dateToMil";

    public static final String USERPICKER = "selectedUser";

  }

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String EXCEEDED_A_YEAR = "plugin.exceeded.year";

    public static final String INVALID_END_TIME = "plugin.invalid_endTime";

    public static final String INVALID_START_TIME = "plugin.invalid_startTime";

    public static final String INVALID_USER_PICKER = "plugin.user.picker.label";

    public static final String WRONG_DATES = "plugin.wrong.dates";
  }

  private static final String GET_WORKLOGS_ERROR_MESSAGE = "Error when trying to get worklogs.";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerTableWebAction.class);

  private static final String SELF_WITH_DATE_AND_USER_URL_FORMAT =
      "/secure/JiraTimetrackerTableWebAction.jspa"
          + "?dateFromMil=%s"
          + "&dateToMil=%s"
          + "&selectedUser=%s"
          + "&search";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private AnalyticsDTO analyticsDTO;

  private JiraRendererPlugin atlassianWikiRenderer;

  private String avatarURL = "";

  private String contextPath;

  private String currentUser = "";

  /**
   * The formated date.
   */
  private Long dateFromFormated;

  /**
   * The formated date.
   */
  private Long dateToFormated;

  public boolean hasBrowseUsersPermission = true;

  private String issueCollectorSrc;

  private IssueRenderContext issueRenderContext;

  private List<Pattern> issuesRegex;

  private DateTimeServer lastDate;

  /**
   * The message.
   */
  private String message = "";

  private PluginCondition pluginCondition;

  private ReportingCondition reportingCondition;

  private ReportingUserSettings reportingUserSettings;

  private TimeTrackerSettingsHelper settingsHelper;

  private String stacktrace = "";

  private DateTimeServer startDate;

  private transient ApplicationUser userPickerObject;

  private EVWorklogManager worklogManager;

  private List<EveritWorklog> worklogs;

  private WorkLogSummarizer workLogSummaryzer;

  /**
   * Simple constructor.
   */
  public JiraTimetrackerTableWebAction(
      final TimeTrackerSettingsHelper settingsHelper,
      final EVWorklogManager worklogManager) {
    this.settingsHelper = settingsHelper;
    this.worklogManager = worklogManager;
    reportingCondition = new ReportingCondition(settingsHelper);
    pluginCondition = new PluginCondition(settingsHelper);
    issueRenderContext = new IssueRenderContext(null);
    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    atlassianWikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
  }

  private void beforeAction() {
    loadIssueCollectorSrc();
    normalizeContextPath();
    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(),
            settingsHelper);
    issuesRegex = settingsHelper.loadGlobalSettings().getNonWorkingIssuePatterns();
    reportingUserSettings = settingsHelper.loadReportingUserSettings();
    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_TABLE_SITEID,
        settingsHelper);

  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
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

    boolean loadedFromSession = loadDataFromUserSettings();
    initDatesIfNecessary();
    initCurrentUserIfNecessary();

    if (loadedFromSession) {
      setReturnUrl(getFormattedRedirectUrl());
      return getRedirect(NONE);
    } else {
      return INPUT;
    }
  }

  @Override
  public String doExecute() throws ParseException, GenericEntityException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    beforeAction();

    parseParams();
    if (!"".equals(message)) {
      return INPUT;
    }
    try {
      validateDates(startDate, lastDate);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    worklogs = new ArrayList<>();
    try {
      worklogs.addAll(worklogManager.getWorklogs(currentUser, startDate, lastDate));
      saveDataToUserSettings();
    } catch (DataAccessException | ParseException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }
    workLogSummaryzer = new WorkLogSummarizer(worklogs, issuesRegex);
    workLogSummaryzer.makeSummary();

    return SUCCESS;

  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public JiraRendererPlugin getAtlassianWikiRenderer() {
    return atlassianWikiRenderer;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getCurrentUserEmail() {
    return currentUser;
  }

  public Long getDateFromFormated() {
    return dateFromFormated;
  }

  public Long getDateToFormated() {
    return dateToFormated;
  }

  public HashMap<Integer, List<Object>> getDaySum() {
    return workLogSummaryzer.getDaySum();
  }

  /**
   * Get end date for date picker.
   */
  public String getEndDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(dateToFormated));
  }

  private String getFormattedRedirectUrl() {
    String currentUserEncoded;
    try {
      currentUserEncoded = URLEncoder.encode(currentUser, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      currentUserEncoded = "";
    }
    return String.format(
        SELF_WITH_DATE_AND_USER_URL_FORMAT,
        dateFromFormated,
        dateToFormated,
        currentUserEncoded);
  }

  /**
   * Get from date for date picker.
   */
  public String getFromDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(dateFromFormated));
  }

  public boolean getHasBrowseUsersPermission() {
    return hasBrowseUsersPermission;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public IssueRenderContext getIssueRenderContext() {
    return issueRenderContext;
  }

  public List<Pattern> getIssuesRegex() {
    return issuesRegex;
  }

  public String getMessage() {
    return message;
  }

  public HashMap<Integer, List<Object>> getMonthSum() {
    return workLogSummaryzer.getMonthSum();
  }

  public HashMap<Integer, List<Object>> getRealDaySum() {
    return workLogSummaryzer.getRealDaySum();
  }

  public HashMap<Integer, List<Object>> getRealMonthSum() {
    return workLogSummaryzer.getRealMonthSum();
  }

  public HashMap<Integer, List<Object>> getRealWeekSum() {
    return workLogSummaryzer.getRealWeekSum();
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public ApplicationUser getUserPickerObject() {
    return userPickerObject;
  }

  public HashMap<Integer, List<Object>> getWeekSum() {
    return workLogSummaryzer.getWeekSum();
  }

  public List<EveritWorklog> getWorklogs() {
    return worklogs;
  }

  private void initCurrentUserIfNecessary() {
    if ("".equals(currentUser) || !hasBrowseUsersPermission) {
      JiraAuthenticationContext authenticationContext = ComponentAccessor
          .getJiraAuthenticationContext();
      currentUser = authenticationContext.getLoggedInUser().getUsername();
      setUserPickerObjectBasedOnCurrentUser();
    }
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

  private boolean loadDataFromUserSettings() {
    ReportingQueryParameters tableReportData = reportingUserSettings.getTableReportData();
    if (tableReportData.currentUser == null) {
      return false;
    }
    currentUser = tableReportData.currentUser;
    dateFromFormated = tableReportData.dateFrom;
    dateToFormated = tableReportData.dateTo;
    return true;
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

  private DateTimeServer parseDateFrom() throws IllegalArgumentException {
    String dateFromParam = getHttpRequest().getParameter(Parameter.DATEFROM);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFromFormated = Long.valueOf(dateFromParam);
      return DateTimeServer.getInstanceBasedOnUserTimeZone(dateFromFormated);
    } else {
      throw new IllegalArgumentException(PropertiesKey.INVALID_START_TIME);
    }
  }

  private DateTimeServer parseDateTo() throws IllegalArgumentException {
    String dateToParam = getHttpRequest().getParameter(Parameter.DATETO);
    if ((dateToParam != null) && !"".equals(dateToParam)) {
      dateToFormated = Long.valueOf(dateToParam);
      return DateTimeServer.getInstanceBasedOnUserTimeZone(dateToFormated);
    } else {
      throw new IllegalArgumentException(PropertiesKey.INVALID_END_TIME);
    }
  }

  private void parseParams() {
    try {
      startDate = parseDateFrom();
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
    }
    try {
      lastDate = parseDateTo();
    } catch (IllegalArgumentException e) {
      if ("".equals(message)) {
        message = e.getMessage();
      }
    }
    try {
      setCurrentUserFromParam();
      setUserPickerObjectBasedOnCurrentUser();
    } catch (IllegalArgumentException e) {
      if ("".equals(message)) {
        message = e.getMessage();
      }
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

  private void saveDataToUserSettings() {
    ReportingUserSettings reportingUserSettings = new ReportingUserSettings();
    reportingUserSettings.putTableReportData(
        new ReportingQueryParameters().currentUser(currentUser).dateFrom(dateFromFormated)
            .dateTo(dateToFormated));
    settingsHelper.saveReportingUserSettings(reportingUserSettings);
  }

  private void setCurrentUserFromParam() throws IllegalArgumentException {
    String selectedUser = getHttpRequest().getParameter(Parameter.USERPICKER);
    if (selectedUser == null) {
      throw new IllegalArgumentException(PropertiesKey.INVALID_USER_PICKER);
    }
    currentUser = selectedUser;
    if ("".equals(currentUser) || !hasBrowseUsersPermission) {
      JiraAuthenticationContext authenticationContext = ComponentAccessor
          .getJiraAuthenticationContext();
      currentUser = authenticationContext.getLoggedInUser().getKey();
    }
  }

  private void setUserPickerObjectBasedOnCurrentUser() {
    if (!"".equals(currentUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByName(currentUser);
      if (userPickerObject == null) {
        throw new IllegalArgumentException(PropertiesKey.INVALID_USER_PICKER);
      }
      AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      avatarURL = avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
          userPickerObject, Avatar.Size.SMALL).toString();
    } else {
      userPickerObject = null;
    }
  }

  private void validateDates(final DateTimeServer startDate, final DateTimeServer lastDate) {
    if (startDate.getUserTimeZone().isAfter(lastDate.getUserTimeZone())) {
      throw new IllegalArgumentException(PropertiesKey.WRONG_DATES);
    }

    DateTime yearCheck = lastDate.getUserTimeZone().minusYears(1);
    if (startDate.getUserTimeZone().isBefore(yearCheck)) {
      throw new IllegalArgumentException(PropertiesKey.EXCEEDED_A_YEAR);
    }
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
