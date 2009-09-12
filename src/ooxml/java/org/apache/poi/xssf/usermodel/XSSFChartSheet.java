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

import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

/**
 * High level representation of of Sheet Parts that are of type 'chartsheet'.
 *
 * TODO: current verion extends XSSFSheet although both should extend AbstractSheet
 *
 * @author Yegor Kozlov
 */
public class XSSFChartSheet extends XSSFSheet  {

    protected CTChartsheet chartsheet;

    protected XSSFChartSheet(PackagePart part, PackageRelationship rel) {
        super(part, rel);
    }

    protected void read(InputStream is) throws IOException {
        try {
            chartsheet = ChartsheetDocument.Factory.parse(is).getChartsheet();
        } catch (XmlException e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Provide access to the CTWorksheet bean holding this sheet's data
     *
     * @return the CTWorksheet bean holding this sheet's data
     */
    public CTChartsheet getCTChartsheet() {
        return chartsheet;
    }

    @Override
    protected void commit() throws IOException {

    }

}