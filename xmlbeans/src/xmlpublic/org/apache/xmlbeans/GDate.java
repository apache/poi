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
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Represents an XML Schema-compatible Gregorian date.
 * <p>
 * There are many date types in XML Schema, and this type
 * represents the natural union of all those types.  A GDate
 * can hold any subset of date fields (Year, Month, Day, Time,
 * Timezone, or some combination). Wherever the specification
 * provides guidance, the guidelines in the
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/">XML Schema 1.0 specification</a>
 * (plus <a target="_blank" href="http://www.w3.org/2001/05/xmlschema-errata">published errata</a>) are followed.
 * <p>
 * Instances may separately have values or no values for
 * the year, month, day-of-month, and time-of-day. Not all
 * operations are meaningful on all combinations.
 */
public final class GDate implements GDateSpecification, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    // XMLSchema spec requires support only for years 1 to 9999, but XMLBeans covers more up to the following limitations
    // to avoid losing precision when transforming to a java.util.Date
    static final int MAX_YEAR =  292277265;  // is Long.MAX_VALUE ms in years - 1 (for the 11month, 31days, 23h, 59m, 59sec case).
    static final int MIN_YEAR = -292275295; // is Long.MIN_VALUE ms in years + 1970 + 1

    // for fast equality comparison, hashing, and serialization
    private transient String _canonicalString;
    private transient String _string;

    private int _bits;
    private int _CY;
    private int _M;
    private int _D;
    private int _h;
    private int _m;
    private int _s;
    private BigDecimal _fs;
    private int _tzsign;
    private int _tzh;
    private int _tzm;
    
    
    /* package */ static final BigDecimal _zero = BigDecimal.valueOf(0);
    /* package */ static final BigDecimal _one = BigDecimal.valueOf(1);

    /**
     * Constructs a GDate based on a lexical representation.
     */
    public GDate(CharSequence string)
    {
        // first trim XML whitespace
        int len = string.length();
        int start = 0;
        while (len > 0 && isSpace(string.charAt(len - 1)))
            len -= 1;
        while (start < len && isSpace(string.charAt(start)))
            start += 1;

        // pick optional timezone off the end
        if (len - start >= 1 && string.charAt(len - 1) == 'Z')
        {
            _bits |= HAS_TIMEZONE;
            len -= 1;
        }
        else if (len - start >= 6)
        timezone: {
            int tzsign;
            int tzhour;
            int tzminute;

            if (string.charAt(len - 3) != ':')
                break timezone;

            switch (string.charAt(len - 6))
            {
                case '-':
                    tzsign = -1; break;
                case '+':
                    tzsign = 1; break;
                default:
                    break timezone;
            }

            tzhour = twoDigit(string, len - 5);
            tzminute = twoDigit(string, len - 2);
            if (tzhour > 14)
                throw new IllegalArgumentException("time zone hour must be two digits between -14 and +14");
            if (tzminute > 59)
                throw new IllegalArgumentException("time zone minute must be two digits between 00 and 59");
            _bits |= HAS_TIMEZONE;
            _tzsign = tzsign;
            _tzh = tzhour;
            _tzm = tzminute;
            len -= 6;
        }

        // pick date fields off the beginning if it doesn't look like a time
        if (start < len && (start + 2 >= len || string.charAt(start + 2) != ':'))
        scandate:
        {
            // parse year sign
            boolean negyear = false;
            if (start < len && string.charAt(start) == '-')
            {
                negyear = true;
                start += 1;
            }

            // scan year digits
            int value = 0;
            int digits = -start;
            char ch;
            boolean startsWithZero = start < len && digitVal(string.charAt(start))==0;

            for (;;)
            {
                ch = start < len ? string.charAt(start) : '\0';
                if (!isDigit(ch))
                    break;
                
                if ( startsWithZero && start+digits>=4 )
                    throw new IllegalArgumentException("year value starting with zero must be 4 or less digits: " + string);

                value = value * 10 + digitVal(ch);
                start += 1;
            }
            digits += start;
            if (digits > 9)
                throw new IllegalArgumentException("year too long (up to 9 digits)");
            else if (digits >= 4)
            {
                _bits |= HAS_YEAR;
                _CY =  negyear ? -value : value;
                if (_CY == 0) throw new IllegalArgumentException("year must not be zero");
            }
            else if (digits > 0)
                throw new IllegalArgumentException("year must be four digits (may pad with zeroes, e.g., 0560)");

            if ( _CY > MAX_YEAR )
                throw new IllegalArgumentException("year value not supported: too big, must be less than " + MAX_YEAR);

            if ( _CY < MIN_YEAR )
                throw new IllegalArgumentException("year values not supported: too small, must be bigger than " + MIN_YEAR);

            // hyphen introduces a month
            if (ch != '-')
            {
                if (negyear && !hasYear())
                    throw new IllegalArgumentException(); // a single minus
                else
                    break scandate;
            }
            start += 1;

            // two-digit month
            if (len - start >= 2)
            {
                value = twoDigit(string, start);
                if (value >= 1 && value <= 12)
                {
                    _bits |= HAS_MONTH;
                    _M = value;
                    start += 2;
                }
            }

            // hyphen introduces a day
            ch = start < len ? string.charAt(start) : '\0';
            if (ch != '-')
            {
                if (!hasMonth())
                    throw new IllegalArgumentException(); // minus after a year
                else
                    break scandate;
            }
            start += 1;

            // two-digit day
            if (len - start >= 2)
            {
                value = twoDigit(string, start);
                if (value >= 1 && value <= 31)
                {
                    _bits |= HAS_DAY;
                    _D = value;
                    start += 2;
                }
            }

            if (!hasDay())
            {
                // error in the original schema spec permits an extra '-' here
                if (hasMonth() && !hasYear())
                {
                    ch = start < len ? string.charAt(start) : '\0';
                    if (ch == '-')
                    {
                        start += 1;
                        break scandate;
                    }
                }
                throw new IllegalArgumentException(); // minus after a month
            }
        }

        // time
        if (start < len)
        {
            if (hasYear() || hasMonth() || hasDay())
            {
                if (string.charAt(start) != 'T')
                   throw new IllegalArgumentException("date and time must be separated by 'T'");
                start += 1;
            }

            if (len < start + 8 || string.charAt(start + 2) != ':' || string.charAt(start + 5) != ':')
                throw new IllegalArgumentException();

            int h = twoDigit(string, start);
            if (h > 24)
                throw new IllegalArgumentException("hour must be between 00 and 23");
            int m = twoDigit(string, start + 3);
            if (m >= 60)
                throw new IllegalArgumentException("minute must be between 00 and 59");
            int s = twoDigit(string, start + 6);
            if (s >= 60)
                throw new IllegalArgumentException("second must be between 00 and 59");

            start += 8;

            BigDecimal fs = _zero;
            if (start < len)
            {
                if (string.charAt(start) != '.')
                    throw new IllegalArgumentException();
                if (start + 1 < len)
                {
                    for (int i = start + 1; i < len; i++)
                    {
                        if (!isDigit(string.charAt(i)))
                            throw new IllegalArgumentException();
                    }
                    try
                    {
                        fs = new BigDecimal(string.subSequence(start, len).toString());
                    }
                    catch (Throwable e)
                    {
                        throw new IllegalArgumentException();
                    }
                }
            }

            _bits |= HAS_TIME;
            _h = h;
            _m = m;
            _s = s;
            _fs = fs;
        }

        if ( hasTime() && _h == 24 )
        {
            if ( _m != 0 || _s != 0 || _fs.compareTo(_zero) != 0 )
                throw new IllegalArgumentException("if hour is 24, minutes, seconds and fraction must be 0");
            else
            {   // normalize to next day if it has date or at least has day
                if ( hasDate() )
                {
                    GDateBuilder gdb = new GDateBuilder(_CY, _M, _D, _h, _m, _s, _fs, _tzsign, _tzh, _tzm);
                    gdb.normalize24h();                    

                    _D = gdb.getDay();
                    _M = gdb.getMonth();
                    _CY = gdb.getYear();
                    _h = 0;
                }
                else if ( hasDay() ) // if no date only days increment
                {
                    _D++;
                    _h = 0;
                }
            }
        }
        
        if (!isValid())
            throw new IllegalArgumentException("invalid date");
    }

    /**
     * Constructs a GDate with the specified year, month, day,
     * hours, minutes, seconds, and optional fractional seconds, in
     * an unspecified timezone.
     * <p>
     * Note that by not specifying the timezone the GDate
     * becomes partially unordered with respect to times that
     * do have a specified timezone.
     */
    public GDate(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fraction)
    {
        _bits = HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME;

        _CY = year;
        _M = month;
        _D = day;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? _zero : fraction;

        if (!isValid())
            throw new IllegalArgumentException();
    }

    /**
     * Constructs an absolute GDate with the specified year,
     * month, day, hours, minutes, seconds, and optional fractional
     * seconds, and in the timezone specified.
     * <p>
     * If you wish to have a time or date that isn't in a specified timezone,
     * then use the constructor that does not include the timezone arguments.
     */
    public GDate(
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
        _bits = HAS_TIMEZONE | HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME;

        _CY = year;
        _M = month;
        _D = day;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? _zero : fraction;
        _tzsign = tzSign;
        _tzh = tzHour;
        _tzm = tzMinute;

        if (!isValid())
            throw new IllegalArgumentException();
    }

    /**
     * Constructs a GDate based on a java.util.Date.
     * <p>
     * The current offset of the default timezone is used as the timezone.
     * <p>
     * For example, if eastern daylight time is in effect at the given
     * date, the timezone on the east coast of the united states
     * translates to GMT-05:00 (EST) + 1:00 (DT offset) == GMT-04:00.
     */
    public GDate(Date date)
    {
        // requires some date math, so ctor lives on GDateBuilder
        this(new GDateBuilder(date));
    }

    /**
     * Constructs a GDate based on a java.util.Calendar.
     * <p>
     * If the calendar does not have some fields set, the same absence
     * of information is reflected in the GDate.  Note that
     * java.util.GregorianCalendar fills in all fields as soon as any
     * are fetched, so constructing a GDate with the same calendar object
     * twice may result in a different GDate because of a changed calendar.
     * Note that org.apache.xmlbeans.XmlCalendar is stable if you re-get a set field,
     * so it does not have the same problem.
     */
    public GDate(Calendar calendar)
    {
        // we must scrape the "isSet" information out before accessing anything
        boolean isSetYear = calendar.isSet(Calendar.YEAR);
        boolean isSetEra = calendar.isSet(Calendar.ERA);
        boolean isSetMonth = calendar.isSet(Calendar.MONTH);
        boolean isSetDay = calendar.isSet(Calendar.DAY_OF_MONTH);
        boolean isSetHourOfDay = calendar.isSet(Calendar.HOUR_OF_DAY);
        boolean isSetHour = calendar.isSet(Calendar.HOUR);
        boolean isSetAmPm = calendar.isSet(Calendar.AM_PM);
        boolean isSetMinute = calendar.isSet(Calendar.MINUTE);
        boolean isSetSecond = calendar.isSet(Calendar.SECOND);
        boolean isSetMillis = calendar.isSet(Calendar.MILLISECOND);
        boolean isSetZone = calendar.isSet(Calendar.ZONE_OFFSET);
        boolean isSetDst = calendar.isSet(Calendar.DST_OFFSET);

        if (isSetYear)
        {
            int y = calendar.get(Calendar.YEAR);
            if (isSetEra && calendar instanceof GregorianCalendar)
                if (calendar.get(Calendar.ERA) == GregorianCalendar.BC)
                    y = -y; //1 - y;
            _bits |= HAS_YEAR;
            _CY = y;
        }
        if (isSetMonth)
        {
            _bits |= HAS_MONTH;
            _M = calendar.get(Calendar.MONTH) + 1; // !!note
        }
        if (isSetDay)
        {
            _bits |= HAS_DAY;
            _D = calendar.get(Calendar.DAY_OF_MONTH);
        }
        boolean gotTime = false;

        int h = 0;
        int m = 0;
        int s = 0;
        BigDecimal fs = _zero;

        if (isSetHourOfDay)
        {
            h = calendar.get(Calendar.HOUR_OF_DAY);
            gotTime = true;
        }
        else if (isSetHour && isSetAmPm)
        {
            h = calendar.get(Calendar.HOUR) + calendar.get(Calendar.AM_PM) * 12;
            gotTime = true;
        }

        if (isSetMinute)
        {
            m = calendar.get(Calendar.MINUTE);
            gotTime = true;
        }

        if (isSetSecond)
        {
            s = calendar.get(Calendar.SECOND);
            gotTime = true;
        }

        if (isSetMillis)
        {
            fs = BigDecimal.valueOf(calendar.get(Calendar.MILLISECOND), 3);
            gotTime = true;
        }

        if (gotTime)
        {
            _bits |= HAS_TIME;
            _h = h;
            _m = m;
            _s = s;
            _fs = fs;
        }

        if (isSetZone)
        {
            int zoneOffsetInMilliseconds = calendar.get(Calendar.ZONE_OFFSET);
            if (isSetDst)
                zoneOffsetInMilliseconds += calendar.get(Calendar.DST_OFFSET);

            _bits |= HAS_TIMEZONE;
            if (zoneOffsetInMilliseconds == 0)
            {
                _tzsign = 0;
                _tzh = 0;
                _tzm = 0;
                TimeZone zone = calendar.getTimeZone();
                String id = zone.getID();
                if (id != null && id.length() > 3) switch (id.charAt(3))
                {
                    case '+': _tzsign = 1; break;   // GMT+00:00
                    case '-': _tzsign = -1; break;  // GMT-00:00
                }
            }
            else
            {
                _tzsign = (zoneOffsetInMilliseconds < 0 ? -1 : +1);
                zoneOffsetInMilliseconds = zoneOffsetInMilliseconds * _tzsign;
                _tzh = zoneOffsetInMilliseconds / 3600000;
                _tzm = (zoneOffsetInMilliseconds - _tzh * 3600000) / 60000;
            }
        }
    }

    /**
     * Constructs a GDate based on another GDateSpecification.
     */
    public GDate(GDateSpecification gdate)
    {
        if (gdate.hasTimeZone())
        {
            _bits |= HAS_TIMEZONE;
            _tzsign = gdate.getTimeZoneSign();
            _tzh = gdate.getTimeZoneHour();
            _tzm = gdate.getTimeZoneMinute();
        }

        if (gdate.hasTime())
        {
            _bits |= HAS_TIME;
            _h = gdate.getHour();
            _m = gdate.getMinute();
            _s = gdate.getSecond();
            _fs = gdate.getFraction();
        }

        if (gdate.hasDay())
        {
            _bits |= HAS_DAY;
            _D = gdate.getDay();
        }

        if (gdate.hasMonth())
        {
            _bits |= HAS_MONTH;
            _M = gdate.getMonth();
        }

        if (gdate.hasYear())
        {
            _bits |= HAS_YEAR;
            _CY = gdate.getYear();
        }
    }

    /* package */ static final boolean isDigit(char ch)
    {
        return ((char)(ch - '0') <= '9' - '0'); // char is unsigned
    }

    /* package */ static final boolean isSpace(char ch)
    {
        switch (ch)
        {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }

    /* package */ static final int digitVal(char ch)
    {
        return (ch - '0');
    }

    private static final int twoDigit(CharSequence str, int index)
    {
        char ch1 = str.charAt(index);
        char ch2 = str.charAt(index + 1);
        if (!isDigit(ch1) || !isDigit(ch2))
            return 100; // not two digits
        return digitVal(ch1) * 10 + digitVal(ch2);
    }

    /**
     * Returns true: all GDate instances are immutable.
     */
    public final boolean isImmutable()
    {
        return true;
    }

    /**
     * Returns a combination of flags indicating the information
     * contained by this GDate.  The five flags are
     * HAS_TIMEZONE, HAS_YEAR, HAS_MONTH, HAS_DAY, and HAS_TIME.
     */
    public int getFlags()
    {
        return _bits;
    }

    /**
     * True if this date/time specification specifies a timezone.
     */
    public final boolean hasTimeZone()
        { return ((_bits & HAS_TIMEZONE) != 0); }

    /**
     * True if this date/time specification specifies a year.
     */
    public final boolean hasYear()
        { return ((_bits & HAS_YEAR) != 0); }

    /**
     * True if this date/time specification specifies a month-of-year.
     */
    public final boolean hasMonth()
        { return ((_bits & HAS_MONTH) != 0); }

    /**
     * True if this date/time specification specifies a day-of-month.
     */
    public final boolean hasDay()
        { return ((_bits & HAS_DAY) != 0); }

    /**
     * True if this date/time specification specifies a time-of-day.
     */
    public final boolean hasTime()
        { return ((_bits & HAS_TIME) != 0); }

    /**
     * True if this date/time specification specifies a full date (year, month, day)
     */
    public final boolean hasDate()
        { return ((_bits & (HAS_DAY | HAS_MONTH | HAS_YEAR)) == (HAS_DAY | HAS_MONTH | HAS_YEAR)); }

    /**
     * Gets the year. Should be a four-digit year specification.
     */
    public final int getYear()
        { return _CY;  }

    /**
     * Gets the month-of-year. January is 1.
     */
    public final int getMonth()
        { return _M;  }

    /**
     * Gets the day-of-month. The first day of each month is 1.
     */
    public final int getDay()
        { return _D; }

    /**
     * Gets the hour-of-day. Midnight is 0, and 11PM is 23.
     */
    public final int getHour()
        { return _h; }

    /**
     * Gets the minute-of-hour. Range from 0 to 59.
     */
    public final int getMinute()
        { return _m; }

    /**
     * Gets the second-of-minute. Range from 0 to 59.
     */
    public final int getSecond()
        { return _s; }

    /**
     * Gets the fraction-of-second. Range from 0 (inclusive) to 1 (exclusive).
     */
    public final BigDecimal getFraction()
        { return _fs; }

    /**
     * Gets the time zone sign. For time zones east of GMT,
     * this is positive; for time zones west, this is negative.
     */
    public final int getTimeZoneSign()
        { return _tzsign; }

    /**
     * Gets the time zone hour.
     *
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    public final int getTimeZoneHour()
        { return _tzh; }

    /**
     * Gets the time zone minutes.
     *
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    public final int getTimeZoneMinute()
        { return _tzm; }

    /**
     * Gets the rounded millisecond value. Range from 0 to 999
     */
    public int getMillisecond()
    {
        if (_fs == null)
            return 0;
        return _fs.setScale(3, BigDecimal.ROUND_DOWN).unscaledValue().intValue();
    }

    /**
     * The canonical string representation. Specific moments or
     * times-of-day in a specified timezone are normalized to
     * UTC time to produce a canonical string form for them.
     * Other recurring time specifications keep their timezone
     * information.
     */
    public String canonicalString()
    {
        ensureCanonicalString();
        return _canonicalString;
    }

    /**
     * True if this GDate corresponds to a valid gregorian date value
     * in XML schema.
     */
    public boolean isValid()
    {
        return GDateBuilder.isValidGDate(this);
    }

    /**
     * Returns the Julian date corresponding to this Gregorian date.
     * The Julian date (JD) is a continuous count of days from
     * 1 January 4713 BC.
     */
    public int getJulianDate()
    {
        return GDateBuilder.julianDateForGDate(this);
    }

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
    public XmlCalendar getCalendar()
    {
        return new XmlCalendar(this);
    }


    /**
     * Retrieves the value of the current time as a java.util.Date
     * instance.
     */
    public Date getDate()
    {
        return GDateBuilder.dateForGDate(this);
    }

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
     */
    public int compareToGDate(GDateSpecification datespec)
    {
        return GDateBuilder.compareGDate(this, datespec);
    }

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
    public int getBuiltinTypeCode()
    {
        return GDateBuilder.btcForFlags(_bits);
    }

    /**
     * Adds a duration to this GDate, and returns a new GDate.
     */
    public GDate add(GDurationSpecification duration)
    {
        GDateBuilder builder = new GDateBuilder(this);
        builder.addGDuration(duration);
        return builder.toGDate();
    }

    /**
     * Adds a duration to this GDate, and returns a new GDate.
     */
    public GDate subtract(GDurationSpecification duration)
    {
        GDateBuilder builder = new GDateBuilder(this);
        builder.subtractGDuration(duration);
        return builder.toGDate();
    }

    /**
     * GDate is an immutable class, and equality is computed based
     * on its canonical value.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof GDate))
            return false;

        ensureCanonicalString();
        return _canonicalString.equals(((GDate)obj).canonicalString());
    }

    /**
     * Returns a hash code for this GDate.
     */
    public int hashCode()
    {
        ensureCanonicalString();
        return _canonicalString.hashCode();
    }

    /**
     * The canonical string representation. Specific moments or
     * times-of-day in a specified timezone are normalized to
     * UTC time to produce a canonical string form for them.
     * Other recurring time specifications keep their timezone
     * information.
     */
    private void ensureCanonicalString()
    {
        if (_canonicalString != null)
            return;

        boolean needNormalize =
            (hasTimeZone() && getTimeZoneSign() != 0 && hasTime() &&
            ((hasDay() == hasMonth() && hasDay() == hasYear())));

        if (!needNormalize && getFraction() != null && getFraction().scale() > 0)
        {
            BigInteger bi = getFraction().unscaledValue();
            needNormalize = (bi.mod(GDateBuilder.TEN).signum() == 0);
        }

        if (!needNormalize)
            _canonicalString = toString();
        else
        {
            GDateBuilder gdb = new GDateBuilder(this);
            gdb.normalize();
            _canonicalString = gdb.toString();
        }
    }

    /**
     * The natural string representation. This represents the information
     * that is available, including timezone. For types that correspond
     * to defined schema types (schemaBuiltinTypeCode() > 0),
     * this provides the natural lexical representation.
     * <p>
     * When both time and timezone are specified, this string is not
     * the canonical representation unless the timezone is UTC (Z)
     * (since the same moment in time can be expressed in different
     * timezones). To get a canonical string, use the canonicalString()
     * method.
     */
    public String toString()
    {
        if (_string == null)
            _string = formatGDate(this);
        return _string;
    }

    private final static char[] _tensDigit =
    {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };
    private final static char[] _onesDigit =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    private static final int _padTwoAppend(char[] b, int i, int n)
    {
        assert(n >= 0 && n < 100);
        b[i] = _tensDigit[n];
        b[i + 1] = _onesDigit[n];
        return i + 2;
    }

    private static final int _padFourAppend(char[] b, int i, int n)
    {
        if (n < 0)
        {
            b[i++] = '-';
            n = -n;
        }
        if (n >= 10000)
        {
            String s = Integer.toString(n);
            s.getChars(0, s.length(), b, i);
            return i + s.length();
        }
        int q = n / 100;
        int r = n - q * 100;
        b[i] = _tensDigit[q];
        b[i + 1] = _onesDigit[q];
        b[i + 2] = _tensDigit[r];
        b[i + 3] = _onesDigit[r];
        return i + 4;
    }
    
    private static final TimeZone GMTZONE = TimeZone.getTimeZone("GMT");
    private static final TimeZone[] MINUSZONE =
            {
                TimeZone.getTimeZone("GMT-00:00"),
                TimeZone.getTimeZone("GMT-01:00"),
                TimeZone.getTimeZone("GMT-02:00"),
                TimeZone.getTimeZone("GMT-03:00"),
                TimeZone.getTimeZone("GMT-04:00"),
                TimeZone.getTimeZone("GMT-05:00"),
                TimeZone.getTimeZone("GMT-06:00"),
                TimeZone.getTimeZone("GMT-07:00"),
                TimeZone.getTimeZone("GMT-08:00"),
                TimeZone.getTimeZone("GMT-09:00"),
                TimeZone.getTimeZone("GMT-10:00"),
                TimeZone.getTimeZone("GMT-11:00"),
                TimeZone.getTimeZone("GMT-12:00"),
                TimeZone.getTimeZone("GMT-13:00"),
                TimeZone.getTimeZone("GMT-14:00"),
            };
    private static final TimeZone[] PLUSZONE =
            {
                TimeZone.getTimeZone("GMT+00:00"),
                TimeZone.getTimeZone("GMT+01:00"),
                TimeZone.getTimeZone("GMT+02:00"),
                TimeZone.getTimeZone("GMT+03:00"),
                TimeZone.getTimeZone("GMT+04:00"),
                TimeZone.getTimeZone("GMT+05:00"),
                TimeZone.getTimeZone("GMT+06:00"),
                TimeZone.getTimeZone("GMT+07:00"),
                TimeZone.getTimeZone("GMT+08:00"),
                TimeZone.getTimeZone("GMT+09:00"),
                TimeZone.getTimeZone("GMT+10:00"),
                TimeZone.getTimeZone("GMT+11:00"),
                TimeZone.getTimeZone("GMT+12:00"),
                TimeZone.getTimeZone("GMT+13:00"),
                TimeZone.getTimeZone("GMT+14:00"),
            };

    /* package */ static final TimeZone timeZoneForGDate(GDateSpecification date)
    {
        // use a cached timezone if integral; otherwise make a new one.
        if (!date.hasTimeZone())
            return TimeZone.getDefault();
        if (date.getTimeZoneSign() == 0)
            return GMTZONE;
        if (date.getTimeZoneMinute() == 0 && date.getTimeZoneHour() <= 14 && date.getTimeZoneHour() >= 0)
            return date.getTimeZoneSign() < 0 ? MINUSZONE[date.getTimeZoneHour()] : PLUSZONE[date.getTimeZoneHour()];
        
        char[] zb = new char[9];
        zb[0] = 'G';
        zb[1] = 'M';
        zb[2] = 'T';
        zb[3] = (date.getTimeZoneSign() < 0) ? '-' : '+';
        GDate._padTwoAppend(zb, 4, date.getTimeZoneHour());
        zb[6] = ':';
        GDate._padTwoAppend(zb, 7, date.getTimeZoneMinute());
        return TimeZone.getTimeZone(new String(zb));
    }
    
    /* package */ static String formatGDate(GDateSpecification spec)
    {
        // We've used a char[] rather than a StringBuffer for a 4x speedup
        // -YY(10)YY-MM-DDTHH:MM:SS.FFFFFF+ZH:ZM
        // 1 + 10   + 3+ 3+ 3+ 3+ 3+1 + s + 3+ 3 = 33 + s
        BigDecimal fs = spec.getFraction();
        char[] message = new char[33 + (fs == null ? 0 : fs.scale())];
        int i = 0;

        if (spec.hasYear() || spec.hasMonth() || spec.hasDay())
        {
            dmy: {
                if (spec.hasYear())
                    i = _padFourAppend(message, 0, spec.getYear());
                else
                    message[i++] = '-';

                if (!(spec.hasMonth() || spec.hasDay()))
                    break dmy;

                message[i++] = '-';
                if (spec.hasMonth())
                    i = _padTwoAppend(message, i, spec.getMonth());

                if (!spec.hasDay())
                    break dmy;

                message[i++] = '-';
                i = _padTwoAppend(message, i, spec.getDay());
                break dmy;
            }
            if (spec.hasTime())
                message[i++] = 'T';
        }

        if (spec.hasTime())
        {
            i = _padTwoAppend(message, i, spec.getHour());
            message[i++] = ':';
            i = _padTwoAppend(message, i, spec.getMinute());
            message[i++] = ':';
            i = _padTwoAppend(message, i, spec.getSecond());
            if (fs != _zero) // (optimization ~3%)
            {
                String frac = fs.toString();
                int point = frac.indexOf('.');
                if (point >= 0)
                {
                    frac.getChars(point, frac.length(), message, i);
                    i += frac.length() - point;
                }
            }
        }

        if (spec.hasTimeZone())
        {
            if (spec.getTimeZoneSign() == 0)
            {
                message[i++] = 'Z';
            }
            else
            {
                message[i++] = spec.getTimeZoneSign() > 0 ? '+' : '-';
                i = _padTwoAppend(message, i, spec.getTimeZoneHour());
                message[i++] = ':';
                i = _padTwoAppend(message, i, spec.getTimeZoneMinute());
            }
        }

        // it would be nice to use (0, i, message) ctor instead
        return new String(message, 0, i);
    }

}

