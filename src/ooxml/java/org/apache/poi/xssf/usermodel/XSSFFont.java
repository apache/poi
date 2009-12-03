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

import org.apache.poi.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontCharset;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.FontScheme;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIntProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTUnderlineProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTVerticalAlignFontProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignRun;

/**
 * Represents a font used in a workbook.
 *
 * @author Gisella Bronzetti
 */
public class XSSFFont implements Font {

    /**
     * By default, Microsoft Office Excel 2007 uses the Calibry font in font size 11
     */
    public static final String DEFAULT_FONT_NAME = "Calibri";
    /**
     * By default, Microsoft Office Excel 2007 uses the Calibry font in font size 11
     */
    public static final short DEFAULT_FONT_SIZE = 11;
    /**
     * Default font color is black
     * @see org.apache.poi.ss.usermodel.IndexedColors#BLACK
     */
    public static final short DEFAULT_FONT_COLOR = IndexedColors.BLACK.getIndex();

    private CTFont _ctFont;
    private short _index;

    /**
     * Create a new XSSFFont
     *
     * @param font the underlying CTFont bean
     */
    public XSSFFont(CTFont font) {
        _ctFont = font;
        _index = 0;
    }

    public XSSFFont(CTFont font, int index) {
        _ctFont = font;
        _index = (short)index;
    }

    /**
     * Create a new XSSFont. This method is protected to be used only by XSSFWorkbook
     */
    protected XSSFFont() {
        this._ctFont = CTFont.Factory.newInstance();
        setFontName(DEFAULT_FONT_NAME);
        setFontHeight((double)DEFAULT_FONT_SIZE);
    }

    /**
     * get the underlying CTFont font
     */
    @Internal
    public CTFont getCTFont() {
        return _ctFont;
    }

    /**
     * get a boolean value for the boldness to use.
     *
     * @return boolean - bold
     */
    public boolean getBold() {
        CTBooleanProperty bold = _ctFont.sizeOfBArray() == 0 ? null : _ctFont.getBArray(0);
        return (bold != null && bold.getVal());
    }

    /**
     * get character-set to use.
     *
     * @return byte - character-set
     * @see org.apache.poi.ss.usermodel.FontCharset
     */
    public byte getCharSet() {
        CTIntProperty charset = _ctFont.sizeOfCharsetArray() == 0 ? null : _ctFont.getCharsetArray(0);
        int val = charset == null ? FontCharset.ANSI.getValue() : FontCharset.valueOf(charset.getVal()).getValue();
        return (byte)val;
    }


    /**
     * get the indexed color value for the font
     * References a color defined in IndexedColors.
     *
     * @return short - indexed color to use
     * @see IndexedColors
     */
    public short getColor() {
        CTColor color = _ctFont.sizeOfColorArray() == 0 ? null : _ctFont.getColorArray(0);
        if (color == null) return IndexedColors.BLACK.getIndex();

        long index = color.getIndexed();
        if (index == XSSFFont.DEFAULT_FONT_COLOR) {
            return IndexedColors.BLACK.getIndex();
        } else if (index == IndexedColors.RED.getIndex()) {
            return IndexedColors.RED.getIndex();
        } else {
            return (short)index;
        }
    }


    /**
     * get the color value for the font
     * References a color defined as  Standard Alpha Red Green Blue color value (ARGB).
     *
     * @return XSSFColor - rgb color to use
     */
    public XSSFColor getXSSFColor() {
        CTColor ctColor = _ctFont.sizeOfColorArray() == 0 ? null : _ctFont.getColorArray(0);
        return ctColor == null ? null : new XSSFColor(ctColor);
    }


    /**
     * get the color value for the font
     * References a color defined in theme.
     *
     * @return short - theme defined to use
     */
    public short getThemeColor() {
        CTColor color = _ctFont.sizeOfColorArray() == 0 ? null : _ctFont.getColorArray(0);
        long index = color == null ? 0 : color.getTheme();
        return (short) index;
    }

    /**
     * get the font height in point.
     *
     * @return short - height in point
     */
    public short getFontHeight() {
        CTFontSize size = _ctFont.sizeOfSzArray() == 0 ? null : _ctFont.getSzArray(0);
        if (size != null) {
            double fontHeight = size.getVal();
            return (short)(fontHeight*20);
        }
        return (short)(DEFAULT_FONT_SIZE*20);
    }

    /**
     * @see #getFontHeight()
     */
    public short getFontHeightInPoints() {
        return (short)(getFontHeight()/20);
    }

    /**
     * get the name of the font (i.e. Arial)
     *
     * @return String - a string representing the name of the font to use
     */
    public String getFontName() {
        CTFontName name = _ctFont.sizeOfNameArray() == 0 ? null : _ctFont.getNameArray(0);
        return name == null ? DEFAULT_FONT_NAME : name.getVal();
    }

    /**
     * get a boolean value that specify whether to use italics or not
     *
     * @return boolean - value for italic
     */
    public boolean getItalic() {
        CTBooleanProperty italic = _ctFont.sizeOfIArray() == 0 ? null : _ctFont.getIArray(0);
        return italic != null && italic.getVal();
    }

    /**
     * get a boolean value that specify whether to use a strikeout horizontal line through the text or not
     *
     * @return boolean - value for strikeout
     */
    public boolean getStrikeout() {
        CTBooleanProperty strike = _ctFont.sizeOfStrikeArray() == 0 ? null : _ctFont.getStrikeArray(0);
        return strike != null && strike.getVal();
    }

    /**
     * get normal,super or subscript.
     *
     * @return short - offset type to use (none,super,sub)
     * @see Font#SS_NONE
     * @see Font#SS_SUPER
     * @see Font#SS_SUB
     */
    public short getTypeOffset() {
        CTVerticalAlignFontProperty vAlign = _ctFont.sizeOfVertAlignArray() == 0 ? null : _ctFont.getVertAlignArray(0);
        if (vAlign == null) {
            return Font.SS_NONE;
        }
        int val = vAlign.getVal().intValue();
        switch (val) {
            case STVerticalAlignRun.INT_BASELINE:
                return Font.SS_NONE;
            case STVerticalAlignRun.INT_SUBSCRIPT:
                return Font.SS_SUB;
            case STVerticalAlignRun.INT_SUPERSCRIPT:
                return Font.SS_SUPER;
            default:
                throw new POIXMLException("Wrong offset value " + val);
        }
    }

    /**
     * get type of text underlining to use
     *
     * @return byte - underlining type
     * @see org.apache.poi.ss.usermodel.FontUnderline
     */
    public byte getUnderline() {
        CTUnderlineProperty underline = _ctFont.sizeOfUArray() == 0 ? null : _ctFont.getUArray(0);
        if (underline != null) {
            FontUnderline val = FontUnderline.valueOf(underline.getVal().intValue());
            return val.getByteValue();
        }
        return Font.U_NONE;
    }

    /**
     * set a boolean value for the boldness to use. If omitted, the default value is true.
     *
     * @param bold - boldness to use
     */
    public void setBold(boolean bold) {
        if(bold){
            CTBooleanProperty ctBold = _ctFont.sizeOfBArray() == 0 ? _ctFont.addNewB() : _ctFont.getBArray(0);
            ctBold.setVal(bold);
        } else {
            _ctFont.setBArray(null);
        }
    }

    public void setBoldweight(short boldweight)
    {
        setBold(boldweight == BOLDWEIGHT_BOLD);
    }

    /**
     * get the boldness to use
     * @return boldweight
     * @see #BOLDWEIGHT_NORMAL
     * @see #BOLDWEIGHT_BOLD
     */

    public short getBoldweight()
    {
        return getBold() ? BOLDWEIGHT_BOLD : BOLDWEIGHT_NORMAL;
    }

    /**
     * set character-set to use.
     *
     * @param charset - charset
     * @see FontCharset
     */
    public void setCharSet(byte charset) {
        CTIntProperty charsetProperty = _ctFont.sizeOfCharsetArray() == 0 ? _ctFont.addNewCharset() : _ctFont.getCharsetArray(0);
        switch (charset) {
            case Font.ANSI_CHARSET:
                charsetProperty.setVal(FontCharset.ANSI.getValue());
                break;
            case Font.SYMBOL_CHARSET:
                charsetProperty.setVal(FontCharset.SYMBOL.getValue());
                break;
            case Font.DEFAULT_CHARSET:
                charsetProperty.setVal(FontCharset.DEFAULT.getValue());
                break;
            default:
                throw new POIXMLException("Attention: an attempt to set a type of unknow charset and charset");
        }
    }

    /**
     * set character-set to use.
     *
     * @param charSet
     */
    public void setCharSet(FontCharset charSet) {
        setCharSet((byte)charSet.getValue());
    }

    /**
     * set the indexed color for the font
     *
     * @param color - color to use
     * @see #DEFAULT_FONT_COLOR - Note: default font color
     * @see IndexedColors
     */
    public void setColor(short color) {
        CTColor ctColor = _ctFont.sizeOfColorArray() == 0 ? _ctFont.addNewColor() : _ctFont.getColorArray(0);
        switch (color) {
            case Font.COLOR_NORMAL: {
                ctColor.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
                break;
            }
            case Font.COLOR_RED: {
                ctColor.setIndexed(IndexedColors.RED.getIndex());
                break;
            }
            default:
                ctColor.setIndexed(color);
        }
    }

    /**
     * set the color for the font in Standard Alpha Red Green Blue color value
     *
     * @param color - color to use
     */
    public void setColor(XSSFColor color) {
        if(color == null) _ctFont.setColorArray(null);
        else {
            CTColor ctColor = _ctFont.sizeOfColorArray() == 0 ? _ctFont.addNewColor() : _ctFont.getColorArray(0);
            ctColor.setRgb(color.getRgb());
        }
    }

    /**
     * set the font height in points.
     *
     * @param height - height in points
     */
    public void setFontHeight(short height) {
        setFontHeight((double) height/20);
    }

    /**
     * set the font height in points.
     *
     * @param height - height in points
     */
    public void setFontHeight(double height) {
        CTFontSize fontSize = _ctFont.sizeOfSzArray() == 0 ? _ctFont.addNewSz() : _ctFont.getSzArray(0);
        fontSize.setVal(height);
    }

    /**
     * set the font height in points.
     *
     * @link #setFontHeight
     */
    public void setFontHeightInPoints(short height) {
        setFontHeight((double)height);
    }

    /**
     * set the theme color for the font to use
     *
     * @param theme - theme color to use
     */
    public void setThemeColor(short theme) {
        CTColor ctColor = _ctFont.sizeOfColorArray() == 0 ? _ctFont.addNewColor() : _ctFont.getColorArray(0);
        ctColor.setTheme(theme);
    }

    /**
     * set the name for the font (i.e. Arial).
     * If the font doesn't exist (because it isn't installed on the system),
     * or the charset is invalid for that font, then another font should
     * be substituted.
     * The string length for this attribute shall be 0 to 31 characters.
     * Default font name is Calibri.
     *
     * @param name - value representing the name of the font to use
     * @see #DEFAULT_FONT_NAME
     */
    public void setFontName(String name) {
        CTFontName fontName = _ctFont.sizeOfNameArray() == 0 ? _ctFont.addNewName() : _ctFont.getNameArray(0);
        fontName.setVal(name == null ? DEFAULT_FONT_NAME : name);
    }


    /**
     * set a boolean value for the property specifying whether to use italics or not
     * If omitted, the default value is true.
     *
     * @param italic - value for italics or not
     */
    public void setItalic(boolean italic) {
        if(italic){
            CTBooleanProperty bool = _ctFont.sizeOfIArray() == 0 ? _ctFont.addNewI() : _ctFont.getIArray(0);
            bool.setVal(italic);
        } else {
            _ctFont.setIArray(null);
        }
    }


    /**
     * set a boolean value for the property specifying whether to use a strikeout horizontal line through the text or not
     * If omitted, the default value is true.
     *
     * @param strikeout - value for strikeout or not
     */
    public void setStrikeout(boolean strikeout) {
        if(!strikeout) _ctFont.setStrikeArray(null);
        else {
            CTBooleanProperty strike = _ctFont.sizeOfStrikeArray() == 0 ? _ctFont.addNewStrike() : _ctFont.getStrikeArray(0);
            strike.setVal(strikeout);
        }
    }

    /**
     * set normal,super or subscript, that representing the vertical-alignment setting.
     * Setting this to either subscript or superscript shall make the font size smaller if a
     * smaller font size is available.
     *
     * @param offset - offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */
    public void setTypeOffset(short offset) {
        if(offset == Font.SS_NONE){
            _ctFont.setVertAlignArray(null);
        } else {
            CTVerticalAlignFontProperty offsetProperty = _ctFont.sizeOfVertAlignArray() == 0 ? _ctFont.addNewVertAlign() : _ctFont.getVertAlignArray(0);
            switch (offset) {
                case Font.SS_NONE:
                    offsetProperty.setVal(STVerticalAlignRun.BASELINE);
                    break;
                case Font.SS_SUB:
                    offsetProperty.setVal(STVerticalAlignRun.SUBSCRIPT);
                    break;
                case Font.SS_SUPER:
                    offsetProperty.setVal(STVerticalAlignRun.SUPERSCRIPT);
                    break;
            }
        }
    }

    /**
     * set the style of underlining that is used.
     * The none style is equivalent to not using underlining at all.
     *
     * @param underline - underline type to use
     * @see FontUnderline
     */
    public void setUnderline(byte underline) {
        setUnderline(FontUnderline.valueOf(underline));
    }

    /**
     * set an enumeration representing the style of underlining that is used.
     * The none style is equivalent to not using underlining at all.
     * The possible values for this attribute are defined by the FontUnderline
     *
     * @param underline - FontUnderline enum value
     */
    public void setUnderline(FontUnderline underline) {
        if(underline == FontUnderline.NONE && _ctFont.sizeOfUArray() > 0){
            _ctFont.setUArray(null);
        } else {
            CTUnderlineProperty ctUnderline = _ctFont.sizeOfUArray() == 0 ? _ctFont.addNewU() : _ctFont.getUArray(0);
            STUnderlineValues.Enum val = STUnderlineValues.Enum.forInt(underline.getValue());
            ctUnderline.setVal(val);
        }
    }


    public String toString() {
        return _ctFont.toString();
    }


    /**
     * Register ourselfs in the style table
     */
    public long putFont(StylesTable styles) {
        short idx = (short)styles.putFont(this);
        this._index = idx;
        return idx;
    }

    /**
     * get the font scheme property.
     * is used only in StylesTable to create the default instance of font
     *
     * @return FontScheme
     * @see org.apache.poi.xssf.model.StylesTable#createDefaultFont()
     */
    public FontScheme getScheme() {
        CTFontScheme scheme = _ctFont.sizeOfSchemeArray() == 0 ? null : _ctFont.getSchemeArray(0);
        return scheme == null ? FontScheme.NONE : FontScheme.valueOf(scheme.getVal().intValue());
    }

    /**
     * set font scheme property
     *
     * @param scheme - FontScheme enum value
     * @see FontScheme
     */
    public void setScheme(FontScheme scheme) {
        CTFontScheme ctFontScheme = _ctFont.sizeOfSchemeArray() == 0 ? _ctFont.addNewScheme() : _ctFont.getSchemeArray(0);
        STFontScheme.Enum val = STFontScheme.Enum.forInt(scheme.getValue());
        ctFontScheme.setVal(val);
    }

    /**
     * get the font family to use.
     *
     * @return the font family to use
     * @see org.apache.poi.ss.usermodel.FontFamily
     */
    public int getFamily() {
        CTIntProperty family = _ctFont.sizeOfFamilyArray() == 0 ? _ctFont.addNewFamily() : _ctFont.getFamilyArray(0);
        return family == null ? FontFamily.NOT_APPLICABLE.getValue() : FontFamily.valueOf(family.getVal()).getValue();
    }

    /**
     * Set the font family this font belongs to.
     * A font family is a set of fonts having common stroke width and serif characteristics.
     * The font name overrides when there are conflicting values.
     *
     * @param value - font family
     * @see FontFamily
     */
    public void setFamily(int value) {
        CTIntProperty family = _ctFont.sizeOfFamilyArray() == 0 ? _ctFont.addNewFamily() : _ctFont.getFamilyArray(0);
        family.setVal(value);
    }

    /**
     * set an enumeration representing the font family this font belongs to.
     * A font family is a set of fonts having common stroke width and serif characteristics.
     *
     * @param family font family
     * @link #setFamily(int value)
     */
    public void setFamily(FontFamily family) {
        setFamily(family.getValue());
    }

    /**
     * get the index within the XSSFWorkbook (sequence within the collection of Font objects)
     * @return unique index number of the underlying record this Font represents (probably you don't care
     *  unless you're comparing which one is which)
     */

    public short getIndex()
    {
        return _index;
    }

    public int hashCode(){
        return _ctFont.toString().hashCode();
    }

    public boolean equals(Object o){
        if(!(o instanceof XSSFFont)) return false;

        XSSFFont cf = (XSSFFont)o;
        return _ctFont.toString().equals(cf.getCTFont().toString());
    }

}
