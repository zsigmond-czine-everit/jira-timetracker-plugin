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
package org.everit.jira.reporting.plugin.dto;

import java.sql.Timestamp;

/**
 * Representation of the version summary query result.
 */
public class VersionSummaryDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String ISSUE_ORIGINAL_ESTIMATE_SUM = "orginalEstimatedSum";

    public static final String ISSUE_TIME_ESTIMATE_SUM = "reaminingTimeSum";

    public static final String PROJECT_KEY = "projectKey";

    public static final String VERSION_ARCHIVED = "archived";

    public static final String VERSION_DESCRIPTION = "description";

    public static final String VERSION_NAME = "name";

    public static final String VERSION_RELEASE_DATE = "releaseDate";

    public static final String VERSION_RELEASED = "released";

    public static final String VERSION_START_DATE = "startDate";

    public static final String WORKLOGGED_TIME_SUM = "workloggedTimeSum";
  }

  private String archived;

  private String description;

  private long expected;

  private String name;

  private long orginalEstimatedSum;

  private String projectKey;

  private long reaminingTimeSum;

  private String released;

  private Timestamp releaseDate;

  private Timestamp startDate;

  private long workloggedTimeSum;

  public String getArchived() {
    return archived;
  }

  public String getDescription() {
    return description;
  }

  public long getExpected() {
    expected = workloggedTimeSum + reaminingTimeSum;
    return expected;
  }

  public String getName() {
    return name;
  }

  public long getOrginalEstimatedSum() {
    return orginalEstimatedSum;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public long getReaminingTimeSum() {
    return reaminingTimeSum;
  }

  public String getReleased() {
    return released;
  }

  public Timestamp getReleaseDate() {
    return releaseDate;
  }

  public Timestamp getStartDate() {
    return startDate;
  }

  public long getWorkloggedTimeSum() {
    return workloggedTimeSum;
  }

  public void setArchived(final String archived) {
    this.archived = archived;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOrginalEstimatedSum(final long orginalEstimatedSum) {
    this.orginalEstimatedSum = orginalEstimatedSum;
  }

  public void setProjectKey(final String projectKey) {
    this.projectKey = projectKey;
  }

  public void setReaminingTimeSum(final long reaminingTimeSum) {
    this.reaminingTimeSum = reaminingTimeSum;
  }

  public void setReleased(final String released) {
    this.released = released;
  }

  public void setReleaseDate(final Timestamp releaseDate) {
    this.releaseDate = releaseDate;
  }

  public void setStartDate(final Timestamp startDate) {
    this.startDate = startDate;
  }

  public void setWorkloggedTimeSum(final long workloggedTimeSum) {
    this.workloggedTimeSum = workloggedTimeSum;
  }

}
