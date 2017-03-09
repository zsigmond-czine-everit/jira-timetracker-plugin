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
import org.everit.jira.reporting.plugin.column.VersionSummaryColumns;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.VersionSummaryDTO;
import org.everit.jira.reporting.plugin.query.VersionSummaryReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * Class that export version summary list report.
 */
public class ExportVersionSummaryListReport extends AbstractExportListReport {

  private static final String VERSION_SUMMARY_PREFIX = "jtrp.report.export.vs.col.";

  public ExportVersionSummaryListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet versionSummarySheet = workbook.createSheet("Version Summary");
    int rowIndex = 0;

    rowIndex = insertHeaderRow(rowIndex, versionSummarySheet);

    List<VersionSummaryDTO> versionSummary =
        querydslSupport.execute(new VersionSummaryReportQueryBuilder(reportSearchParam)
            .buildQuery());

    for (VersionSummaryDTO versionSummaryDTO : versionSummary) {
      rowIndex = insertBodyRow(rowIndex, versionSummarySheet, versionSummaryDTO);
    }
  }

  private String getStatus(final VersionSummaryDTO versionSummaryDTO) {
    String status = i18nHelper.getText("jtrp.report.vs.status.unreleased");
    if (Boolean.parseBoolean(versionSummaryDTO.getArchived())) {
      status = i18nHelper.getText("jtrp.report.vs.status.archived");
    } else {
      if (Boolean.parseBoolean(versionSummaryDTO.getReleased())) {
        status = i18nHelper.getText("jtrp.report.vs.status.released");
      }
    }
    return status;
  }

  private int insertBodyRow(final int rowIndex, final HSSFSheet versionSummarySheet,
      final VersionSummaryDTO versionSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = versionSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, versionSummaryDTO.getProjectKey());
    columnIndex = insertBodyCell(row, columnIndex, versionSummaryDTO.getName());

    columnIndex = insertBodyCell(row, columnIndex, getStatus(versionSummaryDTO));
    columnIndex = insertBodyCell(row, columnIndex, versionSummaryDTO.getStartDate());
    columnIndex = insertBodyCell(row, columnIndex, versionSummaryDTO.getReleaseDate());
    columnIndex = insertBodyCell(row, columnIndex, versionSummaryDTO.getDescription());
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(versionSummaryDTO.getOrginalEstimatedSum()));
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(versionSummaryDTO.getReaminingTimeSum()));
    columnIndex =
        insertBodyCell(row, columnIndex, worklogInSec(versionSummaryDTO.getWorkloggedTimeSum()));
    insertBodyCell(row, columnIndex, worklogInSec(versionSummaryDTO.getExpected()));
    return newRowIndex;
  }

  private int insertHeaderRow(final int rowIndex,
      final HSSFSheet projectSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.PROJECT));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.VERSION));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.STATUS));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.START_DATE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.RELEASE_DATE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.DESCRIPTION));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.REMAINING));
    columnIndex = insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.LOGGED));
    insertHeaderCellInSec(row, columnIndex,
        i18nHelper.getText(VERSION_SUMMARY_PREFIX + VersionSummaryColumns.EXPECTED));

    return newRowIndex;
  }

}
