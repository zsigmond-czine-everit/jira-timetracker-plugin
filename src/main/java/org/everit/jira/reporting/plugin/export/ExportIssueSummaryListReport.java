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
import org.everit.jira.reporting.plugin.column.IssueSummaryColumns;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.query.IssueSummaryReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * Class that export issue summary list report.
 */
public class ExportIssueSummaryListReport extends AbstractExportListReport {

  private static final String ISSUE_SUMMARY_PREFIX = "jtrp.report.export.is.col.";

  public ExportIssueSummaryListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet issueSummarySheet = workbook.createSheet("Issue Summary");
    int rowIndex = 0;

    rowIndex = insertHeaderRow(rowIndex, issueSummarySheet);

    List<IssueSummaryDTO> issueSummary =
        querydslSupport.execute(new IssueSummaryReportQueryBuilder(reportSearchParam)
            .buildQuery());

    for (IssueSummaryDTO issueSummaryDTO : issueSummary) {
      rowIndex = insertBodyRow(rowIndex, issueSummarySheet, issueSummaryDTO);
    }
  }

  private int insertBodyRow(final int rowIndex, final HSSFSheet issueSummarySheet,
      final IssueSummaryDTO issueSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = issueSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueKey());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueSummary());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueTypeName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getPriorityName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getStatusName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getAssignee());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getParentIssueKey());
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(issueSummaryDTO.getOrginalEstimatedSum()));
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(issueSummaryDTO.getReaminingTimeSum()));
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(issueSummaryDTO.getWorkloggedTimeSum()));
    insertBodyCell(row, columnIndex, worklogInSec(issueSummaryDTO.getExpected()));

    return newRowIndex;
  }

  private int insertHeaderRow(final int rowIndex,
      final HSSFSheet issueSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = issueSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ISSUE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ISSUE_SUMMARY));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.TYPE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.PRIORITY));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.STATUS));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ASSIGNEE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.PARENT_ISSUE_KEY));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.REMAINING));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.TOTAL_LOGGED));
    insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.EXPECTED));

    return newRowIndex;
  }

}
