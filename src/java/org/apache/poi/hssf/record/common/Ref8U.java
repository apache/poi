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

package org.apache.poi.hssf.record.common;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Ref8U (Cell Range) common record part
 * <P>
 * This record part specifies common way of encoding a
 *  block of cells via first-last row-column.
 */
public final class Ref8U {
	private short firstRow; // zero-based
	private short lastRow;  // zero-based
	private short firstCol; // zero-based
	private short lastCol;  // zero-based

	public Ref8U() {
	}

	public Ref8U(RecordInputStream in) {
		firstRow = in.readShort();
		lastRow  = in.readShort();
		firstCol = in.readShort();
		lastCol  = in.readShort();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [CELL RANGE]\n");
		buffer.append("   Rows " + firstRow + " to " + lastRow);
		buffer.append("   Cols " + firstCol + " to " + lastCol);
		buffer.append(" [/CELL RANGE]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(firstRow);
		out.writeShort(lastRow);
		out.writeShort(firstCol);
		out.writeShort(lastCol);
	}

	protected int getDataSize() {
		return 8;
	}
}
