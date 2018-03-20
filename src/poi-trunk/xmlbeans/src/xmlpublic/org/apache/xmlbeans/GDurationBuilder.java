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

/**
 * Used to build {@link GDuration GDurations}.
 */ 
public class GDurationBuilder implements GDurationSpecification, java.io.Serializable
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
     * Constructs an empty GDurationBuilder representing zero seconds.
     */
    public GDurationBuilder()
    {
        _sign = +1;
        _fs = GDate._zero;
    }

    /**
     * Constructs a GDuration from a lexical
     * representation.
     */
    public GDurationBuilder(String s)
    {
        this(new GDuration(s));
    }

    /**
     * Constructs a GDurationBuilder with the specified sign,
     * year, month, day, hours, minutes, seconds, and optional
     * fractional seconds.
     * @param sign +1 for a positive duration, -1 for a negative duration
     * @throws java.lang.IllegalArgumentException if the sign is not 1 or -1
     */
    public GDurationBuilder(
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
     * Constructs a GDurationBuilder from another GDurationBuilderSpecification.
     */
    public GDurationBuilder(GDurationSpecification gDuration)
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
     * Builds another GDurationBuilder with the same value
     * as this one.
     */
    public Object clone()
    {
        return new GDurationBuilder(this);
    }

    /**
     * Builds a GDuration from this GDurationBuilder.
     */
    public GDuration toGDuration()
    {
        return new GDuration(this);
    }

    /**
     * Adds to this duration.  Does a fieldwise add, with no
     * normalization.
     */
    public void addGDuration(GDurationSpecification duration)
    {
        int sign = _sign * duration.getSign();
        _add(duration, sign);
    }

    /**
     * Subtracts from this duration.  Does a fieldwise subtraction,
     * with no normalization.
     */
    public void subtractGDuration(GDurationSpecification duration)
    {
        int sign = -_sign * duration.getSign();
        _add(duration, sign);
    }

    private void _add(GDurationSpecification duration, int sign)
    {
        _CY += sign * duration.getYear();
        _M += sign * duration.getMonth();
        _D += sign * duration.getDay();
        _h += sign * duration.getHour();
        _m += sign * duration.getMinute();
        _s += sign * duration.getSecond();

        if (duration.getFraction().signum() == 0)
            return;

        if (_fs.signum() == 0 && sign == 1)
            _fs = duration.getFraction();
        else
            _fs = sign > 0 ?
                    _fs.add(duration.getFraction()) :
                    _fs.subtract(duration.getFraction());
    }

    /**
     * Sets the sign.
     */
    public final void setSign(int sign)
    {
        if (sign != 1 && sign != -1)
            throw new IllegalArgumentException();
        _sign = sign;
    }

    /**
     * Sets the year component.
     */
    public void setYear(int year)
        { _CY = year; }

    /**
     * Sets the month component.
     */
    public void setMonth(int month)
        { _M = month; }

    /**
     * Sets the day component.
     */
    public void setDay(int day)
        { _D = day;  }

    /**
     * Sets the hour component.
     */
    public void setHour(int hour)
        { _h = hour;  }

    /**
     * Sets the minute component.
     */
    public void setMinute(int minute)
        { _m = minute; }

    /**
     * Sets the second component.
     */
    public void setSecond(int second)
        { _s = second; }

    /**
     * Sets the fraction-of-second component.
     */
    public void setFraction(BigDecimal fraction)
        { _fs = fraction == null ? GDate._zero : fraction; }

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
     * This value does not necessarily reflect the
     * true direction of the duration if the duration
     * is not normalized or not normalizable.
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
     * Normalize a duration value. This ensures that months,
     * hours, minutes, seconds, and fractions are positive and
     * within the ranges 0..11, 0..23, 0..59, etc. Negative
     * durations are indicated by a negative sign rather
     * than negative components.
     * <p>
     * Most duration specifications can be normalized to
     * valid durations with all positive components, but
     * not all of them can.
     * <p>
     * The only situations which cannot be normalized are
     * where the year/month and the day/hour/minute/second
     * offsets are of opposite sign. Days cannot be carried
     * into months since the length of a Gregorian month is
     * variable depending on when the duration is applied.
     * In these cases, this method normalizes the components
     * so that "day" is the only negative component.
     */
    public void normalize()
    {
        _normalizeImpl(true);
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
    private static final int _mod(long a, int b, long quotient)
    {
        return (int)(a - quotient*b) ;
    }


    /**
     * Private implemenation of normalize. The flag is
     * to facilitate this method calling itself without
     * danger of infinite recursion.
     */
    private void _normalizeImpl(boolean adjustSign)
    {
        long temp;

        // months to years
        if (_M < 0 || _M > 11)
        {
            temp = _M;
            long ycarry = _fQuotient(temp, 12);
            _M = _mod(temp, 12, ycarry);
            _CY += ycarry;
        }

        long carry = 0;

        // fractions to seconds
        if (_fs != null && (_fs.signum() < 0 || _fs.compareTo(GDate._one) >= 0))
        {
            BigDecimal bdcarry = _fs.setScale(0, BigDecimal.ROUND_FLOOR);
            _fs = _fs.subtract(bdcarry);
            carry = bdcarry.intValue();
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
            _D += carry;
        }

        if (_CY == 0 && _M == 0 && _D == 0 && _h == 0 && _m == 0 && _s == 0 && (_fs == null || _fs.signum() == 0))
            _sign = 1;

        if (adjustSign && (_D < 0 || _CY < 0))
        {
            int sign = (_D <= 0 && (_CY < 0 || _CY == 0 && _M == 0)) ? -_sign : _getTotalSignSlowly();
            if (sign == 2)
                sign = (_CY < 0) ? -_sign : _sign;
            if (sign == 0)
                sign = 1;
            if (sign != _sign)
            {
                _sign = sign;
                _CY = -_CY;
                _M = -_M;
                _D = -_D;
                _h = -_h;
                _m = -_m;
                _s = -_s;
                if (_fs != null)
                    _fs = _fs.negate();
            }
            _normalizeImpl(false);
        }
    }


    /* package */ static boolean isValidDuration(GDurationSpecification spec)
    {
        if (!(spec.getSign() == 1 || spec.getSign() == -1))
            return false;

        return (spec.getYear() >= 0 && spec.getMonth() >= 0 && spec.getDay() >= 0 &&
                spec.getHour() >= 0 && spec.getMinute() >= 0  && spec.getSecond() >= 0 &&
                spec.getFraction().signum() >= 0);
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

    /* package */ static int compareDurations(GDurationSpecification d1, GDurationSpecification d2)
    {
        // first do an all-fields check
        if (d1.getFraction().signum() == 0 && d2.getFraction().signum() == 0)
        {
            int s1 = d1.getSign();
            int s2 = d2.getSign();
            long month1 = s1 * ((long)d1.getYear() * 12 + d1.getMonth());
            long month2 = s2 * ((long)d2.getYear() * 12 + d2.getMonth());
            long sec1 = s1 * ((((long)d1.getDay() * 24 + d1.getHour()) * 60 + d1.getMinute()) * 60 + d1.getSecond());
            long sec2 = s2 * ((((long)d2.getDay() * 24 + d2.getHour()) * 60 + d2.getMinute()) * 60 + d2.getSecond());
            if (month1 == month2)
            {
                if (sec1 == sec2)
                    return 0;
                if (sec1 < sec2)
                    return -1;
                if (sec1 > sec2)
                    return 1;
            }
            if (month1 < month2 && sec1 - sec2 < 28 * 24 * 60 * 60)
                return -1;
            if (month1 > month2 && sec2 - sec1 < 28 * 24 * 60 * 60)
                return 1;
        }

        // the answer isn't obvious, so then do a total-sign check
        GDurationBuilder diff = new GDurationBuilder(d1);
        diff.subtractGDuration(d2);
        return diff._getTotalSignSlowly();
    }

    /**
     * Per schema spec, comparison of durations is simply done
     * by calculating adding the duration to these four dates and
     * comparing the results. If the results are ambiguous, the
     * answer is "incomparable".
     */
    private static final GDate[] _compDate = new GDate[]
    {
        new GDate(1696, 9, 1, 0, 0, 0, null, 0, 0, 0),
        new GDate(1697, 2, 1, 0, 0, 0, null, 0, 0, 0),
        new GDate(1903, 3, 1, 0, 0, 0, null, 0, 0, 0),
        new GDate(1903, 7, 1, 0, 0, 0, null, 0, 0, 0)
    };


    /**
     * This returns the total sign of the duration, +1
     * if the duration moves forward in time, -1 if the
     * duration moves backwards in time, 0 if the duration
     * is zero-length, and 2 if the duration may be positive
     * or negative depending on the date.
     *
     * (For example, one month minus 30 days is indeterminate).
     */
    private int _getTotalSignSlowly()
    {
        int pos = 0;
        int neg = 0;
        int zer = 0;

        GDateBuilder enddate = new GDateBuilder();
        for (int i = 0; i < _compDate.length; i++)
        {
            enddate.setGDate(_compDate[i]);
            enddate.addGDuration(this);
            switch (enddate.compareToGDate(_compDate[i]))
            {
                case -1:
                    neg++; break;
                case 0:
                    zer++; break;
                case 1:
                    pos++; break;
            }
        }

        if (pos == _compDate.length)
            return +1;
        if (neg == _compDate.length)
            return -1;
        if (zer == _compDate.length)
            return 0;
        return 2;
    }

    /* package */ static String formatDuration(GDurationSpecification duration)
    {
        // Sign+P:      (-)?P
        // Year:        (?:(\d+)Y)?
        // Month:       (?:(\d+)M)?
        // Day:         (?:(\d+)D)?
        // Time:        (?:(T)
        // Hours:          (?:(\d+)H)?
        // Minutes:        (?:(\d+)M)?
        // Seconds:        (?:(\d+(?:\.\d*)?|(?:.\d+)S)?

        StringBuffer message = new StringBuffer(30);

        if (duration.getSign() < 0)
            message.append('-');

        message.append('P');

        if (duration.getYear() != 0)
        {
            message.append(duration.getYear());
            message.append('Y');
        }

        if (duration.getMonth() != 0)
        {
            message.append(duration.getMonth());
            message.append('M');
        }

        if (duration.getDay() != 0)
        {
            message.append(duration.getDay());
            message.append('D');
        }

        if (duration.getHour() != 0 || duration.getMinute() != 0 || duration.getSecond() != 0 ||
             (duration.getFraction().signum() != 0))
        {
            message.append('T');
        }

        if (duration.getHour() != 0)
        {
            message.append(duration.getHour());
            message.append('H');
        }

        if (duration.getMinute() != 0)
        {
            message.append(duration.getMinute());
            message.append('M');
        }

        if (duration.getFraction().signum() != 0)
        {
            BigDecimal s = duration.getFraction();
            if (duration.getSecond() != 0)
                s = s.add(BigDecimal.valueOf(duration.getSecond()));
            // todo when upgrade to 1.5  message.append(s.stripTrailingZeros().toPlainString());
            message.append(stripTrailingZeros(toPlainString(s)));
            message.append('S');
        }
        else if (duration.getSecond() != 0)
        {
            message.append(duration.getSecond());
            message.append('S');
        }
        else if (message.length() <= 2)
            // Specify zero seconds if everything was 0
            message.append("T0S");

        return message.toString();
    }

    public static String toPlainString(BigDecimal bd)
    {
        BigInteger intVal = bd.unscaledValue();
        int scale = bd.scale();
        String intValStr = intVal.toString();
        if (scale == 0)
            return intValStr;

        boolean isNegative = (intValStr.charAt(0) == '-');

        int point = intValStr.length() - scale - (isNegative ? 1 : 0);

        StringBuffer sb = new StringBuffer(intValStr.length() + 2 + (point <= 0 ? (-point + 1) : 0));
        if (point <= 0)
        {
            // prepend zeros and a decimal point.
            if (isNegative) sb.append('-');
            sb.append('0').append('.');
            while (point < 0)
            {
                sb.append('0');
                point++;
            }
            sb.append(intValStr.substring(isNegative ? 1 : 0));
        }
        else if (point < intValStr.length())
        {
            // No zeros needed
            sb.append(intValStr);
            sb.insert(point + (isNegative ? 1 : 0), '.');
        }
        else
        {
            // append zeros if not 0
            sb.append(intValStr);
            if (!intVal.equals(BigInteger.ZERO))
                for (int i = intValStr.length(); i < point; i++)
                    sb.append('0');
        }
        return sb.toString();
    }

    public static String stripTrailingZeros(String s)
    {
        boolean seenDot = false;
        int i = s.length() - 1;
        int zeroIndex = i;

        while(i>=0)
        {
            if (s.charAt(i)!='0')
                break;
            i--;
            zeroIndex--;
        }
        while(i>=0)
        {
            if (s.charAt(i)=='E')
                return s;
            if (s.charAt(i)=='.')
            {
                seenDot = true;
                break;
            }
            i--;
        }
        
        return seenDot? s.substring(0, zeroIndex+1) : s;
    }
}
