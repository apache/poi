
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

/*
 * HSSFDataFormat.java
 *
 * Created on December 18, 2001, 12:42 PM
 */
package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to identify builin formats.  The following is a list of the formats as
 * returned by this class.<P>
 *<P>
 *       0, "General"<P>
 *       1, "0"<P>
 *       2, "0.00"<P>
 *       3, "#,##0"<P>
 *       4, "#,##0.00"<P>
 *       5, "($#,##0_);($#,##0)"<P>
 *       6, "($#,##0_);[Red]($#,##0)"<P>
 *       7, "($#,##0.00);($#,##0.00)"<P>
 *       8, "($#,##0.00_);[Red]($#,##0.00)"<P>
 *       9, "0%"<P>
 *       0xa, "0.00%"<P>
 *       0xb, "0.00E+00"<P>
 *       0xc, "# ?/?"<P>
 *       0xd, "# ??/??"<P>
 *       0xe, "m/d/yy"<P>
 *       0xf, "d-mmm-yy"<P>
 *       0x10, "d-mmm"<P>
 *       0x11, "mmm-yy"<P>
 *       0x12, "h:mm AM/PM"<P>
 *       0x13, "h:mm:ss AM/PM"<P>
 *       0x14, "h:mm"<P>
 *       0x15, "h:mm:ss"<P>
 *       0x16, "m/d/yy h:mm"<P>
 *<P>
 *       // 0x17 - 0x24 reserved for international and undocumented
 *       0x25, "(#,##0_);(#,##0)"<P>
 *       0x26, "(#,##0_);[Red](#,##0)"<P>
 *       0x27, "(#,##0.00_);(#,##0.00)"<P>
 *       0x28, "(#,##0.00_);[Red](#,##0.00)"<P>
 *       0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)"<P>
 *       0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)"<P>
 *       0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)"<P>
 *       0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)"<P>
 *       0x2d, "mm:ss"<P>
 *       0x2e, "[h]:mm:ss"<P>
 *       0x2f, "mm:ss.0"<P>
 *       0x30, "##0.0E+0"<P>
 *       0x31, "@"<P>
 *
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 */

public class HSSFDataFormat
{
    private static ArrayList formats;

    private static synchronized void populateFormats()
    {
        formats = new ArrayList();
        formats.add(0, "General");
        formats.add(1, "0");
        formats.add(2, "0.00");
        formats.add(3, "#,##0");
        formats.add(4, "#,##0.00");
        formats.add(5, "($#,##0_);($#,##0)");
        formats.add(6, "($#,##0_);[Red]($#,##0)");
        formats.add(7, "($#,##0.00);($#,##0.00)");
        formats.add(8, "($#,##0.00_);[Red]($#,##0.00)");
        formats.add(9, "0%");
        formats.add(0xa, "0.00%");
        formats.add(0xb, "0.00E+00");
        formats.add(0xc, "# ?/?");
        formats.add(0xd, "# ??/??");
        formats.add(0xe, "m/d/yy");
        formats.add(0xf, "d-mmm-yy");
        formats.add(0x10, "d-mmm");
        formats.add(0x11, "mmm-yy");
        formats.add(0x12, "h:mm AM/PM");
        formats.add(0x13, "h:mm:ss AM/PM");
        formats.add(0x14, "h:mm");
        formats.add(0x15, "h:mm:ss");
        formats.add(0x16, "m/d/yy h:mm");

        // 0x17 - 0x24 reserved for international and undocumented
        formats.add(0x17, "0x17");
        formats.add(0x18, "0x18");
        formats.add(0x19, "0x19");
        formats.add(0x1a, "0x1a");
        formats.add(0x1b, "0x1b");
        formats.add(0x1c, "0x1c");
        formats.add(0x1d, "0x1d");
        formats.add(0x1e, "0x1e");
        formats.add(0x1f, "0x1f");
        formats.add(0x20, "0x20");
        formats.add(0x21, "0x21");
        formats.add(0x22, "0x22");
        formats.add(0x23, "0x23");
        formats.add(0x24, "0x24");

        // 0x17 - 0x24 reserved for international and undocumented
        formats.add(0x25, "(#,##0_);(#,##0)");
        formats.add(0x26, "(#,##0_);[Red](#,##0)");
        formats.add(0x27, "(#,##0.00_);(#,##0.00)");
        formats.add(0x28, "(#,##0.00_);[Red](#,##0.00)");
        formats.add(0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)");
        formats.add(0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)");
        formats.add(0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)");
        formats.add(0x2c,
                    "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)");
        formats.add(0x2d, "mm:ss");
        formats.add(0x2e, "[h]:mm:ss");
        formats.add(0x2f, "mm:ss.0");
        formats.add(0x30, "##0.0E+0");
        formats.add(0x31, "@");
    }

    public static List getFormats()
    {
        if (formats == null)
        {
            populateFormats();
        }
        return formats;
    }

    /**
     * get the format index that matches the given format string
     * @param format string matching a built in format
     * @return index of format or -1 if undefined.
     */

    public static short getFormat(String format)
    {
        if (formats == null)
        {
            populateFormats();
        }
        short retval = -1;

        for (short k = 0; k < 0x31; k++)
        {
            String nformat = ( String ) formats.get(k);

            if ((nformat != null) && nformat.equals(format))
            {
                retval = k;
                break;
            }
        }
        return retval;
    }

    /**
     * get the format string that matches the given format index
     * @param index of a built in format
     * @return string represented at index of format or null if there is not a builtin format at that index
     */

    public static String getFormat(short index)
    {
        if (formats == null)
        {
            populateFormats();
        }
        return ( String ) formats.get(index);
    }

    /**
     * get the number of builtin and reserved formats
     * @return number of builtin and reserved formats
     */

    public static int getNumberOfBuiltinFormats()
    {
        if (formats == null)
        {
            populateFormats();
        }
        return formats.size();
    }
}
