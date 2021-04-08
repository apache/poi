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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Optional record defining a square area of cells to "merged" into one cell.
 */
public final class MergeCellsRecord extends StandardRecord {
    public static final short sid = 0x00E5;

    /** sometimes the regions array is shared with other MergedCellsRecords */
    private final CellRangeAddress[] _regions;
    private final int _startIndex;
    private final int _numberOfRegions;

    public MergeCellsRecord(MergeCellsRecord other) {
        super(other);
        _regions = (other._regions == null) ? null
            : Stream.of(other._regions).map(CellRangeAddress::copy).toArray(CellRangeAddress[]::new);
        _startIndex = other._startIndex;
        _numberOfRegions = other._numberOfRegions;
    }


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
     * @param index the n-th MergedRegion
     *
     * @return MergedRegion at the given index representing the area that is Merged (r1,c1 - r2,c2)
     */
    public CellRangeAddress getAreaAt(int index) {
        return _regions[_startIndex + index];
    }

    @Override
    protected int getDataSize() {
		return CellRangeAddressList.getEncodedSize(_numberOfRegions);
	}

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(_numberOfRegions);
        for (int i = 0; i < _numberOfRegions; i++) {
			_regions[_startIndex + i].serialize(out);
		}
    }

    @Override
    public MergeCellsRecord copy() {
        return new MergeCellsRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.MERGE_CELLS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "numRegions", this::getNumAreas,
            "regions", () -> Arrays.copyOfRange(_regions, _startIndex, _startIndex+_numberOfRegions)
        );
    }
}
