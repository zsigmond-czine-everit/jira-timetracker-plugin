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

import java.util.ArrayList;
import java.util.List;

/**
 * The container for version picker to GUI.
 */
public class VersionPickerContainerDTO {

  private List<PickerVersionDTO> issueAffectedVersions = new ArrayList<>();

  private List<PickerVersionDTO> issueFixedVersions = new ArrayList<>();

  private PickerVersionDTO noVersion = PickerVersionDTO.createNoVersion();

  private PickerVersionDTO releasedVersion = PickerVersionDTO.createReleasedVersion();

  private List<PickerVersionDTO> suggestedVersions = new ArrayList<>();

  private PickerVersionDTO unReleasedVersion = PickerVersionDTO.createUnReleasedVersion();

  public List<PickerVersionDTO> getIssueAffectedVersions() {
    return issueAffectedVersions;
  }

  public List<PickerVersionDTO> getIssueFixedVersions() {
    return issueFixedVersions;
  }

  public PickerVersionDTO getNoVersion() {
    return noVersion;
  }

  public PickerVersionDTO getReleasedVersion() {
    return releasedVersion;
  }

  public List<PickerVersionDTO> getSuggestedVersions() {
    return suggestedVersions;
  }

  public PickerVersionDTO getUnReleasedVersion() {
    return unReleasedVersion;
  }

  public void setIssueAffectedVersions(final List<PickerVersionDTO> issueAffectedVersions) {
    this.issueAffectedVersions = issueAffectedVersions;
  }

  public void setIssueFixedVersions(final List<PickerVersionDTO> issueFixedVersions) {
    this.issueFixedVersions = issueFixedVersions;
  }

  public void setSuggestedVersions(final List<PickerVersionDTO> suggestedVersions) {
    this.suggestedVersions = suggestedVersions;
  }

}
