/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.usermodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Stores width and height details about a font.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class FontDetails
{
    private String fontName;
    private int height;
    private Map charWidths = new HashMap();

    /**
     * Construct the font details with the given name and height.
     *
     * @param fontName  The font name.
     * @param height    The height of the font.
     */
    public FontDetails( String fontName, int height )
    {
        this.fontName = fontName;
        this.height = height;
    }

    public String getFontName()
    {
        return fontName;
    }

    public int getHeight()
    {
        return height;
    }

    public void addChar( char c, int width )
    {
        charWidths.put(new Character(c), new Integer(width));
    }

    /**
     * Retrieves the width of the specified character.  If the metrics for
     * a particular character are not available it defaults to returning the
     * width for the 'W' character.
     */
    public int getCharWidth( char c )
    {
        Integer widthInteger = (Integer)(charWidths.get(new Character(c)));
        if (widthInteger == null && c != 'W')
            return getCharWidth('W');
        else
            return widthInteger.intValue();
    }

    public void addChars( char[] characters, int[] widths )
    {
        for ( int i = 0; i < characters.length; i++ )
        {
            charWidths.put( new Character(characters[i]), new Integer(widths[i]));
        }
    }

    /**
     * Create an instance of <code>FontDetails</code> by loading them from the
     * provided property object.
     * @param fontName          the font name
     * @param fontMetricsProps  the property object holding the details of this
     *                          particular font.
     * @return  a new FontDetails instance.
     */
    public static FontDetails create( String fontName, Properties fontMetricsProps )
    {
        String heightStr = fontMetricsProps.getProperty( "font." + fontName + ".height");
        String widthsStr = fontMetricsProps.getProperty( "font." + fontName + ".widths");
        String charactersStr = fontMetricsProps.getProperty( "font." + fontName + ".characters");
        int height = Integer.parseInt(heightStr);
        FontDetails d = new FontDetails(fontName, height);
        String[] charactersStrArray = split(charactersStr, ",", -1);
        String[] widthsStrArray = split(widthsStr, ",", -1);
        if (charactersStrArray.length != widthsStrArray.length)
            throw new RuntimeException("Number of characters does not number of widths for font " + fontName);
        for ( int i = 0; i < widthsStrArray.length; i++ )
        {
            if (charactersStrArray[i].length() != 0)
                d.addChar(charactersStrArray[i].charAt(0), Integer.parseInt(widthsStrArray[i]));
        }
        return d;
    }

    /**
     * Gets the width of all characters in a string.
     *
     * @param str   The string to measure.
     * @return      The width of the string for a 10 point font.
     */
    public int getStringWidth(String str)
    {
        int width = 0;
        for (int i = 0; i < str.length(); i++)
        {
            width += getCharWidth(str.charAt(i));
        }
        return width;
    }

    /**
     * Split the given string into an array of strings using the given
     * delimiter.
     */
    private static String[] split(String text, String separator, int max)
    {
        StringTokenizer tok = new StringTokenizer(text, separator);
        int listSize = tok.countTokens();
        if(max != -1 && listSize > max)
            listSize = max;
        String list[] = new String[listSize];
        for(int i = 0; tok.hasMoreTokens(); i++)
        {
            if(max != -1 && i == listSize - 1)
            {
                StringBuffer buf = new StringBuffer((text.length() * (listSize - i)) / listSize);
                while(tok.hasMoreTokens())
                {
                    buf.append(tok.nextToken());
                    if(tok.hasMoreTokens())
                        buf.append(separator);
                }
                list[i] = buf.toString().trim();
                break;
            }
            list[i] = tok.nextToken().trim();
        }

        return list;
    }


}
