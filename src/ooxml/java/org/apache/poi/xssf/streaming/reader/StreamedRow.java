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
package org.apache.poi.xssf.streaming.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;


/**
 * Represents an excel row
 *
 */
public class StreamedRow implements Row{
	private List<StreamedCell> cells;
	private int rowNumber;

	public StreamedRow(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	/**
	 * <pre>
	 * Used to get cells of a Row
	 * </pre>
	 * @return Iterator<Cell>
	 */
	public Iterator<StreamedCell> getCellIterator() {
		if (cells == null) {
			cells = new ArrayList<StreamedCell>();
		}
		return cells.iterator();
	}
	
	/**
	 * <pre>
	 * 	Returns the row number
	 * </pre>
	 * @return int
	 */
	public int getRowNum() {
		return rowNumber;
	}

	public void setRowNum(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(250);
		sb.append("Row Number:").append(rowNumber);
		sb.append(" --> ");
		if (cells != null) {
			for (StreamedCell cell : cells) {
				sb.append(cell.toString());
				sb.append(" | ");
			}
		}

		return sb.toString();
	}

/*	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (cells != null) {
			cells.clear();
			cells = null;
		}
	}*/

	public List<StreamedCell> getCells() {
		if (cells == null) {
			cells = new ArrayList<StreamedCell>();
		}
		return cells;
	}

/**
 * <pre>
 * Will not be supported. User getCellIterator() instead
 * </pre>	
 */
public Iterator<Cell> iterator() {
    // TODO Auto-generated method stub
    return null;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public Cell createCell(int column) {
    // TODO Auto-generated method stub
    return null;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public Cell createCell(int column, int type) {
    // TODO Auto-generated method stub
    return null;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public Cell createCell(int column, CellType type) {
    // TODO Auto-generated method stub
    return null;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public void removeCell(Cell cell) {
    // TODO Auto-generated method stub
    
}

/**
 * <pre>
 *  Returns the cell on specified cell number
 * </pre>
 */
public Cell getCell(int cellnum) {
    StreamedCell cell = null;
    
    if(cells != null){
        cell = cells.get(cellnum);
    }
    
    return cell;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public Cell getCell(int cellnum, MissingCellPolicy policy) {
    // TODO Auto-generated method stub
    return null;
}

public short getFirstCellNum() {
    short firstCellNumber = -1;
    
    if(cells != null && cells.size() > 0){
       firstCellNumber = (short)cells.get(0).getCellNumber();
    }
    
    return firstCellNumber;
}

public short getLastCellNum() {
    short lastCellNumber = -1;
    
    if(cells != null && cells.size() > 0){
        lastCellNumber = (short)cells.get((cells.size()-1)).getCellNumber();
    }
    
    return lastCellNumber;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public int getPhysicalNumberOfCells() {
    // TODO Auto-generated method stub
    return 0;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public void setHeight(short height) {
    // TODO Auto-generated method stub
    
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public void setZeroHeight(boolean zHeight) {
    // TODO Auto-generated method stub
    
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public boolean getZeroHeight() {
    // TODO Auto-generated method stub
    return false;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public void setHeightInPoints(float height) {
    // TODO Auto-generated method stub
    
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public short getHeight() {
    // TODO Auto-generated method stub
    return 0;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public float getHeightInPoints() {
    // TODO Auto-generated method stub
    return 0;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public boolean isFormatted() {
    // TODO Auto-generated method stub
    return false;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public CellStyle getRowStyle() {
    // TODO Auto-generated method stub
    return null;
}

/**
 * <pre>
 * Not supported right now, as StreamedWorkbook
 * supports only reading.
 * </pre>
 * 
 */
public void setRowStyle(CellStyle style) {
    // TODO Auto-generated method stub
    
}

/**
 * <pre>
 * Not supported right now. Use getCellIterator instead
 * </pre>
 */
public Iterator<Cell> cellIterator() {
    // TODO Auto-generated method stub
    return null;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public Sheet getSheet() {
    // TODO Auto-generated method stub
    return null;
}

/**
 *  <pre>
 * Will be supported in future.
 *  </pre>
 */
public int getOutlineLevel() {
    // TODO Auto-generated method stub
    return 0;
}


}
