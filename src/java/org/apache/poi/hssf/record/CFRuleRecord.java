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

import java.util.Arrays;

import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Conditional Formatting Rule Record (0x01B1). 
 * 
 * <p>This is for the older-style Excel conditional formattings,
 *  new-style (Excel 2007+) also make use of {@link CFRule12Record}
 *  and {@link CFExRuleRecord} for their rules.
 */
public final class CFRuleRecord extends CFRuleBase {
    public static final short sid = 0x01B1;

    private int field_5_options;

    private static final BitField modificationBits = bf(0x003FFFFF); // Bits: font,align,bord,patt,prot
    private static final BitField alignHor      = bf(0x00000001); // 0 = Horizontal alignment modified
    private static final BitField alignVer      = bf(0x00000002); // 0 = Vertical alignment modified
    private static final BitField alignWrap     = bf(0x00000004); // 0 = Text wrapped flag modified
    private static final BitField alignRot      = bf(0x00000008); // 0 = Text rotation modified
    private static final BitField alignJustLast = bf(0x00000010); // 0 = Justify last line flag modified
    private static final BitField alignIndent   = bf(0x00000020); // 0 = Indentation modified
    private static final BitField alignShrin    = bf(0x00000040); // 0 = Shrink to fit flag modified
    private static final BitField notUsed1      = bf(0x00000080); // Always 1
    private static final BitField protLocked    = bf(0x00000100); // 0 = Cell locked flag modified
    private static final BitField protHidden    = bf(0x00000200); // 0 = Cell hidden flag modified
    private static final BitField bordLeft      = bf(0x00000400); // 0 = Left border style and colour modified
    private static final BitField bordRight     = bf(0x00000800); // 0 = Right border style and colour modified
    private static final BitField bordTop       = bf(0x00001000); // 0 = Top border style and colour modified
    private static final BitField bordBot       = bf(0x00002000); // 0 = Bottom border style and colour modified
    private static final BitField bordTlBr      = bf(0x00004000); // 0 = Top-left to bottom-right border flag modified
    private static final BitField bordBlTr      = bf(0x00008000); // 0 = Bottom-left to top-right border flag modified
    private static final BitField pattStyle     = bf(0x00010000); // 0 = Pattern style modified
    private static final BitField pattCol       = bf(0x00020000); // 0 = Pattern colour modified
    private static final BitField pattBgCol     = bf(0x00040000); // 0 = Pattern background colour modified
    private static final BitField notUsed2      = bf(0x00380000); // Always 111
    private static final BitField undocumented  = bf(0x03C00000); // Undocumented bits
    private static final BitField fmtBlockBits  = bf(0x7C000000); // Bits: font,align,bord,patt,prot
    private static final BitField font          = bf(0x04000000); // 1 = Record contains font formatting block
    private static final BitField align         = bf(0x08000000); // 1 = Record contains alignment formatting block
    private static final BitField bord          = bf(0x10000000); // 1 = Record contains border formatting block
    private static final BitField patt          = bf(0x20000000); // 1 = Record contains pattern formatting block
    private static final BitField prot          = bf(0x40000000); // 1 = Record contains protection formatting block
    private static final BitField alignTextDir  = bf(0x80000000); // 0 = Text direction modified


    private static BitField bf(int i) {
        return BitFieldFactory.getInstance(i);
    }

    private short field_6_not_used;

    private FontFormatting _fontFormatting;

    private BorderFormatting _borderFormatting;

    private PatternFormatting _patternFormatting;

    /** Creates new CFRuleRecord */
    private CFRuleRecord(byte conditionType, byte comparisonOperation) {
        super(conditionType, comparisonOperation);
        setDefaults();
    }

    private CFRuleRecord(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2) {
        super(conditionType, comparisonOperation, formula1, formula2);
        setDefaults();
    }
    private void setDefaults() {
        // Set modification flags to 1: by default options are not modified
        field_5_options = modificationBits.setValue(field_5_options, -1);
        // Set formatting block flags to 0 (no formatting blocks)
        field_5_options = fmtBlockBits.setValue(field_5_options, 0);
        field_5_options = undocumented.clear(field_5_options);

        field_6_not_used = (short)0x8002; // Excel seems to write this value, but it doesn't seem to care what it reads
        _fontFormatting=null;
        _borderFormatting=null;
        _patternFormatting=null;
    }

    /**
     * Creates a new comparison operation rule
     */
    public static CFRuleRecord create(HSSFSheet sheet, String formulaText) {
        Ptg[] formula1 = parseFormula(formulaText, sheet);
        return new CFRuleRecord(CONDITION_TYPE_FORMULA, ComparisonOperator.NO_COMPARISON,
                formula1, null);
    }
    /**
     * Creates a new comparison operation rule
     */
    public static CFRuleRecord create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        return new CFRuleRecord(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation, formula1, formula2);
    }

    public CFRuleRecord(RecordInputStream in) {
        setConditionType(in.readByte());
        setComparisonOperation(in.readByte());
        int field_3_formula1_len = in.readUShort();
        int field_4_formula2_len = in.readUShort();
        field_5_options = in.readInt();
        field_6_not_used = in.readShort();

        if (containsFontFormattingBlock()) {
            _fontFormatting = new FontFormatting(in);
        }

        if (containsBorderFormattingBlock()) {
            _borderFormatting = new BorderFormatting(in);
        }

        if (containsPatternFormattingBlock()) {
            _patternFormatting = new PatternFormatting(in);
        }

        // "You may not use unions, intersections or array constants in Conditional Formatting criteria"
        setFormula1(Formula.read(field_3_formula1_len, in));
        setFormula2(Formula.read(field_4_formula2_len, in));
    }

    public boolean containsFontFormattingBlock() {
        return getOptionFlag(font);
    }
    public void setFontFormatting(FontFormatting fontFormatting) {
        _fontFormatting = fontFormatting;
        setOptionFlag(fontFormatting != null, font);
    }
    public FontFormatting getFontFormatting() {
        if( containsFontFormattingBlock()) {
            return _fontFormatting;
        }
        return null;
    }

    public boolean containsAlignFormattingBlock() {
        return getOptionFlag(align);
    }
    public void setAlignFormattingUnchanged() {
        setOptionFlag(false,align);
    }

    public boolean containsBorderFormattingBlock() {
        return getOptionFlag(bord);
    }
    public void setBorderFormatting(BorderFormatting borderFormatting) {
        _borderFormatting = borderFormatting;
        setOptionFlag(borderFormatting != null, bord);
    }
    public BorderFormatting getBorderFormatting() {
        if( containsBorderFormattingBlock()) {
            return _borderFormatting;
        }
        return null;
    }

    public boolean containsPatternFormattingBlock() {
        return getOptionFlag(patt);
    }
    public void setPatternFormatting(PatternFormatting patternFormatting) {
        _patternFormatting = patternFormatting;
        setOptionFlag(patternFormatting!=null, patt);
    }
    public PatternFormatting getPatternFormatting() {
        if( containsPatternFormattingBlock())
        {
            return _patternFormatting;
        }
        return null;
    }

    public boolean containsProtectionFormattingBlock() {
        return getOptionFlag(prot);
    }
    public void setProtectionFormattingUnchanged() {
        setOptionFlag(false,prot);
    }

    /**
     * get the option flags
     *
     * @return bit mask
     */
    public int getOptions() {
        return field_5_options;
    }

    private boolean isModified(BitField field) {
        return !field.isSet(field_5_options);
    }
    private void setModified(boolean modified, BitField field) {
        field_5_options = field.setBoolean(field_5_options, !modified);
    }

    public boolean isLeftBorderModified() {
        return isModified(bordLeft);
    }
    public void setLeftBorderModified(boolean modified) {
        setModified(modified,bordLeft);
    }

    public boolean isRightBorderModified() {
        return isModified(bordRight);
    }
    public void setRightBorderModified(boolean modified)
    {
        setModified(modified,bordRight);
    }

    public boolean isTopBorderModified() {
        return isModified(bordTop);
    }
    public void setTopBorderModified(boolean modified) {
        setModified(modified,bordTop);
    }

    public boolean isBottomBorderModified() {
        return isModified(bordBot);
    }
    public void setBottomBorderModified(boolean modified) {
        setModified(modified,bordBot);
    }

    public boolean isTopLeftBottomRightBorderModified() {
        return isModified(bordTlBr);
    }
    public void setTopLeftBottomRightBorderModified(boolean modified) {
        setModified(modified,bordTlBr);
    }

    public boolean isBottomLeftTopRightBorderModified() {
        return isModified(bordBlTr);
    }
    public void setBottomLeftTopRightBorderModified(boolean modified) {
        setModified(modified,bordBlTr);
    }

    public boolean isPatternStyleModified() {
        return isModified(pattStyle);
    }
    public void setPatternStyleModified(boolean modified) {
        setModified(modified,pattStyle);
    }

    public boolean isPatternColorModified() {
        return isModified(pattCol);
    }
    public void setPatternColorModified(boolean modified) {
        setModified(modified,pattCol);
    }

    public boolean isPatternBackgroundColorModified() {
        return isModified(pattBgCol);
    }
    public void setPatternBackgroundColorModified(boolean modified) {
        setModified(modified,pattBgCol);
    }

    private boolean getOptionFlag(BitField field) {
        return field.isSet(field_5_options);
    }
    private void setOptionFlag(boolean flag, BitField field) {
        field_5_options = field.setBoolean(field_5_options, flag);
    }

    public short getSid() {
        return sid;
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param out the stream to write to
     */
    public void serialize(LittleEndianOutput out) {
        int formula1Len=getFormulaSize(getFormula1());
        int formula2Len=getFormulaSize(getFormula2());

        out.writeByte(getConditionType());
        out.writeByte(getComparisonOperation());
        out.writeShort(formula1Len);
        out.writeShort(formula2Len);
        out.writeInt(field_5_options);
        out.writeShort(field_6_not_used);

        if (containsFontFormattingBlock()) {
            byte[] fontFormattingRawRecord  = _fontFormatting.getRawRecord();
            out.write(fontFormattingRawRecord);
        }

        if (containsBorderFormattingBlock()) {
            _borderFormatting.serialize(out);
        }

        if (containsPatternFormattingBlock()) {
            _patternFormatting.serialize(out);
        }

        getFormula1().serializeTokens(out);
        getFormula2().serializeTokens(out);
    }

    protected int getDataSize() {
        int i = 12 +
                (containsFontFormattingBlock()?_fontFormatting.getRawRecord().length:0)+
                (containsBorderFormattingBlock()?8:0)+
                (containsPatternFormattingBlock()?4:0)+
                getFormulaSize(getFormula1())+
                getFormulaSize(getFormula2());
        return i;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[CFRULE]\n");
        buffer.append("    .condition_type   =").append(getConditionType()).append("\n");
        buffer.append("    OPTION FLAGS=0x").append(Integer.toHexString(getOptions())).append("\n");
        if (containsFontFormattingBlock()) {
            buffer.append(_fontFormatting.toString()).append("\n");
        }
        if (containsBorderFormattingBlock()) {
            buffer.append(_borderFormatting.toString()).append("\n");
        }
        if (containsPatternFormattingBlock()) {
            buffer.append(_patternFormatting.toString()).append("\n");
        }
        buffer.append("    Formula 1 =").append(Arrays.toString(getFormula1().getTokens())).append("\n");
        buffer.append("    Formula 2 =").append(Arrays.toString(getFormula2().getTokens())).append("\n");
        buffer.append("[/CFRULE]\n");
        return buffer.toString();
    }

    public Object clone() {
        CFRuleRecord rec = new CFRuleRecord(getConditionType(), getComparisonOperation());
        rec.field_5_options = field_5_options;
        rec.field_6_not_used = field_6_not_used;
        if (containsFontFormattingBlock()) {
            rec._fontFormatting = (FontFormatting) _fontFormatting.clone();
        }
        if (containsBorderFormattingBlock()) {
            rec._borderFormatting = (BorderFormatting) _borderFormatting.clone();
        }
        if (containsPatternFormattingBlock()) {
            rec._patternFormatting = (PatternFormatting) _patternFormatting.clone();
        }
        rec.setFormula1(getFormula1().copy());
        rec.setFormula2(getFormula2().copy());

        return rec;
    }
}
