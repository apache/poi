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

package org.apache.poi.ss.usermodel.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;

/**
 * Helper for shifting columns up or down
 *
 * @since POI 4.0.0
 */
// non-Javadoc: This abstract class exists to consolidate duplicated code between XSSFColumnShifter and HSSFColumnShifter
// (currently methods sprinkled throughout HSSFSheet)
@Beta
public abstract class ColumnShifter extends BaseRowColShifter {
    protected final Sheet sheet;

    public ColumnShifter(Sheet sh) {
        sheet = sh;
    }

    /**
     * Shifts, grows, or shrinks the merged regions due to a column shift.
     * Merged regions that are completely overlaid by shifting will be deleted.
     *
     * @param startColumn the column to start shifting
     * @param endColumn   the column to end shifting
     * @param n        the number of columns to shift
     * @return an array of affected merged regions, doesn't contain deleted ones
     * @since POI 4.0.0
     */
    // Keep this code in sync with {@link RowShifter#shiftMergedRegions}
    @Override
    public List<CellRangeAddress> shiftMergedRegions(int startColumn, int endColumn, int n) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<>();
        Set<Integer> removedIndices = new HashSet<>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        int size = sheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);

            // remove merged region that are replaced by the shifting,
            // i.e. where the area includes something in the overwritten area
            if(removalNeeded(merged, startColumn, endColumn, n)) {
                removedIndices.add(i);
                continue;
            }

            boolean inStart = (merged.getFirstColumn() >= startColumn || merged.getLastColumn() >= startColumn);
            boolean inEnd = (merged.getFirstColumn() <= endColumn || merged.getLastColumn() <= endColumn);

            //don't check if it's not within the shifted area
            if (!inStart || !inEnd) {
                continue;
            }

            //only shift if the region outside the shifted columns is not merged too
            if (!merged.containsColumn(startColumn - 1) && !merged.containsColumn(endColumn + 1)) {
                merged.setFirstColumn(merged.getFirstColumn() + n);
                merged.setLastColumn(merged.getLastColumn() + n);
                //have to remove/add it back
                shiftedRegions.add(merged);
                removedIndices.add(i);
            }
        }
        
        if(!removedIndices.isEmpty()) {
            sheet.removeMergedRegions(removedIndices);
        }

        //read so it doesn't get shifted again
        for (CellRangeAddress region : shiftedRegions) {
            sheet.addMergedRegion(region);
        }
        return shiftedRegions;
    }

    // Keep in sync with {@link RowShifter#removalNeeded}
    private boolean removalNeeded(CellRangeAddress merged, int startColumn, int endColumn, int n) {
        final int movedColumns = endColumn - startColumn + 1;

        // build a range of the columns that are overwritten, i.e. the target-area, but without
        // columns that are moved along
        final CellRangeAddress overwrite;
        if(n > 0) {
            // area is moved down => overwritten area is [endColumn + n - movedColumns, endColumn + n]
            final int firstCol = Math.max(endColumn + 1, endColumn + n - movedColumns);
            final int lastCol = endColumn + n;
            overwrite = new CellRangeAddress(0, 0, firstCol, lastCol);
        } else {
            // area is moved up => overwritten area is [startColumn + n, startColumn + n + movedColumns]
            final int firstCol = startColumn + n;
            final int lastCol = Math.min(startColumn - 1, startColumn + n + movedColumns);
            overwrite = new CellRangeAddress(0, 0, firstCol, lastCol);
        }

        // if the merged-region and the overwritten area intersect, we need to remove it
        return merged.intersects(overwrite);
    }

    public void shiftColumns(int firstShiftColumnIndex, int lastShiftColumnIndex, int step){
        if(step > 0){
            for (Row row : sheet)
                if(row != null)
                    row.shiftCellsRight(firstShiftColumnIndex, lastShiftColumnIndex, step);
        }
        else if(step < 0){
            for (Row row : sheet)
                if(row != null)
                    row.shiftCellsLeft(firstShiftColumnIndex, lastShiftColumnIndex, -step);
        }
        //else step == 0 => nothing to shift
    }
}
