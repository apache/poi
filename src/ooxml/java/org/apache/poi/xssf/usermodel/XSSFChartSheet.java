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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTChartsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTLegacyDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.ChartsheetDocument;

/**
 * High level representation of Sheet Parts that are of type 'chartsheet'.
 * <p>
 *  Chart sheet is a special kind of Sheet that contains only chart and no data.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class XSSFChartSheet extends XSSFSheet  {

    private static final byte[] BLANK_WORKSHEET = blankWorksheet();

    protected CTChartsheet chartsheet;

    /**
     * @since POI 3.14-Beta1
     */
    protected XSSFChartSheet(PackagePart part) {
        super(part);
    }
    
    protected void read(InputStream is) throws IOException {
        //initialize the supeclass with a blank worksheet
        super.read(new ByteArrayInputStream(BLANK_WORKSHEET));

        try {
            chartsheet = ChartsheetDocument.Factory.parse(is, DEFAULT_XML_OPTIONS).getChartsheet();
        } catch (XmlException e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Provide access to the CTChartsheet bean holding this sheet's data
     *
     * @return the CTChartsheet bean holding this sheet's data
     */
    public CTChartsheet getCTChartsheet() {
        return chartsheet;
    }

    @Override
    protected CTDrawing getCTDrawing() {
       return chartsheet.getDrawing();
    }
    
    @Override
    protected CTLegacyDrawing getCTLegacyDrawing() {
       return chartsheet.getLegacyDrawing();
    }
    
    @Override
    protected void write(OutputStream out) throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(
                new QName(CTChartsheet.type.getName().getNamespaceURI(), "chartsheet"));
        chartsheet.save(out, xmlOptions);

    }

    private static byte[] blankWorksheet(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new XSSFSheet().write(out);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}