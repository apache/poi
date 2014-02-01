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
import java.util.Comparator;
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
    
    public void cleanColumns() {
        this.newCols = CTCols.Factory.newInstance();

        CTCols aggregateCols = CTCols.Factory.newInstance();
        List<CTCols> colsList = worksheet.getColsList();
        if (colsList != null) {
            for (CTCols cols : colsList) {
                for (CTCol col : cols.getColList()) {
                    cloneCol(aggregateCols, col);
                }
            }
        }
        
        sortColumns(aggregateCols);
        
        CTCol[] colArray = new CTCol[aggregateCols.getColList().size()];
        aggregateCols.getColList().toArray(colArray);
        sweepCleanColumns(newCols, colArray, null);
        
        int i = colsList.size();
        for (int y = i - 1; y >= 0; y--) {
            worksheet.removeCols(y);
        }
        worksheet.addNewCols();
        worksheet.setColsArray(0, newCols);
    }
    
    private static class CTColByMaxComparator implements Comparator<CTCol> {

        public int compare(CTCol arg0, CTCol arg1) {
            if (arg0.getMax() < arg1.getMax()) {
                return -1;
            } else {
                if (arg0.getMax() > arg1.getMax()) return 1;
                else return 0;
            }
        }
        
    }

    /**
     * @see http://en.wikipedia.org/wiki/Sweep_line_algorithm
     */
    private void sweepCleanColumns(CTCols cols, CTCol[] flattenedColsArray, CTCol overrideColumn) {
        List<CTCol> flattenedCols = new ArrayList<CTCol>(Arrays.asList(flattenedColsArray));
        TreeSet<CTCol> currentElements = new TreeSet<CTCol>(new CTColByMaxComparator());
        ListIterator<CTCol> flIter = flattenedCols.listIterator();
        CTCol haveOverrideColumn = null;
        long lastMaxIndex = 0;
        long currentMax = 0;
        while (flIter.hasNext()) {
            CTCol col = flIter.next();
            long currentIndex = col.getMin();
            long nextIndex = (col.getMax() > currentMax) ? col.getMax() : currentMax;
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
                insertCol(cols, lastMaxIndex, currentIndex - 1, currentElements.toArray(new CTCol[]{}), true, haveOverrideColumn);
            }
            currentElements.add(col);
            if (col.getMax() > currentMax) currentMax = col.getMax();
            if (col.equals(overrideColumn)) haveOverrideColumn = overrideColumn;
            while (currentIndex <= nextIndex && !currentElements.isEmpty()) {
                Set<CTCol> currentIndexElements = new HashSet<CTCol>();
                long currentElemIndex;
                
                {
                    // narrow scope of currentElem
                    CTCol currentElem = currentElements.first();
                    currentElemIndex = currentElem.getMax();
                    currentIndexElements.add(currentElem);
                    
                    for (CTCol cc : currentElements.tailSet(currentElem)) {
                        if (cc == null || cc.getMax() == currentElemIndex) break;
                        currentIndexElements.add(cc);
                        if (col.getMax() > currentMax) currentMax = col.getMax();
                        if (col.equals(overrideColumn)) haveOverrideColumn = overrideColumn;
                    }

                    // JDK 6 code
                    // while (currentElements.higher(currentElem) != null && currentElements.higher(currentElem).getMax() == currentElemIndex) {
                    //     currentElem = currentElements.higher(currentElem);
                    //     currentIndexElements.add(currentElem);
                    //     if (col.getMax() > currentMax) currentMax = col.getMax();
                    //     if (col.equals(overrideColumn)) haveOverrideColumn = overrideColumn;
                    // }
                }
                
                
                if (currentElemIndex < nextIndex || !flIter.hasNext()) {
                    insertCol(cols, currentIndex, currentElemIndex, currentElements.toArray(new CTCol[]{}), true, haveOverrideColumn);
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

    public static void sortColumns(CTCols newCols) {
        CTCol[] colArray = new CTCol[newCols.getColList().size()];
        newCols.getColList().toArray(colArray);
        Arrays.sort(colArray, new CTColComparator());
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
        CTCols colsArray = worksheet.getColsArray(0);
        for (int i = 0; i < colsArray.sizeOfColArray(); i++) {
            CTCol colArray = colsArray.getColArray(i);
            if (colArray.getMin() <= index1 && colArray.getMax() >= index1) {
                if (splitColumns) {
                    if (colArray.getMin() < index1) {
                        insertCol(colsArray, colArray.getMin(), (index1 - 1), new CTCol[]{colArray});
                    }
                    if (colArray.getMax() > index1) {
                        insertCol(colsArray, (index1 + 1), colArray.getMax(), new CTCol[]{colArray});
                    }
                    colArray.setMin(index1);
                    colArray.setMax(index1);
                }
                return colArray;
            }
        }
        return null;
    }
    
    public CTCols addCleanColIntoCols(CTCols cols, CTCol col) {
        CTCols newCols = CTCols.Factory.newInstance();
        for (CTCol c : cols.getColList()) {
            cloneCol(newCols, c);
        }
        cloneCol(newCols, col);
        sortColumns(newCols);
        CTCol[] colArray = new CTCol[newCols.getColList().size()];
        newCols.getColList().toArray(colArray);
        CTCols returnCols = CTCols.Factory.newInstance();
        sweepCleanColumns(returnCols, colArray, col);
        colArray = new CTCol[returnCols.getColList().size()];
        returnCols.getColList().toArray(colArray);
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
    private boolean columnExists1Based(CTCols cols, long index1) {
        for (int i = 0; i < cols.sizeOfColArray(); i++) {
            if (cols.getColArray(i).getMin() == index1) {
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
        for (int i = 0; i < cols.sizeOfColArray(); i++) {
            if (cols.getColArray(i).getMin() == min && cols.getColArray(i).getMax() == max) {
                return true;
            }
        }
        return false;
    }
    
    public int getIndexOfColumn(CTCols cols, CTCol col) {
        for (int i = 0; i < cols.sizeOfColArray(); i++) {
            if (cols.getColArray(i).getMin() == col.getMin() && cols.getColArray(i).getMax() == col.getMax()) {
                return i;
            }
        }
        return -1;
    }
}