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
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCacheDefinition;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheetSource;

public class XSSFPivotCacheDefinition extends POIXMLDocumentPart{

    private CTPivotCacheDefinition ctPivotCacheDefinition;

    @Beta
    public XSSFPivotCacheDefinition(){
        super();
        ctPivotCacheDefinition = CTPivotCacheDefinition.Factory.newInstance();
        createDefaultValues();
    }

     /**
     * Creates an XSSFPivotCacheDefintion representing the given package part and relationship.
     * Should only be called when reading in an existing file.
     *
     * @param part - The package part that holds xml data representing this pivot cache definition.
     * 
     * @since POI 3.14-Beta1
     */
    @Beta
    protected XSSFPivotCacheDefinition(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    @Beta
    public void readFrom(InputStream is) throws IOException {
	try {
            XmlOptions options  = new XmlOptions(DEFAULT_XML_OPTIONS);
            //Removing root element
            options.setLoadReplaceDocumentElement(null);
            ctPivotCacheDefinition = CTPivotCacheDefinition.Factory.parse(is, options);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Beta
    @Internal
    public CTPivotCacheDefinition getCTPivotCacheDefinition() {
        return ctPivotCacheDefinition;
    }

    @Beta
    private void createDefaultValues() {
        ctPivotCacheDefinition.setCreatedVersion(XSSFPivotTable.CREATED_VERSION);
        ctPivotCacheDefinition.setMinRefreshableVersion(XSSFPivotTable.MIN_REFRESHABLE_VERSION);
        ctPivotCacheDefinition.setRefreshedVersion(XSSFPivotTable.UPDATED_VERSION);
        ctPivotCacheDefinition.setRefreshedBy("Apache POI");
        ctPivotCacheDefinition.setRefreshedDate(new Date().getTime());
        ctPivotCacheDefinition.setRefreshOnLoad(true);
    }

    @Beta
    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        //Sets the pivotCacheDefinition tag
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTPivotCacheDefinition.type.getName().
                getNamespaceURI(), "pivotCacheDefinition"));
        ctPivotCacheDefinition.save(out, xmlOptions);
        out.close();
    }

    /**
     * Find the 2D base data area for the pivot table, either from its direct reference or named table/range.
     * @return AreaReference representing the current area defined by the pivot table
     * @throws IllegalArgumentException if the ref attribute is not contiguous or the name attribute is not found.
     */
    @Beta
    public AreaReference getPivotArea(Workbook wb) throws IllegalArgumentException {
        final CTWorksheetSource wsSource = ctPivotCacheDefinition.getCacheSource().getWorksheetSource();
        
        final String ref = wsSource.getRef();
        final String name = wsSource.getName();
        
        if (ref == null && name == null) {
            throw new IllegalArgumentException("Pivot cache must reference an area, named range, or table.");
        }
        
        // this is the XML format, so tell the reference that.
        if (ref != null) {
            return new AreaReference(ref, SpreadsheetVersion.EXCEL2007);
        }
        
        assert (name != null);
        
        // named range or table?
        final Name range = wb.getName(name);
        if (range != null) {
            return new AreaReference(range.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
        }
        
        // not a named range, check for a table.
        // do this second, as tables are sheet-specific, but named ranges are not, and may not have a sheet name given.
        final XSSFSheet sheet = (XSSFSheet) wb.getSheet(wsSource.getSheet());
        for (XSSFTable table : sheet.getTables()) {
            // TODO: case-sensitive?
            if (name.equals(table.getName())) {
                return new AreaReference(table.getStartCellReference(), table.getEndCellReference(),
                        SpreadsheetVersion.EXCEL2007);
            }
        }
        
        throw new IllegalArgumentException("Name '" + name + "' was not found.");
    }
    
    /**
     * Generates a cache field for each column in the reference area for the pivot table.
     * @param sheet The sheet where the data i collected from
     */
    @Beta
    protected void createCacheFields(Sheet sheet) {
        //Get values for start row, start and end column
        AreaReference ar = getPivotArea(sheet.getWorkbook());
        CellReference firstCell = ar.getFirstCell();
        CellReference lastCell = ar.getLastCell();
        int columnStart = firstCell.getCol();
        int columnEnd = lastCell.getCol();
        Row row = sheet.getRow(firstCell.getRow());
        CTCacheFields cFields;
        if(ctPivotCacheDefinition.getCacheFields() != null) {
            cFields = ctPivotCacheDefinition.getCacheFields();
        } else {
            cFields = ctPivotCacheDefinition.addNewCacheFields();
        }
        //For each column, create a cache field and give it en empty sharedItems
        for(int i=columnStart; i<=columnEnd; i++) {
            CTCacheField cf = cFields.addNewCacheField();
            if(i==columnEnd){
                cFields.setCount(cFields.sizeOfCacheFieldArray());
            }
            //General number format
            cf.setNumFmtId(0);
            cf.setName(row.getCell(i).getStringCellValue());
            cf.addNewSharedItems();
        }
    }
}