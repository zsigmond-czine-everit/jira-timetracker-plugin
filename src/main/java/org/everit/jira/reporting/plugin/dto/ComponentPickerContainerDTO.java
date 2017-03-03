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
 * The container for component picker to GUI.
 */
public class ComponentPickerContainerDTO {

  private List<PickerComponentDTO> issueComponents = new ArrayList<>();

  private PickerComponentDTO noComponent = PickerComponentDTO.createNoComponent();

  private List<PickerComponentDTO> suggestedComponents = new ArrayList<>();

  public List<PickerComponentDTO> getIssueComponents() {
    return issueComponents;
  }

  public PickerComponentDTO getNoComponent() {
    return noComponent;
  }

  public List<PickerComponentDTO> getSuggestedComponents() {
    return suggestedComponents;
  }

  public void setIssueComponents(final List<PickerComponentDTO> issueComponents) {
    this.issueComponents = issueComponents;
  }

  public void setSuggestedComponents(final List<PickerComponentDTO> suggestedComponents) {
    this.suggestedComponents = suggestedComponents;
  }

}
