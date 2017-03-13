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
 * An error element.
 */
@XmlRootElement
public class ValidationError {

  /**
   * The Error key.
   */
  @XmlElement
  private String error;

  /**
   * The field the error relates to.
   */
  @XmlElement
  private String field;

  public ValidationError(final String error, final String field) {
    this.error = error;
    this.field = field;
  }

  public String getError() {
    return error;
  }

  public String getField() {
    return field;
  }

}
