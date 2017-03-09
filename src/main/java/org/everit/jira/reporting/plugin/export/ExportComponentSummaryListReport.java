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
import org.everit.jira.reporting.plugin.column.ComponentSummaryColumns;
import org.everit.jira.reporting.plugin.dto.ComponentSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.query.ComponentSummaryReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * Class that export component summary list report.
 */
public class ExportComponentSummaryListReport extends AbstractExportListReport {

  private static final String COMPONENT_SUMMARY_PREFIX = "jtrp.report.export.cs.col.";

  public ExportComponentSummaryListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet componentSummarySheet = workbook.createSheet("Component Summary");
    int rowIndex = 0;

    rowIndex = insertHeaderRow(rowIndex, componentSummarySheet);

    List<ComponentSummaryDTO> componentSummary =
        querydslSupport.execute(new ComponentSummaryReportQueryBuilder(reportSearchParam)
            .buildQuery());

    for (ComponentSummaryDTO componentSummaryDTO : componentSummary) {
      rowIndex = insertBodyRow(rowIndex, componentSummarySheet, componentSummaryDTO);
    }
  }

  private int insertBodyRow(final int rowIndex, final HSSFSheet componentSummarySheet,
      final ComponentSummaryDTO componentSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = componentSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, componentSummaryDTO.getProjectKey());
    columnIndex = insertBodyCell(row, columnIndex, componentSummaryDTO.getName());

    columnIndex = insertBodyCell(row, columnIndex, componentSummaryDTO.getLead());
    columnIndex = insertBodyCell(row, columnIndex, componentSummaryDTO.getDescription());
    columnIndex = insertBodyCell(row, columnIndex,
        worklogInSec(componentSummaryDTO.getOrginalEstimatedSum()));
    columnIndex = insertBodyCell(row, columnIndex,
        worklogInSec(componentSummaryDTO.getReaminingTimeSum()));
    columnIndex = insertBodyCell(row, columnIndex,
        worklogInSec(componentSummaryDTO.getWorkloggedTimeSum()));
    insertBodyCell(row, columnIndex, worklogInSec(componentSummaryDTO.getExpected()));
    return newRowIndex;
  }

  private int insertHeaderRow(final int rowIndex,
      final HSSFSheet projectSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.PROJECT));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.COMPONENT));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.LEAD));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.DESCRIPTION));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.REMAINING));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.LOGGED));
    insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(COMPONENT_SUMMARY_PREFIX + ComponentSummaryColumns.EXPECTED));

    return newRowIndex;
  }

}
