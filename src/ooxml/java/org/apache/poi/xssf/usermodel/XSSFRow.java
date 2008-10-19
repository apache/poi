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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;


public class XSSFRow implements Row, Comparable {

    private CTRow row;
    
    private List<Cell> cells;
    
    private XSSFSheet sheet;

    /**
     * Create a new XSSFRow.
     * 
     * @param row The underlying XMLBeans row.
     * @param sheet The parent sheet.
     */
    public XSSFRow(CTRow row, XSSFSheet sheet) {
        this.row = row;
        this.sheet = sheet;
        this.cells = new LinkedList<Cell>(); 
        for (CTCell c : row.getCArray()) {
            this.cells.add(new XSSFCell(this, c));
        }
        
    }

    public XSSFSheet getSheet() {
        return this.sheet;
    }
    
    public Iterator<Cell> cellIterator() {
        return cells.iterator();
    }
    /**
     * Alias for {@link #cellIterator()} to allow
     *  foreach loops
     */
    public Iterator<Cell> iterator() {
    	return cellIterator();
    }

    /**
     * Compares two <code>XSSFRow</code> objects.
     *
     * @param   row   the <code>XSSFRow</code> to be compared.
     * @return	the value <code>0</code> if the row number of this <code>XSSFRow</code> is
     * 		equal to the row number of the argument <code>XSSFRow</code>; a value less than
     * 		<code>0</code> if the row number of this this <code>XSSFRow</code> is numerically less
     * 		than the row number of the argument <code>XSSFRow</code>; and a value greater
     * 		than <code>0</code> if the row number of this this <code>XSSFRow</code> is numerically
     * 		 greater than the row number of the argument <code>XSSFRow</code>.
     */
    public int compareTo(Object row) {
        int thisVal = this.getRowNum();
        int anotherVal = ((XSSFRow)row).getRowNum();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     */
    public XSSFCell createCell(int column) {
    	return createCell(column, Cell.CELL_TYPE_BLANK);
    }

    /**
     * Add a new empty cell to this row.
     * 
     * @param column Cell column number.
     * @param index Position where to insert cell.
     * @param type TODO
     * @return The new cell.
     */
    protected XSSFCell addCell(int column, int index, int type) {
        CTCell ctcell = row.insertNewC(index);
        XSSFCell xcell = new XSSFCell(this, ctcell);
        xcell.setCellNum(column);
        if (type != Cell.CELL_TYPE_BLANK) {
        	xcell.setCellType(type);
        }
        return xcell;
    }

    /**
     * Use this to create new cells within the row and return it.
     *
     * @param column - the column number this cell represents
     * @param type - the cell's data type
     *
     * @return XSSFCell a high level representation of the created cell.
     */
    public XSSFCell createCell(int column, int type) {
        int index = 0;
        for (Cell c : this.cells) {
            if (c.getColumnIndex() == column) {
                // Replace c with new Cell
                XSSFCell xcell = addCell(column, index, type);
                cells.set(index, xcell);
                return xcell;
            }
            if (c.getColumnIndex() > column) {
                XSSFCell xcell = addCell(column, index, type);
                cells.add(index, xcell);
                return xcell;
            }
            ++index;
        }
        XSSFCell xcell = addCell(column, index, type);
        cells.add(xcell);
        return xcell;
    }

    private XSSFCell retrieveCell(int cellnum) {
        Iterator<Cell> it = cellIterator();
        for ( ; it.hasNext() ; ) {
        	Cell cell = it.next();
        	if (cell.getCellNum() == cellnum) {
        		return (XSSFCell)cell;
        	}
        }
        return null;
    }
    
    /**
     * Returns the cell at the given (0 based) index,
     *  with the {@link MissingCellPolicy} from the
     *  parent Workbook.
     */
    public XSSFCell getCell(int cellnum) {
    	return getCell(cellnum, sheet.getWorkbook().getMissingCellPolicy());
    }
    
    /**
     * Returns the cell at the given (0 based) index,
     *  with the specified {@link MissingCellPolicy}
     */
    public XSSFCell getCell(int cellnum, MissingCellPolicy policy) {
    	XSSFCell cell = retrieveCell(cellnum);
    	if(policy == RETURN_NULL_AND_BLANK) {
    		return cell;
    	}
    	if(policy == RETURN_BLANK_AS_NULL) {
    		if(cell == null) return cell;
    		if(cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
    			return null;
    		}
    		return cell;
    	}
    	if(policy == CREATE_NULL_AS_BLANK) {
    		if(cell == null) {
    			return createCell((short)cellnum, HSSFCell.CELL_TYPE_BLANK);
    		}
    		return cell;
    	}
    	throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
    }

    public short getFirstCellNum() {
    	for (Iterator<Cell> it = cellIterator() ; it.hasNext() ; ) {
    		Cell cell = it.next();
    		if (cell != null) {
    			return cell.getCellNum();
    		}
    	}
    	return -1;
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
    public short getHeight() {
        return (short)(getHeightInPoints()*20);
    }

    /**
     * Returns row height measured in point size. If the height is not set, the default worksheet value is returned,
     * See {@link org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in point size
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()
     */
    public float getHeightInPoints() {
    	if (this.row.isSetHt()) {
    		return (float) this.row.getHt();
    	} else {
            return sheet.getDefaultRowHeightInPoints();
        }
    }

    /**
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     *   XSSFCell cell = row.getCell(colIx);
     *   if(cell == null) {
     *     continue;
     *   }
     *   //... do something with cell
     * }
     * </pre>
     *
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>, or -1 if the
     *  row does not contain any cells.
     */
    public short getLastCellNum() {
    	short lastCellNum = -1;
    	for (Iterator<Cell> it = cellIterator() ; it.hasNext() ; ) {
    		Cell cell = it.next();
    		if (cell != null) {
    			lastCellNum = (short)(cell.getCellNum() + 1);
    		}
    	}
    	return lastCellNum;
    }

    public int getPhysicalNumberOfCells() {
    	int count = 0;
    	for (Iterator<Cell> it = cellIterator() ; it.hasNext() ; ) {
    		if (it.next() != null) {
    			count++;
    		}
    	}
    	return count;
    }

    public int getRowNum() {
        return (int) (row.getR() - 1);
    }

    public boolean getZeroHeight() {
    	return this.row.getHidden();
    }

    public void removeCell(Cell cell) {
    	int counter = 0;
    	for (Iterator<Cell> it = cellIterator(); it.hasNext(); ) {
    		Cell c = it.next();
    		if (c.getCellNum() == cell.getCellNum()) {
    			it.remove();
    			row.removeC(counter);
    			continue;
    		}
    		counter++;
    	}
    }

    /**
     *  Set the height in "twips" or  1/20th of a point.
     *
     * @param height the height in "twips" or  1/20th of a point. <code>-1</code>  resets to the default height
     */
    public void setHeight(short height) {
    	if(height == -1){
            this.row.unsetHt();
            this.row.unsetCustomHeight();
        } else {
            this.row.setHt((double)height/20);
            this.row.setCustomHeight(true);

        }
    }
    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    public void setHeightInPoints(float height) {
	    setHeight((short)(height*20));
    }

    public void setRowNum(int rowNum) {
        this.row.setR(rowNum + 1);

    }

    public void setZeroHeight(boolean height) {
    	this.row.setHidden(height);

    }
    
    public CTRow getCTRow(){
    	return this.row;
    }

}
