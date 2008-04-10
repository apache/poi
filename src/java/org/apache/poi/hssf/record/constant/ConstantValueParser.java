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

package org.apache.poi.hssf.record.constant;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.UnicodeString.UnicodeRecordStats;
import org.apache.poi.util.LittleEndian;

/**
 * To support Constant Values (2.5.7) as required by the CRN record.
 * This class should probably also be used for two dimensional arrays which are encoded by 
 * EXTERNALNAME (5.39) records and Array tokens.<p/>
 * TODO - code in ArrayPtg should be merged with this code.  It currently supports only 2 of the constant types
 * 
 * @author Josh Micich
 */
public final class ConstantValueParser {
	// note - these (non-combinable) enum values are sparse.
	private static final int TYPE_EMPTY = 0;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_STRING = 2;
	private static final int TYPE_BOOLEAN = 4; 
	private static final int TYPE_ERROR_CODE = 16; // TODO - update OOO document to include this value
	
	private static final int TRUE_ENCODING = 1; 
	private static final int FALSE_ENCODING = 0;
	
	// TODO - is this the best way to represent 'EMPTY'?
	private static final Object EMPTY_REPRESENTATION = null;

	private ConstantValueParser() {
		// no instances of this class
	}

	public static Object[] parse(RecordInputStream in, int nValues) {
		Object[] result = new Object[nValues];
		for (int i = 0; i < result.length; i++) {
			result[i] = readAConstantValue(in);
		}
		return result;
	}

	private static Object readAConstantValue(RecordInputStream in) {
		byte grbit = in.readByte();
		switch(grbit) {
			case TYPE_EMPTY:
				in.readLong(); // 8 byte 'not used' field
				return EMPTY_REPRESENTATION; 
			case TYPE_NUMBER:
				return new Double(in.readDouble());
			case TYPE_STRING:
				return in.readUnicodeString();
			case TYPE_BOOLEAN:
				return readBoolean(in);
			case TYPE_ERROR_CODE:
				int errCode = in.readUShort();
				// next 6 bytes are unused
				in.readUShort();
				in.readInt();
				return ErrorConstant.valueOf(errCode);
		}
		throw new RuntimeException("Unknown grbit value (" + grbit + ")");
	}

	private static Object readBoolean(RecordInputStream in) {
		byte val = (byte)in.readLong(); // 7 bytes 'not used'
		switch(val) {
			case FALSE_ENCODING:
				return Boolean.FALSE;
			case TRUE_ENCODING:
				return Boolean.TRUE;
		}
		// Don't tolerate unusual boolean encoded values (unless it becomes evident that they occur)
		throw new RuntimeException("unexpected boolean encoding (" + val + ")");
	}

	public static int getEncodedSize(Object[] values) {
		// start with one byte 'type' code for each value
		int result = values.length * 1;
		for (int i = 0; i < values.length; i++) {
			result += getEncodedSize(values[i]);
		}
		return result;
	}

	/**
	 * @return encoded size without the 'type' code byte
	 */
	private static int getEncodedSize(Object object) {
		if(object == EMPTY_REPRESENTATION) {
			return 8;
		}
		Class cls = object.getClass();
		
		if(cls == Boolean.class || cls == Double.class || cls == ErrorConstant.class) {
			return 8;
		}
		UnicodeString strVal = (UnicodeString)object;
		UnicodeRecordStats urs = new UnicodeRecordStats();
		strVal.getRecordSize(urs);
		return urs.recordSize;
	}

	public static void encode(byte[] data, int offset, Object[] values) {
		int currentOffset = offset;
		for (int i = 0; i < values.length; i++) {
			currentOffset += encodeSingleValue(data, currentOffset, values[i]);
		}
	}

	private static int encodeSingleValue(byte[] data, int offset, Object value) {
		if (value == EMPTY_REPRESENTATION) {
			LittleEndian.putByte(data, offset, TYPE_EMPTY);
			LittleEndian.putLong(data, offset+1, 0L);
			return 9;
		}
		if (value instanceof Boolean) {
			Boolean bVal = ((Boolean)value);
			LittleEndian.putByte(data, offset, TYPE_BOOLEAN);
			long longVal = bVal.booleanValue() ? 1L : 0L;
			LittleEndian.putLong(data, offset+1, longVal);
			return 9;
		}
		if (value instanceof Double) {
			Double dVal = (Double) value;
			LittleEndian.putByte(data, offset, TYPE_NUMBER);
			LittleEndian.putDouble(data, offset+1, dVal.doubleValue());
			return 9;
		}
		if (value instanceof UnicodeString) {
			UnicodeString usVal = (UnicodeString) value;
			LittleEndian.putByte(data, offset, TYPE_STRING);
			UnicodeRecordStats urs = new UnicodeRecordStats();
			usVal.serialize(urs, offset +1, data);
			return 1 + urs.recordSize;
		}
		if (value instanceof ErrorConstant) {
			ErrorConstant ecVal = (ErrorConstant) value;
			LittleEndian.putByte(data, offset, TYPE_ERROR_CODE);
			LittleEndian.putUShort(data, offset+1, ecVal.getErrorCode());
			LittleEndian.putUShort(data, offset+3, 0);
			LittleEndian.putInt(data, offset+5, 0);
			return 9;
		}

		throw new IllegalStateException("Unexpected value type (" + value.getClass().getName() + "'");
	}
}
