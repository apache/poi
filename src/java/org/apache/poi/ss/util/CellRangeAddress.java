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

package org.apache.poi.ss.util;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * Note - {@link SelectionRecord} uses the BIFF5 version of this structure
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class CellRangeAddress extends CellRangeAddressBase {
	/*
	 * TODO - replace  org.apache.poi.hssf.util.Region
	 */
	public static final int ENCODED_SIZE = 8;

	public CellRangeAddress(int firstRow, int lastRow, int firstCol, int lastCol) {
		super(firstRow, lastRow, firstCol, lastCol);
	}

	/**
	 * @deprecated use {@link #serialize(LittleEndianOutput)}
	 */
	public int serialize(int offset, byte[] data) {
		serialize(new LittleEndianByteArrayOutputStream(data, offset, ENCODED_SIZE));
		return ENCODED_SIZE;
	}
	public void serialize(LittleEndianOutput out) {
		out.writeShort(getFirstRow());
		out.writeShort(getLastRow());
		out.writeShort(getFirstColumn());
		out.writeShort(getLastColumn());
	}
	
	public CellRangeAddress(RecordInputStream in) {
		super(readUShortAndCheck(in), in.readUShort(), in.readUShort(), in.readUShort());
	}

	private static int readUShortAndCheck(RecordInputStream in) {
		if (in.remaining() < ENCODED_SIZE) {
			// Ran out of data
			throw new RuntimeException("Ran out of data reading CellRangeAddress");
		}
		return in.readUShort();
	}

	public CellRangeAddress copy() {
		return new CellRangeAddress(getFirstRow(), getLastRow(), getFirstColumn(), getLastColumn());
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}

    public String formatAsString() {
        StringBuffer sb = new StringBuffer();
        CellReference cellRefFrom = new CellReference(getFirstRow(), getFirstColumn());
        CellReference cellRefTo = new CellReference(getLastRow(), getLastColumn());
        sb.append(cellRefFrom.formatAsString());
        sb.append(':');
        sb.append(cellRefTo.formatAsString());
        return sb.toString();
    }

    public static CellRangeAddress valueOf(String ref) {
        int sep = ref.indexOf(":");
        CellReference cellFrom = new CellReference(ref.substring(0, sep));
        CellReference cellTo = new CellReference(ref.substring(sep + 1));
        return new CellRangeAddress(cellFrom.getRow(), cellTo.getRow(), cellFrom.getCol(), cellTo.getCol());
    }
}
