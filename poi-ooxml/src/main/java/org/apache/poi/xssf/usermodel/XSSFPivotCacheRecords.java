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

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCacheRecords;

public class XSSFPivotCacheRecords extends POIXMLDocumentPart {
    private CTPivotCacheRecords ctPivotCacheRecords;
    
    @Beta
    public XSSFPivotCacheRecords() {
        super();
        ctPivotCacheRecords = CTPivotCacheRecords.Factory.newInstance();
    }

    /**
     * Creates an XSSFPivotCacheRecords representing the given package part and relationship.
     * Should only be called when reading in an existing file.
     *
     * @param part - The package part that holds xml data representing this pivot cache records.
     * 
     * @since POI 3.14-Beta1
     */
    @Beta
    protected XSSFPivotCacheRecords(PackagePart part) throws IOException {
        super(part);
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
    }
    
    @Beta
    protected void readFrom(InputStream is) throws IOException {
    try {
        XmlOptions options  = new XmlOptions(DEFAULT_XML_OPTIONS);
        //Removing root element
        options.setLoadReplaceDocumentElement(null);
            ctPivotCacheRecords = CTPivotCacheRecords.Factory.parse(is, options);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    @Beta
    @Internal
    public CTPivotCacheRecords getCtPivotCacheRecords() {
        return ctPivotCacheRecords;
    }

    @Beta
    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
            //Sets the pivotCacheDefinition tag
            xmlOptions.setSaveSyntheticDocumentElement(new QName(CTPivotCacheRecords.type.getName().
                    getNamespaceURI(), "pivotCacheRecords"));
            ctPivotCacheRecords.save(out, xmlOptions);
        }
    }
}