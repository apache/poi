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
import java.util.TimeZone;

/**
 * Used to build {@link GDate GDates}.
 * <p>
 * Like GDate, a GDateBuilder represents an Gregorian Date, Time,
 * and Timezone, or subset of information (Year, Month, Day,
 * Time, Timezone, or some combination). Wherever it provides
 * guidance, the XML Schema 1.0 specification (plus published
 * errata) is followed.
 * <p>
 * Instances may separately set or clear the year, month,
 * day-of-month, and time-of-day. Not all operations are
 * meaningful on all combinations. In particular, timezone
 * normalization is only possible if there is a time, or
 * a time together with a full date.
 */
public final class GDateBuilder implements GDateSpecification, java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
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

    /**
     * Constructs a GDateBuilder specifying no date or time
     */
    public GDateBuilder()
    {
    }

    /**
     * Builds another GDateBuilder with the same value
     * as this one.
     */
    public Object clone()
    {
        return new GDateBuilder(this);
    }

    /**
     * Builds a GDate from this GDateBuilder.
     */
    public GDate toGDate()
    {
        return new GDate(this);
    }

    /**
     * Construts a GDateBuilder by copying another GDateSpecificaiton.
     */
    public GDateBuilder(GDateSpecification gdate)
    {
        if (gdate.hasTimeZone())
            setTimeZone(gdate.getTimeZoneSign(), gdate.getTimeZoneHour(), gdate.getTimeZoneMinute());

        if (gdate.hasTime())
            setTime(gdate.getHour(), gdate.getMinute(), gdate.getSecond(), gdate.getFraction());

        if (gdate.hasDay())
            setDay(gdate.getDay());

        if (gdate.hasMonth())
            setMonth(gdate.getMonth());

        if (gdate.hasYear())
            setYear(gdate.getYear());
    }

    // Forms:

    // Date part:
    // Year:       (-?\d{4,})
    // YearMonth:  (-?\d{4,})-(\d{2})
    // Date:       (-?\d{4,})-(\d{2})-(\d{2})
    // Month:      --(\d{2})(--)?              //errata R-48
    // MonthDay:   --(\d{2})-(\d{2})
    // Day:        ---(\d{2})

    // Time part:
    // Time:       (\d{2}):(\d{2}):(\d{2})(.\d*)?

    // Timezone part:
    // TZ:         (Z)|([+-]\d{2}):(\d{2})

    /**
     * Constructs a GDateBuilder from a lexical
     * representation. The lexical space contains the
     * union of the lexical spaces of all the schema
     * date/time types (except for duration).
     */
    public GDateBuilder(CharSequence string)
    {
        this(new GDate(string));
    }


    public GDateBuilder(Calendar calendar)
    {
        this(new GDate(calendar));
    }

    /**
     * Constructs a GDateBuilder with the specified year, month, day,
     * hours, minutes, seconds, and optional fractional seconds, in
     * an unspecified timezone.
     * <p>
     * Note that by not specifying the timezone the GDateBuilder
     * becomes partially unordered with respect to timesthat do have a
     * specified timezone.
     * 
     * @param year The year
     * @param month The month, from 1-12
     * @param day The day of month, from 1-31
     * @param hour The hour of day, from 0-23
     * @param minute The minute of hour, from 0-59
     * @param second The second of minute, from 0-59
     * @param fraction The fraction of second, 0.0 to 0.999... (may be null) 
     */
    public GDateBuilder(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fraction)
    {
        _bits = HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME;

        if (year == 0)
            throw new IllegalArgumentException();

        _CY = (year > 0 ? year : year + 1);
        _M = month;
        _D = day;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? GDate._zero : fraction;

        if (!isValid())
            throw new IllegalArgumentException();
    }

    /**
     * Constructs an absolute GDateBuilder with the specified year,
     * month, day, hours, minutes, seconds, and optional fractional
     * seconds, and in the timezone specified.
     * <p>
     * Note that you can reexpress the GDateBuilder in any timezone using
     * normalizeToTimeZone(). The normalize() method normalizes to UTC.
     * <p>
     * If you wish to have a time or date that isn't in a specified timezone,
     * then use the constructor that does not include the timezone arguments.
     *
     * @param year the year
     * @param month the month, from 1-12
     * @param day the day of month, from 1-31
     * @param hour the hour of day, from 0-23
     * @param minute the minute of hour, from 0-59
     * @param second the second of minute, from 0-59
     * @param fraction the fraction of second, 0.0 to 0.999... (may be null)
     * @param tzSign the timezone offset sign, either +1, 0, or -1
     * @param tzHour the timezone offset hour
     * @param tzMinute the timezone offset minute 
     */
    public GDateBuilder(
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

        if (year == 0)
            throw new IllegalArgumentException();

        _CY = (year > 0 ? year : year + 1);
        _M = month;
        _D = day;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? GDate._zero : fraction;
        _tzsign = tzSign;
        _tzh = tzHour;
        _tzm = tzMinute;

        if (!isValid())
            throw new IllegalArgumentException();
    }

    /**
     * Constructs a GDateBuilder based on a java.util.Date.
     * <p>
     * The current offset of the default timezone is used as the timezone.
     * <p>
     * For example, if eastern daylight time is in effect at the given
     * date, the timezone on the east coast of the united states
     * translates to GMT-05:00 (EST) + 1:00 (DT offset) == GMT-04:00.
     * 
     * @param date the date object to copy
     */
    public GDateBuilder(Date date)
    {
        setDate(date);
    }

    /**
     * True if the instance is immutable.
     */
    public boolean isImmutable()
    {
        return false;
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
        { return (_CY > 0 ? _CY : _CY - 1);  }

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
     * Gets the rounded millisecond value. Range from 0 to 999
     */
    public final int getMillisecond()
    {
        if (_fs == null || _fs == GDate._zero)
            return 0;
        return _fs.setScale(3, BigDecimal.ROUND_HALF_UP).unscaledValue().intValue();
    }

    /**
     * Gets the time zone sign. For time zones east of GMT,
     * this is positive; for time zones west, this is negative.
     */
    public final int getTimeZoneSign()
        { return _tzsign; }

    /**
     * Gets the time zone hour.
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    public final int getTimeZoneHour()
        { return _tzh; }

    /**
     * Gets the time zone minutes.
     * This is always positive: for the sign, look at
     * getTimeZoneSign().
     */
    public final int getTimeZoneMinute()
        { return _tzm; }

    

    /**
     * Sets the year. Should be a four-digit year specification.
     * @param year the year
     */
    public void setYear(int year)
    {
        if (year < GDate.MIN_YEAR || year > GDate.MAX_YEAR)
            throw new IllegalArgumentException("year out of range");
        if (year == 0)
            throw new IllegalArgumentException("year cannot be 0");
        _bits |= HAS_YEAR; _CY = (year > 0 ? year : year + 1);
    }

    /**
     * Sets the month-of-year. January is 1.
     * @param month the month, from 1-12
     */
    public void setMonth(int month)
    {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException("month out of range");
        _bits |= HAS_MONTH; _M = month;
    }

    /**
     * Sets the day-of-month. The first day of each month is 1.
     * @param day the day of month, from 1-31
     */
    public void setDay(int day)
    {
        if (day < 1 || day > 31)
            throw new IllegalArgumentException("day out of range");
        _bits |= HAS_DAY; _D = day;
    }

    /**
     * Sets the time. Hours in the day range from 0 to 23;
     * minutes and seconds range from 0 to 59; and fractional
     * seconds range from 0 (inclusive) to 1 (exclusive).
     * The fraction can be null and is assumed to be zero.
     * @param hour the hour of day, from 0-23 or 24 only if min, sec and fraction are 0
     * @param minute the minute of hour, from 0-59
     * @param second the second of minute, from 0-59
     * @param fraction the fraction of second, 0.0 to 0.999... (may be null)
     */
    public void setTime(int hour, int minute, int second, BigDecimal fraction)
    {
        if (hour < 0 || hour > 24 )
            throw new IllegalArgumentException("hour out of range");
        if (minute < 0 || minute > 59)
            throw new IllegalArgumentException("minute out of range");
        if (second < 0 || second > 59)
            throw new IllegalArgumentException("second out of range");
        if (fraction != null && (fraction.signum() < 0 || fraction.compareTo(GDate._one) > 1))
            throw new IllegalArgumentException("fraction out of range");
        if ( hour == 24 && (minute!=0 || second!=0 || (fraction!=null && (GDate._zero.compareTo(fraction)!=0)) ))
            throw new IllegalArgumentException("when hour is 24, min sec and fracton must be 0");

        _bits |= HAS_TIME;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? GDate._zero : fraction;
    }

    /**
     * Sets the time zone without changing the other time
     * fields. If you with to adjust other time fields to express
     * the same actual moment in time in a different time zone,
     * use normalizeToTimeZone.
     * <p>
     * Timezones must be between -14:00 and +14:00. Sign
     * must be -1 or 1 (or 0 for UTC only), and the offset hours
     * and minute arguments must be nonnegative.
     * 
     * @param tzSign the timezone offset sign, either +1, 0, or -1
     * @param tzHour the timezone offset hour
     * @param tzMinute the timezone offset minute 
     */
    public void setTimeZone(int tzSign, int tzHour, int tzMinute)
    {
        if (!((tzSign == 0 && tzHour == 0 && tzMinute == 0) ||
              ((tzSign == -1 || tzSign == 1) &&
               (tzHour >= 0 && tzMinute >= 0) &&
               (tzHour == 14 && tzMinute == 0 || tzHour < 14 && tzMinute < 60))))
            throw new IllegalArgumentException("time zone out of range (-14:00 to +14:00). (" +
                (tzSign<0 ? "-" : "+") + tzHour + ":" + tzMinute + ")");

        _bits |= HAS_TIMEZONE;
        _tzsign = tzSign;
        _tzh = tzHour;
        _tzm = tzMinute;
    }
    
    /**
     * Sets the time zone based on a number of offset minutes rather
     * than sign/hour/minute; for example, setTimeZone(-60) is the
     * same as setTimeZone(-1, 1, 0).
     */
    public void setTimeZone(int tzTotalMinutes)
    {
        if (tzTotalMinutes < -14 * 60 || tzTotalMinutes > 14 * 60)
            throw new IllegalArgumentException("time zone out of range (-840 to 840 minutes). (" + tzTotalMinutes + ")");
        
        int tzSign = tzTotalMinutes < 0 ? -1 : tzTotalMinutes > 0 ? 1 : 0;
        tzTotalMinutes *= tzSign;
        int tzH = tzTotalMinutes / 60;
        int tzM = tzTotalMinutes - tzH * 60;
        
        setTimeZone(tzSign, tzH, tzM);
    }

    /**
     * Clears the year. After clearing, hasYear returns false and the
     * value of getYear is undefined.
     */
    public void clearYear()
        { _bits &= ~HAS_YEAR; _CY = 0;  }

    /**
     * Clears the month-of-year. After clearing. hasMonth returns false and
     * the value of getMonth is undefined.
     */
    public void clearMonth()
        { _bits &= ~HAS_MONTH; _M = 0;  }

    /**
     * Clears the day-of-month. After clearing. hasDay returns false and
     * the value of getDay is undefined.
     */
    public void clearDay()
        { _bits &= ~HAS_DAY; _D = 0;  }

    /**
     * Clears the time-of-day.
     * After clearing. hasTime returns false and
     * the value of getTime is undefined.
     */
    public void clearTime()
    {
        _bits &= ~HAS_TIME;
        _h = 0;
        _m = 0;
        _s = 0;
        _fs = null;
    }

    /**
     * Clears the timezone. After clearing. hasTimeZone returns false and
     * the value of getTimeZoneHour and getTimeZoneMinute are undefined.
     * Does not change the other time fields.
     */
    public void clearTimeZone()
    {
        _bits &= ~HAS_TIMEZONE;
        _tzsign = 0;
        _tzh = 0;
        _tzm = 0;
    }

    /**
     * True if all date fields lie within their legal ranges.  A GDateBuilder
     * can be invalid, for example, if you change the month to February
     * and the day-of-month is 31.
     */
    public boolean isValid()
    {
        return isValidGDate(this);
    }

    /* package */ static final boolean isValidGDate(GDateSpecification date)
    {
        if (date.hasYear() && date.getYear() == 0)
            return false;

        if (date.hasMonth() && (date.getMonth() < 1 || date.getMonth() > 12))
            return false;

        if (date.hasDay() && 
            (date.getDay() < 1 || date.getDay() > 31 || 
             date.getDay() > 28 && 
             date.hasMonth() && 
             (date.hasYear() ? 
              date.getDay() > _maxDayInMonthFor((date.getYear() > 0 ? 
                                                 date.getYear() : 
                                                 date.getYear() + 1), 
                                                date.getMonth()) : 
              date.getDay() > _maxDayInMonth(date.getMonth()))))
            return false;

        if (date.hasTime() && ((date.getHour() < 0 || date.getHour() > 23 ||
            date.getMinute() < 0 || date.getMinute() > 59 ||
            date.getSecond() < 0 || date.getSecond() > 59 ||
            date.getFraction().signum() < 0 || date.getFraction().compareTo(GDate._one) >= 0)  ) &&
            // check for 24:00:00 valid format
            !(date.getHour() == 24 && date.getMinute() == 0 && date.getSecond() == 0 &&
              date.getFraction().compareTo(GDate._zero) == 0 ) )
            return false;

        if (date.hasTimeZone() &&
            (!((date.getTimeZoneSign() == 0 && date.getTimeZoneHour() == 0 && date.getTimeZoneMinute() == 0) ||
              ((date.getTimeZoneSign() == -1 || date.getTimeZoneSign() == +1) &&
                // NB: allow +00:00 and -00:00
                // (date.getTimeZoneHour() == 0 && date.getTimeZoneMinute() > 0 || date.getTimeZoneHour() > 0 && date.getTimeZoneMinute() >= 0) &&
               (date.getTimeZoneHour() >= 0 && date.getTimeZoneMinute() >= 0) &&
               (date.getTimeZoneHour() == 14 && date.getTimeZoneMinute() == 0 || date.getTimeZoneHour() < 14 && date.getTimeZoneMinute() < 60)))))
            return false;

        // everyting looks kosher
        return true;
    }


    /**
     * Normalizes the instance, ensuring date and time fields are within
     * their normal ranges.
     * <p>
     * If no timezone or no time is specified, or if a partial date is specified, this
     * method does nothing, and leaves the timezone information as-is.
     * <p>
     * If a time or time and date is specified, this method normalizes the timezone
     * to UTC.
     */
    public void normalize()
    {
        // DateTime or Time, with TimeZone: normalize to UTC.
        // In the process all the fields will be normalized.
        if (hasDay() == hasMonth() && hasDay() == hasYear() &&
            hasTimeZone() && hasTime() )
        {
            normalizeToTimeZone(0, 0, 0);
        }
        else
        {
            // No timezone, or incomplete date.
            _normalizeTimeAndDate();
        }

        // remove trailing zeros from fractional seconds
        if (hasTime() && _fs != null && _fs.scale() > 0)
        {
            if (_fs.signum() == 0)
                _fs = GDate._zero;
            else
            {
                BigInteger bi = _fs.unscaledValue();
                String str = bi.toString();
                int lastzero;
                for (lastzero = str.length(); lastzero > 0; lastzero -= 1)
                    if (str.charAt(lastzero - 1) != '0')
                        break;
                if (lastzero < str.length())
                    _fs = _fs.setScale(_fs.scale() - str.length() + lastzero);
            }
        }
    }

   /**
     * Normalizes the instance when hour is 24. If day is present, hour 24 is equivalent to hour 00 next day.
     */
    void normalize24h()
    {
        if ( !hasTime() || getHour()!=24 )
            return;

        _normalizeTimeAndDate();
    }


    private void _normalizeTimeAndDate()
    {
        long carry = 0;

        if (hasTime())
            carry = _normalizeTime();

        if (hasDay())
            _D += carry;

        if (hasDate())
        {
            _normalizeDate();
        }
        else if (hasMonth())
        {
            // with incomplete dates, just months can be normalized:
            // days stay denormalized.
            if (_M < 1 || _M > 12)
            {
                int temp = _M;
                _M = _modulo(temp, 1, 13);
                if (hasYear())
                    _CY = _CY + (int)_fQuotient(temp, 1, 13);
            }
        }
    }

    /**
     * If the time and timezone are known, this method changes the timezone to the
     * specified UTC offset, altering minutes, hours, day, month, and year as
     * necessary to ensure that the actual described moment in time is the same.
     * <p>
     * It is an error to operate on instances without a time or timezone, or
     * with a partially specified date.
     * 
     * @param tzSign the timezone offset sign, either +1, 0, or -1
     * @param tzHour the timezone offset hour
     * @param tzMinute the timezone offset minute 
     */
    public void normalizeToTimeZone(int tzSign, int tzHour, int tzMinute)
    {
        if (!((tzSign == 0 && tzHour == 0 && tzMinute == 0) ||
              ((tzSign == -1 || tzSign == 1) &&
               (tzHour >= 0 && tzMinute >= 0) &&
               (tzHour == 14 && tzMinute == 0 || tzHour < 14 && tzMinute < 60))))
            throw new IllegalArgumentException("time zone must be between -14:00 and +14:00");

        if (!hasTimeZone() || !hasTime())
            throw new IllegalStateException("cannot normalize time zone without both time and timezone");

        if (!(hasDay() == hasMonth() && hasDay() == hasYear()))
            throw new IllegalStateException("cannot do date math without a complete date");

        int hshift = tzSign * tzHour - _tzsign * _tzh;
        int mshift = tzSign * tzMinute - _tzsign * _tzm;

        _tzsign = tzSign;
        _tzh = tzHour;
        _tzm = tzMinute;
        addDuration(1, 0, 0, 0, hshift, mshift, 0, null);
    }

    /**
     * Normalizes to a time zone specified by a number of offset minutes rather
     * than sign/hour/minute; for example, normalizeToTimeZone(-60) is the
     * same as normalizeToTimeZone(-1, 1, 0).
     */
    public void normalizeToTimeZone(int tzTotalMinutes)
    {
        if (tzTotalMinutes < -14 * 60 || tzTotalMinutes > 14 * 60)
            throw new IllegalArgumentException("time zone out of range (-840 to 840 minutes). (" + tzTotalMinutes + ")");
        
        int tzSign = tzTotalMinutes < 0 ? -1 : tzTotalMinutes > 0 ? 1 : 0;
        tzTotalMinutes *= tzSign;
        int tzH = tzTotalMinutes / 60;
        int tzM = tzTotalMinutes - tzH * 60;
        
        normalizeToTimeZone(tzSign, tzH, tzM);
    }


    /**
     * Adds a given duration to the date/time.
     * 
     * @param duration the duration to add
     */
    public void addGDuration(GDurationSpecification duration)
    {
        addDuration(duration.getSign(), duration.getYear(), duration.getMonth(), duration.getDay(),
                    duration.getHour(), duration.getMinute(), duration.getSecond(), duration.getFraction());
    }

    /**
     * Subtracts a given duration from the date/time.
     * 
     * @param duration the duration to subtract
     */
    public void subtractGDuration(GDurationSpecification duration)
    {
        addDuration(-duration.getSign(), duration.getYear(), duration.getMonth(), duration.getDay(),
                    duration.getHour(), duration.getMinute(), duration.getSecond(), duration.getFraction());
    }


    /**
     * Normalizes the date by carrying over to the year any months outside 1..12
     * and carrying over to the month any days outside 1..(days-in-month).
     */
    private void _normalizeDate()
    {
        if (_M < 1 || _M > 12 || _D < 1 || _D > _maxDayInMonthFor(_CY, _M))
        {
            // fix months first
            int temp = _M;
            _M = _modulo(temp, 1, 13);
            _CY = _CY + (int)_fQuotient(temp, 1, 13);

            // then pull days out
            int extradays = _D - 1;
            _D = 1;

            // then use the julian date function to fix
            setJulianDate(getJulianDate() + extradays);
        }
    }

    /**
     * Normalizes time so that fractions are 0..1(exc), seconds/minutes 0..59,
     * and hours 0..24. Returns the number of days to carry over from normalizing
     * away more than 24 hours.
     */
    private long _normalizeTime()
    {
        long carry = 0;
        long temp;

        // fractions
        if (_fs != null && (_fs.signum() < 0 || _fs.compareTo(GDate._one) >= 0))
        {
            BigDecimal bdcarry = _fs.setScale(0, BigDecimal.ROUND_FLOOR);
            _fs = _fs.subtract(bdcarry);
            carry = bdcarry.longValue();
        }

        if (carry != 0 || _s < 0 || _s > 59 || _m < 0 || _m > 50 || _h < 0 || _h > 23)
        {
            // seconds
            temp = _s + carry;
            carry = _fQuotient(temp, 60);
            _s = _mod(temp, 60, carry);

            // minutes
            temp = _m + carry;
            carry = _fQuotient(temp, 60);
            _m = _mod(temp, 60, carry);

            // hours
            temp = _h + carry;
            carry = _fQuotient(temp, 24);
            _h = _mod(temp, 24, carry);
        }

        return carry;
    }

    /**
     * Adds a given duration to the date/time.
     * 
     * @param sign +1 to add, -1 to subtract
     * @param year the number of years to add
     * @param month the number of months to add
     * @param day the number of days to add
     * @param hour the number of hours to add
     * @param minute the number of minutes to add
     * @param second the number of seconds to add
     * @param fraction the number of fractional seconds to add (may be null)
     */
    public void addDuration(int sign, int year, int month, int day,
                            int hour, int minute, int second, BigDecimal fraction)
    {
        boolean timemath = hour != 0 || minute != 0 || second != 0 || fraction != null && fraction.signum() != 0;
        if (timemath && !hasTime())
            throw new IllegalStateException("cannot do time math without a complete time");
        boolean datemath = hasDay() && (day != 0 || timemath);
        if (datemath && !hasDate())
            throw new IllegalStateException("cannot do date math without a complete date");

        int temp;

        // months + years are easy
        if (month != 0 || year != 0)
        {
            // Prepare the _D to be pegged before changing month
            if (hasDay())
                _normalizeDate();

            // Add months and years
            temp = _M + sign * month;
            _M = _modulo(temp, 1, 13);
            _CY = _CY + sign * year + (int)_fQuotient(temp, 1, 13);

            // In new month, day may need to be pegged before proceeding
            if (hasDay())
            {
                assert(_D >= 1);
                temp = _maxDayInMonthFor(_CY, _M);
                if (_D > temp)
                    _D = temp;
            }
        }

        long carry = 0;

        if (timemath)
        {
            // fractions
            if (fraction != null && fraction.signum() != 0)
            {
                if (_fs.signum() == 0 && sign == 1)
                    _fs = fraction;
                else
                    _fs = (sign == 1) ? _fs.add(fraction) : _fs.subtract(fraction);
            }

            // seconds, minutes, hours
            _s += sign * second;
            _m += sign * minute;
            _h += sign * hour;

            // normalize time
            carry = _normalizeTime();
        }

        if (datemath)
        {
            // days: may require renormalization
            _D += sign * day + carry;
            _normalizeDate();
        }
    }

    /**
     * Given {year,month} computes maximum
     * number of days for given month
     */
    private static int _maxDayInMonthFor(int year, int month)
    {
        if (month == 4 || month == 6 || month == 9 || month == 11)
            return 30;

        if (month == 2)
            return (_isLeapYear(year) ? 29 : 28);

        return 31;
    }

    /**
     * Given {year,month} computes maximum
     * number of days for given month
     */
    private static int _maxDayInMonth(int month)
    {
        if (month == 4 || month == 6 || month == 9 || month == 11)
            return 30;

        if (month == 2)
            return 29;

        return 31;
    }

    /**
     * Returns the Julian date corresponding to this Gregorian date.
     * The Julian date (JD) is a continuous count of days from
     * 1 January 4713 BC.
     */
    public final int getJulianDate()
    {
        return julianDateForGDate(this);
    }


    /**
     * Sets the Gregorian date based on the given Julian date.
     * The Julian date (JD) is a continuous count of days from
     * 1 January 4713 BC.
     * 
     * @param julianday the julian day number
     */
    public void setJulianDate(int julianday)
    {
        if (julianday < 0)
            throw new IllegalArgumentException("date before year -4713");

        int temp;
        int qepoc;

        // from http://aa.usno.navy.mil/faq/docs/JD_Formula.html
        temp = julianday + 68569;
        qepoc = 4 * temp / 146097;
        temp = temp - (146097 * qepoc + 3) / 4;
        _CY = 4000 * (temp + 1) / 1461001;
        temp = temp - 1461 * _CY / 4 + 31;
        _M = 80 * temp / 2447;
        _D = temp - 2447 * _M / 80;
        temp = _M / 11;
        _M = _M + 2 - 12 * temp;
        _CY = 100 * (qepoc - 49) + _CY + temp;

        _bits |= HAS_DAY | HAS_MONTH | HAS_YEAR;
    }


    /**
     * Sets the current time and date based on a java.util.Date instance.
     * <p>
     * The timezone offset used is based on the default TimeZone. (The
     * default TimeZone is consulted to incorporate daylight savings offsets
     * if applicable for the current date as well as the base timezone offset.)
     * <p>
     * If you wish to normalize the timezone, e.g., to UTC, follow this with
     * a call to normalizeToTimeZone.
     * 
     * @param date the Date object to copy
     */
    public void setDate(Date date)
    {
        // Default timezone
        TimeZone dtz = TimeZone.getDefault();
        int offset = dtz.getOffset(date.getTime());
        int offsetsign = 1;
        if (offset < 0)
        {
            offsetsign = -1;
            offset = -offset;
        }
        int offsetmin = offset / (1000 * 60);
        int offsethr = offsetmin / 60;
        offsetmin = offsetmin - offsethr * 60;

        setTimeZone(offsetsign, offsethr, offsetmin);
        
        // paranoia: tz.getOffset can return fractions of minutes, but we must round
        int roundedoffset = offsetsign * (offsethr * 60 + offsetmin) * 60 * 1000;

        // midnight
        setTime(0, 0, 0, GDate._zero);

        // Set to January 1, 1970.
        // setJulianDate(2440588);
        _bits |= HAS_DAY | HAS_MONTH | HAS_YEAR;
        _CY = 1970;
        _M = 1;
        _D = 1;

        // Add a duration representing the number of milliseconds
        addGDuration(new GDuration(1, 0, 0, 0, 0, 0, 0,
                BigDecimal.valueOf(date.getTime() + roundedoffset, 3)));

        // special case: ss.000 -> ss
        if (_fs.signum() == 0)
            _fs = GDate._zero;
    }
    
    /**
     * Copies a GDateSpecification, completely replacing the current
     * information in this GDateBuilder.
     * 
     * @param gdate the GDateSpecification to copy
     */ 
    public void setGDate(GDateSpecification gdate)
    {
        _bits = gdate.getFlags() & (HAS_TIMEZONE | HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME);
        int year = gdate.getYear();
        _CY = (year > 0 ? year : year + 1);
        _M = gdate.getMonth();
        _D = gdate.getDay();
        _h = gdate.getHour();
        _m = gdate.getMinute();
        _s = gdate.getSecond();
        _fs = gdate.getFraction();
        _tzsign = gdate.getTimeZoneSign();
        _tzh = gdate.getTimeZoneHour();
        _tzm = gdate.getTimeZoneMinute();
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
        return dateForGDate(this);
    }

    /* package */ static int julianDateForGDate(GDateSpecification date)
    {
        if (!date.hasDate())
            throw new IllegalStateException("cannot do date math without a complete date");

        // from http://aa.usno.navy.mil/faq/docs/JD_Formula.html
        int day = date.getDay();
        int month = date.getMonth();
        int year = date.getYear();
        year = (year > 0 ? year : year + 1);
        int result = day-32075+1461*(year+4800+(month-14)/12)/4+
            367*(month-2-(month-14)/12*12)/12-3*((year+4900+(month-14)/12)/100)/4;

        if (result < 0)
            throw new IllegalStateException("date too far in the past (year allowed to -4713)");

        return result;
    }

    /* package */ static Date dateForGDate(GDateSpecification date)
    {
        long jDate = julianDateForGDate(date);
        long to1970Date = jDate - 2440588;
        long to1970Ms = 1000 * 60 * 60 * 24 * to1970Date;

        to1970Ms += date.getMillisecond();
        to1970Ms += date.getSecond() * 1000;
        to1970Ms += date.getMinute() * 60 * 1000;
        to1970Ms += date.getHour() * 60 * 60 * 1000;
        if (date.hasTimeZone())
        {
            to1970Ms -= (date.getTimeZoneMinute() * date.getTimeZoneSign()) * 60 * 1000;
            to1970Ms -= (date.getTimeZoneHour() * date.getTimeZoneSign()) * 60 * 60 * 1000;
        }
        else
        {
            TimeZone def = TimeZone.getDefault();
            int offset = def.getOffset(to1970Ms);
            to1970Ms -= offset;
        }

        return new Date(to1970Ms);
    }

    /**
     * True for leap years.
     */
    private static boolean _isLeapYear(int year)
    {
        // BUGBUG: Julian calendar?
        return ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0)));
    }

    /**
     * fQuotient(a, b) = the greatest integer less than or equal to a/b
     */
    private static final long _fQuotient(long a, int b)
    {
        if ((a < 0) == (b < 0))
            return a / b;

        return -((b - a - 1) / b);
    }

    /**
     * modulo(a, b) = a - fQuotient(a,b)*b
     */
    private static int _mod(long a, int b, long quotient)
    {
        return (int)(a - quotient*b) ;
    }

    /**
     * modulo(a - low, high - low) + low
     */
    private static final int _modulo(long temp, int low, int high)
    {
        long a = temp - low;
        int b = high - low;
        return (_mod(a, b, _fQuotient(a, b)) + low) ;
    }

    /**
     * Quotient(a - low, high - low)
     */
    private static final long _fQuotient(long temp, int low, int high)
    {
        return _fQuotient(temp - low, high - low);
    }

    /**
     * Sets to the first possible moment that matches the given
     * specification.
     */
    private void _setToFirstMoment()
    {
        // 1584 was the first leap year during which the Gregorian
        // calendar was in use: seems like the most reasonable "first"
        // year to use in absence of a year.

        if (!hasYear())
            setYear(1584);

        if (!hasMonth())
            setMonth(1);

        if (!hasDay())
            setDay(1);

        if (!hasTime())
            setTime(0, 0, 0, GDate._zero);
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
     * 
     * @param datespec the date to compare against
     */
    public final int compareToGDate(GDateSpecification datespec)
    {
        return compareGDate(this, datespec);
    }


    /* package */ static final int compareGDate(GDateSpecification tdate, GDateSpecification datespec)
    {
        // same amount of information: looks good
        int bitdiff = tdate.getFlags() ^ datespec.getFlags();

        easy: if ((bitdiff & (HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME | HAS_TIMEZONE)) == 0)
        {
            // If the other date needs to be normalized to
            // our timezone, make a clone and do so if possible
            if (tdate.hasTimeZone() &&
                (datespec.getTimeZoneHour() != tdate.getTimeZoneHour() ||
                 datespec.getTimeZoneMinute() != tdate.getTimeZoneMinute() ||
                 datespec.getTimeZoneSign() != tdate.getTimeZoneSign()))
            {
                datespec = new GDateBuilder(datespec);

                int flags = tdate.getFlags() & (HAS_YEAR | HAS_MONTH | HAS_DAY);
                if (flags != 0 && flags != (HAS_YEAR | HAS_MONTH | HAS_DAY) || !tdate.hasTime())
                {
                    // in these cases we'll need to fill in fields
                    ((GDateBuilder)datespec)._setToFirstMoment();
                    tdate = new GDateBuilder(tdate);
                    ((GDateBuilder)tdate)._setToFirstMoment();
                }

                ((GDateBuilder)datespec).normalizeToTimeZone(tdate.getTimeZoneSign(), tdate.getTimeZoneHour(), tdate.getTimeZoneMinute());
            }

            // compare by field
            return fieldwiseCompare(tdate, datespec);
        }

        // different amounts of information (except timezone): not comparable
        if ((bitdiff & (HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME)) != 0)
            return 2;

        // The schema spec says we should try to compare with-timezone and
        // without-timezone specifications... Well, OK, sure, if they say so.

        // We don't have a timezone but the other does: reverse the call
        if (!tdate.hasTimeZone())
        {
            int result = compareGDate(datespec, tdate);
            return result == 2 ? 2 : -result;
        }

        // Now tdate is guaranteed to have a timezone and datespec not.

        // To muck with the times, make clones
        GDateBuilder pdate = new GDateBuilder(tdate);

        // To cover the one uncovered case: if one date is 02/28 and the
        // other date is 03/01, shift days closer by one to simulate being
        // the last day of the month within a leap year
        if ((tdate.getFlags() & (HAS_YEAR | HAS_MONTH | HAS_DAY)) == (HAS_MONTH | HAS_DAY))
        {
            if (tdate.getDay() == 28 && tdate.getMonth() == 2)
            {
                if (datespec.getDay() == 01 && datespec.getMonth() == 3)
                {
                    pdate.setDay(29);
                }
            }
            else if (datespec.getDay() == 28 && datespec.getMonth() == 2)
            {
                if (tdate.getDay() == 01 && tdate.getMonth() == 3)
                {
                    pdate.setMonth(02);
                    pdate.setDay(29);
                }
            }
        }

        // For timespans, compare by first instant of time
        // possible. Therefore, fill in Midnight, January 1, 1584 (a leap year)
        // in absence of other information.
        pdate._setToFirstMoment();

        // P < Q if P < (Q with time zone +14:00)
        GDateBuilder qplusdate = new GDateBuilder(datespec);
        qplusdate._setToFirstMoment();
        qplusdate.setTimeZone(1, 14, 0);
        qplusdate.normalizeToTimeZone(tdate.getTimeZoneSign(), tdate.getTimeZoneHour(), tdate.getTimeZoneMinute());
        if (fieldwiseCompare(pdate, qplusdate) == -1)
            return -1;

        // P > Q if P > (Q with time zone -14:00)
        GDateBuilder qminusdate = qplusdate;
        qminusdate.setGDate(datespec);
        qminusdate._setToFirstMoment();
        qminusdate.setTimeZone(-1, 14, 0);
        qminusdate.normalizeToTimeZone(tdate.getTimeZoneSign(), tdate.getTimeZoneHour(), tdate.getTimeZoneMinute());
        if (fieldwiseCompare(pdate, qminusdate) == 1)
            return 1;

        // P <> Q otherwise
        return 2;
    }

    /**
     * Does a simple most-significant-digit-first comparison,
     * ignoring any timezone or has/doesn't have issues.
     * The data must have been digested first.
     */
    private static int fieldwiseCompare(GDateSpecification tdate, GDateSpecification date)
    {
        if (tdate.hasYear())
        {
            int CY = date.getYear();
            int TCY = tdate.getYear();
            if (TCY < CY) return -1;
            if (TCY > CY) return 1;
        }
        if (tdate.hasMonth())
        {
            int M = date.getMonth();
            int TM = tdate.getMonth();
            if (TM < M) return -1;
            if (TM > M) return 1;
        }
        if (tdate.hasDay())
        {
            int D = date.getDay();
            int TD = tdate.getDay();
            if (TD < D) return -1;
            if (TD > D) return 1;
        }
        if (tdate.hasTime())
        {
            int h = date.getHour();
            int th = tdate.getHour();
            if (th < h) return -1;
            if (th > h) return 1;
            int m = date.getMinute();
            int tm = tdate.getMinute();
            if (tm < m) return -1;
            if (tm > m) return 1;
            int s = date.getSecond();
            int ts = tdate.getSecond();
            if (ts < s) return -1;
            if (ts > s) return 1;
            BigDecimal fs = date.getFraction();
            BigDecimal tfs = tdate.getFraction();
            if (tfs == null && fs == null) return 0;
            return (tfs == null ? GDate._zero : tfs).compareTo(fs == null ? GDate._zero : fs);
        }

        return 0;
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
    public final int getBuiltinTypeCode()
    {
        return btcForFlags(_bits);
    }

    /* package */ static int btcForFlags(int flags)
    {
        switch (flags & (HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME))
        {
            case HAS_YEAR:
                return SchemaType.BTC_G_YEAR;
            case HAS_YEAR | HAS_MONTH:
                return SchemaType.BTC_G_YEAR_MONTH;
            case HAS_MONTH:
                return SchemaType.BTC_G_MONTH;
            case HAS_MONTH | HAS_DAY:
                return SchemaType.BTC_G_MONTH_DAY;
            case HAS_DAY:
                return SchemaType.BTC_G_DAY;
            case HAS_YEAR | HAS_MONTH | HAS_DAY:
                return SchemaType.BTC_DATE;
            case HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME:
                return SchemaType.BTC_DATE_TIME;
            case HAS_TIME:
                return SchemaType.BTC_TIME;
            default:
                return SchemaType.BTC_NOT_BUILTIN;
        }
    }

    /**
     * Clears the fields in this GDateBuilder that are not applicable
     * for the given SchemaType date code.  The code should be
     * {@link SchemaType#BTC_G_YEAR},
     * {@link SchemaType#BTC_G_YEAR_MONTH},
     * {@link SchemaType#BTC_G_MONTH},
     * {@link SchemaType#BTC_G_MONTH_DAY},
     * {@link SchemaType#BTC_G_DAY},
     * {@link SchemaType#BTC_DATE},
     * {@link SchemaType#BTC_DATE_TIME}, or
     * {@link SchemaType#BTC_TIME}.
     * 
     * @param typeCode the type code to apply
     */
    public void setBuiltinTypeCode(int typeCode)
    {
        switch (typeCode)
        {
            case SchemaType.BTC_G_YEAR:
                //HAS_YEAR
                clearMonth();
                clearDay();
                clearTime();
                return;
            case SchemaType.BTC_G_YEAR_MONTH:
                //HAS_YEAR | HAS_MONTH
                clearDay();
                clearTime();
                return;
            case SchemaType.BTC_G_MONTH:
                //HAS_MONTH
                clearYear();
                clearDay();
                clearTime();
                return;
            case SchemaType.BTC_G_MONTH_DAY:
                //HAS_MONTH | HAS_DAY
                clearYear();
                clearTime();
                return;
            case SchemaType.BTC_G_DAY:
                //HAS_DAY
                clearYear();
                clearMonth();
                clearTime();
                return;
            case SchemaType.BTC_DATE:
                //HAS_YEAR | HAS_MONTH | HAS_DAY
                clearTime();
                return;
            case SchemaType.BTC_DATE_TIME:
                //HAS_YEAR | HAS_MONTH | HAS_DAY | HAS_TIME
                return;
            case SchemaType.BTC_TIME:
                //HAS_TIME
                clearYear();
                clearMonth();
                clearDay();
                return;
            default:
                throw new IllegalArgumentException("codeType must be one of SchemaType BTC_  DATE TIME related types.");
        }
    }


    /* package */ static final BigInteger TEN = BigInteger.valueOf(10);

    /**
     * The canonical string representation. Specific moments or
     * times-of-day in a specified timezone are normalized to
     * UTC time to produce a canonical string form for them.
     * Other recurring time specifications keep their timezone
     * information.
     */
    public String canonicalString()
    {
        boolean needNormalize =
            (hasTimeZone() && getTimeZoneSign() != 0 && hasTime() &&
            ((hasDay() == hasMonth() && hasDay() == hasYear())));

        if (!needNormalize && getFraction()!=null && getFraction().scale() > 0)
        {
            BigInteger bi = getFraction().unscaledValue();
            needNormalize = (bi.mod(TEN).signum() == 0);
        }

        if (!needNormalize)
            return toString();

        GDateBuilder cdate = new GDateBuilder(this);
        cdate.normalize();
        return cdate.toString();
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
    public final String toString()
    {
        return GDate.formatGDate(this);
    }

}
