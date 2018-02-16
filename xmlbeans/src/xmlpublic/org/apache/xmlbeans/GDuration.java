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

/**
 * Represents an XML Schema-compatible duration.
 * <p>
 * A duration is made up of a number of years, months, days, hours,
 * minutes, seconds, and fractions of seconds. See the
 * XML Schema specification
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#duration">section on xs:duration</a>
 * for details on the rules for
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#duration-order">comparing durations</a> and
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes">adding durations to dates</a>.
 */
public final class GDuration implements GDurationSpecification, java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int _sign;
    private int _CY;
    private int _M;
    private int _D;
    private int _h;
    private int _m;
    private int _s;
    private BigDecimal _fs;

    /**
     * Constructs an empty GDuration representing zero seconds.
     */
    public GDuration()
    {
        _sign = +1;
        _fs = GDate._zero;
    }

    private static final int SEEN_NOTHING = 0;
    private static final int SEEN_YEAR = 1;
    private static final int SEEN_MONTH = 2;
    private static final int SEEN_DAY = 3;
    private static final int SEEN_HOUR = 4;
    private static final int SEEN_MINUTE = 5;
    private static final int SEEN_SECOND = 6;

    /**
     * Constructs a GDuration from a lexical
     * representation. The lexical space contains the
     * union of the lexical spaces of all the schema
     * date/time types (except for duration).
     */
    public GDuration(CharSequence str)
    {
        // Form:        -PnYnMnDTnHnMnS
        // (where each n may be unsigned integer, i.e., an integer that conforms to the pattern [0-9]+
        // {was: preceded by a - for us}, and the whole may be -)

        // first trim XML whitespace
        int len = str.length();
        int start = 0;
        while (len > 0 && GDate.isSpace(str.charAt(len - 1)))
            len -= 1;
        while (start < len && GDate.isSpace(str.charAt(start)))
            start += 1;

        _sign = 1;
        boolean tmark = false;

        if (start < len && str.charAt(start) == '-')
        {
            _sign = -1;
            start += 1;
        }

        if (start >= len || str.charAt(start) != 'P')
            throw new IllegalArgumentException("duration must begin with P");

        start += 1;

        int seen = SEEN_NOTHING;
        _fs = GDate._zero;

        for (;start < len; start += 1)
        {
            char ch = str.charAt(start);
            if (ch == 'T')
            {
                if (tmark)
                    throw new IllegalArgumentException("duration must have no more than one T'");
                if (seen > SEEN_DAY)
                    throw new IllegalArgumentException("T in duration must precede time fields");
                seen = SEEN_DAY;
                tmark = true;
                start += 1;
                if (start >= len)
                    throw new IllegalArgumentException("illegal duration");
                ch = str.charAt(start);
            }
            if (!GDate.isDigit(ch))
                throw new IllegalArgumentException("illegal duration at char[" + start + "]: '" + ch + "'");
            int value = GDate.digitVal(ch);
            for (;;)
            {
                start += 1;
                ch = (start < len) ? str.charAt(start) : '\0';
                if (!GDate.isDigit(ch))
                    break;
                value = value * 10 + GDate.digitVal(ch);
            }
            if (ch == '.')
            {
                int i = start;
                do i += 1;
                while (i < len && GDate.isDigit(ch = str.charAt(i)));
                _fs = new BigDecimal(str.subSequence(start, i).toString());
                if (i >= len || ch != 'S')
                    throw new IllegalArgumentException("illegal duration");
                start = i;
            }

            switch (seen)
            {
                case SEEN_NOTHING:
                    if (ch == 'Y')
                    {
                        seen = SEEN_YEAR;
                        _CY = value;
                        break;
                    } // fallthrough
                case SEEN_YEAR:
                    if (ch == 'M')
                    {
                        seen = SEEN_MONTH;
                        _M = value;
                        break;
                    } // fallthrough
                case SEEN_MONTH:
                    if (ch == 'D')
                    {
                        seen = SEEN_DAY;
                        _D = value;
                        break;
                    } // fallthrough
                case SEEN_DAY:
                    if (ch == 'H')
                    {
                        if (!tmark)
                            throw new IllegalArgumentException("time in duration must follow T");
                        seen = SEEN_HOUR;
                        _h = value;
                        break;
                    } // fallthrough
                case SEEN_HOUR:
                    if (ch == 'M')
                    {
                        if (!tmark)
                            throw new IllegalArgumentException("time in duration must follow T");
                        seen = SEEN_MINUTE;
                        _m = value;
                        break;
                    } // fallthrough
                case SEEN_MINUTE:
                    if (ch == 'S')
                    {
                        if (!tmark)
                            throw new IllegalArgumentException("time in duration must follow T");
                        seen = SEEN_SECOND;
                        _s = value;
                        break;
                    } // fallthrough
                default:
                    throw new IllegalArgumentException("duration must specify Y M D T H M S in order");
            }
        }

        if ( seen == SEEN_NOTHING )
            throw new IllegalArgumentException("duration must contain at least one number and its designator: " + str);
    }

    /**
     * Constructs a GDuration with the specified sign,
     * year, month, day, hours, minutes, seconds, and optional
     * fractional seconds.
     * @param sign +1 for a positive duration, -1 for a negative duration
     * @throws java.lang.IllegalArgumentException if the sign is not 1 or -1
     */
    public GDuration(
            int sign,
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            BigDecimal fraction)
    {
        if (sign != 1 && sign != -1)
            throw new IllegalArgumentException();
        _sign = sign;
        _CY = year;
        _M = month;
        _D = day;
        _h = hour;
        _m = minute;
        _s = second;
        _fs = fraction == null ? GDate._zero : fraction;
    }

    /**
     * Constructs a GDuration from another GDurationSpecification.
     */
    public GDuration(GDurationSpecification gDuration)
    {
        _sign = gDuration.getSign();
        _CY = gDuration.getYear();
        _M = gDuration.getMonth();
        _D = gDuration.getDay();
        _h = gDuration.getHour();
        _m = gDuration.getMinute();
        _s = gDuration.getSecond();
        _fs = gDuration.getFraction();
    }

    /**
     * Builds another GDate with the same value
     * as this one.
     */
    public Object clone()
    {
        return new GDuration(this);
    }

    /**
     * All GDuration instances return true.
     */
    public final boolean isImmutable()
    {
        return true;
    }

    /**
     * Returns the sign of the duration: +1 is forwards
     * and -1 is backwards in time.
     */
    public final int getSign()
        { return _sign; }

    /**
     * Gets the year component.
     */
    public final int getYear()
        { return _CY;  }

    /**
     * Gets the month-of-year component.
     */
    public final int getMonth()
        { return _M;  }

    /**
     * Gets the day-of-month component.
     */
    public final int getDay()
        { return _D; }

    /**
     * Gets the hour-of-day component.
     */
    public final int getHour()
        { return _h; }

    /**
     * Gets the minute-of-hour component.
     */
    public final int getMinute()
        { return _m; }

    /**
     * Gets the second-of-minute component.
     */
    public final int getSecond()
        { return _s; }


    /**
     * Gets the fraction-of-second. Range from 0 (inclusive) to 1 (exclusive).
     */
    public BigDecimal getFraction()
        { return _fs; }

    /**
     * Returns true if all of the individual components
     * of the duration are nonnegative.
     */
    public boolean isValid()
    {
        return GDurationBuilder.isValidDuration(this);
    }

    /**
     * Comparison to another GDuration.
     * <ul>
     * <li>Returns -1 if this < duration. (less-than)
     * <li>Returns 0 if this == duration. (equal)
     * <li>Returns 1 if this > duration. (greater-than)
     * <li>Returns 2 if this <> duration. (incomparable)
     * </ul>
     * Two instances are incomparable if they have different amounts
     * of information.
     */
    public final int compareToGDuration(GDurationSpecification duration)
    {
        return GDurationBuilder.compareDurations(this, duration);
    }

    /**
     * The natural string representation of the duration.
     * <p>
     * Any components that are zero are omitted. Note that if the duration
     * is invalid, i.e., it has negative components, those negative
     * components are serialized out here. To check for validity, use
     * the isValid() method; and to normalize most durations to a valid
     * form use the normalize() method.
     */
    public String toString()
    {
        return GDurationBuilder.formatDuration(this);
    }

    /**
     * Returns a new GDuration which is the sum of this one and the
     * supplied duration.  Does a fieldwise addition, with no normalization.
     */
    public GDuration add(GDurationSpecification duration)
    {
        int sign = _sign * duration.getSign();
        return _add(duration, sign);
    }

    /**
     * Returns a new GDuration which is the result of subtracting
     * the supplied duration from this one.  Does a fieldwise
     * subtraction, with no normalization.
     */
    public GDuration subtract(GDurationSpecification duration)
    {
        int sign = -_sign * duration.getSign();
        return _add(duration, sign);
    }

    private GDuration _add(GDurationSpecification duration, int sign)
    {
        GDuration result = new GDuration(this);
        result._CY += sign * duration.getYear();
        result._M += sign * duration.getMonth();
        result._D += sign * duration.getDay();
        result._h += sign * duration.getHour();
        result._m += sign * duration.getMinute();
        result._s += sign * duration.getSecond();

        if (duration.getFraction().signum() == 0)
            return result;

        if (result._fs.signum() == 0 && sign == 1)
            result._fs = duration.getFraction();
        else
            result._fs = sign > 0 ?
                    result._fs.add(duration.getFraction()) :
                    result._fs.subtract(duration.getFraction());
        return result;
    }

    /**
     * Two GDurations are equal if all their fields are equal.
     * The equals function does not apply normalizatin.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof GDuration))
            return false;

        GDuration duration = (GDuration)obj;
        return (_sign == duration.getSign() &&
                _CY == duration.getYear() &&
                _M == duration.getMonth() &&
                _D == duration.getDay() &&
                _h == duration.getHour() &&
                _m == duration.getMinute() &&
                _s == duration.getSecond() &&
                _fs.equals(duration.getFraction()));
    }

    public int hashCode()
    {
        return (_s +
                _m * (60 + 7) +
                _h * (60 * 60 + 7) +
                _D * (60 * 60 * 24 + 7) +
                _M * (60 * 60 * 24 * 31 + 7) +
                _CY *(60 * 60 * 24 * 372 + 7) +
                _sign * 11917049);
    }

}
