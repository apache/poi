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
import org.apache.poi.util.NotImplemented;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;

/**
 * Represents an excel sheet. StreamedSheet currently supports only the minimal
 * functionalities as additional features requires caching of sheet data, which
 * consumes memory.
 *
 */
public class StreamedSheet implements Sheet {
    private XMLEventReader xmlParser;
    private SharedStringsTable sharedStringsTable;
    private StylesTable stylesTable;
    private int numberOfColumns;
    private int sheetNumber;
    private String sheetName;
    private StreamedSheetEventHandler eventHandler = null;

    /**
     * <pre>
     * Fetch all rows from the excel.
     * </pre>
     * 
     * This method consumes only less memory, but it is advisable to use it only
     * for small excel files, since it will fetch all rows in a single call.
     * 
     * @return Iterator<Row>
     * @throws XMLStreamException
     */
    public Iterator<StreamedRow> getAllRows() throws XMLStreamException {
        return getAllRows(this, sharedStringsTable, stylesTable);
    }

    /**
     * <pre>
     * Used to fetch N number of rows.
     * </pre>
     * 
     * Recommended method to reduce memory utilization. It allows to read big
     * excel files in batch. This gives control to the user to process the
     * records already fetched, before fetching next set of records. After
     * reading N records, invoke the same method with number of rows to be
     * fetched to get the next set of rows. <br>
     * 
     * *********************Usage****************************
     * Iterator<StreamedRow> rows = sheet.getNRows(1); <br>
     * while(rows.hasNext()){ //read the first 1 row <br>
     * StreamedRow row = rows.next(); <br>
     * } <br>
     * rows = sheet.getNRows(10); <br>
     * while(rows.hasNext()){ //read the next 10 rows <br>
     * StreamedRow row = rows.next(); <br>
     * }
     * 
     * @param numberOfRows
     * @return Iterator<Row>
     * @throws XMLStreamException
     */
    public Iterator<StreamedRow> getNRows(int numberOfRows) throws XMLStreamException {
        return getNRows(this, eventHandler, numberOfRows);
    }

    public boolean hasMoreRows() {
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

    public void createEventHandler() {
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

    /*
     * @Override protected void finalize() throws Throwable { super.finalize();
     * 
     * xmlParser = null; sharedStringsTable = null; stylesTable = null;
     * eventHandler = null;
     * 
     * }
     */

    /**
     * reads all data from sheet
     * 
     * @param xmlParser
     * @param sharedStringsTable
     * @param stylesTable
     * @return
     * @throws XMLStreamException
     */
    private Iterator<StreamedRow> getAllRows(StreamedSheet sheet, SharedStringsTable sharedStringsTable,
            StylesTable stylesTable) throws XMLStreamException {
        List<StreamedRow> dataList = new ArrayList<StreamedRow>();
        StreamedSheetEventHandler eventHandler = new StreamedSheetEventHandler(sharedStringsTable, stylesTable);
        while (sheet.getXmlParser().hasNext()) {
            XMLEvent event = sheet.getXmlParser().nextEvent();
            eventHandler.handleEvent(event);
            if (eventHandler.isEndOfRow()) {
                dataList.add(eventHandler.getRow());
                eventHandler.setEndOfRow(false);
            }
            sheet.setNumberOfColumns(eventHandler.getNumberOfColumns());
        }

        return dataList.iterator();
    }

    /**
     * Reads N Rows from excel
     * 
     * @param sheet
     * @param sharedStringsTable
     * @param stylesTable
     * @param numberOFRows
     * @return
     * @throws XMLStreamException
     */
    private Iterator<StreamedRow> getNRows(StreamedSheet sheet, StreamedSheetEventHandler eventHandler,
            int numberOfRows) throws XMLStreamException {
        List<StreamedRow> dataList = new ArrayList<StreamedRow>();
        while (sheet.getXmlParser().hasNext()) {
            XMLEvent event = sheet.getXmlParser().nextEvent();
            eventHandler.handleEvent(event);
            if (eventHandler.isEndOfRow()) {
                dataList.add(eventHandler.getRow());
                eventHandler.setEndOfRow(false);
            }
            sheet.setNumberOfColumns(eventHandler.getNumberOfColumns());
            if (dataList.size() == numberOfRows) {
                break;
            }
        }

        return dataList.iterator();
    }

    /**
     * Not supported. Refer getAllRows or getNRows
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Row> iterator() {
        throw new UnsupportedOperationException("Not supported. Refer getAllRows or getNRows");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Row createRow(int rownum) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeRow(Row row) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Row getRow(int rownum) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getPhysicalNumberOfRows() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getFirstRowNum() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getLastRowNum() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setColumnHidden(int columnIndex, boolean hidden) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     *  Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isColumnHidden(int columnIndex) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setRightToLeft(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isRightToLeft() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setColumnWidth(int columnIndex, int width) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     */
    @Override
    @NotImplemented
    public int getColumnWidth(int columnIndex) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public float getColumnWidthInPixels(int columnIndex) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDefaultColumnWidth(int width) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getDefaultColumnWidth() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public short getDefaultRowHeight() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public float getDefaultRowHeightInPoints() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setDefaultRowHeight(short height) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    public void setDefaultRowHeightInPoints(float height) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellStyle getColumnStyle(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public int addMergedRegion(CellRangeAddress region) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public int addMergedRegionUnsafe(CellRangeAddress region) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void validateMergedRegions() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setVerticallyCenter(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setHorizontallyCenter(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getHorizontallyCenter() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getVerticallyCenter() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void removeMergedRegion(int index) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void removeMergedRegions(Collection<Integer> indices) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getNumMergedRegions() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    public CellRangeAddress getMergedRegion(int index) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public List<CellRangeAddress> getMergedRegions() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported. Use getAllRows or getNRows
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Row> rowIterator() {
        throw new UnsupportedOperationException("Operation not supported.Use getAllRows or getNRows.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    public void setForceFormulaRecalculation(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getForceFormulaRecalculation() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setAutobreaks(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDisplayGuts(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     *  supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDisplayZeros(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isDisplayZeros() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setFitToPage(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRowSumsBelow(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRowSumsRight(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getAutobreaks() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint..
     * </pre>
     */
    @Override
    @NotImplemented
    public boolean getDisplayGuts() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getFitToPage() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getRowSumsBelow() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getRowSumsRight() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isPrintGridlines() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setPrintGridlines(boolean show) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isPrintRowAndColumnHeadings() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setPrintRowAndColumnHeadings(boolean show) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public PrintSetup getPrintSetup() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Header getHeader() {
        // <TO DO>
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Footer getFooter() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setSelected(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public double getMargin(short margin) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setMargin(short margin, double size) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getProtect() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void protectSheet(String password) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getScenarioProtect() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @NotImplemented
    public void setZoom(int numerator, int denominator) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setZoom(int scale) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public short getTopRow() {
        // <TO DO>
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    public short getLeftCol() {
        // <TO DO>
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void showInPane(int toprow, int leftcol) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void shiftRows(int startRow, int endRow, int n) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void createFreezePane(int colSplit, int rowSplit) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public PaneInformation getPaneInformation() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDisplayGridlines(boolean show) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isDisplayGridlines() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setDisplayFormulas(boolean show) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isDisplayFormulas() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDisplayRowColHeadings(boolean show) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isDisplayRowColHeadings() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRowBreak(int row) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isRowBroken(int row) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeRowBreak(int row) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int[] getRowBreaks() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int[] getColumnBreaks() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setColumnBreak(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isColumnBroken(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeColumnBreak(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void groupColumn(int fromColumn, int toColumn) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void ungroupColumn(int fromColumn, int toColumn) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void groupRow(int fromRow, int toRow) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void ungroupRow(int fromRow, int toRow) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRowGroupCollapsed(int row, boolean collapse) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setDefaultColumnStyle(int column, CellStyle style) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void autoSizeColumn(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void autoSizeColumn(int column, boolean useMergedCells) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Comment getCellComment(CellAddress ref) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * WNot supported due to memory footprint..
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Map<CellAddress, ? extends Comment> getCellComments() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @NotImplemented
    public Drawing getDrawingPatriarch() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @NotImplemented
    public Drawing createDrawingPatriarch() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will not be supported due to memory constraints
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Workbook getWorkbook() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     *  Returns sheet name
     * </pre>
     * 
     * @return String
     */
    @Override
    public String getSheetName() {
        return sheetName;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    @NotImplemented
    public void setSheetName(String sheetName) {
        // to avoid setting sheet name by user
        if (this.sheetName == null) {
            this.sheetName = sheetName;
        }
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isSelected() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellRange<? extends Cell> setArrayFormula(String formula, CellRangeAddress range) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public DataValidationHelper getDataValidationHelper() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public List<? extends DataValidation> getDataValidations() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void addValidationData(DataValidation dataValidation) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public AutoFilter setAutoFilter(CellRangeAddress range) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public SheetConditionalFormatting getSheetConditionalFormatting() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellRangeAddress getRepeatingRows() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     */
    @Override
    @NotImplemented
    public CellRangeAddress getRepeatingColumns() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getColumnOutlineLevel(int columnIndex) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Hyperlink getHyperlink(int row, int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Hyperlink getHyperlink(CellAddress addr) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will not be supported due to memory constraints
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public List<? extends Hyperlink> getHyperlinkList() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellAddress getActiveCell() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setActiveCell(CellAddress address) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

}