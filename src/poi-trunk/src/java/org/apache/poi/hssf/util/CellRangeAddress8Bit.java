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

package org.apache.poi.hssf.util;

import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p>
 *
 * Implements a CellRangeAddress with 8-but column fields.
 */
public final class CellRangeAddress8Bit extends CellRangeAddressBase {

	public static final int ENCODED_SIZE = 6;

	public CellRangeAddress8Bit(int firstRow, int lastRow, int firstCol, int lastCol) {
		super(firstRow, lastRow, firstCol, lastCol);
	}

	public CellRangeAddress8Bit(LittleEndianInput in) {
		super(readUShortAndCheck(in), in.readUShort(), in.readUByte(), in.readUByte());
	}

	private static int readUShortAndCheck(LittleEndianInput in) {
		if (in.available() < ENCODED_SIZE) {
			// Ran out of data
			throw new RuntimeException("Ran out of data reading CellRangeAddress");
		}
		return in.readUShort();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(getFirstRow());
		out.writeShort(getLastRow());
		out.writeByte(getFirstColumn());
		out.writeByte(getLastColumn());
	}

	public CellRangeAddress8Bit copy() {
		return new CellRangeAddress8Bit(getFirstRow(), getLastRow(), getFirstColumn(), getLastColumn());
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}
}
