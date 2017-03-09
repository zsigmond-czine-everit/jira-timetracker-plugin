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
package org.everit.jira.gadget.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO class for the Flot gadgets data.
 */
@XmlRootElement(name = "data")
public class GadgetDataDTO {

  @XmlElement
  private String color;

  @XmlElement
  private Object data;

  @XmlElement
  private String label;

  /**
   * Constructor with all fields.
   */
  public GadgetDataDTO(final Object data, final String label, final String color) {
    this.data = data;
    this.label = label;
    this.color = color;
  }

  public String getColor() {
    return color;
  }

  public Object getData() {
    return data;
  }

  public String getLabel() {
    return label;
  }
}
