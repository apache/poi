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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.LittleEndianOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * Subrecords are part of the OBJ class.
 */
public abstract class SubRecord {
	protected SubRecord() {
		// no fields to initialise
	}

    /**
     * read a sub-record from the supplied stream
     *
     * @param in    the stream to read from
     * @param cmoOt the objectType field of the containing CommonObjectDataSubRecord,
     *   we need it to propagate to next sub-records as it defines what data follows
     * @return the created sub-record
     */
    public static SubRecord createSubRecord(LittleEndianInput in, int cmoOt) {
		int sid = in.readUShort();
		int secondUShort = in.readUShort(); // Often (but not always) the datasize for the sub-record

		switch (sid) {
			case CommonObjectDataSubRecord.sid:
				return new CommonObjectDataSubRecord(in, secondUShort);
			case EmbeddedObjectRefSubRecord.sid:
				return new EmbeddedObjectRefSubRecord(in, secondUShort);
			case GroupMarkerSubRecord.sid:
				return new GroupMarkerSubRecord(in, secondUShort);
			case EndSubRecord.sid:
				return new EndSubRecord(in, secondUShort);
			case NoteStructureSubRecord.sid:
				return new NoteStructureSubRecord(in, secondUShort);
			case LbsDataSubRecord.sid:
				return new LbsDataSubRecord(in, secondUShort, cmoOt);
		}
		return new UnknownSubRecord(in, sid, secondUShort);
	}

	/**
	 * @return the size of the data for this record (which is always 4 bytes less than the total
	 * record size).  Note however, that ushort encoded after the record sid is usually but not
	 * always the data size.
	 */
	protected abstract int getDataSize();
	public byte[] serialize() {
		int size = getDataSize() + 4;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
		serialize(new LittleEndianOutputStream(baos));
		if (baos.size() != size) {
			throw new RuntimeException("write size mismatch");
		}
		return baos.toByteArray();
	}

	public abstract void serialize(LittleEndianOutput out);
	public abstract Object clone();

    /**
     * Wether this record terminates the sub-record stream.
     * There are two cases when this method must be overridden and return <code>true</code>
     *  - EndSubRecord (sid = 0x00)
     *  - LbsDataSubRecord (sid = 0x12)
     *
     * @return whether this record is the last in the sub-record stream
     */
    public boolean isTerminating(){
        return false;
    }

    private static final class UnknownSubRecord extends SubRecord {

		private final int _sid;
		private final byte[] _data;

		public UnknownSubRecord(LittleEndianInput in, int sid, int size) {
			_sid = sid;
	    	byte[] buf = new byte[size];
	    	in.readFully(buf);
	        _data = buf;
		}
		protected int getDataSize() {
			return _data.length;
		}
		public void serialize(LittleEndianOutput out) {
			out.writeShort(_sid);
			out.writeShort(_data.length);
			out.write(_data);
		}
		public Object clone() {
			return this;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append("sid=").append(HexDump.shortToHex(_sid));
			sb.append(" size=").append(_data.length);
			sb.append(" : ").append(HexDump.toHex(_data));
			sb.append("]\n");
			return sb.toString();
		}
	}
}
