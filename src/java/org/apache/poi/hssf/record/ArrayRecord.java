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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * ARRAY (0x0221)<p/>
 * 
 * Treated in a similar way to SharedFormulaRecord
 * 
 * @author Josh Micich
 */		
public final class ArrayRecord extends SharedValueRecordBase {

	public final static short sid = 0x0221;
	private static final int OPT_ALWAYS_RECALCULATE = 0x0001;
	private static final int OPT_CALCULATE_ON_OPEN  = 0x0002;
	
	private int	_options;
	private int _field3notUsed;
	private Ptg[] _formulaTokens;

	public ArrayRecord(RecordInputStream in) {
		super(in);
		_options = in.readUShort();
		_field3notUsed = in.readInt();
		int formulaLen = in.readUShort();
		_formulaTokens = Ptg.readTokens(formulaLen, in);
	}

	public boolean isAlwaysRecalculate() {
		return (_options & OPT_ALWAYS_RECALCULATE) != 0;
	}
	public boolean isCalculateOnOpen() {
		return (_options & OPT_CALCULATE_ON_OPEN) != 0;
	}

	protected int getExtraDataSize() {
		return 2 + 4
			+ 2 + Ptg.getEncodedSize(_formulaTokens);
	}
	protected void serializeExtraData(int offset, byte[] data) {
		int pos = offset;
		LittleEndian.putUShort(data, pos, _options);
		pos+=2;
		LittleEndian.putInt(data, pos, _field3notUsed);
		pos+=4;
		int tokenSize = Ptg.getEncodedSizeWithoutArrayData(_formulaTokens);
		LittleEndian.putUShort(data, pos, tokenSize);
		pos+=2;
		Ptg.serializePtgs(_formulaTokens, data, pos);
	}

	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append(" [ARRAY]\n");
		sb.append(" range=").append(getRange().toString()).append("\n");
		sb.append(" options=").append(HexDump.shortToHex(_options)).append("\n");
		sb.append(" notUsed=").append(HexDump.intToHex(_field3notUsed)).append("\n");
		sb.append(" formula:").append("\n");
		for (int i = 0; i < _formulaTokens.length; i++) {
			Ptg ptg = _formulaTokens[i];
			sb.append(ptg.toString()).append(ptg.getRVAType()).append("\n");
		}
		sb.append("]");
		return sb.toString();
	}
}
