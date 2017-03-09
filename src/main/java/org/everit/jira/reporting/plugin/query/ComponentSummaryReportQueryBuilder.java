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

import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.schema.QNodeassociation;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.ComponentSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;

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
 * Queries for component summary report.
 */
public class ComponentSummaryReportQueryBuilder extends AbstractReportQuery<ComponentSummaryDTO> {

  private QComponent qComponent;

  private QNodeassociation qNodeassociation;

  /**
   * Simple constructor.
   */
  public ComponentSummaryReportQueryBuilder(final ReportSearchParam reportSearchParam) {
    super(reportSearchParam);
    qNodeassociation = new QNodeassociation("nodeassocitation");
    qComponent = new QComponent("m_component");
  }

  private Expression<?>[] createQueryGroupBy() {
    return new Expression<?>[] {
        qProject.pkey,
        qComponent.cname,
        qComponent.description,
        qComponent.lead };
  }

  private QBean<ComponentSummaryDTO> createQuerySelectProjection() {
    return Projections.bean(ComponentSummaryDTO.class,
        qProject.pkey.as(ComponentSummaryDTO.AliasNames.PROJECT_KEY),
        qComponent.cname.as(ComponentSummaryDTO.AliasNames.COMPONENT_NAME),
        qComponent.description.as(ComponentSummaryDTO.AliasNames.COMPONENT_DESCRIPTION),
        qComponent.lead.as(ComponentSummaryDTO.AliasNames.COMPONENT_LEAD),
        qIssue.timeoriginalestimate.min()
            .as(ComponentSummaryDTO.AliasNames.ISSUE_ORIGINAL_ESTIMATE_SUM),
        qIssue.timeestimate.min().as(ComponentSummaryDTO.AliasNames.ISSUE_TIME_ESTIMATE_SUM),
        qWorklog.timeworked.sum().as(ComponentSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM));
  }

  private <T> SQLQuery<T> extendJoin(final SQLQuery<T> sqlQuery) {
    return sqlQuery.join(qNodeassociation).on(qNodeassociation.sourceNodeId.eq(qIssue.id)
        .and(qNodeassociation.sourceNodeEntity.eq(Entity.Name.ISSUE))
        .and(qNodeassociation.sinkNodeEntity.eq(Entity.Name.COMPONENT)))
        .join(qComponent).on(qComponent.id.eq(qNodeassociation.sinkNodeId));
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
  protected QuerydslCallable<List<ComponentSummaryDTO>> getQuery() {
    return (connection, configuration) -> {

      SQLQuery<ComponentSummaryDTO> query =
          new SQLQuery<ComponentSummaryDTO>(connection, configuration)
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
