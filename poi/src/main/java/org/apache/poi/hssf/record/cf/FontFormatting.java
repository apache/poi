
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

package org.apache.poi.hssf.record.cf;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;
import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.LittleEndian;

/**
 * Font Formatting Block of the Conditional Formatting Rule Record.
 */
public final class FontFormatting implements Duplicatable, GenericRecord {
    private static final int OFFSET_FONT_NAME = 0;
    private static final int OFFSET_FONT_HEIGHT = 64;
    private static final int OFFSET_FONT_OPTIONS = 68;
    private static final int OFFSET_FONT_WEIGHT = 72;
    private static final int OFFSET_ESCAPEMENT_TYPE = 74;
    private static final int OFFSET_UNDERLINE_TYPE = 76;
    private static final int OFFSET_FONT_COLOR_INDEX = 80;
    private static final int OFFSET_OPTION_FLAGS = 88;
    private static final int OFFSET_ESCAPEMENT_TYPE_MODIFIED = 92;
    private static final int OFFSET_UNDERLINE_TYPE_MODIFIED = 96;
    private static final int OFFSET_FONT_WEIGHT_MODIFIED = 100;
    private static final int OFFSET_NOT_USED1 = 104;
    private static final int OFFSET_NOT_USED2 = 108;
    private static final int OFFSET_NOT_USED3 = 112; // for some reason Excel always writes  0x7FFFFFFF at this offset
    private static final int OFFSET_FONT_FORMATING_END = 116;
    private static final int RAW_DATA_SIZE = 118;


    public static final int  FONT_CELL_HEIGHT_PRESERVED   = 0xFFFFFFFF;

    // option flags and font options masks
    // in the options flags, a true bit activates the overriding and in the font option the bit sets the state
    private static final BitField POSTURE = BitFieldFactory.getInstance(0x00000002);
    private static final BitField OUTLINE = BitFieldFactory.getInstance(0x00000008);
    private static final BitField SHADOW = BitFieldFactory.getInstance(0x00000010);
    private static final BitField CANCELLATION = BitFieldFactory.getInstance(0x00000080);

    /** Normal boldness (not bold) */
    private static final short FONT_WEIGHT_NORMAL = 0x190;

    /**
     * Bold boldness (bold)
     */
    private static final short FONT_WEIGHT_BOLD	 = 0x2bc;

    private final byte[] _rawData = new byte[RAW_DATA_SIZE];

    public FontFormatting() {
        setFontHeight(-1);
        setItalic(false);
        setFontWieghtModified(false);
        setOutline(false);
        setShadow(false);
        setStrikeout(false);
        setEscapementType((short)0);
        setUnderlineType((byte)0);
        setFontColorIndex((short)-1);

        setFontStyleModified(false);
        setFontOutlineModified(false);
        setFontShadowModified(false);
        setFontCancellationModified(false);

        setEscapementTypeModified(false);
        setUnderlineTypeModified(false);

        setShort(OFFSET_FONT_NAME, 0);
        setInt(OFFSET_NOT_USED1, 0x00000001);
        setInt(OFFSET_NOT_USED2, 0x00000000);
        setInt(OFFSET_NOT_USED3, 0x7FFFFFFF);// for some reason Excel always writes  0x7FFFFFFF at this offset
        setShort(OFFSET_FONT_FORMATING_END, 0x0001);
    }

    public FontFormatting(FontFormatting other) {
        System.arraycopy(other._rawData, 0, _rawData, 0, RAW_DATA_SIZE);
    }

    public FontFormatting(RecordInputStream in) {
        in.readFully(_rawData);
    }

    private short getShort(int offset) {
        return LittleEndian.getShort( _rawData, offset);
    }
    private void setShort(int offset, int value) {
        LittleEndian.putShort( _rawData, offset, (short)value);
    }
    private int getInt(int offset) {
        return LittleEndian.getInt( _rawData, offset);
    }
    private void setInt(int offset, int value) {
        LittleEndian.putInt( _rawData, offset, value);
    }

    public byte[] getRawRecord()
    {
        return _rawData;
    }

    public int getDataLength() {
        return RAW_DATA_SIZE;
    }

    /**
     * sets the height of the font in 1/20th point units
     *
     *
     * @param height  fontheight (in points/20); or -1 to preserve the cell font height
     */

    public void setFontHeight(int height) {
        setInt(OFFSET_FONT_HEIGHT, height);
    }

    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20); or -1 if not modified
     */
    public int getFontHeight() {
        return getInt(OFFSET_FONT_HEIGHT);
    }

    private void setFontOption(boolean option, BitField field) {
        int options = getInt(OFFSET_FONT_OPTIONS);
        options = field.setBoolean(options, option);
        setInt(OFFSET_FONT_OPTIONS, options);
    }

    private boolean getFontOption(BitField field) {
        int options = getInt(OFFSET_FONT_OPTIONS);
        return field.isSet(options);
    }

    /**
     * set the font to be italics or not
     *
     * @param italic - whether the font is italics or not
     * @see #setFontOption(boolean, org.apache.poi.util.BitField)
     */

    public void setItalic(boolean italic) {
        setFontOption(italic, POSTURE);
    }

    /**
     * get whether the font is to be italics or not
     *
     * @return italics - whether the font is italics or not
     * @see #getFontOption(org.apache.poi.util.BitField)
     */

    public boolean isItalic() {
        return getFontOption(POSTURE);
    }

    public void setOutline(boolean on) {
        setFontOption(on, OUTLINE);
    }

    public boolean isOutlineOn() {
        return getFontOption(OUTLINE);
    }

    public void setShadow(boolean on) {
        setFontOption(on, SHADOW);
    }

    public boolean isShadowOn() {
        return getFontOption(SHADOW);
    }

    /**
     * set the font to be stricken out or not
     *
     * @param strike - whether the font is stricken out or not
     */
    public void setStrikeout(boolean strike) {
        setFontOption(strike, CANCELLATION);
    }

    /**
     * get whether the font is to be stricken out or not
     *
     * @return strike - whether the font is stricken out or not
     * @see #getFontOption(org.apache.poi.util.BitField)
     */
    public boolean isStruckout() {
        return getFontOption(CANCELLATION);
    }

    /**
     * set the font weight (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     *
     * @param bw - a number between 100-1000 for the fonts "boldness"
     */
    private void setFontWeight(short bw) {
        setShort(OFFSET_FONT_WEIGHT, Math.max(100, Math.min(1000, bw)));
    }

    /**
     * set the font weight to bold (weight=700) or to normal(weight=400) boldness.
     *
     * @param bold - set font weight to bold if true; to normal otherwise
     */
    public void setBold(boolean bold) {
        setFontWeight(bold?FONT_WEIGHT_BOLD:FONT_WEIGHT_NORMAL);
    }

    /**
     * get the font weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     *
     * @return bw - a number between 100-1000 for the fonts "boldness"
     */
    public short getFontWeight() {
        return getShort(OFFSET_FONT_WEIGHT);
    }

    /**
     * get whether the font weight is set to bold or not
     *
     * @return bold - whether the font is bold or not
     */
    public boolean isBold() {
        return getFontWeight()==FONT_WEIGHT_BOLD;
    }

    /**
     * get the type of super or subscript for the font
     *
     * @return super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    public short getEscapementType() {
        return getShort(OFFSET_ESCAPEMENT_TYPE);
    }

    /**
     * set the escapement type for the font
     *
     * @param escapementType  super or subscript option
     * @see org.apache.poi.ss.usermodel.Font#SS_NONE
     * @see org.apache.poi.ss.usermodel.Font#SS_SUPER
     * @see org.apache.poi.ss.usermodel.Font#SS_SUB
     */
    public void setEscapementType( short escapementType) {
        setShort(OFFSET_ESCAPEMENT_TYPE, escapementType);
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
    public short getUnderlineType() {
        return getShort(OFFSET_UNDERLINE_TYPE);
    }

    /**
     * set the type of underlining type for the font
     *
     * @param underlineType underline option
     *
     * @see org.apache.poi.ss.usermodel.Font#U_NONE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE
     * @see org.apache.poi.ss.usermodel.Font#U_SINGLE_ACCOUNTING
     * @see org.apache.poi.ss.usermodel.Font#U_DOUBLE_ACCOUNTING
     */
    public void setUnderlineType( short underlineType) {
        setShort(OFFSET_UNDERLINE_TYPE, underlineType);
    }


    public short getFontColorIndex() {
        return (short)getInt(OFFSET_FONT_COLOR_INDEX);
    }

    public void setFontColorIndex(short fci ) {
        setInt(OFFSET_FONT_COLOR_INDEX,fci);
    }

    private boolean getOptionFlag(BitField field) {
        int optionFlags = getInt(OFFSET_OPTION_FLAGS);
        int value = field.getValue(optionFlags);
        return value == 0;
    }

    private void setOptionFlag(boolean modified, BitField field) {
        int value = modified? 0 : 1;
        int optionFlags = getInt(OFFSET_OPTION_FLAGS);
        optionFlags = field.setValue(optionFlags, value);
        setInt(OFFSET_OPTION_FLAGS, optionFlags);
    }


    public boolean isFontStyleModified() {
        return getOptionFlag(POSTURE);
    }


    public void setFontStyleModified(boolean modified) {
        setOptionFlag(modified, POSTURE);
    }

    public boolean isFontOutlineModified() {
        return getOptionFlag(OUTLINE);
    }

    public void setFontOutlineModified(boolean modified) {
        setOptionFlag(modified, OUTLINE);
    }

    public boolean isFontShadowModified() {
        return getOptionFlag(SHADOW);
    }

    public void setFontShadowModified(boolean modified) {
        setOptionFlag(modified, SHADOW);
    }
    public void setFontCancellationModified(boolean modified) {
        setOptionFlag(modified, CANCELLATION);
    }

    public boolean isFontCancellationModified() {
        return getOptionFlag(CANCELLATION);
    }

    public void setEscapementTypeModified(boolean modified) {
        int value = modified? 0 : 1;
        setInt(OFFSET_ESCAPEMENT_TYPE_MODIFIED, value);
    }

    public boolean isEscapementTypeModified() {
        int escapementModified = getInt(OFFSET_ESCAPEMENT_TYPE_MODIFIED);
        return escapementModified == 0;
    }

    public void setUnderlineTypeModified(boolean modified) {
        int value = modified? 0 : 1;
        setInt(OFFSET_UNDERLINE_TYPE_MODIFIED, value);
    }

    public boolean isUnderlineTypeModified() {
        int underlineModified = getInt(OFFSET_UNDERLINE_TYPE_MODIFIED);
        return underlineModified == 0;
    }

    public void setFontWieghtModified(boolean modified) {
        int value = modified? 0 : 1;
        setInt(OFFSET_FONT_WEIGHT_MODIFIED, value);
    }

    public boolean isFontWeightModified() {
        int fontStyleModified = getInt(OFFSET_FONT_WEIGHT_MODIFIED);
        return fontStyleModified == 0;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("fontHeight", this::getFontHeight);
        m.put("options", getBitsAsString(() -> getInt(OFFSET_OPTION_FLAGS),
            new BitField[]{POSTURE,OUTLINE,SHADOW,CANCELLATION},
            new String[]{"POSTURE_MODIFIED","OUTLINE_MODIFIED","SHADOW_MODIFIED","STRUCKOUT_MODIFIED"}));
        m.put("fontOptions", getBitsAsString(() -> getInt(OFFSET_FONT_OPTIONS),
            new BitField[]{POSTURE,OUTLINE,SHADOW,CANCELLATION},
            new String[]{"ITALIC","OUTLINE","SHADOW","STRUCKOUT"}));
        m.put("fontWEightModified", this::isFontWeightModified);
        m.put("fontWeight", getEnumBitsAsString(this::getFontWeight,
            new int[]{FONT_WEIGHT_NORMAL,FONT_WEIGHT_BOLD},
            new String[]{"NORMAL","BOLD"}));
        m.put("escapementTypeModified", this::isEscapementTypeModified);
        m.put("escapementType", this::getEscapementType);
        m.put("underlineTypeModified", this::isUnderlineTypeModified);
        m.put("underlineType", this::getUnderlineType);
        m.put("colorIndex", this::getFontColorIndex);
        return Collections.unmodifiableMap(m);
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public FontFormatting copy() {
        return new FontFormatting(this);
    }
}
