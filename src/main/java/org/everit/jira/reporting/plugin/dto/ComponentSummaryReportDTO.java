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
package org.everit.jira.reporting.plugin.dto;

import java.util.Collections;
import java.util.List;

/**
 * Contains information to component summary report table.
 */
public class ComponentSummaryReportDTO {

  private List<ComponentSummaryDTO> componentSummaries = Collections.emptyList();

  private Long componentSummaryCount = 0L;

  private PagingDTO paging = new PagingDTO();

  public ComponentSummaryReportDTO componentSummaries(
      final List<ComponentSummaryDTO> componentSummaries) {
    this.componentSummaries = componentSummaries;
    return this;
  }

  public ComponentSummaryReportDTO componentSummaryCount(final Long componentSummaryCount) {
    this.componentSummaryCount = componentSummaryCount;
    return this;
  }

  public List<ComponentSummaryDTO> getComponentSummaries() {
    return componentSummaries;
  }

  public Long getComponentSummaryCount() {
    return componentSummaryCount;
  }

  public PagingDTO getPaging() {
    return paging;
  }

  public ComponentSummaryReportDTO paging(final PagingDTO paging) {
    this.paging = paging;
    return this;
  }

}
