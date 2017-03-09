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
 * Contains information to version summary report table.
 */
public class VersionSummaryReportDTO {

  private PagingDTO paging = new PagingDTO();

  private List<VersionSummaryDTO> versionSummaries = Collections.emptyList();

  private Long versionSummaryCount = 0L;

  public PagingDTO getPaging() {
    return paging;
  }

  public List<VersionSummaryDTO> getVersionSummaries() {
    return versionSummaries;
  }

  public Long getVersionSummaryCount() {
    return versionSummaryCount;
  }

  public VersionSummaryReportDTO paging(final PagingDTO paging) {
    this.paging = paging;
    return this;
  }

  public VersionSummaryReportDTO versionSummaries(final List<VersionSummaryDTO> versionSummaries) {
    this.versionSummaries = versionSummaries;
    return this;
  }

  public VersionSummaryReportDTO versionSummaryCount(final Long versionSummaryCount) {
    this.versionSummaryCount = versionSummaryCount;
    return this;
  }

}
