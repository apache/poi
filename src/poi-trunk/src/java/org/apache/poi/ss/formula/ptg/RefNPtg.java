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

/**
 * RefNPtg
 * @author Jason Height (jheight at apache dot com)
 */
public final class RefNPtg extends Ref2DPtgBase {
	public final static byte sid = 0x2C;

	public RefNPtg(LittleEndianInput in)  {
		super(in);
	}

	protected byte getSid() {
		return sid;
	}

    protected final String formatReferenceAsString() {
        StringBuilder builder = new StringBuilder();

        // The bits in RefNPtg indicate offset, not relative/absolute values!
        if(isRowRelative()) {
            builder.append("RowOffset: ").append(getRow()).append(" ");
        } else {
            builder.append(getRow()+1);
        }
        
        if(isColRelative()) {
            builder.append(" ColOffset: ").append(getColumn());
        } else {
            builder.append(CellReference.convertNumToColString(getColumn()));
        }
        
        return builder.toString();
    }
}
