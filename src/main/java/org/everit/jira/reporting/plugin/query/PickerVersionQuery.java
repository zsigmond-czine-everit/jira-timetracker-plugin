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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.PickerVersionDTO;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets versions to picker.
 */
public class PickerVersionQuery implements QuerydslCallable<List<PickerVersionDTO>> {

  private final long limit;

  private QProjectversion qProjectversion;

  private final String query;

  /**
   * Simple constructor.
   */
  public PickerVersionQuery(final String query, final long limit) {
    qProjectversion = new QProjectversion("p_version");
    this.query = query;
    this.limit = limit;
  }

  @Override
  public List<PickerVersionDTO> call(final Connection connection, final Configuration configuration)
      throws SQLException {

    SQLQuery<PickerVersionDTO> sqlQuery = new SQLQuery<PickerVersionDTO>(connection, configuration)
        .select(Projections.bean(PickerVersionDTO.class,
            qProjectversion.vname.as(PickerVersionDTO.AliasNames.VERSION_NAME)))
        .from(qProjectversion)
        .groupBy(qProjectversion.vname)
        .orderBy(qProjectversion.vname.asc())
        .limit(limit);
    if (query != null) {
      sqlQuery = sqlQuery.where(qProjectversion.vname.toLowerCase()
          .like("%" + query.toLowerCase(Locale.getDefault()) + "%"));
    }
    List<PickerVersionDTO> result = sqlQuery.fetch();

    return result;
  }

}
