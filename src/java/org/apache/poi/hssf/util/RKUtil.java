/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.util;

/**
 * Utility class for helping convert RK numbers.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Rolf-Jürgen Moll
 *
 * @see org.apache.poi.hssf.record.MulRKRecord
 * @see org.apache.poi.hssf.record.RKRecord
 */
public class RKUtil
{
    private RKUtil()
    {
    }

    /**
     * Do the dirty work of decoding; made a private static method to
     * facilitate testing the algorithm
     */

    public static double decodeNumber(int number)
    {
        long raw_number = number;

        // mask off the two low-order bits, 'cause they're not part of
        // the number
        raw_number = raw_number >> 2;
        double rvalue = 0;

        if ((number & 0x02) == 0x02)
        {
            // ok, it's just a plain ol' int; we can handle this
            // trivially by casting
            rvalue = ( double ) (raw_number);
        }
        else
        {

            // also trivial, but not as obvious ... left shift the
            // bits high and use that clever static method in Double
            // to convert the resulting bit image to a double
            rvalue = Double.longBitsToDouble(raw_number << 34);
        }
        if ((number & 0x01) == 0x01)
        {

            // low-order bit says divide by 100, and so we do. Why?
            // 'cause that's what the algorithm says. Can't fight city
            // hall, especially if it's the city of Redmond
            rvalue /= 100;
        }

        return rvalue;
    }

}
