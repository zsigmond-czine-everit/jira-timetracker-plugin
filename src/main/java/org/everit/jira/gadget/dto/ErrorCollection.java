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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Error collection class for gadget configuration.
 */
@XmlRootElement
public class ErrorCollection {
  /**
   * Generic error messages.
   */
  @XmlElement
  private Collection<String> errorMessages = new ArrayList<String>();

  /**
   * Errors specific to a certain field.
   */
  @XmlElement
  private Collection<ValidationError> errors = new ArrayList<ValidationError>();

  public void addError(final String field, final String message) {
    errors.add(new ValidationError(message, field));
  }

  public Collection<String> getErrorMessages() {
    return errorMessages;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}
