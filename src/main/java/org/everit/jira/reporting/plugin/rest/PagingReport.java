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
package org.everit.jira.reporting.plugin.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.ComponentSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ReportingQueryParams;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.VersionSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.velocity.VelocityManager;
import com.google.gson.Gson;

/**
 * Responsible to help table report paging.
 */
@Path("/paging-report")
public class PagingReport {

  private static final String ENCODING = "UTF-8";

  private static final String TEMPLATE_DIRECTORY = "/templates/reporting/";

  private Gson gson;

  private ReportingPlugin reportingPlugin;

  private TimeTrackerSettingsHelper settingsHelper;

  private VelocityManager velocityManager;

  /**
   * Simple constructor. Initialize required members.
   */
  public PagingReport(final ReportingPlugin reportingPlugin,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.reportingPlugin = reportingPlugin;
    this.settingsHelper = settingsHelper;
    gson = new Gson();
    velocityManager = ComponentAccessor.getVelocityManager();
  }

  private void appendRequiredContextParameters(final Map<String, Object> contextParameters,
      final FilterCondition filterCondition) {
    contextParameters.put("durationFormatter", new DurationFormatter());
    contextParameters.put("filterCondition", filterCondition);

    contextParameters.put("dateTimeFormatterDate", getDateTimeFormatterDate());
    contextParameters.put("dateTimeFormatterDateTime", getDateTimeFormatterDateTime());

    IssueRenderContext issueRenderContext = new IssueRenderContext(null);
    contextParameters.put("issueRenderContext", issueRenderContext);

    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    JiraRendererPlugin atlassianWikiRenderer =
        rendererManager.getRendererForType("atlassian-wiki-renderer");
    contextParameters.put("atlassianWikiRenderer", atlassianWikiRenderer);

    contextParameters.put("contextPath", getContextPath());

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(locale);
    contextParameters.put("i18n", i18nHelper);
  }

  private void appendSummaryToContextParameters(final String subTabForProcess,
      final HashMap<String, Object> contextParameters,
      final ConvertedSearchParam converSearchParam) {
    if (PageTabs.SUB_PROJECT.equals(subTabForProcess)) {
      ProjectSummaryReportDTO projectSummaryReport =
          reportingPlugin.getProjectSummaryReport(converSearchParam.reportSearchParam);
      contextParameters.put("projectSummaryReport", projectSummaryReport);
    } else if (PageTabs.SUB_ISSUE.equals(subTabForProcess)) {
      IssueSummaryReportDTO issueSummaryReport =
          reportingPlugin.getIssueSummaryReport(converSearchParam.reportSearchParam);
      contextParameters.put("issueSummaryReport", issueSummaryReport);
    } else if (PageTabs.SUB_USER.equals(subTabForProcess)) {
      UserSummaryReportDTO userSummaryReport =
          reportingPlugin.getUserSummaryReport(converSearchParam.reportSearchParam);
      contextParameters.put("userSummaryReport", userSummaryReport);
    } else if (PageTabs.SUB_VERSION.equals(subTabForProcess)) {
      VersionSummaryReportDTO versionSummaryReport =
          reportingPlugin.getVersionSummaryReport(converSearchParam.reportSearchParam);
      contextParameters.put("versionSummaryReport", versionSummaryReport);
    } else if (PageTabs.SUB_COMPONENT.equals(subTabForProcess)) {
      ComponentSummaryReportDTO componentSummaryReport =
          reportingPlugin.getComponentSummaryReport(converSearchParam.reportSearchParam);
      contextParameters.put("componentSummaryReport", componentSummaryReport);
    }
  }

  private Response buildResponse(final String templateFileName,
      final Map<String, Object> contextParameters) {
    String encodedBody = velocityManager.getEncodedBody(TEMPLATE_DIRECTORY,
        templateFileName,
        ENCODING,
        contextParameters);
    return Response.ok(encodedBody).build();
  }

  private FilterCondition convertJsonToFilterCondition(final String filterConditionJson) {
    return gson.fromJson(filterConditionJson, FilterCondition.class);
  }

  private String getContextPath() {
    return ComponentAccessor.getWebResourceUrlProvider().getBaseUrl();
  }

  private DateTimeFormatter getDateTimeFormatterDate() {
    return ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class).formatter()
        .forLoggedInUser().withStyle(DateTimeStyle.DATE).withSystemZone();
  }

  private DateTimeFormatter getDateTimeFormatterDateTime() {
    return ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class).formatter()
        .withStyle(DateTimeStyle.COMPLETE).withSystemZone();
  }

  /**
   * Gets page based on main and sub tab.
   *
   * @param mainTab
   *          the main tab ( {@link PageTabs} ).
   * @param subTab
   *          the sub tab ( {@link PageTabs} ).
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   * @param selectedColumnsJson
   *          the JSON array from the selected columns for details report.
   * @param orderByString
   *          the name of column how to order by details report.
   * @return the correct page HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/page")
  public Response getPage(
      @QueryParam("maintab") final String mainTab, @QueryParam("subtab") final String subTab,
      @QueryParam("filterConditionJson") final String filterConditionJson,
      @QueryParam("selectedColumnsJson") final String selectedColumnsJson,
      @QueryParam("orderBy") final String orderByString) {
    TimeTrackerUserSettings loadUserSettings = settingsHelper.loadUserSettings();
    ReportingQueryParams reportinQueryParams = loadUserSettings.getReportinQueryParams();

    Response response = null;
    if (PageTabs.MAIN_DETAILS.equals(mainTab)) {
      String selectedColumnsJsonForProcess = selectedColumnsJson;
      if ((selectedColumnsJson == null) || "".equals(selectedColumnsJson)) {
        selectedColumnsJsonForProcess = reportinQueryParams.selectedWorklogDetailsColumnsJson;
      }
      response =
          pageWorklogDetails(filterConditionJson, selectedColumnsJsonForProcess, orderByString);

      reportinQueryParams.selectedWorklogDetailsColumnsJson(selectedColumnsJsonForProcess);
    } else if (PageTabs.MAIN_SUMMARIES.equals(mainTab)) {
      String subTabForProcess = subTab;
      if ((subTab == null) || "".equals(subTabForProcess)) {
        subTabForProcess = reportinQueryParams.selectedActiveTab;
      }
      FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

      HashMap<String, Object> contextParameters = new HashMap<>();
      ConvertedSearchParam converSearchParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

      appendSummaryToContextParameters(subTabForProcess, contextParameters, converSearchParam);

      Long grandTotal = reportingPlugin.getGrandTotal(converSearchParam.reportSearchParam);

      contextParameters.put("selectedActiveTab", subTabForProcess);
      contextParameters.put("grandTotal", grandTotal);

      appendRequiredContextParameters(contextParameters, filterCondition);

      response = buildResponse("reporting_result_summaries.vm", contextParameters);

      reportinQueryParams.selectedActiveTab(subTabForProcess);
    } else {
      return Response.status(Status.BAD_REQUEST)
          .build();
    }

    reportinQueryParams.selectedMainActiveTab(mainTab);
    loadUserSettings.setReportingQueryParams(reportinQueryParams);
    settingsHelper.saveUserSettings(loadUserSettings);

    return response;
  }

  /**
   * Paging the component summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageComponentSummary")
  public Response pageComponentSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    ComponentSummaryReportDTO componentSummaryReport =
        reportingPlugin.getComponentSummaryReport(converSearchParam.reportSearchParam);

    Map<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("componentSummaryReport", componentSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_component_summary.vm", contextParameters);
  }

  /**
   * Paging the issue summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageIssueSummary")
  public Response pageIssueSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    IssueSummaryReportDTO issueSummaryReport =
        reportingPlugin.getIssueSummaryReport(converSearchParam.reportSearchParam);

    Map<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("issueSummaryReport", issueSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_issue_summary.vm", contextParameters);
  }

  /**
   * Paging the project summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageProjectSummary")
  public Response pageProjectSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    ProjectSummaryReportDTO projectSummaryReport =
        reportingPlugin.getProjectSummaryReport(converSearchParam.reportSearchParam);

    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("projectSummaryReport", projectSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_project_summary.vm", contextParameters);
  }

  /**
   * Paging the user summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageUserSummary")
  public Response pageUserSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    UserSummaryReportDTO userSummaryReport =
        reportingPlugin.getUserSummaryReport(converSearchParam.reportSearchParam);

    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("userSummaryReport", userSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_user_summary.vm", contextParameters);
  }

  /**
   * Paging the version summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageVersionSummary")
  public Response pageVersionSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    VersionSummaryReportDTO versionSummaryReport =
        reportingPlugin.getVersionSummaryReport(converSearchParam.reportSearchParam);

    Map<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("versionSummaryReport", versionSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_version_summary.vm", contextParameters);
  }

  /**
   * Paging the worklog details report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   * @param selectedColumnsJson
   *          the JSON array from the selected columns.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageWorklogDetails")
  public Response pageWorklogDetails(
      @QueryParam("filterConditionJson") final String filterConditionJson,
      @QueryParam("selectedColumnsJson") final String selectedColumnsJson,
      @QueryParam("orderBy") final String orderByString) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    String[] selectedColumns = gson.fromJson(selectedColumnsJson, String[].class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    OrderBy orderBy = ConverterUtil.convertToOrderBy(orderByString);

    WorklogDetailsReportDTO worklogDetailsReport =
        reportingPlugin.getWorklogDetailsReport(converSearchParam.reportSearchParam, orderBy);

    Long grandTotal = reportingPlugin.getGrandTotal(converSearchParam.reportSearchParam);
    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("worklogDetailsReport", worklogDetailsReport);
    contextParameters.put("selectedWorklogDetailsColumns", Arrays.asList(selectedColumns));
    contextParameters.put("grandTotal", grandTotal);

    appendRequiredContextParameters(contextParameters, filterCondition);

    contextParameters.put("order", orderBy.order);
    contextParameters.put("orderColumn", orderBy.columnName);

    TimeTrackerUserSettings loadUserSettings = settingsHelper.loadUserSettings();
    ReportingQueryParams reportinQueryParams = loadUserSettings.getReportinQueryParams();
    reportinQueryParams.selectedWorklogDetailsColumnsJson(selectedColumnsJson);
    loadUserSettings.setReportingQueryParams(reportinQueryParams);
    settingsHelper.saveUserSettings(loadUserSettings);

    return buildResponse("reporting_result_worklog_details.vm", contextParameters);
  }
}
