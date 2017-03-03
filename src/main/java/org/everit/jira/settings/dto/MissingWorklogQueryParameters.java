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
 * Parameter object for the missing worklog query.
 */
public class MissingWorklogQueryParameters {

  public Boolean checkHours;

  public Boolean checkNonWorkingIssues;

  public Long dateFrom;

  public Long dateTo;

  public MissingWorklogQueryParameters checkHours(final boolean checkHours) {
    this.checkHours = checkHours;
    return this;
  }

  public MissingWorklogQueryParameters checkNonWorkingIssues(final boolean checkNonWorkingIssues) {
    this.checkNonWorkingIssues = checkNonWorkingIssues;
    return this;
  }

  public MissingWorklogQueryParameters dateFrom(final Long dateFrom) {
    this.dateFrom = dateFrom;
    return this;
  }

  public MissingWorklogQueryParameters dateTo(final Long dateTo) {
    this.dateTo = dateTo;
    return this;
  }
}
