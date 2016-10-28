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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.ReportingGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class PermissionSettingsWebAction extends JiraWebActionSupport {

  private static final String FREQUENT_FEEDBACK = "jttp.plugin.frequent.feedback";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  private static final String NOT_RATED = "Not rated";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private List<String> browseGroups;

  /**
   * The first day of the week.
   */
  private String contextPath;

  private boolean feedBackSendAviable;

  private String issueCollectorSrc;

  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * The message.
   */
  private String message = "";

  private List<String> pluginGroups;

  private List<String> reportingGroups;

  private TimetrackerSettingsHelper settingsHelper;

  private List<String> timetrackerGroups;

  public PermissionSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final TimetrackerSettingsHelper settingsHelper) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.settingsHelper = settingsHelper;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    checkMailServer();

    loadPluginSettingAndParseResult();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    checkMailServer();

    loadPluginSettingAndParseResult();

    if (getHttpRequest().getParameter("sendfeedback") != null) {
      String feedbacktResult = parseFeedback();
      if (feedbacktResult != null) {
        return feedbacktResult;
      }
    }

    if (getHttpRequest().getParameter("savesettings") != null) {
      parseSaveSettings(getHttpRequest());

      savePluginSettings();
      return getRedirect(INPUT);
    }
    setReturnUrl("/secure/admin/JiraTimetrackerPermissionSettingsWebAction!default.jspa");
    return getRedirect(INPUT);
  }

  public List<String> getBrowseGroups() {
    return browseGroups;
  }

  public String getContextPath() {
    return contextPath;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getPluginGroups() {
    return pluginGroups;
  }

  public List<String> getReportingGroups() {
    return reportingGroups;
  }

  public List<String> getTimetrackerGroups() {
    return timetrackerGroups;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    ReportingGlobalSettings reportingGlobalSettings =
        settingsHelper.loadReportingGlobalSettings();
    TimeTrackerGlobalSettings globalSettings = settingsHelper.loadGlobalSettings();
    reportingGroups = reportingGlobalSettings.getReportingGroups();
    browseGroups = reportingGlobalSettings.getBrowseGroups();
    pluginGroups = globalSettings.getPluginGroups();
    timetrackerGroups = globalSettings.getTimetrackerGroups();
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parseBrowseGroups(final String[] browseGroupsValue) {
    if (browseGroupsValue == null) {
      browseGroups = new ArrayList<>();
    } else {
      browseGroups = Arrays.asList(browseGroupsValue);
    }
  }

  private String parseFeedback() {
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
      String mailSubject = JiraTimetrackerUtil
          .createFeedbackMailSubject(JiraTimetrackerAnalytics.getPluginVersion());
      String mailBody =
          JiraTimetrackerUtil.createFeedbackMailBody(customerMail, rating, feedBack);
      jiraTimetrackerPlugin.sendEmail(mailSubject, mailBody);
      JiraTimetrackerUtil.saveFeedBackTimeStampToSession(getHttpSession());
    } else {
      message = FREQUENT_FEEDBACK;
      return SUCCESS;
    }
    return null;
  }

  private void parsePluginGroups(final String[] pluginGroupsvalue) {
    if (pluginGroupsvalue == null) {
      pluginGroups = new ArrayList<>();
    } else {
      pluginGroups = Arrays.asList(pluginGroupsvalue);
    }
  }

  private void parseReportingGroups(final String[] reportingGroupsValue) {
    if (reportingGroupsValue == null) {
      reportingGroups = new ArrayList<>();
    } else {
      reportingGroups = Arrays.asList(reportingGroupsValue);
    }
  }

  /**
   * Parse the request after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public void parseSaveSettings(final HttpServletRequest request) {
    String[] reportingGroupSelectValue = request.getParameterValues("reportingGroupSelect");
    String[] browseGroupSelectValue = request.getParameterValues("browseGroupSelect");
    String[] pluginGroupsvalue = getHttpRequest().getParameterValues("pluginGroupSelect");
    String[] timetrackerGroupsValue = request.getParameterValues("timetrackerGroupSelect");
    parsePluginGroups(pluginGroupsvalue);
    parseReportingGroups(reportingGroupSelectValue);
    parseBrowseGroups(browseGroupSelectValue);
    parseTimetrackerGroups(timetrackerGroupsValue);
  }

  private void parseTimetrackerGroups(final String[] timetrackerGroupsValue) {
    if (timetrackerGroupsValue == null) {
      timetrackerGroups = new ArrayList<>();
    } else {
      timetrackerGroups = Arrays.asList(timetrackerGroupsValue);
    }
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    ReportingGlobalSettings reportingGlobalSettings =
        new ReportingGlobalSettings().browseGroups(browseGroups).reportingGroups(reportingGroups);
    settingsHelper.saveReportingGlobalSettings(reportingGlobalSettings);
    TimeTrackerGlobalSettings globalSettings = new TimeTrackerGlobalSettings()
        .pluginGroups(pluginGroups)
        .timetrackerGroups(timetrackerGroups);
    settingsHelper.saveGlobalSettings(globalSettings);
  }

  public void setBrowseGroups(final List<String> browseGroups) {
    this.browseGroups = browseGroups;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setReportingGroups(final List<String> reportingGroups) {
    this.reportingGroups = reportingGroups;
  }
}