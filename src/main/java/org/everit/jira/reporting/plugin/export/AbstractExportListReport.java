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

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;

/**
 * Helper class to export list reports to XLS.
 */
public abstract class AbstractExportListReport extends AbstractExporter {

  protected boolean isWorklogInSec;

  protected List<String> notBrowsableProjectKeys;

  protected QuerydslSupport querydslSupport;

  protected ReportSearchParam reportSearchParam;

  private TimeTrackerUserSettings userSettings;

  /**
   * Simple constructor.
   *
   * @param querydslSupport
   *          the {@link QuerydslSupport} instance.
   * @param reportSearchParam
   *          the {@link ReportSearchParam} object, that contains parameters to filter condition.
   * @param notBrowsableProjectKeys
   *          the list of not browsable project keys.
   * @param userSettings
   *          the user settings.
   */
  public AbstractExportListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    this.querydslSupport = querydslSupport;
    this.reportSearchParam = reportSearchParam;
    this.notBrowsableProjectKeys = notBrowsableProjectKeys;
    this.userSettings = userSettings;

  }

  @Override
  protected abstract void appendContent(HSSFWorkbook workbook);

  private void appendNotBrowsalbeProjectsSheet(final HSSFWorkbook workbook) {
    if (!notBrowsableProjectKeys.isEmpty()) {
      HSSFSheet noBrowsableProjectsSheet = workbook.createSheet("No Browsable Projects");
      int rowIndex = 0;
      HSSFRow headerRow = noBrowsableProjectsSheet.createRow(rowIndex++);
      insertHeaderCell(headerRow, 0, i18nHelper.getText("jtrp.report.projectKeys"));
      for (String projectKey : notBrowsableProjectKeys) {
        HSSFRow bodyRow = noBrowsableProjectsSheet.createRow(rowIndex++);
        insertBodyCell(bodyRow, 0, projectKey);
      }
    }
  }

  /**
   * Export list report to Workbook (XLS).
   */
  @Override
  public HSSFWorkbook exportToXLS() {
    HSSFWorkbook workbook = super.exportToXLS();

    appendNotBrowsalbeProjectsSheet(workbook);

    return workbook;
  }

  /**
   * Insert header cell to workbook in seconds.
   *
   * @param headerRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the cell value.
   * @return the next column index.
   */
  protected int insertHeaderCellInSec(final HSSFRow headerRow, final int columnIndex,
      final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = headerRow.createCell(newColumnIndex++);
    cell.setCellStyle(headerCellStyle);
    if (userSettings.getWorklogTimeInSeconds()) {
      cell.setCellValue(value + " (s)");
    } else {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

  /**
   * Worklog in seconds or default.
   *
   * @param worklog
   *          worklog in seconds
   */
  protected String worklogInSec(final Long worklog) {
    DurationFormatter durationFormatter = new DurationFormatter();
    isWorklogInSec = true;
    if (!userSettings.getWorklogTimeInSeconds()) {
      isWorklogInSec = false;
    }
    if (worklog != null) {
      if (isWorklogInSec) {
        return worklog.toString();
      }
      return durationFormatter.exactDuration(worklog);
    }
    return "";
  }
}
