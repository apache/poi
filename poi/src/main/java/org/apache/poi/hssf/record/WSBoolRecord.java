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

package org.apache.poi.hssf.record;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Stores workbook settings (aka its a big "everything we didn't put somewhere else")
 */
public final class WSBoolRecord extends StandardRecord {
    public static final short     sid = 0x0081;
    // are automatic page breaks visible
    private static final BitField autobreaks    = BitFieldFactory.getInstance(0x01);

    // bits 1 to 3 unused
    // is sheet dialog sheet
    private static final BitField dialog        = BitFieldFactory.getInstance(0x10);
    // whether to apply automatic styles to outlines
    private static final BitField applystyles   = BitFieldFactory.getInstance(0x20);
    // whether summary rows will appear below detail in outlines
    private static final BitField rowsumsbelow  = BitFieldFactory.getInstance(0x40);
    // whether summary rows will appear right of the detail in outlines
    private static final BitField rowsumsright  = BitFieldFactory.getInstance(0x80);
    // whether to fit stuff to the page
    private static final BitField fittopage     = BitFieldFactory.getInstance(0x01);

    // bit 2 reserved
    // whether to display outline symbols (in the gutters)
    private static final BitField displayguts   = BitFieldFactory.getInstance(0x06);

    // bits 4-5 reserved
    // whether to use alternate expression eval
    private static final BitField alternateexpression = BitFieldFactory.getInstance(0x40);
    // whether to use alternate formula entry
    private static final BitField alternateformula    = BitFieldFactory.getInstance(0x80);

    // crappy names are because this is really one big short field (2byte)
    private byte field_1_wsbool;
    // but the docs inconsistently use it as 2 separate bytes
    private byte field_2_wsbool;


    public WSBoolRecord() {}

    public WSBoolRecord(WSBoolRecord other) {
        super(other);
        field_1_wsbool = other.field_1_wsbool;
        field_2_wsbool = other.field_2_wsbool;
    }

    public WSBoolRecord(RecordInputStream in) {
        byte[] data = in.readRemainder();
        field_1_wsbool =
            data[ 1 ];   // backwards because theoretically this is one short field
        field_2_wsbool =
            data[ 0 ];   // but it was easier to implement it this way to avoid confusion
    }                             // because the dev kit shows the masks for it as 2 byte fields

    // why?  Why ask why?  But don't drink bud dry as its a really
    // crappy beer, try the czech "Budvar" beer (which is the real
    // budweiser though its ironically good...its sold in the USs
    // as czechvar  --- odd that they had the name first but can't
    // use it)...

    /**
     * set first byte (see bit setters)
     *
     * @param bool1 Set boolean 1 of this record
     */
    public void setWSBool1(byte bool1) {
        field_1_wsbool = bool1;
    }

    // bool1 bitfields

    /**
     * show automatic page breaks or not
     * @param ab  whether to show auto page breaks
     */
    public void setAutobreaks(boolean ab)
    {
        field_1_wsbool = autobreaks.setByteBoolean(field_1_wsbool, ab);
    }

    /**
     * set whether sheet is a dialog sheet or not
     * @param isDialog or not
     */
    public void setDialog(boolean isDialog)
    {
        field_1_wsbool = dialog.setByteBoolean(field_1_wsbool, isDialog);
    }

    /**
     * set if row summaries appear below detail in the outline
     * @param below or not
     */
    public void setRowSumsBelow(boolean below)
    {
        field_1_wsbool = rowsumsbelow.setByteBoolean(field_1_wsbool, below);
    }

    /**
     * set if col summaries appear right of the detail in the outline
     * @param right or not
     */
    public void setRowSumsRight(boolean right)
    {
        field_1_wsbool = rowsumsright.setByteBoolean(field_1_wsbool, right);
    }

    // end bitfields

    /**
     * set the second byte (see bit setters)
     *
     * @param bool2 Set boolean 2 of this record
     */
    public void setWSBool2(byte bool2)
    {
        field_2_wsbool = bool2;
    }

    // bool2 bitfields

    /**
     * fit to page option is on
     * @param fit2page  fit or not
     */
    public void setFitToPage(boolean fit2page)
    {
        field_2_wsbool = fittopage.setByteBoolean(field_2_wsbool, fit2page);
    }

    /**
     * set whether to display the guts or not
     *
     * @param guts or no guts (or glory)
     */
    public void setDisplayGuts(boolean guts)
    {
        field_2_wsbool = displayguts.setByteBoolean(field_2_wsbool, guts);
    }

    /**
     * whether alternate expression evaluation is on
     * @param altexp  alternative expression evaluation or not
     */
    public void setAlternateExpression(boolean altexp)
    {
        field_2_wsbool = alternateexpression.setByteBoolean(field_2_wsbool,
                altexp);
    }

    /**
     * whether alternative formula entry is on
     * @param formula  alternative formulas or not
     */
    public void setAlternateFormula(boolean formula)
    {
        field_2_wsbool = alternateformula.setByteBoolean(field_2_wsbool,
                formula);
    }

    // end bitfields

    /**
     * get first byte (see bit getters)
     *
     * @return boolean 1 of this record
     */
    public byte getWSBool1()
    {
        return field_1_wsbool;
    }

    // bool1 bitfields

    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */
    public boolean getAutobreaks()
    {
        return autobreaks.isSet(field_1_wsbool);
    }

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */
    public boolean getDialog()
    {
        return dialog.isSet(field_1_wsbool);
    }

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */
    public boolean getRowSumsBelow()
    {
        return rowsumsbelow.isSet(field_1_wsbool);
    }

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */
    public boolean getRowSumsRight()
    {
        return rowsumsright.isSet(field_1_wsbool);
    }

    // end bitfields

    /**
     * get the second byte (see bit getters)
     *
     * @return boolean 1 of this record
     */
    public byte getWSBool2() {
        return field_2_wsbool;
    }

    // bool2 bitfields

    /**
     * fit to page option is on
     * @return fit or not
     */
    public boolean getFitToPage() {
        return fittopage.isSet(field_2_wsbool);
    }

    /**
     * get whether to display the guts or not
     *
     * @return guts or no guts (or glory)
     */
    public boolean getDisplayGuts() {
        return displayguts.isSet(field_2_wsbool);
    }

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */
    public boolean getAlternateExpression() {
        return alternateexpression.isSet(field_2_wsbool);
    }

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */
    public boolean getAlternateFormula() {
        return alternateformula.isSet(field_2_wsbool);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(getWSBool2());
        out.writeByte(getWSBool1());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public WSBoolRecord copy() {
      return new WSBoolRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.WS_BOOL;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "wsbool1", getBitsAsString(this::getWSBool1,
                new BitField[]{autobreaks, dialog, applystyles, rowsumsbelow, rowsumsright},
                new String[]{"AUTO_BREAKS", "DIALOG", "APPLY_STYLES", "ROW_SUMS_BELOW", "ROW_SUMS_RIGHT"}),
            "wsbool2", getBitsAsString(this::getWSBool2,
                new BitField[]{fittopage, displayguts, alternateexpression, alternateformula},
                new String[]{"FIT_TO_PAGE", "DISPLAY_GUTS", "ALTERNATE_EXPRESSION", "ALTERNATE_FORMULA"})
        );
    }
}
