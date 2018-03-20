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

import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Common superclass of 2-D area refs
 */
public abstract class Area2DPtgBase extends AreaPtgBase {
	private final static int SIZE = 9;

	protected Area2DPtgBase(int firstRow, int lastRow, int firstColumn, int lastColumn, boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative) {
		super(firstRow, lastRow, firstColumn, lastColumn, firstRowRelative, lastRowRelative, firstColRelative, lastColRelative);
	}
	protected Area2DPtgBase(AreaReference ar) {
		super(ar);
	}

	protected Area2DPtgBase(LittleEndianInput in)  {
		readCoordinates(in);
	}

	protected abstract byte getSid();

	public final void write(LittleEndianOutput out) {
		out.writeByte(getSid() + getPtgClass());
		writeCoordinates(out);
	}

	public final int getSize() {
		return SIZE;
	}

	public final String toFormulaString() {
		return formatReferenceAsString();
	}

	public final String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append(" [");
		sb.append(formatReferenceAsString());
		sb.append("]");
		return sb.toString();
	}
}
