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

/**
 * Representation of the component summary query result.
 */
public class ComponentSummaryDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String COMPONENT_DESCRIPTION = "description";

    public static final String COMPONENT_LEAD = "lead";

    public static final String COMPONENT_NAME = "name";

    public static final String ISSUE_ORIGINAL_ESTIMATE_SUM = "orginalEstimatedSum";

    public static final String ISSUE_TIME_ESTIMATE_SUM = "reaminingTimeSum";

    public static final String PROJECT_KEY = "projectKey";

    public static final String WORKLOGGED_TIME_SUM = "workloggedTimeSum";
  }

  private String description;

  private long expected;

  private String lead;

  private String name;

  private long orginalEstimatedSum;

  private String projectKey;

  private long reaminingTimeSum;

  private long workloggedTimeSum;

  public String getDescription() {
    return description;
  }

  public long getExpected() {
    expected = workloggedTimeSum + reaminingTimeSum;
    return expected;
  }

  public String getLead() {
    return lead;
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

  public long getWorkloggedTimeSum() {
    return workloggedTimeSum;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setLead(final String lead) {
    this.lead = lead;
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

  public void setWorkloggedTimeSum(final long workloggedTimeSum) {
    this.workloggedTimeSum = workloggedTimeSum;
  }

}
