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

/**
 * The settings keys for reporting query parameters.
 */
public enum ReportingUserSettingsKey implements SettingsMapper {

  CHART_REPROT_CURRENTUSER {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.CHART_REPROT_CURRENTUSER;
    }
  },
  CHART_REPROT_DATE_FROM_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.CHART_REPROT_DATE_FROM_FORMATED;
    }
  },
  CHART_REPROT_DATE_TO_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.CHART_REPROT_DATE_TO_FORMATED;
    }
  },
  MISSING_WORKLOG_REPROT_CHECK_HOURS {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.MISSING_WORKLOG_REPROT_CHECK_HOURS;
    }
  },
  MISSING_WORKLOG_REPROT_CHECK_NON_WORKING_ISSUES {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.MISSING_WORKLOG_REPROT_CHECK_NON_WORKING_ISSUES;
    }
  },
  MISSING_WORKLOG_REPROT_DATE_FROM_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.MISSING_WORKLOG_REPROT_DATE_FROM_FORMATED;
    }
  },
  MISSING_WORKLOG_REPROT_DATE_TO_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.MISSING_WORKLOG_REPROT_DATE_TO_FORMATED;
    }
  },
  TABLE_REPROT_CURRENTUSER {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.TABLE_REPROT_CURRENTUSER;
    }
  },
  TABLE_REPROT_DATE_FROM_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.TABLE_REPROT_DATEFROMFORMATED;
    }
  },
  TABLE_REPROT_DATE_TO_FORMATED {
    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.TABLE_REPROT_DATETOFORMATED;
    }
  };

}
