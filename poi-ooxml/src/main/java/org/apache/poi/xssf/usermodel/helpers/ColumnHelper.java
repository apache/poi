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

package org.apache.poi.xssf.usermodel.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.util.CTColComparator;
import org.apache.poi.xssf.util.NumericRanges;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

/**
 * Helper class for dealing with the Column settings on
 *  a CTWorksheet (the data part of a sheet).
 * Note - within POI, we use 0 based column indexes, but
 *  the column definitions in the XML are 1 based!
 */
public class ColumnHelper {

    private CTWorksheet worksheet;

    public ColumnHelper(CTWorksheet worksheet) {
        super();
        this.worksheet = worksheet;
        cleanColumns();
    }
    
    public void cleanColumns() {
        TreeSet<CTCol> trackedCols = new TreeSet<>(CTColComparator.BY_MIN_MAX);
        CTCols newCols = CTCols.Factory.newInstance();
        CTCols[] colsArray = worksheet.getColsArray();
        int i;
        for (i = 0; i < colsArray.length; i++) {
            CTCols cols = colsArray[i];
            for (CTCol col : cols.getColList()) {
                addCleanColIntoCols(newCols, col, trackedCols);
            }
        }
        for (int y = i - 1; y >= 0; y--) {
            worksheet.removeCols(y);
        }
        
        newCols.setColArray(trackedCols.toArray(new CTCol[0]));
        worksheet.addNewCols();
        worksheet.setColsArray(0, newCols);
    }

    public CTCols addCleanColIntoCols(CTCols cols, CTCol newCol) {
        // Performance issue. If we encapsulated management of min/max in this
        // class then we could keep trackedCols as state,
        // making this log(N) rather than Nlog(N). We do this for the initial
        // read above.
        TreeSet<CTCol> trackedCols = new TreeSet<>(
                CTColComparator.BY_MIN_MAX);
        trackedCols.addAll(cols.getColList());
        addCleanColIntoCols(cols, newCol, trackedCols);
        cols.setColArray(trackedCols.toArray(new CTCol[0]));
        return cols;
    }

    private void addCleanColIntoCols(final CTCols cols, final CTCol newCol, final TreeSet<CTCol> trackedCols) {
        List<CTCol> overlapping = getOverlappingCols(newCol, trackedCols);
        if (overlapping.isEmpty()) {
            trackedCols.add(cloneCol(cols, newCol));
            return;
        }

        trackedCols.removeAll(overlapping);
        for (CTCol existing : overlapping) {
            // We add up to three columns for each existing one: non-overlap
            // before, overlap, non-overlap after.
            long[] overlap = getOverlap(newCol, existing);

            CTCol overlapCol = cloneCol(cols, existing, overlap);
            setColumnAttributes(newCol, overlapCol);
            trackedCols.add(overlapCol);

            CTCol beforeCol = existing.getMin() < newCol.getMin() ? existing
                    : newCol;
            long[] before = new long[] {
                    Math.min(existing.getMin(), newCol.getMin()),
                    overlap[0] - 1 };
            if (before[0] <= before[1]) {
                trackedCols.add(cloneCol(cols, beforeCol, before));
            }

            CTCol afterCol = existing.getMax() > newCol.getMax() ? existing
                    : newCol;
            long[] after = new long[] { overlap[1] + 1,
                    Math.max(existing.getMax(), newCol.getMax()) };
            if (after[0] <= after[1]) {
                trackedCols.add(cloneCol(cols, afterCol, after));
            }
        }
    }

    private CTCol cloneCol(final CTCols cols, final CTCol col, final long[] newRange) {
        CTCol cloneCol = cloneCol(cols, col);
        cloneCol.setMin(newRange[0]);
        cloneCol.setMax(newRange[1]);
        return cloneCol;
    }

    private long[] getOverlap(final CTCol col1, final CTCol col2) {
        return getOverlappingRange(col1, col2);
    }

    private List<CTCol> getOverlappingCols(final CTCol newCol, final TreeSet<CTCol> trackedCols) {
        CTCol lower = trackedCols.lower(newCol);
        NavigableSet<CTCol> potentiallyOverlapping = lower == null ? trackedCols : trackedCols.tailSet(lower, overlaps(lower, newCol));
        List<CTCol> overlapping = new ArrayList<>();
        for (CTCol existing : potentiallyOverlapping) {
            if (overlaps(newCol, existing)) {
                overlapping.add(existing);
            } else {
                break;
            }
        }
        return overlapping;
    }

    private boolean overlaps(final CTCol col1, final CTCol col2) {
        return NumericRanges.getOverlappingType(toRange(col1), toRange(col2)) != NumericRanges.NO_OVERLAPS;
    }

    private long[] getOverlappingRange(final CTCol col1, final CTCol col2) {
        return NumericRanges.getOverlappingRange(toRange(col1), toRange(col2));
    }

    private long[] toRange(final CTCol col) {
        return new long[] { col.getMin(), col.getMax() };
    }
    
    public static void sortColumns(CTCols newCols) {
        CTCol[] colArray = newCols.getColArray();
        Arrays.sort(colArray, CTColComparator.BY_MIN_MAX);
        newCols.setColArray(colArray);
    }

    public CTCol cloneCol(CTCols cols, CTCol col) {
        CTCol newCol = cols.addNewCol();
        newCol.setMin(col.getMin());
        newCol.setMax(col.getMax());
        setColumnAttributes(col, newCol);
        return newCol;
    }

    /**
     * Returns the Column at the given 0 based index
     */
    public CTCol getColumn(long index, boolean splitColumns) {
        return getColumn1Based(index+1, splitColumns);
    }

    /**
     * Returns the Column at the given 1 based index.
     * POI default is 0 based, but the file stores
     *  as 1 based.
     */
    public CTCol getColumn1Based(long index1, boolean splitColumns) {
        CTCols cols = worksheet.getColsArray(0);
        
        // Fetching the array is quicker than working on the new style
        //  list, assuming we need to read many of them (which we often do),
        //  and assuming we're not making many changes (which we're not)
        CTCol[] colArray = cols.getColArray();

        for (CTCol col : colArray) {
            long colMin = col.getMin();
            long colMax = col.getMax();
            if (colMin <= index1 && colMax >= index1) {
                if (splitColumns) {
                    if (colMin < index1) {
                        insertCol(cols, colMin, (index1 - 1), new CTCol[]{col});
                    }
                    if (colMax > index1) {
                        insertCol(cols, (index1 + 1), colMax, new CTCol[]{col});
                    }
                    col.setMin(index1);
                    col.setMax(index1);
                }
                return col;
            }
        }
        return null;
    }

    /*
     * Insert a new CTCol at position 0 into cols, setting min=min, max=max and
     * copying all the colsWithAttributes array cols attributes into newCol
     */
    private CTCol insertCol(CTCols cols, long min, long max, CTCol[] colsWithAttributes) {
        return insertCol(cols, min, max, colsWithAttributes, false, null);
    }
    
    private CTCol insertCol(CTCols cols, long min, long max,            
        CTCol[] colsWithAttributes, boolean ignoreExistsCheck, CTCol overrideColumn) {
        if(ignoreExistsCheck || !columnExists(cols,min,max)){
            CTCol newCol = cols.insertNewCol(0);
            newCol.setMin(min);
            newCol.setMax(max);
            for (CTCol col : colsWithAttributes) {
                setColumnAttributes(col, newCol);
            }
            if (overrideColumn != null) setColumnAttributes(overrideColumn, newCol); 
            return newCol;
        }
        return null;
    }

    /**
     * Does the column at the given 0 based index exist
     *  in the supplied list of column definitions?
     */
    public boolean columnExists(CTCols cols, long index) {
        return columnExists1Based(cols, index+1);
    }

    private boolean columnExists1Based(CTCols cols, long index1) {
        for (CTCol col : cols.getColArray()) {
            if (col.getMin() == index1) {
                return true;
            }
        }
        return false;
    }

    public void setColumnAttributes(CTCol fromCol, CTCol toCol) {
        if(fromCol.isSetBestFit()) toCol.setBestFit(fromCol.getBestFit());
        if(fromCol.isSetCustomWidth()) toCol.setCustomWidth(fromCol.getCustomWidth());
        if(fromCol.isSetHidden()) toCol.setHidden(fromCol.getHidden());
        if(fromCol.isSetStyle()) toCol.setStyle(fromCol.getStyle());
        if(fromCol.isSetWidth()) toCol.setWidth(fromCol.getWidth());
        if(fromCol.isSetCollapsed()) toCol.setCollapsed(fromCol.getCollapsed());
        if(fromCol.isSetPhonetic()) toCol.setPhonetic(fromCol.getPhonetic());
        if(fromCol.isSetOutlineLevel()) toCol.setOutlineLevel(fromCol.getOutlineLevel());
        toCol.setCollapsed(fromCol.isSetCollapsed());
    }

    public void setColBestFit(long index, boolean bestFit) {
        CTCol col = getOrCreateColumn1Based(index+1, false);
        col.setBestFit(bestFit);
    }
    public void setCustomWidth(long index, boolean bestFit) {
        CTCol col = getOrCreateColumn1Based(index+1, true);
        col.setCustomWidth(bestFit);
    }

    public void setColWidth(long index, double width) {
        CTCol col = getOrCreateColumn1Based(index+1, true);
        col.setWidth(width);
    }

    public void setColHidden(long index, boolean hidden) {
        CTCol col = getOrCreateColumn1Based(index+1, true);
        col.setHidden(hidden);
    }

    /**
     * Return the CTCol at the given (0 based) column index,
     *  creating it if required.
     */
    protected CTCol getOrCreateColumn1Based(long index1, boolean splitColumns) {
        CTCol col = getColumn1Based(index1, splitColumns);
        if (col == null) {
            col = worksheet.getColsArray(0).addNewCol();
            col.setMin(index1);
            col.setMax(index1);
        }
        return col;
    }

    public void setColDefaultStyle(long index, CellStyle style) {
        setColDefaultStyle(index, style.getIndex());
    }
    
    public void setColDefaultStyle(long index, int styleId) {
        CTCol col = getOrCreateColumn1Based(index+1, true);
        col.setStyle(styleId);
    }
    
    // Returns -1 if no column is found for the given index
    public int getColDefaultStyle(long index) {
        if (getColumn(index, false) != null) {
            return (int) getColumn(index, false).getStyle();
        }
        return -1;
    }

    private boolean columnExists(CTCols cols, long min, long max) {
        for (CTCol col : cols.getColList()) {
            if (col.getMin() == min && col.getMax() == max) {
                return true;
            }
        }
        return false;
    }

    public int getIndexOfColumn(CTCols cols, CTCol searchCol) {
        if (cols == null || searchCol == null) return -1;
        int i = 0;
        for (CTCol col : cols.getColList()) {
            if (col.getMin() == searchCol.getMin() && col.getMax() == searchCol.getMax()) {
                return i;
            }
            i++;
        }
        return -1;
    }
}