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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StaxHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Represents the excel workbook. This version of Workbook is used fore reading
 * very large excel files with a less memory footprint. It does not support
 * creation of a workbook. It uses StAX(Streaming API for XML) to stream the
 * spreadsheet. In order to reduce the memory footprint, only minimal
 * functionalities are supported.
 *
 */
public class StreamedWorkbook implements Workbook {

    private static final POILogger logger = POILogFactory.getLogger(StreamedWorkbook.class);

    private File inputFile;

    private Iterator<InputStream> sheetIterator;
    private SharedStringsTable sharedStringsTable;
    private StylesTable stylesTable;
    private XSSFReader globalReader;
    private List<StreamedSheet> sheetList = null;
    private boolean isClosed;

    private static final NamespaceContext NS_CONTEXT = new SimpleNamespaceContext();
    private static final String ACTIVE_SHEET_PATH = "//ss:workbookView/@activeTab";
    private static final String SHEET_NAME_PATH = "//ss:sheets/ss:sheet/@name";
    private static final String VISIBLE_SHEET_PATH = "//ss:sheets/ss:sheet[@state='visible']/@sheetId";
    private static final String HIDDEN_SHEET_PATH = "//ss:sheets/ss:sheet[@state='hidden']/@sheetId";
    private static final String VERY_HIDDEN_SHEET_PATH = "//ss:sheets/ss:sheet[@state='veryHidden']/@sheetId";

    /**
     * <pre>
     * Accepts the file path and return an instance of StreamedWorkBook
     * </pre>
     * 
     * @param filePath
     * @throws Exception
     */
    public StreamedWorkbook(String filePath) throws Exception {
        if ((filePath != null) && !(filePath.trim().isEmpty())) {
            inputFile = new File(filePath);

            if (inputFile != null) {
                XSSFReader reader = getExcelReader(inputFile);
                if (reader != null) {
                    this.sheetIterator = reader.getSheetsData();
                    this.sharedStringsTable = reader.getSharedStringsTable();
                    this.stylesTable = reader.getStylesTable();
                    this.globalReader = reader;
                }
            }

        } else {
            throw new Exception("No sheets found");
        }
    }

    /**
     * <pre>
     * Fetch all sheets from given excel file
     * </pre>
     * 
     * @return Iterator<StreamedSheet>
     * @throws Exception
     */
    public Iterator<StreamedSheet> getSheetIterator() throws Exception {
        return getAllSheets();
    }

    /**
     * Returns the list of sheets for the given excel file
     * 
     * @param file
     * @return Iterator<StreamedSheet>
     */
    private Iterator<StreamedSheet> getAllSheets() {
        return getStreamedSheetIterator();
    }

    private Iterator<StreamedSheet> getStreamedSheetIterator() {
        XMLInputFactory factory = StaxHelper.newXMLInputFactory();

        if (sheetIterator != null) {
            sheetList = new ArrayList<StreamedSheet>();
            int sheetNumber = 0;
            Object[] sheetNames = getValuesFromWorkbookData(SHEET_NAME_PATH);
            while (sheetIterator.hasNext()) {
                InputStream sheetInputStream = sheetIterator.next();
                StreamedSheet sheet = null;
                try {
                    sheet = createStreamedSheet(factory.createXMLEventReader(sheetInputStream), sheetNumber,
                            sheetNames[sheetNumber].toString());
                    sheetList.add(sheet);
                    sheetNumber++;
                } catch (XMLStreamException e) {
                    logger.log(POILogger.ERROR, "Exception while reading the workbook. " + e.getMessage());
                }
            }
        } else {
            throw new RuntimeException("Workbook already closed");
        }

        return sheetList.iterator();

    }

    /**
     * Creates and returns the instance of StreamedSheet
     *
     * @param parser
     * @param sheetNumber
     * @param sheetName
     * @return StreamedSheet
     */
    private StreamedSheet createStreamedSheet(XMLEventReader parser, int sheetNumber, String sheetName) {
        StreamedSheet sheet = new StreamedSheet();
        sheet.setXmlParser(parser);
        sheet.setSharedStringsTable(sharedStringsTable);
        sheet.setStylesTable(stylesTable);
        sheet.setSheetNumber(sheetNumber);
        sheet.setSheetName(sheetName);
        sheet.createEventHandler();

        return sheet;
    }

    /**
     * Receives the excel file and returns the file excel file reader
     * 
     * @param inputStream
     * @return
     * @throws Exception
     */
    private XSSFReader getExcelReader(File file) throws Exception {
        XSSFReader reader = null;
        OPCPackage pkg = null;
        pkg = OPCPackage.open(file);
        reader = new XSSFReader(pkg);
        return reader;
    }

    /**
     * <pre>
     * Convenience method to get the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     * </pre>
     * 
     * @return int
     */
    @Override
    public int getActiveSheetIndex() {

        return Integer.parseInt(getValuesFromWorkbookData(ACTIVE_SHEET_PATH)[0].toString());

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
    public void setActiveSheet(int sheetIndex) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Returns the sheet number of the first visible sheet.
     * </pre>
     * 
     * @return int
     */
    @Override
    public int getFirstVisibleTab() {
        Object[] visibleTabs = getValuesFromWorkbookData(VISIBLE_SHEET_PATH);

        return (Integer.parseInt(visibleTabs[0].toString()) - 1);
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
    public void setFirstVisibleTab(int sheetIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setSheetOrder(String sheetname, int pos) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setSelectedTab(int index) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void setSheetName(int sheet, String name) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Returns the sheet name of specified index
     * </pre>
     * 
     * @return String;
     * 
     */
    @Override
    public String getSheetName(int sheet) {
        return getValuesFromWorkbookData(SHEET_NAME_PATH)[sheet].toString();
    }

    /**
     * <pre>
     * Returns the sheet index for the sheet name given
     * </pre>
     * 
     * @return int
     * 
     */
    @Override
    public int getSheetIndex(String name) {
        int index = 0;
        for (Object sheetName : getValuesFromWorkbookData(SHEET_NAME_PATH)) {
            if (sheetName.toString().equals(name)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public int getSheetIndex(Sheet sheet) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Sheet createSheet() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public Sheet createSheet(String sheetname) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public Sheet cloneSheet(int sheetNum) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Use Iterator<StreamedSheet> getSheetIterator() instead.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Sheet> sheetIterator() {
        throw new UnsupportedOperationException("Not implemented yet. Use getSheetIterator()");
    }

    /**
     * <pre>
     *  Returns the number of sheets,
     * </pre>
     * 
     * @return int representing the number of sheets in workbook
     */
    @Override
    public int getNumberOfSheets() {
        int sheetCount = 0;

        if (this.sheetIterator != null) {
            while (sheetIterator.hasNext()) {
                sheetIterator.next();
                sheetCount++;
            }
        } else {
            logger.log(POILogger.ERROR, "Workbook already closed");
        }

        return sheetCount;
    }

    /**
     * <pre>
     *  Returns sheet at specified index
     * </pre>
     * 
     * @return Sheet for the specified index
     */
    @Override
    public Sheet getSheetAt(int index) {
        if (sheetList == null && !isClosed) {
            getAllSheets();
        }

        if (sheetList != null) {
            for (StreamedSheet sheet : sheetList) {
                if (sheet.getSheetNumber() == index) {
                    return sheet;
                }
            }
        }

        return null;
    }

    /**
     * <pre>
     * Returns the sheet with specified name
     * </pre>
     */
    @Override
    public Sheet getSheet(String name) {

        if (sheetList == null && !isClosed) {
            getAllSheets();
        }

        if (sheetList != null) {
            for (StreamedSheet sheet : sheetList) {
                if (sheet.getSheetName().equals(name)) {
                    return sheet;
                }
            }
        }

        return null;
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
    public void removeSheetAt(int index) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public Font createFont() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout,
            short typeOffset, byte underline) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public short getNumberOfFonts() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Font getFontAt(short idx) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public CellStyle createCellStyle() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public int getNumCellStyles() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * <pre>
     * Not Supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellStyle getCellStyleAt(int idx) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void write(OutputStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Close the workbook
     * </pre>
     * 
     */
    @Override
    public void close() throws IOException {
        if (sheetIterator != null) {
            while (sheetIterator.hasNext()) {
                sheetIterator.next().close();
            }
            sheetIterator = null;
        }

        if (sheetList != null) {
            sheetList.clear();
            sheetList = null;
        }

        isClosed = true;
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
    public int getNumberOfNames() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Name getName(String name) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public List<? extends Name> getNames(String name) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public List<? extends Name> getAllNames() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Name getNameAt(int nameIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Name createName() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public int getNameIndex(String name) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void removeName(int index) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void removeName(String name) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void removeName(Name name) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    @Override
    @NotImplemented
    public void setPrintArea(int sheetIndex, String reference) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public String getPrintArea(int sheetIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void removePrintArea(int sheetIndex) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public MissingCellPolicy getMissingCellPolicy() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public DataFormat createDataFormat() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public int addPicture(byte[] pictureData, int format) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    public List<? extends PictureData> getAllPictures() {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public CreationHelper getCreationHelper() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public boolean isHidden() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public void setHidden(boolean hiddenFlag) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Return true if the sheet with given index is hidden
     * </pre>
     * 
     * @return boolean
     */
    @Override
    public boolean isSheetHidden(int sheetIx) {
        // throw new UnsupportedOperationException("Not implemented yet");

        Object[] hiddenSheetIdxAry = getValuesFromWorkbookData(HIDDEN_SHEET_PATH);

        for (Object o : hiddenSheetIdxAry) {
            int index = Integer.parseInt(o.toString()) - 1;
            if (index == sheetIx) {
                return true;
            }
        }

        return false;
    }

    /**
     * <pre>
     * Return true if the sheet with given index is veryHidden
     * </pre>
     * 
     * @return boolean
     */
    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        Object[] veryHiddedSheetIdxAry = getValuesFromWorkbookData(VERY_HIDDEN_SHEET_PATH);

        for (Object o : veryHiddedSheetIdxAry) {
            int index = Integer.parseInt(o.toString()) - 1;
            if (index == sheetIx) {
                return true;
            }
        }

        return false;
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
    public void setSheetHidden(int sheetIx, boolean hidden) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void setSheetHidden(int sheetIx, int hidden) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void addToolPack(UDFFinder toopack) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public void setForceFormulaRecalculation(boolean value) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    @Override
    @NotImplemented
    public boolean getForceFormulaRecalculation() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public SpreadsheetVersion getSpreadsheetVersion() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * <pre>
     * Use Iterator<StreamedSheet> getSheetIterator() instead.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Sheet> iterator() {
        throw new UnsupportedOperationException("Not implemented yet. Use getSheetIterator()");
    }

    /**
     * <pre>
     * Returns the visibility of sheet for the given index.
     * </pre>
     * 
     * @return SheetVisibility[Hidden/Visible/VeryHidden]
     */
    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        Object[] visibleSheetIdxAry = getValuesFromWorkbookData(VISIBLE_SHEET_PATH);
        for (Object o : visibleSheetIdxAry) {
            int index = (Integer.parseInt(o.toString()) - 1);
            if (index == sheetIx) {
                return SheetVisibility.VISIBLE;
            }
        }

        if (isSheetHidden(sheetIx)) {
            return SheetVisibility.HIDDEN;
        }

        if (isSheetVeryHidden(sheetIx)) {
            return SheetVisibility.VERY_HIDDEN;
        }

        throw new IllegalArgumentException("Invalid sheet index");

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
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
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
    public int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException {
        throw new UnsupportedOperationException("Not supported due to memory footprint");
    }

    /**
     * Strictly for reading any specific value from any node in workbook.xml DOM
     * Parser is used here assuming that workbook.xml will never contribute
     * towards the size of excel file.
     *
     * @param xml
     * @param xpathQuery
     * @return Object[]
     * @throws Exception
     */
    private Object[] getValuesFromWorkbookData(String xpathQuery) {
        List<String> valueAry = new ArrayList<String>();
        DocumentBuilder builder = DocumentHelper.newDocumentBuilder();
        try {
            Document doc = builder.parse(globalReader.getWorkbookData());
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(NS_CONTEXT);
            doc.normalize();

            XPathExpression xPathExpr = xpath.compile(xpathQuery);
            NodeList nodeList = (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                valueAry.add(nodeList.item(i).getNodeValue());
            }
        } catch (Exception e) {
            logger.log(POILogger.ERROR, "Exception while reading the workbook.");
        }

        return valueAry.toArray();
    }

    private static final class SimpleNamespaceContext implements NamespaceContext {

        private final Map<String, String> map = new HashMap<String, String>();
        private final Map<String, String> reverseMap = new HashMap<String, String>();

        SimpleNamespaceContext() {
            map.put("ss", XSSFRelation.NS_SPREADSHEETML);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                reverseMap.put(entry.getValue(), entry.getKey());
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return map.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return reverseMap.get(namespaceURI);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String namespaceURI) {
            return map.keySet().iterator();
        }
    }
}