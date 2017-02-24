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
package org.everit.jira.core.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.RemainingEstimateType;
import org.everit.jira.core.dto.WorklogParameter;
import org.everit.jira.core.util.WorklogUtil;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.EveritWorklogComparator;
import org.everit.jira.timetracker.plugin.exception.WorklogException;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl.Builder;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Implementation of {@link EVWorklogManager}.
 */
public class WorklogComponent implements EVWorklogManager {

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String DATE_PARSE = "plugin.date_parse";

    public static final String INVALID_ISSUE = "plugin.invalid_issue";

    public static final String NOPERMISSION_CREATE_WORKLOG = "jttp.nopermission.worklog.create";

    public static final String NOPERMISSION_DELETE_WORKLOG = "jttp.nopermission.worklog.delete";

    public static final String NOPERMISSION_ISSUE = "plugin.nopermission_issue";

    public static final String NOPERMISSION_UPDATE_WORKLOG = "jttp.nopermission.worklog.update";

    public static final String WORKLOG_CREATE_FAIL = "plugin.worklog.create.fail";

    public static final String WORKLOG_DELETE_FAIL = "plugin.worklog.delete.fail";

    public static final String WORKLOG_NOT_EXISTS = "plugin.worklog.not.exists";

    public static final String WORKLOG_UPDATE_FAIL = "plugin.worklog.update.fail";

    private PropertiesKey() {
    }
  }

  private void checkPermissionOnIssue(final ApplicationUser user,
      final List<MutableIssue> issues) {
    StringBuilder wrongIssueKeys = new StringBuilder();
    for (MutableIssue issue : issues) {
      PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
      if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue, user)
          || !permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, user)) {
        if (!(wrongIssueKeys.length() == 0)) {
          wrongIssueKeys.append(", ");
        }
        wrongIssueKeys.append(issue.getKey());
      }
    }
    if (!(wrongIssueKeys.length() == 0)) {
      throw new WorklogException(PropertiesKey.NOPERMISSION_ISSUE, wrongIssueKeys.toString());
    }
  }

  private void checkPermissionWorklogCreate(final List<MutableIssue> issues,
      final JiraServiceContext serviceContext, final WorklogService worklogService) {
    StringBuilder wrongIssueKeys = new StringBuilder();
    for (MutableIssue issue : issues) {
      if (!worklogService.hasPermissionToCreate(serviceContext, issue, true)) {
        if (!(wrongIssueKeys.length() == 0)) {
          wrongIssueKeys.append(", ");
        }
        wrongIssueKeys.append(issue.getKey());
      }
    }
    if (!(wrongIssueKeys.length() == 0)) {
      throw new WorklogException(PropertiesKey.NOPERMISSION_CREATE_WORKLOG,
          wrongIssueKeys.toString());
    }
  }

  @Override
  public long countWorklogsWithoutPermissionChecks(final DateTimeServer startDateServer,
      final DateTimeServer endDateServer) {

    Calendar startDateCalendar = Calendar.getInstance();
    startDateCalendar.setTime(startDateServer.getSystemTimeZoneDayStartDate());
    Calendar endDateCalendar = (Calendar) startDateCalendar.clone();
    if (endDateServer == null) {
      endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
    } else {
      endDateCalendar = Calendar.getInstance();
      endDateCalendar.setTime(endDateServer.getSystemTimeZoneDayStartDate());
      endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprList(startDateCalendar.getTimeInMillis(),
            endDateCalendar.getTimeInMillis(),
            loggedInUser.getKey());

    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator()
        .findByAnd("IssueWorklogView", exprList);
    return worklogGVList.size();
  }

  @Override
  public void createWorklog(final WorklogParameter worklogParameter) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    List<MutableIssue> issues = validateIssueKeys(worklogParameter, issueManager);
    checkPermissionOnIssue(user, issues);
    // securityLevel.
    WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
    checkPermissionWorklogCreate(issues, serviceContext, worklogService);

    // TODO start create worklogs, only thing if one fail. others fail
    for (MutableIssue issue : issues) {
      Builder builder = getBuilder(issue,
          worklogParameter.getDate().getSystemTimeZoneDate(),
          worklogParameter.getTimeSpent(),
          worklogParameter.getComment(),
          null);
      RemainingEstimateType remainingEstimateType = worklogParameter.getRemainingEstimateType();
      WorklogInputParameters params = remainingEstimateType.build(builder,
          worklogParameter.getOptinalValue());

      WorklogResult worklogResult = remainingEstimateType.validateCreate(worklogService,
          serviceContext, params);
      if (worklogResult == null) {
        throw new WorklogException(PropertiesKey.WORKLOG_CREATE_FAIL);
      }
      Worklog createdWorklog = remainingEstimateType.create(worklogService, serviceContext,
          worklogResult);
      if (createdWorklog == null) {
        throw new WorklogException(PropertiesKey.WORKLOG_CREATE_FAIL);
      }
    }
  }

  @Override
  public void deleteWorklog(final Long worklogId, final String optionalValue,
      final RemainingEstimateType remainingEstimateType) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
    WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
    Worklog worklog = getWorklogById(worklogId);
    if (!worklogService.hasPermissionToDelete(serviceContext, worklog)) {
      throw new WorklogException(PropertiesKey.NOPERMISSION_DELETE_WORKLOG,
          worklog.getIssue().getKey());
    }
    WorklogResult deleteWorklogResult = remainingEstimateType.validateDelete(worklogService,
        serviceContext, worklogId, optionalValue);
    if (deleteWorklogResult == null) {
      throw new WorklogException(PropertiesKey.WORKLOG_DELETE_FAIL, worklogId.toString());
    }

    remainingEstimateType.delete(worklogService, serviceContext, deleteWorklogResult);
  }

  @Override
  public void editWorklog(final Long worklogId, final WorklogParameter worklogParameter) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);

    Worklog worklog = getWorklogById(worklogId);
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    List<MutableIssue> issues = validateIssueKeys(worklogParameter, issueManager);

    for (MutableIssue issue : issues) {
      if (!worklog.getIssue().getKey().equals(issue.getKey())) {
        WorklogParameter createWorklogParameter =
            new WorklogParameter(Arrays.asList(issue.getKey()),
                worklogParameter.getComment(),
                worklogParameter.getDate(),
                worklogParameter.getTimeSpent(),
                worklogParameter.getOptinalValue(),
                worklogParameter.getRemainingEstimateType());
        createWorklog(createWorklogParameter);
      } else {
        WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
        if (!worklogService.hasPermissionToUpdate(serviceContext, worklog)) {
          throw new WorklogException(PropertiesKey.NOPERMISSION_UPDATE_WORKLOG, issue.getKey());
        }

        Builder builder =
            getBuilder(issue, worklogParameter.getDate().getSystemTimeZoneDate(),
                worklogParameter.getTimeSpent(), worklogParameter.getComment(), worklogId);
        RemainingEstimateType remainingEstimateType = worklogParameter.getRemainingEstimateType();
        WorklogInputParameters params = remainingEstimateType.build(builder,
            worklogParameter.getOptinalValue());

        WorklogResult worklogResult = remainingEstimateType.validateUpdate(worklogService,
            serviceContext, params);
        if (worklogResult == null) {
          throw new WorklogException(PropertiesKey.WORKLOG_UPDATE_FAIL);
        }

        remainingEstimateType.update(worklogService, serviceContext, worklogResult);

      }
    }
    if (!issues.stream().filter(x -> worklog.getIssue().getKey().equals(x.getKey())).findAny()
        .isPresent()) {
      deleteWorklog(worklogId, null, RemainingEstimateType.AUTO);
    }
  }

  private WorklogInputParametersImpl.Builder getBuilder(final MutableIssue issue,
      final Date startDate, final String timeSpent, final String comment, final Long worklogId) {
    Builder builder = WorklogInputParametersImpl.issue(issue)
        .startDate(startDate)
        .timeSpent(timeSpent)
        .comment(comment);

    if (worklogId != null) {
      builder.worklogId(worklogId);
    }
    return builder;
  }

  @Override
  public EveritWorklog getWorklog(final Long worklogId) throws ParseException, WorklogException {
    return new EveritWorklog(getWorklogById(worklogId));
  }

  private Worklog getWorklogById(final Long worklogId) throws WorklogException {
    WorklogManager worklogManager = ComponentAccessor.getWorklogManager();
    Worklog worklog = worklogManager.getById(worklogId);
    if (worklog == null) {
      throw new WorklogException(PropertiesKey.WORKLOG_NOT_EXISTS);
    }
    return worklog;
  }

  @Override
  public List<EveritWorklog> getWorklogs(final String selectedUser,
      final DateTimeServer startDateServer,
      final DateTimeServer endDateServer) throws DataAccessException, ParseException {

    Calendar startDateCalendar = Calendar.getInstance();
    startDateCalendar.setTime(startDateServer.getSystemTimeZoneDayStartDate());
    Calendar endDateCalendar = (Calendar) startDateCalendar.clone();
    if (endDateServer == null) {
      endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
    } else {
      endDateCalendar = Calendar.getInstance();
      endDateCalendar.setTime(endDateServer.getSystemTimeZoneDayStartDate());
      endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
    }

    List<EveritWorklog> worklogs = new ArrayList<>();

    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();

    String userKey;
    if ((selectedUser == null) || "".equals(selectedUser)) {
      userKey = loggedInUser.getKey();
    } else {
      userKey = ComponentAccessor.getUserUtil().getUserByName(selectedUser).getKey();
    }

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(userKey,
            loggedInUser, startDateCalendar.getTimeInMillis(), endDateCalendar.getTimeInMillis());

    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator()
        .findByAnd("IssueWorklogView", exprList);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    ProjectRoleManager projectRoleManager =
        ComponentAccessor.getComponent(ProjectRoleManager.class);

    for (GenericValue worklogGv : worklogGVList) {
      boolean hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(loggedInUser,
          issueManager,
          groupManager,
          projectRoleManager,
          permissionManager,
          worklogGv);
      if (hasWorklogVisibility) {
        EveritWorklog worklog = new EveritWorklog(worklogGv);
        worklogs.add(worklog);
      }
    }

    Collections.sort(worklogs, EveritWorklogComparator.INSTANCE);
    return worklogs;
  }

  private List<MutableIssue> validateIssueKeys(final WorklogParameter worklogParameter,
      final IssueManager issueManager) {
    List<MutableIssue> issues = new ArrayList<>();
    StringBuilder wrongIssueKeys = new StringBuilder();
    for (String issueKey : worklogParameter.getIssueKeys()) {
      MutableIssue issue = issueManager.getIssueObject(issueKey);
      if (issue == null) {
        if (!(wrongIssueKeys.length() == 0)) {
          wrongIssueKeys.append(", ");
        }
        wrongIssueKeys.append(issueKey);
      } else {
        issues.add(issue);
      }
    }
    if (!(wrongIssueKeys.length() == 0)) {
      throw new WorklogException(PropertiesKey.INVALID_ISSUE, wrongIssueKeys.toString());
    }
    return issues;
  }

}
