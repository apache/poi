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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Conditional Formatting Rules. This can hold old-style rules
 *   
 * 
 * <p>This is for the older-style Excel conditional formattings,
 *  new-style (Excel 2007+) also make use of {@link CFRule12Record}
 *  for their rules.</p>
 */
public abstract class CFRuleBase extends StandardRecord implements Cloneable {
    // FIXME: Merge with org.apache.poi.ss.usermodel.ComparisonOperator and rewrite as an enum
    public static final class ComparisonOperator {
        public static final byte NO_COMPARISON = 0;
        public static final byte BETWEEN       = 1;
        public static final byte NOT_BETWEEN   = 2;
        public static final byte EQUAL         = 3;
        public static final byte NOT_EQUAL     = 4;
        public static final byte GT            = 5;
        public static final byte LT            = 6;
        public static final byte GE            = 7;
        public static final byte LE            = 8;
        private static final byte max_operator = 8;
    }
    protected static final POILogger logger = POILogFactory.getLogger(CFRuleBase.class);

    private byte condition_type;
    // The only kinds that CFRuleRecord handles
    public static final byte CONDITION_TYPE_CELL_VALUE_IS = 1;
    public static final byte CONDITION_TYPE_FORMULA = 2;
    // These are CFRule12Rule only
    public static final byte CONDITION_TYPE_COLOR_SCALE = 3;
    public static final byte CONDITION_TYPE_DATA_BAR = 4;
    public static final byte CONDITION_TYPE_FILTER = 5;
    public static final byte CONDITION_TYPE_ICON_SET = 6;

    private byte comparison_operator;

    public static final int TEMPLATE_CELL_VALUE = 0x0000;
    public static final int TEMPLATE_FORMULA = 0x0001;
    public static final int TEMPLATE_COLOR_SCALE_FORMATTING = 0x0002;
    public static final int TEMPLATE_DATA_BAR_FORMATTING = 0x0003;
    public static final int TEMPLATE_ICON_SET_FORMATTING = 0x0004;
    public static final int TEMPLATE_FILTER = 0x0005;
    public static final int TEMPLATE_UNIQUE_VALUES = 0x0007;
    public static final int TEMPLATE_CONTAINS_TEXT = 0x0008;
    public static final int TEMPLATE_CONTAINS_BLANKS = 0x0009;
    public static final int TEMPLATE_CONTAINS_NO_BLANKS = 0x000A;
    public static final int TEMPLATE_CONTAINS_ERRORS = 0x000B;
    public static final int TEMPLATE_CONTAINS_NO_ERRORS = 0x000C;
    public static final int TEMPLATE_TODAY = 0x000F;
    public static final int TEMPLATE_TOMORROW = 0x0010;
    public static final int TEMPLATE_YESTERDAY = 0x0011;
    public static final int TEMPLATE_LAST_7_DAYS = 0x0012;
    public static final int TEMPLATE_LAST_MONTH = 0x0013;
    public static final int TEMPLATE_NEXT_MONTH = 0x0014;
    public static final int TEMPLATE_THIS_WEEK = 0x0015;
    public static final int TEMPLATE_NEXT_WEEK = 0x0016;
    public static final int TEMPLATE_LAST_WEEK = 0x0017;
    public static final int TEMPLATE_THIS_MONTH = 0x0018;
    public static final int TEMPLATE_ABOVE_AVERAGE = 0x0019;
    public static final int TEMPLATE_BELOW_AVERAGE = 0x001A;
    public static final int TEMPLATE_DUPLICATE_VALUES = 0x001B;
    public static final int TEMPLATE_ABOVE_OR_EQUAL_TO_AVERAGE = 0x001D;
    public static final int TEMPLATE_BELOW_OR_EQUAL_TO_AVERAGE = 0x001E;
    
    static final BitField modificationBits = bf(0x003FFFFF); // Bits: font,align,bord,patt,prot
    static final BitField alignHor         = bf(0x00000001); // 0 = Horizontal alignment modified
    static final BitField alignVer         = bf(0x00000002); // 0 = Vertical alignment modified
    static final BitField alignWrap        = bf(0x00000004); // 0 = Text wrapped flag modified
    static final BitField alignRot         = bf(0x00000008); // 0 = Text rotation modified
    static final BitField alignJustLast    = bf(0x00000010); // 0 = Justify last line flag modified
    static final BitField alignIndent      = bf(0x00000020); // 0 = Indentation modified
    static final BitField alignShrin       = bf(0x00000040); // 0 = Shrink to fit flag modified
    static final BitField mergeCell        = bf(0x00000080); // Normally 1, 0 = Merge Cell flag modified
    static final BitField protLocked       = bf(0x00000100); // 0 = Cell locked flag modified
    static final BitField protHidden       = bf(0x00000200); // 0 = Cell hidden flag modified
    static final BitField bordLeft         = bf(0x00000400); // 0 = Left border style and colour modified
    static final BitField bordRight        = bf(0x00000800); // 0 = Right border style and colour modified
    static final BitField bordTop          = bf(0x00001000); // 0 = Top border style and colour modified
    static final BitField bordBot          = bf(0x00002000); // 0 = Bottom border style and colour modified
    static final BitField bordTlBr         = bf(0x00004000); // 0 = Top-left to bottom-right border flag modified
    static final BitField bordBlTr         = bf(0x00008000); // 0 = Bottom-left to top-right border flag modified
    static final BitField pattStyle        = bf(0x00010000); // 0 = Pattern style modified
    static final BitField pattCol          = bf(0x00020000); // 0 = Pattern colour modified
    static final BitField pattBgCol        = bf(0x00040000); // 0 = Pattern background colour modified
    static final BitField notUsed2         = bf(0x00380000); // Always 111 (ifmt / ifnt / 1)
    static final BitField undocumented     = bf(0x03C00000); // Undocumented bits
    static final BitField fmtBlockBits     = bf(0x7C000000); // Bits: font,align,bord,patt,prot
    static final BitField font             = bf(0x04000000); // 1 = Record contains font formatting block
    static final BitField align            = bf(0x08000000); // 1 = Record contains alignment formatting block
    static final BitField bord             = bf(0x10000000); // 1 = Record contains border formatting block
    static final BitField patt             = bf(0x20000000); // 1 = Record contains pattern formatting block
    static final BitField prot             = bf(0x40000000); // 1 = Record contains protection formatting block
    static final BitField alignTextDir     = bf(0x80000000); // 0 = Text direction modified

    private static BitField bf(int i) {
        return BitFieldFactory.getInstance(i);
    }

    protected int formatting_options;
    protected short formatting_not_used; // TODO Decode this properly

    protected FontFormatting _fontFormatting;
    protected BorderFormatting _borderFormatting;
    protected PatternFormatting _patternFormatting;
    
    private Formula formula1;
    private Formula formula2;

    /**
     * Creates new CFRuleRecord
     * 
     * @param conditionType the condition type
     * @param comparisonOperation the comparison operation
     */
    protected CFRuleBase(byte conditionType, byte comparisonOperation) {
        setConditionType(conditionType);
        setComparisonOperation(comparisonOperation);
        formula1 = Formula.create(Ptg.EMPTY_PTG_ARRAY);
        formula2 = Formula.create(Ptg.EMPTY_PTG_ARRAY);
    }
    protected CFRuleBase(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2) {
        this(conditionType, comparisonOperation);
        this.formula1 = Formula.create(formula1);
        this.formula2 = Formula.create(formula2);
    }
    protected CFRuleBase() {}
    
    protected int readFormatOptions(RecordInputStream in) {
        formatting_options = in.readInt();
        formatting_not_used = in.readShort();

        int len = 6;
        
        if (containsFontFormattingBlock()) {
            _fontFormatting = new FontFormatting(in);
            len += _fontFormatting.getDataLength();
        }

        if (containsBorderFormattingBlock()) {
            _borderFormatting = new BorderFormatting(in);
            len += _borderFormatting.getDataLength();
        }

        if (containsPatternFormattingBlock()) {
            _patternFormatting = new PatternFormatting(in);
            len += _patternFormatting.getDataLength();
        }
        
        return len;
    }

    public byte getConditionType() {
        return condition_type;
    }
    protected void setConditionType(byte condition_type) {
        if ((this instanceof CFRuleRecord)) {
            if (condition_type == CONDITION_TYPE_CELL_VALUE_IS ||
                condition_type == CONDITION_TYPE_FORMULA) {
                // Good, valid combination
            } else {
                throw new IllegalArgumentException("CFRuleRecord only accepts Value-Is and Formula types");
            }
        }
        this.condition_type = condition_type;
    }

    public void setComparisonOperation(byte operation) {
        if (operation < 0 || operation > ComparisonOperator.max_operator)
            throw new IllegalArgumentException(
                    "Valid operators are only in the range 0 to " +ComparisonOperator.max_operator);
        
        this.comparison_operator = operation;
    }
    public byte getComparisonOperation() {
        return comparison_operator;
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
        return formatting_options;
    }

    private boolean isModified(BitField field) {
        return !field.isSet(formatting_options);
    }
    private void setModified(boolean modified, BitField field) {
        formatting_options = field.setBoolean(formatting_options, !modified);
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
        return field.isSet(formatting_options);
    }
    private void setOptionFlag(boolean flag, BitField field) {
        formatting_options = field.setBoolean(formatting_options, flag);
    }
    
    protected int getFormattingBlockSize() {
        return 6 +
          (containsFontFormattingBlock()?_fontFormatting.getRawRecord().length:0)+
          (containsBorderFormattingBlock()?8:0)+
          (containsPatternFormattingBlock()?4:0);
    }
    protected void serializeFormattingBlock(LittleEndianOutput out) {
        out.writeInt(formatting_options);
        out.writeShort(formatting_not_used);

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
    }
    
    /**
     * get the stack of the 1st expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from
     *	 existing excel file
     * callers should check for null!
     */
    public Ptg[] getParsedExpression1() {
        return formula1.getTokens();
    }
    public void setParsedExpression1(Ptg[] ptgs) {
        formula1 = Formula.create(ptgs);
    }
    protected Formula getFormula1() {
        return formula1;
    }
    protected void setFormula1(Formula formula1) {
        this.formula1 = formula1;
    }

    /**
     * get the stack of the 2nd expression as a list
     *
     * @return array of {@link Ptg}s, possibly <code>null</code>
     */
    public Ptg[] getParsedExpression2() {
        return Formula.getTokens(formula2);
    }
    public void setParsedExpression2(Ptg[] ptgs) {
        formula2 = Formula.create(ptgs);
    }
    protected Formula getFormula2() {
        return formula2;
    }
    protected void setFormula2(Formula formula2) {
        this.formula2 = formula2;
    }

    /**
     * @param formula must not be <code>null</code>
     * @return encoded size of the formula tokens (does not include 2 bytes for ushort length)
     */
    protected static int getFormulaSize(Formula formula) {
        return formula.getEncodedTokenSize();
    }

    /**
     * TODO - parse conditional format formulas properly i.e. produce tRefN and tAreaN instead of tRef and tArea
     * this call will produce the wrong results if the formula contains any cell references
     * One approach might be to apply the inverse of SharedFormulaRecord.convertSharedFormulas(Stack, int, int)
     * Note - two extra parameters (rowIx &amp; colIx) will be required. They probably come from one of the Region objects.
     *
     * @param formula  The formula to parse, excluding the leading equals sign.
     * @param sheet  The sheet that the formula is on.
     * @return <code>null</code> if <tt>formula</tt> was null.
     */
    public static Ptg[] parseFormula(String formula, HSSFSheet sheet) {
        if(formula == null) {
            return null;
        }
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        return HSSFFormulaParser.parse(formula, sheet.getWorkbook(), FormulaType.CELL, sheetIndex);
    }
    
    protected void copyTo(CFRuleBase rec) {
        rec.condition_type = condition_type;
        rec.comparison_operator = comparison_operator;
        
        rec.formatting_options = formatting_options;
        rec.formatting_not_used = formatting_not_used;
        if (containsFontFormattingBlock()) {
            rec._fontFormatting = _fontFormatting.clone();
        }
        if (containsBorderFormattingBlock()) {
            rec._borderFormatting = _borderFormatting.clone();
        }
        if (containsPatternFormattingBlock()) {
            rec._patternFormatting = (PatternFormatting) _patternFormatting.clone();
        }
        
        rec.setFormula1(getFormula1().copy());
        rec.setFormula2(getFormula2().copy());
    }
    
    @Override
    public abstract CFRuleBase clone();
}
