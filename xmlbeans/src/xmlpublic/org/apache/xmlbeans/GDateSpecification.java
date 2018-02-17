/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents an XML Schema-compatible Gregorian date.
 * <p>
 * Both the immutable GDate and the mutable GDateBuilder are
 * GDateSpecifications.  Use this interface where you want to
 * allow callers to pass any implementation of a GDate.
 * 
 * @see GDate
 * @see XmlCalendar
 */
public interface GDateSpecification
{
    /**
     * Returns a combination of flags indicating the information
     * contained by this GDate.  The five flags are
     * {@link #HAS_TIMEZONE}, {@link #HAS_YEAR}, {@link #HAS_MONTH},
     * {@link #HAS_DAY}, and {@link #HAS_TIME}.
     */
    int getFlags();

    /** Timezone is specified. See {@link #getFlags}. */
    public final int HAS_TIMEZONE = 1;
    /** Year is specified. See {@link #getFlags}. */
    public final int HAS_YEAR = 2;
    /** Month of year is specified. See {@link #getFlags}. */
    public final int HAS_MONTH = 4;
    /** Day of month is specified. See {@link #getFlags}. */
    public final int HAS_DAY = 8;
    /** Time of day is specified. See {@link #getFlags}. */
    public final int HAS_TIME = 16;

    /**
     * True if this GDate specification is immutable. GDate returns true,
     * and GDateBuilder returns false.
     */
    boolean isImmutable();

    /**
     * True if this GDate corresponds to a valid gregorian date value
     * in XML schema.
     */
    boolean isValid();

    /**
     * True if this date/time specification specifies a timezone.
     */
    boolean hasTimeZone();

    /**
     * True if this date/time specification specifies a year.
     */
    boolean hasYear();

    /**
     * True if this date/time specification specifies a month-of-year.
     */
    boolean hasMonth();

    /**
     * True if this date/time specification specifies a day-of-month.
     */
    boolean hasDay();

    /**
     * True if this date/time specification specifies a time-of-day.
     */
    boolean hasTime();

    /**
     * True if this date/time specification specifies a full date (year, month, day)
     */
    boolean hasDate();

    /**
     * Gets the year. Should be a four-digit year specification.
     */
    int getYear();

    /**
     * Gets the month-of-year. January is 1.
     */
    int getMonth();

    /**
     * Gets the day-of-month. The first day of each month is 1.
     */
    int getDay();

    /**
     * Gets the hour-of-day. Midnight is 0, and 11PM is 23.
     */
    int getHour();

    /**
     * Gets the minute-of-hour. Range from 0 to 59.
     */
    int getMinute();

    /**
     * Gets the second-of-minute. Range from 0 to 59.
     */
    int getSecond();

    /**
     * Gets the time zone sign. For time zones east of GMT,
     * this is positive; for time zones west, this is negative.
     */
    int getTimeZoneSign();

    /**
     * Gets the time zone hour.
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    int getTimeZoneHour();

    /**
     * Gets the time zone minutes.
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    int getTimeZoneMinute();

    /**
     * Gets the fraction-of-second. Range from 0 (inclusive) to 1 (exclusive).
     */
    BigDecimal getFraction();

    /**
     * Gets the rounded millisecond value. Range from 0 to 999
     */
    int getMillisecond();

    /**
     * Returns the Julian date corresponding to this Gregorian date.
     * The Julian date (JD) is a continuous count of days from
     * 1 January 4713 BC.
     */
    int getJulianDate();

    /**
     * Retrieves the value of the current time as an {@link XmlCalendar}.
     * <p>
     * {@link XmlCalendar} is a subclass of {@link java.util.GregorianCalendar}
     * which is slightly customized to match XML schema date rules.
     * <p>
     * The returned {@link XmlCalendar} has only those time and date fields
     * set that are reflected in the GDate object.  Because of the way the
     * {@link java.util.Calendar} contract works, any information in the isSet() vanishes
     * as soon as you view any unset field using get() methods.
     * This means that if it is important to understand which date fields
     * are set, you must call isSet() first before get().
     */
    XmlCalendar getCalendar();

    /**
     * Retrieves the value of the current time as a java.util.Date
     * instance.
     */
    Date getDate();

    /**
     * Comparison to another GDate.
     * <ul>
     * <li>Returns -1 if this < date. (less-than)
     * <li>Returns 0 if this == date. (equal)
     * <li>Returns 1 if this > date. (greater-than)
     * <li>Returns 2 if this <> date. (incomparable)
     * </ul>
     * Two instances are incomparable if they have different amounts
     * of information.
     * 
     * @param gdatespec the date to compare against.
     */
    int compareToGDate(GDateSpecification gdatespec);

    /**
     * Returns the builtin type code for the shape of the information
     * contained in this instance, or 0 if the
     * instance doesn't contain information corresponding to a
     * Schema type.
     * <p> 
     * Value will be equal to
     * {@link SchemaType#BTC_NOT_BUILTIN},
     * {@link SchemaType#BTC_G_YEAR},
     * {@link SchemaType#BTC_G_YEAR_MONTH},
     * {@link SchemaType#BTC_G_MONTH},
     * {@link SchemaType#BTC_G_MONTH_DAY},
     * {@link SchemaType#BTC_G_DAY},
     * {@link SchemaType#BTC_DATE},
     * {@link SchemaType#BTC_DATE_TIME}, or
     * {@link SchemaType#BTC_TIME}.
     */
    int getBuiltinTypeCode();

    /**
     * The canonical string representation. Specific moments or
     * times-of-day in a specified timezone are normalized to
     * UTC time to produce a canonical string form for them.
     * Other recurring time specifications keep their timezone
     * information.
     */
    String canonicalString();

    /**
     * The natural string representation. This represents the information
     * that is available, including timezone. For types that correspond
     * to defined schema types (schemaBuiltinTypeCode() > 0),
     * this provides the natural lexical representation.
     *
     * When both time and timezone are specified, this string is not
     * the canonical representation unless the timezone is UTC (Z)
     * (since the same moment in time can be expressed in different
     * timezones). To get a canonical string, use the canonicalString()
     * method.
     */
    String toString();

}
