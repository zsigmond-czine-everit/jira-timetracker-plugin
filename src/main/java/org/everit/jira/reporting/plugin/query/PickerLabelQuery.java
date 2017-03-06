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

import org.everit.jira.querydsl.schema.QLabel;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.PickerLabelDTO;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets labels to picker.
 */
public class PickerLabelQuery implements QuerydslCallable<List<PickerLabelDTO>> {

  private final long limit;

  private QLabel qLabel;

  private final String query;

  /**
   * Simple constructor.
   */
  public PickerLabelQuery(final String query, final long limit) {
    qLabel = new QLabel("label");
    this.query = query;
    this.limit = limit;
  }

  @Override
  public List<PickerLabelDTO> call(final Connection connection, final Configuration configuration)
      throws SQLException {

    SQLQuery<PickerLabelDTO> sqlQuery = new SQLQuery<PickerLabelDTO>(connection, configuration)
        .select(Projections.bean(PickerLabelDTO.class,
            qLabel.label.as(PickerLabelDTO.AliasNames.LABEL_NAME)))
        .from(qLabel)
        .groupBy(qLabel.label)
        .orderBy(qLabel.label.asc())
        .limit(limit);
    if (query != null) {
      sqlQuery = sqlQuery.where(qLabel.label.toLowerCase()
          .like("%" + query.toLowerCase(Locale.getDefault()) + "%"));
    }

    List<PickerLabelDTO> result = sqlQuery
        .fetch();

    return result;
  }

}
