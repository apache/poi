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

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Helper for shifting rows up or down
 * 
 * This abstract class exists to consolidate duplicated code between XSSFRowShifter and HSSFRowShifter (currently methods sprinkled throughout HSSFSheet)
 */
public abstract class RowShifter {
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
    public List<CellRangeAddress> shiftMergedRegions(int startRow, int endRow, int n) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<CellRangeAddress>();
        Set<Integer> removedIndices = new HashSet<Integer>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        int size = sheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);

            // remove merged region that overlaps shifting
            if (startRow + n <= merged.getFirstRow() && endRow + n >= merged.getLastRow()) {
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

    /**
     * Updated named ranges
     */
    public abstract void updateNamedRanges(FormulaShifter shifter);

    /**
     * Update formulas.
     */
    public abstract void updateFormulas(FormulaShifter shifter);

    /**
     * Update the formulas in specified row using the formula shifting policy specified by shifter
     *
     * @param row the row to update the formulas on
     * @param shifter the formula shifting policy
     */
    @Internal
    public abstract void updateRowFormulas(Row row, FormulaShifter shifter);

    public abstract void updateConditionalFormatting(FormulaShifter shifter);
    
    /**
     * Shift the Hyperlink anchors (not the hyperlink text, even if the hyperlink
     * is of type LINK_DOCUMENT and refers to a cell that was shifted). Hyperlinks
     * do not track the content they point to.
     *
     * @param shifter the formula shifting policy
     */
    public abstract void updateHyperlinks(FormulaShifter shifter);

}
