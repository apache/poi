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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LocaleUtil;

/**
 * Helper for shifting rows up or down
 */
// non-Javadoc: This abstract class exists to consolidate duplicated code between
// {@link org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter} and
// {@link org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter}
// (currently methods sprinkled throughout HSSFSheet)
public abstract class RowShifter extends BaseRowColShifter {
    protected final Sheet sheet;

    public RowShifter(Sheet sh) { 
        sheet = sh; 
    } 
 
  /**
     * Shifts, grows, or shrinks the merged regions due to a row shift.
     * Merged regions that are completely overlaid by shifting will be deleted.
     *
     * @param startRow the row to start shifting
     * @param endRow   the row to end shifting
     * @param n        the number of rows to shift
     * @return an array of affected merged regions, doesn't contain deleted ones
     */
    // Keep this code in sync with {@link ColumnShifter#shiftMergedRegions}
    @Override
    public List<CellRangeAddress> shiftMergedRegions(int startRow, int endRow, int n) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<>();
        Set<Integer> removedIndices = new HashSet<>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        int size = sheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);

            // remove merged region that are replaced by the shifting,
            // i.e. where the area includes something in the overwritten area
            if(removalNeeded(merged, startRow, endRow, n)) {
                removedIndices.add(i);
                continue;
            }

            boolean inStart = (merged.getFirstRow() >= startRow || merged.getLastRow() >= startRow);
            boolean inEnd = (merged.getFirstRow() <= endRow || merged.getLastRow() <= endRow);

            //don't check if it's not within the shifted area
            if (!inStart || !inEnd) {
                continue;
            }

            //only shift if the region outside the shifted rows is not merged too
            if (!merged.containsRow(startRow - 1) && !merged.containsRow(endRow + 1)) {
                merged.setFirstRow(merged.getFirstRow() + n);
                merged.setLastRow(merged.getLastRow() + n);
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

    // Keep in sync with {@link ColumnShifter#removalNeeded}
    private boolean removalNeeded(CellRangeAddress merged, int startRow, int endRow, int n) {
        final int movedRows = endRow - startRow + 1;

        // build a range of the rows that are overwritten, i.e. the target-area, but without
        // rows that are moved along
        final CellRangeAddress overwrite;
        if(n > 0) {
            // area is moved down => overwritten area is [endRow + n - movedRows, endRow + n]
            final int firstRow = Math.max(endRow + 1, endRow + n - movedRows);
            final int lastRow = endRow + n;
            overwrite = new CellRangeAddress(firstRow, lastRow, 0, 0);
        } else {
            // area is moved up => overwritten area is [startRow + n, startRow + n + movedRows]
            final int firstRow = startRow + n;
            final int lastRow = Math.min(startRow - 1, startRow + n + movedRows);
            overwrite = new CellRangeAddress(firstRow, lastRow, 0, 0);
        }

        // if the merged-region and the overwritten area intersect, we need to remove it
        return merged.intersects(overwrite);
    }

    /**
     * Verify that the given column indices and step denote a valid range of columns to shift
     *
     * @param firstShiftColumnIndex the column to start shifting
     * @param lastShiftColumnIndex the column to end shifting
     * @param step length of the shifting step
     */
    public static void validateShiftParameters(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        if(step < 0) {
            throw new IllegalArgumentException("Shifting step may not be negative, but had " + step);
        }
        if(firstShiftColumnIndex > lastShiftColumnIndex) {
            throw new IllegalArgumentException(String.format(LocaleUtil.getUserLocale(),
                    "Incorrect shifting range : %d-%d", firstShiftColumnIndex, lastShiftColumnIndex));
        }
    }

    /**
     * Verify that the given column indices and step denote a valid range of columns to shift to the left
     *
     * @param firstShiftColumnIndex the column to start shifting
     * @param lastShiftColumnIndex the column to end shifting
     * @param step length of the shifting step
     */
    public static void validateShiftLeftParameters(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        validateShiftParameters(firstShiftColumnIndex, lastShiftColumnIndex, step);

        if(firstShiftColumnIndex - step < 0) {
            throw new IllegalStateException("Column index less than zero: " + (firstShiftColumnIndex + step));
        }
    }
}
