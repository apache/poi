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

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.namespace.QName;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STXstring;


/**
 * Rich text unicode string.  These strings can have fonts applied to arbitary parts of the string.
 *
 * <p>
 * Most strings in a workbook have formatting applied at the cell level, that is, the entire string in the cell has the
 * same formatting applied. In these cases, the formatting for the cell is stored in the styles part,
 * and the string for the cell can be shared across the workbook. The following code illustrates the example.
 * </p>
 *
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
 *
 * @author Yegor Kozlov
 */
public class XSSFRichTextString implements RichTextString {
    private static final Pattern utfPtrn = Pattern.compile("_x([0-9A-F]{4})_");

    private CTRst st;
    private StylesTable styles;

    /**
     * Create a rich text string
     */
    public XSSFRichTextString(String str) {
        st = CTRst.Factory.newInstance();
        st.setT(str);
        preserveSpaces(st.xgetT());
    }

    /**
     * Create empty rich text string and initialize it with empty string
     */
    public XSSFRichTextString() {
        st = CTRst.Factory.newInstance();
    }

    /**
     * Create a rich text string from the supplied XML bean
     */
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
        XSSFFont font;
        if(styles == null) {
            //style table is not set, remember fontIndex and set the run properties later,
            //when setStylesTableReference is called
            font = new XSSFFont();
            font.setFontName("#" + fontIndex);
        } else {
            font = styles.getFontAt(fontIndex);
        }
        applyFont(startIndex, endIndex, font);
    }

    /**
     * Applies a font to the specified characters of a string.
     *
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply to font to (exclusive)
     * @param font          The index of the font to use.
     */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public void applyFont(int startIndex, int endIndex, Font font) {
        if (startIndex > endIndex)
            throw new IllegalArgumentException("Start index must be less than end index.");
        if (startIndex < 0 || endIndex > length())
            throw new IllegalArgumentException("Start and end index not in range.");
        if (startIndex == endIndex)
            return;

        if(st.sizeOfRArray() == 0 && st.isSetT()) {
            //convert <t>string</t> into a text run: <r><t>string</t></r>
            st.addNewR().setT(st.getT());
            st.unsetT();
        }

        String text = getString();

        XSSFFont xssfFont = (XSSFFont)font;
        ArrayList<CTRElt> runs = new ArrayList<CTRElt>();

        CTRElt[] r = st.getRArray();
        int pos = 0;
        for (int i = 0; i < r.length; i++) {
            int rStart = pos;
            String t = r[i].getT();
            int rEnd = rStart + t.length();

            if(rEnd <= startIndex) {
                runs.add(r[i]);
                pos += r[i].getT().length();
            }
            else if (startIndex > rStart && startIndex < rEnd){
                CTRElt c = (CTRElt)r[i].copy();
                String txt = text.substring(rStart, startIndex);
                c.setT(txt);
                runs.add(c);
                pos += txt.length();
            } else {
                break;
            }
        }
        CTRElt rt = CTRElt.Factory.newInstance();
        String txt = text.substring(startIndex, endIndex);
        rt.setT(txt);
        CTRPrElt pr = rt.addNewRPr();
        setRunAttributes(xssfFont.getCTFont(), pr);
        runs.add(rt);
        pos += txt.length();

        for (int i = 0; i < r.length; i++) {
            int rStart = pos;
            String t = r[i].getT();
            int rEnd = Math.min(rStart + t.length(), text.length());

            if (endIndex < rEnd){
                CTRElt c = (CTRElt)r[i].copy();
                txt = text.substring(rStart, rEnd);
                c.setT(txt);
                runs.add(c);
                pos += txt.length();
                preserveSpaces(c.xgetT());
            }
        }


        st.setRArray(runs.toArray(new CTRElt[runs.size()]));
    }

    /**
     * Sets the font of the entire string.
     * @param font          The font to use.
     */
    public void applyFont(Font font) {
        if(st.sizeOfRArray() == 0 && st.isSetT()) {
            CTRElt r = st.addNewR();
            r.setT(st.getT());
            setRunAttributes(((XSSFFont)font).getCTFont(), r.addNewRPr());
            st.unsetT();
        } else {
            CTRElt r = CTRElt.Factory.newInstance();
            r.setT(getString());
            setRunAttributes(((XSSFFont)font).getCTFont(), r.addNewRPr());
            st.setRArray(new CTRElt[]{r});
        }
    }

    /**
     * Applies the specified font to the entire string.
     *
     * @param fontIndex  the font to apply.
     */
    public void applyFont(short fontIndex) {
        XSSFFont font;
        if(styles == null) {
            font = new XSSFFont();
            font.setFontName("#" + fontIndex);
        } else {
            font = styles.getFontAt(fontIndex);
        }
        applyFont(font);
    }

    /**
     * Append new text to this text run and apply the specify font to it
     *
     * @param text  the text to append
     * @param font  the font to apply to the appended text or <code>null</code> if no formatting is required
     */
    public void append(String text, XSSFFont font){
        if(st.sizeOfRArray() == 0 && st.isSetT()) {
            //convert <t>string</t> into a text run: <r><t>string</t></r>
            st.addNewR().setT(st.getT());
            st.unsetT();
        }
        CTRElt lt = st.addNewR();
        lt.setT(text);
        CTRPrElt pr = lt.addNewRPr();
        if(font != null) setRunAttributes(font.getCTFont(), pr);
    }

    /**
     * Append new text to this text run
     *
     * @param text  the text to append
     */
    public void append(String text){
        append(text, null);
    }

    /**
     * Copy font attributes from CTFont bean into CTRPrElt bean
     */
    private void setRunAttributes(CTFont ctFont, CTRPrElt pr){
        if(ctFont.sizeOfBArray() > 0) pr.addNewB().setVal(ctFont.getBArray(0).getVal());
        if(ctFont.sizeOfUArray() > 0) pr.addNewU().setVal(ctFont.getUArray(0).getVal());
        if(ctFont.sizeOfIArray() > 0) pr.addNewI().setVal(ctFont.getIArray(0).getVal());
        if(ctFont.sizeOfColorArray() > 0) {
            CTColor c1 = ctFont.getColorArray(0);
            CTColor c2 = pr.addNewColor();
            if(c1.isSetAuto()) c2.setAuto(c1.getAuto());
            if(c1.isSetIndexed()) c2.setIndexed(c1.getIndexed());
            if(c1.isSetRgb()) c2.setRgb(c1.getRgb());
            if(c1.isSetTheme()) c2.setTheme(c1.getTheme());
            if(c1.isSetTint()) c2.setTint(c1.getTint());
        }
        if(ctFont.sizeOfSzArray() > 0) pr.addNewSz().setVal(ctFont.getSzArray(0).getVal());
        if(ctFont.sizeOfNameArray() > 0) pr.addNewRFont().setVal(ctFont.getNameArray(0).getVal());
        if(ctFont.sizeOfFamilyArray() > 0) pr.addNewFamily().setVal(ctFont.getFamilyArray(0).getVal());
        if(ctFont.sizeOfSchemeArray() > 0) pr.addNewScheme().setVal(ctFont.getSchemeArray(0).getVal());
        if(ctFont.sizeOfCharsetArray() > 0) pr.addNewCharset().setVal(ctFont.getCharsetArray(0).getVal());
        if(ctFont.sizeOfCondenseArray() > 0) pr.addNewCondense().setVal(ctFont.getCondenseArray(0).getVal());
        if(ctFont.sizeOfExtendArray() > 0) pr.addNewExtend().setVal(ctFont.getExtendArray(0).getVal());
        if(ctFont.sizeOfVertAlignArray() > 0) pr.addNewVertAlign().setVal(ctFont.getVertAlignArray(0).getVal());
        if(ctFont.sizeOfOutlineArray() > 0) pr.addNewOutline().setVal(ctFont.getOutlineArray(0).getVal());
        if(ctFont.sizeOfShadowArray() > 0) pr.addNewShadow().setVal(ctFont.getShadowArray(0).getVal());
        if(ctFont.sizeOfStrikeArray() > 0) pr.addNewStrike().setVal(ctFont.getStrikeArray(0).getVal());
    }

    /**
     * Removes any formatting that may have been applied to the string.
     */
    public void clearFormatting() {
        String text = getString();
        while (st.sizeOfRArray() > 0) {
            st.removeR(st.sizeOfRArray()-1);
        }
        st.setT(text);
    }

    /**
     * The index within the string to which the specified formatting run applies.
     *
     * @param index     the index of the formatting run
     * @return  the index within the string.
     */
    public int getIndexOfFormattingRun(int index) {
        if(st.sizeOfRArray() == 0) return 0;

        int pos = 0;
        for(int i = 0; i < st.sizeOfRArray(); i++){
            CTRElt r = st.getRArray(i);
            if(i == index) return pos;

            pos += r.getT().length();
        }
        return -1;
    }

    /**
     * Returns the number of characters this format run covers.
     *
     * @param index     the index of the formatting run
     * @return  the number of characters this format run covers
     */
    public int getLengthOfFormattingRun(int index) {
        if(st.sizeOfRArray() == 0) return length();

        for(int i = 0; i < st.sizeOfRArray(); i++){
            CTRElt r = st.getRArray(i);
            if(i == index) return r.getT().length();
        }
        return -1;
    }

    /**
     * Returns the plain string representation.
     */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public String getString() {
        if(st.sizeOfRArray() == 0) {
            return utfDecode(st.getT());
        }
        StringBuffer buf = new StringBuffer();
        for(CTRElt r : st.getRArray()){
            buf.append(r.getT());
        }
        return utfDecode(buf.toString());
    }

    /**
     * Removes any formatting and sets new string value
     *
     * @param s new string value
     */
    public void setString(String s){
        clearFormatting();
        st.setT(s);
        preserveSpaces(st.xgetT());
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
     * Gets a copy of the font used in a particular formatting run.
     *
     * @param index     the index of the formatting run
     * @return  A copy of the  font used or null if no formatting is applied to the specified text run.
     */
    public XSSFFont getFontOfFormattingRun(int index) {
        if(st.sizeOfRArray() == 0) return null;

        for(int i = 0; i < st.sizeOfRArray(); i++){
            CTRElt r = st.getRArray(i);
            if(i == index) return new XSSFFont(toCTFont(r.getRPr()));
        }
        return null;
    }

    /**
     * Return a copy of the font in use at a particular index.
     *
     * @param index         The index.
     * @return              A copy of the  font that's currently being applied at that
     *                      index or null if no font is being applied or the
     *                      index is out of range.
     */
    public XSSFFont getFontAtIndex( int index ) {
        if(st.sizeOfRArray() == 0) return null;

        int pos = 0;
        for(int i = 0; i < st.sizeOfRArray(); i++){
            CTRElt r = st.getRArray(i);
            if(index >= pos && index < pos + r.getT().length()) return new XSSFFont(toCTFont(r.getRPr()));

            pos += r.getT().length();
        }
        return null;

    }

    /**
     * Return the underlying xml bean
     */
    @Internal
    public CTRst getCTRst() {
        return st;
    }

    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    protected void setStylesTableReference(StylesTable tbl){
        styles = tbl;
        if(st.sizeOfRArray() > 0) {
            for (CTRElt r : st.getRArray()) {
                CTRPrElt pr = r.getRPr();
                if(pr != null){
                    String fontName = pr.getRFontArray(0).getVal();
                    if(fontName.startsWith("#")){
                        int idx = Integer.parseInt(fontName.substring(1));
                        XSSFFont font = styles.getFontAt(idx);
                        pr.removeRFont(0);
                        setRunAttributes(font.getCTFont(), pr);
                    }
                }
            }
        }
    }

    /**
     *
     * CTRPrElt --> CTFont adapter
     */
    protected static CTFont toCTFont(CTRPrElt pr){
        CTFont ctFont =  CTFont.Factory.newInstance();

        if(pr.sizeOfBArray() > 0) ctFont.addNewB().setVal(pr.getBArray(0).getVal());
        if(pr.sizeOfUArray() > 0) ctFont.addNewU().setVal(pr.getUArray(0).getVal());
        if(pr.sizeOfIArray() > 0) ctFont.addNewI().setVal(pr.getIArray(0).getVal());
        if(pr.sizeOfColorArray() > 0) {
            CTColor c1 = pr.getColorArray(0);
            CTColor c2 = ctFont.addNewColor();
            if(c1.isSetAuto()) c2.setAuto(c1.getAuto());
            if(c1.isSetIndexed()) c2.setIndexed(c1.getIndexed());
            if(c1.isSetRgb()) c2.setRgb(c1.getRgb());
            if(c1.isSetTheme()) c2.setTheme(c1.getTheme());
            if(c1.isSetTint()) c2.setTint(c1.getTint());
        }
        if(pr.sizeOfSzArray() > 0) ctFont.addNewSz().setVal(pr.getSzArray(0).getVal());
        if(pr.sizeOfRFontArray() > 0) ctFont.addNewName().setVal(pr.getRFontArray(0).getVal());
        if(pr.sizeOfFamilyArray() > 0) ctFont.addNewFamily().setVal(pr.getFamilyArray(0).getVal());
        if(pr.sizeOfSchemeArray() > 0) ctFont.addNewScheme().setVal(pr.getSchemeArray(0).getVal());
        if(pr.sizeOfCharsetArray() > 0) ctFont.addNewCharset().setVal(pr.getCharsetArray(0).getVal());
        if(pr.sizeOfCondenseArray() > 0) ctFont.addNewCondense().setVal(pr.getCondenseArray(0).getVal());
        if(pr.sizeOfExtendArray() > 0) ctFont.addNewExtend().setVal(pr.getExtendArray(0).getVal());
        if(pr.sizeOfVertAlignArray() > 0) ctFont.addNewVertAlign().setVal(pr.getVertAlignArray(0).getVal());
        if(pr.sizeOfOutlineArray() > 0) ctFont.addNewOutline().setVal(pr.getOutlineArray(0).getVal());
        if(pr.sizeOfShadowArray() > 0) ctFont.addNewShadow().setVal(pr.getShadowArray(0).getVal());
        if(pr.sizeOfStrikeArray() > 0) ctFont.addNewStrike().setVal(pr.getStrikeArray(0).getVal());

        return ctFont;
    }

    /**
     * Add the xml:spaces="preserve" attribute if the string has leading or trailing white spaces
     *
     * @param xs    the string to check
     */
    protected static void preserveSpaces(STXstring xs) {
        String text = xs.getStringValue();
        if (text != null && (text.startsWith(" ") || text.endsWith(" "))) {
            XmlCursor c = xs.newCursor();
            c.toNextToken();
            c.insertAttributeWithValue(new QName("http://www.w3.org/XML/1998/namespace", "space"), "preserve");
            c.dispose();
        }
    }

    /**
     * For all characters which cannot be represented in XML as defined by the XML 1.0 specification,
     * the characters are escaped using the Unicode numerical character representation escape character
     * format _xHHHH_, where H represents a hexadecimal character in the character's value.
     * <p>
     * Example: The Unicode character 0D is invalid in an XML 1.0 document,
     * so it shall be escaped as <code>_x000D_</code>.
     * </p>
     * See section 3.18.9 in the OOXML spec.
     *
     * @param   value the string to decode
     * @return  the decoded string
     */
    static String utfDecode(String value){
        if(value == null) return null;
        
        StringBuffer buf = new StringBuffer();
        Matcher m = utfPtrn.matcher(value);
        int idx = 0;
        while(m.find()) {
            int pos = m.start();
            if( pos > idx) {
                buf.append(value.substring(idx, pos));
            }

            String code = m.group(1);
            int icode = Integer.decode("0x" + code);
            buf.append((char)icode);

            idx = m.end();
        }
        buf.append(value.substring(idx));
        return buf.toString();
    }
}
