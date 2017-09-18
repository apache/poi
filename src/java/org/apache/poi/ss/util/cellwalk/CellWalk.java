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

package org.apache.poi.ss.util.cellwalk;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Traverse cell range.
 *
 * @author Roman Kashitsyn
 */
public class CellWalk {

    private Sheet sheet;
    private CellRangeAddress range;
    private boolean traverseEmptyCells;


    public CellWalk(Sheet sheet, CellRangeAddress range) {
        this.sheet = sheet;
        this.range = range;
        this.traverseEmptyCells = false;
    }

    /**
     * Should we call handler on empty (blank) cells. Default is
     * false.
     *
     * @return true if handler should be called on empty (blank)
     *         cells, false otherwise.
     */
    public boolean isTraverseEmptyCells() {
        return traverseEmptyCells;
    }

    /**
     * Sets the traverseEmptyCells property.
     *
     * @param traverseEmptyCells new property value
     */
    public void setTraverseEmptyCells(boolean traverseEmptyCells) {
        this.traverseEmptyCells = traverseEmptyCells;
    }

    /**
     * Traverse cell range from top left to bottom right cell.
     *
     * @param handler handler to call on each appropriate cell
     */
    public void traverse(CellHandler handler) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int firstColumn = range.getFirstColumn();
        int lastColumn = range.getLastColumn();
        final int width = lastColumn - firstColumn + 1;
        SimpleCellWalkContext ctx = new SimpleCellWalkContext();
        Row currentRow = null;
        Cell currentCell = null;

        for (ctx.rowNumber = firstRow; ctx.rowNumber <= lastRow; ++ctx.rowNumber) {
            currentRow = sheet.getRow(ctx.rowNumber);
            if (currentRow == null) {
                continue;
            }
            for (ctx.colNumber = firstColumn; ctx.colNumber <= lastColumn; ++ctx.colNumber) {
                currentCell = currentRow.getCell(ctx.colNumber);

                if (currentCell == null) {
                    continue;
                }
                if (isEmpty(currentCell) && !traverseEmptyCells) {
                    continue;
                }

                ctx.ordinalNumber =
                        (ctx.rowNumber - firstRow) * width +
                                (ctx.colNumber - firstColumn + 1);

                handler.onCell(currentCell, ctx);
            }
        }
    }

    private boolean isEmpty(Cell cell) {
        return (cell.getCellType() == CellType.BLANK);
    }

    /**
     * Inner class to hold walk context.
     *
     * @author Roman Kashitsyn
     */
    private static class SimpleCellWalkContext implements CellWalkContext {
        public long ordinalNumber;
        public int rowNumber;
        public int colNumber;

        public long getOrdinalNumber() {
            return ordinalNumber;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public int getColumnNumber() {
            return colNumber;
        }
    }
}
