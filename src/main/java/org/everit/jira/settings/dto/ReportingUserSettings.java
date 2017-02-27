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
package org.everit.jira.settings.dto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * The mapper class for reporting query parameters obejcts.
 */
public class ReportingUserSettings {

  private Map<ReportingUserSettingsKey, String> pluginSettingsKeyValues = new HashMap<>();

  /**
   * Get chart report parameters data.
   */
  public ReportingQueryParameters getChartReportData() {
    String currentUser =
        pluginSettingsKeyValues.get(ReportingUserSettingsKey.CHART_REPROT_CURRENTUSER);
    if (currentUser == null) {
      return new ReportingQueryParameters();
    }
    return new ReportingQueryParameters().currentUser(currentUser)
        .dateFrom(
            Long.parseLong(pluginSettingsKeyValues
                .get(ReportingUserSettingsKey.CHART_REPROT_DATE_FROM_FORMATED)))
        .dateTo(Long.parseLong(pluginSettingsKeyValues
            .get(ReportingUserSettingsKey.CHART_REPROT_DATE_TO_FORMATED)));
  }

  /**
   * Get table report parameters data.
   */
  public MissingWorklogQueryParameters getMissingWorklogData() {
    String dateFrom = pluginSettingsKeyValues
        .get(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_DATE_FROM_FORMATED);
    if (dateFrom == null) {
      return new MissingWorklogQueryParameters();
    }
    return new MissingWorklogQueryParameters()
        .dateFrom(
            Long.parseLong(pluginSettingsKeyValues
                .get(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_DATE_FROM_FORMATED)))
        .dateTo(Long.parseLong(pluginSettingsKeyValues
            .get(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_DATE_TO_FORMATED)))
        .checkHours(
            Boolean.parseBoolean(pluginSettingsKeyValues
                .get(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_CHECK_HOURS)))
        .checkNonWorkingIssues(Boolean.parseBoolean(pluginSettingsKeyValues
            .get(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_CHECK_NON_WORKING_ISSUES)));
  }

  public Map<ReportingUserSettingsKey, String> getSettingsKeyValues() {
    return pluginSettingsKeyValues;
  }

  /**
   * Get table report parameters data.
   */
  public ReportingQueryParameters getTableReportData() {
    String currentUser =
        pluginSettingsKeyValues.get(ReportingUserSettingsKey.TABLE_REPROT_CURRENTUSER);
    if (currentUser == null) {
      return new ReportingQueryParameters();
    }
    return new ReportingQueryParameters().currentUser(currentUser)
        .dateFrom(
            Long.parseLong(pluginSettingsKeyValues
                .get(ReportingUserSettingsKey.TABLE_REPROT_DATE_FROM_FORMATED)))
        .dateTo(Long.parseLong(pluginSettingsKeyValues
            .get(ReportingUserSettingsKey.TABLE_REPROT_DATE_TO_FORMATED)));
  }

  /**
   * Put all query parameters into internal storage map.
   */
  public void putChartReportData(final ReportingQueryParameters saveData) {
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.CHART_REPROT_CURRENTUSER,
        saveData.currentUser);
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.CHART_REPROT_DATE_FROM_FORMATED,
        String.valueOf(saveData.dateFrom));
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.CHART_REPROT_DATE_TO_FORMATED,
        String.valueOf(saveData.dateTo));
  }

  /**
   * Put all query parameters into internal storage map.
   */
  public void putMissingWorklogsReportData(
      final MissingWorklogQueryParameters missingWorklogSaveData) {
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_DATE_FROM_FORMATED,
        String.valueOf(missingWorklogSaveData.dateFrom));
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_DATE_TO_FORMATED,
        String.valueOf(missingWorklogSaveData.dateTo));

    pluginSettingsKeyValues.put(ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_CHECK_HOURS,
        String.valueOf(missingWorklogSaveData.checkHours));
    pluginSettingsKeyValues.put(
        ReportingUserSettingsKey.MISSING_WORKLOG_REPROT_CHECK_NON_WORKING_ISSUES,
        String.valueOf(missingWorklogSaveData.checkNonWorkingIssues));
  }

  /**
   * Put all query parameters into internal storage map.
   */
  public void putTableReportData(final ReportingQueryParameters saveData) {
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.TABLE_REPROT_CURRENTUSER,
        saveData.currentUser);
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.TABLE_REPROT_DATE_FROM_FORMATED,
        String.valueOf(saveData.dateFrom));
    pluginSettingsKeyValues.put(ReportingUserSettingsKey.TABLE_REPROT_DATE_TO_FORMATED,
        String.valueOf(saveData.dateTo));
  }

  public void putUserSettingValue(final ReportingUserSettingsKey settingKey, final String value) {
    pluginSettingsKeyValues.put(settingKey, value);
  }

}
