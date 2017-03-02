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
package org.everit.jira.reporting.plugin.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;

/**
 *
 * Helper class for table report to make the summaries.
 *
 */
public class WorkLogSummarizer {

  /**
   * EveritWorklog comparator by Date.
   */
  private static class OrderByDate implements Comparator<EveritWorklog>, Serializable {
    private static final long serialVersionUID = 2000628478189889582L;

    @Override
    public int compare(final EveritWorklog wl1, final EveritWorklog wl2) {
      return wl1.getDate().compareTo(wl2.getDate());
    }
  }

  private static final int MILLISEC_IN_SEC = 1000;

  private HashMap<Integer, List<Object>> daySum = new HashMap<>();

  private DurationFormatter durationFormatter;

  private List<Pattern> issuesRegex;

  private HashMap<Integer, List<Object>> monthSum = new HashMap<>();

  private final HashMap<Integer, List<Object>> realDaySum = new HashMap<>();

  private final HashMap<Integer, List<Object>> realMonthSum = new HashMap<>();

  private final HashMap<Integer, List<Object>> realWeekSum = new HashMap<>();

  private HashMap<Integer, List<Object>> weekSum = new HashMap<>();

  private List<EveritWorklog> worklogs;

  /**
   * Default constructor.
   */
  public WorkLogSummarizer(final List<EveritWorklog> worklogs, final List<Pattern> issuesRegex) {
    this.worklogs = worklogs;
    this.issuesRegex = issuesRegex;
    durationFormatter = new DurationFormatter();
  }

  private void addToDaySummary(final EveritWorklog worklog) {
    int dayNo = worklog.getDayNo();
    ArrayList<Object> list = new ArrayList<>();
    Long prevDaySum = (daySum.get(dayNo) == null) ? Long.valueOf(0)
        : (Long) daySum.get(dayNo).get(0);
    Long sumSec = prevDaySum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(durationFormatter.exactDuration(sumSec));
    daySum.put(dayNo, list);
  }

  private void addToMonthSummary(final EveritWorklog worklog) {
    int monthNo = worklog.getMonthNo();
    ArrayList<Object> list = new ArrayList<>();
    Long prevMonthSum = (monthSum.get(monthNo) == null) ? Long.valueOf(0)
        : (Long) monthSum.get(monthNo).get(0);
    Long sumSec = prevMonthSum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(durationFormatter.exactDuration(sumSec));
    monthSum.put(monthNo, list);
  }

  private void addToRealDaySummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int dayNo = worklog.getDayNo();
    ArrayList<Object> realList = new ArrayList<>();
    Long prevRealDaySum = (realDaySum.get(dayNo) == null) ? Long.valueOf(0)
        : (Long) realDaySum.get(dayNo).get(0);
    Long realSumSec = prevRealDaySum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(durationFormatter.exactDuration(realSumSec));
    realDaySum.put(dayNo, realList);
  }

  private void addToRealMonthSummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int monthNo = worklog.getMonthNo();
    ArrayList<Object> realList = new ArrayList<>();
    Long prevRealMonthSum = realMonthSum.get(monthNo) == null ? Long.valueOf(0)
        : (Long) realMonthSum.get(monthNo).get(0);
    Long realSumSec = prevRealMonthSum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(durationFormatter.exactDuration(realSumSec));
    realMonthSum.put(monthNo, realList);
  }

  private void addToRealWeekSummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int weekNo = worklog.getWeekNo();
    ArrayList<Object> realList = new ArrayList<>();
    Long prevRealWeekSum = realWeekSum.get(weekNo) == null ? Long.valueOf(0)
        : (Long) realWeekSum.get(weekNo).get(0);
    Long realSumSec = prevRealWeekSum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(durationFormatter.exactDuration(realSumSec));
    realWeekSum.put(weekNo, realList);
  }

  private void addToWeekSummary(final EveritWorklog worklog) {
    ArrayList<Object> list = new ArrayList<>();
    int weekNo = worklog.getWeekNo();
    Long prevWeekSum = weekSum.get(weekNo) == null ? Long.valueOf(0)
        : (Long) weekSum.get(weekNo).get(0);
    Long sumSec = prevWeekSum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(durationFormatter.exactDuration(sumSec));
    weekSum.put(weekNo, list);
  }

  public HashMap<Integer, List<Object>> getDaySum() {
    return daySum;
  }

  public HashMap<Integer, List<Object>> getMonthSum() {
    return monthSum;
  }

  public HashMap<Integer, List<Object>> getRealDaySum() {
    return realDaySum;
  }

  public HashMap<Integer, List<Object>> getRealMonthSum() {
    return realMonthSum;
  }

  public HashMap<Integer, List<Object>> getRealWeekSum() {
    return realWeekSum;
  }

  public HashMap<Integer, List<Object>> getWeekSum() {
    return weekSum;
  }

  private boolean isRealWorklog(final EveritWorklog worklog) {
    boolean isRealWorklog = true;
    if (issuesRegex != null) {
      for (Pattern issuePattern : issuesRegex) {
        boolean issueMatches = issuePattern.matcher(worklog.getIssue()).matches();
        // if match not count in summary
        if (issueMatches) {
          isRealWorklog = false;
          break;
        }
      }
    }
    return isRealWorklog;
  }

  /**
   * Sort the worklogs and make the daily, weekly and monthly summaries.
   */
  public void makeSummary() {
    Collections.sort(worklogs, new OrderByDate());

    for (EveritWorklog worklog : worklogs) {

      boolean isRealWorklog = isRealWorklog(worklog);

      addToMonthSummary(worklog);
      addToRealMonthSummary(worklog, isRealWorklog);

      addToWeekSummary(worklog);
      addToRealWeekSummary(worklog, isRealWorklog);

      addToDaySummary(worklog);
      addToRealDaySummary(worklog, isRealWorklog);
    }
  }
}
