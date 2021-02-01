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
 * STARTBLOCK - Chart Future Record Type Start Block (0x0852)
 */
public final class ChartStartBlockRecord extends StandardRecord {
	public static final short sid = 0x0852;

	private short rt;
	private short grbitFrt;
	private short iObjectKind;
	private short iObjectContext;
	private short iObjectInstance1;
	private short iObjectInstance2;

	public ChartStartBlockRecord() {}

	public ChartStartBlockRecord(ChartStartBlockRecord other) {
		super(other);
		rt = other.rt;
		grbitFrt = other.grbitFrt;
		iObjectKind = other.iObjectKind;
		iObjectContext = other.iObjectContext;
		iObjectInstance1 = other.iObjectInstance1;
		iObjectInstance2 = other.iObjectInstance2;
	}

	public ChartStartBlockRecord(RecordInputStream in) {
		rt = in.readShort();
		grbitFrt = in.readShort();
		iObjectKind = in.readShort();
		iObjectContext = in.readShort();
		iObjectInstance1 = in.readShort();
		iObjectInstance2 = in.readShort();
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 2 + 2 + 2 + 2;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public void serialize(LittleEndianOutput out) {
		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.writeShort(iObjectKind);
		out.writeShort(iObjectContext);
		out.writeShort(iObjectInstance1);
		out.writeShort(iObjectInstance2);
	}

	@Override
	public ChartStartBlockRecord copy() {
		return new ChartStartBlockRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.CHART_START_BLOCK;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"rt", () -> rt,
			"grbitFrt", () -> grbitFrt,
			"iObjectKind", () -> iObjectKind,
			"iObjectContext", () -> iObjectContext,
			"iObjectInstance1", () -> iObjectInstance1,
			"iObjectInstance2", () -> iObjectInstance2
		);
	}
}
