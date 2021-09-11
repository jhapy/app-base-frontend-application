/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
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

package org.jhapy.frontend.utils.i18n;

import java.time.*;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-22
 */
public class DateTimeFormatter {

  public static String formatNoYear(LocalDate localDate, Locale currentLocal) {
    if (localDate == null) {
      return "";
    } else {
      return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));
    }
  }

  public static String format(LocalDate localDate, Locale currentLocal) {
    if (localDate == null) {
      return "";
    } else {
      return java.time.format.DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
          .withLocale(currentLocal)
          .format(localDate);
    }
  }

  public static String format(LocalDateTime localDateTime, Locale currentLocal) {
    if (localDateTime == null) {
      return "";
    } else {
      return java.time.format.DateTimeFormatter.ofLocalizedDateTime(
              FormatStyle.SHORT, FormatStyle.MEDIUM)
          .withLocale(currentLocal)
          .format(localDateTime);
    }
  }

  public static String format(Instant instant, Locale currentLocal) {
    if (instant == null) {
      return "";
    } else {
      return java.time.format.DateTimeFormatter.ofLocalizedDateTime(
              FormatStyle.SHORT, FormatStyle.MEDIUM)
          .withLocale(currentLocal)
          .format(LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault()));
    }
  }

  public static Instant parse(String instant, Locale currentLocal) {
    if (instant == null) {
      return null;
    } else {
      return LocalDateTime.parse(
              instant,
              java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                      FormatStyle.SHORT, FormatStyle.MEDIUM)
                  .withLocale(currentLocal))
          .atZone(ZoneOffset.systemDefault())
          .toInstant();
    }
  }

  public static String format(Date date, Locale currentLocal) {
    if (date == null) {
      return "";
    } else {
      return java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withLocale(currentLocal)
          .withZone(ZoneId.systemDefault())
          .format(date.toInstant());
    }
  }
}