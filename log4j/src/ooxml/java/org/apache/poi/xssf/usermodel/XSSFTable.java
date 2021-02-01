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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.usermodel.TableStyleInfo;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;

/**
 * 
 * This class implements the Table Part (Open Office XML Part 4: chapter 3.5.1)
 * 
 * Columns of this table may contains mappings to a subtree of an XML. The root
 * element of this subtree can occur multiple times (one for each row of the
 * table). The child nodes of the root element can be only attributes or
 * elements with maxOccurs=1 property set.
 * 
 *
 * @author Roberto Manicardi
 */
public class XSSFTable extends POIXMLDocumentPart implements Table {

    private CTTable ctTable;
    private transient List<XSSFXmlColumnPr> xmlColumnPrs;
    private transient List<XSSFTableColumn> tableColumns;
    private transient HashMap<String, Integer> columnMap;
    private transient CellReference startCellReference;
    private transient CellReference endCellReference;    
    private transient String commonXPath; 
    private transient String name;
    private transient String styleName;

    /**
     * empty implementation, not attached to a workbook/worksheet yet
     */
    public XSSFTable() {
        super();
        ctTable = CTTable.Factory.newInstance();
    }

    /** 
     * @param part The part used to initialize the table
     * @throws IOException If reading data from the part fails.
     * @since POI 3.14-Beta1
     */
    public XSSFTable(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    /**
     * Read table XML from an {@link InputStream}
     * @param is The stream which provides the XML data for the table.
     * @throws IOException If reading from the stream fails
     */
    public void readFrom(InputStream is) throws IOException {
        try {
            TableDocument doc = TableDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctTable = doc.getTable();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    
    /**
     * @return owning sheet
     */
    public XSSFSheet getXSSFSheet(){
        return (XSSFSheet) getParent();
    }

    /**
     * write table XML to an {@link OutputStream}
     * @param out The stream to write the XML data to
     * @throws IOException If writing to the stream fails.
     */
    public void writeTo(OutputStream out) throws IOException {
        updateHeaders();

        TableDocument doc = TableDocument.Factory.newInstance();
        doc.setTable(ctTable);
        doc.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }
    
    /**
      * get the underlying CTTable XML bean
     * @return underlying OOXML object
      */
    @Internal(since="POI 3.15 beta 3")
    public CTTable getCTTable() {
        return ctTable;
    }
    
    /**
     * Checks if this Table element contains even a single mapping to the map identified by id
     * @param id the XSSFMap ID
     * @return true if the Table element contain mappings
     */
    public boolean mapsTo(long id){
        List<XSSFXmlColumnPr> pointers = getXmlColumnPrs();
        
        for (XSSFXmlColumnPr pointer: pointers) {
            if (pointer.getMapId()==id) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * Calculates the xpath of the root element for the table. This will be the common part
     * of all the mapping's xpaths
     * Note: this function caches the result for performance. To flush the cache {@link #updateHeaders()} must be called.
     * 
     * @return the xpath of the table's root element
     */
    public String getCommonXpath() {
        if (commonXPath == null) {
            String[] commonTokens = {};
            for (XSSFTableColumn column : getColumns()) {
                if (column.getXmlColumnPr()!=null) {
                    String xpath = column.getXmlColumnPr().getXPath();
                    String[] tokens =  xpath.split("/");
                    if (commonTokens.length==0) {
                        commonTokens = tokens;
                        
                    } else {
                        final int maxLength = Math.min(commonTokens.length, tokens.length);
                         
                        for (int i =0; i<maxLength; i++) {
                            if (!commonTokens[i].equals(tokens[i])) {
                             List<String> subCommonTokens = Arrays.asList(commonTokens).subList(0, i);
                             
                             String[] container = {};
                             
                             commonTokens = subCommonTokens.toArray(container);
                             break;
                            }
                        }
                    }
                }
            }

            commonTokens[0] = "";
            commonXPath = StringUtil.join(commonTokens, "/");
        }
        
        return commonXPath;
    }

    /**
     * Note this list is static - once read, it does not notice later changes to the underlying column structures
     * To clear the cache, call {@link #updateHeaders}
     * @return List of XSSFTableColumn
     * @since 4.0.0
     */
    public List<XSSFTableColumn> getColumns() {
        if (tableColumns == null) {
           List<XSSFTableColumn> columns = new ArrayList<>();
            CTTableColumns ctTableColumns = ctTable.getTableColumns();
            if (ctTableColumns != null) {
                for (CTTableColumn column : ctTableColumns.getTableColumnList()) {
                    XSSFTableColumn tableColumn = new XSSFTableColumn(this, column);
                    columns.add(tableColumn);
                }
            }
            tableColumns = Collections.unmodifiableList(columns);
        }
        return tableColumns;
    }

    /**
     * Use {@link XSSFTableColumn#getXmlColumnPr()} instead.
     */
    private List<XSSFXmlColumnPr> getXmlColumnPrs() {
        if (xmlColumnPrs == null) {
            xmlColumnPrs = new ArrayList<>();
            for (XSSFTableColumn column: getColumns()) {
                XSSFXmlColumnPr xmlColumnPr = column.getXmlColumnPr();
                if (xmlColumnPr != null) {
                    xmlColumnPrs.add(xmlColumnPr);
                }
            }
        }
        return xmlColumnPrs;
    }
    
    /**
     * Add a new column to the right end of the table.
     * 
     * @param columnName
     *            the unique name of the column, must not be {@code null}
     * @return the created table column
     * @since 4.0.0
     */
    public XSSFTableColumn createColumn(String columnName) {
        return createColumn(columnName, getColumnCount());
    }
    
    /**
     * Adds a new column to the table.
     * 
     * @param columnName
     *            the unique name of the column, or {@code null} for a generated name
     * @param columnIndex
     *            the 0-based position of the column in the table
     * @return the created table column
     * @throws IllegalArgumentException
     *             if the column name is not unique or missing or if the column
     *             can't be created at the given index
     * @since 4.0.0
     */
    public XSSFTableColumn createColumn(String columnName, int columnIndex) {
                
        int columnCount = getColumnCount();
        if(columnIndex < 0 || columnIndex > columnCount) {
            throw new IllegalArgumentException("Column index out of bounds");
        }
        
        // Ensure we have Table Columns
        CTTableColumns columns = ctTable.getTableColumns();
        if (columns == null) {
            columns = ctTable.addNewTableColumns();
        }
        
        // check if name is unique and calculate unique column id 
        long nextColumnId = 0; 
        for (XSSFTableColumn tableColumn : getColumns()) {
            if (columnName != null && columnName.equalsIgnoreCase(tableColumn.getName())) {
                throw new IllegalArgumentException("Column '" + columnName
                        + "' already exists. Column names must be unique per table.");
            }
            nextColumnId = Math.max(nextColumnId, tableColumn.getId());
        }
        // Bug #62740, the logic was just re-using the existing max ID, not incrementing beyond it.
        nextColumnId++;
        
        // Add the new Column
        CTTableColumn column = columns.insertNewTableColumn(columnIndex);
        columns.setCount(columns.sizeOfTableColumnArray());
        
        column.setId(nextColumnId);
        if(columnName != null) {
            column.setName(columnName); 
        } else {
            column.setName("Column " + nextColumnId);
        }
        
        if (ctTable.getRef() != null) {
            // calculate new area
            int newColumnCount = columnCount + 1;
            CellReference tableStart = getStartCellReference();
            CellReference tableEnd = getEndCellReference();
            SpreadsheetVersion version = getXSSFSheet().getWorkbook().getSpreadsheetVersion();
            CellReference newTableEnd = new CellReference(tableEnd.getRow(),
                    tableStart.getCol() + newColumnCount - 1);
            AreaReference newTableArea = new AreaReference(tableStart, newTableEnd, version);

            setCellRef(newTableArea);
        }
        
        updateHeaders();
        
        return getColumns().get(columnIndex);
    }
    
    /**
     * Remove a column from the table.
     *
     * @param column
     *            the column to remove
     * @since 4.0.0
     */
    public void removeColumn(XSSFTableColumn column) {
        int columnIndex = getColumns().indexOf(column);
        if (columnIndex >= 0) {
            ctTable.getTableColumns().removeTableColumn(columnIndex);
            updateReferences();
            updateHeaders();
        }
    }
    
    /**
     * Remove a column from the table.
     *
     * @param columnIndex
     *            the 0-based position of the column in the table
     * @throws IllegalArgumentException
     *             if no column at the index exists or if the table has only a
     *             single column
     * @since 4.0.0
     */
    public void removeColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex > getColumnCount() - 1) {
            throw new IllegalArgumentException("Column index out of bounds");
        }
        
        if(getColumnCount() == 1) {
            throw new IllegalArgumentException("Table must have at least one column");
        }
        
        CTTableColumns tableColumns = ctTable.getTableColumns();
        tableColumns.removeTableColumn(columnIndex);
        tableColumns.setCount(tableColumns.getTableColumnList().size());
        updateReferences();
        updateHeaders();
    }
    
    /**
     * @return the name of the Table, if set
     */
    public String getName() {
        if (name == null && ctTable.getName() != null) {
            setName(ctTable.getName());
        }
        return name;
    }
    
    /**
     * Changes the name of the Table
     * @param newName The name of the table.
     */
    public void setName(String newName) {
        if (newName == null) {
            ctTable.unsetName();
            name = null;
            return;
        }
        ctTable.setName(newName);
        name = newName;
    }

    /**
     * @return the table style name, if set
     * @since 3.17 beta 1
     */
    public String getStyleName() {
        if (styleName == null && ctTable.isSetTableStyleInfo()) {
            setStyleName(ctTable.getTableStyleInfo().getName());
        }
        return styleName;
    }
    
    /**
     * Changes the name of the Table
     * @param newStyleName The name of the style.
     * @since 3.17 beta 1
     */
    public void setStyleName(String newStyleName) {
        if (newStyleName == null) {
            if (ctTable.isSetTableStyleInfo()) {
                ctTable.getTableStyleInfo().unsetName();
            }
            styleName = null;
            return;
        }
        if (! ctTable.isSetTableStyleInfo()) {
            ctTable.addNewTableStyleInfo();
        }
        ctTable.getTableStyleInfo().setName(newStyleName);
        styleName = newStyleName;
    }
    
    /**
     * @return the display name of the Table, if set
     */
    public String getDisplayName() {
        return ctTable.getDisplayName();
    }

    /**
     * Changes the display name of the Table
     * @param name to use
     */
    public void setDisplayName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Display name must not be null or empty");
        }
        ctTable.setDisplayName(name);
    }

    /**
     * Get the area reference for the cells which this table covers. The area
     * includes header rows and totals rows.
     *
     * Does not track updates to underlying changes to CTTable To synchronize
     * with changes to the underlying CTTable, call {@link #updateReferences()}.
     * 
     * @return the area of the table
     * @see "Open Office XML Part 4: chapter 3.5.1.2, attribute ref"
     * @since 3.17 beta 1
     */
    public AreaReference getCellReferences() {
        return new AreaReference(
                getStartCellReference(),
                getEndCellReference(),
                SpreadsheetVersion.EXCEL2007
        );
    }
    
    /**
     * Set the area reference for the cells which this table covers. The area
     * includes includes header rows and totals rows. Automatically synchronizes
     * any changes by calling {@link #updateHeaders()}.
     * 
     * Note: The area's width should be identical to the amount of columns in
     * the table or the table may be invalid. All header rows, totals rows and
     * at least one data row must fit inside the area. Updating the area with
     * this method does not create or remove any columns and does not change any
     * cell values.
     * 
     * @see "Open Office XML Part 4: chapter 3.5.1.2, attribute ref"
     * @since 3.17 beta 1
     */
    public void setCellReferences(AreaReference refs) {
        setCellRef(refs);
    }
    
    @Internal
    protected void setCellRef(AreaReference refs) {
        
        // Strip the sheet name,
        // CTWorksheet.getTableParts defines in which sheet the table is
        String ref = refs.formatAsString();
        if (ref.indexOf('!') != -1) {
            ref = ref.substring(ref.indexOf('!')+1);
        }
        
        // Update
        ctTable.setRef(ref);
        if (ctTable.isSetAutoFilter()) {
            String filterRef;
            int totalsRowCount = getTotalsRowCount();
            if (totalsRowCount == 0) {
                filterRef = ref;
            } else {
                final CellReference start = new CellReference(refs.getFirstCell().getRow(), refs.getFirstCell().getCol());
                // account for footer row(s) in auto-filter range, which doesn't include footers
                final CellReference end = new CellReference(refs.getLastCell().getRow() - totalsRowCount, refs.getLastCell().getCol());
                // this won't have sheet references because we built the cell references without them
                filterRef = new AreaReference(start, end, SpreadsheetVersion.EXCEL2007).formatAsString();
            }
            ctTable.getAutoFilter().setRef(filterRef);
        }
        
        // Have everything recomputed
        updateReferences();
        updateHeaders();
    }
    
    /**
     * Set the area reference for the cells which this table covers. The area
     * includes includes header rows and totals rows.
     * 
     * Updating the area with this method will create new column as necessary to
     * the right side of the table but will not modify any cell values.
     * 
     * @param tableArea
     *            the new area of the table
     * @throws IllegalArgumentException
     *             if the area is {@code null} or not
     * @since 4.0.0
     */
    public void setArea(AreaReference tableArea) {

        if (tableArea == null) {
            throw new IllegalArgumentException("AreaReference must not be null");
        }
        
        String areaSheetName = tableArea.getFirstCell().getSheetName();
        if (areaSheetName != null && !areaSheetName.equals(getXSSFSheet().getSheetName())) {
            // TODO to move a table from one sheet to another
            // CTWorksheet.getTableParts needs to be updated on both sheets
            throw new IllegalArgumentException(
                    "The AreaReference must not reference a different sheet");
        }
        
        int rowCount = (tableArea.getLastCell().getRow() - tableArea.getFirstCell().getRow()) + 1;
        int minimumRowCount = 1 + getHeaderRowCount() + getTotalsRowCount();
        if (rowCount < minimumRowCount) {
            throw new IllegalArgumentException("AreaReference needs at least " + minimumRowCount
                    + " rows, to cover at least one data row and all header rows and totals rows");
        }

        // Strip the sheet name,
        // CTWorksheet.getTableParts defines in which sheet the table is
        String ref = tableArea.formatAsString();
        if (ref.indexOf('!') != -1) {
            ref = ref.substring(ref.indexOf('!') + 1);
        }

        // Update
        ctTable.setRef(ref);
        if (ctTable.isSetAutoFilter()) {
            ctTable.getAutoFilter().setRef(ref);
        }
        updateReferences();

        // add or remove columns on the right side of the table
        int columnCount = getColumnCount();
        int newColumnCount = (tableArea.getLastCell().getCol() - tableArea.getFirstCell().getCol()) + 1;
        if (newColumnCount > columnCount) {
            for (int i = columnCount; i < newColumnCount; i++) {
                createColumn(null, i);
            }
        } else if (newColumnCount < columnCount) {
            for (int i = columnCount; i > newColumnCount; i--) {
                removeColumn(i -1);
            }
        }

        updateHeaders();
    }
    
    /**
     * Get the area that this table covers.
     *
     * @return the table's area or {@code null} if the area has not been
     *         initialized
     * @since 4.0.0
     */
    public AreaReference getArea() {
        String ref = ctTable.getRef();
        if (ref != null) {
            SpreadsheetVersion version = getXSSFSheet().getWorkbook().getSpreadsheetVersion();
            return new AreaReference(ctTable.getRef(), version);
        } else {
            return null;
        }
    }
    
    /**
     * @return The reference for the cell in the top-left part of the table
     * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref) 
     *
     * Does not track updates to underlying changes to CTTable
     * To synchronize with changes to the underlying CTTable,
     * call {@link #updateReferences()}.
     */
    public CellReference getStartCellReference() {
        if (startCellReference==null) {
             setCellReferences();
        }
        return startCellReference;
    }
    
    /**
     * @return The reference for the cell in the bottom-right part of the table
     * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref)
     *
     * Does not track updates to underlying changes to CTTable
     * To synchronize with changes to the underlying CTTable,
     * call {@link #updateReferences()}.
     */
    public CellReference getEndCellReference() {
        if (endCellReference==null) {
             setCellReferences();
        }
        return endCellReference;
    }

    /**
     * @since POI 3.15 beta 3
     */
    private void setCellReferences() {
        String ref = ctTable.getRef();
        if (ref != null) {
            String[] boundaries = ref.split(":", 2);
            String from = boundaries[0];
            String to = boundaries.length == 2 ? boundaries[1] : boundaries[0];
            startCellReference = new CellReference(from);
            endCellReference = new CellReference(to);
        }
    }

    
    /**
     * Clears the cached values set by {@link #getStartCellReference()}
     * and {@link #getEndCellReference()}.
     * The next call to {@link #getStartCellReference()} and
     * {@link #getEndCellReference()} will synchronize the
     * cell references with the underlying <code>CTTable</code>.
     * Thus this method is inexpensive.
     *
     * @since POI 3.15 beta 3
     */
    public void updateReferences() {
        startCellReference = null;
        endCellReference = null;
    }

    
    /**
     * Get the total number of rows in this table, including all
     * {@linkplain #getHeaderRowCount() header rows} and all
     * {@linkplain #getTotalsRowCount() totals rows}. (Note: in this version
     * autofiltering is ignored)
     * 
     * Returns <code>0</code> if the start or end cell references are not set.
     * 
     * Does not track updates to underlying changes to CTTable To synchronize
     * with changes to the underlying CTTable, call {@link #updateReferences()}.
     * 
     * @return the total number of rows
     */
    public int getRowCount() {
        CellReference from = getStartCellReference();
        CellReference to = getEndCellReference();
        
        int rowCount = 0;
        if (from!=null && to!=null) {
            rowCount = to.getRow() - from.getRow() + 1;
        }
        return rowCount;
    }
    
    /**
     * Get the number of data rows in this table. This does not include any
     * header rows or totals rows.
     * 
     * Returns <code>0</code> if the start or end cell references are not set.
     * 
     * Does not track updates to underlying changes to CTTable To synchronize
     * with changes to the underlying CTTable, call {@link #updateReferences()}.
     * 
     * @return the number of data rows
     * @since 4.0.0
     */
    public int getDataRowCount() {
        CellReference from = getStartCellReference();
        CellReference to = getEndCellReference();

        int rowCount = 0;
        if (from != null && to != null) {
            rowCount = (to.getRow() - from.getRow() + 1) - getHeaderRowCount()
                    - getTotalsRowCount();
        }
        return rowCount;
    }

    /**
     * Set the number of rows in the data area of the table. This does not
     * affect any header rows or totals rows.
     * 
     * If the new row count is less than the current row count, superfluous rows
     * will be cleared. If the new row count is greater than the current row
     * count, cells below the table will be overwritten by the table.
     * 
     * To resize the table without overwriting cells, use
     * {@link #setArea(AreaReference)} instead.
     *
     * @param newDataRowCount
     *            new row count for the table
     * @throws IllegalArgumentException
     *             if the row count is less than 1
     * @since 4.0.0
     */
    public void setDataRowCount(int newDataRowCount) {

        if (newDataRowCount < 1) {
            throw new IllegalArgumentException("Table must have at least one data row");
        }

        updateReferences();
        int dataRowCount = getDataRowCount();
        if (dataRowCount == newDataRowCount) {
            return;
        }

        CellReference tableStart = getStartCellReference();
        CellReference tableEnd = getEndCellReference();
        SpreadsheetVersion version = getXSSFSheet().getWorkbook().getSpreadsheetVersion();

        // calculate new area
        int newTotalRowCount = getHeaderRowCount() + newDataRowCount + getTotalsRowCount();
        CellReference newTableEnd = new CellReference(tableStart.getRow() + newTotalRowCount - 1,
                tableEnd.getCol());
        AreaReference newTableArea = new AreaReference(tableStart, newTableEnd, version);

        // clear cells
        CellReference clearAreaStart;
        CellReference clearAreaEnd;
        if (newDataRowCount < dataRowCount) {
            // table size reduced -
            // clear all table cells that are outside of the new area
            clearAreaStart = new CellReference(newTableArea.getLastCell().getRow() + 1,
                    newTableArea.getFirstCell().getCol());
            clearAreaEnd = tableEnd;
        } else {
            // table size increased -
            // clear all cells below the table that are inside the new area
            clearAreaStart = new CellReference(tableEnd.getRow() + 1,
                    newTableArea.getFirstCell().getCol());
            clearAreaEnd = newTableEnd;
        }
        AreaReference areaToClear = new AreaReference(clearAreaStart, clearAreaEnd, version);
        for (CellReference cellRef : areaToClear.getAllReferencedCells()) {
            XSSFRow row = getXSSFSheet().getRow(cellRef.getRow());
            if (row != null) {
                XSSFCell cell = row.getCell(cellRef.getCol());
                if (cell != null) {
                    cell.setBlank();
                    cell.setCellStyle(null);
                }
            }
        }

        // update table area
        setCellRef(newTableArea);
    }

    /**
     * Get the total number of columns in this table.
     *
     * @return the column count
     * @since 4.0.0
     */
    public int getColumnCount() {
        CTTableColumns tableColumns = ctTable.getTableColumns();
        if(tableColumns == null) {
            return 0;
        }
        // Casting to int should be safe here - tables larger than the
        // sheet (which holds the actual data of the table) can't exists.
        return (int) tableColumns.getCount();
    }

    /**
     * Synchronize table headers with cell values in the parent sheet.
     * Headers <em>must</em> be in sync, otherwise Excel will display a
     * "Found unreadable content" message on startup.
     * 
     * If calling both {@link #updateReferences()} and
     * this method, {@link #updateReferences()}
     * should be called first.
     * 
     * Note that a Table <em>must</em> have a header. To reproduce
     *  the equivalent of inserting a table in Excel without Headers,
     *  manually add cells with values of "Column1", "Column2" etc first. 
     */
    public void updateHeaders() {
        XSSFSheet sheet = (XSSFSheet)getParent();
        CellReference ref = getStartCellReference();
        if (ref == null) return;

        int headerRow = ref.getRow();
        int firstHeaderColumn = ref.getCol();
        XSSFRow row = sheet.getRow(headerRow);
        DataFormatter formatter = new DataFormatter();

        if (row != null && row.getCTRow().validate()) {
            int cellnum = firstHeaderColumn;
            CTTableColumns ctTableColumns = getCTTable().getTableColumns();
            if(ctTableColumns != null) {
                for (CTTableColumn col : ctTableColumns.getTableColumnList()) {
                    XSSFCell cell = row.getCell(cellnum);
                    if (cell != null) {
                        col.setName(formatter.formatCellValue(cell));
                    }
                    cellnum++;
                }
            }
        }
        tableColumns = null;
        columnMap = null;
        xmlColumnPrs = null;
        commonXPath = null;
    }

    private static String caseInsensitive(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    /**
     * Gets the relative column index of a column in this table having the header name <code>column</code>.
     * The column index is relative to the left-most column in the table, 0-indexed.
     * Returns <code>-1</code> if <code>column</code> is not a header name in table.
     *
     * Column Header names are case-insensitive
     *
     * Note: this function caches column names for performance. To flush the cache (because columns
     * have been moved or column headers have been changed), {@link #updateHeaders()} must be called.
     *
     * @since 3.15 beta 2
     */
    public int findColumnIndex(String columnHeader) {
        if (columnHeader == null) return -1;
        if (columnMap == null) {
            // FIXME: replace with org.apache.commons.collections.map.CaseInsensitiveMap
            final int count = getColumnCount();
            columnMap = new HashMap<>(count * 3 / 2);
            
            int i = 0;
            for (XSSFTableColumn column : getColumns()) {
                String columnName = column.getName();
                columnMap.put(caseInsensitive(columnName), i);
                i++;
            }
        }
        // Table column names with special characters need a single quote escape
        // but the escape is not present in the column definition
        Integer idx = columnMap.get(caseInsensitive(columnHeader.replace("'", "")));
        return idx == null ? -1 : idx.intValue();
    }

    /**
     * @since 3.15 beta 2
     */
    public String getSheetName() {
        return getXSSFSheet().getSheetName();
    }

    /**
     * Note: This is misleading.  The Spec indicates this is true if the totals row
     * has <b><i>ever</i></b> been shown, not whether or not it is currently displayed.
     * Use {@link #getTotalsRowCount()} &gt; 0 to decide whether or not the totals row is visible.
     * @since 3.15 beta 2
     * @see #getTotalsRowCount()
     */
    public boolean isHasTotalsRow() {
        return ctTable.getTotalsRowShown();
    }
    
    /**
     * @return 0 for no totals rows, 1 for totals row shown.
     * Values &gt; 1 are not currently used by Excel up through 2016, and the OOXML spec
     * doesn't define how they would be implemented.
     * @since 3.17 beta 1
     */
    public int getTotalsRowCount() {
        return (int) ctTable.getTotalsRowCount();
    }

    /**
     * @return 0 for no header rows, 1 for table headers shown.
     * Values &gt; 1 might be used by Excel for pivot tables?
     * @since 3.17 beta 1
     */
    public int getHeaderRowCount() {
        return (int) ctTable.getHeaderRowCount();
    }
    
    /**
     * @since 3.15 beta 2
     */
    public int getStartColIndex() {
        return getStartCellReference().getCol();
    }

    /**
     * @since 3.15 beta 2
     */
    public int getStartRowIndex() {
        return getStartCellReference().getRow();
    }

    /**
     * @since 3.15 beta 2
     */
    public int getEndColIndex() {
        return getEndCellReference().getCol();
    }

    /**
     * @since 3.15 beta 2
     */
    public int getEndRowIndex() {
        return getEndCellReference().getRow();
    }
    
    /**
     * @since 3.17 beta 1
     */
    public TableStyleInfo getStyle() {
        if (! ctTable.isSetTableStyleInfo()) return null;
        return new XSSFTableStyleInfo(((XSSFSheet) getParent()).getWorkbook().getStylesSource(), ctTable.getTableStyleInfo());
    }

    /**
     * @see org.apache.poi.ss.usermodel.Table#contains(org.apache.poi.ss.usermodel.Cell)
     * @since 3.17 beta 1
     */
    public boolean contains(CellReference cell) {
        if (cell == null) return false;
        // check if cell is on the same sheet as the table
        if ( ! getSheetName().equals(cell.getSheetName())) return false;
        // check if the cell is inside the table
        if (cell.getRow() >= getStartRowIndex()
            && cell.getRow() <= getEndRowIndex()
            && cell.getCol() >= getStartColIndex()
            && cell.getCol() <= getEndColIndex()) {
            return true;
        }
        return false;
    }
    
    /**
     * Remove relations
     */
    protected void onTableDelete() {
        for (RelationPart part : getRelationParts()) {
            removeRelation(part.getDocumentPart(), true);
        }
    }
}