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
 * Title:        Label SST Record<P>
 * Description:  Refers to a string in the shared string table and is a column
 *               value.  <P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class LabelSSTRecord extends CellRecord {
    public final static short sid = 0xfd;
    private int field_4_sst_index;

    public LabelSSTRecord() {
    	// fields uninitialised
    }

    public LabelSSTRecord(RecordInputStream in) {
        super(in);
        field_4_sst_index = in.readInt();
    }

    /**
     * set the index to the string in the SSTRecord
     *
     * @param index - of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */
    public void setSSTIndex(int index) {
        field_4_sst_index = index;
    }


    /**
     * get the index to the string in the SSTRecord
     *
     * @return index of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */
    public int getSSTIndex() {
        return field_4_sst_index;
    }
    
    @Override
    protected String getRecordName() {
    	return "LABELSST";
    }

    @Override
    protected void appendValueText(StringBuilder sb) {
		sb.append("  .sstIndex = ");
    	sb.append(HexDump.shortToHex(getXFIndex()));
    }
    @Override
    protected void serializeValue(LittleEndianOutput out) {
        out.writeInt(getSSTIndex());
    }

    @Override
    protected int getValueDataSize() {
        return 4;
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
      LabelSSTRecord rec = new LabelSSTRecord();
      copyBaseFields(rec);
      rec.field_4_sst_index = field_4_sst_index;
      return rec;
    }
}
