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

import java.io.ByteArrayOutputStream;

import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * Subrecords are part of the OBJ class.
 */
public abstract class SubRecord {
	protected SubRecord() {
		// no fields to initialise
	}

	public static SubRecord createSubRecord(LittleEndianInput in) {
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
				return new LbsDataSubRecord(in, secondUShort);
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

	// TODO make into a top level class
	// perhaps all SubRecord sublcasses could go to their own package
	private static final class LbsDataSubRecord extends SubRecord {

		public static final int sid = 0x0013;

		private int _unknownShort1;
		private int _unknownInt4;
		private Ptg _linkPtg;
		private Byte _unknownByte6;
		private int _nEntryCount;
		private int _selectedEntryIndex;
		private int _style;
		private int _unknownShort10;
		private int _comboStyle;
		private int _lineCount;
		private int _unknownShort13;

		public LbsDataSubRecord(LittleEndianInput in, int unknownShort1) {
			_unknownShort1 = unknownShort1;
			int linkSize = in.readUShort();
			if (linkSize > 0) {
				int formulaSize = in.readUShort();
				_unknownInt4 = in.readInt();


				byte[] buf = new byte[formulaSize];
				in.readFully(buf);
				_linkPtg = readRefPtg(buf);
				switch (linkSize - formulaSize - 6) {
					case 1:
						_unknownByte6 = new Byte(in.readByte());
						break;
					case 0:
						_unknownByte6 = null;
						break;
					default:
						throw new RecordFormatException("Unexpected leftover bytes");
				}

			} else {
				_unknownInt4 = 0;
				_linkPtg = null;
				_unknownByte6 = null;
			}
			_nEntryCount = in.readUShort();
			_selectedEntryIndex = in.readUShort();
			_style = in.readUShort();
			_unknownShort10 = in.readUShort();
			_comboStyle = in.readUShort();
			_lineCount = in.readUShort();
			_unknownShort13 = in.readUShort();

		}
		protected int getDataSize() {
			int result = 2; // 2 initial shorts

			// optional link formula
			if (_linkPtg != null) {
				result += 2; // encoded Ptg size
				result += 4; // unknown int
				result += _linkPtg.getSize();
				if (_unknownByte6 != null) {
					result += 1;
				}
			}
			result += 7 * 2; // 7 shorts
			return result;
		}
		public void serialize(LittleEndianOutput out) {
			out.writeShort(sid);
			out.writeShort(_unknownShort1); // note - this is *not* the size
			if (_linkPtg == null) {
				out.writeShort(0);
			} else {
				int formulaSize = _linkPtg.getSize();
				int linkSize = formulaSize + 6;
				if (_unknownByte6 != null) {
					linkSize++;
				}
				out.writeShort(linkSize);
				out.writeShort(formulaSize);
				out.writeInt(_unknownInt4);
				_linkPtg.write(out);
				if (_unknownByte6 != null) {
					out.writeByte(_unknownByte6.intValue());
				}
			}
			out.writeShort(_nEntryCount);
			out.writeShort(_selectedEntryIndex);
			out.writeShort(_style);
			out.writeShort(_unknownShort10);
			out.writeShort(_comboStyle);
			out.writeShort(_lineCount);
			out.writeShort(_unknownShort13);
		}
		private static Ptg readRefPtg(byte[] formulaRawBytes) {
			LittleEndianInput in = new LittleEndianByteArrayInputStream(formulaRawBytes);
			byte ptgSid = in.readByte();
			switch(ptgSid) {
				case AreaPtg.sid:   return new AreaPtg(in);
				case Area3DPtg.sid: return new Area3DPtg(in);
				case RefPtg.sid:    return new RefPtg(in);
				case Ref3DPtg.sid:  return new Ref3DPtg(in);
			}
			return null;
		}
		public Object clone() {
			return this;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer(256);

			sb.append("[ftLbsData]\n");
			sb.append("    .unknownShort1 =").append(HexDump.shortToHex(_unknownShort1)).append("\n");
			if (_linkPtg == null) {
				sb.append("  <no link formula>\n");
			} else {
				sb.append("    .unknownInt4   =").append(HexDump.intToHex(_unknownInt4)).append("\n");
				sb.append("    .linkPtg       =").append(_linkPtg.toFormulaString()).append(" (").append(_linkPtg.getRVAType()).append(")").append("\n");
				if (_unknownByte6 != null) {
					sb.append("    .unknownByte6  =").append(HexDump.byteToHex(_unknownByte6.byteValue())).append("\n");
				}
			}
			sb.append("    .nEntryCount   =").append(HexDump.shortToHex(_nEntryCount)).append("\n");
			sb.append("    .selEntryIx    =").append(HexDump.shortToHex(_selectedEntryIndex)).append("\n");
			sb.append("    .style         =").append(HexDump.shortToHex(_style)).append("\n");
			sb.append("    .unknownShort10=").append(HexDump.shortToHex(_unknownShort10)).append("\n");
			sb.append("    .comboStyle    =").append(HexDump.shortToHex(_comboStyle)).append("\n");
			sb.append("    .lineCount     =").append(HexDump.shortToHex(_lineCount)).append("\n");
			sb.append("    .unknownShort13=").append(HexDump.shortToHex(_unknownShort13)).append("\n");
			sb.append("[/ftLbsData]\n");
			return sb.toString();
		}
	}
}
