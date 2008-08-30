/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.util;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.util.LittleEndian;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * Like {@link CellRangeAddress} except column fields are 8-bit.
 * 
 * @author Josh Micich
 */
public final class CellRangeAddress8Bit extends CellRangeAddressBase {

	public static final int ENCODED_SIZE = 6;

	public CellRangeAddress8Bit(int firstRow, int lastRow, int firstCol, int lastCol) {
		super(firstRow, lastRow, firstCol, lastCol);
	}

	public CellRangeAddress8Bit(RecordInputStream in) {
		super(readUShortAndCheck(in), in.readUShort(), in.readUByte(), in.readUByte());
	}

	private static int readUShortAndCheck(RecordInputStream in) {
		if (in.remaining() < ENCODED_SIZE) {
			// Ran out of data
			throw new RuntimeException("Ran out of data reading CellRangeAddress");
		}
		return in.readUShort();
	}

	public int serialize(int offset, byte[] data) {
		LittleEndian.putUShort(data, offset + 0, getFirstRow());
		LittleEndian.putUShort(data, offset + 2, getLastRow());
		LittleEndian.putByte(data, offset + 4, getFirstColumn());
		LittleEndian.putByte(data, offset + 5, getLastColumn());
		return ENCODED_SIZE;
	}
	
	public CellRangeAddress8Bit copy() {
		return new CellRangeAddress8Bit(getFirstRow(), getLastRow(), getFirstColumn(), getLastColumn());
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}
}
