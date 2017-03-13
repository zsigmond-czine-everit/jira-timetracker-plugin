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
package org.everit.jira.tests.core.util;

import java.util.TimeZone;

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class DateTImeConverterUtilTest {

  @Test
  public void setDateTimeToWeekStartSundayTest() {
    DateTime setDateToWeekStart =
        DateTimeConverterUtil
            .setDateToWeekStart(new DateTime(1488896070000L, DateTimeZone.forID("UTC")), false);
    Assert.assertEquals(1488672000000L, setDateToWeekStart.getMillis());
  }

  @Test
  public void setDateTimeToWeekStartTest() {
    DateTime setDateToWeekStart =
        DateTimeConverterUtil
            .setDateToWeekStart(new DateTime(1488896070000L, DateTimeZone.forID("UTC")), true);
    Assert.assertEquals(1488758400000L, setDateToWeekStart.getMillis());
  }

  @Test
  public void setDateToMonthEndTes() {
    DateTime setDateToWeekEnd =
        DateTimeConverterUtil
            .setDateToMonthEnd(new DateTime(1488896070000L, DateTimeZone.forID("UTC")));
    Assert.assertEquals(1491004799000L, setDateToWeekEnd.getMillis());
  }

  @Test
  public void setDateToMonthStartTest() {
    DateTime setDateToWeekStart =
        DateTimeConverterUtil
            .setDateToMonthStart(new DateTime(1488896070000L, DateTimeZone.forID("UTC")));
    Assert.assertEquals(1488326400000L, setDateToWeekStart.getMillis());
  }

  @Test
  public void setDateToWeekEndTest() {
    DateTime setDateToWeekEnd =
        DateTimeConverterUtil
            .setDateToWeekEnd(new DateTime(1488896070000L, DateTimeZone.forID("UTC")), true);
    Assert.assertEquals(1489363199000L, setDateToWeekEnd.getMillis());
  }

  @Test
  public void setDateToWeekEndTestSunday() {
    DateTime setDateToWeekEnd =
        DateTimeConverterUtil
            .setDateToWeekEnd(new DateTime(1488896070000L, DateTimeZone.forID("UTC")), false);
    Assert.assertEquals(1489276799000L, setDateToWeekEnd.getMillis());
  }

  @Test
  public void setDayBeginingTest() {
    DateTime dayStart =
        DateTimeConverterUtil.setDateToDayStart(new DateTime(1483236000000L, DateTimeZone.UTC));
    Assert.assertEquals(1483228800000L, dayStart.getMillis());

    dayStart =
        DateTimeConverterUtil
            .setDateToDayStart(new DateTime(1483236000000L, DateTimeZone.forID("Etc/GMT+3")));
    Assert.assertEquals(1483153200000L, dayStart.getMillis());
  }

  @Test
  public void testTimeToTimeInDayAfter() {
    long calculatedTime =
        DateTimeConverterUtil.calculateTimeBetweenTimeAndDayTime(1488463200000L, 900,
            TimeZone.getTimeZone("UTC"));
    Assert.assertEquals(3600000, calculatedTime);
  }

  @Test
  public void testTimeToTimeInDayBefore() {
    long calculatedTime =
        DateTimeConverterUtil.calculateTimeBetweenTimeAndDayTime(1488463200000L, 780,
            TimeZone.getTimeZone("UTC"));
    Assert.assertEquals(82800000, calculatedTime);
  }
}
