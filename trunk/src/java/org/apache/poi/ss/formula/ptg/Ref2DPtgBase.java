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

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * @author Josh Micich
 */
abstract class Ref2DPtgBase extends RefPtgBase {
	private final static int SIZE = 5;


	protected Ref2DPtgBase(int row, int column, boolean isRowRelative, boolean isColumnRelative) {
		setRow(row);
		setColumn(column);
		setRowRelative(isRowRelative);
		setColRelative(isColumnRelative);
	}

	protected Ref2DPtgBase(LittleEndianInput in)  {
		readCoordinates(in);
	}

	protected Ref2DPtgBase(CellReference cr) {
		super(cr);
	}

	@Override
    public void write(LittleEndianOutput out) {
		out.writeByte(getSid() + getPtgClass());
		writeCoordinates(out);
	}

	@Override
    public final String toFormulaString() {
		return formatReferenceAsString();
	}

	protected abstract byte getSid();

	@Override
    public final int getSize() {
		return SIZE;
	}

    @Override
    public final String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" [");
        sb.append(formatReferenceAsString());
        sb.append("]");
        return sb.toString();
    }
}
