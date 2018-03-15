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
 * Both the immutable GDuration and the mutable GDurationBuilder are
 * GDurationSpecifications.  Use this interface where you want to
 * allow callers to pass any implementation of a GDuration.
 * 
 * @see GDuration
 */
public interface GDurationSpecification
{
    /**
     * True if this instance is immutable.
     */
    boolean isImmutable();

    /**
     * Returns the sign of the duration: +1 is forwards
     * and -1 is backwards in time.
     */
    int getSign();

    /**
     * Gets the year component.
     */
    int getYear();

    /**
     * Gets the month-of-year component.
     */
    int getMonth();

    /**
     * Gets the day-of-month component.
     */
    int getDay();

    /**
     * Gets the hour-of-day component.
     */
    int getHour();

    /**
     * Gets the minute-of-hour component.
     */
    int getMinute();

    /**
     * Gets the second-of-minute component.
     */
    int getSecond();

    /**
     * Gets the fraction-of-second. Range from 0 (inclusive) to 1 (exclusive).
     */
    BigDecimal getFraction();

    /**
     * Returns true if all of the individual components
     * of the duration are nonnegative.
     */
    boolean isValid();

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
    int compareToGDuration(GDurationSpecification duration);
}
