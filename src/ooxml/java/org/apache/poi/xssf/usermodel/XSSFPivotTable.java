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

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTItems;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTLocation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCacheDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRowFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheetSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STAxis;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataConsolidateFunction;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STItemType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STSourceType;

public class XSSFPivotTable extends POIXMLDocumentPart {

    protected static final short CREATED_VERSION = 3;
    protected static final short MIN_REFRESHABLE_VERSION = 3;
    protected static final short UPDATED_VERSION = 3;

    private CTPivotTableDefinition pivotTableDefinition;
    private XSSFPivotCacheDefinition pivotCacheDefinition;
    private XSSFPivotCache pivotCache;
    private XSSFPivotCacheRecords pivotCacheRecords;
    private Sheet parentSheet;
    private Sheet dataSheet;

    @Beta
    protected XSSFPivotTable() {
        super();
        pivotTableDefinition = CTPivotTableDefinition.Factory.newInstance();
        pivotCache = new XSSFPivotCache();
        pivotCacheDefinition = new XSSFPivotCacheDefinition();
        pivotCacheRecords = new XSSFPivotCacheRecords();
    }

     /**
     * Creates an XSSFPivotTable representing the given package part and relationship.
     * Should only be called when reading in an existing file.
     *
     * @param part - The package part that holds xml data representing this pivot table.
     * 
     * @since POI 3.14-Beta1
     */
    @Beta
    protected XSSFPivotTable(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    @Beta
    public void readFrom(InputStream is) throws IOException {
	try {
            XmlOptions options  = new XmlOptions(DEFAULT_XML_OPTIONS);
            //Removing root element
            options.setLoadReplaceDocumentElement(null);
            pivotTableDefinition = CTPivotTableDefinition.Factory.parse(is, options);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    @Beta
    public void setPivotCache(XSSFPivotCache pivotCache) {
        this.pivotCache = pivotCache;
    }

    @Beta
    public XSSFPivotCache getPivotCache() {
        return pivotCache;
    }

    @Beta
    public Sheet getParentSheet() {
        return parentSheet;
    }

    @Beta
    public void setParentSheet(XSSFSheet parentSheet) {
        this.parentSheet = parentSheet;
    }

    @Beta
    @Internal
    public CTPivotTableDefinition getCTPivotTableDefinition() {
        return pivotTableDefinition;
    }

    @Beta
    @Internal
    public void setCTPivotTableDefinition(CTPivotTableDefinition pivotTableDefinition) {
        this.pivotTableDefinition = pivotTableDefinition;
    }

    @Beta
    public XSSFPivotCacheDefinition getPivotCacheDefinition() {
        return pivotCacheDefinition;
    }

    @Beta
    public void setPivotCacheDefinition(XSSFPivotCacheDefinition pivotCacheDefinition) {
        this.pivotCacheDefinition = pivotCacheDefinition;
    }

    @Beta
    public XSSFPivotCacheRecords getPivotCacheRecords() {
        return pivotCacheRecords;
    }

    @Beta
    public void setPivotCacheRecords(XSSFPivotCacheRecords pivotCacheRecords) {
        this.pivotCacheRecords = pivotCacheRecords;
    }

    @Beta
    public Sheet getDataSheet() {
        return dataSheet;
    }

    @Beta
    private void setDataSheet(Sheet dataSheet) {
        this.dataSheet = dataSheet;
    }

    @Beta
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        //Sets the pivotTableDefinition tag
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTPivotTableDefinition.type.getName().
                getNamespaceURI(), "pivotTableDefinition"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        pivotTableDefinition.save(out, xmlOptions);
        out.close();
    }

    /**
     * Set default values for the table definition.
     */
    @Beta
    protected void setDefaultPivotTableDefinition() {
        //Not more than one until more created
        pivotTableDefinition.setMultipleFieldFilters(false);
        //Indentation increment for compact rows
        pivotTableDefinition.setIndent(0);
        //The pivot version which created the pivot cache set to default value
        pivotTableDefinition.setCreatedVersion(CREATED_VERSION);
        //Minimun version required to update the pivot cache
        pivotTableDefinition.setMinRefreshableVersion(MIN_REFRESHABLE_VERSION);
        //Version of the application which "updated the spreadsheet last"
        pivotTableDefinition.setUpdatedVersion(UPDATED_VERSION);
        //Titles shown at the top of each page when printed
        pivotTableDefinition.setItemPrintTitles(true);
        //Set autoformat properties
        pivotTableDefinition.setUseAutoFormatting(true);
        pivotTableDefinition.setApplyNumberFormats(false);
        pivotTableDefinition.setApplyWidthHeightFormats(true);
        pivotTableDefinition.setApplyAlignmentFormats(false);
        pivotTableDefinition.setApplyPatternFormats(false);
        pivotTableDefinition.setApplyFontFormats(false);
        pivotTableDefinition.setApplyBorderFormats(false);
        pivotTableDefinition.setCacheId(pivotCache.getCTPivotCache().getCacheId());
        pivotTableDefinition.setName("PivotTable"+pivotTableDefinition.getCacheId());
        pivotTableDefinition.setDataCaption("Values");

        //Set the default style for the pivot table
        CTPivotTableStyle style = pivotTableDefinition.addNewPivotTableStyleInfo();
        style.setName("PivotStyleLight16");
        style.setShowLastColumn(true);
        style.setShowColStripes(false);
        style.setShowRowStripes(false);
        style.setShowColHeaders(true);
        style.setShowRowHeaders(true);
    }

    protected AreaReference getPivotArea() {
        final Workbook wb = getDataSheet().getWorkbook();
        return getPivotCacheDefinition().getPivotArea(wb);
    }
    
    /**
     * Verify column index (relative to first column in pivot area) is within the
     * pivot area
     *
     * @param columnIndex
     * @throws IndexOutOfBoundsException
     */
    private void checkColumnIndex(int columnIndex) throws IndexOutOfBoundsException {
        AreaReference pivotArea = getPivotArea();
        int size = pivotArea.getLastCell().getCol() - pivotArea.getFirstCell().getCol() + 1;

        if (columnIndex < 0 || columnIndex >= size) {
            throw new IndexOutOfBoundsException("Column Index: " + columnIndex + ", Size: " + size);
        }
    }

    /**
     * Add a row label using data from the given column.
     * @param columnIndex the index of the source column to be used as row label.
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source.
     */
    @Beta
    public void addRowLabel(int columnIndex) {
        checkColumnIndex(columnIndex);
        
        AreaReference pivotArea = getPivotArea();
        final int lastRowIndex = pivotArea.getLastCell().getRow() - pivotArea.getFirstCell().getRow();
        CTPivotFields pivotFields = pivotTableDefinition.getPivotFields();

        CTPivotField pivotField = CTPivotField.Factory.newInstance();
        CTItems items = pivotField.addNewItems();

        pivotField.setAxis(STAxis.AXIS_ROW);
        pivotField.setShowAll(false);
        for (int i = 0; i <= lastRowIndex; i++) {
            items.addNewItem().setT(STItemType.DEFAULT);
        }
        items.setCount(items.sizeOfItemArray());
        pivotFields.setPivotFieldArray(columnIndex, pivotField);

        CTRowFields rowFields;
        if(pivotTableDefinition.getRowFields() != null) {
            rowFields = pivotTableDefinition.getRowFields();
        } else {
            rowFields = pivotTableDefinition.addNewRowFields();
        }

        rowFields.addNewField().setX(columnIndex);
        rowFields.setCount(rowFields.sizeOfFieldArray());
    }
    
    @Beta
    public List<Integer> getRowLabelColumns() {
        if (pivotTableDefinition.getRowFields() != null) {
            List<Integer> columnIndexes = new ArrayList<>();
            for (CTField f : pivotTableDefinition.getRowFields().getFieldArray()) {
                columnIndexes.add(f.getX());
            }
            return columnIndexes;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Add a col label using data from the given column.
     * @param columnIndex the index of the source column to be used as row label.
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source.
     * @param valueFormat format of column value (e.g. for date: "DD.MM.YYYY")
     */
    @Beta
    public void addColLabel(int columnIndex, String valueFormat) {
        checkColumnIndex(columnIndex);
        
        AreaReference pivotArea = getPivotArea();
        final int lastRowIndex = pivotArea.getLastCell().getRow() - pivotArea.getFirstCell().getRow();
        CTPivotFields pivotFields = pivotTableDefinition.getPivotFields();

        CTPivotField pivotField = CTPivotField.Factory.newInstance();
        CTItems items = pivotField.addNewItems();

        pivotField.setAxis(STAxis.AXIS_COL);
        pivotField.setShowAll(false);
        if (valueFormat != null && !valueFormat.trim().isEmpty()) {
            DataFormat df = parentSheet.getWorkbook().createDataFormat();
            pivotField.setNumFmtId(df.getFormat(valueFormat));
        }
        for (int i = 0; i <= lastRowIndex; i++) {
            items.addNewItem().setT(STItemType.DEFAULT);
        }
        items.setCount(items.sizeOfItemArray());
        pivotFields.setPivotFieldArray(columnIndex, pivotField);

        CTColFields colFields;
        if(pivotTableDefinition.getColFields() != null) {
            colFields = pivotTableDefinition.getColFields();
        } else {
            colFields = pivotTableDefinition.addNewColFields();
        }

        colFields.addNewField().setX(columnIndex);
        colFields.setCount(colFields.sizeOfFieldArray());
    }

    /**
     * Add a col label using data from the given column.
     * @param columnIndex the index of the source column to be used as row label.
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source.
     */
    @Beta
    public void addColLabel(int columnIndex) {
        addColLabel(columnIndex, null);
    }
    
    @Beta
    public List<Integer> getColLabelColumns() {
        if (pivotTableDefinition.getColFields() != null) {
            List<Integer> columnIndexes = new ArrayList<>();
            for (CTField f : pivotTableDefinition.getColFields().getFieldArray()) {
                columnIndexes.add(f.getX());
            }
            return columnIndexes;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Add a column label using data from the given column and specified function
     * @param columnIndex the index of the source column to be used as column label.
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source.
     * @param function the function to be used on the data
     * The following functions exists:
     * Sum, Count, Average, Max, Min, Product, Count numbers, StdDev, StdDevp, Var, Varp
     * @param valueFieldName the name of pivot table value field
     * @param valueFormat format of value field (e.g. "#,##0.00")
     */
    @Beta
    public void addColumnLabel(DataConsolidateFunction function, int columnIndex, String valueFieldName, String valueFormat) {
        checkColumnIndex(columnIndex);

        addDataColumn(columnIndex, true);
        addDataField(function, columnIndex, valueFieldName, valueFormat);

        // colfield should be added for the second one.
        if (pivotTableDefinition.getDataFields().getCount() == 2) {
            CTColFields colFields;
            if(pivotTableDefinition.getColFields() != null) {
                colFields = pivotTableDefinition.getColFields();
            } else {
                colFields = pivotTableDefinition.addNewColFields();
            }
            colFields.addNewField().setX(-2);
            colFields.setCount(colFields.sizeOfFieldArray());
        }
    }

    /**
     * Add a column label using data from the given column and specified function
     * @param columnIndex the index of the source column to be used as column label.
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source.
     * @param function the function to be used on the data
     * The following functions exists:
     * Sum, Count, Average, Max, Min, Product, Count numbers, StdDev, StdDevp, Var, Varp
     * @param valueFieldName the name of pivot table value field
     */
    @Beta
    public void addColumnLabel(DataConsolidateFunction function, int columnIndex, String valueFieldName) {
        addColumnLabel(function, columnIndex, valueFieldName, null);
    }

    /**
     * Add a column label using data from the given column and specified function
     * @param columnIndex the index of the source column to be used as column label
     * {@code columnIndex} is 0-based indexed and relative to the first column in the source..
     * @param function the function to be used on the data
     * The following functions exists:
     * Sum, Count, Average, Max, Min, Product, Count numbers, StdDev, StdDevp, Var, Varp
     */
    @Beta
    public void addColumnLabel(DataConsolidateFunction function, int columnIndex) {
        addColumnLabel(function, columnIndex, function.getName(), null);
    }

    /**
     * Add data field with data from the given column and specified function.
     * @param function the function to be used on the data
     *      The following functions exists:
     *      Sum, Count, Average, Max, Min, Product, Count numbers, StdDev, StdDevp, Var, Varp
     * @param columnIndex the index of the column to be used as column label.
     * @param valueFieldName the name of pivot table value field
     */
    @Beta
    private void addDataField(DataConsolidateFunction function, int columnIndex, String valueFieldName, String valueFormat) {
        checkColumnIndex(columnIndex);
        
        AreaReference pivotArea = getPivotArea();
        
        CTDataFields dataFields;
        if(pivotTableDefinition.getDataFields() != null) {
            dataFields = pivotTableDefinition.getDataFields();
        } else {
            dataFields = pivotTableDefinition.addNewDataFields();
        }
        CTDataField dataField = dataFields.addNewDataField();
        dataField.setSubtotal(STDataConsolidateFunction.Enum.forInt(function.getValue()));
        Cell cell = getDataSheet().getRow(pivotArea.getFirstCell().getRow())
                .getCell(pivotArea.getFirstCell().getCol() + columnIndex);
        cell.setCellType(CellType.STRING);
        dataField.setName(valueFieldName);
        dataField.setFld(columnIndex);
        if (valueFormat != null && !valueFormat.trim().isEmpty()) {
            DataFormat df = parentSheet.getWorkbook().createDataFormat();
            dataField.setNumFmtId(df.getFormat(valueFormat));
        }
        dataFields.setCount(dataFields.sizeOfDataFieldArray());
    }

    /**
     * Add column containing data from the referenced area.
     * @param columnIndex the index of the column containing the data
     * @param isDataField true if the data should be displayed in the pivot table.
     */
    @Beta
    public void addDataColumn(int columnIndex, boolean isDataField) {
        checkColumnIndex(columnIndex);

        CTPivotFields pivotFields = pivotTableDefinition.getPivotFields();
        CTPivotField pivotField = CTPivotField.Factory.newInstance();

        pivotField.setDataField(isDataField);
        pivotField.setShowAll(false);
        pivotFields.setPivotFieldArray(columnIndex, pivotField);
    }

    /**
     * Add filter for the column with the corresponding index and cell value
     * @param columnIndex index of column to filter on
     */
    @Beta
    public void addReportFilter(int columnIndex) {
        checkColumnIndex(columnIndex);
        
        AreaReference pivotArea = getPivotArea();
        int lastRowIndex = pivotArea.getLastCell().getRow() - pivotArea.getFirstCell().getRow();
        // check and change row of location
        CTLocation location = pivotTableDefinition.getLocation();
        AreaReference destination = new AreaReference(location.getRef(), SpreadsheetVersion.EXCEL2007);
        if (destination.getFirstCell().getRow() < 2) {
            AreaReference newDestination = new AreaReference(new CellReference(2, destination.getFirstCell().getCol()), new CellReference(
                    3, destination.getFirstCell().getCol()+1), SpreadsheetVersion.EXCEL2007);
            location.setRef(newDestination.formatAsString());
       }

        CTPivotFields pivotFields = pivotTableDefinition.getPivotFields();
        CTPivotField pivotField = CTPivotField.Factory.newInstance();
        CTItems items = pivotField.addNewItems();

        pivotField.setAxis(STAxis.AXIS_PAGE);
        pivotField.setShowAll(false);
        for(int i = 0; i <= lastRowIndex; i++) {
            items.addNewItem().setT(STItemType.DEFAULT);
        }
        items.setCount(items.sizeOfItemArray());
        pivotFields.setPivotFieldArray(columnIndex, pivotField);

        CTPageFields pageFields;
        if (pivotTableDefinition.getPageFields()!= null) {
            pageFields = pivotTableDefinition.getPageFields();
            //Another filter has already been created
            pivotTableDefinition.setMultipleFieldFilters(true);
        } else {
            pageFields = pivotTableDefinition.addNewPageFields();
        }
        CTPageField pageField = pageFields.addNewPageField();
        pageField.setHier(-1);
        pageField.setFld(columnIndex);

        pageFields.setCount(pageFields.sizeOfPageFieldArray());
        pivotTableDefinition.getLocation().setColPageCount(pageFields.getCount());
    }

    /**
     * Creates cacheSource and workSheetSource for pivot table and sets the source reference as well assets the location of the pivot table
     * @param position Position for pivot table in sheet
     * @param sourceSheet Sheet where the source will be collected from
     * @param refConfig  an configurator that knows how to configure pivot table references
     */
    @Beta
    protected void createSourceReferences(CellReference position, Sheet sourceSheet, PivotTableReferenceConfigurator refConfig){
        
        //Get cell one to the right and one down from position, add both to AreaReference and set pivot table location.
        AreaReference destination = new AreaReference(position, new CellReference(
                position.getRow()+1, position.getCol()+1), SpreadsheetVersion.EXCEL2007);

        CTLocation location;
        if(pivotTableDefinition.getLocation() == null) {
            location = pivotTableDefinition.addNewLocation();
            location.setFirstDataCol(1);
            location.setFirstDataRow(1);
            location.setFirstHeaderRow(1);
        } else {
            location = pivotTableDefinition.getLocation();
        }
        location.setRef(destination.formatAsString());
        pivotTableDefinition.setLocation(location);

        //Set source for the pivot table
        CTPivotCacheDefinition cacheDef = getPivotCacheDefinition().getCTPivotCacheDefinition();
        CTCacheSource cacheSource = cacheDef.addNewCacheSource();
        cacheSource.setType(STSourceType.WORKSHEET);
        CTWorksheetSource worksheetSource = cacheSource.addNewWorksheetSource();
        worksheetSource.setSheet(sourceSheet.getSheetName());
        setDataSheet(sourceSheet);

        refConfig.configureReference(worksheetSource);
        if (worksheetSource.getName() == null && worksheetSource.getRef() == null) throw new IllegalArgumentException("Pivot table source area reference or name must be specified.");
    }

    @Beta
    protected void createDefaultDataColumns() {
        CTPivotFields pivotFields;
        if (pivotTableDefinition.getPivotFields() != null) {
            pivotFields = pivotTableDefinition.getPivotFields();
        } else {
            pivotFields = pivotTableDefinition.addNewPivotFields();
        }
        AreaReference sourceArea = getPivotArea();
        int firstColumn = sourceArea.getFirstCell().getCol();
        int lastColumn = sourceArea.getLastCell().getCol();
        CTPivotField pivotField;
        for(int i = firstColumn; i<=lastColumn; i++) {
            pivotField = pivotFields.addNewPivotField();
            pivotField.setDataField(false);
            pivotField.setShowAll(false);
        }
        pivotFields.setCount(pivotFields.sizeOfPivotFieldArray());
    }
    
    protected static interface PivotTableReferenceConfigurator {
        
        /**
         * Configure the name or area reference for the pivot table 
         * @param wsSource CTWorksheetSource that needs the pivot source reference assignment
         */
        public void configureReference(CTWorksheetSource wsSource);
    }
}
