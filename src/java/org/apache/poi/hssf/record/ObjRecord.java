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
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * OBJRECORD (0x005D)<p/>
 * 
 * The obj record is used to hold various graphic objects and controls.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class ObjRecord extends Record {
	public final static short sid = 0x005D;
	
	private List subrecords;
	/** used when POI has no idea what is going on */
	private byte[] _uninterpretedData;

	//00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
	//00000010 26 01 00 00 00 00 00 00 00 00                   &.........


	public ObjRecord() {
		subrecords = new ArrayList(2);
		// TODO - ensure 2 sub-records (ftCmo 15h, and ftEnd 00h) are always created
	}

	public ObjRecord(RecordInputStream in) {
		// TODO - problems with OBJ sub-records stream
		// MS spec says first sub-records is always CommonObjectDataSubRecord,
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
			return;
		}

//		System.out.println(HexDump.toHex(subRecordData));

		subrecords = new ArrayList();
		ByteArrayInputStream bais = new ByteArrayInputStream(subRecordData);
		LittleEndianInputStream subRecStream = new LittleEndianInputStream(bais);
		while (true) {
			SubRecord subRecord = SubRecord.createSubRecord(subRecStream);
			subrecords.add(subRecord);
			if (subRecord instanceof EndSubRecord) {
				break;
			}
		}
		if (bais.available() > 0) {
			// earlier versions of the code had allowances for padding
			// At present (Oct-2008), no unit test samples exhibit such padding
			String msg = "Leftover " + bais.available() 
				+ " bytes in subrecord data " + HexDump.toHex(subRecordData);
			throw new RecordFormatException(msg);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[OBJ]\n");
		for (int i = 0; i < subrecords.size(); i++) {
			SubRecord record = (SubRecord) subrecords.get(i);
			sb.append("SUBRECORD: ").append(record.toString());
		}
		sb.append("[/OBJ]\n");
		return sb.toString();
	}
	
	private int getDataSize() {
		if (_uninterpretedData != null) {
			return _uninterpretedData.length;
		}
		int size = 0;
		for (int i=subrecords.size()-1; i>=0; i--) {
			SubRecord record = (SubRecord) subrecords.get(i);
			size += record.getDataSize()+4;
		}
		return size;
	}

	public int serialize(int offset, byte[] data) {
		int dataSize = getDataSize();

		LittleEndian.putUShort(data, 0 + offset, sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);

		byte[] subRecordBytes;
		if (_uninterpretedData == null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(dataSize);
			LittleEndianOutput leo = new LittleEndianOutputStream(baos);

			for (int i = 0; i < subrecords.size(); i++) {
				SubRecord record = (SubRecord) subrecords.get(i);
				record.serialize(leo);
			}
			// padding
			while (baos.size() < dataSize) {
				baos.write(0);
			}
			subRecordBytes = baos.toByteArray();
		} else {
			subRecordBytes = _uninterpretedData;
		}
		System.arraycopy(subRecordBytes, 0, data, offset + 4, dataSize);
		return 4 + dataSize;
	}

	public int getRecordSize() {
		return 4 + getDataSize();
	}

	public short getSid() {
		return sid;
	}

	public List getSubRecords() {
		return subrecords;
	}

	public void clearSubRecords() {
		subrecords.clear();
	}

	public void addSubRecord(int index, Object element) {
		subrecords.add(index, element);
	}

	public boolean addSubRecord(Object o) {
		return subrecords.add(o);
	}

	public Object clone() {
		ObjRecord rec = new ObjRecord();

		for (int i = 0; i < subrecords.size(); i++) {
			SubRecord record = (SubRecord) subrecords.get(i);
			rec.addSubRecord(record.clone());
		}
		return rec;
	}
}
