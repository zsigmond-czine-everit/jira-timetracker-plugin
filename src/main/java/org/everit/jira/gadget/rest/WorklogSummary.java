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
package org.everit.jira.gadget.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.gadget.dto.ErrorCollection;
import org.everit.jira.gadget.dto.GadgetDataDTO;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.SearcherValue;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.google.gson.Gson;

/**
 *
 * Rest interface for the worklog summary gadget.
 *
 */
@Path("/worklog-summary-gadget")
public class WorklogSummary {

  private static final int DAYS_30 = 30;

  private static final int DAYS_OF_WEEK = 7;

  private static final double SECONDS_TO_HOUR = 3600.0;

  private Gson gson = new Gson();

  private ReportingPlugin reportingPlugin;

  private TimeTrackerSettingsHelper settingsHelper;

  public WorklogSummary(final ReportingPlugin reportingPlugin,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.reportingPlugin = reportingPlugin;
    this.settingsHelper = settingsHelper;
  }

  private Response buildResponse(final String groupBy, final String display,
      final FilterCondition filterCondition) {
    ConvertedSearchParam reportParam = null;
    try {
      reportParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);
    } catch (IllegalArgumentException e) {
      return Response.serverError()
          .entity("Failed to create search parameters, cause: " + e.getMessage())
          .build();
    }
    List<GadgetDataDTO> result = null;

    if ("Issue".equalsIgnoreCase(groupBy)) {
      IssueSummaryReportDTO issueSummaryReport =
          reportingPlugin.getIssueSummaryReport(reportParam.reportSearchParam);
      result = getIssueSummaryGadgetData(issueSummaryReport, display);
    } else if ("User".equalsIgnoreCase(groupBy)) {
      ProjectSummaryReportDTO projectSummaryReport =
          reportingPlugin.getProjectSummaryReport(reportParam.reportSearchParam);
      result = getProjectSummaryGadgetData(projectSummaryReport, display);
    } else {
      UserSummaryReportDTO userSummaryReport =
          reportingPlugin.getUserSummaryReport(reportParam.reportSearchParam);
      result = getUserSummaryGadgetData(userSummaryReport, display);
    }

    return Response
        .ok(result)
        .build();
  }

  /**
   * Get the end date based on the system time and the specified period.
   */
  private long getEndDateFromPeriod(final String period) {
    if ("CurrentWeek".equals(period)) {
      ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
      boolean isMondayTheFirstDay =
          applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);
      return DateTimeConverterUtil.setDateToWeekEnd(
          new DateTime(TimetrackerUtil.getLoggedUserTimeZone()), isMondayTheFirstDay).getMillis();
    } else if ("CurrentMonth".equals(period)) {
      return DateTimeConverterUtil.setDateToMonthEnd(
          new DateTime(TimetrackerUtil.getLoggedUserTimeZone())).getMillis();
    } else {
      return new DateTime(TimetrackerUtil.getLoggedUserTimeZone()).getMillis();
    }
  }

  private List<GadgetDataDTO> getIssueSummaryGadgetData(
      final IssueSummaryReportDTO summaryReportDTO, final String display) {
    List<GadgetDataDTO> result = new ArrayList<>();
    if ("BarChart".equals(display)) {
      for (IssueSummaryDTO dto : summaryReportDTO.getIssueSummaries()) {
        result.add(new GadgetDataDTO(
            new Object[] {
                new Object[] { dto.getIssueKey(), dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR } },
            dto.getIssueKey(), null));
      }
    } else {
      for (IssueSummaryDTO dto : summaryReportDTO.getIssueSummaries()) {
        result.add(new GadgetDataDTO(dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR,
            dto.getIssueKey(), null));
      }
    }
    return result;
  }

  private List<GadgetDataDTO> getProjectSummaryGadgetData(
      final ProjectSummaryReportDTO projectSummaryReport, final String display) {
    List<GadgetDataDTO> result = new ArrayList<>();
    if ("BarChart".equals(display)) {
      for (ProjectSummaryDTO dto : projectSummaryReport.getProjectSummaries()) {
        result.add(new GadgetDataDTO(
            new Object[] {
                new Object[] { dto.getProjectName(),
                    dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR } },
            dto.getProjectKey(), null));
      }
    } else {
      for (ProjectSummaryDTO dto : projectSummaryReport.getProjectSummaries()) {
        result.add(new GadgetDataDTO(dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR,
            dto.getProjectName(), null));
      }
    }
    return result;
  }

  /**
   * Get the start date based on the system time and the specified period.
   */
  private long getStartDateFromPeriod(final String period) {
    if ("CurrentWeek".equals(period)) {
      ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
      boolean isMondayTheFirstDay =
          applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);
      return DateTimeConverterUtil.setDateToWeekStart(
          new DateTime(TimetrackerUtil.getLoggedUserTimeZone()), isMondayTheFirstDay).getMillis();
    } else if ("CurrentMonth".equals(period)) {
      return DateTimeConverterUtil.setDateToMonthStart(
          new DateTime(TimetrackerUtil.getLoggedUserTimeZone())).getMillis();
    } else if ("Last7days".equals(period)) {
      return new DateTime(TimetrackerUtil.getLoggedUserTimeZone()).minusDays(DAYS_OF_WEEK)
          .getMillis();
    } else {
      return new DateTime(TimetrackerUtil.getLoggedUserTimeZone()).minusDays(DAYS_30).getMillis();
    }

  }

  /**
   * Get the user summary gadget data.
   */
  private List<GadgetDataDTO> getUserSummaryGadgetData(final UserSummaryReportDTO userSummaryReport,
      final String display) {
    List<GadgetDataDTO> result = new ArrayList<>();
    if ("BarChart".equals(display)) {
      for (UserSummaryDTO dto : userSummaryReport.getUserSummaries()) {
        result.add(new GadgetDataDTO(
            new Object[] {
                new Object[] { dto.getUserDisplayName(),
                    dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR } },
            dto.getUserDisplayName(), null));
      }
    } else {
      for (UserSummaryDTO dto : userSummaryReport.getUserSummaries()) {
        result.add(new GadgetDataDTO(dto.getWorkloggedTimeSum() / SECONDS_TO_HOUR,
            dto.getUserDisplayName(), null));
      }
    }
    return result;
  }

  /**
   * Get the worklog summary data.
   */
  @Path("/getWorklogSummaryData")
  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getWorklogIssueSummary(@QueryParam("jqlId") final long filterId,
      @QueryParam("period") final String period, @QueryParam("groupBy") final String groupBy,
      @QueryParam("display") final String display) {
    FilterCondition filterCondition = new FilterCondition();
    filterCondition.setWorklogStartDate(getStartDateFromPeriod(period));
    filterCondition.setWorklogEndDate(getEndDateFromPeriod(period));
    filterCondition.setFilter(Arrays.asList(filterId));
    filterCondition.setSearcherValue(SearcherValue.FILTER.lowerCaseValue);
    return buildResponse(groupBy, display, filterCondition);

  }

  /**
   * Get the worklog summary data.
   */
  @Path("/getWorklogSummaryDataForChart")
  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getWorklogSummaryDataForChart(@QueryParam("users") final List<String> users,
      @QueryParam("groupBy") final String groupBy, @QueryParam("display") final String display,
      @QueryParam("start") final String start, @QueryParam("end") final String end) {
    FilterCondition filterCondition = new FilterCondition();
    filterCondition.setWorklogStartDate(Long.valueOf(start));
    filterCondition.setWorklogEndDate(Long.valueOf(end));
    filterCondition.setSearcherValue(SearcherValue.BASIC.lowerCaseValue);
    filterCondition.setGroupUsers(users.stream()
        .map(user -> "users:" + user)
        .collect(Collectors.toList()));
    return buildResponse(groupBy, display, filterCondition);
  }

  /**
   * Validate preferences for the gadget.
   */
  @Path("/validate")
  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public Response validatePrefs(@QueryParam("filterId") final String filterId,
      @QueryParam("groupBy") final String groupBy, @QueryParam("display") final String display,
      @QueryParam("period") final String period) {
    ErrorCollection errors = new ErrorCollection();

    if (StringUtils.isEmpty(filterId)) {
      errors.addError("filterId", "jttp.gadget.validation.filter.id");
    }
    if (StringUtils.isEmpty(groupBy)) {
      errors.addError("groupBy", "jttp.gadget.validation.groupBy");
    }
    if (StringUtils.isEmpty(display)) {
      errors.addError("display", "jttp.gadget.validation.display");
    }
    if (StringUtils.isEmpty(period)) {
      errors.addError("period", "jttp.gadget.validation.period");
    }
    if (errors.hasErrors()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errors)).build();
    } else {
      return Response.ok(new TextMessage("No errors found.")).build();
    }
  }

}
