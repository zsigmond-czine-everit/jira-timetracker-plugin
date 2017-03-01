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
package org.everit.jira.tests.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SimpleTes {
  private static class Example {
    private List<Integer> elements;

    public Example(final List<Integer> elements) {
      super();
      this.elements = elements;
    }

    public void order() {
      Collections.sort(elements);
    }
  }

  @Test
  public void test1() {
    List<Integer> elements = new ArrayList<>();
    elements.add(5);
    elements.add(4);
    elements.add(1);
    elements.add(17);
    Example example = new Example(elements);
    example.order();
    System.out.println(Arrays.toString(example.elements.toArray()));
  }
}
