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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.util.CTColComparator;
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
    private CTCols newCols;

    public ColumnHelper(CTWorksheet worksheet) {
        super();
        this.worksheet = worksheet;
        cleanColumns();
    }
    
    @SuppressWarnings("deprecation")
    public void cleanColumns() {
        this.newCols = CTCols.Factory.newInstance();

        CTCols aggregateCols = CTCols.Factory.newInstance();
        CTCols[] colsArray = worksheet.getColsArray();
        assert(colsArray != null);
        
        for (CTCols cols : colsArray) {
            for (CTCol col : cols.getColArray()) {
                cloneCol(aggregateCols, col);
            }
        }
        
        sortColumns(aggregateCols);
        
        CTCol[] colArray = aggregateCols.getColArray();
        sweepCleanColumns(newCols, colArray, null);
        
        int i = colsArray.length;
        for (int y = i - 1; y >= 0; y--) {
            worksheet.removeCols(y);
        }
        worksheet.addNewCols();
        worksheet.setColsArray(0, newCols);
    }

    /**
     * @see <a href="http://en.wikipedia.org/wiki/Sweep_line_algorithm">Sweep line algorithm</a>
     */
    private void sweepCleanColumns(CTCols cols, CTCol[] flattenedColsArray, CTCol overrideColumn) {
        List<CTCol> flattenedCols = new ArrayList<CTCol>(Arrays.asList(flattenedColsArray));
        TreeSet<CTCol> currentElements = new TreeSet<CTCol>(CTColComparator.BY_MAX);
        ListIterator<CTCol> flIter = flattenedCols.listIterator();
        CTCol haveOverrideColumn = null;
        long lastMaxIndex = 0;
        long currentMax = 0;
        while (flIter.hasNext()) {
            CTCol col = flIter.next();
            long currentIndex = col.getMin();
            long colMax = col.getMax();
            long nextIndex = (colMax > currentMax) ? colMax : currentMax;
            if (flIter.hasNext()) {
                nextIndex = flIter.next().getMin();
                flIter.previous();
            }
            Iterator<CTCol> iter = currentElements.iterator();
            while (iter.hasNext()) {
                CTCol elem = iter.next();
                if (currentIndex <= elem.getMax()) break; // all passed elements have been purged
                iter.remove();
            }
            if (!currentElements.isEmpty() && lastMaxIndex < currentIndex) {
                // we need to process previous elements first
                insertCol(cols, lastMaxIndex, currentIndex - 1, currentElements.toArray(new CTCol[currentElements.size()]), true, haveOverrideColumn);
            }
            currentElements.add(col);
            if (colMax > currentMax) currentMax = colMax;
            if (col.equals(overrideColumn)) haveOverrideColumn = overrideColumn;
            while (currentIndex <= nextIndex && !currentElements.isEmpty()) {
                Set<CTCol> currentIndexElements = new HashSet<CTCol>();
                long currentElemIndex;
                
                {
                    // narrow scope of currentElem
                    CTCol currentElem = currentElements.first();
                    currentElemIndex = currentElem.getMax();
                    currentIndexElements.add(currentElem);

                    while (true) {
                        CTCol higherElem = currentElements.higher(currentElem);
                        if (higherElem == null || higherElem.getMax() != currentElemIndex)
                            break;
                        currentElem = higherElem;
                        currentIndexElements.add(currentElem);
                        if (colMax > currentMax) currentMax = colMax;
                        if (col.equals(overrideColumn)) haveOverrideColumn = overrideColumn;
                    }
                }
                
                
                if (currentElemIndex < nextIndex || !flIter.hasNext()) {
                    insertCol(cols, currentIndex, currentElemIndex, currentElements.toArray(new CTCol[currentElements.size()]), true, haveOverrideColumn);
                    if (flIter.hasNext()) {
                        if (nextIndex > currentElemIndex) {
                            currentElements.removeAll(currentIndexElements);
                            if (currentIndexElements.contains(overrideColumn)) haveOverrideColumn = null;
                        }
                    } else {
                        currentElements.removeAll(currentIndexElements);
                        if (currentIndexElements.contains(overrideColumn)) haveOverrideColumn = null;
                    }
                    lastMaxIndex = currentIndex = currentElemIndex + 1;
                } else {
                    lastMaxIndex = currentIndex;
                    currentIndex = nextIndex + 1;
                }
                
            }        
        }
        sortColumns(cols);
    }

    @SuppressWarnings("deprecation")
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
        @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    public CTCols addCleanColIntoCols(CTCols cols, CTCol col) {
        CTCols newCols = CTCols.Factory.newInstance();
        for (CTCol c : cols.getColArray()) {
            cloneCol(newCols, c);
        }
        cloneCol(newCols, col);
        sortColumns(newCols);
        CTCol[] colArray = newCols.getColArray();
        CTCols returnCols = CTCols.Factory.newInstance();
        sweepCleanColumns(returnCols, colArray, col);
        colArray = returnCols.getColArray();
        cols.setColArray(colArray);
        return returnCols;
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    private boolean columnExists(CTCols cols, long min, long max) {
        for (CTCol col : cols.getColArray()) {
            if (col.getMin() == min && col.getMax() == max) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public int getIndexOfColumn(CTCols cols, CTCol searchCol) {
        int i = 0;
        for (CTCol col : cols.getColArray()) {
            if (col.getMin() == searchCol.getMin() && col.getMax() == searchCol.getMax()) {
                return i;
            }
            i++;
        }
        return -1;
    }
}