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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;

/**
 * Represents an excel sheet.
 *
 */
public class StreamedSheet implements Sheet{
	private XMLEventReader xmlParser;
	private SharedStringsTable sharedStringsTable;
	private StylesTable stylesTable;
	private int numberOfColumns;
	private int sheetNumber;
	private StreamedSheetEventHandler eventHandler = null;
	
	/**
	 * <pre>
	 * Fetch all rows from the excel.
	 * </pre>
	 * 
	 * This method consumes only less memory, but it is 
	 * advisable to use it only for small excel files, since 
	 * it will fetch all rows in a single call.
	 * 
	 * @return Iterator<Row>
	 * @throws XMLStreamException
	 */
	public Iterator<StreamedRow> getAllRows() throws XMLStreamException{
		return getAllRows(this, sharedStringsTable, stylesTable);
	}
	
	/**
	 * <pre>
	 * Used to fetch N number of rows.
	 * </pre>
	 * 
	 * Recommended method to reduce memory utilization.
	 * It allows to read big excel files in batch.
	 * This gives control to the user to process the records already fetched, 
	 * before fetching next set of records.
	 * After reading N records, invoke the same method with number of rows to be
	 * fetched to get the next set of rows.
	 * <br>
	 * 
	 * *********************Usage****************************
	 * Iterator<StreamedRow> rows = sheet.getNRows(1);
	 * <br>
	 * while(rows.hasNext()){ //read the first 1 row
	 * <br>
	 *     StreamedRow row = rows.next();
	 * <br>
	 * }
	 *  <br>
	 * rows = sheet.getNRows(10);
	 *  <br>
     * while(rows.hasNext()){ //read the next 10 rows
     *  <br>
     *     StreamedRow row = rows.next();
     *  <br>
     * }
	 * 
	 * @param numberOfRows
	 * @return Iterator<Row>
	 * @throws XMLStreamException
	 */
	public Iterator<StreamedRow> getNRows(int numberOfRows) throws XMLStreamException{
		return getNRows(this, eventHandler, numberOfRows);
	}

	public boolean hasMoreRows(){
		return xmlParser.hasNext();
	}
	
	
	public XMLEventReader getXmlParser() {
		return xmlParser;
	}

	public void setXmlParser(XMLEventReader xmlParser) {
		this.xmlParser = xmlParser;
	}

	public void setSharedStringsTable(SharedStringsTable sharedStringsTable) {
		this.sharedStringsTable = sharedStringsTable;
	}

	public void setStylesTable(StylesTable stylesTable) {
		this.stylesTable = stylesTable;
	}
	
	public void createEventHandler(){
		eventHandler = new StreamedSheetEventHandler(sharedStringsTable, stylesTable);
	}


	public int getNumberOfColumns() {
		return numberOfColumns;
	}


	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}


	public int getSheetNumber() {
		return sheetNumber;
	}


	public void setSheetNumber(int sheetNumber) {
		this.sheetNumber = sheetNumber;
	}

/*	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
		xmlParser = null;
		sharedStringsTable = null;
		stylesTable = null;
		eventHandler = null;
		
	}*/
	
	 /**
     * reads all data from sheet
     * @param xmlParser
     * @param sharedStringsTable
     * @param stylesTable
     * @return
     * @throws XMLStreamException 
     */
    private Iterator<StreamedRow> getAllRows(StreamedSheet sheet,
            SharedStringsTable sharedStringsTable, StylesTable stylesTable) throws XMLStreamException {
        List<StreamedRow> dataList = new ArrayList<StreamedRow>();
        StreamedSheetEventHandler eventHandler = new StreamedSheetEventHandler(sharedStringsTable, stylesTable);
        while(sheet.getXmlParser().hasNext()){
            XMLEvent event = sheet.getXmlParser().nextEvent();  
            eventHandler.handleEvent(event);
            if(eventHandler.isEndOfRow()){
                dataList.add(eventHandler.getRow());
                eventHandler.setEndOfRow(false);
            }
            sheet.setNumberOfColumns(eventHandler.getNumberOfColumns());
        }
        
        return dataList.iterator();
    }
    
    /**
     * Reads N Rows from excel
     * @param sheet
     * @param sharedStringsTable
     * @param stylesTable
     * @param numberOFRows
     * @return
     * @throws XMLStreamException
     */
    private Iterator<StreamedRow> getNRows(StreamedSheet sheet, StreamedSheetEventHandler eventHandler, int numberOfRows) throws XMLStreamException {
        List<StreamedRow> dataList = new ArrayList<StreamedRow>();      
        while(sheet.getXmlParser().hasNext()){
            XMLEvent event = sheet.getXmlParser().nextEvent();  
            eventHandler.handleEvent(event);
            if(eventHandler.isEndOfRow()){
                dataList.add(eventHandler.getRow());
                eventHandler.setEndOfRow(false);
            }
            sheet.setNumberOfColumns(eventHandler.getNumberOfColumns());
            if(dataList.size() == numberOfRows){
                break;
            }
        }
        
        return dataList.iterator();
    }

    /**
     * Not supported. Refer getAllRows or getNRows
     */
    public Iterator<Row> iterator() {
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
    public Row createRow(int rownum) {
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
    public void removeRow(Row row) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     */
    public Row getRow(int rownum) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     */
    public int getPhysicalNumberOfRows() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getFirstRowNum() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     */
    public int getLastRowNum() {
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
    public void setColumnHidden(int columnIndex, boolean hidden) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     */
    public boolean isColumnHidden(int columnIndex) {
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
    public void setRightToLeft(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public boolean isRightToLeft() {
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
    public void setColumnWidth(int columnIndex, int width) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getColumnWidth(int columnIndex) {
        // TODO Auto-generated method stub
        return 0;
    }


    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public float getColumnWidthInPixels(int columnIndex) {
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
    public void setDefaultColumnWidth(int width) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getDefaultColumnWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public short getDefaultRowHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public float getDefaultRowHeightInPoints() {
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
    public void setDefaultRowHeight(short height) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void setDefaultRowHeightInPoints(float height) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellStyle getColumnStyle(int column) {
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
    public int addMergedRegion(CellRangeAddress region) {
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
    public int addMergedRegionUnsafe(CellRangeAddress region) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public void validateMergedRegions() {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setVerticallyCenter(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setHorizontallyCenter(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getHorizontallyCenter() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getVerticallyCenter() {
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
    public void removeMergedRegion(int index) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void removeMergedRegions(Collection<Integer> indices) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Not supported due to memory footprint.
     *  </pre>
     */
    public int getNumMergedRegions() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Not supported due to memory footprint.
     *  </pre>
     */
    public CellRangeAddress getMergedRegion(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Not supported due to memory footprint.
     *  </pre>
     */
    public List<CellRangeAddress> getMergedRegions() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported. Use getAllRows or getNRows
     * </pre>
     */
    public Iterator<Row> rowIterator() {
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
    public void setForceFormulaRecalculation(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getForceFormulaRecalculation() {
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
    public void setAutobreaks(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setDisplayGuts(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public void setDisplayZeros(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isDisplayZeros() {
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
    public void setFitToPage(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setRowSumsBelow(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setRowSumsRight(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getAutobreaks() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getDisplayGuts() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getFitToPage() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getRowSumsBelow() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getRowSumsRight() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isPrintGridlines() {
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
    public void setPrintGridlines(boolean show) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isPrintRowAndColumnHeadings() {
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
    public void setPrintRowAndColumnHeadings(boolean show) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public PrintSetup getPrintSetup() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Header getHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Footer getFooter() {
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
    public void setSelected(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public double getMargin(short margin) {
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
    public void setMargin(short margin, double size) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getProtect() {
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
    public void protectSheet(String password) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean getScenarioProtect() {
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
    public void setZoom(int numerator, int denominator) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setZoom(int scale) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public short getTopRow() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public short getLeftCol() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public void showInPane(int toprow, int leftcol) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void shiftRows(int startRow, int endRow, int n) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void createFreezePane(int colSplit, int rowSplit,
            int leftmostColumn, int topRow) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void createFreezePane(int colSplit, int rowSplit) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void createSplitPane(int xSplitPos, int ySplitPos,
            int leftmostColumn, int topRow, int activePane) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public PaneInformation getPaneInformation() {
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
    public void setDisplayGridlines(boolean show) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isDisplayGridlines() {
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
    public void setDisplayFormulas(boolean show) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isDisplayFormulas() {
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
    public void setDisplayRowColHeadings(boolean show) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isDisplayRowColHeadings() {
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
    public void setRowBreak(int row) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isRowBroken(int row) {
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
    public void removeRowBreak(int row) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int[] getRowBreaks() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int[] getColumnBreaks() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setColumnBreak(int column) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isColumnBroken(int column) {
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
    public void removeColumnBreak(int column) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void groupColumn(int fromColumn, int toColumn) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void ungroupColumn(int fromColumn, int toColumn) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void groupRow(int fromRow, int toRow) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void ungroupRow(int fromRow, int toRow) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setRowGroupCollapsed(int row, boolean collapse) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setDefaultColumnStyle(int column, CellStyle style) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void autoSizeColumn(int column) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void autoSizeColumn(int column, boolean useMergedCells) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Comment getCellComment(int row, int column) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Comment getCellComment(CellAddress ref) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Map<CellAddress, ? extends Comment> getCellComments() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Drawing getDrawingPatriarch() {
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
    public Drawing createDrawingPatriarch() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will not be supported due to memory constraints
     * </pre>
     */
    public Workbook getWorkbook() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public String getSheetName() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public boolean isSelected() {
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
    public CellRange<? extends Cell> setArrayFormula(String formula,
            CellRangeAddress range) {
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
    public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public DataValidationHelper getDataValidationHelper() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public List<? extends DataValidation> getDataValidations() {
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
    public void addValidationData(DataValidation dataValidation) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public AutoFilter setAutoFilter(CellRangeAddress range) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public SheetConditionalFormatting getSheetConditionalFormatting() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellRangeAddress getRepeatingRows() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellRangeAddress getRepeatingColumns() {
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
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
        // TODO Auto-generated method stub
        
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public int getColumnOutlineLevel(int columnIndex) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Hyperlink getHyperlink(int row, int column) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public Hyperlink getHyperlink(CellAddress addr) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * <pre>
     * Will not be supported due to memory constraints
     * </pre>
     */
    public List<? extends Hyperlink> getHyperlinkList() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  <pre>
     * Will be supported in future.
     *  </pre>
     */
    public CellAddress getActiveCell() {
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
    public void setActiveCell(CellAddress address) {
        // TODO Auto-generated method stub
        
    }



	
	
	
}
