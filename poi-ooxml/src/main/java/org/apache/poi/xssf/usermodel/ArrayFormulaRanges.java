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

package org.apache.poi.xssf.usermodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * The ranges of the array formulas in a sheet, indexed so that finding the range holding a cell
 * doesn't need to check every array formula in the sheet.
 * <p>
 * Each range is registered against every block of 64 rows that it spans, so a lookup only checks
 * the ranges that share the cell's row block. Indexing by row block rather than by cell keeps a
 * large array formula from costing one entry per cell it covers.
 */
final class ArrayFormulaRanges {
    private static final int ROW_BLOCK_SHIFT = 6;

    private final Set<CellRangeAddress> ranges = new HashSet<>();
    private final Map<Integer, List<CellRangeAddress>> rowBlocks = new HashMap<>();

    /**
     * Records {@code range}. Does nothing if an equal range is already recorded.
     */
    void add(CellRangeAddress range) {
        // copy, so that a caller changing its range later doesn't corrupt the index
        CellRangeAddress copy = range.copy();
        if (!ranges.add(copy)) {
            return;
        }
        for (int block = firstBlock(copy); block <= lastBlock(copy); block++) {
            rowBlocks.computeIfAbsent(block, k -> new ArrayList<>()).add(copy);
        }
    }

    /**
     * Forgets {@code range}, which must be one returned by {@link #find(int, int)}.
     */
    void remove(CellRangeAddress range) {
        if (!ranges.remove(range)) {
            return;
        }
        for (int block = firstBlock(range); block <= lastBlock(range); block++) {
            List<CellRangeAddress> inBlock = rowBlocks.get(block);
            if (inBlock != null) {
                inBlock.remove(range);
                if (inBlock.isEmpty()) {
                    rowBlocks.remove(block);
                }
            }
        }
    }

    /**
     * @return the earliest recorded range holding the cell, or {@code null} if none does
     */
    CellRangeAddress find(int rowIndex, int columnIndex) {
        List<CellRangeAddress> inBlock = rowBlocks.get(rowIndex >> ROW_BLOCK_SHIFT);
        if (inBlock != null) {
            for (CellRangeAddress range : inBlock) {
                if (range.isInRange(rowIndex, columnIndex)) {
                    return range;
                }
            }
        }
        return null;
    }

    private static int firstBlock(CellRangeAddress range) {
        return range.getFirstRow() >> ROW_BLOCK_SHIFT;
    }

    private static int lastBlock(CellRangeAddress range) {
        return range.getLastRow() >> ROW_BLOCK_SHIFT;
    }
}
