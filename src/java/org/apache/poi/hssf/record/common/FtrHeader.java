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

package org.apache.poi.hssf.record.common;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: FtrHeader (Future Record Header) common record part
 * <P>
 * This record part specifies a header for a Ftr (Future)
 *  style record, which includes extra attributes above and
 *  beyond those of a traditional record. 
 */
public final class FtrHeader {
	/** This MUST match the type on the containing record */
	private short recordType;
	/** This is a FrtFlags */
	private short grbitFrt;
	/** MUST be 8 bytes and all zero */
	private byte[] reserved;

	public FtrHeader() {
		reserved = new byte[8];
	}

	public FtrHeader(RecordInputStream in) {
		recordType = in.readShort();
		grbitFrt   = in.readShort();
		
		reserved = new byte[8];
		in.read(reserved, 0, 8);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [FUTURE HEADER]\n");
		buffer.append("   Type " + recordType);
		buffer.append("   Flags " + grbitFrt);
		buffer.append(" [/FUTURE HEADER]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(recordType);
		out.writeShort(grbitFrt);
		out.write(reserved);
	}

	public static int getDataSize() {
		return 12;
	}

	public short getRecordType() {
		return recordType;
	}
	public void setRecordType(short recordType) {
		this.recordType = recordType;
	}

	public short getGrbitFrt() {
		return grbitFrt;
	}
	public void setGrbitFrt(short grbitFrt) {
		this.grbitFrt = grbitFrt;
	}

	public byte[] getReserved() {
		return reserved;
	}
	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}
}