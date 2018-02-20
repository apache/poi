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

import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.math.BigDecimal;

/**
 * An XML Schema compatible subclass of {@link java.util.GregorianCalendar GregorianCalendar}.
 * XmlCalendar modifies several key details in the behavior of
 * GregorianCalendar to make it more useful when dealing with XML dates.
 * <p>
 * It is easy to convert between XmlCalendar and {@link GDate}, or to
 * parse or emit an XmlCalendar using a standard XML Schema
 * lexical representation.
 * <ol>
 * <li>
 * To match XML Schema dates, this XmlCalendar is a fully proleptic
 * Gregorian calendar by default, which means that Gregorian calendar
 * rules are applied backwards in time as if they had always been in
 * effect, actual historical circumstances concerning the observance
 * of the 1582 decree of Pope Gregory XIII notwithstanding.
 * </li>
 * <li>
 * In order to better support partially-specified dates for XML Schema,
 * this implementation provides a stable get(field) method
 * that does not modify the instance if you are acessing a field right
 * after it was explicitly set: a set followed by a get will always
 * return the same thing and will not fill in any other fields. However,
 * if you get a field that was not explicitly set, then all the fields
 * are still automatically filled and normalized for you, just like a
 * regular GregorianCalendar. If you wish to force the completion and
 * defaulting of all the fields (without hunting to get one that happens
 * to be unset), you can always do so by calling getTime().
 * </li>
 * <li>
 * When a year is unspecified and needs to be filled in automatically
 * (for example when using a .get or .getTime method as discussed above),
 * the year is defaulted to year 0 (also known as 1 BC).  This is different
 * from {@link GregorianCalendar}, which chooses 1970. The reason 0 is preferable
 * is that it is a leap year and so it permits the date --2-29 to be specified
 * stably. A different default year can be chosen via the static method
 * {@link #setDefaultYear(int) XmlCalendar.setDefaultYear()}, or by setting the 
 * system property "user.defaultyear". If you do change this value, you should 
 * pick another leap year such as 2000 and avoid non-leap years such as 1900.
 * </li>
 * <li>
 * When constructing an XmlCalendar from an XML Schema
 * formatted date or time string or GDate object, the timezone
 * for the calendar is taken from the string if it is present, or
 * taken to be {@link java.util.TimeZone#getDefault() TimeZone.getDefault()} if not.
 * <p>
 *     For example, the XML timezone "Z" is translated to "GMT";
 *     the XML timezone "+05:00" is translated to "GMT+05:00".
 * </p>
 * </li>
 * <li>
 * Finally, this implementation provides a String constructor and a
 * toString() method that comply with the XML Schema conventions
 * for formatting a date. If only a subset of fields have been
 * explicitly set, toString() produces a string with the proper subset
 * of information.
 * </li>
 * </ol>
 */ 
public class XmlCalendar extends GregorianCalendar
{
    /**
     * Constructs an XmlCalendar for a standard XML
     * schema formatted date string.
     * 
     * The parser accepts any of the following formats:
     * 
     * YYYY-MM-DDThh:mm:ss          - dateTime
     * YYYY-MM-DD                   - date
     *            hh:mm:ss          - time
     * YYYY                         - gYear
     *    --MM                      - gMonth
     *      ---DD                   - gDay
     * 
     * The parser actually accepts all 16 combinations of subsets of
     * fields (Y, M, D, T) using the same scheme, even for combinations
     * that are not defined as types in the schema spec, such as
     * year, day, and time:
     * 
     * YYYY--DDThh:mm:ss            - [everything but month specified]
     * 
     * In the string, each field must be padded to its full width, for
     * example, January must be --01, not just --1.
     * 
     * In particular, a year must be padded to at least four digits, so
     * "98" is not a valid year, although "1998" and "0098" are both valid
     * years, unambiguously 19 centuries separated from each other.  A year
     * may also be preceded by a minus symbol: -0001 is 1 BC and -0002 is
     * 2 BC.
     *
     * Finally a timezone is always allowed (yet optional) at the end.
     * Timezones must be either "Z" (UTC, which we translate to GMT),
     * or simple offsets from UTC in the range "-14:00" to "+14:00",
     * for example: "14:30:00-05:00" specifies 2:30 PM in the
     * afternoon at UTC-05:00, which is the same as EST.
     * 
     * If a timezone is not specified, the default TimeZone is used.
     */ 
    public XmlCalendar(String xmlSchemaDateString)
    {
        this(new GDate(xmlSchemaDateString)); // use GDate to parse
    }
    
    /**
     * Constructs an XmlCalendar from a GDate.
     * 
     * If the instance is not completed, you can round-trip to an
     * equivalent GDate by writing "new GDate(new XmlCalendar(gdate))".
     * However, if you access any of the unset fields of the calendar, all
     * the fields will be automatically filled in, so partial dates
     * without timezones or other fields will not round-trip after access.
     */ 
    public XmlCalendar(GDateSpecification date)
    {
        this(GDate.timeZoneForGDate(date), date);
    }
    
    private XmlCalendar(TimeZone tz, GDateSpecification date)
    {
        super(tz);
        setGregorianChange(_beginningOfTime); // proleptic
        clear();
        
        if (date.hasYear())
        {
            int y = date.getYear(); // is never 0
            if (y > 0)
            {
                set(Calendar.ERA, GregorianCalendar.AD);
            }
            else // y < 0
            {
                set(Calendar.ERA, GregorianCalendar.BC);
                //y = 1 - y;
                y = -y; // no need to add 1
            }
            set(Calendar.YEAR, y);
        }
        if (date.hasMonth())
            set(Calendar.MONTH, date.getMonth() - 1); // note!!
        if (date.hasDay())
            set(Calendar.DAY_OF_MONTH, date.getDay());
        if (date.hasTime())
        {
            set(Calendar.HOUR_OF_DAY, date.getHour());
            set(Calendar.MINUTE, date.getMinute());
            set(Calendar.SECOND, date.getSecond());
            if (date.getFraction().scale() > 0)
                set(Calendar.MILLISECOND, date.getMillisecond());
        }
        if (date.hasTimeZone())
        {
            set(Calendar.ZONE_OFFSET, date.getTimeZoneSign() * 1000 * 60 * (date.getTimeZoneHour() * 60 + date.getTimeZoneMinute()));
            set(Calendar.DST_OFFSET, 0); // note!!  if we don't do this, then GregorianCalendar will pick up DST from the time zone
        }
    }
    
    /**
     * Constructs an XmlCalendar from a Date.
     * 
     * The default TimeZone is used for computing the various fields.
     */
    public XmlCalendar(Date date)
    {
        this(TimeZone.getDefault(), new GDate(date));
        complete();
    }
    
    /**
     * Constructs an XmlCalendar with the specified year, month, day,
     * hours, minutes, seconds, and optional fractional seconds, in
     * the default timezone.
     */
    public XmlCalendar(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fraction)
    {
        this(TimeZone.getDefault(), new GDate(year, month, day, hour, minute, second, fraction));
    }
    
    /**
     * Constructs an XmlCalendar with the specified year, month, day,
     * hours, minutes, seconds, and optional fractional seconds, in
     * the specified timezone.
     */
    public XmlCalendar(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fraction,
            int tzSign,
            int tzHour,
            int tzMinute)
    {
        this(new GDate(year, month, day, hour, minute, second, fraction, tzSign, tzHour, tzMinute));
    }
    

    /**
     * Gets the value for a given time field.
     * 
     * Unlike the GregorianCalendar implementation, the get() does not
     * force a complete of all fields.  If you wish to force a completion
     * of all the fields, call getTime() first.
     */
    public int get(int field)
    {
        if (!isSet(field) || isTimeSet)
            return super.get(field); // forces a complete
        else
            return internalGet(field); // does not force a complete.
    }

    /**
     * Constructs an empty instance with no fields set.
     */ 
    public XmlCalendar()
    {
        setGregorianChange(_beginningOfTime); // proleptic
        clear();
    }
    
    private static int defaultYear = Integer.MIN_VALUE;
    private static final int DEFAULT_DEFAULT_YEAR = 0; 
    
    /**
     * Returns the default year that is used when no year is specified.
     */ 
    public static int getDefaultYear()
    {
        if (defaultYear == Integer.MIN_VALUE)
        {
            try
            {
                String yearstring = SystemProperties.getProperty("user.defaultyear");
                if (yearstring != null)
                    defaultYear = Integer.parseInt(yearstring);
                else
                    defaultYear = DEFAULT_DEFAULT_YEAR;
            }
            catch (Throwable t)
            {
                defaultYear = DEFAULT_DEFAULT_YEAR;
            }
        }
        return defaultYear;
    }
    
    /**
     * Sets the default year to be used when no year is specified.
     */ 
    public static void setDefaultYear(int year)
    {
        defaultYear = year;
    }
    
    /**
     * Overrides GregorianCalendar.computeTime to apply a different
     * default year.  (It must be a leap year.)
     */ 
    protected void computeTime()
    {
        boolean unsetYear = !isSet(YEAR);
        if (unsetYear)
            set(YEAR, getDefaultYear());
        try
        {
            super.computeTime();
        }
        finally
        {
            if (unsetYear)
                clear(YEAR);
        }
    }
        
    private static Date _beginningOfTime = new Date(Long.MIN_VALUE);
    
    /**
     * Prints the XmlCalendar using a standard XML Schema
     * format, as described in XmlCalendar(String s).
     */
    public String toString()
    {
        return (new GDate(this)).toString(); // use GDate to print
    }
}
