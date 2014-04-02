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

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Merged Cells Record (0x00E5)
 * <br/>
 * Description:  Optional record defining a square area of cells to "merged" into one cell. <br>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public final class MergeCellsRecord extends StandardRecord {
    public final static short sid = 0x00E5;
    /** sometimes the regions array is shared with other MergedCellsRecords */ 
    private CellRangeAddress[] _regions;
    private final int _startIndex;
    private final int _numberOfRegions;

    public MergeCellsRecord(CellRangeAddress[] regions, int startIndex, int numberOfRegions) {
		_regions = regions;
		_startIndex = startIndex;
		_numberOfRegions = numberOfRegions;
    }
    /**
     * Constructs a MergedCellsRecord and sets its fields appropriately
     * @param in the RecordInputstream to read the record from
     */
    public MergeCellsRecord(RecordInputStream in) {
     	int nRegions = in.readUShort();
    	CellRangeAddress[] cras = new CellRangeAddress[nRegions];
    	for (int i = 0; i < nRegions; i++) {
			cras[i] = new CellRangeAddress(in);
		}
    	_numberOfRegions = nRegions;
    	_startIndex = 0;
    	_regions = cras;
    }
    /**
     * get the number of merged areas.  If this drops down to 0 you should just go
     * ahead and delete the record.
     * @return number of areas
     */
    public short getNumAreas() {
        return (short)_numberOfRegions;
    }

    /**
     * @return MergedRegion at the given index representing the area that is Merged (r1,c1 - r2,c2)
     */
    public CellRangeAddress getAreaAt(int index) {
        return _regions[_startIndex + index];
    }

    protected int getDataSize() {
		return CellRangeAddressList.getEncodedSize(_numberOfRegions);
	}

    public short getSid() {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        int nItems = _numberOfRegions;
        out.writeShort(nItems);
        for (int i = 0; i < _numberOfRegions; i++) {
			_regions[_startIndex + i].serialize(out);
		}
    }

    public String toString() {
        StringBuffer retval = new StringBuffer();

        retval.append("[MERGEDCELLS]").append("\n");
        retval.append("     .numregions =").append(getNumAreas()).append("\n");
        for (int k = 0; k < _numberOfRegions; k++) {
            CellRangeAddress r = _regions[_startIndex + k];

            retval.append("     .rowfrom =").append(r.getFirstRow()).append("\n");
            retval.append("     .rowto   =").append(r.getLastRow()).append("\n");
            retval.append("     .colfrom =").append(r.getFirstColumn()).append("\n");
            retval.append("     .colto   =").append(r.getLastColumn()).append("\n");
        }
        retval.append("[MERGEDCELLS]").append("\n");
        return retval.toString();
    }

    public Object clone() {
    	int nRegions = _numberOfRegions;
    	CellRangeAddress[] clonedRegions = new CellRangeAddress[nRegions];
		for (int i = 0; i < clonedRegions.length; i++) {
			clonedRegions[i] = _regions[_startIndex + i].copy();
		}
        return new MergeCellsRecord(clonedRegions, 0, nRegions);
    }
}
