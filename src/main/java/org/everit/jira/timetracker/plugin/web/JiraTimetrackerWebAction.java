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
package org.everit.jira.timetracker.plugin.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The timetracker web action support class.
 */
public class JiraTimetrackerWebAction extends JiraWebActionSupport {

  /**
   * The default worklog ID.
   */
  private static final Long DEFAULT_WORKLOG_ID = Long.valueOf(0);

  private static final String FREQUENT_FEEDBACK = "jttp.plugin.frequent.feedback";

  private static final String INVALID_DURATION_TIME = "plugin.invalid_durationTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * The JiraTimetrackerWebAction logger..
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWebAction.class);

  private static final String MISSING_ISSUE = "plugin.missing_issue";

  private static final String NOT_RATED = "Not rated";

  private static final String PARAM_DATE = "date";

  private static final String PARAM_ISSUESELECT = "issueSelect";

  private static final String PARAM_STARTTIME = "startTime";

  private static final String SELF_WITH_DATE_URL_FORMAT =
      "/secure/JiraTimetrackerWebAction.jspa?dateFormatted=%s";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String VERSION_SPLITTER = "\\.";

  private AnalyticsDTO analyticsDTO;

  private String avatarURL = "";

  /**
   * The worklog comment.
   */
  private String comment = "";

  /**
   * The worklog comment.
   */
  private String commentForActions = "";

  private String contextPath;

  /**
   * The copied worklog id.
   */
  private Long copiedWorklogId = DEFAULT_WORKLOG_ID;

  /**
   * The date.
   */
  private Date date = null;

  /**
   * The formated date.
   */
  private String dateFormatted = "";

  /**
   * The summary of day.
   */
  private String dayFilteredSummary = "";

  /**
   * The summary of day.
   */
  private String daySummary = "";

  private String debugMessage = "";

  /**
   * The deleted worklog id.
   */
  private Long deletedWorklogId = DEFAULT_WORKLOG_ID;

  private DurationFormatter durationFormatter = new DurationFormatter();

  /**
   * The worklog duration.
   */
  private String durationTime = "";

  /**
   * The all edit worklogs ids.
   */
  private String editAllIds = "";

  /**
   * The edited worklog id.
   */
  private Long editedWorklogId = DEFAULT_WORKLOG_ID;

  /**
   * The worklog end time.
   */
  private String endTime = "";

  /**
   * The endTime input field changer buttons value.
   */
  private int endTimeChange;

  /**
   * List of the exclude days of the date variable current months.
   */
  private List<String> excludeDays = new ArrayList<String>();

  private boolean feedBackSendAviable;

  private String installedPluginId;

  /**
   * The calendar show actual Date Or Last Worklog Date.
   */
  private boolean isActualDate;

  /**
   * The calendar highlights coloring function is active or not.
   */
  private boolean isColoring;

  /**
   * The WebAction is copying a worklog or not.
   */
  private boolean isCopy = false;

  private boolean isDurationSelected = false;
  /**
   * The WebAction is edit a worklog or not.
   */
  private boolean isEdit = false;

  /**
   * The WebAction is edit all worklog or not.
   */
  private boolean isEditAll = false;
  /**
   * The calendar isPopup.
   */
  private int isPopup;
  /**
   * The issue key.
   */
  private String issueKey = "";
  /**
   * The issues.
   */
  private transient List<Issue> issues = new ArrayList<Issue>();

  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesRegex;

  /**
   * The jira main version.
   */
  private int jiraMainVersion;

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private transient JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * List of the logged days of the date variable current months.
   */
  private List<String> loggedDays = new ArrayList<String>();

  /**
   * The message.
   */
  private String message = "";

  /**
   * The message parameter.
   */
  private String messageParameter = "";

  /**
   * The summary of month.
   */
  private String monthFilteredSummary = "";

  /**
   * The summary of month.
   */
  private String monthSummary = "";

  private final PluginSettingsFactory pluginSettingsFactory;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  /**
   * The selected User for get Worklogs.
   */
  private String selectedUser = "";

  /**
   * The worklog start time.
   */
  private String startTime = "";

  /**
   *
   */
  // private Date datePCalendar = new Date();
  /**
   * The startTime input field changer buttons value.
   */
  private int startTimeChange;

  /**
   * The spent time in Jira time format (1h 20m).
   */
  private String timeSpent = "";

  private transient ApplicationUser userPickerObject;

  /**
   * The summary of week.
   */
  private String weekFilteredSummary = "";

  /**
   * The summary of week.
   */
  private String weekSummary = "";

  /**
   * The worklogs.
   */
  private List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();

  /**
   * The ids of the woklogs.
   */
  private List<Long> worklogsIds = new ArrayList<Long>();

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param pluginSettingsFactory
   *          the {@link PluginSettingsFactory}.
   */
  public JiraTimetrackerWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final PluginSettingsFactory pluginSettingsFactory) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
  }

  /**
   * Put the worklogs id into a array.
   *
   * @param worklogsParam
   *          The worklogs.
   * @return The array of the ids.
   */
  private List<Long> copyWorklogIdsToArray(final List<EveritWorklog> worklogsParam) {
    List<Long> worklogIds = new ArrayList<Long>();
    for (EveritWorklog worklog : worklogsParam) {
      worklogIds.add(worklog.getWorklogId());
    }
    return worklogIds;
  }

  private String createOrCopyAction() {
    String result;
    String validateInputFieldsResult = validateInputFields();
    if (!validateInputFieldsResult.equals(SUCCESS)) {
      result = INPUT;
    } else {
      result = createWorklog();
    }
    boolean copying = (copiedWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(copiedWorklogId);
    if (SUCCESS.equals(result)) {
      if (copying) {
        return redirectWithDateFormattedParameterOnly(result);
      } else {
        return result;
      }
    } else {
      if (copying) {
        isCopy = true;
      }
      return result;
    }
  }

  private String createWorklog() {
    String startTimeValue = getHttpRequest().getParameter(PARAM_STARTTIME);

    ActionResult createResult = jiraTimetrackerPlugin.createWorklog(
        issueKey, commentForActions, dateFormatted, startTimeValue, timeSpent);
    if (createResult.getStatus() == ActionResultStatus.FAIL) {
      message = createResult.getMessage();
      messageParameter = createResult.getMessageParameter();
      return INPUT;
    }
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
      isDurationSelected = false;
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Handle the date change.
   *
   * @throws ParseException
   *           When can't parse date.
   */
  public void dateSwitcherAction() throws ParseException {
    String dayBackValue = getHttpRequest().getParameter("dayBack");
    String dayNextValue = getHttpRequest().getParameter("dayNext");
    String weekBackValue = getHttpRequest().getParameter("weekBack");
    String weekNextValue = getHttpRequest().getParameter("weekNext");
    String monthBackValue = getHttpRequest().getParameter("monthBack");
    String monthNextVaule = getHttpRequest().getParameter("monthNext");

    Calendar tempCal = Calendar.getInstance();
    date = DateTimeConverterUtil.stringToDate(dateFormatted);
    tempCal.setTime(date);
    if (dayNextValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else if (dayBackValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else if (monthNextVaule != null) {
      tempCal.add(Calendar.MONTH, 1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else if (monthBackValue != null) {
      tempCal.add(Calendar.MONTH, -1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else if (weekNextValue != null) {
      tempCal.add(Calendar.WEEK_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else if (weekBackValue != null) {
      tempCal.add(Calendar.WEEK_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else {
      parseDateParam();
    }
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID);

    normalizeContextPath();

    getJiraVersionFromBuildUtilsInfo();

    loadPluginSettingAndParseResult();
    checkMailServer();
    // Just the here have to use the plugin actualDateOrLastWorklogDate setting
    if (setDateAndDateFormatted().equals(ERROR)) {
      return ERROR;
    }

    excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(date);
    try {
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(selectedUser, date);
    } catch (GenericEntityException e1) {
      // Not return with error. Log the error and set a message to inform the user.
      // The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling", e1);
      message = "plugin.calendar.logged.coloring.fail";
    }

    boolean deleteWorklog =
        (deletedWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(deletedWorklogId);
    ActionResult deleteResult = null;
    if (deleteWorklog) {
      deleteResult = jiraTimetrackerPlugin.deleteWorklog(deletedWorklogId);
    }
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
      loadWorklogsAndMakeSummary();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
    endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    try {
      handleInputWorklogId();
    } catch (ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }
    if (deleteWorklog) {
      if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
        message = deleteResult.getMessage();
        messageParameter = deleteResult.getMessageParameter();
        return INPUT;
      }
      return redirectWithDateFormattedParameterOnly(INPUT);
    } else {
      return INPUT;
    }
  }

  @Override
  public String doExecute() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID);

    normalizeContextPath();

    getJiraVersionFromBuildUtilsInfo();

    loadPluginSettingAndParseResult();
    checkMailServer();

    message = "";
    messageParameter = "";

    setSelectedUserFromParam();
    dateSwitcherAction();

    try {
      excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(date);
      loadWorklogsAndMakeSummary();
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }

    setFieldsValue();
    // TODO if you add any submit action you have to check in this method too! (for exaple:
    // sendfeedback)
    String result = handleDateChangeAction();
    if (result != null) {
      return result;
    }

    selectedUser = "";
    userPickerObject = null;
    // edit all save before the input fields validate
    if (getHttpRequest().getParameter("editallsave") != null) {
      result = editAllAction();
    } else if (getHttpRequest().getParameter("edit") != null) {
      result = editAction();
    } else if (getHttpRequest().getParameter("sendfeedback") != null) {
      result = sendFeedBack();
    } else {
      return createOrCopyAction();
    }

    if (SUCCESS.equals(result)) {
      return redirectWithDateFormattedParameterOnly(result);
    } else {
      return result;
    }

  }

  /**
   * Edit the worklog and handle the problems.
   *
   * @return String which will be passed to the WebAction.
   */
  public String editAction() {
    String startTimeValue = getHttpRequest().getParameter(PARAM_STARTTIME);
    startTime = startTimeValue;
    String validateInputFieldsResult = validateInputFields();
    if (validateInputFieldsResult.equals(INPUT)) {
      isEdit = true;
      return INPUT;
    }
    ActionResult updateResult = jiraTimetrackerPlugin.editWorklog(
        editedWorklogId, issueKey, commentForActions, dateFormatted,
        startTimeValue, timeSpent);
    if (updateResult.getStatus() == ActionResultStatus.FAIL) {
      message = updateResult.getMessage();
      messageParameter = updateResult.getMessageParameter();
      isEdit = true;
      return INPUT;
    }
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    editedWorklogId = DEFAULT_WORKLOG_ID;
    isDurationSelected = false;
    return SUCCESS;
  }

  /**
   * The edit all function save action. Save the worklogs in the given date. The worklogs come form
   * the editAllIds, the date from the {@code dateFormatted}.
   *
   * @return SUCCESS if the save was success else FAIL.
   * @throws ParseException
   *           If cannot parse date or time.
   */
  public String editAllAction() throws ParseException {
    // parse the editAllIds
    List<Long> editWorklogIds = parseEditAllIds();
    // edit the worklogs!
    // TODO what if result is a fail?????? what if just one fail?
    // ActionResult editResult;
    for (Long editWorklogId : editWorklogIds) {
      EveritWorklog editWorklog = jiraTimetrackerPlugin
          .getWorklog(editWorklogId);
      // editResult =
      jiraTimetrackerPlugin.editWorklog(editWorklog
          .getWorklogId(), editWorklog.getIssue(), editWorklog
              .getBody(),
          dateFormatted, editWorklog.getStartTime(),
          DateTimeConverterUtil.stringTimeToString(editWorklog
              .getDuration()));
    }
    // set editAllIds to default and list worklogs
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    editAllIds = "";
    return SUCCESS;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getComment() {
    return comment;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Long getCopiedWorklogId() {
    return copiedWorklogId;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public String getDateFormatted() {
    return dateFormatted;
  }

  public String getDayFilteredSummary() {
    return dayFilteredSummary;
  }

  public String getDaySummary() {
    return daySummary;
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public Long getDeletedWorklogId() {
    return deletedWorklogId;
  }

  public String getDurationTime() {
    return durationTime;
  }

  public String getEditAllIds() {
    return editAllIds;
  }

  public Long getEditedWorklogId() {
    return editedWorklogId;
  }

  public String getEndTime() {
    return endTime;
  }

  public int getEndTimeChange() {
    return endTimeChange;
  }

  public List<String> getExcludeDays() {
    return excludeDays;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  public String getInstalledPluginId() {
    return installedPluginId;
  }

  public boolean getIsColoring() {
    return isColoring;
  }

  public boolean getIsCopy() {
    return isCopy;
  }

  public boolean getIsDurationSelected() {
    return isDurationSelected;
  }

  public boolean getIsEdit() {
    return isEdit;
  }

  public boolean getIsEditAll() {
    return isEditAll;
  }

  public int getIsPopup() {
    return isPopup;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public List<Pattern> getIssuesRegex() {
    return issuesRegex;
  }

  public int getJiraMainVersion() {
    return jiraMainVersion;
  }

  public JiraTimetrackerPlugin getJiraTimetrackerPlugin() {
    return jiraTimetrackerPlugin;
  }

  private void getJiraVersionFromBuildUtilsInfo() {
    String jiraVersion = analyticsDTO.getJiraVersion();
    String[] versionSplit = jiraVersion.split(VERSION_SPLITTER);
    jiraMainVersion = Integer.parseInt(versionSplit[0]);

  }

  public List<String> getLoggedDays() {
    return loggedDays;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageParameter() {
    return messageParameter;
  }

  public String getMonthFilteredSummary() {
    return monthFilteredSummary;
  }

  public String getMonthSummary() {
    return monthSummary;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getSelectedeUser() {
    return selectedUser;
  }

  public String getStartTime() {
    return startTime;
  }

  public int getStartTimeChange() {
    return startTimeChange;
  }

  public ApplicationUser getUserPickerObject() {
    return userPickerObject;
  }

  public String getWeekFilteredSummary() {
    return weekFilteredSummary;
  }

  public String getWeekSummary() {
    return weekSummary;
  }

  public List<EveritWorklog> getWorklogs() {
    return worklogs;
  }

  public List<Long> getWorklogsIds() {
    return worklogsIds;
  }

  /**
   * Date change action handler.
   *
   * @return null if the current action is not a Date change action
   */
  private String handleDateChangeAction() {
    if ((getHttpRequest().getParameter("edit") == null)
        && (getHttpRequest().getParameter("submit") == null)
        && (getHttpRequest().getParameter("editallsave") == null)
        && (getHttpRequest().getParameter("sendfeedback") == null)
        && (getHttpRequest().getParameter("reporting-send-button") == null)) {
      try {
        handleInputWorklogId();
      } catch (ParseException e) {
        LOGGER.error("Error when try parse the worklog.", e);
        return ERROR;
      }
      setUserPickerObjectBasedOnSelectedUser();
      return SUCCESS;
    }
    return null;
  }

  private String handleDuration() {
    String startTimeValue = getHttpRequest().getParameter(PARAM_STARTTIME);
    String durationTimeValue = getHttpRequest().getParameter("durationTime");
    Date startDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTime(startTimeValue);
    } catch (ParseException e) {
      message = INVALID_START_TIME;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(durationTimeValue)) {
      if (!DateTimeConverterUtil.isValidJiraTime(durationTimeValue)) {
        message = INVALID_DURATION_TIME;
        return INPUT;
      } else {
        timeSpent = durationTimeValue;
        int seconds = DateTimeConverterUtil.jiraDurationToSeconds(durationTimeValue);
        Date endTime = DateUtils.addSeconds(startDateTime, seconds);
        if (!DateUtils.isSameDay(startDateTime, endTime)) {
          message = INVALID_DURATION_TIME;
          return INPUT;
        }
      }
    } else {
      String result = handleValidDuration(startDateTime);
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }

  private String handleEndTime() {
    String startTimeValue = getHttpRequest().getParameter(PARAM_STARTTIME);
    String endTimeValue = getHttpRequest().getParameter("endTime");
    if (!DateTimeConverterUtil.isValidTime(endTimeValue)) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }
    Date startDateTime;
    Date endDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(startTimeValue);
      endDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(endTimeValue);
    } catch (ParseException e) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }

    long seconds = (endDateTime.getTime() - startDateTime.getTime())
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    if (seconds > 0) {
      timeSpent = durationFormatter.exactDuration(seconds);
    } else {
      message = "plugin.invalid_timeInterval";
      return INPUT;
    }
    return SUCCESS;
  }

  /**
   * Handle the editAllIds, the editedWorklogIds and the copiedWorklogId variable values. If the
   * values different from the default, then make the necessary settings.
   *
   * @throws ParseException
   *           If can't parse the editWorklog date.
   */
  private void handleInputWorklogId() throws ParseException {
    if (!"".equals(editAllIds)) {
      isEditAll = true;
    }
    if ((editedWorklogId != null)
        && !DEFAULT_WORKLOG_ID.equals(editedWorklogId)) {
      isEdit = true;
      EveritWorklog editWorklog;
      editWorklog = jiraTimetrackerPlugin.getWorklog(editedWorklogId);
      issueKey = editWorklog.getIssue();
      comment = editWorklog.getBody();
      startTime = editWorklog.getStartTime();
      endTime = editWorklog.getEndTime();
      durationTime = editWorklog.getDuration();
    }

    if ((copiedWorklogId != null)
        && !DEFAULT_WORKLOG_ID.equals(copiedWorklogId)) {
      isCopy = true;
      EveritWorklog editWorklog;
      editWorklog = jiraTimetrackerPlugin.getWorklog(copiedWorklogId);
      issueKey = editWorklog.getIssue();
      comment = editWorklog.getBody();
    }
  }

  private String handleValidDuration(final Date startDateTime) {
    String durationTimeValue = getHttpRequest().getParameter("durationTime");
    Date durationDateTime;
    try {
      durationDateTime = DateTimeConverterUtil
          .stringTimeToDateTimeGMT(durationTimeValue);
    } catch (ParseException e) {
      message = INVALID_DURATION_TIME;
      return INPUT;
    }

    long seconds = durationDateTime.getTime()
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    timeSpent = durationFormatter.exactDuration(seconds);

    // check the duration time to not exceed the present day
    Date endTime = DateUtils.addSeconds(startDateTime, (int) seconds);
    if (!DateUtils.isSameDay(startDateTime, endTime)) {
      message = INVALID_DURATION_TIME;
      return INPUT;
    }
    return SUCCESS;
  }

  private void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    isPopup = pluginSettingsValues.isCalendarPopup;
    isActualDate = pluginSettingsValues.isActualDate;
    issuesRegex = pluginSettingsValues.filteredSummaryIssues;
    startTimeChange = pluginSettingsValues.startTimeChange;
    endTimeChange = pluginSettingsValues.endTimeChange;
    isColoring = pluginSettingsValues.isColoring;
    installedPluginId = pluginSettingsValues.pluginUUID;
  }

  /**
   * Set worklogs list, the worklogsIds list and make Summary.
   *
   * @throws GenericEntityException
   *           If GenericEntity Exception.
   * @throws ParseException
   *           If getWorklogs can't parse date.
   * @throws SQLException
   *           Cannot get the worklogs
   * @throws DataAccessException
   *           Cannot get the worklogs
   */
  private void loadWorklogsAndMakeSummary() throws GenericEntityException,
      ParseException, DataAccessException, SQLException {
    try {
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(selectedUser, date);
    } catch (GenericEntityException e1) {
      // Not return whit error. Log the error and set a message to
      // inform the user. The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling",
          e1);
      message = "plugin.calendar.logged.coloring.fail";
    }
    worklogs = jiraTimetrackerPlugin.getWorklogs(selectedUser, date, null);
    worklogsIds = copyWorklogIdsToArray(worklogs);
    makeSummary();
  }

  /**
   * Make summary today, this week and this month.
   *
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  public void makeSummary() throws GenericEntityException {
    ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
    boolean useISO8601 = applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);

    Calendar startCalendar = Calendar.getInstance();
    if (useISO8601) {
      startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
    }
    startCalendar.setTime(date);
    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
    startCalendar.set(Calendar.MINUTE, 0);
    startCalendar.set(Calendar.SECOND, 0);
    startCalendar.set(Calendar.MILLISECOND, 0);
    Calendar originalStartcalendar = (Calendar) startCalendar.clone();
    Date start = startCalendar.getTime();

    Calendar endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);

    Date end = endCalendar.getTime();
    daySummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      dayFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    while (startCalendar.get(Calendar.DAY_OF_WEEK) != startCalendar.getFirstDayOfWeek()) {
      startCalendar.add(Calendar.DATE, -1); // Substract 1 day until first day of week.
    }
    start = startCalendar.getTime();
    endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DATE, DateTimeConverterUtil.DAYS_PER_WEEK);
    end = endCalendar.getTime();
    weekSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      weekFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    startCalendar.set(Calendar.DAY_OF_MONTH, 1);
    start = startCalendar.getTime();

    endCalendar = (Calendar) originalStartcalendar.clone();
    endCalendar.set(Calendar.DAY_OF_MONTH,
        endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
    end = endCalendar.getTime();

    monthSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      monthFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parseDateParam() throws ParseException {
    String requestDate = getHttpRequest().getParameter(PARAM_DATE);
    if (requestDate != null) {
      if (!"".equals(requestDate)) {
        dateFormatted = requestDate;
      }
      date = DateTimeConverterUtil.stringToDate(dateFormatted);
    } else if ((dateFormatted == null) || "".equals(dateFormatted)) {
      date = new Date();
      dateFormatted = DateTimeConverterUtil.dateToString(date);
    } else {
      date = DateTimeConverterUtil.stringToDate(dateFormatted);
    }
  }

  /**
   * Parses the {@link #editAllIds} string to a list of {@code Long} values.
   */
  public List<Long> parseEditAllIds() {
    List<Long> editWorklogIds = new ArrayList<Long>();
    String editAllIdsCopy = editAllIds;
    editAllIdsCopy = editAllIdsCopy.replace("[", "");
    editAllIdsCopy = editAllIdsCopy.replace("]", "");
    editAllIdsCopy = editAllIdsCopy.replace(" ", "");
    if (editAllIdsCopy.trim().equals("")) {
      return Collections.emptyList();
    }
    String[] editIds = editAllIdsCopy.split(",");
    for (String editId : editIds) {
      editWorklogIds.add(Long.valueOf(editId));
    }
    return editWorklogIds;
  }

  /**
   * The readObject method for the transient variable.
   *
   * @param in
   *          The ObjectInputStream.
   * @throws IOException
   *           IOException.
   * @throws ClassNotFoundException
   *           ClassNotFoundException.
   */
  private void readObject(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    issues = new ArrayList<Issue>();
  }

  private String redirectWithDateFormattedParameterOnly(final String action) {
    setReturnUrl(
        String.format(SELF_WITH_DATE_URL_FORMAT,
            JiraTimetrackerUtil.urlEndcodeHandleException(dateFormatted)));
    return getRedirect(action);
  }

  private String sendFeedBack() {
    if (JiraTimetrackerUtil.loadAndCheckFeedBackTimeStampFromSession(getHttpSession())) {
      String feedBackValue = getHttpRequest().getParameter("feedbackinput");
      String ratingValue = getHttpRequest().getParameter("rating");
      String customerMail =
          JiraTimetrackerUtil.getCheckCustomerMail(getHttpRequest().getParameter("customerMail"));
      String feedBack = "";
      String rating = NOT_RATED;
      if (feedBackValue != null) {
        feedBack = feedBackValue.trim();
      }
      if (ratingValue != null) {
        rating = ratingValue;
      }
      String mailSubject =
          JiraTimetrackerUtil.createFeedbackMailSubject(analyticsDTO.getPluginVersion());
      String mailBody = JiraTimetrackerUtil.createFeedbackMailBody(customerMail, rating, feedBack);
      jiraTimetrackerPlugin.sendEmail(mailSubject, mailBody);
      try {
        loadWorklogsAndMakeSummary();
        startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
        endTime = DateTimeConverterUtil.dateTimeToString(new Date());
        comment = "";
      } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
        LOGGER.error("Error when try set the plugin variables.", e);
        return ERROR;
      }
      JiraTimetrackerUtil.saveFeedBackTimeStampToSession(getHttpSession());
      return SUCCESS;
    } else {
      message = FREQUENT_FEEDBACK;
      return INPUT;
    }
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setColoring(final boolean isColoring) {
    this.isColoring = isColoring;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setCopiedWorklogId(final Long copiedWorklogId) {
    this.copiedWorklogId = copiedWorklogId;
  }

  public void setDate(final Date date) {
    this.date = (Date) date.clone();
  }

  private String setDateAndDateFormatted() {
    if ("".equals(dateFormatted)) {
      if (isActualDate) {
        date = Calendar.getInstance().getTime();
        dateFormatted = DateTimeConverterUtil.dateToString(date);
      } else {
        try {
          date = jiraTimetrackerPlugin.firstMissingWorklogsDate(selectedUser);
          dateFormatted = DateTimeConverterUtil.dateToString(date);
        } catch (GenericEntityException e) {
          LOGGER.error("Error when try set the plugin date.", e);
          return ERROR;
        }
      }
    } else {
      try {
        date = DateTimeConverterUtil.stringToDate(dateFormatted);
      } catch (ParseException e) {
        return ERROR;
      }
    }
    return SUCCESS;
  }

  public void setDateFormatted(final String dateFormatted) {
    this.dateFormatted = dateFormatted;
  }

  public void setDayFilteredSummary(final String dayFilteredSummary) {
    this.dayFilteredSummary = dayFilteredSummary;
  }

  public void setDaySummary(final String daySummary) {
    this.daySummary = daySummary;
  }

  public void setDebugMessage(final String debugMessage) {
    this.debugMessage = debugMessage;
  }

  public void setDeletedWorklogId(final Long deletedWorklogId) {
    this.deletedWorklogId = deletedWorklogId;
  }

  public void setDurationTime(final String durationTime) {
    this.durationTime = durationTime;
  }

  public void setEdit(final boolean edit) {
    isEdit = edit;
  }

  public void setEditAll(final boolean isEditAll) {
    this.isEditAll = isEditAll;
  }

  public void setEditAllIds(final String editAllIds) {
    this.editAllIds = editAllIds;
  }

  public void setEditedWorklogId(final Long editedWorklogId) {
    this.editedWorklogId = editedWorklogId;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setEndTimeChange(final int endTimeChange) {
    this.endTimeChange = endTimeChange;
  }

  public void setExcludeDays(final List<String> excludeDays) {
    this.excludeDays = excludeDays;
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  /**
   * Set the read values to the input fields back.
   */
  private String setFieldsValue() {
    String issueSelectValue = getHttpRequest().getParameter(PARAM_ISSUESELECT);
    String endTimeValue = getHttpRequest().getParameter("endTime");
    String durationTimeValue = getHttpRequest().getParameter("durationTime");
    String commentsValue = getHttpRequest().getParameter("comments");
    String endOrDurationValue = getHttpRequest().getParameter("endOrDuration");

    if ((endOrDurationValue != null) && "duration".equals(endOrDurationValue)) {
      isDurationSelected = true;
    }

    if (issueSelectValue != null) {
      issueKey = issueSelectValue;
    }

    try {
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
    } catch (ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }

    if (endTimeValue != null) {
      endTime = endTimeValue;
    } else {
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    }

    if (durationTimeValue != null) {
      durationTime = durationTimeValue;
    }
    if (commentsValue != null) {
      comment = commentsValue;
      commentForActions = commentsValue;
      comment = comment.replace("\"", "\\\"");
      comment = comment.replace("\r", "\\r");
      comment = comment.replace("\n", "\\n");
    } else {
      comment = "";
    }
    return null;
  }

  public void setInstalledPluginId(final String installedPluginId) {
    this.installedPluginId = installedPluginId;
  }

  public void setIsCopy(final boolean isCopy) {
    this.isCopy = isCopy;
  }

  public void setIsDurationSelected(final boolean isDurationSelected) {
    this.isDurationSelected = isDurationSelected;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setIssues(final List<Issue> issues) {
    this.issues = issues;
  }

  public void setIssuesRegex(final List<Pattern> issuesRegex) {
    this.issuesRegex = issuesRegex;
  }

  public void setJiraMainVersion(final int jiraMainVersion) {
    this.jiraMainVersion = jiraMainVersion;
  }

  public void setJiraTimetrackerPlugin(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  public void setLoggedDays(final List<String> loggedDays) {
    this.loggedDays = loggedDays;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMessageParameter(final String messageParameter) {
    this.messageParameter = messageParameter;
  }

  public void setMonthFilteredSummary(final String monthFilteredSummary) {
    this.monthFilteredSummary = monthFilteredSummary;
  }

  public void setMonthSummary(final String monthSummary) {
    this.monthSummary = monthSummary;
  }

  public void setPopup(final int isPopup) {
    this.isPopup = isPopup;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setSelectedeUser(final String selectedeUser) {
    selectedUser = selectedeUser;
  }

  private void setSelectedUserFromParam() {
    String selectedUserValue = getHttpRequest().getParameter("selectedUser");
    if (selectedUserValue != null) {
      selectedUser = selectedUserValue;
    } else {
      selectedUser = "";
    }
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

  public void setStartTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
  }

  public void setUserPickerObject(final ApplicationUser userPickerObject) {
    this.userPickerObject = userPickerObject;
  }

  private void setUserPickerObjectBasedOnSelectedUser() {

    if ((selectedUser != null) && !"".equals(selectedUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByName(selectedUser);
      AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getUser(),
          userPickerObject, Avatar.Size.SMALL).toString());
    } else {
      userPickerObject = null;
    }
  }

  public void setWeekFilteredSummary(final String weekFilteredSummary) {
    this.weekFilteredSummary = weekFilteredSummary;
  }

  public void setWeekSummary(final String weekSummary) {
    this.weekSummary = weekSummary;
  }

  public void setWorklogs(final List<EveritWorklog> worklogs) {
    this.worklogs = worklogs;
  }

  public void setWorklogsIds(final List<Long> worklogsIds) {
    this.worklogsIds = worklogsIds;
  }

  /**
   * Check the startTime, endTime or durationTime fields values.
   *
   * @return If the values valid the return SUCCESS else return INPUT.
   */
  public String validateInputFields() {
    String startTimeValue = getHttpRequest().getParameter(PARAM_STARTTIME);
    String endOrDurationValue = getHttpRequest().getParameter("endOrDuration");
    String issueSelectValue = getHttpRequest().getParameter(PARAM_ISSUESELECT);

    // if (commentsValue[0] == null) {
    // return INPUT;
    // }
    if (issueSelectValue == null) {
      message = MISSING_ISSUE;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(startTimeValue)) {
      message = INVALID_START_TIME;
      return INPUT;
    }
    if ("duration".equals(endOrDurationValue)) {
      String result = handleDuration();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    } else {
      String result = handleEndTime();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
