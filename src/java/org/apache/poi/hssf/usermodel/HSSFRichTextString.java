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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Rich text unicode string.  These strings can have fonts applied to
 * arbitary parts of the string.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFRichTextString
        implements Comparable
{
    /** Place holder for indicating that NO_FONT has been applied here */
    public static final short NO_FONT = -1;

    String string;
    SortedMap formattingRuns = new TreeMap();

    public HSSFRichTextString()
    {
        this("");
    }

    public HSSFRichTextString( String string )
    {
        this.string = string;
        this.formattingRuns.put(new Integer(0), new Short(NO_FONT));
    }

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply the font to (exclusive)
     * @param fontIndex     The font to use.
     */
    public void applyFont(int startIndex, int endIndex, short fontIndex)
    {
        if (startIndex > endIndex)
            throw new IllegalArgumentException("Start index must be less than end index.");
        if (startIndex < 0 || endIndex > length())
            throw new IllegalArgumentException("Start and end index not in range.");
        if (startIndex == endIndex)
            return;

        Integer from = new Integer(startIndex);
        Integer to = new Integer(endIndex);
        short fontAtIndex = NO_FONT;
        if (endIndex != length())
            fontAtIndex = getFontAtIndex(endIndex);
        formattingRuns.subMap(from, to).clear();
        formattingRuns.put(from, new Short(fontIndex));
        if (endIndex != length())
        {
            if (fontIndex != fontAtIndex)
                formattingRuns.put(to, new Short(fontAtIndex));
        }
    }

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply to font to (exclusive)
     * @param font          The index of the font to use.
     */
    public void applyFont(int startIndex, int endIndex, HSSFFont font)
    {
        applyFont(startIndex, endIndex, font.getIndex());
    }

    /**
     * Sets the font of the entire string.
     * @param font          The font to use.
     */
    public void applyFont(HSSFFont font)
    {
        applyFont(0, string.length(), font);
    }

    /**
     * Returns the plain string representation.
     */
    public String getString()
    {
        return string;
    }

    /**
     * @return  the number of characters in the font.
     */
    public int length()
    {
        return string.length();
    }

    /**
     * Returns the font in use at a particular index.
     *
     * @param index         The index.
     * @return              The font that's currently being applied at that
     *                      index or null if no font is being applied or the
     *                      index is out of range.
     */
    public short getFontAtIndex( int index )
    {
        if (index < 0 || index >= string.length())
            throw new ArrayIndexOutOfBoundsException("Font index " + index + " out of bounds of string");
        Integer key = new Integer(index + 1);
        SortedMap head = formattingRuns.headMap(key);
        if (head.isEmpty())
            throw new IllegalStateException("Should not reach here.  No font found.");
        else
            return ((Short) head.get(head.lastKey())).shortValue();
    }

    /**
     * @return  The number of formatting runs used. There will always be at
     *          least one of font NO_FONT.
     *
     * @see #NO_FONT
     */
    public int numFormattingRuns()
    {
        return formattingRuns.size();
    }

    /**
     * The index within the string to which the specified formatting run applies.
     * @param index     the index of the formatting run
     * @return  the index within the string.
     */
    public int getIndexOfFormattingRun(int index)
    {
        Map.Entry[] runs = (Map.Entry[]) formattingRuns.entrySet().toArray(new Map.Entry[formattingRuns.size()] );
        return ((Integer)runs[index].getKey()).intValue();
    }

    /**
     * Gets the font used in a particular formatting run.
     *
     * @param index     the index of the formatting run
     * @return  the font number used.
     */
    public short getFontOfFormattingRun(int index)
    {
        Map.Entry[] runs = (Map.Entry[]) formattingRuns.entrySet().toArray(new Map.Entry[formattingRuns.size()] );
        return ((Short)(runs[index].getValue())).shortValue();
    }

    /**
     * Compares one rich text string to another.
     */
    public int compareTo( Object o )
    {
        return 0; // todo
    }

    /**
     * @return  the plain text representation of this string.
     */
    public String toString()
    {
        return string;
    }

    /**
     * Applies the specified font to the entire string.
     *
     * @param fontIndex  the font to apply.
     */
    public void applyFont( short fontIndex )
    {
        applyFont(0, string.length(), fontIndex);
    }
}
