package org.everit.jira.reporting.plugin.export;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;

import com.atlassian.jira.exception.DataAccessException;

public class ExportTableReport extends AbstractExporter {

  private static final String[] HEADER_KEYS = { "jtrp.table.report.header.date",
      "jtrp.table.report.header.issue",
      "jtrp.table.report.header.summary",
      "jtrp.table.report.header.remaining",
      "jtrp.table.report.header.start",
      "jtrp.table.report.header.end",
      "jtrp.table.report.header.duration",
      "jtrp.table.report.header.note" };

  private DateTimeServer endDate;

  private String selectedUser;

  private DateTimeServer startDate;

  private EVWorklogManager worklogManager;

  public ExportTableReport(final EVWorklogManager worklogManager, final String selectedUser,
      final DateTimeServer startDate,
      final DateTimeServer endDate) {
    this.worklogManager = worklogManager;
    this.selectedUser = selectedUser;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    List<EveritWorklog> worklogs = new ArrayList<>();
    try {
      worklogs.addAll(worklogManager.getWorklogs(selectedUser, startDate, endDate));
    } catch (DataAccessException | ParseException e) {
      // TODO
      return;
    }

    HSSFSheet worklogDetailsSheet = workbook.createSheet("Table report");
    int rowIndex = 0;
    HSSFRow headerRow = worklogDetailsSheet.createRow(rowIndex++);
    int columnIndex = 0;
    for (String headerKey : HEADER_KEYS) {
      columnIndex = insertHeaderCell(headerRow, columnIndex, i18nHelper.getText(headerKey));
    }
    for (EveritWorklog worklog : worklogs) {
      HSSFRow row = worklogDetailsSheet.createRow(rowIndex++);
      columnIndex = 0;

      columnIndex = insertBodyCell(row, columnIndex, worklog.getStartDate());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getIssue());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getIssueSummary());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getRoundedRemaining());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getStartTime());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getEndTime());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getDuration());
      columnIndex = insertBodyCell(row, columnIndex, worklog.getBody());
    }
  }

}
