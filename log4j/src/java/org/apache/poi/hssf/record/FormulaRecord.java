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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.Removal;

/**
 * Formula Record (0x0006).
 */
public final class FormulaRecord extends CellRecord {

	// docs say 406...because of a bug Microsoft support site article #Q184647)
	public static final short sid = 0x0006;
	// double + short + int
	private static final int FIXED_SIZE = 14;

	private static final BitField alwaysCalc = BitFieldFactory.getInstance(0x0001);
	private static final BitField calcOnLoad = BitFieldFactory.getInstance(0x0002);
	private static final BitField sharedFormula = BitFieldFactory.getInstance(0x0008);

	private double field_4_value;
	private short  field_5_options;
	/**
	 * Unused field.  As it turns out this field is often not zero..
	 * According to Microsoft Excel Developer's Kit Page 318:
	 * when writing the chn field (offset 20), it's supposed to be 0 but ignored on read
	 */
	private int field_6_zero;
	private Formula field_8_parsed_expr;

	/**
	 * Since the NaN support seems sketchy (different constants) we'll store and spit it out directly
	 */
	private FormulaSpecialCachedValue specialCachedValue;

	/** Creates new FormulaRecord */
	public FormulaRecord() {
		field_8_parsed_expr = Formula.create(Ptg.EMPTY_PTG_ARRAY);
	}

	public FormulaRecord(FormulaRecord other) {
		super(other);
		field_4_value = other.field_4_value;
		field_5_options = other.field_5_options;
		field_6_zero = other.field_6_zero;
		field_8_parsed_expr = (other.field_8_parsed_expr == null) ? null : new Formula(other.field_8_parsed_expr);
		specialCachedValue = (other.specialCachedValue == null) ? null : new FormulaSpecialCachedValue(other.specialCachedValue);
	}

	public FormulaRecord(RecordInputStream ris) {
		super(ris);
		long valueLongBits  = ris.readLong();
		field_5_options = ris.readShort();
		specialCachedValue = FormulaSpecialCachedValue.create(valueLongBits);
		if (specialCachedValue == null) {
			field_4_value = Double.longBitsToDouble(valueLongBits);
		}

		field_6_zero = ris.readInt();

		int field_7_expression_len = ris.readShort(); // this length does not include any extra array data
		int nBytesAvailable = ris.available();
		field_8_parsed_expr = Formula.read(field_7_expression_len, ris, nBytesAvailable);
	}

	/**
	 * set the calculated value of the formula
	 *
	 * @param value  calculated value
	 */
	public void setValue(double value) {
		field_4_value = value;
		specialCachedValue = null;
	}

	public void setCachedResultTypeEmptyString() {
		specialCachedValue = FormulaSpecialCachedValue.createCachedEmptyValue();
	}
	public void setCachedResultTypeString() {
		specialCachedValue = FormulaSpecialCachedValue.createForString();
	}
	public void setCachedResultErrorCode(int errorCode) {
		specialCachedValue = FormulaSpecialCachedValue.createCachedErrorCode(errorCode);
	}
	public void setCachedResultBoolean(boolean value) {
		specialCachedValue = FormulaSpecialCachedValue.createCachedBoolean(value);
	}
	/**
	 * @return <code>true</code> if this {@link FormulaRecord} is followed by a
	 *  {@link StringRecord} representing the cached text result of the formula
	 *  evaluation.
	 */
	public boolean hasCachedResultString() {
		return specialCachedValue != null &&
				specialCachedValue.getTypeCode() == FormulaSpecialCachedValue.STRING;
	}

	/**
	 * @deprecated POI 5.0.0, will be removed in 6.0, use getCachedResultTypeEnum until switch to enum is fully done
	 */
	@Deprecated
	@Removal(version = "6.0.0")
	public int getCachedResultType() {
		if (specialCachedValue == null) {
			return CellType.NUMERIC.getCode();
		}
		return specialCachedValue.getValueType();
	}

	/**
	 * Returns the type of the cached result
	 * @return A CellType
	 * @since POI 5.0.0
	 */
	public CellType getCachedResultTypeEnum() {
		if (specialCachedValue == null) {
			return CellType.NUMERIC;
		}
		return specialCachedValue.getValueTypeEnum();
	}

	public boolean getCachedBooleanValue() {
		return specialCachedValue.getBooleanValue();
	}
	public int getCachedErrorValue() {
		return specialCachedValue.getErrorValue();
	}


	/**
	 * set the option flags
	 *
	 * @param options  bitmask
	 */
	public void setOptions(short options) {
		field_5_options = options;
	}

	/**
	 * get the calculated value of the formula
	 *
	 * @return calculated value
	 */
	public double getValue() {
		return field_4_value;
	}

	/**
	 * get the option flags
	 *
	 * @return bitmask
	 */
	public short getOptions() {
		return field_5_options;
	}

	public boolean isSharedFormula() {
		return sharedFormula.isSet(field_5_options);
	}
	public void setSharedFormula(boolean flag) {
		field_5_options =
			sharedFormula.setShortBoolean(field_5_options, flag);
	}

	public boolean isAlwaysCalc() {
		return alwaysCalc.isSet(field_5_options);
	}
	public void setAlwaysCalc(boolean flag) {
		field_5_options =
			alwaysCalc.setShortBoolean(field_5_options, flag);
	}

	public boolean isCalcOnLoad() {
		return calcOnLoad.isSet(field_5_options);
	}
	public void setCalcOnLoad(boolean flag) {
		field_5_options =
			calcOnLoad.setShortBoolean(field_5_options, flag);
	}

	/**
	 * @return the formula tokens. never <code>null</code>
	 */
	public Ptg[] getParsedExpression() {
		return field_8_parsed_expr.getTokens();
	}

	public Formula getFormula() {
		return field_8_parsed_expr;
	}

	public void setParsedExpression(Ptg[] ptgs) {
		field_8_parsed_expr = Formula.create(ptgs);
	}

	@Override
    public short getSid() {
		return sid;
	}

	@Override
	protected int getValueDataSize() {
		return FIXED_SIZE + field_8_parsed_expr.getEncodedSize();
	}
	@Override
	protected void serializeValue(LittleEndianOutput out) {

		if (specialCachedValue == null) {
			out.writeDouble(field_4_value);
		} else {
			specialCachedValue.serialize(out);
		}

		out.writeShort(getOptions());

		out.writeInt(field_6_zero); // may as well write original data back so as to minimise differences from original
		field_8_parsed_expr.serialize(out);
	}

	@Override
	protected String getRecordName() {
		return "FORMULA";
	}

	@Override
	public FormulaRecord copy() {
		return new FormulaRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.FORMULA;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"base", super::getGenericProperties,
			"options", this::getOptions,
			"alwaysCalc", this::isAlwaysCalc,
			"calcOnLoad", this::isCalcOnLoad,
			"shared", this::isSharedFormula,
			"zero", () -> field_6_zero,
			"value", () -> specialCachedValue == null ? field_4_value : specialCachedValue,
			"formula", this::getFormula
		);
	}
}

