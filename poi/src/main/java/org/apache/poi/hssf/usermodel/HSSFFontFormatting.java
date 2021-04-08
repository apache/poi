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

import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Color;

/**
 * High level representation for Font Formatting component
 * of Conditional Formatting settings
 */
@SuppressWarnings("unused")
public final class HSSFFontFormatting implements org.apache.poi.ss.usermodel.FontFormatting {

    private final FontFormatting fontFormatting;
    private final HSSFWorkbook workbook;

    HSSFFontFormatting(CFRuleBase cfRuleRecord, HSSFWorkbook workbook) {
        this.fontFormatting = cfRuleRecord.getFontFormatting();
        this.workbook = workbook;
    }

    FontFormatting getFontFormattingBlock() {
        return fontFormatting;
    }

    /**
     * get the type of super or subscript for the font
     *
     * @return super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    public short getEscapementType()
    {
        return fontFormatting.getEscapementType();
    }

    /**
     * @return font color index
     */
    public short getFontColorIndex()
    {
        return fontFormatting.getFontColorIndex();
    }

    public HSSFColor getFontColor() {
        return workbook.getCustomPalette().getColor(
                getFontColorIndex()
        );
    }

    public void setFontColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            fontFormatting.setFontColorIndex((short)0);
        } else {
            fontFormatting.setFontColorIndex(hcolor.getIndex());
        }
    }

    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20); or -1 if not modified
     */
    public int getFontHeight() {
        return fontFormatting.getFontHeight();
    }

    /**
     * get the font weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     *
     * @return bw - a number between 100-1000 for the fonts "boldness"
     */
    public short getFontWeight() {
        return fontFormatting.getFontWeight();
    }

    /**
     * @see org.apache.poi.hssf.record.cf.FontFormatting#getRawRecord()
     */
    byte[] getRawRecord() {
        return fontFormatting.getRawRecord();
    }

    /**
     * get the type of underlining for the font
     *
     * @return font underlining type
     *
     * @see org.apache.poi.ss.usermodel.Font#U_NONE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE_ACCOUNTING
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE_ACCOUNTING
     */
    public short getUnderlineType()
    {
        return fontFormatting.getUnderlineType();
    }

    /**
     * get whether the font weight is set to bold or not
     *
     * @return bold - whether the font is bold or not
     */
    public boolean isBold()
    {
        return fontFormatting.isFontWeightModified() && fontFormatting.isBold();
    }

    /**
     * @return true if escapement type was modified from default
     */
    public boolean isEscapementTypeModified()
    {
        return fontFormatting.isEscapementTypeModified();
    }

    /**
     * @return true if font cancellation was modified from default
     */
    public boolean isFontCancellationModified()
    {
        return fontFormatting.isFontCancellationModified();
    }

    /**
     * @return true if font outline type was modified from default
     */
    public boolean isFontOutlineModified()
    {
        return fontFormatting.isFontOutlineModified();
    }

    /**
     * @return true if font shadow type was modified from default
     */
    public boolean isFontShadowModified()
    {
        return fontFormatting.isFontShadowModified();
    }

    /**
     * @return true if font style was modified from default
     */
    public boolean isFontStyleModified()
    {
        return fontFormatting.isFontStyleModified();
    }

    /**
     * @return true if font style was set to <i>italic</i>
     */
    public boolean isItalic()
    {
        return fontFormatting.isFontStyleModified() && fontFormatting.isItalic();
    }

    /**
     * @return true if font outline is on
     */
    public boolean isOutlineOn()
    {
        return fontFormatting.isFontOutlineModified() && fontFormatting.isOutlineOn();
    }

    /**
     * @return true if font shadow is on
     */
    public boolean isShadowOn()
    {
        return fontFormatting.isFontOutlineModified() && fontFormatting.isShadowOn();
    }

    /**
     * @return true if font strikeout is on
     */
    public boolean isStruckout()
    {
        return fontFormatting.isFontCancellationModified() && fontFormatting.isStruckout();
    }

    /**
     * @return true if font underline type was modified from default
     */
    public boolean isUnderlineTypeModified()
    {
        return fontFormatting.isUnderlineTypeModified();
    }

    /**
     * @return true if font weight was modified from default
     */
    public boolean isFontWeightModified()
    {
        return fontFormatting.isFontWeightModified();
    }

    /**
     * set font style options.
     *
     * @param italic - if true, set posture style to italic, otherwise to normal
     * @param bold if true, set font weight to bold, otherwise to normal
     */

    public void setFontStyle(boolean italic, boolean bold)
    {
        boolean modified = italic || bold;
        fontFormatting.setItalic(italic);
        fontFormatting.setBold(bold);
        fontFormatting.setFontStyleModified(modified);
        fontFormatting.setFontWieghtModified(modified);
    }

    /**
     * set font style options to default values (non-italic, non-bold)
     */
    public void resetFontStyle()
    {
        setFontStyle(false,false);
    }

    /**
     * set the escapement type for the font
     *
     * @param escapementType  super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    public void setEscapementType(short escapementType) {
        switch(escapementType) {
            case org.apache.poi.ss.usermodel.Font.SS_SUB:
            case org.apache.poi.ss.usermodel.Font.SS_SUPER:
                fontFormatting.setEscapementType(escapementType);
                fontFormatting.setEscapementTypeModified(true);
                break;
            case org.apache.poi.ss.usermodel.Font.SS_NONE:
                fontFormatting.setEscapementType(escapementType);
                fontFormatting.setEscapementTypeModified(false);
                break;
            default:
        }
    }

    /**
     * @param modified flag, if escapement type is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setEscapementTypeModified(boolean)
     */
    public void setEscapementTypeModified(boolean modified) {
        fontFormatting.setEscapementTypeModified(modified);
    }

    /**
     * @param modified flag, if font cancellation is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontCancellationModified(boolean)
     */
    public void setFontCancellationModified(boolean modified)
    {
        fontFormatting.setFontCancellationModified(modified);
    }

    /**
     * @param fci the font color index
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontColorIndex(short)
     */
    public void setFontColorIndex(short fci)
    {
        fontFormatting.setFontColorIndex(fci);
    }

    /**
     * @param height the font height
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontHeight(int)
     */
    public void setFontHeight(int height)
    {
        fontFormatting.setFontHeight(height);
    }

    /**
     * @param modified flag, if font outline is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontOutlineModified(boolean)
     */
    public void setFontOutlineModified(boolean modified)
    {
        fontFormatting.setFontOutlineModified(modified);
    }

    /**
     * @param modified flag, if font shadow is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontShadowModified(boolean)
     */
    public void setFontShadowModified(boolean modified)
    {
        fontFormatting.setFontShadowModified(modified);
    }

    /**
     * @param modified flag, if font style is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setFontStyleModified(boolean)
     */
    public void setFontStyleModified(boolean modified)
    {
        fontFormatting.setFontStyleModified(modified);
    }

    /**
     * @param on flag, if outline is set
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setOutline(boolean)
     */
    public void setOutline(boolean on)
    {
        fontFormatting.setOutline(on);
        fontFormatting.setFontOutlineModified(on);
    }

    /**
     * @param on flag, if shadow is set
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setShadow(boolean)
     */
    public void setShadow(boolean on)
    {
        fontFormatting.setShadow(on);
        fontFormatting.setFontShadowModified(on);
    }

    /**
     * @param strike flag, if strikeout is set
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setStrikeout(boolean)
     */
    public void setStrikeout(boolean strike)
    {
        fontFormatting.setStrikeout(strike);
        fontFormatting.setFontCancellationModified(strike);
    }

    /**
     * set the type of underlining type for the font
     *
     * @param underlineType  super or subscript option
     *
     * @see org.apache.poi.ss.usermodel.Font#U_NONE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE_ACCOUNTING
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE_ACCOUNTING
     */
    public void setUnderlineType(short underlineType) {
        switch(underlineType) {
            case org.apache.poi.ss.usermodel.Font.U_SINGLE:
            case org.apache.poi.ss.usermodel.Font.U_DOUBLE:
            case org.apache.poi.ss.usermodel.Font.U_SINGLE_ACCOUNTING:
            case org.apache.poi.ss.usermodel.Font.U_DOUBLE_ACCOUNTING:
                fontFormatting.setUnderlineType(underlineType);
                setUnderlineTypeModified(true);
                break;

            case org.apache.poi.ss.usermodel.Font.U_NONE:
                fontFormatting.setUnderlineType(underlineType);
                setUnderlineTypeModified(false);
                break;
            default:
        }
    }

    /**
     * @param modified flag, if underline type is modified
     * @see org.apache.poi.hssf.record.cf.FontFormatting#setUnderlineTypeModified(boolean)
     */
    public void setUnderlineTypeModified(boolean modified) {
        fontFormatting.setUnderlineTypeModified(modified);
    }
}
