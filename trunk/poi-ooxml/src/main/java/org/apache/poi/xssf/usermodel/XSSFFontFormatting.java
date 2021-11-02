/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STVerticalAlignRun;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTUnderlineProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTVerticalAlignFontProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;


public class XSSFFontFormatting implements FontFormatting {
    private IndexedColorMap _colorMap;
    private CTFont _font;

    /*package*/ XSSFFontFormatting(CTFont font, IndexedColorMap colorMap) {
        _font = font;
        _colorMap = colorMap;
    }

    /**
     * get the type of super or subscript for the font
     *
     * @return super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    @Override
    public short getEscapementType(){
        if(_font.sizeOfVertAlignArray() == 0) return org.apache.poi.ss.usermodel.Font.SS_NONE;

        CTVerticalAlignFontProperty prop = _font.getVertAlignArray(0);
        return (short)(prop.getVal().intValue() - 1);
   }

    /**
     * set the escapement type for the font
     *
     * @param escapementType  super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    @Override
    public void setEscapementType(short escapementType){
        _font.setVertAlignArray(null);
        if(escapementType != org.apache.poi.ss.usermodel.Font.SS_NONE){
            _font.addNewVertAlign().setVal(STVerticalAlignRun.Enum.forInt(escapementType + 1));
        }
    }

    /**
     * XMLBeans and the XSD make this look like it can have multiple values, but it is maxOccurrs=1.
     * Use get*Array(), it is much faster than get*List().
     *
     * @see org.apache.poi.ss.usermodel.FontFormatting#isStruckout()
     */
    @Override
    public boolean isStruckout() {
        return _font.sizeOfStrikeArray() > 0 && _font.getStrikeArray(0).getVal();
    }

    /**
     * @return font color index
     */
    @Override
    public short getFontColorIndex(){
        if(_font.sizeOfColorArray() == 0) return -1;

        int idx = 0;
        CTColor color = _font.getColorArray(0);
        if(color.isSetIndexed()) idx = (int)color.getIndexed();
        return (short)idx;
    }

    /**
     * @param color font color index
     */
    @Override
    public void setFontColorIndex(short color){
        _font.setColorArray(null);
        if(color != -1){
            _font.addNewColor().setIndexed(color);
        }
    }

    @Override
    public XSSFColor getFontColor() {
        if(_font.sizeOfColorArray() == 0) return null;

        return XSSFColor.from(_font.getColorArray(0), _colorMap);
    }

    @Override
    public void setFontColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) {
            _font.getColorList().clear();
        } else if(_font.sizeOfColorArray() == 0) {
            _font.addNewColor().setRgb(xcolor.getRGB());
        } else {
            _font.setColorArray(0, xcolor.getCTColor());
        }
    }

    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20); or -1 if not modified
     */
    @Override
    public int getFontHeight(){
        if(_font.sizeOfSzArray() == 0) return -1;

        CTFontSize sz = _font.getSzArray(0);
        return (int)(20*sz.getVal());
    }

    /**
     * Sets the height of the font in 1/20th point units
     *
     * @param height the height in twips (in points/20)
     */
    @Override
    public void setFontHeight(int height){
        _font.setSzArray(null);
        if(height != -1){
            _font.addNewSz().setVal((double)height / 20);
        }
    }

    /**
     * get the type of underlining for the font
     *
     * @return font underlining type
     *
     * @see Font#U_NONE
     * @see Font#U_SINGLE
     * @see Font#U_DOUBLE
     * @see Font#U_SINGLE_ACCOUNTING
     * @see Font#U_DOUBLE_ACCOUNTING
     */
    @Override
    public short getUnderlineType(){
        if(_font.sizeOfUArray() == 0) return Font.U_NONE;
        CTUnderlineProperty u = _font.getUArray(0);
        switch(u.getVal().intValue()){
            case STUnderlineValues.INT_SINGLE: return Font.U_SINGLE;
            case STUnderlineValues.INT_DOUBLE: return Font.U_DOUBLE;
            case STUnderlineValues.INT_SINGLE_ACCOUNTING: return Font.U_SINGLE_ACCOUNTING;
            case STUnderlineValues.INT_DOUBLE_ACCOUNTING: return Font.U_DOUBLE_ACCOUNTING;
            default: return Font.U_NONE;
        }
    }

    /**
     * set the type of underlining type for the font
     *
     * @param underlineType  super or subscript option
     *
     * @see Font#U_NONE
     * @see Font#U_SINGLE
     * @see Font#U_DOUBLE
     * @see Font#U_SINGLE_ACCOUNTING
     * @see Font#U_DOUBLE_ACCOUNTING
     */
    @Override
    public void setUnderlineType(short underlineType){
        _font.setUArray(null);
        if(underlineType != Font.U_NONE){
            FontUnderline fenum = FontUnderline.valueOf(underlineType);
            STUnderlineValues.Enum val = STUnderlineValues.Enum.forInt(fenum.getValue());
            _font.addNewU().setVal(val);
        }
     }

    /**
     * get whether the font weight is set to bold or not
     *
     * @return bold - whether the font is bold or not
     */
    @Override
    public boolean isBold(){
        return _font.sizeOfBArray() == 1 && _font.getBArray(0).getVal();
    }

    /**
     * @return true if font style was set to <i>italic</i>
     */
    @Override
    public boolean isItalic(){
        return _font.sizeOfIArray() == 1 && _font.getIArray(0).getVal();
    }

    /**
     * set font style options.
     *
     * @param italic - if true, set posture style to italic, otherwise to normal
     * @param bold if true, set font weight to bold, otherwise to normal
     */
    @Override
    public void setFontStyle(boolean italic, boolean bold){
        _font.setIArray(null);
        _font.setBArray(null);
        if(italic) _font.addNewI().setVal(true);
        if(bold) _font.addNewB().setVal(true);
    }

    /**
     * set font style options to default values (non-italic, non-bold)
     */
    @Override
    public void resetFontStyle(){
        _font.set(CTFont.Factory.newInstance());
    }
}
