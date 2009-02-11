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
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Recalc Id Record (0x01C1)<p/>
 * Description:  This record contains an ID that marks when a worksheet was last
 *               recalculated. It's an optimization Excel uses to determine if it
 *               needs to  recalculate the spreadsheet when it's opened. So far, only
 *               the two values <code>0xC1 0x01 0x00 0x00 0x80 0x38 0x01 0x00</code>
 *               (do not recalculate) and <code>0xC1 0x01 0x00 0x00 0x60 0x69 0x01
 *               0x00</code> have been seen. If the field <code>isNeeded</code> is
 *               set to false (default), then this record is swallowed during the
 *               serialization process<p/>
 * REFERENCE:  http://chicago.sourceforge.net/devel/docs/excel/biff8.html<p/>
 * @author Luc Girardin (luc dot girardin at macrofocus dot com)
 *
 * @see org.apache.poi.hssf.model.Workbook
 */
public final class RecalcIdRecord extends StandardRecord {
    public final static short sid = 0x01C1;
    private final int _reserved0;
    private final int _engineId;

    public RecalcIdRecord(RecordInputStream in) {
    	int rt = in.readUShort();
    	if (rt != sid) {
    		throw new RecordFormatException("expected " + sid + " but got " + rt);
    	}
    	_reserved0 = in.readUShort();
    	_engineId = in.readInt();
    }

    public boolean isNeeded() {
        return true;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[RECALCID]\n");
        buffer.append("    .reserved = ").append(HexDump.shortToHex(_reserved0)).append("\n");
        buffer.append("    .engineId = ").append(HexDump.intToHex(_engineId)).append("\n");
        buffer.append("[/RECALCID]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(_reserved0);
        out.writeInt(_engineId);
    }

    protected int getDataSize() {
        return 8;
    }

    public short getSid() {
        return sid;
    }
}
