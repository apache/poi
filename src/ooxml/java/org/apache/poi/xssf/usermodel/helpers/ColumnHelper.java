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

import java.util.Arrays;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.util.CTColComparator;
import org.apache.poi.xssf.util.NumericRanges;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

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
        CTCols[] colsArray = worksheet.getColsArray();
        int i = 0;
        for (i = 0; i < colsArray.length; i++) {
            CTCols cols = colsArray[i];
            CTCol[] colArray = cols.getColArray();
            for (int y = 0; y < colArray.length; y++) {
                CTCol col = colArray[y];
                newCols = addCleanColIntoCols(newCols, col);
            }
        }
        for (int y = i - 1; y >= 0; y--) {
            worksheet.removeCols(y);
        }
        worksheet.addNewCols();
        worksheet.setColsArray(0, newCols);
    }

    public void sortColumns(CTCols newCols) {
        CTCol[] colArray = newCols.getColArray();
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

    public CTCol getColumn(long index, boolean splitColumns) {
        CTCols colsArray = worksheet.getColsArray(0);
		for (int i = 0; i < colsArray.sizeOfColArray(); i++) {
            CTCol colArray = colsArray.getColArray(i);
			if (colArray.getMin() <= index && colArray.getMax() >= index) {
				if (splitColumns) {
					if (colArray.getMin() < index) {
						insertCol(colsArray, colArray.getMin(), (index - 1), new CTCol[]{colArray});
					}
					if (colArray.getMax() > index) {
						insertCol(colsArray, (index + 1), colArray.getMax(), new CTCol[]{colArray});
					}
					colArray.setMin(index);
					colArray.setMax(index);
				}
                return colArray;
            }
        }
        return null;
    }

    public CTCols addCleanColIntoCols(CTCols cols, CTCol col) {
        boolean colOverlaps = false;
        for (int i = 0; i < cols.sizeOfColArray(); i++) {
            CTCol ithCol = cols.getColArray(i);
            long[] range1 = { ithCol.getMin(), ithCol.getMax() };
            long[] range2 = { col.getMin(), col.getMax() };
            long[] overlappingRange = NumericRanges.getOverlappingRange(range1,
                    range2);
            int overlappingType = NumericRanges.getOverlappingType(range1,
                    range2);
            // different behavior required for each of the 4 different
            // overlapping types
            if (overlappingType == NumericRanges.OVERLAPS_1_MINOR) {
                ithCol.setMax(overlappingRange[0] - 1);
                CTCol rangeCol = insertCol(cols, overlappingRange[0],
                        overlappingRange[1], new CTCol[] { ithCol, col });
                i++;
                CTCol newCol = insertCol(cols, (overlappingRange[1] + 1), col
                        .getMax(), new CTCol[] { col });
                i++;
            } else if (overlappingType == NumericRanges.OVERLAPS_2_MINOR) {
                ithCol.setMin(overlappingRange[1] + 1);
                CTCol rangeCol = insertCol(cols, overlappingRange[0],
                        overlappingRange[1], new CTCol[] { ithCol, col });
                i++;
                CTCol newCol = insertCol(cols, col.getMin(),
                        (overlappingRange[0] - 1), new CTCol[] { col });
                i++;
            } else if (overlappingType == NumericRanges.OVERLAPS_2_WRAPS) {
                setColumnAttributes(col, ithCol);
                if (col.getMin() != ithCol.getMin()) {
                    CTCol newColBefore = insertCol(cols, col.getMin(), (ithCol
                            .getMin() - 1), new CTCol[] { col });
                    i++;
                }
                if (col.getMax() != ithCol.getMax()) {
                    CTCol newColAfter = insertCol(cols, (ithCol.getMax() + 1),
                            col.getMax(), new CTCol[] { col });
                    i++;
                }
            } else if (overlappingType == NumericRanges.OVERLAPS_1_WRAPS) {
                if (col.getMin() != ithCol.getMin()) {
                    CTCol newColBefore = insertCol(cols, ithCol.getMin(), (col
                            .getMin() - 1), new CTCol[] { ithCol });
                    i++;
                }
                if (col.getMax() != ithCol.getMax()) {
                    CTCol newColAfter = insertCol(cols, (col.getMax() + 1),
                            ithCol.getMax(), new CTCol[] { ithCol });
                    i++;
                }
                ithCol.setMin(overlappingRange[0]);
                ithCol.setMax(overlappingRange[1]);
                setColumnAttributes(col, ithCol);
            }
            if (overlappingType != NumericRanges.NO_OVERLAPS) {
                colOverlaps = true;
            }
        }
        if (!colOverlaps) {
            CTCol newCol = cloneCol(cols, col);
        }
        sortColumns(cols);
        return cols;
    }

    /*
     * Insert a new CTCol at position 0 into cols, setting min=min, max=max and
     * copying all the colsWithAttributes array cols attributes into newCol
     */
    private CTCol insertCol(CTCols cols, long min, long max,            
        CTCol[] colsWithAttributes) {
        if(!columnExists(cols,min,max)){
                CTCol newCol = cols.insertNewCol(0);
                newCol.setMin(min);
                newCol.setMax(max);
                for (CTCol col : colsWithAttributes) {
                        setColumnAttributes(col, newCol);
                }
                return newCol;
        }
        return null;
    }

    public boolean columnExists(CTCols cols, long index) {
        for (int i = 0; i < cols.sizeOfColArray(); i++) {
            if (cols.getColArray(i).getMin() == index) {
                return true;
            }
        }
        return false;
    }

    public void setColumnAttributes(CTCol fromCol, CTCol toCol) {
    	toCol.setWidth(fromCol.getWidth());
    	toCol.setHidden(fromCol.getHidden());
    	toCol.setBestFit(fromCol.getBestFit());
        toCol.setStyle(fromCol.getStyle());
        if(fromCol.getOutlineLevel()!=0){
        	toCol.setOutlineLevel(fromCol.getOutlineLevel());
        }
    }

    public void setColBestFit(long index, boolean bestFit) {
        CTCol col = getOrCreateColumn(index, false);
        col.setBestFit(bestFit);
    }

    public void setColWidth(long index, double width) {
        CTCol col = getOrCreateColumn(index, false);
        col.setWidth(width);
    }

    public void setColHidden(long index, boolean hidden) {
        CTCol col = getOrCreateColumn(index, false);
        col.setHidden(hidden);
    }

    protected CTCol getOrCreateColumn(long index, boolean splitColumns) {
        CTCol col = getColumn(index, splitColumns);
        if (col == null) {
            col = worksheet.getColsArray(0).addNewCol();
            col.setMin(index);
            col.setMax(index);
        }
        return col;
    }

	public void setColDefaultStyle(long index, CellStyle style) {
		setColDefaultStyle(index, style.getIndex());
	}
	
	public void setColDefaultStyle(long index, int styleId) {
		CTCol col = getOrCreateColumn(index, true);
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
