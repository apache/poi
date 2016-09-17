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
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCacheFields;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCacheDefinition;

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

    /**
     * @deprecated in POI 3.14, scheduled for removal in POI 3.16
     */
    @Deprecated
    protected XSSFPivotCacheDefinition(PackagePart part, PackageRelationship rel) throws IOException {
        this(part);
    }
    
    @Beta
    public void readFrom(InputStream is) throws IOException {
	try {
            XmlOptions options  = new XmlOptions(DEFAULT_XML_OPTIONS);
            //Removing root element
            options.setLoadReplaceDocumentElement(null);
            ctPivotCacheDefinition = CTPivotCacheDefinition.Factory.parse(is, options);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
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
     * Generates a cache field for each column in the reference area for the pivot table.
     * @param sheet The sheet where the data i collected from
     */
    @Beta
    protected void createCacheFields(Sheet sheet) {
        //Get values for start row, start and end column
        AreaReference ar = new AreaReference(ctPivotCacheDefinition.getCacheSource().getWorksheetSource().getRef());
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
            Cell cell = row.getCell(i);
            cell.setCellType(CellType.STRING);
            cf.setName(row.getCell(i).getStringCellValue());
            cf.addNewSharedItems();
        }
    }
}