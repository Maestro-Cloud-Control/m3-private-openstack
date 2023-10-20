/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.util;

import io.maestro3.sdk.internal.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public final class M3DateUtils {


    public static final String DATE_FORMAT = "%s %s, %s";

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("y");
    //private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss");
    private static final String UTC = "UTC";

    private M3DateUtils() {
        throw new UnsupportedOperationException();
    }

    public static String getFormattedDay(Date date) {
        Assert.notNull(date, "date can`t be null");
        return DAY_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC)));
    }

    public static String getFormattedMonth(Date date) {
        Assert.notNull(date, "date can`t be null");
        return MONTH_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC)));
    }

    public static String getFormattedYear(Date date) {
        Assert.notNull(date, "date can`t be null");
        return YEAR_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC)));
    }
//
//    public static String getFormattedDate(Date date) {
//        Assert.notNull(date, "date can`t be null");
//        return DAY_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC))) + " " +
//                MONTH_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC))) + " " +
//                YEAR_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC)));
//
//    }
//
//    public static String getFormattedDateTime(Date date) {
//        return getFormattedDate(date)
//                + " " + TIME_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC)));
//    }
//    public static Date truncateToUtcMonth(Date date) {
//        return new DateTime(date, DateTimeZone.UTC).withDayOfMonth(1).withMillisOfDay(0).toDate();
//    }
//
//    public static Date truncateDateToMinutes(Date date) {
//        Calendar calendar = Calendar.getInstance(); // locale-specific
//        calendar.setTime(date);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return new Date(calendar.getTimeInMillis());
//    }
}
