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
 * The container for label picker to GUI.
 */
public class LabelPickerContainerDTO {

  private List<PickerLabelDTO> labels = new ArrayList<>();

  private List<PickerLabelDTO> suggestedLabels = new ArrayList<>();

  public List<PickerLabelDTO> getLabels() {
    return labels;
  }

  public List<PickerLabelDTO> getSuggestedLabels() {
    return suggestedLabels;
  }

  public void setLabels(final List<PickerLabelDTO> labels) {
    this.labels = labels;
  }

  public void setSuggestedLabels(final List<PickerLabelDTO> suggestedLabels) {
    this.suggestedLabels = suggestedLabels;
  }
}
