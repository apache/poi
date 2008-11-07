/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


/*
 * HSSFDataFormat.java
 *
 * Created on December 18, 2001, 12:42 PM
 */
package org.apache.poi.hssf.usermodel;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * Utility to identify builtin formats.  Now can handle user defined data formats also.  The following is a list of the formats as
 * returned by this class.<P>
 *<P>
 *       0, "General"<br>
 *       1, "0"<br>
 *       2, "0.00"<br>
 *       3, "#,##0"<br>
 *       4, "#,##0.00"<br>
 *       5, "($#,##0_);($#,##0)"<br>
 *       6, "($#,##0_);[Red]($#,##0)"<br>
 *       7, "($#,##0.00);($#,##0.00)"<br>
 *       8, "($#,##0.00_);[Red]($#,##0.00)"<br>
 *       9, "0%"<br>
 *       0xa, "0.00%"<br>
 *       0xb, "0.00E+00"<br>
 *       0xc, "# ?/?"<br>
 *       0xd, "# ??/??"<br>
 *       0xe, "m/d/yy"<br>
 *       0xf, "d-mmm-yy"<br>
 *       0x10, "d-mmm"<br>
 *       0x11, "mmm-yy"<br>
 *       0x12, "h:mm AM/PM"<br>
 *       0x13, "h:mm:ss AM/PM"<br>
 *       0x14, "h:mm"<br>
 *       0x15, "h:mm:ss"<br>
 *       0x16, "m/d/yy h:mm"<br>
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
 *       0x31, "@" - This is text format.<P>
 *       0x31  "text" - Alias for "@"<P>
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Shawn M. Laubach (slaubach at apache dot org)
 */

public class HSSFDataFormat implements DataFormat
{
    private static List builtinFormats = createBuiltinFormats();

    private Vector formats = new Vector();
    private Workbook workbook;
    private boolean movedBuiltins = false;  // Flag to see if need to
    // check the built in list
    // or if the regular list
    // has all entries.

    /**
     * Construncts a new data formatter.  It takes a workbook to have
     * access to the workbooks format records.
     * @param workbook the workbook the formats are tied to.
     */
    public HSSFDataFormat( Workbook workbook )
    {
        this.workbook = workbook;
        Iterator i = workbook.getFormats().iterator();
        while ( i.hasNext() )
        {
            FormatRecord r = (FormatRecord) i.next();
            if ( formats.size() < r.getIndexCode() + 1 )
            {
                formats.setSize( r.getIndexCode() + 1 );
            }
            formats.set( r.getIndexCode(), r.getFormatString() );
        }

    }

    private static synchronized List createBuiltinFormats()
    {
        List builtinFormats = new Vector();
        builtinFormats.add( 0, "General" );
        builtinFormats.add( 1, "0" );
        builtinFormats.add( 2, "0.00" );
        builtinFormats.add( 3, "#,##0" );
        builtinFormats.add( 4, "#,##0.00" );
        builtinFormats.add( 5, "($#,##0_);($#,##0)" );
        builtinFormats.add( 6, "($#,##0_);[Red]($#,##0)" );
        builtinFormats.add( 7, "($#,##0.00);($#,##0.00)" );
        builtinFormats.add( 8, "($#,##0.00_);[Red]($#,##0.00)" );
        builtinFormats.add( 9, "0%" );
        builtinFormats.add( 0xa, "0.00%" );
        builtinFormats.add( 0xb, "0.00E+00" );
        builtinFormats.add( 0xc, "# ?/?" );
        builtinFormats.add( 0xd, "# ??/??" );
        builtinFormats.add( 0xe, "m/d/yy" );
        builtinFormats.add( 0xf, "d-mmm-yy" );
        builtinFormats.add( 0x10, "d-mmm" );
        builtinFormats.add( 0x11, "mmm-yy" );
        builtinFormats.add( 0x12, "h:mm AM/PM" );
        builtinFormats.add( 0x13, "h:mm:ss AM/PM" );
        builtinFormats.add( 0x14, "h:mm" );
        builtinFormats.add( 0x15, "h:mm:ss" );
        builtinFormats.add( 0x16, "m/d/yy h:mm" );

        // 0x17 - 0x24 reserved for international and undocumented
        builtinFormats.add( 0x17, "0x17" );
        builtinFormats.add( 0x18, "0x18" );
        builtinFormats.add( 0x19, "0x19" );
        builtinFormats.add( 0x1a, "0x1a" );
        builtinFormats.add( 0x1b, "0x1b" );
        builtinFormats.add( 0x1c, "0x1c" );
        builtinFormats.add( 0x1d, "0x1d" );
        builtinFormats.add( 0x1e, "0x1e" );
        builtinFormats.add( 0x1f, "0x1f" );
        builtinFormats.add( 0x20, "0x20" );
        builtinFormats.add( 0x21, "0x21" );
        builtinFormats.add( 0x22, "0x22" );
        builtinFormats.add( 0x23, "0x23" );
        builtinFormats.add( 0x24, "0x24" );

        // 0x17 - 0x24 reserved for international and undocumented
        builtinFormats.add( 0x25, "(#,##0_);(#,##0)" );
        builtinFormats.add( 0x26, "(#,##0_);[Red](#,##0)" );
        builtinFormats.add( 0x27, "(#,##0.00_);(#,##0.00)" );
        builtinFormats.add( 0x28, "(#,##0.00_);[Red](#,##0.00)" );
        builtinFormats.add( 0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)" );
        builtinFormats.add( 0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)" );
        builtinFormats.add( 0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)" );
        builtinFormats.add( 0x2c,
                "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)" );
        builtinFormats.add( 0x2d, "mm:ss" );
        builtinFormats.add( 0x2e, "[h]:mm:ss" );
        builtinFormats.add( 0x2f, "mm:ss.0" );
        builtinFormats.add( 0x30, "##0.0E+0" );
        builtinFormats.add( 0x31, "@" );
        return builtinFormats;
    }

    public static List getBuiltinFormats()
    {
        return builtinFormats;
    }

    /**
     * get the format index that matches the given format string<p>
     * Automatically converts "text" to excel's format string to represent text.
     * @param format string matching a built in format
     * @return index of format or -1 if undefined.
     */

    public static short getBuiltinFormat( String format )
    {
	if (format.toUpperCase().equals("TEXT")) 
		format = "@";

        short retval = -1;

        for (short k = 0; k <= 0x31; k++)
        {
            String nformat = (String) builtinFormats.get( k );

            if ( ( nformat != null ) && nformat.equals( format ) )
            {
                retval = k;
                break;
            }
        }
        return retval;
    }

    /**
     * Get the format index that matches the given format
     *  string, creating a new format entry if required.
     * Aliases text to the proper format as required.
     * @param format string matching a built in format
     * @return index of format.
     */
    public short getFormat( String format )
    {
        ListIterator i;
        int ind;

	if (format.toUpperCase().equals("TEXT")) 
		format = "@";

        if ( !movedBuiltins )
        {
            i = builtinFormats.listIterator();
            while ( i.hasNext() )
            {
                ind = i.nextIndex();
		if ( formats.size() < ind + 1 )
		{
		    formats.setSize( ind + 1 );
		}
		
                formats.set( ind, i.next() );
            }
            movedBuiltins = true;
        }
        i = formats.listIterator();
        while ( i.hasNext() )
        {
            ind = i.nextIndex();
            if ( format.equals( i.next() ) )
                return (short) ind;
        }

        ind = workbook.getFormat( format, true );
        if ( formats.size() <= ind )
            formats.setSize( ind + 1 );
        formats.set( ind, format );

        return (short) ind;
    }

    /**
     * get the format string that matches the given format index
     * @param index of a format
     * @return string represented at index of format or null if there is not a  format at that index
     */

    public String getFormat( short index )
    {
        if ( movedBuiltins )
            return (String) formats.get( index );
        else
            return (String) ( builtinFormats.size() > index
                    && builtinFormats.get( index ) != null
                    ? builtinFormats.get( index ) : formats.get( index ) );
    }

    /**
     * get the format string that matches the given format index
     * @param index of a built in format
     * @return string represented at index of format or null if there is not a builtin format at that index
     * @throws ArrayOutOfBoundsException when the index exceeds the number of builtin formats.
     */

    public static String getBuiltinFormat( short index )
    {
        return (String) builtinFormats.get( index );
    }

    /**
     * get the number of builtin and reserved builtinFormats
     * @return number of builtin and reserved builtinFormats
     */

    public static int getNumberOfBuiltinBuiltinFormats()
    {
        return builtinFormats.size();
    }
}
