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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;


/**
 * Rich text unicode string.  These strings can have fonts applied to arbitary parts of the string.
 *
 * <p>
 * Most strings in a workbook have formatting applied at the cell level, that is, the entire string in the cell has the
 * same formatting applied. In these cases, the formatting for the cell is stored in the styles part,
 * and the string for the cell can be shared across the workbook. The following xml and code snippet illustrate the example.
 * </p>
 *
 * <blockquote>
 * <pre>
 * &lt;sst xmlns=http://schemas.openxmlformats.org/spreadsheetml/2006/5/main
 * count="1" uniqueCount="1">
 * &lt;si&gt;
 * &lt;t&gt;Apache POI&lt;/t&gt;
 * &lt;/si&gt;
 * &lt;/sst&gt;
 * </pre>
 * </blockquote>
 *
 * The code to produce xml above:
 * <blockquote>
 * <pre>
 *     cell1.setCellValue(new XSSFRichTextString("Apache POI"));
 *     cell2.setCellValue(new XSSFRichTextString("Apache POI"));
 *     cell3.setCellValue(new XSSFRichTextString("Apache POI"));
 * </pre>
 * </blockquote>
 * In the above example all three cells will use the same string cached on workbook level.
 *
 * <p>
 * Some strings in the workbook may have formatting applied at a level that is more granular than the cell level.
 * For instance, specific characters within the string may be bolded, have coloring, italicizing, etc.
 * In these cases, the formatting is stored along with the text in the string table, and is treated as
 * a unique entry in the workbook. The following xml and code snippet illustrate this.
 * </p>
 *
 * <blockquote>
 * <pre>
 *     XSSFRichTextString s1 = new XSSFRichTextString("Apache POI");
 *     s1.applyFont(boldArial);
 *     cell1.setCellValue(s1);
 *
 *     XSSFRichTextString s2 = new XSSFRichTextString("Apache POI");
 *     s2.applyFont(italicCourier);
 *     cell2.setCellValue(s2);
 * </pre>
 * </blockquote>
 *
 * The code above will produce the following xml:
 * <blockquote>
 * <pre>
 * &lt;sst xmlns=http://schemas.openxmlformats.org/spreadsheetml/2006/5/main count="2" uniqueCount="2"&gt;
 *  &lt;si&gt;
 *    &lt;r&gt;
 *      &lt;rPr&gt;
 *        &lt;b/&gt;
 *        &lt;sz val="11"/&gt;
 *        &lt;color theme="1"/&gt;
 *        &lt;rFont val="Arial"/&gt;
 *        &lt;family val="2"/&gt;
 *        &lt;scheme val="minor"/&gt;
 *      &lt;/rPr&gt;
 *      &lt;t&gt;Apache POI&lt;/t&gt;
 *    &lt;/r&gt;
 *  &lt;/si&gt;
 *  &lt;si&gt;
 *    &lt;r&gt;
 *      &lt;rPr&gt;
 *       &lt;i/&gt;
 *       &lt;sz val="11"/&gt;
 *        &lt;color theme="1"/&gt;
 *        &lt;rFont val="Courier"/&gt;
 *        &lt;family val="1"/&gt;
 *        &lt;scheme val="minor"/&gt;
 *      &lt;/rPr&gt;
 *      &lt;t&gt;Apache POI&lt;/t&gt;
 *    &lt;/r&gt;
 *  &lt;/si&gt;
 *&lt;/sst&gt;
 *
 * </pre>
 * </blockquote>
 *
 * @author Yegor Kozlov
 */
public class XSSFRichTextString implements RichTextString {
    private CTRst st;
    private StylesTable styles;

    public XSSFRichTextString(String str) {
        st = CTRst.Factory.newInstance();
        st.setT(str);
    }

    public XSSFRichTextString() {
        st = CTRst.Factory.newInstance();
    }

    public XSSFRichTextString(CTRst st) {
        this.st = st;
    }

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply the font to (exclusive)
     * @param fontIndex     The font to use.
     */
    public void applyFont(int startIndex, int endIndex, short fontIndex) {
        // TODO Auto-generated method stub

    }

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply to font to (exclusive)
     * @param font          The index of the font to use.
     */
    public void applyFont(int startIndex, int endIndex, Font font) {
        applyFont(0, length(), font.getIndex());
    }

    /**
     * Sets the font of the entire string.
     * @param font          The font to use.
     */
    public void applyFont(Font font) {
        applyFont(0, length(), font);
    }

    /**
     * Applies the specified font to the entire string.
     *
     * @param fontIndex  the font to apply.
     */
    public void applyFont(short fontIndex) {
        applyFont(0, length(), fontIndex);
    }

    /**
     * Removes any formatting that may have been applied to the string.
     */
    public void clearFormatting() {
        for (int i = 0; i < st.sizeOfRArray(); i++) {
            st.removeR(i);
        }
    }

    /**
     * Returns the font in use at a particular index.
     *
     * @param index         The index.
     * @return              The font that's currently being applied at that
     *                      index or null if no font is being applied or the
     *                      index is out of range.
     */
    public short getFontAtIndex(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Gets the font used in a particular formatting run.
     *
     * @param index     the index of the formatting run
     * @return  the font number used.
     */
    public short getFontOfFormattingRun(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * The index within the string to which the specified formatting run applies.
     * @param index     the index of the formatting run
     * @return  the index within the string.
     */
    public int getIndexOfFormattingRun(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Returns the plain string representation.
     */
    public String getString() {
        if(st.sizeOfRArray() == 0) return st.getT();
        else {
            StringBuffer buf = new StringBuffer();
            for(CTRElt r : st.getRArray()){
                buf.append(r.getT());
            }
            return buf.toString();
        }
    }

    /**
     * Removes any formatting and sets new string value
     *
     * @param s new string value
     */
    public void setString(String s){
        clearFormatting();
        st.setT(s);
    }

    /**
     * Returns the plain string representation.
     */
    public String toString() {
        return getString();
    }

    /**
     * Returns the number of characters in this string.
     */
    public int length() {
        return getString().length();
    }

    /**
     * @return  The number of formatting runs used.
     */
    public int numFormattingRuns() {
        return st.sizeOfRArray();
    }

    /**
     * Return the underlying xml bean
     */
    public CTRst getCTRst() {
        return st;
    }

    protected void setStylesTableReference(StylesTable tbl){
        styles = tbl;
    }
}
