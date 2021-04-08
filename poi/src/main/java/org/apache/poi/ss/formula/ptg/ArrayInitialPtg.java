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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.formula.constant.ConstantValueParser;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Represents the initial plain tArray token (without the constant data that trails the whole
 * formula).  Objects of this class are only temporary and cannot be used as {@link Ptg}s.
 * These temporary objects get converted to {@link ArrayInitialPtg} by the
 * {@link #finishReading(LittleEndianInput)} method.
 */
final class ArrayInitialPtg extends Ptg {
	private final int _reserved0;
	private final int _reserved1;
	private final int _reserved2;

	public ArrayInitialPtg(LittleEndianInput in) {
		_reserved0 = in.readInt();
		_reserved1 = in.readUShort();
		_reserved2 = in.readUByte();
	}
	private static RuntimeException invalid() {
		throw new IllegalStateException("This object is a partially initialised tArray, and cannot be used as a Ptg");
	}
	public byte getDefaultOperandClass() {
		throw invalid();
	}
	public int getSize() {
		return ArrayPtg.PLAIN_TOKEN_SIZE;
	}
	public boolean isBaseToken() {
		return false;
	}
	public String toFormulaString() {
		throw invalid();
	}
	public void write(LittleEndianOutput out) {
		throw invalid();
	}
	/**
	 * Read in the actual token (array) values. This occurs
	 * AFTER the last Ptg in the expression.
	 * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
	 */
	public ArrayPtg finishReading(LittleEndianInput in) {
		int nColumns = in.readUByte();
		short nRows = in.readShort();
		//The token_1_columns and token_2_rows do not follow the documentation.
		//The number of physical rows and columns is actually +1 of these values.
		//Which is not explicitly documented.
		nColumns++;
		nRows++;

		int totalCount = nRows * nColumns;
		Object[] arrayValues = ConstantValueParser.parse(in, totalCount);

		ArrayPtg result = new ArrayPtg(_reserved0, _reserved1, _reserved2, nColumns, nRows, arrayValues);
		result.setClass(getPtgClass());
		return result;
	}

	@Override
	public ArrayInitialPtg copy() {
		// immutable
		return this;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"reserved0", () -> _reserved0,
			"reserved1", () -> _reserved1,
			"reserved2", () -> _reserved2
		);
	}

	@Override
	public byte getSid() {
		return -1;
	}
}
