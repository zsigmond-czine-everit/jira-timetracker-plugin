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
import org.everit.jira.reporting.plugin.column.ProjectSummaryColumns;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.query.ProjectSummaryReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * Class that export project summary list report.
 */
public class ExportProjectSummaryListReport extends AbstractExportListReport {

  private static final String PROJECT_SUMMARY_PREFIX = "jtrp.report.export.ps.col.";

  public ExportProjectSummaryListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet projectSummarySheet = workbook.createSheet("Project Summary");
    int rowIndex = 0;

    rowIndex = insertHeaderRow(rowIndex, projectSummarySheet);

    List<ProjectSummaryDTO> projectSummary =
        querydslSupport.execute(new ProjectSummaryReportQueryBuilder(reportSearchParam)
            .buildQuery());

    for (ProjectSummaryDTO projectSummaryDTO : projectSummary) {
      rowIndex = insertBodyRow(rowIndex, projectSummarySheet, projectSummaryDTO);
    }
  }

  private int insertBodyRow(final int rowIndex, final HSSFSheet projectSummarySheet,
      final ProjectSummaryDTO projectSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, projectSummaryDTO.getProjectName());
    columnIndex = insertBodyCell(row, columnIndex, projectSummaryDTO.getProjectKey());

    columnIndex = insertBodyCell(row, columnIndex,
        worklogInSec(projectSummaryDTO.getIssuesOrginalEstimatedSum()));
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(projectSummaryDTO.getWorkloggedTimeSum()));
    columnIndex = insertBodyCell(row, columnIndex,
        worklogInSec(projectSummaryDTO.getIssuesReaminingTimeSum()));
    insertBodyCell(row, columnIndex, worklogInSec(projectSummaryDTO.getExpectedTotal()));

    return newRowIndex;
  }

  private int insertHeaderRow(final int rowIndex,
      final HSSFSheet projectSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.PROJECT));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.PROJECT_KEY));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.TOTAL_LOGGED));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.REMAINING));
    insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.EXPECTED_TOTAL));

    return newRowIndex;
  }

}
