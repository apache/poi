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

package org.apache.poi.ss.usermodel;

/**
 * Rich text unicode string.  These strings can have fonts 
 *  applied to arbitary parts of the string.
 *  
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
public interface RichTextString {
    
    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply the font to (exclusive)
     * @param fontIndex     The font to use.
     */
    void applyFont(int startIndex, int endIndex, short fontIndex);

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply to font to (exclusive)
     * @param font          The index of the font to use.
     */
    void applyFont(int startIndex, int endIndex, Font font);

    /**
     * Sets the font of the entire string.
     * @param font          The font to use.
     */
    void applyFont(Font font);

    /**
     * Removes any formatting that may have been applied to the string.
     */
    void clearFormatting();

    /**
     * Returns the plain string representation.
     */
    String getString();

    /**
     * @return  the number of characters in the font.
     */
    int length();

    /**
     * @return  The number of formatting runs used.
     *
     */
    int numFormattingRuns();

    /**
     * The index within the string to which the specified formatting run applies.
     * @param index     the index of the formatting run
     * @return  the index within the string.
     */
    int getIndexOfFormattingRun(int index);

    /**
     * Applies the specified font to the entire string.
     *
     * @param fontIndex  the font to apply.
     */
    void applyFont(short fontIndex);

}