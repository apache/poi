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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.ss.usermodel.Header;

/**
 * Class to read and manipulate the header.
 * <P>
 * The header works by having a left, center, and right side.  The total cannot
 * be more that 255 bytes long.  One uses this class by getting the HSSFHeader
 * from HSSFSheet and then getting or setting the left, center, and right side.
 * For special things (such as page numbers and date), one can use a the methods
 * that return the characters used to represent these.  One can also change the
 * fonts by using similar methods.
 * <P>
 *
 * @author Shawn Laubach (slaubach at apache dot org)
 */
public class HSSFHeader implements Header, HeaderFooter
{

    HeaderRecord headerRecord;
    String left;
    String center;
    String right;

    /**
     * Constructor.  Creates a new header interface from a header record
     *
     * @param headerRecord Header record to create the header with
     */
    protected HSSFHeader( HeaderRecord headerRecord )
    {
        this.headerRecord = headerRecord;
        String head = headerRecord.getHeader();
        while ( head != null && head.length() > 1 )
        {
            int pos = head.length();
            switch ( head.substring( 1, 2 ).charAt( 0 ) )
            {
                case 'L':
                    if ( head.indexOf( "&C" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&C" ) );
                    }
                    if ( head.indexOf( "&R" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&R" ) );
                    }
                    left = head.substring( 2, pos );
                    head = head.substring( pos );
                    break;
                case 'C':
                    if ( head.indexOf( "&L" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&L" ) );
                    }
                    if ( head.indexOf( "&R" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&R" ) );
                    }
                    center = head.substring( 2, pos );
                    head = head.substring( pos );
                    break;
                case 'R':
                    if ( head.indexOf( "&C" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&C" ) );
                    }
                    if ( head.indexOf( "&L" ) >= 0 )
                    {
                        pos = Math.min( pos, head.indexOf( "&L" ) );
                    }
                    right = head.substring( 2, pos );
                    head = head.substring( pos );
                    break;
                default :
                    head = null;
            }
        }
    }

    /**
     * Get the left side of the header.
     *
     * @return The string representing the left side.
     */
    public String getLeft()
    {
        return left;
    }

    /**
     * Sets the left string.
     *
     * @param newLeft The string to set as the left side.
     */
    public void setLeft( String newLeft )
    {
        left = newLeft;
        createHeaderString();
    }

    /**
     * Get the center of the header.
     *
     * @return The string representing the center.
     */
    public String getCenter()
    {
        return center;
    }

    /**
     * Sets the center string.
     *
     * @param newCenter The string to set as the center.
     */
    public void setCenter( String newCenter )
    {
        center = newCenter;
        createHeaderString();
    }

    /**
     * Get the right side of the header.
     *
     * @return The string representing the right side.
     */
    public String getRight()
    {
        return right;
    }

    /**
     * Sets the right string.
     *
     * @param newRight The string to set as the right side.
     */
    public void setRight( String newRight )
    {
        right = newRight;
        createHeaderString();
    }

    /**
     * Creates the complete header string based on the left, center, and middle
     * strings.
     */
    private void createHeaderString()
    {
        headerRecord.setHeader( "&C" + ( center == null ? "" : center ) +
                "&L" + ( left == null ? "" : left ) +
                "&R" + ( right == null ? "" : right ) );
        headerRecord.setHeaderLength( (byte) headerRecord.getHeader().length() );
    }

    /**
     * Returns the string that represents the change in font size.
     *
     * @param size the new font size
     * @return The special string to represent a new font size
     */
    public static String fontSize( short size )
    {
        return "&" + size;
    }

    /**
     * Returns the string that represents the change in font.
     *
     * @param font  the new font
     * @param style the fonts style
     * @return The special string to represent a new font size
     */
    public static String font( String font, String style )
    {
        return "&\"" + font + "," + style + "\"";
    }

    /**
     * Returns the string representing the current page number
     *
     * @return The special string for page number
     */
    public static String page()
    {
        return "&P";
    }

    /**
     * Returns the string representing the number of pages.
     *
     * @return The special string for the number of pages
     */
    public static String numPages()
    {
        return "&N";
    }

    /**
     * Returns the string representing the current date
     *
     * @return The special string for the date
     */
    public static String date()
    {
        return "&D";
    }

    /**
     * Returns the string representing the current time
     *
     * @return The special string for the time
     */
    public static String time()
    {
        return "&T";
    }

    /**
     * Returns the string representing the current file name
     *
     * @return The special string for the file name
     */
    public static String file()
    {
        return "&F";
    }

    /**
     * Returns the string representing the current tab (sheet) name
     *
     * @return The special string for tab name
     */
    public static String tab()
    {
        return "&A";
    }

    /**
     * Returns the string representing the start underline
     *
     * @return The special string for start underline
     */
    public static String startUnderline()
    {
        return "&U";
    }

    /**
     * Returns the string representing the end underline
     *
     * @return The special string for end underline
     */
    public static String endUnderline()
    {
        return "&U";
    }

    /**
     * Returns the string representing the start double underline
     *
     * @return The special string for start double underline
     */
    public static String startDoubleUnderline()
    {
        return "&E";
    }

    /**
     * Returns the string representing the end double underline
     *
     * @return The special string for end double underline
     */
    public static String endDoubleUnderline()
    {
        return "&E";
    }
}

