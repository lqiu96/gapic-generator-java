/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link com.google.cloud.Timestamp}. */
class TimestampTest {
  private static final String TEST_TIME_ISO = "2015-10-12T15:14:54Z";
  private static final long TEST_TIME_SECONDS = 1444662894L;
  private static final long TEST_TIME_MICROSECONDS = 10000100L;
  private static final long TEST_TIME_MILLISECONDS =
      TimeUnit.SECONDS.toMillis(1444662894L) + TimeUnit.MICROSECONDS.toMillis(1234);
  private static final long TEST_TIME_MILLISECONDS_NEGATIVE = -1000L;
  private static final Date TEST_DATE = new Date(TEST_TIME_MILLISECONDS);
  private static final Date TEST_DATE_PRE_EPOCH = new Date(TEST_TIME_MILLISECONDS_NEGATIVE);

  @Test
  void minValue() {
    // MIN_VALUE is before the start of the Gregorian calendar... use magic value.
    assertThat(Timestamp.MIN_VALUE.getSeconds()).isEqualTo(-62135596800L);
    assertThat(Timestamp.MIN_VALUE.getNanos()).isEqualTo(0);
  }

  @Test
  void maxValue() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    GregorianCalendar calendar = new GregorianCalendar(tz);
    calendar.set(9999, Calendar.DECEMBER, 31, 23, 59, 59);
    java.sql.Timestamp expectedMin = new java.sql.Timestamp(calendar.getTimeInMillis());
    expectedMin.setNanos(999999999);

    assertThat(Timestamp.MAX_VALUE.getSeconds()).isEqualTo(calendar.getTimeInMillis() / 1000L);
    assertThat(Timestamp.MAX_VALUE.getNanos()).isEqualTo(999999999);
  }

  @Test
  void ofMicroseconds() {
    Timestamp timestamp = Timestamp.ofTimeMicroseconds(TEST_TIME_MICROSECONDS);
    assertThat(timestamp.getSeconds()).isEqualTo(TEST_TIME_MICROSECONDS / 1000000L);
    assertThat(timestamp.getNanos()).isEqualTo(TEST_TIME_MICROSECONDS % 1000000L * 1000);
  }

  @Test
  void ofDate() {
    Timestamp timestamp = Timestamp.of(TEST_DATE);
    Long expectedSeconds = TimeUnit.MILLISECONDS.toSeconds(TEST_TIME_MILLISECONDS);
    Long expectedNanos =
        TimeUnit.MILLISECONDS.toNanos(TEST_TIME_MILLISECONDS)
            - TimeUnit.SECONDS.toNanos(expectedSeconds);
    assertThat(timestamp.getSeconds()).isEqualTo(expectedSeconds);
    assertThat(timestamp.getNanos()).isEqualTo(expectedNanos);
  }

  @Test
  void testOf() {
    String expectedTimestampString = "1970-01-01T00:00:12.345000000Z";
    java.sql.Timestamp input = new java.sql.Timestamp(12345);
    Timestamp timestamp = Timestamp.of(input);
    assertEquals(timestamp.toString(), expectedTimestampString);
  }

  @Test
  void testOf_exactSecond() {
    String expectedTimestampString = "1970-01-01T00:00:12Z";
    java.sql.Timestamp input = new java.sql.Timestamp(12000);
    Timestamp timestamp = Timestamp.of(input);
    assertEquals(timestamp.toString(), expectedTimestampString);
  }

  @Test
  void testOf_preEpoch() {
    String expectedTimestampString = "1969-12-31T23:59:47.655000000Z";
    java.sql.Timestamp input = new java.sql.Timestamp(-12345);
    Timestamp timestamp = Timestamp.of(input);
    assertEquals(timestamp.toString(), expectedTimestampString);
  }

  @Test
  void testOf_onEpoch() {
    String expectedTimestampString = "1970-01-01T00:00:00Z";
    java.sql.Timestamp input = new java.sql.Timestamp(0);
    Timestamp timestamp = Timestamp.of(input);
    assertEquals(timestamp.toString(), expectedTimestampString);
  }

  @Test
  void testOf_preEpochExactSecond() {
    String expectedTimestampString = "1969-12-31T23:59:59Z";
    java.sql.Timestamp input = new java.sql.Timestamp(-1000);
    Timestamp timestamp = Timestamp.of(input);
    assertEquals(timestamp.toString(), expectedTimestampString);
  }

  @Test
  void ofDatePreEpoch() {
    Timestamp timestamp = Timestamp.of(TEST_DATE_PRE_EPOCH);
    long expectedSeconds = TEST_TIME_MILLISECONDS_NEGATIVE / 1_000;
    int expectedNanos = (int) (TEST_TIME_MILLISECONDS_NEGATIVE % 1_000 * 1000_000);
    if (expectedNanos < 0) {
      expectedSeconds--;
      expectedNanos += 1_000_000_000;
    }
    assertThat(timestamp.getSeconds()).isEqualTo(expectedSeconds);
    assertThat(timestamp.getNanos()).isEqualTo(expectedNanos);
  }

  @Test
  void toDate() {
    Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 1234 * 1000);
    Date date = timestamp.toDate();
    assertThat(TEST_TIME_MILLISECONDS).isEqualTo(date.getTime());
  }

  @Test
  void toFromSqlTimestamp() {
    long seconds = TEST_TIME_SECONDS;
    int nanos = 500000000;

    java.sql.Timestamp sqlTs = new java.sql.Timestamp(seconds * 1000);
    sqlTs.setNanos(nanos);

    Timestamp ts = Timestamp.of(sqlTs);
    assertThat(ts.getSeconds()).isEqualTo(seconds);
    assertThat(ts.getNanos()).isEqualTo(nanos);

    assertThat(ts.toSqlTimestamp()).isEqualTo(sqlTs);
  }

  @Test
  void boundsSecondsMin() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.ofTimeSecondsAndNanos(Timestamp.MIN_VALUE.getSeconds() - 1, 999999999));
  }

  @Test
  void boundsSecondsMax() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.ofTimeSecondsAndNanos(Timestamp.MAX_VALUE.getSeconds() + 1, 0));
  }

  @Test
  void boundsNanosMin() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, -1));
  }

  @Test
  void boundsNanosMax() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 1000000000));
  }

  @Test
  void boundsSqlTimestampMin() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.of(new java.sql.Timestamp((Timestamp.MIN_VALUE.getSeconds() - 1) * 1000)));
  }

  @Test
  void boundsSqlTimestampMax() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Timestamp.of(new java.sql.Timestamp((Timestamp.MAX_VALUE.getSeconds() + 1) * 1000)));
  }

  @Test
  void equalsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 0),
        Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 0),
        Timestamp.of(new java.sql.Timestamp(TEST_TIME_SECONDS * 1000)));
    tester.addEqualityGroup(Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS + 1, 0));
    tester.addEqualityGroup(Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 1));
    tester.testEquals();
  }

  @Test
  void testToString() {
    assertThat(Timestamp.MIN_VALUE.toString()).isEqualTo("0001-01-01T00:00:00Z");
    assertThat(Timestamp.MAX_VALUE.toString()).isEqualTo("9999-12-31T23:59:59.999999999Z");
    assertThat(Timestamp.ofTimeSecondsAndNanos(0, 0).toString()).isEqualTo("1970-01-01T00:00:00Z");
    assertThat(Timestamp.ofTimeSecondsAndNanos(0, 100).toString())
        .isEqualTo("1970-01-01T00:00:00.000000100Z");
    assertThat(Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 0).toString())
        .isEqualTo(TEST_TIME_ISO);
  }

  @Test
  void parseTimestamp() {
    assertThat(Timestamp.parseTimestamp("0001-01-01T00:00:00Z")).isEqualTo(Timestamp.MIN_VALUE);
    assertThat(Timestamp.parseTimestamp("9999-12-31T23:59:59.999999999Z"))
        .isEqualTo(Timestamp.MAX_VALUE);
    assertThat(Timestamp.parseTimestamp(TEST_TIME_ISO))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 0));
  }

  @Test
  void parseTimestampWithoutTimeZoneOffset() {
    assertThat(Timestamp.parseTimestamp("0001-01-01T00:00:00")).isEqualTo(Timestamp.MIN_VALUE);
    assertThat(Timestamp.parseTimestamp("9999-12-31T23:59:59.999999999"))
        .isEqualTo(Timestamp.MAX_VALUE);
    assertThat(Timestamp.parseTimestamp("2015-10-12T15:14:54"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(TEST_TIME_SECONDS, 0));
  }

  @Test
  void parseTimestampWithTimeZoneOffset() {
    // Max values
    assertThat(Timestamp.parseTimestampDuration("0001-01-01T00:00:00-00:00"))
        .isEqualTo(Timestamp.MIN_VALUE);
    assertThat(Timestamp.parseTimestampDuration("9999-12-31T23:59:59.999999999-00:00"))
        .isEqualTo(Timestamp.MAX_VALUE);
    // Extreme values (close to min/max)
    assertThat(Timestamp.parseTimestampDuration("0001-01-01T00:00:00.000000001Z"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(Timestamp.MIN_VALUE.getSeconds(), 1));
    assertThat(Timestamp.parseTimestampDuration("9999-12-31T23:59:59.999999998Z"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(Timestamp.MAX_VALUE.getSeconds(), 999999998));
    // Common use cases
    assertThat(Timestamp.parseTimestampDuration("2020-07-10T14:03:00.123-07:00"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1594414980, 123000000));
    assertThat(Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123+05:30"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1607262672, 123000000));
    // We also confirm that parsing a timestamp with nano precision will behave the same as the
    // threeten counterpart
    assertThat(Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123+05:30"))
        .isEqualTo(Timestamp.parseTimestamp("2020-12-06T19:21:12.123+05:30"));
    // Timestamps with fractional seconds at nanosecond level
    assertThat(Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123456789+05:30"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1607262672, 123456789));
    // Fractional seconds beyond nanos should throw an exception
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123456789321+05:30"));
    // Missing components (should throw exceptions)
    assertThrows(
        DateTimeParseException.class, () -> Timestamp.parseTimestampDuration("2020-12-06"));
    // Whitespace should not be supported
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("  2020-12-06T19:21:12.123+05:30  "));
    // It should be case-insensitive
    assertThat(Timestamp.parseTimestampDuration("2020-07-10t14:03:00-07:00"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1594414980, 0));
    // Invalid time zone offsets
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123+25:00"));
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123-25:00"));
    // Int values for SecondOfMinute should be between 0 and 59
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("2016-12-31T23:59:60Z"));
  }

  @Test
  void parseTimestampWithZoneString() {
    // Valid RFC 3339 timestamps with time zone names
    assertThat(Timestamp.parseTimestampDuration("2020-12-06T08:51:12.123America/Toronto"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1607262672, 123000000));
    assertThat(Timestamp.parseTimestampDuration("2023-04-10T22:42:10.123456789Europe/London"))
        .isEqualTo(Timestamp.ofTimeSecondsAndNanos(1681162930, 123456789));

    // Invalid time zone names
    assertThrows(
        DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("2020-12-06T19:21:12.123Invalid/TimeZone"));
  }

  @Test
  void fromProto() {
    com.google.protobuf.Timestamp proto =
        com.google.protobuf.Timestamp.newBuilder().setSeconds(1234).setNanos(567).build();
    Timestamp timestamp = Timestamp.fromProto(proto);
    assertThat(timestamp.getSeconds()).isEqualTo(1234);
    assertThat(timestamp.getNanos()).isEqualTo(567);
  }

  @Test
  void comparable() {
    assertThat(Timestamp.MIN_VALUE).isLessThan(Timestamp.MAX_VALUE);
    assertThat(Timestamp.MAX_VALUE).isGreaterThan(Timestamp.MIN_VALUE);

    assertThat(Timestamp.ofTimeSecondsAndNanos(100, 0))
        .isAtLeast(Timestamp.ofTimeSecondsAndNanos(100, 0));
    assertThat(Timestamp.ofTimeSecondsAndNanos(100, 0))
        .isAtMost(Timestamp.ofTimeSecondsAndNanos(100, 0));

    assertThat(Timestamp.ofTimeSecondsAndNanos(100, 1000))
        .isLessThan(Timestamp.ofTimeSecondsAndNanos(101, 0));
    assertThat(Timestamp.ofTimeSecondsAndNanos(100, 1000))
        .isAtMost(Timestamp.ofTimeSecondsAndNanos(101, 0));

    assertThat(Timestamp.ofTimeSecondsAndNanos(101, 0))
        .isGreaterThan(Timestamp.ofTimeSecondsAndNanos(100, 1000));
    assertThat(Timestamp.ofTimeSecondsAndNanos(101, 0))
        .isAtLeast(Timestamp.ofTimeSecondsAndNanos(100, 1000));
  }

  @Test
  void serialization() {
    reserializeAndAssert(Timestamp.parseTimestamp("9999-12-31T23:59:59.999999999Z"));
  }

  @Test
  void parseInvalidTimestampThreetenThrowsThreetenException() {
    assertThrows(
        org.threeten.bp.format.DateTimeParseException.class,
        () -> Timestamp.parseTimestamp("00x1-01-01T00:00:00"));
    assertThrows(
        java.time.format.DateTimeParseException.class,
        () -> Timestamp.parseTimestampDuration("00x1-01-01T00:00:00"));
  }
}
