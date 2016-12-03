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

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;

/**
 * 
 * This class implements the Table Part (Open Office XML Part 4:
 * chapter 3.5.1)
 * 
 * This implementation works under the assumption that a table contains mappings to a subtree of an XML.
 * The root element of this subtree an occur multiple times (one for each row of the table). The child nodes
 * of the root element can be only attributes or element with maxOccurs=1 property set
 * 
 *
 * @author Roberto Manicardi
 */
public class XSSFTable extends POIXMLDocumentPart implements Table {

    private CTTable ctTable;
    private transient List<XSSFXmlColumnPr> xmlColumnPr;
    private transient CTTableColumn[] ctColumns;
    private transient HashMap<String, Integer> columnMap;
    private transient CellReference startCellReference;
    private transient CellReference endCellReference;    
    private transient String commonXPath; 


    public XSSFTable() {
        super();
        ctTable = CTTable.Factory.newInstance();
    }

    /** 
     * @since POI 3.14-Beta1
     */
    public XSSFTable(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    public void readFrom(InputStream is) throws IOException {
        try {
            TableDocument doc = TableDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctTable = doc.getTable();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    
    public XSSFSheet getXSSFSheet(){
        return (XSSFSheet) getParent();
    }

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
      * caches table columns for performance.
      * Updated via updateHeaders
      * @since 3.15 beta 2
      */
    private CTTableColumn[] getTableColumns() {
        if (ctColumns == null) {
            ctColumns = ctTable.getTableColumns().getTableColumnArray();
        }
        return ctColumns;
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
            for (CTTableColumn column : getTableColumns()) {
                if (column.getXmlColumnPr()!=null) {
                    String xpath = column.getXmlColumnPr().getXpath();
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
     * @return List of XSSFXmlColumnPr
     */
    public List<XSSFXmlColumnPr> getXmlColumnPrs() {
        
        if (xmlColumnPr==null) {
            xmlColumnPr = new ArrayList<XSSFXmlColumnPr>();
            for (CTTableColumn column: getTableColumns()) {
                if (column.getXmlColumnPr()!=null) {
                    XSSFXmlColumnPr columnPr = new XSSFXmlColumnPr(this,column,column.getXmlColumnPr());
                    xmlColumnPr.add(columnPr);
                }
            }
        }
        return xmlColumnPr;
    }
    
    /**
     * @return the name of the Table, if set
     */
    public String getName() {
        return ctTable.getName();
    }
    
    /**
     * Changes the name of the Table
     */
    public void setName(String name) {
        if (name == null) {
            ctTable.unsetName();
            return;
        }
        ctTable.setName(name);
    }

    /**
     * @return the display name of the Table, if set
     */
    public String getDisplayName() {
        return ctTable.getDisplayName();
    }

    /**
     * Changes the display name of the Table
     */
    public void setDisplayName(String name) {
        ctTable.setDisplayName(name);
    }

    /**
     * @return  the number of mapped table columns (see Open Office XML Part 4: chapter 3.5.1.4)
     */
    public long getNumberOfMappedColumns() {
        return ctTable.getTableColumns().getCount();
    }

    /**
     * @return  the number of mapped table columns (see Open Office XML Part 4: chapter 3.5.1.4)
     * @deprecated 3.15 beta 2. Use {@link #getNumberOfMappedColumns}.
     */
    public long getNumerOfMappedColumns() {
        return getNumberOfMappedColumns();
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
            String to = boundaries[1];
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
     * Thus, {@link #updateReferences()} is inexpensive.
     *
     * @since POI 3.15 beta 3
     */
    public void updateReferences() {
        startCellReference = null;
        endCellReference = null;
    }

    
    /**
     * @return the total number of rows in the selection. (Note: in this version autofiltering is ignored)
     * Returns <code>0</code> if the start or end cell references are not set.
     * 
     * Does not track updates to underlying changes to CTTable
     * To synchronize with changes to the underlying CTTable,
     * call {@link #updateReferences()}.
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
     * Synchronize table headers with cell values in the parent sheet.
     * Headers <em>must</em> be in sync, otherwise Excel will display a
     * "Found unreadable content" message on startup.
     * 
     * If calling both {@link #updateReferences()} and
     * {@link #updateHeaders()}, {@link #updateReferences()}
     * should be called first.
     */
    public void updateHeaders() {
        XSSFSheet sheet = (XSSFSheet)getParent();
        CellReference ref = getStartCellReference();
        if(ref == null) return;

        int headerRow = ref.getRow();
        int firstHeaderColumn = ref.getCol();
        XSSFRow row = sheet.getRow(headerRow);

        if (row != null && row.getCTRow().validate()) {
            int cellnum = firstHeaderColumn;
            for (CTTableColumn col : getCTTable().getTableColumns().getTableColumnArray()) {
                XSSFCell cell = row.getCell(cellnum);
                if (cell != null) {
                    col.setName(cell.getStringCellValue());
                }
                cellnum++;
            }
            ctColumns = null;
            columnMap = null;
            xmlColumnPr = null;
            commonXPath = null;
        }
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
            final int count = getTableColumns().length;
            columnMap = new HashMap<String, Integer>(count * 3 / 2);
            
            int i = 0;
            for (CTTableColumn column : getTableColumns()) {
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
     * @since 3.15 beta 2
     */
    public boolean isHasTotalsRow() {
        return ctTable.getTotalsRowShown();
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
}
