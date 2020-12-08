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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Parent of Conditional Formatting Header records,
 *  {@link CFHeaderRecord} and {@link CFHeader12Record}.
 */
public abstract class CFHeaderBase extends StandardRecord {
    private int field_1_numcf;
    private int field_2_need_recalculation_and_id;
    private CellRangeAddress field_3_enclosing_cell_range;
    private CellRangeAddressList field_4_cell_ranges;

    protected CFHeaderBase() {}

    protected CFHeaderBase(CFHeaderBase other) {
        super(other);
        field_1_numcf = other.field_1_numcf;
        field_2_need_recalculation_and_id = other.field_2_need_recalculation_and_id;
        field_3_enclosing_cell_range = other.field_3_enclosing_cell_range.copy();
        field_4_cell_ranges = other.field_4_cell_ranges.copy();
    }

    protected CFHeaderBase(CellRangeAddress[] regions, int nRules) {
        CellRangeAddress[] mergeCellRanges = CellRangeUtil.mergeCellRanges(regions);
        setCellRanges(mergeCellRanges);
        field_1_numcf = nRules;
    }

    protected void createEmpty() {
        field_3_enclosing_cell_range = new CellRangeAddress(0, 0, 0, 0);
        field_4_cell_ranges = new CellRangeAddressList();
    }
    protected void read(RecordInputStream in) {
        field_1_numcf = in.readShort();
        field_2_need_recalculation_and_id = in.readShort();
        field_3_enclosing_cell_range = new CellRangeAddress(in);
        field_4_cell_ranges = new CellRangeAddressList(in);
    }

    public int getNumberOfConditionalFormats() {
        return field_1_numcf;
    }
    public void setNumberOfConditionalFormats(int n) {
        field_1_numcf=n;
    }

    public boolean getNeedRecalculation() {
        // Held on the 1st bit
        return (field_2_need_recalculation_and_id & 1) == 1;
    }
    public void setNeedRecalculation(boolean b) {
        // held on the first bit
        if (b == getNeedRecalculation()) {
            return;
        }

        if (b) {
            field_2_need_recalculation_and_id++;
        } else {
            field_2_need_recalculation_and_id--;
        }
    }

    public int getID() {
        // Remaining 15 bits of field 2
        return field_2_need_recalculation_and_id>>1;
    }
    public void setID(int id) {
        // Remaining 15 bits of field 2
        boolean needsRecalc = getNeedRecalculation();
        field_2_need_recalculation_and_id = (id<<1);
        if (needsRecalc) {
            field_2_need_recalculation_and_id++;
        }
    }

    public CellRangeAddress getEnclosingCellRange() {
        return field_3_enclosing_cell_range;
    }
    public void setEnclosingCellRange(CellRangeAddress cr) {
        field_3_enclosing_cell_range = cr;
    }

    /**
     * Set cell ranges list to a single cell range and
     * modify the enclosing cell range accordingly.
     * @param cellRanges - list of CellRange objects
     */
    public void setCellRanges(CellRangeAddress[] cellRanges) {
        if(cellRanges == null) {
            throw new IllegalArgumentException("cellRanges must not be null");
        }
        CellRangeAddressList cral = new CellRangeAddressList();
        CellRangeAddress enclosingRange = null;
        for (CellRangeAddress cr : cellRanges) {
            enclosingRange = CellRangeUtil.createEnclosingCellRange(cr, enclosingRange);
            cral.addCellRangeAddress(cr);
        }
        field_3_enclosing_cell_range = enclosingRange;
        field_4_cell_ranges = cral;
    }

    public CellRangeAddress[] getCellRanges() {
        return field_4_cell_ranges.getCellRangeAddresses();
    }

    protected abstract String getRecordName();

    protected int getDataSize() {
        return 4 // 2 short fields
             + CellRangeAddress.ENCODED_SIZE
             + field_4_cell_ranges.getSize();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_numcf);
        out.writeShort(field_2_need_recalculation_and_id);
        field_3_enclosing_cell_range.serialize(out);
        field_4_cell_ranges.serialize(out);
    }

    @Override
    public abstract CFHeaderBase copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "id", this::getID,
            "numCF", this::getNumberOfConditionalFormats,
            "needRecalculationAndId", this::getNeedRecalculation,
            "enclosingCellRange", this::getEnclosingCellRange,
            "cfRanges", this::getCellRanges
        );
    }
}
