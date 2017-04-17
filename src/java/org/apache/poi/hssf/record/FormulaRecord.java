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

import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.*;

/**
 * Formula Record (0x0006).
 * REFERENCE:  PG 317/444 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class FormulaRecord extends CellRecord implements Cloneable {

	public static final short sid = 0x0006;   // docs say 406...because of a bug Microsoft support site article #Q184647)
	private static int FIXED_SIZE = 14; // double + short + int

	private static final BitField alwaysCalc = BitFieldFactory.getInstance(0x0001);
	private static final BitField calcOnLoad = BitFieldFactory.getInstance(0x0002);
	private static final BitField sharedFormula = BitFieldFactory.getInstance(0x0008);

	/**
	 * Manages the cached formula result values of other types besides numeric.
	 * Excel encodes the same 8 bytes that would be field_4_value with various NaN
	 * values that are decoded/encoded by this class. 
	 */
	static final class SpecialCachedValue {
		/** deliberately chosen by Excel in order to encode other values within Double NaNs */
		private static final long BIT_MARKER = 0xFFFF000000000000L;
		private static final int VARIABLE_DATA_LENGTH = 6;
		private static final int DATA_INDEX = 2;

		// FIXME: can these be merged with {@link CellType}?
		// are the numbers specific to the HSSF formula record format or just a poor-man's enum?
		public static final int STRING = 0;
		public static final int BOOLEAN = 1;
		public static final int ERROR_CODE = 2;
		public static final int EMPTY = 3;

		private final byte[] _variableData;

		private SpecialCachedValue(byte[] data) {
			_variableData = data;
		}

		public int getTypeCode() {
			return _variableData[0];
		}

		/**
		 * @return <code>null</code> if the double value encoded by <tt>valueLongBits</tt> 
		 * is a normal (non NaN) double value.
		 */
		public static SpecialCachedValue create(long valueLongBits) {
			if ((BIT_MARKER & valueLongBits) != BIT_MARKER) {
				return null;
			}

			byte[] result = new byte[VARIABLE_DATA_LENGTH];
			long x = valueLongBits;
			for (int i=0; i<VARIABLE_DATA_LENGTH; i++) {
				result[i] = (byte) x;
				x >>= 8;
			}
			switch (result[0]) {
				case STRING:
				case BOOLEAN:
				case ERROR_CODE:
				case EMPTY:
					break;
				default:
					throw new org.apache.poi.util.RecordFormatException("Bad special value code (" + result[0] + ")");
			}
			return new SpecialCachedValue(result);
		}

		public void serialize(LittleEndianOutput out) {
			out.write(_variableData);
			out.writeShort(0xFFFF);
		}

		public String formatDebugString() {
			return formatValue() + ' ' + HexDump.toHex(_variableData);
		}

		private String formatValue() {
			int typeCode = getTypeCode();
			switch (typeCode) {
				case STRING:
					return "<string>";
				case BOOLEAN:
					return getDataValue() == 0 ? "FALSE" : "TRUE";
				case ERROR_CODE:
					return ErrorEval.getText(getDataValue());
				case EMPTY:
					return "<empty>";
			}
			return "#error(type=" + typeCode + ")#";
		}

		private int getDataValue() {
			return _variableData[DATA_INDEX];
		}

		public static SpecialCachedValue createCachedEmptyValue() {
			return create(EMPTY, 0);
		}

		public static SpecialCachedValue createForString() {
			return create(STRING, 0);
		}

		public static SpecialCachedValue createCachedBoolean(boolean b) {
			return create(BOOLEAN, b ? 1 : 0);
		}

		public static SpecialCachedValue createCachedErrorCode(int errorCode) {
			return create(ERROR_CODE, errorCode);
		}

		private static SpecialCachedValue create(int code, int data) {
			byte[] vd = {
					(byte) code,
					0,
					(byte) data,
					0,
					0,
					0,
			};
			return new SpecialCachedValue(vd);
		}

		@Override
        public String toString() {
			return getClass().getName() + '[' + formatValue() + ']';
		}

		public int getValueType() {
			int typeCode = getTypeCode();
			switch (typeCode) {
				case STRING:	 return CellType.STRING.getCode();
				case BOOLEAN:	return CellType.BOOLEAN.getCode();
				case ERROR_CODE: return CellType.ERROR.getCode();
				case EMPTY:	  return CellType.STRING.getCode(); // is this correct?
			}
			throw new IllegalStateException("Unexpected type id (" + typeCode + ")");
		}

		public boolean getBooleanValue() {
			if (getTypeCode() != BOOLEAN) {
				throw new IllegalStateException("Not a boolean cached value - " + formatValue());
			}
			return getDataValue() != 0;
		}

		public int getErrorValue() {
			if (getTypeCode() != ERROR_CODE) {
				throw new IllegalStateException("Not an error cached value - " + formatValue());
			}
			return getDataValue();
		}
	}

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
	private SpecialCachedValue specialCachedValue;

	/** Creates new FormulaRecord */

	public FormulaRecord() {
		field_8_parsed_expr = Formula.create(Ptg.EMPTY_PTG_ARRAY);
	}

	public FormulaRecord(RecordInputStream ris) {
		super(ris);
		long valueLongBits  = ris.readLong();
		field_5_options = ris.readShort();
		specialCachedValue = SpecialCachedValue.create(valueLongBits);
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
		specialCachedValue = SpecialCachedValue.createCachedEmptyValue();
	}
	public void setCachedResultTypeString() {
		specialCachedValue = SpecialCachedValue.createForString();
	}
	public void setCachedResultErrorCode(int errorCode) {
		specialCachedValue = SpecialCachedValue.createCachedErrorCode(errorCode);
	}
	public void setCachedResultBoolean(boolean value) {
		specialCachedValue = SpecialCachedValue.createCachedBoolean(value);
	}
	/**
	 * @return <code>true</code> if this {@link FormulaRecord} is followed by a
	 *  {@link StringRecord} representing the cached text result of the formula
	 *  evaluation.
	 */
	public boolean hasCachedResultString() {
		return specialCachedValue != null &&
				specialCachedValue.getTypeCode() == SpecialCachedValue.STRING;
	}

	public int getCachedResultType() {
		if (specialCachedValue == null) {
			return CellType.NUMERIC.getCode();
		}
		return specialCachedValue.getValueType();
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
	protected void appendValueText(StringBuilder sb) {
		sb.append("  .value	 = ");
		if (specialCachedValue == null) {
			sb.append(field_4_value).append("\n");
		} else {
			sb.append(specialCachedValue.formatDebugString()).append("\n");
		}
		sb.append("  .options   = ").append(HexDump.shortToHex(getOptions())).append("\n");
		sb.append("    .alwaysCalc= ").append(isAlwaysCalc()).append("\n");
		sb.append("    .calcOnLoad= ").append(isCalcOnLoad()).append("\n");
		sb.append("    .shared    = ").append(isSharedFormula()).append("\n");
		sb.append("  .zero      = ").append(HexDump.intToHex(field_6_zero)).append("\n");

		Ptg[] ptgs = field_8_parsed_expr.getTokens();
		for (int k = 0; k < ptgs.length; k++ ) {
			if (k>0) {
				sb.append("\n");
			}
			sb.append("    Ptg[").append(k).append("]=");
			Ptg ptg = ptgs[k];
			sb.append(ptg).append(ptg.getRVAType());
		}
	}

	@Override
    public FormulaRecord clone() {
		FormulaRecord rec = new FormulaRecord();
		copyBaseFields(rec);
		rec.field_4_value = field_4_value;
		rec.field_5_options = field_5_options;
		rec.field_6_zero = field_6_zero;
		rec.field_8_parsed_expr = field_8_parsed_expr;
		rec.specialCachedValue = specialCachedValue;
		return rec;
	}
}

