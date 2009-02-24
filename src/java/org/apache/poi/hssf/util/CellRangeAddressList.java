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

import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Implementation of the cell range address lists,like is described
 * in OpenOffice.org's Excel Documentation: excelfileformat.pdf sec 2.5.14 -
 * 'Cell Range Address List'
 * 
 * In BIFF8 there is a common way to store absolute cell range address lists in
 * several records (not formulas). A cell range address list consists of a field
 * with the number of ranges and the list of the range addresses. Each cell
 * range address (called an ADDR structure) contains 4 16-bit-values.
 * </p>
 * 
 * @deprecated use {@link org.apache.poi.ss.util.CellRangeAddressList}
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class CellRangeAddressList extends org.apache.poi.ss.util.CellRangeAddressList {
	public CellRangeAddressList(int firstRow, int lastRow, int firstCol, int lastCol) {
		super(firstRow,lastRow,firstCol,lastCol);
	}
	public CellRangeAddressList() {
		super();
	}

	/**
	 * @param in the RecordInputstream to read the record from
	 */
	public CellRangeAddressList(RecordInputStream in) {
		super();
		int nItems = in.readUShort();

		for (int k = 0; k < nItems; k++) {
			_list.add(new CellRangeAddress(in));
		}
	}
}
