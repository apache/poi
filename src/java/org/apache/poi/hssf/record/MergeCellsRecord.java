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

import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.util.LittleEndian;

/**
 * Title: Merged Cells Record (0x00E5)
 * <br/>
 * Description:  Optional record defining a square area of cells to "merged" into
 *               one cell. <br>
 * REFERENCE:  NONE (UNDOCUMENTED PRESENTLY) <br>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */
public final class MergeCellsRecord extends Record {
    public final static short sid = 0x00E5;
    private CellRangeAddressList _regions;

    /** 
     * Creates an empty <tt>MergedCellsRecord</tt>
     */
    public MergeCellsRecord() {
    	_regions = new CellRangeAddressList();
    }

    /**
     * Constructs a MergedCellsRecord and sets its fields appropriately
     * @param in the RecordInputstream to read the record from
     */
    public MergeCellsRecord(RecordInputStream in) {
        super(in);
    }

    protected void fillFields(RecordInputStream in) {
    	_regions = new CellRangeAddressList(in);
    }

    /**
     * get the number of merged areas.  If this drops down to 0 you should just go
     * ahead and delete the record.
     * @return number of areas
     */
    public short getNumAreas() {
        return (short)_regions.countRanges();
    }

    /**
     * Add an area to consider a merged cell.  The index returned is only gauranteed to
     * be correct provided you do not add ahead of or remove ahead of it  (in which case
     * you should increment or decrement appropriately....in other words its an arrayList)
     *
     * @param firstRow - the upper left hand corner's row
     * @param firstCol - the upper left hand corner's col
     * @param lastRow - the lower right hand corner's row
     * @param lastCol - the lower right hand corner's col
     * @return new index of said area (don't depend on it if you add/remove)
     */
    public void addArea(int firstRow, int firstCol, int lastRow, int lastCol) {
    	_regions.addCellRangeAddress(firstRow, firstCol, lastRow, lastCol);
    }

    /**
     * essentially unmerge the cells in the "area" stored at the passed in index
     * @param areaIndex
     */
    public void removeAreaAt(int areaIndex) {
        _regions.remove(areaIndex);
    }

    /**
     * @return MergedRegion at the given index representing the area that is Merged (r1,c1 - r2,c2)
     */
    public CellRangeAddress getAreaAt(int index) {
        return _regions.getCellRangeAddress(index);
    }

    public int getRecordSize() {
    	return 4 + _regions.getSize();
    }

    public short getSid() {
        return sid;
    }

    public int serialize(int offset, byte [] data) {
        int dataSize = _regions.getSize();

        LittleEndian.putShort(data, offset + 0, sid);
        LittleEndian.putUShort(data, offset + 2, dataSize);
        _regions.serialize(offset + 4, data);
        return 4 + dataSize;
    }

    public String toString() {
        StringBuffer retval = new StringBuffer();

        retval.append("[MERGEDCELLS]").append("\n");
        retval.append("     .sid        =").append(sid).append("\n");
        retval.append("     .numregions =").append(getNumAreas())
            .append("\n");
        for (int k = 0; k < _regions.countRanges(); k++) {
            CellRangeAddress region = _regions.getCellRangeAddress(k);

            retval.append("     .rowfrom    =").append(region.getFirstRow())
                .append("\n");
            retval.append("     .colfrom    =").append(region.getFirstColumn())
                .append("\n");
            retval.append("     .rowto      =").append(region.getLastRow())
                .append("\n");
            retval.append("     .colto      =").append(region.getLastColumn())
                .append("\n");
        }
        retval.append("[MERGEDCELLS]").append("\n");
        return retval.toString();
    }

    protected void validateSid(short id) {
        if (id != sid) {
            throw new RecordFormatException("NOT A MERGEDCELLS RECORD!! "
                                            + id);
        }
    }

    public Object clone() {
        MergeCellsRecord rec = new MergeCellsRecord();        
        for (int k = 0; k < _regions.countRanges(); k++) {
            CellRangeAddress oldRegion = _regions.getCellRangeAddress(k);
           rec.addArea(oldRegion.getFirstRow(), oldRegion.getFirstColumn(), 
        		   oldRegion.getLastRow(), oldRegion.getLastColumn());
        }
        
        return rec;
    }
}
