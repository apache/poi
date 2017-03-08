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

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Represents cell in a row
 * Value of cell is represented as a string.
 *
 */
public class StreamedCell  implements Cell{
	private String value;
	private int cellNumber;
	
	public StreamedCell(){
		
	}

	/**
	 * <pre>
	 * Return cell value
	 * </pre>
	 * Return the value of a cell in String format.
	 * Value will be same as how it is represented in excel.
	 * @return String
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString(){
		return value;
	}

	/**
	 * <pre>
	 * Returns the cell number
	 * </pre>
	 *
	 * @return
	 */
    public int getCellNumber() {
        return cellNumber;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getColumnIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getRowIndex() {
        // TODO Auto-generated method stub
        return 0;
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
    public Row getRow() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */    
    public void setCellType(int cellType) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellType(CellType cellType) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getCellType() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellType getCellTypeEnum() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getCachedFormulaResultType() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellType getCachedFormulaResultTypeEnum() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellValue(double value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellValue(Date value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellValue(Calendar value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellValue(RichTextString value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellValue(String value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * @param cellNumber
     */
    public void setCellFormula(String formula) throws FormulaParseException {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public String getCellFormula() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public double getNumericCellValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Date getDateCellValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public RichTextString getRichStringCellValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     *  Returns the String value of cell content
     *  </pre>
     */
    public String getStringCellValue() {
        return value;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setCellValue(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setCellErrorValue(byte value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getBooleanCellValue() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public byte getErrorCellValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setCellStyle(CellStyle style) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellStyle getCellStyle() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setAsActiveCell() {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellAddress getAddress() {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setCellComment(Comment comment) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Comment getCellComment() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void removeCellComment() {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Hyperlink getHyperlink() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void setHyperlink(Hyperlink link) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     *</pre>
     * 
     */
    public void removeHyperlink() {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellRangeAddress getArrayFormulaRange() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isPartOfArrayFormulaGroup() {
        // TODO Auto-generated method stub
        return false;
    }

	/*@Override
	protected void finalize() throws Throwable {
		super.finalize();
		value = null;
	}*/
	
	
	
}
