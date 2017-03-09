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

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.column.UserSummaryColumns;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.query.UserSummaryReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * Class that export user summary list report.
 */
public class ExportUserSummaryListReport extends AbstractExportListReport {

  private static final String USER_SUMMARY_PREFIX = "jtrp.report.export.us.col.";

  public ExportUserSummaryListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet userSummarySheet = workbook.createSheet("User Summary");
    int rowIndex = 0;

    rowIndex = insertHeaderRow(rowIndex, userSummarySheet);

    List<UserSummaryDTO> userSummary =
        querydslSupport.execute(new UserSummaryReportQueryBuilder(reportSearchParam)
            .buildQuery());
    for (UserSummaryDTO userSummaryDTO : userSummary) {
      rowIndex = insertBodyRow(rowIndex, userSummarySheet, userSummaryDTO);
    }
  }

  private int insertBodyRow(final int rowIndex, final HSSFSheet userSummarySheet,
      final UserSummaryDTO userSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = userSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, userSummaryDTO.getUserDisplayName());
    insertBodyCell(row, columnIndex, worklogInSec(userSummaryDTO.getWorkloggedTimeSum()));
    return newRowIndex;
  }

  private int insertHeaderRow(final int rowIndex,
      final HSSFSheet userSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = userSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(USER_SUMMARY_PREFIX + UserSummaryColumns.USER));
    insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(USER_SUMMARY_PREFIX + UserSummaryColumns.TOTAL_LOGGED));

    return newRowIndex;
  }

}
