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
package org.everit.jira.reporting.plugin.query;

import java.util.List;

import org.everit.jira.querydsl.schema.QNodeassociation;
import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.VersionSummaryDTO;

import com.atlassian.jira.entity.Entity;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.SQLQuery;

/**
 * Queries for version summary report.
 */
public class VersionSummaryReportQueryBuilder extends AbstractReportQuery<VersionSummaryDTO> {

  private QNodeassociation qNodeassociation;

  private QProjectversion qProjectversion;

  /**
   * Simple constructor.
   */
  public VersionSummaryReportQueryBuilder(final ReportSearchParam reportSearchParam) {
    super(reportSearchParam);
    qNodeassociation = new QNodeassociation("nodeassocitation");
    qProjectversion = new QProjectversion("m_p_version");
  }

  private Expression<?>[] createQueryGroupBy() {
    return new Expression<?>[] {
        qProject.pkey,
        qProjectversion.vname,
        qProjectversion.released,
        qProjectversion.archived,
        qProjectversion.startdate,
        qProjectversion.releasedate,
        qProjectversion.description };
  }

  private QBean<VersionSummaryDTO> createQuerySelectProjection() {
    return Projections.bean(VersionSummaryDTO.class,
        qProject.pkey.as(VersionSummaryDTO.AliasNames.PROJECT_KEY),
        qProjectversion.vname.as(VersionSummaryDTO.AliasNames.VERSION_NAME),
        qProjectversion.released.as(VersionSummaryDTO.AliasNames.VERSION_RELEASED),
        qProjectversion.archived.as(VersionSummaryDTO.AliasNames.VERSION_ARCHIVED),
        qProjectversion.startdate.as(VersionSummaryDTO.AliasNames.VERSION_START_DATE),
        qProjectversion.releasedate.as(VersionSummaryDTO.AliasNames.VERSION_RELEASE_DATE),
        qProjectversion.description.as(VersionSummaryDTO.AliasNames.VERSION_DESCRIPTION),
        qIssue.timeoriginalestimate.min()
            .as(VersionSummaryDTO.AliasNames.ISSUE_ORIGINAL_ESTIMATE_SUM),
        qIssue.timeestimate.min().as(VersionSummaryDTO.AliasNames.ISSUE_TIME_ESTIMATE_SUM),
        qWorklog.timeworked.sum().as(VersionSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM));
  }

  private <T> SQLQuery<T> extendJoin(final SQLQuery<T> sqlQuery) {
    return sqlQuery.join(qNodeassociation).on(qNodeassociation.sourceNodeId.eq(qIssue.id)
        .and(qNodeassociation.sourceNodeEntity.eq(Entity.Name.ISSUE))
        .and(qNodeassociation.sinkNodeEntity.eq(Entity.Name.VERSION)))
        .join(qProjectversion).on(qProjectversion.id.eq(qNodeassociation.sinkNodeId));
  }

  @Override
  protected QuerydslCallable<Long> getCountQuery() {
    return (connection, configuration) -> {
      NumberPath<Long> projectCountPath = Expressions.numberPath(Long.class,
          new PathMetadata(null, "projectCount", PathType.VARIABLE));

      SQLQuery<Long> fromQuery = new SQLQuery<Long>(connection, configuration)
          .select(qProject.id.count().as(projectCountPath));

      appendBaseFromAndJoin(fromQuery);

      fromQuery = extendJoin(fromQuery);

      appendBaseWhere(fromQuery);

      fromQuery.groupBy(qProject.id);

      SQLQuery<Long> query = new SQLQuery<Long>(connection, configuration)
          .select(projectCountPath.count())
          .from(fromQuery.as("fromCount"));

      return query.fetchOne();
    };
  }

  @Override
  protected QuerydslCallable<List<VersionSummaryDTO>> getQuery() {
    return (connection, configuration) -> {

      SQLQuery<VersionSummaryDTO> query = new SQLQuery<VersionSummaryDTO>(connection, configuration)
          .select(createQuerySelectProjection());

      appendBaseFromAndJoin(query);

      query = extendJoin(query);

      appendBaseWhere(query);
      appendQueryRange(query);

      query.groupBy(createQueryGroupBy());

      return query.fetch();
    };
  }
}
