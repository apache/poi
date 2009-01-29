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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * OBJRECORD (0x005D)<p/>
 * 
 * The obj record is used to hold various graphic objects and controls.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class ObjRecord extends Record {
	public final static short sid = 0x005D;

	private static final int NORMAL_PAD_ALIGNMENT = 2;
	private static int MAX_PAD_ALIGNMENT = 4;
	
	private List<SubRecord> subrecords;
	/** used when POI has no idea what is going on */
	private final byte[] _uninterpretedData;
	/**
	 * Excel seems to tolerate padding to quad or double byte length
	 */
	private boolean _isPaddedToQuadByteMultiple;

	//00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
	//00000010 26 01 00 00 00 00 00 00 00 00                   &.........


	public ObjRecord() {
		subrecords = new ArrayList<SubRecord>(2);
		// TODO - ensure 2 sub-records (ftCmo 15h, and ftEnd 00h) are always created
		_uninterpretedData = null;
	}

	public ObjRecord(RecordInputStream in) {
		// TODO - problems with OBJ sub-records stream
		// MS spec says first sub-record is always CommonObjectDataSubRecord,
		// and last is
		// always EndSubRecord. OOO spec does not mention ObjRecord(0x005D).
		// Existing POI test data seems to violate that rule. Some test data
		// seems to contain
		// garbage, and a crash is only averted by stopping at what looks like
		// the 'EndSubRecord'

		// Check if this can be continued, if so then the
		// following wont work properly
		byte[] subRecordData = in.readRemainder();
		if (LittleEndian.getUShort(subRecordData, 0) != CommonObjectDataSubRecord.sid) {
			// seems to occur in just one junit on "OddStyleRecord.xls" (file created by CrystalReports)
			// Excel tolerates the funny ObjRecord, and replaces it with a corrected version
			// The exact logic/reasoning is not yet understood
			_uninterpretedData = subRecordData;
			subrecords = null;
			return;
		}
		if (subRecordData.length % 2 != 0) {
			String msg = "Unexpected length of subRecordData : " + HexDump.toHex(subRecordData);
			throw new RecordFormatException(msg);
		}

//		System.out.println(HexDump.toHex(subRecordData));

		subrecords = new ArrayList<SubRecord>();
		ByteArrayInputStream bais = new ByteArrayInputStream(subRecordData);
		LittleEndianInputStream subRecStream = new LittleEndianInputStream(bais);
		while (true) {
			SubRecord subRecord = SubRecord.createSubRecord(subRecStream);
			subrecords.add(subRecord);
			if (subRecord instanceof EndSubRecord) {
				break;
			}
		}
		int nRemainingBytes = bais.available();
		if (nRemainingBytes > 0) {
			// At present (Oct-2008), most unit test samples have (subRecordData.length % 2 == 0)
			_isPaddedToQuadByteMultiple = subRecordData.length % MAX_PAD_ALIGNMENT == 0;
			if (nRemainingBytes >= (_isPaddedToQuadByteMultiple ? MAX_PAD_ALIGNMENT : NORMAL_PAD_ALIGNMENT)) {
				if (!canPaddingBeDiscarded(subRecordData, nRemainingBytes)) {
					String msg = "Leftover " + nRemainingBytes 
						+ " bytes in subrecord data " + HexDump.toHex(subRecordData);
					throw new RecordFormatException(msg);
				}
				_isPaddedToQuadByteMultiple = false;
			}
		} else {
			_isPaddedToQuadByteMultiple = false;
		}
		_uninterpretedData = null;
	}

	/**
	 * Some XLS files have ObjRecords with nearly 8Kb of excessive padding. These were probably
	 * written by a version of POI (around 3.1) which incorrectly interpreted the second short of
	 * the ftLbs subrecord (0x1FEE) as a length, and read that many bytes as padding (other bugs
	 * helped allow this to occur).
	 * 
	 * Excel reads files with this excessive padding OK, truncating the over-sized ObjRecord back
	 * to the its proper size.  POI does the same.
	 */
	private static boolean canPaddingBeDiscarded(byte[] data, int nRemainingBytes) {
		if (data.length < 0x1FEE) {
			// this looks like a different problem
			return false;
		}
		// make sure none of the padding looks important
		for(int i=data.length-nRemainingBytes; i<data.length; i++) {
			if (data[i] != 0x00) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[OBJ]\n");
		for (int i = 0; i < subrecords.size(); i++) {
			SubRecord record = subrecords.get(i);
			sb.append("SUBRECORD: ").append(record.toString());
		}
		sb.append("[/OBJ]\n");
		return sb.toString();
	}
	
	public int getRecordSize() {
		if (_uninterpretedData != null) {
			return _uninterpretedData.length + 4;
		}
		int size = 0;
		for (int i=subrecords.size()-1; i>=0; i--) {
			SubRecord record = subrecords.get(i);
			size += record.getDataSize()+4;
		}
		if (_isPaddedToQuadByteMultiple) {
			while (size % MAX_PAD_ALIGNMENT != 0) {
				size++;
			}
		} else {
			while (size % NORMAL_PAD_ALIGNMENT != 0) {
				size++;
			}
		}
		return size + 4;
	}

	public int serialize(int offset, byte[] data) {
		int recSize = getRecordSize();
		int dataSize = recSize - 4;
		LittleEndianByteArrayOutputStream out = new LittleEndianByteArrayOutputStream(data, offset, recSize);

		out.writeShort(sid);
		out.writeShort(dataSize);

		if (_uninterpretedData == null) {

			for (int i = 0; i < subrecords.size(); i++) {
				SubRecord record = subrecords.get(i);
				record.serialize(out);
			}
			int expectedEndIx = offset+dataSize;
			// padding
			while (out.getWriteIndex() < expectedEndIx) {
				out.writeByte(0);
			}
		} else {
			out.write(_uninterpretedData);
		}
		return recSize;
	}

	public short getSid() {
		return sid;
	}

	public List<SubRecord> getSubRecords() {
		return subrecords;
	}

	public void clearSubRecords() {
		subrecords.clear();
	}

	public void addSubRecord(int index, SubRecord element) {
		subrecords.add(index, element);
	}

	public boolean addSubRecord(SubRecord o) {
		return subrecords.add(o);
	}

	public Object clone() {
		ObjRecord rec = new ObjRecord();

		for (int i = 0; i < subrecords.size(); i++) {
			SubRecord record = subrecords.get(i);
			rec.addSubRecord((SubRecord) record.clone());
		}
		return rec;
	}
}
