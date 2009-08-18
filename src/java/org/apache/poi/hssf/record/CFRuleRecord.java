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
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Conditional Formatting Rule Record (0x01B1).<br/>
 *
 * @author Dmitriy Kumshayev
 */
public final class CFRuleRecord extends StandardRecord {

	public static final short sid = 0x01B1;

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
	}

	private byte  field_1_condition_type;
	public static final byte CONDITION_TYPE_CELL_VALUE_IS = 1;
	public static final byte CONDITION_TYPE_FORMULA = 2;

	private byte  field_2_comparison_operator;

	private int   field_5_options;

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

	private Formula field_17_formula1;
	private Formula field_18_formula2;

	/** Creates new CFRuleRecord */
	private CFRuleRecord(byte conditionType, byte comparisonOperation)
	{
		field_1_condition_type=conditionType;
		field_2_comparison_operator=comparisonOperation;

		// Set modification flags to 1: by default options are not modified
		field_5_options = modificationBits.setValue(field_5_options, -1);
		// Set formatting block flags to 0 (no formatting blocks)
		field_5_options = fmtBlockBits.setValue(field_5_options, 0);
		field_5_options = undocumented.clear(field_5_options);

		field_6_not_used = (short)0x8002; // Excel seems to write this value, but it doesn't seem to care what it reads
		_fontFormatting=null;
		_borderFormatting=null;
		_patternFormatting=null;
		field_17_formula1=Formula.create(Ptg.EMPTY_PTG_ARRAY);
		field_18_formula2=Formula.create(Ptg.EMPTY_PTG_ARRAY);
	}

	private CFRuleRecord(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2) {
		this(conditionType, comparisonOperation);
		field_17_formula1 = Formula.create(formula1);
		field_18_formula2 = Formula.create(formula2);
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
		field_1_condition_type = in.readByte();
		field_2_comparison_operator = in.readByte();
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
		field_17_formula1 = Formula.read(field_3_formula1_len, in);
		field_18_formula2 = Formula.read(field_4_formula2_len, in);
	}

	public byte getConditionType()
	{
		return field_1_condition_type;
	}

	public boolean containsFontFormattingBlock()
	{
		return getOptionFlag(font);
	}
	public void setFontFormatting(FontFormatting fontFormatting)
	{
		_fontFormatting = fontFormatting;
		setOptionFlag(fontFormatting != null, font);
	}
	public FontFormatting getFontFormatting()
	{
		if( containsFontFormattingBlock())
		{
			return _fontFormatting;
		}
		return null;
	}

	public boolean containsAlignFormattingBlock()
	{
		return getOptionFlag(align);
	}
	public void setAlignFormattingUnchanged()
	{
		setOptionFlag(false,align);
	}

	public boolean containsBorderFormattingBlock()
	{
		return getOptionFlag(bord);
	}
	public void setBorderFormatting(BorderFormatting borderFormatting)
	{
		_borderFormatting = borderFormatting;
		setOptionFlag(borderFormatting != null, bord);
	}
	public BorderFormatting getBorderFormatting()
	{
		if( containsBorderFormattingBlock())
		{
			return _borderFormatting;
		}
		return null;
	}

	public boolean containsPatternFormattingBlock()
	{
		return getOptionFlag(patt);
	}
	public void setPatternFormatting(PatternFormatting patternFormatting)
	{
		_patternFormatting = patternFormatting;
		setOptionFlag(patternFormatting!=null, patt);
	}
	public PatternFormatting getPatternFormatting()
	{
		if( containsPatternFormattingBlock())
		{
			return _patternFormatting;
		}
		return null;
	}

	public boolean containsProtectionFormattingBlock()
	{
		return getOptionFlag(prot);
	}
	public void setProtectionFormattingUnchanged()
	{
		setOptionFlag(false,prot);
	}

	public void setComparisonOperation(byte operation)
	{
		field_2_comparison_operator = operation;
	}

	public byte getComparisonOperation()
	{
		return field_2_comparison_operator;
	}


	/**
	 * get the option flags
	 *
	 * @return bit mask
	 */
	public int getOptions()
	{
		return field_5_options;
	}

	private boolean isModified(BitField field)
	{
		return !field.isSet(field_5_options);
	}

	private void setModified(boolean modified, BitField field)
	{
		field_5_options = field.setBoolean(field_5_options, !modified);
	}

	public boolean isLeftBorderModified()
	{
		return isModified(bordLeft);
	}

	public void setLeftBorderModified(boolean modified)
	{
		setModified(modified,bordLeft);
	}

	public boolean isRightBorderModified()
	{
		return isModified(bordRight);
	}

	public void setRightBorderModified(boolean modified)
	{
		setModified(modified,bordRight);
	}

	public boolean isTopBorderModified()
	{
		return isModified(bordTop);
	}

	public void setTopBorderModified(boolean modified)
	{
		setModified(modified,bordTop);
	}

	public boolean isBottomBorderModified()
	{
		return isModified(bordBot);
	}

	public void setBottomBorderModified(boolean modified)
	{
		setModified(modified,bordBot);
	}

	public boolean isTopLeftBottomRightBorderModified()
	{
		return isModified(bordTlBr);
	}

	public void setTopLeftBottomRightBorderModified(boolean modified)
	{
		setModified(modified,bordTlBr);
	}

	public boolean isBottomLeftTopRightBorderModified()
	{
		return isModified(bordBlTr);
	}

	public void setBottomLeftTopRightBorderModified(boolean modified)
	{
		setModified(modified,bordBlTr);
	}

	public boolean isPatternStyleModified()
	{
		return isModified(pattStyle);
	}

	public void setPatternStyleModified(boolean modified)
	{
		setModified(modified,pattStyle);
	}

	public boolean isPatternColorModified()
	{
		return isModified(pattCol);
	}

	public void setPatternColorModified(boolean modified)
	{
		setModified(modified,pattCol);
	}

	public boolean isPatternBackgroundColorModified()
	{
		return isModified(pattBgCol);
	}

	public void setPatternBackgroundColorModified(boolean modified)
	{
		setModified(modified,pattBgCol);
	}

	private boolean getOptionFlag(BitField field)
	{
		return field.isSet(field_5_options);
	}

	private void setOptionFlag(boolean flag, BitField field)
	{
		field_5_options = field.setBoolean(field_5_options, flag);
	}

	/**
	 * get the stack of the 1st expression as a list
	 *
	 * @return list of tokens (casts stack to a list and returns it!)
	 * this method can return null is we are unable to create Ptgs from
	 *	 existing excel file
	 * callers should check for null!
	 */

	public Ptg[] getParsedExpression1()
	{
		return field_17_formula1.getTokens();
	}
	public void setParsedExpression1(Ptg[] ptgs) {
		field_17_formula1 = Formula.create(ptgs);
	}

	/**
	 * get the stack of the 2nd expression as a list
	 *
	 * @return array of {@link Ptg}s, possibly <code>null</code>
	 */
	public Ptg[] getParsedExpression2() {
		return Formula.getTokens(field_18_formula2);
	}
	public void setParsedExpression2(Ptg[] ptgs) {
		field_18_formula2 = Formula.create(ptgs);
	}

	public short getSid()
	{
		return sid;
	}

	/**
	 * @param ptgs must not be <code>null</code>
	 * @return encoded size of the formula tokens (does not include 2 bytes for ushort length)
	 */
	private static int getFormulaSize(Formula formula) {
		return formula.getEncodedTokenSize();
	}

	/**
	 * called by the class that is responsible for writing this sucker.
	 * Subclasses should implement this so that their data is passed back in a
	 * byte array.
	 *
	 * @param out the stream to write to
	 */
	public void serialize(LittleEndianOutput out) {

		int formula1Len=getFormulaSize(field_17_formula1);
		int formula2Len=getFormulaSize(field_18_formula2);

		out.writeByte(field_1_condition_type);
		out.writeByte(field_2_comparison_operator);
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

		field_17_formula1.serializeTokens(out);
		field_18_formula2.serializeTokens(out);
	}

	protected int getDataSize() {
		return 12 +
					(containsFontFormattingBlock()?_fontFormatting.getRawRecord().length:0)+
					(containsBorderFormattingBlock()?8:0)+
					(containsPatternFormattingBlock()?4:0)+
					getFormulaSize(field_17_formula1)+
					getFormulaSize(field_18_formula2)
					;
	}


	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[CFRULE]\n");
        buffer.append("    .condition_type   ="+field_1_condition_type);
		buffer.append("    OPTION FLAGS=0x"+Integer.toHexString(getOptions()));
		if (false) {
			if (containsFontFormattingBlock()) {
				buffer.append(_fontFormatting.toString());
			}
			if (containsBorderFormattingBlock()) {
				buffer.append(_borderFormatting.toString());
			}
			if (containsPatternFormattingBlock()) {
				buffer.append(_patternFormatting.toString());
			}
			buffer.append("[/CFRULE]\n");
		}
		return buffer.toString();
	}

	public Object clone() {
		CFRuleRecord rec = new CFRuleRecord(field_1_condition_type, field_2_comparison_operator);
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
		rec.field_17_formula1 = field_17_formula1.copy();
		rec.field_18_formula2 = field_17_formula1.copy();

		return rec;
	}

	/**
	 * TODO - parse conditional format formulas properly i.e. produce tRefN and tAreaN instead of tRef and tArea
	 * this call will produce the wrong results if the formula contains any cell references
	 * One approach might be to apply the inverse of SharedFormulaRecord.convertSharedFormulas(Stack, int, int)
	 * Note - two extra parameters (rowIx & colIx) will be required. They probably come from one of the Region objects.
	 *
	 * @return <code>null</code> if <tt>formula</tt> was null.
	 */
    private static Ptg[] parseFormula(String formula, HSSFSheet sheet) {
        if(formula == null) {
            return null;
        }
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        return HSSFFormulaParser.parse(formula, sheet.getWorkbook(), FormulaType.CELL, sheetIndex);
    }
}
