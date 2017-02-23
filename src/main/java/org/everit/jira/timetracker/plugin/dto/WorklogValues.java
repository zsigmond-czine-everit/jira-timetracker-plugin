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
package org.everit.jira.timetracker.plugin.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represent the Timetracker worklog input parameters.
 */
@XmlRootElement
public class WorklogValues {

  @XmlElement
  private String adjustmentAmount = "";

  @XmlElement
  private String comment = "";

  public String commentForActions = "";

  @XmlElement
  private String durationTime = "";

  @XmlElement
  private Long endDate;

  @XmlElement
  private String endTime;

  @XmlElement
  private Boolean isDuration = Boolean.FALSE;

  @XmlElement
  private String issueKey = "";

  @XmlElement
  private String newEstimate = "";

  @XmlElement
  private Boolean period = Boolean.FALSE;

  @XmlElement
  private String remainingEstimateType;

  @XmlElement
  private String startTime;

  public String getAdjustmentAmount() {
    return adjustmentAmount;
  }

  @XmlElement
  public String getComment() {
    return comment;
  }

  public String getCommentForActions() {
    return commentForActions;
  }

  public String getDurationTime() {
    return durationTime;
  }

  public Long getEndDate() {
    return endDate;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getNewEstimate() {
    return newEstimate;
  }

  public String getRemainingEstimateType() {
    return remainingEstimateType;
  }

  public String getStartTime() {
    return startTime;
  }

  public Boolean isDuration() {
    return isDuration;
  }

  public Boolean isPeriod() {
    return period;
  }

  public void setAdjustmentAmount(final String adjustmentAmount) {
    this.adjustmentAmount = adjustmentAmount;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setCommentForActions(final String commentForActions) {
    this.commentForActions = commentForActions;
  }

  public void setDurationTime(final String durationTime) {
    this.durationTime = durationTime;
  }

  public void setEndDate(final Long endDate) {
    this.endDate = endDate;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setIsDuration(final Boolean isDuration) {
    this.isDuration = isDuration;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setNewEstimate(final String newEstimate) {
    this.newEstimate = newEstimate;
  }

  public void setPeriod(final Boolean period) {
    this.period = period;
  }

  public void setRemainingEstimateType(final String remainingEstimateType) {
    this.remainingEstimateType = remainingEstimateType;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

}
