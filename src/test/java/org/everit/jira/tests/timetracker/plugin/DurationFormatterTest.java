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
package org.everit.jira.tests.timetracker.plugin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper.BeanFactory;

@RunWith(Parameterized.class)
public class DurationFormatterTest {

  private static DurationBuilder duration(final double hoursPerDayParam,
      final double daysPerWeekParam) {
    return new DurationBuilder(hoursPerDayParam, daysPerWeekParam);
  }

  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(
        new Object[] { "0m", DurationFormatterTest.duration(8, 5), 8, 5 },
        new Object[] { "3m", DurationFormatterTest.duration(8, 5).min(3), 8, 5 },
        new Object[] { "2h 3m", DurationFormatterTest.duration(8, 5).hour(2).min(3), 8, 5 },
        new Object[] { "2h", DurationFormatterTest.duration(8, 5).hour(2), 8, 5 },
        new Object[] { "2d 2h", DurationFormatterTest.duration(8, 5).day(2).hour(2), 8, 5 },
        new Object[] { "3w 2d", DurationFormatterTest.duration(8, 5).week(3).day(2), 8, 5 },
        new Object[] { "1d", DurationFormatterTest.duration(7, 5).hour(7), 7, 5 },
        new Object[] { "7h", DurationFormatterTest.duration(7.5, 5).hour(7), 7.5, 5 },
        new Object[] { "1d", DurationFormatterTest.duration(7.5, 5).hour(7).min(30), 7.5, 5 },
        new Object[] { "1d 30m", DurationFormatterTest.duration(7.5, 5).hour(8), 7.5, 5 },
        new Object[] { "1w 4h", DurationFormatterTest.duration(8, 4.5).hour(40), 8, 4.5 },
        new Object[] { "1w 6h 15m", DurationFormatterTest.duration(7.5, 4.5).hour(40), 7.5,
            4.5 });
  }

  private final double dayPerWeek;

  private final String expectedString;

  private final double hoursPerDay;

  private final long inputSeconds;

  public DurationFormatterTest(final String expectedString,
      final DurationBuilder inputSeconds, final double hoursPerDay, final double dayPerWeek) {
    super();
    this.expectedString = expectedString;
    this.inputSeconds = inputSeconds.toSeconds();
    this.hoursPerDay = hoursPerDay;
    this.dayPerWeek = dayPerWeek;
  }

  /**
   * Mocks the {@code ComponentAccessor.getComponent(TimeTrackingConfiguration.class);} call in the
   * {@link DateTimeConverterUtil.secondConvertToString} constructor.
   */
  public void setupMockTimeTrackerConfig(final double hoursPerDayParam,
      final double daysPerWeekParam) {
    BigDecimal daysPerWeek = new BigDecimal(daysPerWeekParam);
    BigDecimal hoursPerDay = new BigDecimal(hoursPerDayParam);
    TimeTrackingConfiguration ttConfig =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(ttConfig.getDaysPerWeek()).thenReturn(daysPerWeek);
    Mockito.when(ttConfig.getHoursPerDay()).thenReturn(hoursPerDay);
    // EasyMock.replay(ttConfig);

    ApplicationProperties mockApplicationProperties =
        Mockito.mock(ApplicationProperties.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockApplicationProperties
        .getDefaultBackedString(Matchers.matches("jira.timetracking.format")))
        .thenReturn("pretty");

    BeanFactory mockBeanFactory = Mockito.mock(BeanFactory.class, Mockito.RETURNS_DEEP_STUBS);

    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);

    new MockComponentWorker()
        .addMock(TimeTrackingConfiguration.class, ttConfig)
        .addMock(ApplicationProperties.class, mockApplicationProperties)
        .addMock(BeanFactory.class, mockBeanFactory)
        .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .init();
  }

  @Test
  public void testExactDuration() {
    setupMockTimeTrackerConfig(hoursPerDay, dayPerWeek);
    Assert.assertEquals(expectedString, new DurationFormatter().exactDuration(inputSeconds));
  }
}
