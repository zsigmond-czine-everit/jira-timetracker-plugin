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
package org.everit.jira.core.util;

import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.dto.UserSettingsValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;

import com.google.gson.Gson;

/**
 * Utility class for timetracker user settoings.
 */
public final class TimetrackerUserSettingsUtil {

  public static final int FIFTEEN_MINUTES = 15;

  public static final int FIVE_MINUTES = 5;

  public static final int TEN_MINUTES = 10;

  public static final int THIRTY_MINUTES = 30;

  public static final int TWENTY_MINUTES = 20;

  public static final String USER_SETTINGS_VALUES_JSON = "userSettingsValuesJson";

  /**
   * Convert Json in to UserSettingsValues.
   *
   * @param json
   *          The worklog values in Json.
   * @return The User Settings values objcet.
   */
  public static UserSettingsValues convertJsonToUserSettingsValues(final String json) {
    if (json == null) {
      throw new NullPointerException("EMPTY_JSON");
    }
    UserSettingsValues userSettingsValues = new Gson()
        .fromJson(json, UserSettingsValues.class);

    return userSettingsValues;
  }

  /**
   * Convert {@link UserSettingsValues} class to json string.
   *
   * @param userSettingsValues
   *          the {@link UserSettingsValues} object. Cannot be <code>null</code>.
   *
   * @return the json string.
   *
   * @throws NullPointerException
   *           if worklogValues parameter is <code>null</code>.
   */
  public static String convertUserSettingsValuesToJson(
      final UserSettingsValues userSettingsValues) {
    if (userSettingsValues == null) {
      throw new NullPointerException("EMPTY_FILTER");
    }
    return new Gson().toJson(userSettingsValues);
  }

  /**
   * Get the original "hardcoded" default start time value.
   *
   * @return The default start time value.
   */
  public static String getOriginalDefaultStartTime() {
    DateTime dateTime = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
    dateTime = dateTime.withHourOfDay(DateTimeConverterUtil.HOUR_EIGHT);
    dateTime = dateTime.withMinuteOfHour(0);
    dateTime = dateTime.withSecondOfMinute(0);
    return DateTimeConverterUtil
        .dateTimeToString(DateTimeConverterUtil.convertDateTimeToDate(dateTime));
  }

  /**
   * Load TimeTrackerUserSettings values in to UserSettingsValues DTO.
   *
   * @param loaduserSettings
   *          The TimeTrackerUserSettings values.
   * @return The user settings values.
   */
  public static UserSettingsValues loadUserSettingValues(
      final TimeTrackerUserSettings loaduserSettings) {
    UserSettingsValues userSettingsValues = new UserSettingsValues();
    userSettingsValues.setEndTime(loaduserSettings.getEndTimeChange());
    userSettingsValues.setActualDate(loaduserSettings.isActualDate());
    userSettingsValues.setColoring(loaduserSettings.isColoring());
    userSettingsValues.setRounded(loaduserSettings.isRounded());
    userSettingsValues.setProgressIndDaily(loaduserSettings.isProgressIndicatordaily());
    userSettingsValues.setShowFutureLogWarning(loaduserSettings.isShowFutureLogWarning());
    userSettingsValues.setShowIssueSummary(loaduserSettings.isShowIssueSummary());
    userSettingsValues.setStartTime(loaduserSettings.getStartTimeChange());
    userSettingsValues.setActiveFieldDuration(loaduserSettings.isActiveFieldDuration());
    userSettingsValues.setDefaultStartTime(loaduserSettings.getDefaultStartTime());
    userSettingsValues.setShowRemaningEstimate(loaduserSettings.isShowRemaningEstimate());
    userSettingsValues.setShowPeriodWorklogs(loaduserSettings.isShowPeriodWorklogs());
    return userSettingsValues;
  }

  /**
   * Save UserSettingsValues DTO values in to TimeTrackerUserSettings.
   *
   * @param userSettingsValues
   *          The user settings values.
   * @return The TimeTrackerUserSettings.
   */
  public static TimeTrackerUserSettings saveUserSettingValues(
      final UserSettingsValues userSettingsValues) {
    TimeTrackerUserSettings timeTrackerUserSettings = new TimeTrackerUserSettings()
        .coloring(userSettingsValues.isColoring())
        .isProgressIndicatordaily(userSettingsValues.isProgressIndDaily())
        .actualDate(userSettingsValues.isActualDate())
        .startTimeChange(userSettingsValues.getStartTime())
        .endTimeChange(userSettingsValues.getEndTime())
        .isRounded(userSettingsValues.isRounded())
        .isShowIssueSummary(userSettingsValues.isShowIssueSummary())
        .isShowFutureLogWarning(userSettingsValues.isShowFutureLogWarning())
        .activeFieldDuration(userSettingsValues.isActiveFieldDuration())
        .defaultStartTime(userSettingsValues.getDefaultStartTime())
        .showRemaningEstimate(userSettingsValues.isShowRemaningEstimate())
        .showPeriodWorklogs(userSettingsValues.isShowPeriodWorklogs());
    return timeTrackerUserSettings;
  }

  /**
   * Validate the time change value. Possible values is 5, 10, 15, 20, 30.
   *
   * @param changeValue
   *          the change value.
   *
   * @return true if changeValue is valid change time value.
   * @throws NumberFormatException
   *           if parse failed.
   */
  public static boolean validateTimeChange(final int changeValue) {

    switch (changeValue) {
      case FIVE_MINUTES:
        return true;
      case TEN_MINUTES:
        return true;
      case FIFTEEN_MINUTES:
        return true;
      case TWENTY_MINUTES:
        return true;
      case THIRTY_MINUTES:
        return true;
      default:
        return false;
    }
  }

  private TimetrackerUserSettingsUtil() {

  }

}
