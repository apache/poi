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

package org.apache.poi.hssf.eventusermodel.dummyrecord;


/**
 * A dummy record to indicate that we've now had the last
 *  cell record for this row.
 */
public final class LastCellOfRowDummyRecord extends DummyRecordBase {
	private final int row;
	private final int lastColumnNumber;
	
	public LastCellOfRowDummyRecord(int row, int lastColumnNumber) {
		this.row = row;
		this.lastColumnNumber = lastColumnNumber;
	}
	
	/**
	 * Returns the (0 based) number of the row we are
	 *  currently working on.
	 *  
	 * @return the (0 based) number of the row
	 */
	public int getRow() {
	    return row;
    }
	
	/**
	 * Returns the (0 based) number of the last column
	 *  seen for this row. You should have already been
	 *  called with that record.
	 * This is -1 in the case of there being no columns
	 *  for the row.
	 *  
	 * @return the (0 based) number of the last column
	 */
	public int getLastColumnNumber() {
	    return lastColumnNumber;
    }
	
	@Override
	public String toString() {
	    return "End-of-Row for Row=" + row + " at Column=" + lastColumnNumber;
	}
}
