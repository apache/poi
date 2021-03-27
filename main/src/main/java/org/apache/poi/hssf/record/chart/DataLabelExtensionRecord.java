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

package org.apache.poi.hssf.record.chart;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * DATALABEXT - Chart Data Label Extension (0x086A)
 */
public final class DataLabelExtensionRecord extends StandardRecord {
	public static final short sid = 0x086A;

	private int rt;
	private int grbitFrt;
	private final byte[] unused = new byte[8];

	public DataLabelExtensionRecord(DataLabelExtensionRecord other) {
		super(other);
		rt = other.rt;
		grbitFrt = other.grbitFrt;
		System.arraycopy(other.unused, 0, unused, 0, unused.length);
	}

	public DataLabelExtensionRecord(RecordInputStream in) {
		rt = in.readShort();
		grbitFrt = in.readShort();
		in.readFully(unused);
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 8;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	protected void serialize(LittleEndianOutput out) {
		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.write(unused);
	}

	@Override
	public DataLabelExtensionRecord copy() {
		return new DataLabelExtensionRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.DATA_LABEL_EXTENSION;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"rt", () -> rt,
			"grbitFrt", () -> grbitFrt,
			"unused", () -> unused
		);
	}
}
