/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.util.CellReference;

import java.io.*;
import java.util.Iterator;

/**
 * Initially copied from BigGridDemo "SpreadsheetWriter".
 * Unlike the original code which wrote the entire document,
 * this class only writes the "sheetData" document fragment
 * so that it was renamed to "SheetDataWriter"
 */
public class SheetDataWriter {
    private final File _fd;
    private final Writer _out;
    private int _rownum;
    private boolean _rowContainedNullCells = false;
    int _numberOfFlushedRows;
    int _lowestIndexOfFlushedRows; // meaningful only of _numberOfFlushedRows>0
    int _numberOfCellsOfLastFlushedRow; // meaningful only of _numberOfFlushedRows>0

    public SheetDataWriter() throws IOException {
        _fd = createTempFile();
        _out = createWriter(_fd);
    }

    /**
     * Create a temp file to write sheet data. 
     * By default, temp files are created in the default temporary-file directory
     * with a prefix "poi-sxssf-sheet" and suffix ".xml".  Subclasses can override 
     * it and specify a different temp directory or filename or suffix, e.g. <code>.gz</code>
     * 
     * @return temp file to write sheet data
     */
    public File createTempFile()throws IOException {
        File fd = File.createTempFile("poi-sxssf-sheet", ".xml");
        fd.deleteOnExit();
        return fd;
    }

    /**
     * Create a writer for the sheet data.
     * 
     * @param  fd the file to write to
     */
    public Writer createWriter(File fd)throws IOException {
        return new BufferedWriter(new FileWriter(fd));
    }

    /**
     * flush and close the temp data writer. 
     * This method <em>must</em> be invoked before calling {@link #getWorksheetXMLInputStream()}
     */
    public void close() throws IOException{
        _out.flush();
        _out.close();
    }

    File getTempFile(){
        return _fd;
    }
    
    /**
     * @return a stream to read temp file with the sheet data
     */
    public InputStream getWorksheetXMLInputStream() throws IOException {
        File fd = getTempFile();
        return new FileInputStream(fd);
    }

    public int getNumberOfFlushedRows() {
        return _numberOfFlushedRows;
    }

    public int getNumberOfCellsOfLastFlushedRow() {
        return _numberOfCellsOfLastFlushedRow;
    }

    public int getLowestIndexOfFlushedRows() {
        return _lowestIndexOfFlushedRows;
    }

    protected void finalize() throws Throwable {
        _fd.delete();
    }

    /**
     * Write a row to the file
     *
     * @param rownum 0-based row number
     * @param row    a row
     */
    public void writeRow(int rownum, SXSSFRow row) throws IOException {
        if (_numberOfFlushedRows == 0)
            _lowestIndexOfFlushedRows = rownum;
        _numberOfCellsOfLastFlushedRow = row.getLastCellNum();
        _numberOfFlushedRows++;
        beginRow(rownum, row);
        Iterator<Cell> cells = row.allCellsIterator();
        int columnIndex = 0;
        while (cells.hasNext()) {
            writeCell(columnIndex++, cells.next());
        }
        endRow();
    }

    void beginRow(int rownum, SXSSFRow row) throws IOException {
        _out.write("<row r=\"" + (rownum + 1) + "\"");
        if (row.hasCustomHeight())
            _out.write(" customHeight=\"true\"  ht=\"" + row.getHeightInPoints() + "\"");
        if (row.getZeroHeight())
            _out.write(" hidden=\"true\"");
        if (row.isFormatted()) {
            _out.write(" s=\"" + row._style + "\"");
            _out.write(" customFormat=\"1\"");
        }
        if (row.getOutlineLevel() != 0) {
            _out.write(" outlineLevel=\"" + row.getOutlineLevel() + "\"");
        }
        _out.write(">\n");
        this._rownum = rownum;
        _rowContainedNullCells = false;
    }

    void endRow() throws IOException {
        _out.write("</row>\n");
    }

    public void writeCell(int columnIndex, Cell cell) throws IOException {
        if (cell == null) {
            _rowContainedNullCells = true;
            return;
        }
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\"" + ref + "\"");
        CellStyle cellStyle = cell.getCellStyle();
        if (cellStyle.getIndex() != 0) _out.write(" s=\"" + cellStyle.getIndex() + "\"");
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK: {
                _out.write(">");
                break;
            }
            case Cell.CELL_TYPE_FORMULA: {
                _out.write(">");
                _out.write("<f>");
                outputQuotedString(cell.getCellFormula());
                _out.write("</f>");
                switch (cell.getCachedFormulaResultType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        double nval = cell.getNumericCellValue();
                        if (!Double.isNaN(nval)) {
                            _out.write("<v>" + nval + "</v>");
                        }
                        break;
                }
                break;
            }
            case Cell.CELL_TYPE_STRING: {
                _out.write(" t=\"inlineStr\">");
                _out.write("<is><t>");
                outputQuotedString(cell.getStringCellValue());
                _out.write("</t></is>");
                break;
            }
            case Cell.CELL_TYPE_NUMERIC: {
                _out.write(" t=\"n\">");
                _out.write("<v>" + cell.getNumericCellValue() + "</v>");
                break;
            }
            case Cell.CELL_TYPE_BOOLEAN: {
                _out.write(" t=\"b\">");
                _out.write("<v>" + (cell.getBooleanCellValue() ? "1" : "0") + "</v>");
                break;
            }
            case Cell.CELL_TYPE_ERROR: {
                FormulaError error = FormulaError.forInt(cell.getErrorCellValue());

                _out.write(" t=\"e\">");
                _out.write("<v>" + error.getString() + "</v>");
                break;
            }
            default: {
                assert false;
                throw new RuntimeException("Huh?");
            }
        }
        _out.write("</c>");
    }

    //Taken from jdk1.3/src/javax/swing/text/html/HTMLWriter.java
    protected void outputQuotedString(String s) throws IOException {
        if (s == null || s.length() == 0) {
            return;
        }

        char[] chars = s.toCharArray();
        int last = 0;
        int length = s.length();
        for (int counter = 0; counter < length; counter++) {
            char c = chars[counter];
            switch (c) {
                case '<':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    last = counter + 1;
                    _out.write("&lt;");
                    break;
                case '>':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    last = counter + 1;
                    _out.write("&gt;");
                    break;
                case '&':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    last = counter + 1;
                    _out.write("&amp;");
                    break;
                case '"':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    last = counter + 1;
                    _out.write("&quot;");
                    break;
                // Special characters
                case '\n':
                case '\r':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    _out.write("&#xa;");
                    last = counter + 1;
                    break;
                case '\t':
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    _out.write("&#x9;");
                    last = counter + 1;
                    break;
                case 0xa0:
                    if (counter > last) {
                        _out.write(chars, last, counter - last);
                    }
                    _out.write("&#xa0;");
                    last = counter + 1;
                    break;
                default:
                    // YK: XmlBeans silently replaces all ISO control characters ( < 32) with question marks.
                    // the same rule applies to unicode surrogates and "not a character" symbols.
                    if( c < ' ' || Character.isLowSurrogate(c) || Character.isHighSurrogate(c) ||
                            ('\uFFFE' <= c && c <= '\uFFFF')) {
                        _out.write('?');
                        last = counter + 1;
                    }
                    else if (c > 127) {
                        if (counter > last) {
                            _out.write(chars, last, counter - last);
                        }
                        last = counter + 1;
                        // If the character is outside of ascii, write the
                        // numeric value.
                        _out.write("&#");
                        _out.write(String.valueOf((int) c));
                        _out.write(";");
                    }
                    break;
            }
        }
        if (last < length) {
            _out.write(chars, last, length - last);
        }
    }
}
