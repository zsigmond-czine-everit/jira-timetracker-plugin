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

import java.sql.Timestamp;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

/**
 * Abstract class for excel report classes.
 *
 */
public abstract class AbstractExporter {

  protected HSSFCellStyle bodyCellStyle;

  protected HSSFCellStyle headerCellStyle;

  protected I18nHelper i18nHelper;

  public AbstractExporter() {
    i18nHelper = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
  }

  protected abstract void appendContent(HSSFWorkbook workbook);

  private void createBodyCellStyle(final HSSFWorkbook workbook) {
    bodyCellStyle = workbook.createCellStyle();
    bodyCellStyle.setWrapText(true);
  }

  private void createHeaderCellStyle(final HSSFWorkbook workbook) {
    headerCellStyle = workbook.createCellStyle();
    HSSFFont headerFont = workbook.createFont();
    headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    headerCellStyle.setFont(headerFont);
    headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    headerCellStyle.setWrapText(true);
  }

  /**
   * Export list report to Workbook (XLS).
   */
  public HSSFWorkbook exportToXLS() {
    HSSFWorkbook workbook = new HSSFWorkbook();
    createHeaderCellStyle(workbook);
    createBodyCellStyle(workbook);

    appendContent(workbook);

    return workbook;
  }

  /**
   * Insert body cell to workbook. The cell value is "v1; v2; v3".
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex,
      final List<String> value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    String cValue = "";
    if (value.size() == 1) {
      cValue = value.get(0);
    } else {
      StringBuffer sb = new StringBuffer();
      for (String v : value) {
        sb.append(v);
        sb.append(", ");
      }
      cValue = sb.toString();
    }
    cell.setCellValue(cValue);
    return newColumnIndex;
  }

  /**
   * Insert body cell to workbook.
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex, final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

  /**
   * Insert body cell to workbook.
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex,
      final Timestamp value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(DateTimeConverterUtil.dateAndTimeToString(value));
    }
    return newColumnIndex;
  }

  /**
   * Insert header cell to workbook.
   *
   * @param headerRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the cell value.
   * @return the next column index.
   */
  protected int insertHeaderCell(final HSSFRow headerRow, final int columnIndex,
      final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = headerRow.createCell(newColumnIndex++);
    cell.setCellStyle(headerCellStyle);
    cell.setCellValue(value);
    return newColumnIndex;
  }

}
