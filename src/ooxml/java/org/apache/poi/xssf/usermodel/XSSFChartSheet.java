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

import java.io.*;
import java.util.Map;
import java.util.HashMap;


import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTChartsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.ChartsheetDocument;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;

import javax.xml.namespace.QName;

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

    protected XSSFChartSheet(PackagePart part, PackageRelationship rel) {
        super(part, rel);
    }

    protected void read(InputStream is) throws IOException {
        //initialize the supeclass with a blank worksheet
        super.read(new ByteArrayInputStream(BLANK_WORKSHEET));

        try {
            chartsheet = ChartsheetDocument.Factory.parse(is).getChartsheet();
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
    protected void write(OutputStream out) throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(
                new QName(CTChartsheet.type.getName().getNamespaceURI(), "chartsheet"));
        Map<String, String> map = new HashMap<String, String>();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

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