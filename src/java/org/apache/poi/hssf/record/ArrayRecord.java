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
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * ARRAY (0x0221)<p/>
 * 
 * Treated in a similar way to SharedFormulaRecord
 * 
 * @author Josh Micich
 */		
public final class ArrayRecord extends Record {

	public final static short sid = 0x0221;
	private static final int OPT_ALWAYS_RECALCULATE = 0x0001;
	private static final int OPT_CALCULATE_ON_OPEN  = 0x0002;

	private CellRangeAddress8Bit _range;
	
	private int	_options;
	private int _field3notUsed;
	private Ptg[] _formulaTokens;

	public ArrayRecord(RecordInputStream in) {
		super(in);
	}

	public boolean isAlwaysRecalculate() {
		return (_options & OPT_ALWAYS_RECALCULATE) != 0;
	}
	public boolean isCalculateOnOpen() {
		return (_options & OPT_CALCULATE_ON_OPEN) != 0;
	}

	protected void validateSid(short id) {
		if (id != sid) {
			throw new RecordFormatException("NOT A valid Array RECORD");
		}
	}

	private int getDataSize(){
		return CellRangeAddress8Bit.ENCODED_SIZE 
			+ 2 + 4
			+ getFormulaSize();
	}

	public int serialize( int offset, byte[] data ) {
		int dataSize = getDataSize();

		LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);

		int pos = offset+4;
		_range.serialize(pos, data);
		pos += CellRangeAddress8Bit.ENCODED_SIZE;
		LittleEndian.putUShort(data, pos, _options);
		pos+=2;
		LittleEndian.putInt(data, pos, _field3notUsed);
		pos+=4;
		int tokenSize = Ptg.getEncodedSizeWithoutArrayData(_formulaTokens);
		LittleEndian.putUShort(data, pos, tokenSize);
		pos+=2;
		Ptg.serializePtgs(_formulaTokens, data, pos);
		return dataSize + 4;
	}

	private int getFormulaSize() {
		int result = 0;
		for (int i = 0; i < _formulaTokens.length; i++) {
			result += _formulaTokens[i].getSize();
		}
		return result;
	}


	public int getRecordSize(){
		return 4 + getDataSize();
	}


	protected void fillFields(RecordInputStream in) {
		_range = new CellRangeAddress8Bit(in);
		_options = in.readUShort();
		_field3notUsed = in.readInt();
		int formulaLen = in.readUShort();
		_formulaTokens = Ptg.readTokens(formulaLen, in);
	}

	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append(" [ARRAY]\n");
		sb.append(" range=").append(_range.toString()).append("\n");
		sb.append(" options=").append(HexDump.shortToHex(_options)).append("\n");
		sb.append(" notUsed=").append(HexDump.intToHex(_field3notUsed)).append("\n");
		sb.append(" formula:").append("\n");
		for (int i = 0; i < _formulaTokens.length; i++) {
			sb.append(_formulaTokens[i].toString());
		}
		sb.append("]");
		return sb.toString();
	}
}
