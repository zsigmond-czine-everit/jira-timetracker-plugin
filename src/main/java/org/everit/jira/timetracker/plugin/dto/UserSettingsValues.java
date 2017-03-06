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
 * Represent the Timetracker User Settings input parameters.
 */
@XmlRootElement
public class UserSettingsValues {

  @XmlElement
  private boolean activeFieldDuration = Boolean.FALSE;

  @XmlElement
  private boolean actualDate = Boolean.FALSE;

  @XmlElement
  private boolean coloring = Boolean.FALSE;

  @XmlElement
  private String defaultStartTime;

  @XmlElement
  private int endTime;

  @XmlElement
  private boolean progressIndDaily = Boolean.FALSE;

  @XmlElement
  private boolean rounded = Boolean.FALSE;

  @XmlElement
  private boolean showFutureLogWarning = Boolean.FALSE;

  @XmlElement
  private boolean showIssueSummary = Boolean.FALSE;

  @XmlElement
  private boolean showPeriodWorklogs = Boolean.FALSE;

  @XmlElement
  private boolean showRemaningEstimate = Boolean.FALSE;

  @XmlElement
  private int startTime;

  public String getDefaultStartTime() {
    return defaultStartTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public int getStartTime() {
    return startTime;
  }

  public boolean isActiveFieldDuration() {
    return activeFieldDuration;
  }

  public boolean isActualDate() {
    return actualDate;
  }

  public boolean isColoring() {
    return coloring;
  }

  public boolean isProgressIndDaily() {
    return progressIndDaily;
  }

  public boolean isRounded() {
    return rounded;
  }

  public boolean isShowFutureLogWarning() {
    return showFutureLogWarning;
  }

  public boolean isShowIssueSummary() {
    return showIssueSummary;
  }

  public boolean isShowPeriodWorklogs() {
    return showPeriodWorklogs;
  }

  public boolean isShowRemaningEstimate() {
    return showRemaningEstimate;
  }

  public void setActiveFieldDuration(final boolean activeFieldDuration) {
    this.activeFieldDuration = activeFieldDuration;
  }

  public void setActualDate(final boolean actualDate) {
    this.actualDate = actualDate;
  }

  public void setColoring(final boolean coloring) {
    this.coloring = coloring;
  }

  public void setDefaultStartTime(final String defaultStartTime) {
    this.defaultStartTime = defaultStartTime;
  }

  public void setEndTime(final int endTime) {
    this.endTime = endTime;
  }

  public void setProgressIndDaily(final boolean progressIndDaily) {
    this.progressIndDaily = progressIndDaily;
  }

  public void setRounded(final boolean rounded) {
    this.rounded = rounded;
  }

  public void setShowFutureLogWarning(final boolean showFutureLogWarning) {
    this.showFutureLogWarning = showFutureLogWarning;
  }

  public void setShowIssueSummary(final boolean showIssueSummary) {
    this.showIssueSummary = showIssueSummary;
  }

  public void setShowPeriodWorklogs(final boolean showPeriodWorklogs) {
    this.showPeriodWorklogs = showPeriodWorklogs;
  }

  public void setShowRemaningEstimate(final boolean showRemaningEstimate) {
    this.showRemaningEstimate = showRemaningEstimate;
  }

  public void setStartTime(final int startTime) {
    this.startTime = startTime;
  }
}
