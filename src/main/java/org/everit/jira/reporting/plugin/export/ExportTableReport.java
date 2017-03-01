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
package org.everit.jira.reporting.plugin.export;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.reporting.plugin.exception.JTRPException;
import org.everit.jira.reporting.plugin.web.WorkLogSummarizer;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;

import com.atlassian.jira.exception.DataAccessException;

/**
 * Table report exporter into excel file.
 */
public class ExportTableReport extends AbstractExporter {

  private static final String[] HEADER_KEYS = { "jtrp.table.report.header.date",
      "jtrp.table.report.header.issue",
      "jtrp.table.report.header.summary",
      "jtrp.table.report.header.remaining",
      "jtrp.table.report.header.start",
      "jtrp.table.report.header.end",
      "jtrp.table.report.header.duration",
      "jtrp.table.report.header.note" };

  private static final int NUMBER_OF_SUMMARY_CELLS = 3;

  private DateTimeServer endDate;

  private final List<Pattern> issuesRegex;

  private String selectedUser;

  private DateTimeServer startDate;

  private EVWorklogManager worklogManager;

  /**
   * Default constructor.
   */
  public ExportTableReport(final EVWorklogManager worklogManager, final String selectedUser,
      final DateTimeServer startDate,
      final DateTimeServer endDate, final List<Pattern> issuesRegex) {
    this.worklogManager = worklogManager;
    this.selectedUser = selectedUser;
    this.startDate = startDate;
    this.endDate = endDate;
    this.issuesRegex = issuesRegex;
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    List<EveritWorklog> worklogs = new ArrayList<>();
    try {
      worklogs.addAll(worklogManager.getWorklogs(selectedUser, startDate, endDate));
    } catch (DataAccessException | ParseException e) {
      throw new JTRPException("Error when trying to get worklogs.", e);
    }
    WorkLogSummarizer worklogSummarizer = new WorkLogSummarizer(worklogs, issuesRegex);
    worklogSummarizer.makeSummary();

    HSSFSheet worklogDetailsSheet = workbook.createSheet("Table report");
    insertTableReportHeaderCells(worklogDetailsSheet);
    int rowIndex = 1;
    for (int i = 0; i < worklogs.size(); i++) {
      EveritWorklog everitWorklog = worklogs.get(i);
      EveritWorklog nextEveritWorklog = null;
      int columnIndex = 0;
      if (i < (worklogs.size() - 1)) {
        nextEveritWorklog = worklogs.get(i + 1);
      }
      HSSFRow row = worklogDetailsSheet.createRow(rowIndex++);

      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getStartDate());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getIssue());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getIssueSummary());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getRoundedRemaining());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getStartTime());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getEndTime());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getDuration());
      columnIndex = insertBodyCell(row, columnIndex, everitWorklog.getBody());

      if ((nextEveritWorklog == null)
          || (everitWorklog.getDayNo() != nextEveritWorklog.getDayNo())) {
        row = worklogDetailsSheet.createRow(rowIndex++);
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.daily") + " " + i18nHelper.getText("plugin.summary"));
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.work") + " "
                + worklogSummarizer.getDaySum().get(everitWorklog.getDayNo()).get(1).toString());
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.real.work") + " "
                + worklogSummarizer.getRealDaySum().get(everitWorklog.getDayNo()).get(1)
                    .toString());
        columnIndex = columnIndex - NUMBER_OF_SUMMARY_CELLS;
      }
      if ((nextEveritWorklog == null)
          || (everitWorklog.getWeekNo() != nextEveritWorklog.getWeekNo())) {
        row = worklogDetailsSheet.createRow(rowIndex++);
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.weekly") + " " + i18nHelper.getText("plugin.summary"));
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.work") + " "
                + worklogSummarizer.getWeekSum().get(everitWorklog.getWeekNo()).get(1).toString());
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.real.work") + " "
                + worklogSummarizer.getRealWeekSum().get(everitWorklog.getWeekNo()).get(1)
                    .toString());
        columnIndex = columnIndex - NUMBER_OF_SUMMARY_CELLS;
      }
      if ((nextEveritWorklog == null)
          || (everitWorklog.getMonthNo() != nextEveritWorklog.getMonthNo())) {
        row = worklogDetailsSheet.createRow(rowIndex++);
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.monthly") + " " + i18nHelper.getText("plugin.summary"));
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.work") + " "
                + worklogSummarizer.getMonthSum().get(everitWorklog.getMonthNo()).get(1)
                    .toString());
        columnIndex = insertBodyCell(row, columnIndex,
            i18nHelper.getText("plugin.real.work") + " "
                + worklogSummarizer.getRealMonthSum().get(everitWorklog.getMonthNo()).get(1)
                    .toString());
        columnIndex = columnIndex - NUMBER_OF_SUMMARY_CELLS;
      }
    }

  }

  private void insertTableReportHeaderCells(final HSSFSheet worklogDetailsSheet) {
    HSSFRow headerRow = worklogDetailsSheet.createRow(0);
    int columnIndex = 0;
    for (String headerKey : HEADER_KEYS) {
      columnIndex = insertHeaderCell(headerRow, columnIndex, i18nHelper.getText(headerKey));
    }
  }
}
