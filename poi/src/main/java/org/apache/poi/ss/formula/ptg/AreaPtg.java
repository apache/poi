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

/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 */
public final class AreaPtg extends Area2DPtgBase {
	public static final short sid  = 0x25;

	public AreaPtg(int firstRow, int lastRow, int firstColumn, int lastColumn, boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative) {
		super(firstRow, lastRow, firstColumn, lastColumn, firstRowRelative, lastRowRelative, firstColRelative, lastColRelative);
	}

	public AreaPtg(AreaPtg other)  {
		super(other);
	}

	public AreaPtg(LittleEndianInput in)  {
		super(in);
	}

    public AreaPtg(AreaReference arearef) {
        super(arearef);
    }

	@Override
	public byte getSid() {
		return sid;
	}

	@Override
	public AreaPtg copy() {
		return new AreaPtg(this);
	}
}
