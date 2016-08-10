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

import org.apache.poi.hssf.record.common.FtrHeader;
import org.apache.poi.hssf.record.common.FutureRecord;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Conditional Formatting Header v12 record CFHEADER12 (0x0879),
 *  for conditional formattings introduced in Excel 2007 and newer.
 */
public final class CFHeader12Record extends CFHeaderBase implements FutureRecord, Cloneable {
    public static final short sid = 0x0879;

    private FtrHeader futureHeader;

    /** Creates new CFHeaderRecord */
    public CFHeader12Record() {
        createEmpty();
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);
    }
    public CFHeader12Record(CellRangeAddress[] regions, int nRules) {
        super(regions, nRules);
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);
    }
    public CFHeader12Record(RecordInputStream in) {
        futureHeader = new FtrHeader(in);
        read(in);
    }

    @Override
    protected String getRecordName() {
        return "CFHEADER12";
    }

    protected int getDataSize() {
        return FtrHeader.getDataSize() + super.getDataSize();
    }

    public void serialize(LittleEndianOutput out) {
        // Sync the associated range
        futureHeader.setAssociatedRange(getEnclosingCellRange());
        // Write the future header first
        futureHeader.serialize(out);
        // Then the rest of the CF Header details
        super.serialize(out);
    }

    public short getSid() {
        return sid;
    }

    public short getFutureRecordType() {
        return futureHeader.getRecordType();
    }
    public FtrHeader getFutureHeader() {
        return futureHeader;
    }
    public CellRangeAddress getAssociatedRange() {
        return futureHeader.getAssociatedRange();
    }
    
    @Override
    public CFHeader12Record clone() {
        CFHeader12Record result = new CFHeader12Record();
        result.futureHeader = (FtrHeader)futureHeader.clone();
        super.copyTo(result);
        return result;
    }
}
